package com.elevare.active

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

@Composable fun ProgressJournal(s:UserState,onBack:()->Unit,onSleep:()->Unit){
 var scope by rememberSaveable{mutableStateOf("cycle")}
 var type by rememberSaveable{mutableStateOf("all")}
 var limit by rememberSaveable(scope,type){mutableIntStateOf(20)}
 val records=progressRecords(s,scope,type)
 val workouts=records.filter{it.completed&&it.type=="workout"}
 PageColumn{
  TopBar("İlerlemem",onBack)
  Text("Küçük adımlar.\nGerçek kayıtlar.",style=MaterialTheme.typography.headlineLarge)
  Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)){
   listOf("cycle" to "Bu döngü","week" to "Son 7 gün","all" to "Tüm zamanlar").forEach{(id,label)->FilterChip(scope==id,{scope=id},label={Text(label)})}
  }
  QuietText(when(scope){"cycle"->"${s.start} başlangıçlı döngü · Gün ${journeyDay(s)} / 90";"week"->"${LocalDate.now().minusDays(6)} — ${LocalDate.now()}";else->"Kayıtlı tüm döngüler"})
  Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){
   Metric("${workouts.size}","tamamlanan antrenman",Mint,Modifier.weight(1f))
   Metric("${workouts.sumOf{it.seconds}/60}","antrenman dakikası",Track,Modifier.weight(1f))
  }
  Surface(color=Mint,shape=RoundedCornerShape(18.dp)){
   Column(Modifier.fillMaxWidth().padding(16.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){
    Eyebrow("KAYITLARIN NE SÖYLÜYOR?")
    Text(if(workouts.isEmpty())"İlk adımın için yer var." else "${workouts.map{it.date}.distinct().size} farklı günde hareket ettin.",fontWeight=FontWeight.Bold)
    QuietText("${records.count{!it.completed}} kısmi seans · ${records.count{it.completed&&it.type=="breath"}} nefes molası. Bunlar antrenman sayısına eklenmez.")
   }
  }
  BlockTitle("Seans günlüğü")
  Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(6.dp)){
   listOf("all" to "Hepsi","workout" to "Antrenman","breath" to "Nefes","partial" to "Kısmi").forEach{(id,label)->FilterChip(type==id,{type=id},label={Text(label)})}
  }
  if(records.isEmpty())QuietText("Bu aralıkta kayıt yok. Geçmişin değişmedi; başka bir dönem seçebilirsin.")
  records.take(limit).forEach{l->
   Surface(color=Track,shape=RoundedCornerShape(14.dp)){
    Column(Modifier.fillMaxWidth().padding(14.dp),verticalArrangement=Arrangement.spacedBy(5.dp)){
     Text(l.title,fontWeight=FontWeight.Bold)
     QuietText("${l.date} · ${minutesText(l.seconds)} · ${if(l.completed)"Tamamlandı" else "Kısmi"}")
     if(l.feeling.isNotBlank())Text(l.feeling,fontSize=13.sp,color=Sky)
    }
   }
  }
  if(records.size>limit)OutlinedButton(onClick={limit+=20},modifier=Modifier.fillMaxWidth()){Text("Daha eski kayıtlar (${records.size-limit})")}
  MenuRow("Uyku günlüğüm","${s.sleeps.size} gece kaydı",ArcIcons.Moon,onSleep)
  if(s.archivedCycles.isNotEmpty())ExpandSection("Önceki döngüler",ArcIcons.History){
   s.archivedCycles.asReversed().forEach{c->Text("${c.start} → ${c.end} · ${c.completedSessions} tamamlanan gün");QuietText("Geçmiş plan oranı gösterilmez; tercihler dönem içinde değişmiş olabilir.")}
  }
 }
}

@Composable fun PlanStudio(store:Store,onBack:()->Unit){
 val s=store.state
 var draft by remember(s.age,s.focus,s.dailyMinutes,s.trainingDays,s.environment,s.equipment,s.safety){mutableStateOf(s.answers())}
 var field by rememberSaveable{mutableStateOf("")}
 var confirm by remember{mutableStateOf(false)}
 var saved by remember{mutableStateOf(false)}
 val changed=draft!=s.answers()
 val preview=remember(s,draft){nextTrainingPreview(applyPlanPreferences(s,draft),LocalDate.now())}
 PageColumn{
  TopBar("Plan atölyesi",onBack)
  Eyebrow("SENİN HAFTAN · SENİN KARARIN")
  Text("Hayat değişir.\nPlanın uyum sağlar.",style=MaterialTheme.typography.headlineLarge)
  QuietText("Yalnız değiştirmek istediğin tercihe dokun. Kaydedene kadar mevcut planın korunur.")
  listOf("minutes" to "Seans süresi: ${draft.minutes} dk","days" to "Haftalık ritim: ${draft.days} gün",
   "environment" to "Alan: ${ProfileChoices.environment[draft.environment]}","focus" to "Odak: ${focusLabel(draft.focus)}",
   "equipment" to "Destekler: ${draft.equipment.mapNotNull{ProfileChoices.equipment[it]}.joinToString()}",
   "safety" to "Hareket uygunluğu", "age" to "Yaş: ${draft.age}","activity" to "Aktivite düzenim",
   "running" to "Koşu deneyimim","sleep" to "Uyku düzenim","growth" to "Büyüme gözlemim").forEach{(id,label)->
    MenuRow(label,icon=if(id=="safety")ArcIcons.Shield else ArcIcons.Settings){field=id;saved=false}
   }
  Surface(color=Mint,shape=RoundedCornerShape(18.dp)){
   Column(Modifier.fillMaxWidth().padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
    Eyebrow("KAYDETMEDEN ÖNCE")
    if(s.active!=null)Text("Açık seansın var. Önce onu bitir veya bırak.")
    else if(preview!=null){
     Text("${preview.day}. gün · ${preview.workout.title}",fontWeight=FontWeight.Bold)
     Text(workoutDistribution(preview.workout),fontSize=14.sp)
     QuietText("${s.trainingDays} → ${draft.days} gün / hafta · ${s.dailyMinutes} → ${draft.minutes} dk")
    }else QuietText("Yeni tercihlerin kaydolur. Duraklatılmış veya bitmiş döngünde yeni bir seans başlatılmaz.")
    QuietText("Şu anda hazırlık akışları açık. Koşu, güç ve yoga programları uzman incelemesi bekliyor.")
   }
  }
  BigButton(if(saved)"Plan tercihin kaydedildi" else "Değişiklikleri incele",{confirm=true},enabled=changed&&draft.complete()&&s.active==null)
 }
 if(field.isNotEmpty())AlertDialog(onDismissRequest={field=""},title={Text("Tercihini düzenle")},text={
  Column(Modifier.heightIn(max=380.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(6.dp)){
   fun set(a:TrainingAnswers){draft=a;if(field!="equipment")field=""}
   when(field){
    "minutes"->ProfileChoices.minutes.forEach{Choice("$it dk",selected=draft.minutes==it){set(draft.copy(minutes=it))}}
    "days"->ProfileChoices.days.forEach{Choice("$it gün",selected=draft.days==it){set(draft.copy(days=it))}}
    "age"->ProfileChoices.ages.forEach{Choice("$it yaş",selected=draft.age==it){set(draft.copy(age=it))}}
    "equipment"->ProfileChoices.equipment.forEach{(id,label)->Choice(label,selected=id in draft.equipment){set(draft.copy(equipment=toggleEquipment(draft.equipment,id)))}}
    else->{
     val options=when(field){"environment"->ProfileChoices.environment;"focus"->ProfileChoices.focus;"safety"->ProfileChoices.safety;"activity"->ProfileChoices.activity;"running"->ProfileChoices.runningExperience;"sleep"->ProfileChoices.sleep;else->ProfileChoices.growth}
     val selected=when(field){"environment"->draft.environment;"focus"->draft.focus;"safety"->draft.safety;"activity"->draft.activity;"running"->draft.runningExperience;"sleep"->draft.sleep;else->draft.recentGrowth}
     options.forEach{(id,label)->Choice(label,selected=selected==id){set(when(field){"environment"->draft.copy(environment=id);"focus"->draft.copy(focus=id);"safety"->draft.copy(safety=id);"activity"->draft.copy(activity=id);"running"->draft.copy(runningExperience=id);"sleep"->draft.copy(sleep=id);else->draft.copy(recentGrowth=id)})}}
    }
   }
  }
 },confirmButton={TextButton(onClick={field=""}){Text("Tamam")}})
 if(confirm)AlertDialog(onDismissRequest={confirm=false},title={Text("Plan tercihleri güncellensin mi?")},text={Text("Sonraki seanslar yeni tercihlerle düzenlenecek. Başlangıç tarihi, geçmiş ve dinlenme ihtiyacı korunur. Seviye onayları yeniden değerlendirilir; sağlık veya hormon hesabı yapılmaz.")},confirmButton={TextButton(onClick={if(store.update{applyPlanPreferences(it,draft)}){confirm=false;saved=true}}){Text("Planımı güncelle")}},dismissButton={TextButton(onClick={confirm=false}){Text("Vazgeç")}})
}

@Composable fun SafetyReview(store:Store,onBack:()->Unit,onProfile:()->Unit){
 var acknowledged by rememberSaveable{mutableStateOf(false)}
 PageColumn{
  TopBar("Hareket uygunluğu",onBack)
  Text("Önce nasıl olduğunu\nnetleştirelim.",style=MaterialTheme.typography.headlineLarge)
  QuietText("Önceki rahatsızlık bildirimin nedeniyle seanslar durdu. Bu ekran tanı koymaz; tarih değişince kendiliğinden onay verilmez.")
  if(store.state.safety!="clear"){
   InfoCard("Profilinde hareketi etkileyen bir durum var. Uygunluğunu gerektiğinde uzman desteğiyle netleştir.",ArcIcons.Shield)
   BigButton("Uygunluk yanıtımı düzenle",onProfile)
  }else{
   Choice("Belirti devam ediyor / emin değilim",selected=!acknowledged){acknowledged=false}
   Choice("Artık rahatsızlık bildirmiyorum; varsa uzman kısıtlamama uyuyorum",selected=acknowledged){acknowledged=true}
   QuietText("Ağrı veya baş dönmesi yeniden olursa dur. Bu yanıt tıbbi onay yerine geçmez.")
   BigButton("Günlük kontrole dön",{if(store.update{reviewMovementSafety(it,acknowledged)})onBack()},enabled=acknowledged)
  }
 }
}

@Composable fun BackupScreen(store:Store,onBack:()->Unit,onExport:()->Unit){
 val context=LocalContext.current;val scope=rememberCoroutineScope()
 var raw by remember{mutableStateOf<String?>(null)}
 var candidate by remember{mutableStateOf<UserState?>(null)}
 var issue by remember{mutableStateOf("")}
 var busy by remember{mutableStateOf(false)}
 var undo by remember{mutableStateOf(false)}
 val picker=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){uri->
  if(uri!=null){busy=true;scope.launch{
   val result=withContext(Dispatchers.IO){runCatching{
    val bytes=context.contentResolver.openInputStream(uri)?.use{stream->
     val out=java.io.ByteArrayOutputStream();val buffer=ByteArray(8192)
     while(true){val count=stream.read(buffer);if(count<0)break;require(out.size()+count<=4*1024*1024){"Dosya 4 MB sınırını aşıyor."};out.write(buffer,0,count)}
     out.toByteArray()
    }?:error("Dosya açılamadı.")
    val text=String(bytes,Charsets.UTF_8);text to validatedBackup(text)
   }}
   result.onSuccess{raw=it.first;candidate=it.second;issue=""}.onFailure{issue=it.message?:"Dosya doğrulanamadı."};busy=false
  }}
 }
 PageColumn{
  TopBar("Kayıtlarım güvende",onBack)
  Text("Yedeğini yanında taşı.",style=MaterialTheme.typography.headlineLarge)
  QuietText("Bulut hesabı gerekmez. Dosya kişisel yanıtlarını ve kayıtlarını içerir; güvenli bir yerde sakla.")
  BigButton("Yedek dosyası oluştur",onExport,icon=ArcIcons.Download)
  OutlinedButton(onClick={picker.launch(arrayOf("application/json","text/plain","application/octet-stream"))},enabled=!busy&&store.state.active==null,modifier=Modifier.fillMaxWidth()){Text(if(busy)"Dosya kontrol ediliyor…" else "Dosyadan geri yükle")}
  if(store.state.active!=null)QuietText("Geri yüklemeden önce açık seansını bitir veya bırak.")
  QuietText("Geri yükleme mevcut verilerin yerine geçer; birleştirme yapılmaz. Önceki kayıtların cihazda tek adımlık geri alma için korunur. Bildirimler kapalı, yarım seans duraklatılmış olarak gelir.")
  if(issue.isNotBlank())InfoCard(issue,ArcIcons.Info)
  if(store.canUndoRestore())TextButton(onClick={undo=true}){Text("Son geri yüklemeyi geri al")}
 }
 candidate?.let{c->AlertDialog(onDismissRequest={candidate=null;raw=null},title={Text("Yedeği geri yükle?")},text={Text("Dosyada ${c.sessions.size} seans, ${c.sleeps.size} uyku kaydı ve ${c.life.plans.size} rutin var. Şu anki kayıtlarının yerine geçecek. İşlemden sonra eklediğin kayıtlar geri alma sırasında kaldırılır.")},confirmButton={TextButton(onClick={if(raw!=null&&store.restore(raw!!)){candidate=null;raw=null;issue="Yedek yüklendi. Hatırlatmalar kapalı."}else issue=store.error}){Text("Bu yedeği yükle")}},dismissButton={TextButton(onClick={candidate=null;raw=null}){Text("Vazgeç")}})}
 if(undo)AlertDialog(onDismissRequest={undo=false},title={Text("Önceki kayıtlara dön?")},text={Text("Geri yüklemeden sonra yaptığın değişiklikler kaldırılır. Yalnız cihazda saklanan önceki kayda dönülür.")},confirmButton={TextButton(onClick={if(store.undoRestore()){undo=false;issue="Önceki kayıtlarına dönüldü."}else issue=store.error}){Text("Geri al")}},dismissButton={TextButton(onClick={undo=false}){Text("Vazgeç")}})
}

@Composable fun VoiceCheck(store:Store){
 val context=LocalContext.current
 val audio=remember(context){CoachAudio(context){}}
 val lifecycle=(context as? androidx.activity.ComponentActivity)?.lifecycle
 DisposableEffect(audio,lifecycle){
  val observer=androidx.lifecycle.LifecycleEventObserver{_,event->if(event==androidx.lifecycle.Lifecycle.Event.ON_STOP)audio.stop()}
  lifecycle?.addObserver(observer)
  onDispose{lifecycle?.removeObserver(observer);audio.close()}
 }
 TextButton(onClick={audio.configure(true,"off",true);audio.speak("Elevare. Rahat adımlar, doğal nefes. Bu bir ses denemesidir.")}){Icon(ArcIcons.Sound,null);Spacer(Modifier.width(8.dp));Text("Türkçe sesi dene")}
 if(audio.status.isNotBlank())QuietText(audio.status)
 QuietText("Ses cihazındaki Türkçe pakete bağlı. Ekranı kapattığında seans ve koç durur; eller serbest mod henüz açık değil.")
}
