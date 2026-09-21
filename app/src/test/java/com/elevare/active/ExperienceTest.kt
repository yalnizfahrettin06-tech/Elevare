package com.elevare.active
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class ExperienceTest {
 private val today=LocalDate.of(2026,9,21)
 @Test fun missingDaysAreNotSkippedDays(){assertNull(routineSuggestion(addRoutine(LifeState(),"morning"),today))}
 @Test fun severalSkippedStepsCountAsOneDay(){assertNull(routineSuggestion(skipRoutine(addRoutine(LifeState(),"morning"),"morning",today),today))}
 @Test fun threeDistinctDaysProduceEvidence(){var life=addRoutine(LifeState(),"morning");repeat(3){life=skipRoutine(life,"morning",today.minusDays(it.toLong()))};assertEquals(3,routineSuggestion(life,today)!!.skippedDays)}
 @Test fun oldSkipsAreNotUsed(){val life=skipRoutine(addRoutine(LifeState(),"morning"),"morning",today.minusDays(8));assertNull(routineSuggestion(life,today))}
 @Test fun disabledRoutineIsNotSuggested(){var life=addRoutine(LifeState(),"morning");repeat(3){life=skipRoutine(life,"morning",today.minusDays(it.toLong()))};assertNull(routineSuggestion(life.copy(plans=life.plans.map{it.copy(enabled=false)}),today))}
 @Test fun partialSetupCannotChangePlan(){val s=UserState();assertEquals(s,applyTimePreference(s,10))}
 @Test fun invalidDurationCannotChangePlan(){val s=UserState();assertEquals(s,applyTimePreference(s,8))}
 @Test fun chapterLanguageDoesNotClaimMeasuredHealth(){assertTrue(chapterPurpose(90).contains("kayıtlarını"));assertEquals(chapterPurpose(1),chapterPurpose(0))}
 @Test fun reasonsShowActualChoices(){val a=TrainingAnswers(minutes=15,days=3,environment="indoor");assertTrue(planReasons(a)[0].contains("15"));assertTrue(planReasons(a)[1].contains("3"));assertTrue(planReasons(a)[2].contains("planlanmaz"))}
 private fun ready()=completeOnboarding(UserState(),TrainingAnswers(18,"performance","unknown","8to10","regular",15,"clear","outdoor",3,"interval",setOf("none")),true,1000L,today)
 @Test fun timePreferencePreservesHistoryCycleAndDemo(){
  val s=ready().copy(life=skipRoutine(addRoutine(LifeState(),"morning"),"morning",today))
  val changed=applyTimePreference(s,10)
  assertEquals(10,changed.dailyMinutes);assertEquals(s.start,changed.start);assertEquals(s.demoStartedAt,changed.demoStartedAt);assertEquals(s.life,changed.life);assertEquals(s.sessions,changed.sessions)
 }
 @Test fun activeSessionBlocksPreferenceChange(){val s=ready().copy(active=ActiveSession("breath",remaining=10,running=false));assertEquals(s,applyTimePreference(s,10))}
 @Test fun unchangedPreferenceIsNoOp(){val s=ready().copy(progressionAccepted=true);assertEquals(s,applyTimePreference(s,15))}
 @Test fun lastDayRemainsOpenUntilFollowingDay(){val s=ready().copy(start=today.minusDays(89).toString());assertFalse(isCycleComplete(s,today));assertEquals(s,nextCycle(s,today));assertTrue(isCycleComplete(s,today.plusDays(1)))}
 @Test fun pauseOnLastDayDoesNotCloseCycle(){val s=ready().copy(start=today.minusDays(89).toString(),pausedOn=today.toString());assertFalse(isCycleComplete(s,today.plusDays(10)))}
 @Test fun restDayPreviewSelectsNextTrainingDay(){val s=ready().copy(start=today.minusDays(1).toString());assertFalse(todayProgram(s,today).training);val p=nextTrainingPreview(applyTimePreference(s,10),today)!!;assertTrue(p.training);assertTrue(p.day>2);assertEquals(600,p.workout.seconds)}
 @Test fun closedAndPausedPlansHaveNoTimePreview(){assertNull(nextTrainingPreview(ready().copy(pausedOn=today.toString()),today));assertNull(nextTrainingPreview(ready().copy(start=today.minusDays(90).toString()),today))}
 @Test fun disabledRoutineRejectsNewRecordsButAllowsUndo(){
  val life=recordRoutine(addRoutine(LifeState(),"morning"),"morning","priority","done",today)
  val off=life.copy(plans=life.plans.map{it.copy(enabled=false)})
  assertEquals(off,recordRoutine(off,"morning","ready","done",today))
  assertNull(routineStatus(recordRoutine(off,"morning","priority",null,today),"morning","priority",today))
 }
 @Test fun nonScheduledRoutineRejectsNewRecords(){val life=addRoutine(LifeState(),"morning").let{it.copy(plans=it.plans.map{p->p.copy(days=setOf(2))})};assertEquals(life,recordRoutine(life,"morning","priority","done",today))}
 @Test fun catalogReactivationDoesNotReenableNotifications(){val life=addRoutine(LifeState(),"morning").let{it.copy(plans=it.plans.map{p->p.copy(enabled=false,remind=true)})};val restored=addRoutine(life,"morning");assertTrue(restored.plans.single().enabled);assertFalse(restored.plans.single().remind)}
}
