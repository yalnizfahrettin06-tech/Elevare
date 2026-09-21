package com.elevare.active
import org.junit.Assert.*
import org.junit.Test
import java.time.*

class RenewalTest {
 private val today=LocalDate.of(2026,9,22)
 private fun ready()=completeOnboarding(UserState(),TrainingAnswers(18,"performance","unknown","8to10","regular",15,"clear","outdoor",3,"interval",setOf("none")),false,1000,today)
 @Test fun preferenceCannotClearPain(){val s=ready().copy(dailyReadiness="pain");assertEquals("pain",applyTimePreference(s,10).dailyReadiness)}
 @Test fun painDoesNotBecomePermanentProfile(){val s=ready().copy(dailyReadiness="pain");assertEquals("clear",s.safety);assertFalse(trainingAllowed(s));assertTrue(trainingAllowed(reviewMovementSafety(s,true)))}
 @Test fun declineReviewPreservesBlock(){val s=ready().copy(dailyReadiness="pain");assertEquals(s,reviewMovementSafety(s,false))}
 @Test fun painBlocksStartAndResume(){val s=ready().copy(dailyReadiness="pain");assertNotNull(programStartBlockReason(s,Content.workout("breath")));assertNotNull(sessionResumeBlockReason(s,ActiveSession("breath")))}
 @Test fun sleepDeletionPreservesOtherHabits(){val date=today.toString();val s=ready().copy(sleeps=listOf(SleepLog(date,"22:00","07:00")),done=mapOf(date to setOf("sleep","move")));val n=removeSleep(s,date);assertTrue(n.sleeps.isEmpty());assertEquals(setOf("move"),n.done[date])}
 @Test fun sevenDaysCrossWeekBoundary(){val s=ready().copy(sessions=(0..8).map{SessionLog("$it","Test",today.minusDays(it.toLong()).toString(),60,"",cycleId="old")});assertEquals(7,lastSevenWorkouts(s,today));assertEquals(7,progressRecords(s,"week","all",today).size);assertTrue(progressRecords(s,"cycle","all",today).isEmpty())}
 @Test fun partialAndBreathAreNotWorkoutTotals(){val s=ready().copy(sessions=listOf(SessionLog("1","A",today.toString(),10,"",completed=false),SessionLog("2","N",today.toString(),10,"",type="breath")));assertEquals(0,lastSevenWorkouts(s,today));assertEquals(1,progressRecords(s,"all","partial",today).size)}
 @Test fun backupRoundTripMutesReminders(){val s=ready().copy(reminders=true,workoutReminders=true,life=addRoutine(LifeState(),"morning").copy(notes=mapOf("morning" to "Hazırla")));val n=validatedBackup(StateCodec.encode(s));assertFalse(n.reminders);assertFalse(n.workoutReminders);assertTrue(n.life.notificationsMuted);assertEquals(s.life.notes,n.life.notes)}
 @Test(expected=IllegalArgumentException::class) fun malformedBackupRejected(){validatedBackup("{}")}
 @Test(expected=IllegalArgumentException::class) fun duplicateSessionsRejected(){val l=SessionLog("1","A",today.toString(),10,"");validatedBackup(StateCodec.encode(ready().copy(sessions=listOf(l,l))))}
 @Test fun pausedSessionDoesNotMuteAllRoutines(){
  val now=today.atTime(12,0).atZone(ZoneId.of("Europe/Istanbul"))
  val life=addRoutine(LifeState(),"morning").let{it.copy(plans=it.plans.map{p->p.copy(time="12:00",remind=true)})}
  val s=ready().copy(life=life,active=ActiveSession("breath",running=false))
  assertTrue(canDeliverLife(s,"morning",life.plans.single().revision,now.toInstant().toEpochMilli(),now))
  assertFalse(canDeliverLife(s.copy(life=life.copy(notificationsMuted=true)),"morning",life.plans.single().revision,now.toInstant().toEpochMilli(),now))
 }
 @Test fun reviewedContentExpiresAndWithdrawalWins(){
  val f=ScienceFact("1","s","Text","Info",13,21,"summary",status="published",reviewer="review-record",reviewedAt=today.minusDays(1).toString(),nextReviewAt=today.toString())
  assertTrue(factVisible(f,today));assertFalse(factVisible(f,today.plusDays(1)));assertFalse(factVisible(f.copy(status="withdrawn"),today));assertFalse(factVisible(f.copy(reviewer=""),today))
 }
 @Test fun longHistoryKeepsAllRecords(){
  val s=ready().copy(sessions=(0..999).map{SessionLog("$it","Test",today.minusDays(it.toLong()).toString(),60,"",cycleId="older")})
  val n=validatedBackup(StateCodec.encode(s))
  assertEquals(1000,progressRecords(n,"all","all",today).size)
 }
 @Test fun archiveNeverRecountsAgainstNewSchedule(){
  for(days in listOf(2,3,4)){
   val s=ready().copy(start=today.minusDays(90).toString(),trainingDays=days)
   val logs=listOf(1,3,5).map{SessionLog("$it","Test",today.minusDays((90-it).toLong()).toString(),300,"",cycleId=s.cycleId,programDay=it)}
   val archive=nextCycle(s.copy(sessions=logs),today).archivedCycles.single()
   assertEquals(3,archive.completedSessions);assertEquals(-1,archive.plannedSessions)
  }
 }
 @Test fun sameTimePriorityIsDeterministic(){
  val jobs=listOf(Triple(1000L,3,"routine"),Triple(1000L,1,"workout"),Triple(1000L,0,"sleep"))
  assertEquals("sleep",earliestReminder(jobs)!!.third)
  assertEquals("sleep",earliestReminder(jobs.reversed())!!.third)
  assertEquals("earlier",earliestReminder(jobs+Triple(999L,7,"earlier"))!!.third)
 }
 @Test fun notesAndMuteSurviveCodec(){val life=LifeState(notes=mapOf("morning" to "Çantamı hazırla"),notificationsMuted=true);assertEquals(life,StateCodec.decode(StateCodec.encode(ready().copy(life=life))).life)}
}
