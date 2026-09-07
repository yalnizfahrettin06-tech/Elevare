package com.elevare.active

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class WorkoutProgramTest {
 private val a=TrainingAnswers(18,"performance","unknown","8to10","regular",15,"clear","outdoor",3)
 @Test fun plansRespectEveryBudgetAndEnvironment(){
  for(age in listOf(13,17,18,21))for(minutes in ProfileChoices.minutes)for(env in ProfileChoices.environment.keys)for(days in ProfileChoices.days)for(activity in ProfileChoices.activity.keys){
   val week=weeklyProgram(a.copy(age=age,minutes=minutes,environment=env,days=days,activity=activity))
   assertEquals(days,week.count{it.training})
   week.forEach{d->
    assertEquals(if(d.training)minutes*60 else 300,d.workout.seconds)
    assertEquals(d.workout,Content.workout(d.workout.id))
    d.workout.steps.forEach{assertTrue(it.seconds>0);assertNotNull(Content.move(it.moveId))}
    if(env=="indoor"||minutes==5)assertFalse(d.workout.hasSprint())
   }
   (0..6).forEach{i->assertFalse(week[i].workout.hasSprint()&&week[(i+1)%7].workout.hasSprint())}
  }
 }
 @Test fun runningHasWarmupAndActualRecoveries(){
  val w=weeklyProgram(a).first().workout
  assertEquals(300,w.steps.take(3).sumOf{it.seconds})
  assertEquals(6,w.intervalCount())
  w.steps.forEachIndexed{i,step->if(step.moveId=="sprint"){assertTrue(step.seconds in 10..20);assertTrue(w.steps[i+1].rest);assertEquals(60,step.seconds+w.steps[i+1].seconds)}}
  assertTrue(w.steps.any{it.moveId=="walk"&&!it.rest&&it.seconds>=120})
 }
 @Test fun sleepAndSafetyRemoveRunning(){
  listOf(a.copy(sleep="under6"),a.copy(safety="pain"),a.copy(safety="unknown"),a.copy(focus="recovery")).forEach{answer->assertFalse(weeklyProgram(answer).any{it.workout.hasSprint()})}
  assertFalse(weeklyProgram(a,true).any{it.workout.hasSprint()})
 }
 @Test fun beginnerIntervalsAreShorter(){
  val adult=weeklyProgram(a).first().workout.steps.first{it.moveId=="sprint"}.seconds
  val young=weeklyProgram(a.copy(age=13,activity="new")).first().workout.steps.first{it.moveId=="sprint"}.seconds
  assertTrue(young<adult)
 }
 @Test fun sessionIdentitySurvivesAnswerChanges(){
  val w=weeklyProgram(a).first().workout
  val active=ActiveSession(w.id,remaining=0,running=true,deadline=100)
  val state=UserState(safety="clear",active=active,dailyMinutes=5)
  val advanced=advanceSession(state,1000).active!!
  assertEquals(1,advanced.step);assertEquals(w.steps[1].seconds,advanced.remaining)
  assertEquals(w,Content.workout(advanced.workoutId))
 }
 @Test fun noTransitionBeforeDeadlineOrWhilePaused(){
  val state=UserState(active=ActiveSession("p7_run_15_2",remaining=180,deadline=5000))
  assertEquals(state,advanceSession(state,4000))
  val paused=state.copy(active=state.active!!.copy(running=false,remaining=0))
  assertEquals(paused,advanceSession(paused,9000))
 }
 @Test fun cannotStartAnotherRunAfterRecentRun(){
  val today=LocalDate.now()
  val s=UserState(age=18,safety="clear",environment="outdoor",trainingDays=3,dailyMinutes=15,focus="performance",sleepHabit="8to10",activityHabit="regular",
   sessions=listOf(SessionLog("old","Sprint intervalleri",today.minusDays(1).toString(),900,"İyi")))
  assertFalse(todayProgram(s).workout.hasSprint())
  assertEquals(s,beginWorkout(s,"p7_run_15_2"))
 }
 @Test fun lastStepDoesNotLoop(){
  val w=buildProgramWorkout("run",15,2)
  val s=UserState(active=ActiveSession(w.id,step=w.steps.lastIndex,remaining=0,deadline=1))
  assertEquals(s,advanceSession(s,100))
 }
 @Test fun malformedGeneratedIdsDoNotCrashStart(){
  listOf("p7_run_5_2","p7_run_999_2","p7_run_10_9","p7_unknown_10_1","p7_run_x_1").forEach{assertNull(programWorkout(it));val s=UserState(safety="clear");assertEquals(s,beginWorkout(s,it))}
 }
}
