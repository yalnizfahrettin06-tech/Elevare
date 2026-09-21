package com.elevare.active

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import java.time.*

object Reminder{
 fun allowed(c:Context):Boolean=runCatching{
  (Build.VERSION.SDK_INT<33 || c.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED) &&
   c.getSystemService(NotificationManager::class.java).areNotificationsEnabled()
 }.getOrDefault(false)
 private fun pending(c:Context,kind:ReminderKind,scheduled:Long=0):PendingIntent{
  val receiver=if(kind==ReminderKind.SLEEP)SleepReminderReceiver::class.java else WorkoutReminderReceiver::class.java
  return PendingIntent.getBroadcast(c,kind.requestCode,Intent(c,receiver).putExtra("scheduled",scheduled),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
 }
 private fun lifeCode(id:String)=100+LifeCatalog.all.indexOfFirst{it.id==id}
 private fun lifePending(c:Context,id:String,revision:Long=0,scheduled:Long=0)=PendingIntent.getBroadcast(c,lifeCode(id),
  Intent(c,RoutineReminderReceiver::class.java).putExtra("routine",id).putExtra("revision",revision).putExtra("scheduled",scheduled),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
 fun cancel(c:Context){
  val alarm=c.getSystemService(AlarmManager::class.java)
  ReminderKind.entries.forEach{kind->runCatching{alarm.cancel(pending(c,kind))}}
  LifeCatalog.all.forEach{runCatching{alarm.cancel(lifePending(c,it.id))}}
 }
 fun schedule(c:Context,s:UserState){
  cancel(c)
  if(!s.ready || s.onboardingVersion<TRAINING_ONBOARDING_VERSION || !allowed(c) || s.life.notificationsMuted)return
  val nm=c.getSystemService(NotificationManager::class.java)
  ReminderKind.entries.forEach{kind->nm.createNotificationChannel(NotificationChannel(kind.channelId,kind.title,NotificationManager.IMPORTANCE_DEFAULT))}
  val now=ZonedDateTime.now()
  val candidates=mutableListOf<Triple<Long,Int,()->PendingIntent>>()
  fun scheduleOne(kind:ReminderKind,time:String){
   if(nm.getNotificationChannel(kind.channelId)?.importance==NotificationManager.IMPORTANCE_NONE)return
   val local=runCatching{LocalTime.parse(time)}.getOrNull()?:return
   val next=nextReminderAt(local,now)
   // Inexact alarms: no exact-alarm permission and no promise of minute-precise delivery.
   val epoch=next.toInstant().toEpochMilli()
   candidates+=Triple(epoch,if(kind==ReminderKind.SLEEP)0 else 1,{pending(c,kind,epoch)})
  }
  if(s.reminders)sleepReminderTime(s)?.let{scheduleOne(ReminderKind.SLEEP,it.toString())}
  if(s.workoutReminders && s.pausedOn==null && s.safety=="clear" && !isCycleComplete(s,now.toLocalDate()))scheduleOne(ReminderKind.WORKOUT,s.workoutReminderTime)
  nm.createNotificationChannel(NotificationChannel("elevare_routines","Seçtiğin rutinler",NotificationManager.IMPORTANCE_DEFAULT))
  if(nm.getNotificationChannel("elevare_routines")?.importance!=NotificationManager.IMPORTANCE_NONE)s.life.plans.forEach{p->
   nextRoutineAt(p,now)?.let{next->val epoch=next.toInstant().toEpochMilli();candidates+=Triple(epoch,2+LifeCatalog.all.indexOfFirst{it.id==p.id},{lifePending(c,p.id,p.revision,epoch)})}
  }
  // Exactly one future alarm: stable priority resolves collisions before delivery.
  candidates.minWithOrNull(compareBy<Triple<Long,Int,()->PendingIntent>>{it.first}.thenBy{it.second})?.let{next->
   runCatching{c.getSystemService(AlarmManager::class.java).setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,next.first,next.third())}
  }
 }
 private fun runtimeState(c:Context,s:UserState):UserState {
  val until=c.getSharedPreferences("elevare_active_v2",Context.MODE_PRIVATE).getLong("running_until",0)
  val running=until-System.currentTimeMillis() in 1..3_600_000L
  return s.copy(active=s.active?.copy(running=running))
 }
 fun show(c:Context)=show(c,ReminderKind.SLEEP,Store(c).state)
 fun show(c:Context,kind:ReminderKind,s:UserState,scheduled:Long=0){
  if(!allowed(c) || !s.ready || s.onboardingVersion<TRAINING_ONBOARDING_VERSION)return
  val now=ZonedDateTime.now()
  val live=runtimeState(c,s)
  if(s.life.notificationsMuted||live.active?.running==true||lifeQuiet(s.life,now.toLocalTime()))return
  if(scheduled>0&&(now.toInstant().toEpochMilli()-scheduled !in 0..90*60*1000L||Instant.ofEpochMilli(scheduled).atZone(now.zone).toLocalDate()!=now.toLocalDate()))return
  if(scheduled>0){
   val expected=if(kind==ReminderKind.SLEEP)sleepReminderTime(s) else runCatching{LocalTime.parse(s.workoutReminderTime)}.getOrNull()
   if(expected==null||Instant.ofEpochMilli(scheduled).atZone(now.zone).toLocalTime()!=expected)return
  }
  if(kind==ReminderKind.WORKOUT && !shouldRemindWorkout(live,now))return
  if(kind==ReminderKind.SLEEP && !shouldRemindSleep(s,now))return
  val nm=c.getSystemService(NotificationManager::class.java)
  nm.createNotificationChannel(NotificationChannel(kind.channelId,kind.title,NotificationManager.IMPORTANCE_DEFAULT))
  if(nm.getNotificationChannel(kind.channelId)?.importance==NotificationManager.IMPORTANCE_NONE)return
  val intent=Intent(c,MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP).putExtra("reminder_target",if(kind==ReminderKind.SLEEP)"sleep" else "home")
  val open=PendingIntent.getActivity(c,kind.requestCode+1,intent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
  val title=if(kind==ReminderKind.SLEEP)"Geceye alan aç" else "Bugünkü kısa antrenmanın hazır"
  val message=if(kind==ReminderKind.SLEEP)"Geceye hazırlanmak için biraz yavaşlamak ister misin?" else "Uygun olduğunda planını açıp kendi ritminde başlayabilirsin."
  val notification=NotificationCompat.Builder(c,kind.channelId)
   .setSmallIcon(com.elevare.active.R.drawable.ic_notification)
   .setContentTitle(title).setContentText(message).setContentIntent(open)
   .setAutoCancel(true).setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
   .setOnlyAlertOnce(true).build()
  // Record first so duplicate receiver deliveries cannot produce a second alert.
  if(!claim(c,kind.name,s,now))return
  runCatching{nm.notify(kind.requestCode,notification)}
 }
 @Synchronized private fun claim(c:Context,key:String,s:UserState,now:ZonedDateTime):Boolean {
  val prefs=c.getSharedPreferences("elevare_reminder_delivery_v1",Context.MODE_PRIVATE)
  val date=now.toLocalDate().toString();val epoch=now.toInstant().toEpochMilli()
  if(prefs.getString(key+"_date","")==date)return false
  val count=if(prefs.getString("budget_date","")==date)prefs.getInt("budget_count",0)else 0
  if(!deliveryBudgetAllows(count,prefs.getLong("last_any_epoch",0),epoch,s.life.budget))return false
  return prefs.edit().putString(key+"_date",date).putString("budget_date",date).putInt("budget_count",count+1).putLong("last_any_epoch",epoch).commit()
 }
 fun showLife(c:Context,s:UserState,id:String,revision:Long,scheduled:Long){
  val now=ZonedDateTime.now()
  if(!allowed(c)||!canDeliverLife(runtimeState(c,s),id,revision,scheduled,now))return
  // The first eligible receiver claims the shared budget. No deferred catch-up.
  val nm=c.getSystemService(NotificationManager::class.java)
  if(nm.getNotificationChannel("elevare_routines")?.importance==NotificationManager.IMPORTANCE_NONE)return
  nm.createNotificationChannel(NotificationChannel("elevare_routines","Seçtiğin rutinler",NotificationManager.IMPORTANCE_DEFAULT))
  val open=PendingIntent.getActivity(c,lifeCode(id)+200,Intent(c,MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP).putExtra("reminder_target","routine:$id"),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
  val notification=NotificationCompat.Builder(c,"elevare_routines").setSmallIcon(R.drawable.ic_notification)
   .setContentTitle("Küçük rutinine yer aç").setContentText("Seçtiğin adımlar hazır. Uygun olduğunda göz atabilirsin.")
   .setContentIntent(open).setAutoCancel(true).setOnlyAlertOnce(true).setVisibility(NotificationCompat.VISIBILITY_PRIVATE).build()
  if(claim(c,"routine:$id",s,now))runCatching{nm.notify(lifeCode(id),notification)}
 }
}
class SleepReminderReceiver:BroadcastReceiver(){
 override fun onReceive(c:Context,intent:Intent){
  val s=Store(c).state
  Reminder.show(c,ReminderKind.SLEEP,s,intent.getLongExtra("scheduled",0))
  Reminder.schedule(c,s)
 }
}
class WorkoutReminderReceiver:BroadcastReceiver(){
 override fun onReceive(c:Context,intent:Intent){
  val s=Store(c).state
  Reminder.show(c,ReminderKind.WORKOUT,s,intent.getLongExtra("scheduled",0))
  Reminder.schedule(c,s)
 }
}
class RoutineReminderReceiver:BroadcastReceiver(){
 override fun onReceive(c:Context,intent:Intent){
  val s=Store(c).state
  Reminder.showLife(c,s,intent.getStringExtra("routine")?:"",intent.getLongExtra("revision",0),intent.getLongExtra("scheduled",0))
  Reminder.schedule(c,s)
 }
}
class ReminderBootReceiver:BroadcastReceiver(){
 override fun onReceive(c:Context,intent:Intent){
  if(intent.action in setOf(Intent.ACTION_BOOT_COMPLETED,Intent.ACTION_TIME_CHANGED,Intent.ACTION_TIMEZONE_CHANGED,Intent.ACTION_MY_PACKAGE_REPLACED))
   Reminder.schedule(c,Store(c).state)
 }
}
