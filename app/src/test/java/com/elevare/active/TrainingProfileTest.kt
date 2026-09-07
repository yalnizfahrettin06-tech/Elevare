package com.elevare.active
import org.junit.Assert.*
import org.junit.Test

class TrainingProfileTest {
 private val valid=TrainingAnswers(18,"performance","unknown","8to10","regular",10,"clear","outdoor",3)
 @Test fun everyAnswerRequired(){
  assertTrue(valid.complete())
  listOf(valid.copy(age=0),valid.copy(focus=""),valid.copy(recentGrowth=""),valid.copy(sleep=""),valid.copy(activity=""),valid.copy(minutes=0),valid.copy(safety=""),valid.copy(environment=""),valid.copy(days=0)).forEach{assertFalse(it.complete())}
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
  assertNotNull(beginWorkout(UserState(safety="clear"),"runprep").active)
 }
 @Test fun discomfortBlocksNextSession(){
  val a=ActiveSession("breath",remaining=0,running=false)
  val s=completeSession(UserState(safety="clear",active=a),a,"Rahatsızlık")
  assertEquals("pain",s.safety);assertFalse(trainingAllowed(s));assertEquals(s,beginWorkout(s,"flow"))
 }
 @Test fun recommendationsRespondToAnswers(){
  assertTrue(Content.workout(routinePlan(valid).workoutId).hasSprint())
  assertFalse(Content.workout(routinePlan(valid.copy(minutes=5)).workoutId).hasSprint())
  assertEquals("Yoga",Content.workout(routinePlan(valid.copy(sleep="under6")).workoutId).category)
  assertEquals("Yoga",Content.workout(routinePlan(valid,true).workoutId).category)
  for(minutes in ProfileChoices.minutes)for(focus in ProfileChoices.focus.keys){
   val p=routinePlan(valid.copy(minutes=minutes,focus=focus))
   assertEquals(minutes*60,Content.workout(p.workoutId).seconds)
  }
 }

 @Test fun ageAppropriateSleep(){assertTrue(sleepGuide(17).contains("8–10"));assertTrue(sleepGuide(18).contains("7–9"))}
 @Test fun preparationIs30Seconds(){
  assertEquals(30000L,PREPARATION_DURATION_MS)
  assertNotEquals(preparationStage(14999),preparationStage(15000))
  assertNotEquals(preparationStage(29999),preparationStage(30000))
  assertNotEquals(preparationStage(7499),preparationStage(7500))
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
