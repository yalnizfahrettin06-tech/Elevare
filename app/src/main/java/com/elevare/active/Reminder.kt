package com.elevare.active
import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import java.time.*

object Reminder{
 private const val channel="elevare_sleep"
 fun allowed(c:Context):Boolean=(Build.VERSION.SDK_INT<33||c.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED)&&c.getSystemService(NotificationManager::class.java).areNotificationsEnabled()
 private fun pending(c:Context)=PendingIntent.getBroadcast(c,31,Intent(c,SleepReminderReceiver::class.java),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
 fun cancel(c:Context){c.getSystemService(AlarmManager::class.java).cancel(pending(c))}
 fun schedule(c:Context,s:UserState){
  cancel(c);if(!s.reminders||!allowed(c))return
  val nm=c.getSystemService(NotificationManager::class.java)
  nm.createNotificationChannel(NotificationChannel(channel,"Uyku hazırlığı",NotificationManager.IMPORTANCE_DEFAULT))
  val now=ZonedDateTime.now();val t=LocalTime.parse(s.bed).minusMinutes(30)
  var next=now.withHour(t.hour).withMinute(t.minute).withSecond(0).withNano(0)
  if(!next.isAfter(now))next=next.plusDays(1)
  c.getSystemService(AlarmManager::class.java).setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,next.toInstant().toEpochMilli(),pending(c))
 }
 fun show(c:Context){
  if(!allowed(c))return
  val open=PendingIntent.getActivity(c,32,Intent(c,MainActivity::class.java),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
  c.getSystemService(NotificationManager::class.java).notify(31,NotificationCompat.Builder(c,channel).setSmallIcon(com.elevare.active.R.drawable.ic_notification).setContentTitle("Geceye alan aç").setContentText("Uyku planına yaklaşırken ekranı bırakıp yavaşlamayı deneyebilirsin.").setContentIntent(open).setAutoCancel(true).setVisibility(NotificationCompat.VISIBILITY_PRIVATE).build())
 }
}
class SleepReminderReceiver:BroadcastReceiver(){override fun onReceive(c:Context,intent:Intent){val s=Store(c).state;if(s.reminders){Reminder.show(c);Reminder.schedule(c,s)}}}
class ReminderBootReceiver:BroadcastReceiver(){override fun onReceive(c:Context,intent:Intent){Reminder.schedule(c,Store(c).state)}}
