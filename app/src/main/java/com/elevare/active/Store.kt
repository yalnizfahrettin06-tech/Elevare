package com.elevare.active

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class Store(private val context:Context) {
 private val prefs=context.getSharedPreferences("elevare_active_v2",Context.MODE_PRIVATE)
 var error by mutableStateOf("")
  private set
 var recoveryRequired by mutableStateOf(false)
  private set
 var state by mutableStateOf(read())
  private set
 private fun read():UserState {
  val raw=prefs.getString("state",null)?:return UserState()
  return try { StateCodec.decode(raw) } catch(_:Exception) {
   recoveryRequired=true;error="Kayıtların açılamadı. Eski verilerin korunuyor; kurtarma dosyasını kaydedebilirsin."
   UserState()
  }
 }
 fun update(change:(UserState)->UserState):Boolean {
  if(recoveryRequired)return false
  return try {
   val changed=change(state)
   if(changed==state)return true
   val next=changed.copy(trialDays=normalizeTrialDays(changed.trialDays),dark=true,schemaVersion=9,
    sleepReminderLeadMinutes=normalizedSleepReminderLead(changed.sleepReminderLeadMinutes))
   if(!prefs.edit().putString("state",StateCodec.encode(next,::remainingRuntime))
    .putLong("running_until",if(next.active?.running==true)System.currentTimeMillis()+remainingRuntime(next.active)*1000L else 0L).commit()) {
    error="Kayıt kaydedilemedi. Alanını kontrol edip tekrar dene.";false
   } else {
    val previous=state;state=next;error=""
    if(previous.reminders!=next.reminders||previous.bed!=next.bed||previous.wake!=next.wake||
     previous.sleepReminderLeadMinutes!=next.sleepReminderLeadMinutes||previous.pausedOn!=next.pausedOn||
     previous.workoutReminders!=next.workoutReminders||previous.workoutReminderTime!=next.workoutReminderTime||
     previous.sessions.size!=next.sessions.size||previous.life!=next.life||previous.safety!=next.safety||previous.dailyReadiness!=next.dailyReadiness||previous.trainingDays!=next.trainingDays||previous.start!=next.start)runCatching{Reminder.schedule(context,next)}
    true
   }
  }catch(_:Exception){error="Değişiklik kaydedilemedi. Önceki kayıtların korunuyor.";false}
 }
 fun retryRead(){recoveryRequired=false;error="";state=read()}
 fun restore(raw:String):Boolean {
  if(state.active!=null){error="Önce açık seansını bitir veya bırak.";return false}
  return try {
   val next=validatedBackup(raw)
   val previous=prefs.getString("state",null)?:StateCodec.encode(state)
   if(!prefs.edit().putString("restore_previous",previous).putString("state",StateCodec.encode(next)).putLong("running_until",0).commit()){
    error="Yedek kaydedilemedi; mevcut veriler korundu.";false
   }else{state=next;recoveryRequired=false;error="";Reminder.cancel(context);true}
  }catch(e:Exception){error=e.message?:"Yedek doğrulanamadı; kayıtların değiştirilmedi.";false}
 }
 fun canUndoRestore()=prefs.contains("restore_previous")
 fun undoRestore():Boolean {
  if(state.active!=null){error="Önce açık seansını bitir veya bırak.";return false}
  val raw=prefs.getString("restore_previous",null)?:return false
  if(!prefs.edit().putString("state",raw).putLong("running_until",0).remove("restore_previous").commit()){error="Geri alınamadı.";return false}
  recoveryRequired=false;error="";state=read();Reminder.schedule(context,state);return true
 }
 fun reset():Boolean {
  if(!prefs.edit().clear().commit()){error="Kayıtlar silinemedi.";return false}
  Reminder.cancel(context)
  context.getSharedPreferences("elevare_reminder_delivery_v1",Context.MODE_PRIVATE).edit().clear().apply()
  context.getSystemService(android.app.NotificationManager::class.java).cancelAll()
  state=UserState();error="";recoveryRequired=false;return true
 }
 fun pauseTimer(){state.active?.takeIf{it.running}?.let{a->
  val paused=a.copy(remaining=remainingRuntime(a),running=false,deadline=0,elapsedRealtimeDeadline=0)
  // Losing storage must not leave an off-screen workout running. Keep the
  // write failure visible; the older persisted snapshot also restores paused.
  if(!update{it.copy(active=paused)})state=state.copy(active=paused)
 }}
 fun export():String=if(recoveryRequired)prefs.getString("state","{}")?:"{}" else StateCodec.encode(state.copy(active=null))
}
