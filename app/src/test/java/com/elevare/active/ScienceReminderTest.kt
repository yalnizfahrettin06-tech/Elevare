package com.elevare.active

import org.junit.Assert.*
import org.junit.Test
import java.time.*

class ScienceReminderTest {
 private val today=LocalDate.of(2026,9,8)
 private val sourceIds=listOf("sprint","strength","who","teen-sleep")
 private val facts=(0..15).map{ScienceFact("f$it",sourceIds[it%4],"Örnek $it","Hareket",13,21,"Editoryal özet")}
 private val catalog=ScienceCatalog(sourceIds.map{ScienceSource(it,it,"","https://example.org/$it","Kaynak","Örneklem","Özet","Sınır")},facts)
 private val profile=UserState(ready=true,onboardingVersion=8,age=17,focus="performance",recentGrowth="private",sleepHabit="8to10",activityHabit="new",dailyMinutes=15,safety="clear",environment="outdoor",trainingDays=3,runningExperience="new",equipment=setOf("none"),start=today.toString(),workoutReminders=true)

 @Test fun todaySelectionIsPersistedAndDoesNotRotateOnReopen(){
  val first=selectDailyFact(catalog,profile,today)!!
  val seen=recordFactView(profile,first,today)
  assertEquals(first,selectDailyFact(catalog,seen,today))
  assertEquals(first,selectDailyFact(catalog,seen.copy(focus="growth"),today))
  assertEquals(seen,recordFactView(seen,first,today))
 }
 @Test fun manualChangesKeepAllSeenCardsForSevenDaySuppression(){
  val first=selectDailyFact(catalog,profile,today)!!
  val once=recordFactView(profile,first,today)
  val second=selectDailyFact(catalog,once,today,true)!!
  assertNotEquals(first.id,second.id)
  val twice=recordFactView(once,second,today)
  assertEquals(today.toString(),twice.factSeenAt[first.id])
  assertEquals(today.toString(),twice.factSeenAt[second.id])
  assertEquals(second.id,twice.factHistory[today.toString()])
  assertTrue(factSeenRecently(twice,first.id,today.plusDays(6)))
  assertFalse(factSeenRecently(twice,first.id,today.plusDays(7)))
  assertFalse(selectDailyFact(catalog,twice,today.plusDays(1))!!.id in setOf(first.id,second.id))
 }
 @Test fun thirdConsecutiveCardUsesDifferentSourceWhenAvailable(){
  val state=profile.copy(recentFactIds=listOf("f0","f4"))
  assertNotEquals("sprint",selectDailyFact(catalog,state,today)!!.sourceId)
 }
 @Test fun openingDetailDoesNotReplaceDailyHomeCard(){
  val state=recordFactView(profile,facts[0],today)
  val changed=recordFactView(state,facts[1],today,daily=false)
  assertEquals("f0",changed.factHistory[today.toString()])
  assertEquals(today.toString(),changed.factSeenAt["f1"])
 }
 @Test fun withdrawnAndWrongAgeCardsAreNotSelectable(){
  val small=ScienceCatalog(catalog.sources,listOf(facts[0].copy(status="withdrawn"),facts[1].copy(minAge=18)))
  assertNull(selectDailyFact(small,profile,today))
 }
 @Test fun sourceAndFavoriteChangesNeverAffectWorkoutChoices(){
  val changed=recordFactView(profile,facts[0],today).copy(savedFacts=setOf("f0"))
  assertEquals(profile.answers(),changed.answers())
  assertEquals(todayProgram(profile,today),todayProgram(changed,today))
 }
 @Test fun shortCatalogHasAValidRepeatFallback(){
  val one=ScienceCatalog(catalog.sources,listOf(facts[0]))
  val seen=recordFactView(profile,facts[0],today)
  assertEquals(facts[0],selectDailyFact(one,seen,today,true))
 }
 @Test fun localReminderRebuildsAfterDayAndTimezoneChanges(){
  val zone=ZoneId.of("Europe/Istanbul")
  val now=today.atTime(18,0).atZone(zone)
  assertEquals(today.plusDays(1).atTime(17,0).atZone(zone),nextReminderAt(LocalTime.of(17,0),now))
  assertEquals(today.atTime(21,30).atZone(zone),nextReminderAt(LocalTime.of(21,30),now))
  val spring=ZonedDateTime.of(2026,3,28,18,0,0,0,ZoneId.of("Europe/Berlin"))
  assertTrue(nextReminderAt(LocalTime.of(2,30),spring).isAfter(spring))
 }
 @Test fun quietHoursHandleOvernightAndDaytimeSleep(){
  assertTrue(isQuietTime(LocalTime.of(23,0),"22:00","07:00"))
  assertTrue(isQuietTime(LocalTime.of(6,59),"22:00","07:00"))
  assertFalse(isQuietTime(LocalTime.of(7,0),"22:00","07:00"))
  assertTrue(isQuietTime(LocalTime.of(15,0),"14:00","18:00"))
  assertFalse(isQuietTime(LocalTime.of(12,0),"bad","18:00"))
 }
 @Test fun duplicateAndNearbyNotificationsAreSuppressed(){
  assertTrue(reminderSuppressed(ReminderKind.WORKOUT,today,today.toString(),0,1000))
  assertTrue(reminderSuppressed(ReminderKind.SLEEP,today,"",1000,2000))
  assertFalse(reminderSuppressed(ReminderKind.SLEEP,today,"",1000,1000+15*60*1000))
 }
 @Test fun delayedSleepAlarmDoesNotWakeTheUserAfterBedtime(){
  val state=profile.copy(reminders=true)
  val night=today.atTime(21,30).atZone(ZoneId.of("Europe/Istanbul"))
  assertTrue(shouldRemindSleep(state,night))
  assertTrue(shouldRemindSleep(state,night.plusMinutes(29)))
  assertFalse(shouldRemindSleep(state,night.plusMinutes(30)))
  assertFalse(shouldRemindSleep(state,night.plusHours(12)))
 }
 @Test fun eachSleepLeadSchedulesTheChosenWindowBeforeBedtime(){
  val bed=today.atTime(22,0).atZone(ZoneId.of("Europe/Istanbul"))
  SLEEP_REMINDER_LEAD_OPTIONS.forEach{lead->
   val state=profile.copy(reminders=true,sleepReminderLeadMinutes=lead)
   val scheduled=bed.minusMinutes(lead.toLong())
   assertEquals(scheduled.toLocalTime(),sleepReminderTime(state))
   assertFalse(shouldRemindSleep(state,scheduled.minusMinutes(1)))
   assertTrue(shouldRemindSleep(state,scheduled))
   assertTrue(shouldRemindSleep(state,bed.minusMinutes(1)))
   assertFalse(shouldRemindSleep(state,bed))
   assertFalse(shouldRemindSleep(state.copy(reminders=false),scheduled))
  }
 }
 @Test fun sleepLeadAcrossMidnightStillStopsAtBedtime(){
  val state=profile.copy(reminders=true,bed="00:15",sleepReminderLeadMinutes=60)
  val night=today.atTime(23,15).atZone(ZoneId.of("Europe/Istanbul"))
  assertEquals(LocalTime.of(23,15),sleepReminderTime(state))
  assertTrue(shouldRemindSleep(state,night))
  assertTrue(shouldRemindSleep(state,night.plusMinutes(59)))
  assertFalse(shouldRemindSleep(state,night.plusMinutes(60)))
 }
 @Test fun invalidSleepLeadUsesThirtyMinutesWithoutEnablingNotifications(){
  listOf(-15,0,45,1440).forEach{invalid->
   val state=profile.copy(sleepReminderLeadMinutes=invalid)
   assertEquals(30,normalizedSleepReminderLead(invalid))
   assertEquals(LocalTime.of(21,30),sleepReminderTime(state))
   assertFalse(state.reminders)
   assertFalse(shouldRemindSleep(state,today.atTime(21,30).atZone(ZoneId.of("Europe/Istanbul"))))
  }
 }
 @Test fun onlyOptedInEligibleWorkoutDaysCanNotify(){
  val now=today.atTime(17,0).atZone(ZoneId.of("Europe/Istanbul"))
  assertTrue(shouldRemindWorkout(profile,now))
  assertFalse(shouldRemindWorkout(profile.copy(workoutReminders=false),now))
  assertFalse(shouldRemindWorkout(profile.copy(pausedOn=today.toString()),now))
  assertFalse(shouldRemindWorkout(profile.copy(safety="pain"),now))
  assertFalse(shouldRemindWorkout(profile.copy(active=ActiveSession("breath")),now))
  assertFalse(shouldRemindWorkout(profile,now.plusDays(1)))
  assertFalse(shouldRemindWorkout(profile,now.plusDays(89)))
 }
 @Test fun completionStopsWorkoutReminderButPartialSessionDoesNot(){
  val now=today.atTime(17,0).atZone(ZoneId.of("Europe/Istanbul"))
  val partial=SessionLog("a","Başlık",today.toString(),30,"",completed=false)
  assertTrue(shouldRemindWorkout(profile.copy(sessions=listOf(partial)),now))
  assertFalse(shouldRemindWorkout(profile.copy(sessions=listOf(partial.copy(completed=true))),now))
 }
 @Test fun scheduledStartAndCurrentSafetyBlockWorkoutNotifications(){
  val now=today.atTime(17,0).atZone(ZoneId.of("Europe/Istanbul"))
  val scheduled=chooseProgramStart(profile,today.plusDays(2),today)
  assertFalse(shouldRemindWorkout(scheduled,now))
  assertFalse(shouldRemindWorkout(scheduled,now.plusDays(1)))
  assertTrue(shouldRemindWorkout(scheduled,now.plusDays(2)))
  assertFalse(shouldRemindWorkout(profile.copy(dailyCheckDate=today.toString(),dailyReadiness="pain"),now))
 }
}
