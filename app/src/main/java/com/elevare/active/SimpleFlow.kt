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

fun beginWorkout(s:UserState,id:String,now:Long=System.currentTimeMillis()):UserState {
 if(s.pausedOn!=null||s.active!=null||!trainingAllowed(s))return s
 val w=runCatching{Content.workout(id)}.getOrNull()?:return s
 if(w.hasSprint()){
  if(s.environment !in listOf("outdoor","both")||s.sleepHabit=="under6"||s.gentle)return s
  val recent=s.sessions.any{it.title=="Sprint intervalleri"&&runCatching{ChronoUnit.DAYS.between(LocalDate.parse(it.date),LocalDate.now()) in 0..1}.getOrDefault(false)}
  if(recent)return s
 }
 val first=w.steps.first()
 return s.copy(active=ActiveSession(w.id,remaining=first.seconds,deadline=now+first.seconds*1000L,planDay=journeyDay(s)))
}
fun resumePlan(s:UserState):UserState=if(s.pausedOn==null)s else s.copy(pausedDays=s.pausedDays+ChronoUnit.DAYS.between(LocalDate.parse(s.pausedOn),LocalDate.now()).coerceAtLeast(0),pausedOn=null)

@Composable fun CompactProfileScreen(store:Store,onSources:()->Unit,onSleep:()->Unit,onExport:()->Unit,onDelete:()->Unit,onPlan:()->Unit,onProgress:()->Unit,onTrial:()->Unit,notify:(String)->Unit){
 val s=store.state
 var name by rememberSaveable{mutableStateOf(s.name)}
 var pause by remember{mutableStateOf(false)}
 PageColumn{
  TopBar(if(s.name.isBlank())"Profil" else s.name)
  QuietText("${s.sessions.size} seans · Haftada ${s.trainingDays} gün · ${s.dailyMinutes} dk")
  MenuRow("İlerlemem",icon=Icons.Rounded.BarChart,onClick=onProgress)
  MenuRow("Takvim",icon=Icons.Rounded.CalendarMonth,onClick=onPlan)
  ExpandSection("Kişisel bilgiler",Icons.Rounded.PersonOutline){
   OutlinedTextField(value=name,onValueChange={name=it.take(18)},label={Text("İsim veya takma ad")},singleLine=true,modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(14.dp))
   TextButton(onClick={store.update{it.copy(name=name.trim())};notify("İsmin kaydedildi.")}){Text("İsmi kaydet")}
   QuietText("${s.age} yaş · Günlük süre tercihi ${s.dailyMinutes} dk")
   QuietText("Öncelik: ${focusLabel(s.focus)}")
   QuietText("Yanıtlar hormon düzeyini veya büyüme potansiyelini ölçmez.")
   TextButton(onClick={store.update{it.copy(onboardingVersion=0)}}){Text("Yanıtlarımı düzenle")}
  }
  ExpandSection("Uyku ve bildirimler",Icons.Rounded.Bedtime){
   MenuRow("Uyku planı","${s.bed} → ${s.wake}",Icons.Rounded.Schedule,onSleep)
   GrowthProfileHeader(store,notify)
  }
  ExpandSection("Sesli koç",Icons.Rounded.VolumeUp){
   SettingToggle("Sesli anlatım","Cihazdaki çevrimdışı Türkçe ses",s.voiceCoach){v->store.update{it.copy(voiceCoach=v)}}
   listOf("off" to "Süre sesi kapalı","countdown" to "Son 3 saniye","every_second" to "Her saniye").forEach{(value,label)->Choice(label,selected=s.timerSound==value){store.update{it.copy(timerSound=value)}}}
  }
  ExpandSection("Görünüm ve tercihler",Icons.Rounded.Tune){
   QuietText("Workout görünümü · gece mavisi")
   SettingToggle("Azaltılmış hareket","Figür ve nefes animasyonunu durdur",s.reducedMotion){v->store.update{it.copy(reducedMotion=v)}}
   SettingToggle("Titreşim","",s.haptic){v->store.update{it.copy(haptic=v)}}
   SettingToggle("Hafif antrenman","",s.gentle){v->store.update{it.copy(gentle=v)}}
   OutlinedButton(onClick={pause=true},modifier=Modifier.fillMaxWidth()){Text(if(s.pausedOn==null)"Plana ara ver" else "Plana devam et")}
  }
  ExpandSection("Gizlilik ve yardım",Icons.Rounded.Lock){
   QuietText("Kayıtlar yalnızca cihazında. Uygulamayı kaldırmak kayıtlarını siler.")
   MenuRow("Bilgi ve kaynaklar",icon=Icons.Rounded.MenuBook,onClick=onSources)
   MenuRow("Kayıtları dışa aktar",icon=Icons.Rounded.Download,onClick=onExport)
   TextButton(onClick=onDelete){Text("Tüm kayıtları sil",color=MaterialTheme.colorScheme.error)}
  }
  MenuRow("Elevare Pro","3 günlük demo",Icons.Rounded.AutoAwesome,onTrial)
  QuietText("Elevare 0.7.0")
 }
 if(pause)AlertDialog(onDismissRequest={pause=false},title={Text(if(s.pausedOn==null)"Plana ara ver?" else "Plana devam et?")},text={Text("Takvim tercihin değişir; geçmiş kayıtların korunur.")},confirmButton={TextButton(onClick={store.pauseTimer();store.update{if(it.pausedOn==null)it.copy(pausedOn=LocalDate.now().toString())else resumePlan(it)};pause=false}){Text("Onayla")}},dismissButton={TextButton(onClick={pause=false}){Text("Vazgeç")}})
}
