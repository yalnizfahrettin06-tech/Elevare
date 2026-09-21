package com.elevare.active
import android.Manifest
import android.app.TimePickerDialog
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import java.time.LocalTime

@Composable fun WorkoutReminderSettings(store:Store,notify:(String)->Unit) {
 val s=store.state
 val c=LocalContext.current
 val permission=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){granted->
  store.update{it.copy(workoutReminders=granted)}
  notify(if(granted)"Antrenman hatırlatıcısı açıldı." else "Bildirimler kapalı. Antrenmanlarına devam edebilirsin.")
 }
 SettingToggle("Antrenman hatırlatıcısı","Yalnız uygun antrenman günlerinde",s.workoutReminders){enabled->
  if(!enabled)store.update{it.copy(workoutReminders=false)}
  else if(Build.VERSION.SDK_INT>=33&&!Reminder.allowed(c))permission.launch(Manifest.permission.POST_NOTIFICATIONS)
  else if(Reminder.allowed(c))store.update{it.copy(workoutReminders=true)}
  else notify("Bildirimler Android ayarlarında kapalı.")
 }
 if(s.workoutReminders)TextButton(onClick={
  val t=LocalTime.parse(s.workoutReminderTime)
  TimePickerDialog(c,{_,h,m->store.update{it.copy(workoutReminderTime="%02d:%02d".format(h,m))}},t.hour,t.minute,true).show()
 }){Text("Hatırlatma saati · ${s.workoutReminderTime}")}
 QuietText("İzin isteğe bağlı. Dinlenme günlerinde ve seansını tamamladığında antrenman bildirimi gönderilmez.")
}
