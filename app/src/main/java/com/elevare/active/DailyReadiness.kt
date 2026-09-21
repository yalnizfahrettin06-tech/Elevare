package com.elevare.active
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import java.time.LocalDate

@Composable fun DailyReadinessDialog(store:Store,requestedId:String,onDismiss:()->Unit,onReady:(String)->Unit) {
 var readiness by remember{mutableStateOf("")}
 var indoors by remember{mutableStateOf(false)}
 var sports by remember{mutableStateOf(false)}
 var supports by remember{mutableStateOf(false)}
 val w=runCatching{Content.workout(requestedId)}.getOrNull()
 if(w==null){LaunchedEffect(Unit){onDismiss()};return}
 val today=LocalDate.now().toString()
 fun adaptedState(s:UserState)=s.copy(dailyCheckDate=today,dailyReadiness=readiness,dailyEnvironment=if(indoors)"indoor" else s.environment,
   externalSportDate=if(sports)today else s.externalSportDate,dailyEquipmentConfirmed=supports)
 val selectedDay=todayProgram(adaptedState(store.state))
 val chosen=if(requestedId=="breath")w else selectedDay.workout
 AlertDialog(onDismissRequest=onDismiss,title={Text("Bugün nasılsın?")},text={
  Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(10.dp)){
   listOf("ready" to "Hazırım","tired" to "Yorgunum","pain" to "Ağrı / rahatsızlık var").forEach{(id,label)->Choice(label,selected=readiness==id){readiness=id}}
   if(readiness=="pain")QuietText("Seans başlatılmayacak. Yeniden başlamadan önce kısa uygunluk kontrolünü tamamlaman gerekecek; tarih değişmesi bu kontrolü kaldırmaz. Gerekirse bir sağlık uzmanından destek al.")
   else {
    QuietText("${chosen.title} · ${minutesText(chosen.seconds)}")
    QuietText(chosen.equipment)
    if(chosen.id!=w.id)QuietText("Yanıtına göre seans uyarlandı.")
    if(store.state.environment!="indoor")SettingToggle("Bugün evdeyim","Yalnız bugünkü seans uyarlanır",indoors){indoors=it}
    SettingToggle("Yakın zamanda yoğun spor yaptım","Ek koşu yerine toparlanma",sports){sports=it}
    Row(Modifier.fillMaxWidth().heightIn(min=48.dp).toggleable(value=supports,role=Role.Checkbox,onValueChange={supports=it}),verticalAlignment=Alignment.CenterVertically) {
     Checkbox(supports,onCheckedChange=null)
     Text("Alanım ve gerekli destekler hazır",Modifier.weight(1f).padding(vertical=8.dp))
    }
    if(selectedDay.plannedTemplateCode.startsWith("K"))QuietText("Koşu ve destekler aynı güvenli alanda olmalı.")
    QuietText(if(store.state.age<18)"İlk denemede güvendiğin bir yetişkinden destek al. Ağrı veya baş dönmesinde dur." else "Ağrı veya baş dönmesinde dur. Tekniğinden emin değilsen destek al.")
   }
  }
 },confirmButton={TextButton(enabled=readiness.isNotBlank()&&(supports||readiness=="pain"),onClick={
  val ok=store.update{adaptedState(it)}
  if(ok){
   if(readiness=="pain")onDismiss()
   else {
    val adapted=if(requestedId=="breath")"breath" else todayProgram(store.state).workout.id
    onReady(adapted)
   }
  }
 }){Text(if(readiness=="pain")"Şimdilik dur" else "Başla")}},dismissButton={TextButton(onClick=onDismiss){Text("Şimdi değil")}})
}

@Composable fun DemoStatusScreen(store:Store,onBack:()->Unit) {
 val s=store.state
 val used=s.trialDays>0||s.demoStartedAt>0
 val remaining=demoRemainingMillis(s)
 PageColumn {
  TopBar("3 günlük demo",onBack)
  Text(when{!used->"Rutinini keşfet.";remaining>0->"Demon açık.";else->"Demo önizlemen tamamlandı."},style=MaterialTheme.typography.headlineLarge)
  if(remaining>0)QuietText("Yaklaşık ${(remaining+3_599_999)/3_600_000} saat kaldı.")
  InfoCard("Bu prototipte ücret, ödeme bilgisi ve otomatik yenileme yok. Geçmişin ve antrenmanlarına erişimin korunur.")
  if(!used&&trainingAllowed(s))BigButton("3 günlük denemeyi başlat",{store.update{startDemo(it)}},icon=ArcIcons.Play)
  else BigButton("Uygulamaya dön",onBack)
 }
}
