package com.elevare.active

import java.time.*

data class RoutineStep(val id:String,val title:String,val detail:String)
data class RoutineTemplate(val id:String,val title:String,val slot:String,val minutes:Int,val summary:String,val steps:List<RoutineStep>)
data class LifeRoutinePlan(val id:String,val enabled:Boolean=true,val days:Set<Int> = (1..7).toSet(),val time:String="20:30",val remind:Boolean=false,val revision:Long=1)
data class WeekReflection(val load:String,val next:String)
data class LifeState(
 val welcomed:Boolean=false,val plans:List<LifeRoutinePlan> = emptyList(),val entries:Map<String,String> = emptyMap(),
 val quietEnabled:Boolean=false,val quietStart:String="08:00",val quietEnd:String="16:00",val budget:Int=2,
 val reflections:Map<String,WeekReflection> = emptyMap()
)
object LifeCatalog {
 val all=listOf(
  RoutineTemplate("morning","Güne yer aç","morning",2,"Güne küçük bir hazırlıkla başla.",listOf(
   RoutineStep("priority","Bugünün bir önceliğini seç","Her şeyi yetiştirmek zorunda değilsin. Bugün önem verdiğin tek bir şeyi düşün."),
   RoutineStep("ready","Gün için hazırlan","Çantan, kıyafetlerin veya çalışma alanın. Sana uyan küçük hazırlığı yap."))),
  RoutineTemplate("evening","Günü kapat","evening",3,"Yarın için hazırlan, akşamı sadeleştir.",listOf(
   RoutineStep("tomorrow","Yarın için bir şeyi hazırla","Sabahını kolaylaştıracak küçük bir hazırlık yeterli."),
   RoutineStep("screen","Ekrana kısa bir ara ver","Uygun olduğunda telefonu bırakıp sakin bir etkinliğe geç. Bu adımı kaydedip uygulamayı kapatabilirsin."))),
  RoutineTemplate("water","Su molası","day",1,"Kendi seçtiğin küçük bir hatırlatma.",listOf(
   RoutineStep("pause","Su molanı ver","Sana uygun şekilde. Litre hedefi veya içme zorunluluğu yok; varsa sağlık uzmanının sıvı planını izle."))),
  RoutineTemplate("meal","Öğün hazırlığı","day",3,"Bir sonraki öğününü kolaylaştır.",listOf(
   RoutineStep("prepare","Bir sonraki öğününü düşün","Erişebildiğin yiyeceklerden sana uygun bir seçenek hazırla veya planla. Kalori ve kilo hedefi yok."))),
  RoutineTemplate("care","Kendine bakım","evening",3,"Kendi basit bakım düzenine alan aç.",listOf(
   RoutineStep("personal","Mevcut bakım rutinine zaman ayır","Zaten kullandığın, sana uygun basit bakımını yap. Yeni ürün satın alman gerekmez."))),
  RoutineTemplate("focus","Tek işe alan aç","day",2,"Küçük bir odak başlangıcı.",listOf(
   RoutineStep("choose","Tek bir iş seç","Ders, okuma veya kişisel bir iş. Başlamak istediğin küçük kısmı seç."),
   RoutineStep("space","Dikkatini vereceğin alanı hazırla","İhtiyacın olmayan şeyleri kenara al. Hazırlığını kaydet ve kendi süren boyunca işine geç; burada odak süresi ölçülmez.")))
 )
 fun find(id:String)=all.firstOrNull{it.id==id}
 fun defaultTime(id:String)=when(find(id)?.slot){"morning"->"07:30";"evening"->"20:30";else->"16:30"}
}
fun routineKey(date:LocalDate,id:String,step:String)="$date|$id|$step"
fun routineStatus(life:LifeState,id:String,step:String,date:LocalDate)=life.entries[routineKey(date,id,step)]
fun routineRecorded(life:LifeState,id:String,date:LocalDate)=LifeCatalog.find(id)?.steps?.all{routineStatus(life,id,it.id,date)!=null}?:false
fun routineDoneCount(life:LifeState,id:String,date:LocalDate)=LifeCatalog.find(id)?.steps?.count{routineStatus(life,id,it.id,date)=="done"}?:0
fun routineDue(plan:LifeRoutinePlan,date:LocalDate)=plan.enabled&&date.dayOfWeek.value in plan.days
fun addRoutine(life:LifeState,id:String):LifeState {
 if(LifeCatalog.find(id)==null)return life
 val old=life.plans.find{it.id==id}
 return life.copy(welcomed=true,plans=if(old==null)life.plans+LifeRoutinePlan(id,time=LifeCatalog.defaultTime(id)) else life.plans.map{if(it.id==id)it.copy(enabled=true,revision=it.revision+1)else it})
}
fun recordRoutine(life:LifeState,id:String,step:String,status:String?,date:LocalDate):LifeState {
 if(LifeCatalog.find(id)?.steps?.none{it.id==step}!=false || life.plans.none{it.id==id} || status !in setOf(null,"done","skip"))return life
 val key=routineKey(date,id,step)
 return life.copy(entries=if(status==null)life.entries-key else life.entries+(key to status))
}
fun skipRoutine(life:LifeState,id:String,date:LocalDate):LifeState {
 var result=life
 LifeCatalog.find(id)?.steps?.filter{routineStatus(result,id,it.id,date)==null}?.forEach{result=recordRoutine(result,id,it.id,"skip",date)}
 return result
}
fun deleteRoutine(life:LifeState,id:String)=life.copy(plans=life.plans.filterNot{it.id==id},entries=life.entries.filterKeys{it.split('|').getOrNull(1)!=id})
fun visibleRoutines(life:LifeState,date:LocalDate,hour:Int):List<LifeRoutinePlan> {
 val slot=if(hour<12)"morning" else if(hour<18)"day" else "evening"
 return life.plans.filter{routineDue(it,date)}.sortedWith(compareBy<LifeRoutinePlan>{routineRecorded(life,it.id,date)}.thenBy{LifeCatalog.find(it.id)?.slot!=slot}.thenBy{it.time}).take(2)
}
fun reflectionWeek(date:LocalDate)=date.with(DayOfWeek.MONDAY).toString()
fun lifeWeekCount(life:LifeState,today:LocalDate):Int=life.entries.count{(key,value)->
 val d=runCatching{LocalDate.parse(key.substringBefore('|'))}.getOrNull()
 value=="done"&&d!=null&&d>=today.minusDays(6)&&d<=today
}
fun lifeQuiet(life:LifeState,time:LocalTime)=life.quietEnabled&&isQuietTime(time,life.quietStart,life.quietEnd)
fun nextRoutineAt(plan:LifeRoutinePlan,now:ZonedDateTime):ZonedDateTime? {
 if(!plan.enabled||!plan.remind)return null
 val time=runCatching{LocalTime.parse(plan.time)}.getOrNull()?:return null
 return (0..7).map{now.toLocalDate().plusDays(it.toLong())}.filter{routineDue(plan,it)}
  .map{it.atTime(time).atZone(now.zone)}.firstOrNull{it.isAfter(now)}
}
fun canDeliverLife(s:UserState,id:String,revision:Long,scheduled:Long,now:ZonedDateTime):Boolean {
 val plan=s.life.plans.find{it.id==id}?:return false
 val age=now.toInstant().toEpochMilli()-scheduled
 return s.ready&&s.onboardingVersion>=TRAINING_ONBOARDING_VERSION&&s.active==null&&plan.remind&&plan.revision==revision&&
  routineDue(plan,now.toLocalDate())&&scheduled>0&&age in 0..90*60*1000L&&
  Instant.ofEpochMilli(scheduled).atZone(now.zone).toLocalDate()==now.toLocalDate()&&
  !routineRecorded(s.life,id,now.toLocalDate())&&!lifeQuiet(s.life,now.toLocalTime())&&!isQuietTime(now.toLocalTime(),s.bed,s.wake)
}
fun deliveryBudgetAllows(count:Int,lastEpoch:Long,nowEpoch:Long,budget:Int)=
 count<budget.coerceIn(1,2)&&(lastEpoch<=0||nowEpoch-lastEpoch>=3*60*60*1000L)
