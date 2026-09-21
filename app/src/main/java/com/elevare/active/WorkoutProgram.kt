package com.elevare.active

import java.time.LocalDate
import java.time.temporal.ChronoUnit

// Kept byte-for-byte in its timing/identity contract for already saved p7 sessions.
// New plans use buildVersionedWorkout; changing a p7 recipe would rewrite history.
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
 val parts=id.split("_");require(parts.size==4)
 val workout=when(parts[0]){
  "p7"->buildProgramWorkout(parts[1],parts[2].toInt(),parts[3].toInt())
  "p8"->buildVersionedWorkout(parts[1],parts[2].toInt(),parts[3].toInt())
  else->error("Unknown program version")
 }
 require(workout.id==id)
 workout
}.getOrNull()
fun Workout.hasSprint()=steps.any{it.moveId=="sprint"}
fun Workout.heroMove()=when{
 hasSprint()->"sprint"
 category=="Yoga"->steps.firstOrNull{it.moveId in setOf("catcow","child","lunge")}?.moveId?:"walk"
 category=="Güç"->steps.firstOrNull{it.moveId in setOf("squat","wall","calf","balance")}?.moveId?:"walk"
 steps.all{it.moveId=="breath"}->"breath"
 else->"walk"
}
fun Workout.intervalSeconds()=steps.mapIndexed{i,step->
 if(step.moveId=="sprint")step.seconds+(steps.getOrNull(i+1)?.takeIf{it.rest&&it.moveId=="walk"}?.seconds?:0) else 0
}.sum()
fun Workout.intervalCount()=steps.count{it.moveId=="sprint"}
fun Workout.dayBrief()=if(hasSprint())"${intervalSeconds()/60} dk interval · ${intervalCount()} tur" else when(category){"Yoga"->"Nefesle birlikte yavaş geçişler";"Güç"->"Güç · denge · kontrol";else->"Rahat tempo · toparlanma"}
fun advanceSession(s:UserState,now:Long=System.currentTimeMillis()):UserState{
 val a=s.active?:return s
 val w=activeWorkout(a)
 if(!a.running||remaining(a,now)>0||a.step>=w.steps.lastIndex)return s
 val next=w.steps[a.step+1]
 return s.copy(active=a.copy(step=a.step+1,elapsed=a.elapsed+w.steps[a.step].seconds,remaining=next.seconds,deadline=now+next.seconds*1000L))
}
