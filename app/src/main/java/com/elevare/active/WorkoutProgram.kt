package com.elevare.active

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class ProgramDay(val index:Int,val kind:String,val workout:Workout,val training:Boolean)
fun programReason(a:TrainingAnswers,gentle:Boolean=false):String=when{
 a.safety!="clear"->"Başlamadan önce hareket uygunluğunu bir uzmanla netleştir."
 gentle||a.sleep=="under6"->"Toparlanmaya alan açan hafif bir hafta."
 a.environment=="indoor"->"Evde güç, denge ve yoga; ekipmansız bir hafta."
 a.minutes==5->"Kısa hazırlıklarla koşu alışkanlığına ilk adım."
 a.activity=="new"->"Kısa koşular, uzun yürüyüş aralarıyla başlangıç."
 else->"Koşu günleri, güç ve toparlanma birlikte."
}
fun weeklyProgram(a:TrainingAnswers,gentle:Boolean=false):List<ProgramDay>{
 val scheduled=when(a.days){2->setOf(0,3);4->setOf(0,2,4,6);else->setOf(0,2,4)}
 val soft=gentle||a.sleep=="under6"||a.focus=="recovery"||a.safety!="clear"
 val level=if(a.age>=18&&a.activity=="regular")2 else if(a.activity=="new")0 else 1
 return (0..6).map{day->
  val training=day in scheduled
  val kind=when{
   !training->"recovery"
   soft->"yoga"
   a.environment=="indoor"->if(day==0||day==4)"strength" else "yoga"
   day==0||day==4||(a.days==2&&day==3)->if(a.minutes==5)"prep" else "run"
   a.focus=="sleep"->"yoga"
   else->"strength"
  }
  val minutes=if(training)a.minutes.takeIf{it in ProfileChoices.minutes}?:5 else 5
  ProgramDay(day,kind,buildProgramWorkout(kind,minutes,level),training)
 }
}
fun todayProgram(s:UserState,today:LocalDate=LocalDate.now()):ProgramDay {
 val day=weeklyProgram(s.answers(),s.gentle)[(journeyDay(s,today)-1)%7]
 if(day.kind!="run")return day
 val last=s.sessions.filter{it.title=="Sprint intervalleri"}.mapNotNull{runCatching{LocalDate.parse(it.date)}.getOrNull()}.maxOrNull()
 return if(last!=null&&ChronoUnit.DAYS.between(last,today) in 0..1)day.copy(kind="recovery",workout=buildProgramWorkout("recovery",5,0),training=false) else day
}
fun buildProgramWorkout(kind:String,minutes:Int,level:Int):Workout{
 require(kind in listOf("run","prep","strength","yoga","recovery"))
 require(minutes in ProfileChoices.minutes&&level in 0..2)
 val steps=mutableListOf<Step>()
 val total=minutes*60
 fun add(id:String,seconds:Int,rest:Boolean=false){if(seconds>0)steps+=Step(id,seconds,rest)}
 when(kind){
  "run"->{
   require(minutes>=10)
   // All durations include warm-up, intervals, recovery and cool-down.
   add("walk",180);add("march",60);add("step",60)
   val cycles=when(minutes){10->3;15->6;else->10}
   val fast=when(level){0->10;1->15;else->20}
   repeat(cycles){add("sprint",fast);add("walk",60-fast,true)}
   add("walk",120)
   var remaining=total-steps.sumOf{it.seconds}
   val extra=listOf("lunge","catcow","child","wall","reach")
   var i=0
   while(remaining>0){val count=minOf(60,remaining);add(extra[i++%extra.size],count);remaining-=count}
  }
  else->{
   val warm=if(minutes==5)60 else 120
   add(if(kind=="recovery")"walk" else "march",warm)
   val sequence=when(kind){
    "strength"->listOf("squat","wall","calf","walk","lunge","reach")
    "yoga"->listOf("catcow","child","lunge","reach")
    "prep"->listOf("step","calf","balance","reach")
    else->listOf("shoulder","reach","child")
   }
   var remaining=total-warm-60;var i=0
   while(remaining>0){val id=sequence[i++%sequence.size];val count=minOf(if(kind=="strength")30 else 45,remaining);add(id,count,id=="walk");remaining-=count}
   add("walk",60)
  }
 }
 val title=when(kind){"run"->"Sprint intervalleri";"prep"->"Koşuya ilk adım";"strength"->"Güç ve denge";"yoga"->"Yoga akışı";else->"Aktif toparlanma"}
 return Workout("p7_${kind}_${minutes}_$level",title,
  when(kind){"run"->"Isın · hızlan · yürü · toparlan";"yoga"->"Yavaş geçişler, rahat bir hareket aralığı";"strength"->"Kontrol, güç ve dengeli hareket";else->"Rahat adımlarla kendi ritminde"},
  when(kind){"run","prep"->"Koşu";"strength"->"Güç";"yoga"->"Yoga";else->"Toparlanma"},steps,if(kind=="run")0 else 1)
}
fun programWorkout(id:String):Workout?=runCatching{
 val parts=id.split("_");require(parts.size==4&&parts[0]=="p7")
 buildProgramWorkout(parts[1],parts[2].toInt(),parts[3].toInt())
}.getOrNull()
fun Workout.hasSprint()=steps.any{it.moveId=="sprint"}
fun Workout.heroMove()=when{hasSprint()->"sprint";category=="Yoga"->"lunge";category=="Güç"->"squat";id=="breath"->"breath";else->"walk"}
fun Workout.intervalSeconds()=if(!hasSprint())0 else steps.filter{it.moveId=="sprint"||it.rest}.sumOf{it.seconds}
fun Workout.intervalCount()=steps.count{it.moveId=="sprint"}
fun Workout.dayBrief()=if(hasSprint())"${intervalSeconds()/60} dk interval · ${intervalCount()} tur" else when(category){"Yoga"->"Nefesle birlikte yavaş geçişler";"Güç"->"Güç · denge · kontrol";else->"Rahat tempo · toparlanma"}
fun advanceSession(s:UserState,now:Long=System.currentTimeMillis()):UserState{
 val a=s.active?:return s
 val w=Content.workout(a.workoutId)
 if(!a.running||remaining(a,now)>0||a.step>=w.steps.lastIndex)return s
 val next=w.steps[a.step+1]
 return s.copy(active=a.copy(step=a.step+1,elapsed=a.elapsed+w.steps[a.step].seconds,remaining=next.seconds,deadline=now+next.seconds*1000L))
}
