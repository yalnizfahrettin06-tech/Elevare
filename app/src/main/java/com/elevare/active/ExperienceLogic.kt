package com.elevare.active

import java.time.LocalDate

fun planReasons(a:TrainingAnswers)=listOf(
 "${a.minutes} dakika seçtin → antrenman süresi bu tercihle düzenlendi.",
 "Haftada ${a.days} gün seçtin → aralara toparlanma günleri yerleştirildi.",
 when(a.environment){"indoor"->"Evde çalışmayı seçtin → dış mekân koşusu planlanmaz.";"outdoor"->"Dışarıda çalışmayı seçtin → ortam uygunluğu seans öncesi tekrar sorulur.";else->"İki ortamı seçtin → seans öncesi bugünkü alanını doğrulayabilirsin."}
)

data class RoutineSuggestion(val id:String,val skippedDays:Int,val title:String)
fun routineSuggestion(life:LifeState,today:LocalDate):RoutineSuggestion? = life.plans.filter{it.enabled}.mapNotNull{p->
 val days=(0L..6L).count{offset->val d=today.minusDays(offset);LifeCatalog.find(p.id)?.steps?.any{routineStatus(life,p.id,it.id,d)=="skip"}==true}
 if(days>=3)RoutineSuggestion(p.id,days,LifeCatalog.find(p.id)!!.title)else null
}.maxByOrNull{it.skippedDays}

/** Explicit confirmation only; no load increase, reset, or active snapshot mutation. */
fun applyTimePreference(s:UserState,minutes:Int):UserState {
 if(!s.ready||minutes !in ProfileChoices.minutes||minutes==s.dailyMinutes||s.active!=null||!s.answers().complete())return s
 return completeOnboarding(s,s.answers().copy(minutes=minutes),false).copy(
  progressionAccepted=false,progressionConsents=emptyList(),dailyCheckDate="",dailyReadiness="",dailyEquipmentConfirmed=false)
}
fun weekCompleted(s:UserState,today:LocalDate)=s.sessions.count{
 it.completed&&it.type=="workout"&&it.date>=today.with(java.time.DayOfWeek.MONDAY).toString()&&it.date<=today.toString()
}
/** Preview the next actual training day, not a rest-day placeholder. */
fun nextTrainingPreview(s:UserState,today:LocalDate):ProgramDay? {
 if(s.pausedOn!=null||isCycleComplete(s,today))return null
 return (programElapsedDay(s,today)..PROGRAM_LENGTH).asSequence()
  .map{programForDay(s,it,today)}.firstOrNull{it.training}
}
fun chapterPurpose(day:Int)=when(day.coerceIn(1,90)){
 in 1..14->"Hareketleri tanı, sana uyan günleri bul."
 in 15..28->"Takvimindeki ritmi koru; gerekirse sadeleştir."
 in 29..49->"Kayıtlarını gözden geçir, uygun adımı bilinçli seç."
 in 50..77->"Sürdürebildiğin düzeni pekiştir."
 else->"90 günün kayıtlarını değerlendir; devamını sen seç."
}
