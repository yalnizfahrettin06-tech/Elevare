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
}
