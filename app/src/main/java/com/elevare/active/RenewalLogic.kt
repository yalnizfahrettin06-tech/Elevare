package com.elevare.active

import java.time.LocalDate

fun removeSleep(s:UserState,date:String)=s.copy(
 sleeps=s.sleeps.filterNot{it.date==date},
 done=s.done.mapValues{(key,value)->if(key==date||key.startsWith("plan:"))value-"sleep" else value})

fun lastSevenWorkouts(s:UserState,today:LocalDate=LocalDate.now())=s.sessions.count{
 it.completed&&it.type=="workout"&&it.date>=today.minusDays(6).toString()&&it.date<=today.toString()}

fun progressRecords(s:UserState,scope:String,type:String="all",today:LocalDate=LocalDate.now())=s.sessions.filter{
 (scope!="cycle"||it.cycleId==s.cycleId) &&
 (scope!="week"||(it.date>=today.minusDays(6).toString()&&it.date<=today.toString())) &&
 (type=="all"||if(type=="partial")!it.completed else it.type==type&&it.completed)
}.sortedWith(compareByDescending<SessionLog>{it.date}.thenByDescending{it.completedAtEpochMs})

fun applyPlanPreferences(s:UserState,a:TrainingAnswers):UserState {
 if(s.active!=null||!s.ready||!a.complete())return s
 return completeOnboarding(s,a,false).copy(progressionAccepted=false,progressionConsents=emptyList(),
  dailyCheckDate="",dailyEnvironment="",dailyEquipmentConfirmed=false,
  // Editing a time preference cannot dismiss a symptom review.
  dailyReadiness=if(s.dailyReadiness=="pain")"pain" else "")
}

fun reviewMovementSafety(s:UserState,clear:Boolean):UserState = if(!clear)s else
 s.copy(dailyReadiness="",dailyCheckDate="",dailyEquipmentConfirmed=false)

fun workoutDistribution(w:Workout):String {
 val walk=w.steps.filter{it.moveId=="walk"}.sumOf{it.seconds}
 val other=w.seconds-walk
 return "${minutesText(walk)} yürüyüş · ${minutesText(other)} diğer adımlar"
}

fun planChangeSummary(s:UserState,a:TrainingAnswers,today:LocalDate=LocalDate.now()):List<String> {
 val next=applyPlanPreferences(s,a)
 val first=programElapsedDay(s,today).coerceIn(1,90)
 return (first..minOf(first+6,90)).mapNotNull{day->
  val before=programForDay(s,day,today);val after=programForDay(next,day,today)
  fun label(p:ProgramDay)=if(p.training)"${p.workout.title} · ${minutesText(p.workout.seconds)}" else "Toparlanma"
  if(label(before)==label(after))null else "Gün $day: ${label(before)} → ${label(after)}"
 }.ifEmpty{listOf("Önümüzdeki 7 günün akışı aynı kalıyor; yanıt tercihin güncellenir.")}
}

fun growthLearningNote(s:UserState)=when(s.recentGrowth){
 "yes"->"Ölçüm değişimini anlamak: tek bir değişiklik nedenini veya gelecekteki boyunu göstermez."
 "no"->"Ölçümleri yorumlamak: fark görmemek tek başına gelişimin hakkında bir sonuç vermez."
 else->"Büyümeyi anlamak: araştırmaların ölçtüğü sonuç ile kişisel sonuç aynı şey değildir."
}
