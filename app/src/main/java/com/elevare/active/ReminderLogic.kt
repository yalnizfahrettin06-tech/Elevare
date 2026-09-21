package com.elevare.active

import java.time.*

enum class ReminderKind(val channelId:String,val requestCode:Int,val title:String){
 SLEEP("elevare_sleep",31,"Uyku hazırlığı"),
 WORKOUT("elevare_workout",41,"Antrenman hatırlatması")
}

val SLEEP_REMINDER_LEAD_OPTIONS=listOf(15,30,60)
fun normalizedSleepReminderLead(minutes:Int)=minutes.takeIf{it in SLEEP_REMINDER_LEAD_OPTIONS}?:30
fun sleepReminderTime(s:UserState):LocalTime?=runCatching{
 LocalTime.parse(s.bed).minusMinutes(normalizedSleepReminderLead(s.sleepReminderLeadMinutes).toLong())
}.getOrNull()

/** Local wall time is reconstructed for the next date, including timezone/DST changes. */
fun nextReminderAt(time:LocalTime,now:ZonedDateTime):ZonedDateTime {
 val candidate=now.toLocalDate().atTime(time).atZone(now.zone)
 return if(candidate.isAfter(now))candidate else now.toLocalDate().plusDays(1).atTime(time).atZone(now.zone)
}
fun isQuietTime(time:LocalTime,bed:String,wake:String):Boolean {
 val from=runCatching{LocalTime.parse(bed)}.getOrNull()?:return false
 val until=runCatching{LocalTime.parse(wake)}.getOrNull()?:return false
 return if(from==until)false else if(from<until)time>=from && time<until else time>=from || time<until
}
fun shouldRemindWorkout(s:UserState,now:ZonedDateTime):Boolean {
 if(!s.ready || s.onboardingVersion<TRAINING_ONBOARDING_VERSION || !s.workoutReminders || s.pausedOn!=null || !trainingAllowed(s) || s.active?.running==true || s.life.notificationsMuted)return false
 if(isQuietTime(now.toLocalTime(),s.bed,s.wake))return false
 val date=now.toLocalDate()
 if(isCycleComplete(s,date) || isProgramScheduled(s,date))return false
 if(s.sessions.any{it.date==date.toString() && it.completed && it.type=="workout"})return false
 if("move" in s.done[date.toString()].orEmpty())return false
 return runCatching{todayProgram(s,date).let{it.training && !it.blocked}}.getOrDefault(false)
}
fun shouldRemindSleep(s:UserState,now:ZonedDateTime):Boolean {
 if(!s.ready || s.onboardingVersion<TRAINING_ONBOARDING_VERSION || !s.reminders)return false
 if(isQuietTime(now.toLocalTime(),s.bed,s.wake))return false
 val planned=sleepReminderTime(s)?:return false
 val lead=normalizedSleepReminderLead(s.sleepReminderLeadMinutes)
 val minutesSince=Math.floorMod(now.hour*60+now.minute-(planned.hour*60+planned.minute),1440)
 // A very late inexact alarm must not wake somebody after their bedtime.
 return minutesSince in 0 until lead
}
fun reminderSuppressed(kind:ReminderKind,date:LocalDate,lastDate:String,lastAnyEpochMs:Long,nowEpochMs:Long):Boolean {
 // A shared 15-minute window prevents sleep + training notifications competing.
 return lastDate==date.toString() || (lastAnyEpochMs>0 && nowEpochMs-lastAnyEpochMs in 0L until 15*60*1000L)
}
