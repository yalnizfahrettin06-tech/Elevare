package com.elevare.active

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable fun CompactProfileScreen(store:Store,onSources:()->Unit,onSleep:()->Unit,onExport:()->Unit,onDelete:()->Unit,onPlan:()->Unit,onProgress:()->Unit,onTrial:()->Unit,notify:(String)->Unit){
 val s=store.state
 var name by rememberSaveable{mutableStateOf(s.name)}
 var pause by remember{mutableStateOf(false)}
 PageColumn{
  if(s.name.isNotBlank())Text(s.name,style=MaterialTheme.typography.titleLarge)
  QuietText("${s.sessions.count{it.completed&&it.type=="workout"}} seans · Haftada ${s.trainingDays} gün · ${s.dailyMinutes} dk")
  MenuRow("İlerlemem",icon=ArcIcons.Progress,onClick=onProgress)
  MenuRow("Takvim",icon=ArcIcons.Program,onClick=onPlan)
  ExpandSection("Kişisel bilgiler",ArcIcons.Person){
   OutlinedTextField(value=name,onValueChange={name=it.take(18)},label={Text("İsim veya takma ad")},singleLine=true,modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(14.dp))
   TextButton(onClick={if(store.update{it.copy(name=name.trim())})notify("İsmin kaydedildi.")else notify(store.error)}){Text("İsmi kaydet")}
   QuietText("${s.age} yaş · Günlük süre tercihi ${s.dailyMinutes} dk")
   QuietText("Öncelik: ${focusLabel(s.focus)}")
   QuietText("Yanıtlar hormon düzeyini veya büyüme potansiyelini ölçmez.")
   TextButton(onClick={store.pauseTimer();store.update{it.copy(onboardingVersion=0)}}){Text("Yanıtlarımı düzenle")}
   QuietText("Güncelleme sonraki seansları değiştirir. Geçmişin korunur.")
  }
  ExpandSection("Uyku ve bildirimler",ArcIcons.Moon){
   MenuRow("Uyku planı","${s.bed} → ${s.wake}",ArcIcons.Clock,onSleep)
   GrowthProfileHeader(store,notify)
   WorkoutReminderSettings(store,notify)
   LifeNotificationSettings(store)
  }
  ExpandSection("Sesli koç",ArcIcons.Sound){
   SettingToggle("Sesli anlatım","Cihazdaki çevrimdışı Türkçe ses",s.voiceCoach){v->store.update{it.copy(voiceCoach=v)}}
   listOf("off" to "Süre sesi kapalı","countdown" to "Son 3 saniye","every_second" to "Her saniye").forEach{(value,label)->Choice(label,selected=s.timerSound==value){store.update{it.copy(timerSound=value)}}}
  }
  ExpandSection("Görünüm ve tercihler",ArcIcons.Settings){
   QuietText("Training Arc · sıcak kömür ve mercan")
   SettingToggle("Azaltılmış hareket","Figür ve nefes animasyonunu durdur",s.reducedMotion){v->store.update{it.copy(reducedMotion=v)}}
   SettingToggle("Titreşim","",s.haptic){v->store.update{it.copy(haptic=v)}}
   SettingToggle("Hafif antrenman","",s.gentle){v->store.update{it.copy(gentle=v)}}
   OutlinedButton(onClick={pause=true},modifier=Modifier.fillMaxWidth()){Text(if(s.pausedOn==null)"Plana ara ver" else "Plana devam et")}
  }
  ExpandSection("Gizlilik ve yardım",ArcIcons.Lock){
   QuietText("Kayıtlar yalnızca cihazında. Uygulamayı kaldırmak kayıtlarını siler.")
   MenuRow("Bilgi ve kaynaklar",icon=ArcIcons.Book,onClick=onSources)
   MenuRow("Kayıtları dışa aktar",icon=ArcIcons.Download,onClick=onExport)
   if(s.onboardingDraft!=null)TextButton(onClick={if(store.update{it.copy(onboardingDraft=null)})notify("Başlangıç taslağı silindi.")else notify(store.error)}){Text("Kaydedilen form taslağını sil")}
   TextButton(onClick=onDelete){Text("Tüm kayıtları sil",color=MaterialTheme.colorScheme.error)}
  }
  MenuRow("Elevare Pro önizlemesi","Farkını deneyimle · Ödeme yok",ArcIcons.Spark,onTrial)
  QuietText("Elevare 0.11.0 · Training Arc · Kendi ritmini kur")
 }
 if(pause)AlertDialog(onDismissRequest={pause=false},title={Text(if(s.pausedOn==null)"Plana ara ver?" else "Plana devam et?")},text={Text("Takvim tercihin değişir; geçmiş kayıtların korunur.")},confirmButton={TextButton(onClick={store.pauseTimer();store.update{if(it.pausedOn==null)it.copy(pausedOn=LocalDate.now().toString())else resumePlan(it)};pause=false}){Text("Onayla")}},dismissButton={TextButton(onClick={pause=false}){Text("Vazgeç")}})
}
