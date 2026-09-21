package com.elevare.active

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class ProgramCycleTest {
 private val today=LocalDate.of(2026,9,8)
 private fun state(days:Int=3,minutes:Int=15,environment:String="outdoor")=UserState(
  ready=true,start=today.toString(),age=16,safety="clear",environment=environment,trainingDays=days,
  dailyMinutes=minutes,focus="performance",sleepHabit="8to10",activityHabit="new",
  recentGrowth="unknown",runningExperience="new",equipment=setOf("chair","wall","mat"),cycleId="test-cycle")
 private val reviewed=ProgramReviewApprovals(running=true,strength=true,yoga=true,combined=true,reviewerRecord="TEST FIXTURE ONLY")
 private fun log(s:UserState,code:String,day:Int,daysAgo:Long=3,feeling:String="Uygundu",complete:Boolean=true):SessionLog {
  val w=buildVersionedWorkout(code,15)
  return SessionLog("test-$day",w.title,today.minusDays(daysAgo).toString(),w.seconds,feeling,"workout",
   w.id,cycleId=s.cycleId,completed=complete,programDay=day)
 }
 private fun datedLog(s:UserState,code:String,day:Int,feeling:String="Uygundu",completed:Boolean=true):SessionLog {
  val w=buildVersionedWorkout(code,s.dailyMinutes)
  val date=LocalDate.parse(s.start).plusDays(day-1L)
  return SessionLog("dated-$code-$day",w.title,date.toString(),w.seconds,feeling,"workout",w.id,
   date.atTime(18,0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),s.cycleId,completed,
   programDay=day,plannedSeconds=w.seconds)
 }
 private fun midday(date:LocalDate)=date.atTime(12,0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
 private fun acceptAt(s:UserState,date:LocalDate):UserState {
  val proposal=progressionProposal(s,date,reviewed)
  assertNotNull("Expected a reviewed, evidence-backed proposal on $date",proposal)
  return acceptProgression(s,proposal!!.id,midday(date),reviewed)
 }
 @Test fun ninetyDaysHaveCorrectDistributionAndClose() {
  for(days in listOf(2,3,4)) {
   val plan=ninetyDayProgram(state(days),today)
   assertEquals((1..90).toList(),plan.map{it.day})
   assertEquals(when(days){2->26;3->39;else->51},plan.count{it.training})
   assertFalse(plan.last().training)
   assertEquals("D",plan.last().plannedTemplateCode)
  }
  val all=ninetyDayProgram(state(),today)
  assertEquals(51,all.count{!it.training})
  assertEquals(4,all.count{it.plannedTemplateCode=="P"})
  assertEquals(22,all.count{it.plannedTemplateCode.startsWith("R")})
  assertEquals(13,all.count{it.plannedTemplateCode.startsWith("G")})
 }
 @Test fun phasesAreNotWeeklyRepetitionOrAutomaticLevel() {
  val s=state()
  mapOf(1 to "P",12 to "P",15 to "R1",29 to "R2",43 to "R1",57 to "R3",80 to "G-B-L",89 to "R1").forEach { (day,code) ->
   assertEquals(code,programForDay(s,day,today).plannedTemplateCode)
  }
  assertEquals(listOf("F1","F2","F3","F4","F5"),listOf(1,15,29,50,78).map{programPhase(it).code})
  listOf(13,17,18,21).forEach { age ->
   val d=programForDay(s.copy(age=age,activityHabit="regular",runningExperience="interval"),57,today)
   assertFalse(d.workout.hasSprint())
   assertEquals("P",d.templateCode)
  }
 }
 @Test fun shippedFlagsAreFalseAndPreviewCannotBypassReview() {
  assertEquals(ProgramReviewApprovals(),RELEASE_PROGRAM_APPROVALS)
  assertFalse(ProgramReviewApprovals(running=true).allows("R1"))
  val s=state().copy(progressionAccepted=true)
  val p=ninetyDayProgram(s,today)
  assertTrue(p.filter{it.plannedTemplateCode.startsWith("R")}.all{it.reviewPending})
  assertTrue(p.none{it.workout.hasSprint()})
  assertTrue(p.any{it.plannedWorkout.hasSprint()})
  assertNotNull(programStartBlockReason(s,buildVersionedWorkout("R1",15)))
 }
 @Test fun allVariantsHaveExactDurationsAndNoAssumedEquipment() {
  for(age in listOf(13,17,18,21))for(minutes in ProfileChoices.minutes)
   for(environment in ProfileChoices.environment.keys)for(days in ProfileChoices.days) {
    val s=state(days,minutes,environment).copy(age=age,equipment=setOf("none"))
    val first=ninetyDayProgram(s,today)
    assertEquals(first,ninetyDayProgram(s,today))
    first.forEach { day ->
     assertEquals(if(day.training)minutes*60 else 300,day.workout.seconds)
     assertEquals(day.workout,Content.workout(day.workout.id))
     assertFalse(workoutEquipmentMissing(day.workout,s.equipment))
     assertTrue(day.workout.steps.all{it.seconds>0})
     day.workout.steps.forEach{assertNotNull(Content.move(it.moveId))}
    }
   }
 }
 @Test fun versionedRecipeBudgetsRoundTrip() {
  for(code in listOf("P","R1","R2","R3","G-A","G-B","G-A-L","G-B-L","Y","K","K-P"))
   for(minutes in ProfileChoices.minutes) {
    val w=buildVersionedWorkout(code,minutes)
    assertEquals(if(code=="Y"&&minutes==30)900 else minutes*60,w.seconds)
    assertEquals(w,programWorkout(w.id))
    assertTrue(w.steps.all{it.seconds>0})
    w.steps.forEach{assertNotNull(Content.move(it.moveId))}
   }
  assertEquals(120,buildVersionedWorkout("N",2).seconds)
 }
 @Test fun runningUsesWholeBudgetWithoutRandomFloorMoves() {
  for(level in 1..3)for(minutes in listOf(10,15,30)) {
   val w=buildVersionedWorkout("R$level",minutes)
   assertEquals(300,w.steps.filter{it.phase=="warmup"}.sumOf{it.seconds})
   assertEquals(120,w.steps.filter{it.phase=="cooldown"}.sumOf{it.seconds})
   assertEquals(when(minutes){10->3;15->3+level;else->5+level},w.intervalCount())
   w.steps.forEachIndexed { i,step ->
    if(step.moveId=="sprint") {
     assertEquals(10,step.seconds)
     assertTrue(w.steps[i+1].rest)
     assertEquals(60,step.seconds+w.steps[i+1].seconds)
    }
   }
   assertFalse(w.steps.any{it.moveId in setOf("catcow","child","lunge","squat","wall")})
  }
 }
 @Test fun lightStrengthReducesRealLoad() {
  fun count(w:Workout)=w.steps.count{it.moveId in setOf("squat","wall","calf")&&it.phase=="main"}
  for(minutes in ProfileChoices.minutes)for(code in listOf("G-A","G-B")) {
   val normal=buildVersionedWorkout(code,minutes)
   val light=buildVersionedWorkout("$code-L",minutes)
   assertTrue(count(normal)<=when(minutes){5->3;10->4;15->6;else->8})
   assertTrue(count(light)<count(normal))
   assertEquals(normal.seconds,light.seconds)
  }
 }
 @Test fun combinedBudgetAndFirstFortnightAvoidFastRunning() {
  assertEquals(3,buildVersionedWorkout("K",15).intervalCount())
  assertEquals(180,buildVersionedWorkout("K",15).intervalSeconds())
  assertEquals(6,buildVersionedWorkout("K",30).intervalCount())
  assertEquals(360,buildVersionedWorkout("K",30).intervalSeconds())
  assertFalse(buildVersionedWorkout("K-P",30).hasSprint())
  assertEquals("Güç",buildVersionedWorkout("K",10).category)
  assertEquals("K-P",programForDay(state(days=2),4,today).plannedTemplateCode)
  assertFalse(programForDay(state(days=2),11,today).plannedWorkout.hasSprint())
 }
 @Test fun fourDayYogaIsDaySixNotDaySeven() {
  val a=state(days=4).answers()
  assertEquals(listOf("P","D","G-A","D","P","Y","D"),(1..7).map{calendarTemplate(a,it)})
  assertEquals("D",calendarTemplate(a,90))
 }
 @Test fun indoorPlanAlsoHonorsLightWeeks() {
  val a=state(environment="indoor").answers()
  assertEquals("G-A-L",calendarTemplate(a,43))
  assertEquals("G-B-L",calendarTemplate(a,47))
  assertEquals("G-A-L",calendarTemplate(a,78))
  assertEquals("G-A",calendarTemplate(a,50))
 }
 @Test fun twoDayRecoveryFocusAdaptsSecondCombinedSlot() {
  val s=state(days=2).copy(start=today.minusDays(17).toString(),focus="recovery")
  val day=programForDay(s,18,today,reviewed)
  assertEquals("P",day.templateCode)
  assertTrue("RECOVERY_FOCUS" in day.reasonCodes)
 }
 @Test fun fiveMinuteNormalizationDoesNotAdvertiseUnneededRunReview() {
  val day=programForDay(state(minutes=5),57,today)
  assertEquals("P",day.plannedTemplateCode)
  assertFalse(day.reviewPending)
  assertTrue("SHORT_TIME_BUDGET" in day.reasonCodes)
 }
 @Test fun combinedNeedsCurrentSameAreaConfirmationBeyondContentApproval() {
  val s=state(days=2).copy(start=today.minusDays(3).toString())
  val notConfirmed=programForDay(s,4,today,reviewed)
  assertEquals("P",notConfirmed.templateCode)
  assertTrue("COMBINED_AREA_UNCONFIRMED" in notConfirmed.reasonCodes)
  val confirmed=s.copy(dailyCheckDate=today.toString(),dailyEquipmentConfirmed=true)
  assertEquals("K-P",programForDay(confirmed,4,today,reviewed).templateCode)
  assertEquals("P",programForDay(confirmed.copy(dailyCheckDate=today.minusDays(1).toString()),4,today,reviewed).templateCode)
 }
 @Test fun yogaAndBalanceHaveExplicitSidesAndTransitions() {
  val w=buildVersionedWorkout("Y",10)
  for(side in listOf("left","right")) {
   val steps=w.steps.filter{it.moveId=="lunge"&&it.side==side}
   assertEquals(listOf("prepare","main","exit"),steps.map{it.phase})
   assertEquals(listOf(15,30,15),steps.map{it.seconds})
  }
  val balance=buildVersionedWorkout("G-B",5).steps
  val left=balance.indexOfFirst{it.moveId=="balance"&&it.side=="left"}
  assertEquals("transition",balance[left+1].phase)
  assertEquals("right",balance[left+2].side)
 }
 @Test fun reviewFeedbackAndAcceptanceAllRequiredForProgression() {
  var s=state().copy(start=today.minusDays(56).toString(),progressionAccepted=true)
  val entry=today.minusDays(42)
  s=s.copy(sessions=listOf(datedLog(s,"P",8),datedLog(s,"P",12)))
  assertEquals("P",programForDay(s,15,entry,reviewed).templateCode) // Old boolean grants nothing.
  val first=progressionProposal(s,entry,reviewed)!!
  assertEquals("P",first.fromCode);assertEquals("R1",first.toCode)
  s=acceptAt(s,entry)
  assertEquals("R1",programForDay(s,15,entry,reviewed).templateCode)
  s=s.copy(sessions=s.sessions+datedLog(s,"R1",22)+datedLog(s,"R1",26))
  val secondDate=today.minusDays(28)
  assertEquals("R1",programForDay(s,29,secondDate,reviewed).templateCode)
  assertEquals("R2",progressionProposal(s,secondDate,reviewed)!!.toCode)
  s=acceptAt(s,secondDate)
  assertEquals("R2",programForDay(s,29,secondDate,reviewed).templateCode)
  s=s.copy(sessions=s.sessions+datedLog(s,"R2",50)+datedLog(s,"R2",54))
  assertEquals("R2",programForDay(s,57,today,reviewed).templateCode)
  s=acceptAt(s,today)
  assertEquals("R3",programForDay(s,57,today,reviewed).templateCode)
  assertEquals(3,s.progressionConsents.size)
  assertFalse(s.progressionAccepted)
  assertEquals("P",programForDay(s.copy(progressionConsents=emptyList()),57,today,reviewed).templateCode)
  assertEquals("P",programForDay(s,57,today).templateCode)
 }
 @Test fun unreviewedOrStaleProposalsNeverActivateConsent() {
  var s=state().copy(start=today.minusDays(14).toString())
  s=s.copy(sessions=listOf(datedLog(s,"P",8),datedLog(s,"P",12)))
  assertNull(progressionProposal(s,today))
  val p=progressionProposal(s,today,reviewed)!!
  assertEquals(s,acceptProgression(s,p.id,midday(today)))
  assertEquals(s,acceptProgression(s,"altered-id",midday(today),reviewed))
  assertEquals(s,acceptProgression(s,p.id,midday(today.plusDays(1)),reviewed))
  val accepted=acceptAt(s,today)
  assertEquals(accepted,acceptProgression(accepted,p.id,midday(today),reviewed))
  assertEquals("P",programForDay(accepted.copy(dailyMinutes=30),15,today,reviewed).templateCode)
  assertEquals("P",programForDay(accepted.copy(cycleId="another-cycle"),15,today,reviewed).templateCode)
  assertEquals("P",programForDay(accepted,15,today,reviewed.copy(reviewerRecord="different review")).templateCode)
 }
 @Test fun proposalChecksLatestTwoAttemptsNotOnlySuccessfulOnes() {
  var s=state().copy(start=today.minusDays(14).toString())
  s=s.copy(sessions=listOf(datedLog(s,"P",8),datedLog(s,"P",12)))
  assertNotNull(progressionProposal(s,today,reviewed))
  listOf("", "Zorlayıcı", "Rahatsızlık").forEach { feeling ->
   assertNull(progressionProposal(s.copy(sessions=s.sessions+datedLog(s,"P",13,feeling)),today,reviewed))
  }
  assertNull(progressionProposal(s.copy(sessions=s.sessions+datedLog(s,"P",13,completed=false)),today,reviewed))
  val short=s.sessions.last().copy(seconds=100)
  assertNull(progressionProposal(s.copy(sessions=s.sessions.dropLast(1)+short),today,reviewed))
  val duplicate=s.sessions.last().copy(id="duplicate",programDay=8)
  assertNull(progressionProposal(s.copy(sessions=listOf(s.sessions.first(),duplicate)),today,reviewed))
 }
 @Test fun proposalRespectsReadinessExperienceLoadAndCalendar() {
  var s=state().copy(start=today.minusDays(14).toString())
  s=s.copy(sessions=listOf(datedLog(s,"P",8),datedLog(s,"P",12)))
  listOf(s.copy(safety="pain"),s.copy(runningExperience="unknown"),s.copy(environment="indoor"),
   s.copy(sleepHabit="under6"),s.copy(gentle=true),s.copy(pausedOn=today.toString()),
   s.copy(active=ActiveSession("breath")),s.copy(externalSportDate=today.toString()),
   s.copy(dailyCheckDate=today.toString(),dailyReadiness="tired"),
   s.copy(dailyCheckDate=today.toString(),dailyEnvironment="indoor"),
   s.copy(start=today.minusDays(10).toString()),s.copy(dailyMinutes=5)
  ).forEach{assertNull(progressionProposal(it,today,reviewed))}
 }
 @Test fun consentMustCarryTwoVerifiedEvidenceRecords() {
  var s=state().copy(start=today.minusDays(14).toString())
  s=s.copy(sessions=listOf(datedLog(s,"P",8),datedLog(s,"P",12)))
  s=acceptAt(s,today)
  val c=s.progressionConsents.single()
  val corrupt=listOf(c.copy(level=3),c.copy(evidenceSessionIds=listOf("missing","other")),
   c.copy(acceptedAtEpochMs=0),c.copy(profileKey="different"),c.copy(proposalId="made-up"))
  corrupt.forEach{assertEquals("P",programForDay(s.copy(progressionConsents=listOf(it)),15,today,reviewed).templateCode)}
  assertEquals("P",programForDay(s.copy(sessions=s.sessions.dropLast(1)),15,today,reviewed).templateCode)
 }
 @Test fun tenMinuteConsentDoesNotOfferAnIdenticalNextDose() {
  var s=state(minutes=10).copy(start=today.minusDays(28).toString())
  s=s.copy(sessions=listOf(datedLog(s,"P",8),datedLog(s,"P",12)))
  s=acceptAt(s,today.minusDays(14))
  s=s.copy(sessions=s.sessions+datedLog(s,"R1",22)+datedLog(s,"R1",26))
  assertNull(progressionProposal(s,today,reviewed))
  assertEquals("R1",programForDay(s,29,today,reviewed).templateCode)
 }
 @Test fun incompleteDuplicateAndUnknownFeedbackNeverUnlock() {
  val s=state().copy(progressionAccepted=true)
  listOf(
   listOf(log(s,"P",1),log(s,"P",5,complete=false)),
   listOf(log(s,"P",1),log(s,"P",1)),
   listOf(log(s,"P",1),log(s,"P",5,feeling="")),
   listOf(log(s,"P",1),log(s,"P",5,feeling="Zorlayıcı"))
  ).forEach{logs->assertEquals("P",programForDay(s.copy(sessions=logs),15,today,reviewed).templateCode)}
 }
 @Test fun longGapReturnsToPreparation() {
  var s=state().copy(progressionAccepted=true)
  s=s.copy(sessions=listOf(log(s,"P",1,15),log(s,"P",5,14)))
  val day=programForDay(s,57,today,reviewed)
  assertEquals("P",day.templateCode)
  assertTrue("RETURN_REASSESSMENT" in day.reasonCodes)
 }
 @Test fun runningLoadUsesIdsAndFortyEightHours() {
  val s=state()
  val now=today.atTime(12,0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
  val renamed=log(s,"R1",15).copy(title="Changed title",completedAtEpochMs=now-47*60*60*1000L)
  assertTrue(recentRunningLoad(s.copy(sessions=listOf(renamed)),today,now))
  assertFalse(recentRunningLoad(s.copy(sessions=listOf(renamed.copy(completedAtEpochMs=now-48*60*60*1000L))),today,now))
  assertTrue(recentRunningLoad(s.copy(sessions=listOf(renamed.copy(completedAtEpochMs=now+1000L))),today,now))
  assertTrue(recentRunningLoad(s.copy(externalSportDate=today.toString()),today,now))
  val unknown=SessionLog("legacy","Any title",today.minusDays(1).toString(),300,"İyi")
  assertTrue(unknownRecentLoad(s.copy(sessions=listOf(unknown)),today))
  assertFalse(recentRunningLoad(s.copy(sessions=listOf(unknown)),today,now))
 }
 @Test fun candidatesNeverPutRunsOnAdjacentDaysOrWeekSeams() {
  for(days in ProfileChoices.days) {
   val runs=ninetyDayProgram(state(days),today).filter{it.plannedWorkout.hasSprint()}
   runs.zipWithNext().forEach{(a,b)->assertTrue(b.day-a.day>=2)}
  }
 }
 @Test fun equipmentAndPainOverridePerformance() {
  val s=state().copy(start=today.minusDays(16).toString())
  val d=programForDay(s.copy(equipment=setOf("none")),17,today,reviewed)
  assertEquals("P",d.templateCode)
  assertTrue("EQUIPMENT_MISSING" in d.reasonCodes)
  assertTrue(todayProgram(s.copy(dailyCheckDate=today.toString(),dailyReadiness="pain"),today).blocked)
 }
 @Test fun dayNinetyOneClosesAndRestartArchivesHistory() {
  var s=state().copy(start=today.minusDays(90).toString(),done=mapOf("plan:1" to setOf("move"),"2026-09-01" to setOf("move")))
  s=s.copy(sessions=listOf(log(s,"P",1)))
  assertTrue(isCycleComplete(s,today))
  assertFalse(todayProgram(s,today.plusDays(10)).training)
  assertTrue(runCatching{programForDay(s,91,today)}.isFailure)
  val next=nextCycle(s,today)
  assertNotEquals(s.cycleId,next.cycleId)
  assertEquals(s.sessions,next.sessions)
  assertEquals(s.cycleId,next.archivedCycles.single().id)
  assertEquals(setOf("move"),next.done["2026-09-01"])
  assertNull(next.done["plan:1"])
  val active=s.copy(active=ActiveSession("breath"))
  assertEquals(active,nextCycle(active,today))
  assertEquals(1,programElapsedDay(next,today))
 }
 @Test fun pausedCalendarDoesNotAdvanceOnRepeatedReads() {
  val s=state().copy(start=today.minusDays(20).toString(),pausedOn=today.minusDays(4).toString(),pausedDays=3)
  assertEquals(14,programElapsedDay(s,today))
  assertEquals(14,programElapsedDay(s,today.plusDays(30)))
 }
 @Test fun firstTrainingDateRotatesSafeRelativeCalendarWithoutChangingCounts() {
  for(days in ProfileChoices.days)for(offset in 0L..6L) {
   val s=state(days=days)
   val date=today.plusDays(offset)
   val scheduled=chooseProgramStart(s,date,today)
   assertEquals(date.toString(),scheduled.start)
   assertEquals(offset>0,isProgramScheduled(scheduled,today))
   assertEquals(when(days){2->26;3->39;else->51},ninetyDayProgram(scheduled,today).count{it.training})
   assertEquals(ninetyDayProgram(s,today).map{it.plannedTemplateCode},ninetyDayProgram(scheduled,today).map{it.plannedTemplateCode})
   assertEquals(1,programElapsedDay(scheduled,date))
   assertEquals(90,programElapsedDay(scheduled,date.plusDays(89)))
   assertFalse(isProgramScheduled(scheduled,date))
  }
 }
 @Test fun futureStartBlocksTrainingButNotOptionalBreathing() {
  val s=chooseProgramStart(state(),today.plusDays(2),today)
  val w=buildVersionedWorkout("P",15)
  assertTrue(todayProgram(s,today).blocked)
  assertTrue("PROGRAM_NOT_STARTED" in todayProgram(s,today).reasonCodes)
  assertNotNull(programStartBlockReason(s,w,midday(today)))
  assertNull(programStartBlockReason(s,Content.workout("breath"),midday(today)))
  assertNull(programStartBlockReason(s,buildVersionedWorkout("N",2),midday(today)))
  assertNull(programStartBlockReason(s,w,midday(today.plusDays(2))))
  assertEquals(s,beginWorkout(s,w.id,midday(today)))
 }
 @Test fun firstDateSelectionCannotMoveTrainingHistoryOrUnsupportedDates() {
  val s=state()
  assertEquals(s,chooseProgramStart(s,today.minusDays(1),today))
  assertEquals(s,chooseProgramStart(s,today.plusDays(7),today))
  val blocked=listOf(s.copy(active=ActiveSession("breath")),s.copy(pausedOn=today.toString()),
   s.copy(start=today.minusDays(90).toString()),s.copy(sessions=listOf(log(s,"P",1))),
   s.copy(sessions=listOf(log(s,"P",1,complete=false))),
   s.copy(sessions=listOf(log(s,"P",1).copy(cycleId=""))))
  blocked.forEach { current->
   assertFalse(canChooseProgramStart(current,today))
   assertEquals(current,chooseProgramStart(current,today.plusDays(1),today))
  }
  val archived=s.copy(sessions=listOf(log(s,"P",1).copy(cycleId="archived-cycle")))
  assertTrue(canChooseProgramStart(archived,today))
  val breath=s.copy(sessions=listOf(SessionLog("breath","Nefes",today.toString(),120,"Uygundu",type="breath")))
  assertTrue(canChooseProgramStart(breath,today))
 }
 @Test fun restartClosesAnOpenPauseOnceInArchive() {
  val s=state().copy(start=today.minusDays(101).toString(),pausedDays=2,pausedOn=today.minusDays(9).toString())
  assertTrue(isCycleComplete(s,today))
  val next=nextCycle(s,today)
  assertEquals(11L,next.archivedCycles.single().pausedDays)
  assertEquals(0L,next.pausedDays)
  assertEquals(next,nextCycle(next,today))
 }
 @Test fun malformedAndNonCanonicalV8IdsAreRejected() {
  listOf("p8_R3_5_10","p8_Y_30_10","p8_K_5_10","p8_R1_15_20","p8_unknown_5_10","p8_P_05_10").forEach{assertNull(it,programWorkout(it))}
 }
 @Test fun activeSnapshotSurvivesPreferenceChange() {
  val w=buildVersionedWorkout("P",15).copy(title="Immutable title")
  val s=state(minutes=5).copy(active=ActiveSession(w.id,remaining=0,running=true,deadline=100,snapshot=w))
  val advanced=advanceSession(s,1000).active!!
  assertEquals(1,advanced.step)
  assertEquals(w.steps[1].seconds,advanced.remaining)
  assertEquals(w,activeWorkout(advanced))
 }
 @Test fun restoringRechecksSafetyButDoesNotRegenerateBenignSnapshot() {
  val w=buildVersionedWorkout("P",15)
  val a=ActiveSession(w.id,snapshot=w,running=false)
  val s=state(minutes=5).copy(active=a)
  assertNull(sessionResumeBlockReason(s,a))
  assertNotNull(sessionResumeBlockReason(s.copy(safety="pain"),a))
  assertNotNull(sessionResumeBlockReason(s.copy(pausedOn=today.toString()),a))
  assertNotNull(sessionResumeBlockReason(s,a.copy(snapshot=w.copy(steps=listOf(Step("unknown",10))))))
 }
 @Test fun legacyAndRestoredMovePoolsCannotBypassReview() {
  val s=state()
  listOf(buildProgramWorkout("run",15,2),buildProgramWorkout("strength",15,2),buildProgramWorkout("yoga",15,2)).forEach { w ->
   assertNotNull(programStartBlockReason(s,w))
   assertNotNull(sessionResumeBlockReason(s,ActiveSession(w.id,snapshot=w,running=false)))
  }
 }
}
