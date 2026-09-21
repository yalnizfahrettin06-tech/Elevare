package com.elevare.active

import java.time.LocalDate

/** Values are stable storage keys, not medical findings or inferred hormone levels. */
data class TrainingAnswers(
 val age:Int=0,
 val focus:String="",
 val recentGrowth:String="",
 val sleep:String="",
 val activity:String="",
 val minutes:Int=0,
 val safety:String="",
 val environment:String="",
 val days:Int=0,
 val runningExperience:String="",
 val equipment:Set<String> = emptySet()
)

const val TRAINING_ONBOARDING_VERSION=8
const val ONBOARDING_QUESTION_COUNT=11
const val PREPARATION_STEP=12
const val PROGRAM_READY_STEP=13
const val PREPARATION_DURATION_MS=30_000L

data class OnboardingDraft(
 val answers:TrainingAnswers=TrainingAnswers(),
 val step:Int=0,
 val preparingElapsedMs:Long=0L,
 val version:Int=TRAINING_ONBOARDING_VERSION
)

object ProfileChoices {
 val ages=13..21
 val minutes=listOf(5,10,15,30)
 val focus=linkedMapOf("performance" to "Güç ve kondisyon","recovery" to "Esneklik ve toparlanma","sleep" to "Enerji ve uyku","growth" to "Büyümeyi anlamak")
 val growth=linkedMapOf("yes" to "Ölçümde artış var","no" to "Artış fark etmedim","unknown" to "Ölçmedim / bilmiyorum","private" to "Belirtmek istemiyorum")
 val sleep=linkedMapOf("under6" to "6 saatten az","6to8" to "6–8 saat","8to10" to "8–10 saat","over10" to "10 saatten fazla","unknown" to "Değişiyor / bilmiyorum")
 val activity=linkedMapOf("new" to "Yeni başlıyorum","some" to "Haftada 1–2 gün","regular" to "Haftada 3 gün veya daha fazla")
 val runningExperience=linkedMapOf("new" to "Yeni başlıyorum","easy" to "Rahat tempoda koşuyorum","interval" to "Düzenli interval deneyimim var","unknown" to "Emin değilim")
 val environment=linkedMapOf("indoor" to "Evde","outdoor" to "Dışarıda","both" to "Hem evde hem dışarıda")
 val equipment=linkedMapOf("chair" to "Sağlam sandalye","wall" to "Duvar veya sabit destek","mat" to "Mat / uygun yumuşak yüzey","none" to "Hiçbiri")
 val days=listOf(2,3,4)
 val safety=linkedMapOf("clear" to "Hayır, bildiğim bir engel yok","pain" to "Ağrı / rahatsızlık var","restricted" to "Bir uzman hareketimi kısıtladı","unknown" to "Emin değilim")
}

fun validEquipment(selected:Set<String>)=selected.isNotEmpty() && selected.all{it in ProfileChoices.equipment} && ("none" !in selected || selected.size==1)

fun toggleEquipment(selected:Set<String>,option:String):Set<String> = when {
 option !in ProfileChoices.equipment -> selected
 option=="none" -> if(selected==setOf("none")) emptySet() else setOf("none")
 option in selected -> selected-option
 else -> (selected-"none")+option
}

fun onboardingStepValid(answers:TrainingAnswers,step:Int):Boolean=with(answers){when(step){
 0->true
 1->age in ProfileChoices.ages
 2->focus in ProfileChoices.focus
 3->activity in ProfileChoices.activity
 4->runningExperience in ProfileChoices.runningExperience
 5->environment in ProfileChoices.environment
 6->validEquipment(equipment)
 7->days in ProfileChoices.days
 8->minutes in ProfileChoices.minutes
 9->sleep in ProfileChoices.sleep
 10->recentGrowth in ProfileChoices.growth
 11->safety in ProfileChoices.safety
 else->(1..ONBOARDING_QUESTION_COUNT).all{onboardingStepValid(this,it)}
}}

fun TrainingAnswers.complete()=(1..ONBOARDING_QUESTION_COUNT).all{onboardingStepValid(this,it)}

/** A malformed or old draft cannot skip required questions or manufacture completion. */
fun OnboardingDraft.restored():OnboardingDraft {
 val safeStep=step.coerceIn(0,PROGRAM_READY_STEP)
 val invalid=(1..ONBOARDING_QUESTION_COUNT).firstOrNull{!onboardingStepValid(answers,it)}
 val resumeStep=if(invalid!=null && invalid<safeStep)invalid else safeStep
 val elapsed=if(resumeStep>=PREPARATION_STEP && version==TRAINING_ONBOARDING_VERSION)preparingElapsedMs.coerceIn(0,PREPARATION_DURATION_MS) else 0L
 return copy(step=if(resumeStep==PROGRAM_READY_STEP && elapsed<PREPARATION_DURATION_MS)PREPARATION_STEP else resumeStep,preparingElapsedMs=elapsed,version=TRAINING_ONBOARDING_VERSION)
}

fun UserState.answers()=TrainingAnswers(age,focus,recentGrowth,sleepHabit,activityHabit,dailyMinutes,safety,environment,trainingDays,runningExperience,equipment)
fun focusLabel(value:String)=ProfileChoices.focus[value]?:"Antrenman ritmim"
fun trainingAllowed(s:UserState)=s.safety=="clear"
fun sleepGuide(age:Int)=if(age<18)"Gençler için genel aralık: 8–10 saat." else "Genç yetişkinler için genel aralık: 7–9 saat."

/** Profile revision changes future choices, never the existing cycle or session history. */
fun completeOnboarding(s:UserState,answers:TrainingAnswers,startDemo:Boolean,nowMillis:Long=System.currentTimeMillis(),today:LocalDate=LocalDate.now()):UserState {
 if(!answers.complete())return s
 val firstSetup=!s.ready
 val beginDemo=firstSetup && startDemo && answers.safety=="clear" && s.demoStartedAt==0L
 return s.copy(
  ready=true,
  start=if(firstSetup)today.toString() else s.start,
  onboardingVersion=TRAINING_ONBOARDING_VERSION,
  age=answers.age,focus=answers.focus,recentGrowth=answers.recentGrowth,sleepHabit=answers.sleep,
  activityHabit=answers.activity,dailyMinutes=answers.minutes,safety=answers.safety,
  environment=answers.environment,trainingDays=answers.days,runningExperience=answers.runningExperience,
  equipment=answers.equipment,dark=true,onboardingDraft=null,
  trialDays=if(beginDemo)TRIAL_DAYS else s.trialDays,
  demoStartedAt=if(beginDemo)nowMillis.coerceAtLeast(1L) else s.demoStartedAt
 )
}

data class RoutinePlan(val workoutId:String,val headline:String,val note:String,val optionalMinutes:Int,val blocked:Boolean)
fun routinePlan(a:TrainingAnswers,gentle:Boolean=false):RoutinePlan {
 val w=weeklyProgram(a,gentle).first().workout
 return RoutinePlan(w.id,w.title,programReason(a,gentle),0,a.safety!="clear")
}

fun advancePreparation(elapsed:Long,foregroundDeltaMs:Long,resumed:Boolean):Long {
 val safeElapsed=elapsed.coerceIn(0,PREPARATION_DURATION_MS)
 return if(resumed)(safeElapsed+foregroundDeltaMs.coerceIn(0,PREPARATION_DURATION_MS)).coerceAtMost(PREPARATION_DURATION_MS) else safeElapsed
}

fun preparationStage(elapsed:Long)=when {
 elapsed<7_500L->"Tercihlerin değerlendiriliyor"
 elapsed<15_000L->"Antrenman günlerin düzenleniyor"
 elapsed<22_500L->"Hareket listen oluşturuluyor"
 elapsed<PREPARATION_DURATION_MS->"İlk haftan hazırlanıyor"
 else->"Programın hazır"
}
