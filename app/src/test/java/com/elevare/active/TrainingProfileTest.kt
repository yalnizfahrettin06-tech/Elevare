package com.elevare.active
import org.junit.Assert.*
import org.junit.Test

class TrainingProfileTest {
 private val valid=TrainingAnswers(18,"performance","unknown","8to10","regular",10,"clear","outdoor",3,"interval",setOf("none"))
 @Test fun everyAnswerRequired(){
  assertTrue(valid.complete())
  listOf(valid.copy(age=0),valid.copy(focus=""),valid.copy(recentGrowth=""),valid.copy(sleep=""),valid.copy(activity=""),valid.copy(minutes=0),valid.copy(safety=""),valid.copy(environment=""),valid.copy(days=0),valid.copy(runningExperience=""),valid.copy(equipment=emptySet())).forEach{assertFalse(it.complete())}
 }
 @Test fun ageAndTimeBounds(){
  (13..21).forEach{assertTrue(valid.copy(age=it).complete())}
  listOf(12,22).forEach{assertFalse(valid.copy(age=it).complete())}
  listOf(5,10,15,30).forEach{assertTrue(valid.copy(minutes=it).complete())}
  listOf(0,7,20,60).forEach{assertFalse(valid.copy(minutes=it).complete())}
 }
 @Test fun unknownDoesNotInventMedicalData(){assertTrue(valid.copy(recentGrowth="unknown",sleep="unknown",safety="unknown").complete());assertTrue(routinePlan(valid.copy(safety="unknown")).blocked)}
 @Test fun safetyStopsSessions(){
  listOf("","pain","restricted","unknown").forEach{val s=UserState(safety=it);assertEquals(s,beginWorkout(s,"runprep"))}
  val completedProfile=completeOnboarding(UserState(),valid.copy(equipment=setOf("wall")),false)
  assertNotNull(beginWorkout(completedProfile,buildVersionedWorkout("P",5).id).active)
 }
 @Test fun discomfortBlocksNextSession(){
  val a=ActiveSession("breath",remaining=0,running=false)
  val s=completeSession(UserState(safety="clear",active=a),a,"Rahatsızlık")
  assertEquals("pain",s.safety);assertFalse(trainingAllowed(s));assertEquals(s,beginWorkout(s,"flow"))
 }
 @Test fun recommendationsRespondToAnswers(){
  assertFalse(Content.workout(routinePlan(valid).workoutId).hasSprint())
  assertFalse(Content.workout(routinePlan(valid.copy(minutes=5)).workoutId).hasSprint())
  assertFalse(Content.workout(routinePlan(valid.copy(sleep="under6")).workoutId).hasSprint())
  assertFalse(Content.workout(routinePlan(valid,true).workoutId).hasSprint())
  for(minutes in ProfileChoices.minutes)for(focus in ProfileChoices.focus.keys){
   val p=routinePlan(valid.copy(minutes=minutes,focus=focus))
   assertEquals(minutes*60,Content.workout(p.workoutId).seconds)
  }
 }

 @Test fun equipmentIsExplicitAndNoneIsExclusive(){
  assertTrue(validEquipment(setOf("none")))
  assertTrue(validEquipment(setOf("chair","wall","mat")))
  assertFalse(validEquipment(emptySet()))
  assertFalse(validEquipment(setOf("none","mat")))
  assertFalse(validEquipment(setOf("treadmill")))
  assertEquals(setOf("none"),toggleEquipment(setOf("wall","mat"),"none"))
  assertEquals(setOf("mat"),toggleEquipment(setOf("none"),"mat"))
  assertEquals(emptySet<String>(),toggleEquipment(setOf("mat"),"mat"))
 }
 @Test fun privateGrowthAnswerIsValidAndNotATrainingInput(){
  val privateAnswers=valid.copy(recentGrowth="private")
  assertTrue(privateAnswers.complete())
  assertEquals(routinePlan(valid),routinePlan(privateAnswers))
 }
 @Test fun questionOrderAndCountMatchRequiredInputs(){
  assertEquals(11,ONBOARDING_QUESTION_COUNT)
  assertEquals(12,PREPARATION_STEP)
  assertEquals(13,PROGRAM_READY_STEP)
  (1..11).forEach{assertFalse(onboardingStepValid(TrainingAnswers(),it));assertTrue(onboardingStepValid(valid,it))}
  assertFalse(onboardingStepValid(valid.copy(runningExperience=""),4))
  assertFalse(onboardingStepValid(valid.copy(equipment=emptySet()),6))
  assertFalse(onboardingStepValid(valid.copy(minutes=0),8))
  assertFalse(onboardingStepValid(valid.copy(recentGrowth=""),10))
 }
 @Test fun draftsRestoreWithoutSkippingQuestions(){
  val unfinished=OnboardingDraft(valid.copy(equipment=emptySet()),13,30000).restored()
  assertEquals(6,unfinished.step)
  assertEquals(0L,unfinished.preparingElapsedMs)
  assertEquals(12,OnboardingDraft(valid,13,1200).restored().step)
  assertEquals(1200L,OnboardingDraft(valid,12,1200).restored().preparingElapsedMs)
  assertEquals(4000L,OnboardingDraft(valid,12,999999).restored().preparingElapsedMs)
  assertEquals(0,OnboardingDraft(valid,-4,-10).restored().step)
  assertEquals(0L,OnboardingDraft(valid,12,12000,7).restored().preparingElapsedMs)
 }
 @Test fun preparationCountsForegroundTimeOnly(){
  assertEquals(500L,advancePreparation(500,45000,false))
  assertEquals(750L,advancePreparation(650,100,true))
  assertEquals(4000L,advancePreparation(3900,1000,true))
  assertEquals(1200L,advancePreparation(1200,-500,true))
 }
 @Test fun completingSetupStartsOneDemoOnly(){
  val today=java.time.LocalDate.of(2026,9,8)
  val before=UserState(onboardingDraft=OnboardingDraft(valid,13,30000))
  val first=completeOnboarding(before,valid,true,1000L,today)
  assertTrue(first.ready)
  assertNull(first.onboardingDraft)
  assertEquals(8,first.onboardingVersion)
  assertEquals(1000L,first.demoStartedAt)
  assertEquals(3,first.trialDays)
  assertEquals(today.toString(),first.start)
  val repeated=completeOnboarding(first,valid,true,9999L,today.plusDays(1))
  assertEquals(first,repeated)
 }
 @Test fun skippedOrBlockedSetupDoesNotStartDemo(){
  assertEquals(0L,completeOnboarding(UserState(),valid,false,1000).demoStartedAt)
  listOf("pain","restricted","unknown").forEach{value->
   val completed=completeOnboarding(UserState(),valid.copy(safety=value),true,1000)
   assertTrue(completed.ready)
   assertEquals(0L,completed.demoStartedAt)
   assertFalse(trainingAllowed(completed))
  }
 }
 @Test fun incompleteSetupCannotOverwriteUserState(){
  val original=UserState(name="Deniz")
  assertEquals(original,completeOnboarding(original,valid.copy(runningExperience=""),true,1000))
 }
 @Test fun profileRevisionPreservesCyclePauseActiveAndHistory(){
  val existing=UserState(ready=true,start="2026-08-01",onboardingVersion=8,pausedDays=3,pausedOn="2026-09-07",active=ActiveSession("breath"),done=mapOf("2026-09-01" to setOf("runprep")),trialDays=3,demoStartedAt=1000L)
  val changed=completeOnboarding(existing,valid.copy(minutes=30,environment="indoor"),true,9999)
  assertEquals(existing.start,changed.start)
  assertEquals(existing.cycleId,changed.cycleId)
  assertEquals(existing.pausedDays,changed.pausedDays)
  assertEquals(existing.pausedOn,changed.pausedOn)
  assertEquals(existing.active,changed.active)
  assertEquals(existing.sessions,changed.sessions)
  assertEquals(existing.done,changed.done)
  assertEquals(existing.demoStartedAt,changed.demoStartedAt)
  assertEquals(30,changed.dailyMinutes)
  assertEquals("indoor",changed.environment)
 }

 @Test fun ageAppropriateSleep(){assertTrue(sleepGuide(17).contains("8–10"));assertTrue(sleepGuide(18).contains("7–9"))}
 @Test fun preparationIs30Seconds(){
  assertEquals(4000L,PREPARATION_DURATION_MS)
  assertNotEquals(preparationStage(1999),preparationStage(2000))
  assertNotEquals(preparationStage(3999),preparationStage(4000))
  assertNotEquals(preparationStage(999),preparationStage(1000))
 }
 @Test fun allMovesHaveBoundedLineGeometry(){
  val first=motionFrame("sprint",0f)
  val bones=listOf(1 to 3,3 to 4,1 to 5,5 to 6,2 to 7,7 to 8,2 to 9,9 to 10)
  fun length(f:StickFrame,b:Pair<Int,Int>)=kotlin.math.hypot(f.joints[b.first].x-f.joints[b.second].x,f.joints[b.first].y-f.joints[b.second].y)
  for(i in 0..1000){
   val f=motionFrame("sprint",i/1000f)
   f.joints.forEach{assertTrue(it.x.isFinite()&&it.y.isFinite()&&it.x in 0f..100f&&it.y in 0f..100f)}
   bones.forEach{assertEquals(length(first,it),length(f,it),.001f)}
  }
  assertEquals(first,motionFrame("sprint",1f))
  Content.moves.forEach{m->listOf(0f,.5f,1f).forEach{t->
   val p=stickFrame(m.id,t);assertEquals(11,p.joints.size)
   p.joints.forEach{assertTrue(it.x.isFinite()&&it.y.isFinite());assertTrue(it.x in 0f..100f&&it.y in 0f..100f)}
  }}
  assertEquals(stickFrame("march",0f),stickFrame("march",-1f))
  assertEquals(stickFrame("march",1f),stickFrame("march",2f))
 }
 @Test fun soundModes(){
  (-1..10).forEach{assertFalse(shouldBeep("off",it))}
  (0..3).forEach{assertTrue(shouldBeep("countdown",it))}
  assertFalse(shouldBeep("countdown",4));assertFalse(shouldBeep("every_second",-1));assertTrue(shouldBeep("every_second",10))
 }
}
