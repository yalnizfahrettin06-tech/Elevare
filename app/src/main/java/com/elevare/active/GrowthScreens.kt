package com.elevare.active

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*

const val TRIAL_DAYS=3
const val ONBOARDING_STEPS=10
fun normalizeTrialDays(value:Int)=if(value>0)TRIAL_DAYS else 0
fun validHeight(value:String)=value.isBlank()||(value.toIntOrNull()?.let{it in 90..230}==true)
fun shortWorkoutTitle(w:Workout)=w.title.lowercase(java.util.Locale.forLanguageTag("tr")).replaceFirstChar{it.titlecase(java.util.Locale.forLanguageTag("tr"))}
fun evidenceLabel(id:String)=when(id){"milk"->"Süt ve büyüme";"sprint"->"Sprint ve büyüme hormonu";"breath"->"Nefes ve rahatlama";"breath-gh"->"Nefes ve hormonlar";"uv"->"Güneşlenme iddiaları";"bone"->"Kemik sağlığı";else->Research.article(id).title}
@Composable fun Eyebrow(text:String){Text(text,fontSize=11.sp,fontWeight=FontWeight.SemiBold,letterSpacing=1.sp,color=MaterialTheme.colorScheme.primary)}
@Composable fun QuietText(text:String){Text(text,fontSize=13.sp,lineHeight=19.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)}
@Composable fun Choice(title:String,subtitle:String="",selected:Boolean,onClick:()->Unit){
 Surface(onClick=onClick,shape=RoundedCornerShape(14.dp),color=if(selected)MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,border=BorderStroke(1.dp,if(selected)MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),modifier=Modifier.fillMaxWidth().semantics{this.selected=selected;role=Role.RadioButton}){
  Row(Modifier.heightIn(min=52.dp).padding(horizontal=16.dp,vertical=12.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){
   Column(Modifier.weight(1f)){Text(title,fontWeight=FontWeight.Medium,fontSize=16.sp);if(subtitle.isNotBlank())QuietText(subtitle)}
   Icon(if(selected)Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,null,tint=MaterialTheme.colorScheme.primary,modifier=Modifier.size(22.dp))
  }
 }
}
@Composable fun MenuRow(title:String,subtitle:String="",icon:ImageVector=Icons.Rounded.ChevronRight,onClick:()->Unit){
 Surface(onClick=onClick,color=MaterialTheme.colorScheme.surface,shape=RoundedCornerShape(16.dp),modifier=Modifier.fillMaxWidth()){
  Row(Modifier.heightIn(min=56.dp).padding(14.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){
   Icon(icon,null,tint=MaterialTheme.colorScheme.primary,modifier=Modifier.size(22.dp))
   Column(Modifier.weight(1f)){Text(title,fontSize=15.sp,fontWeight=FontWeight.Medium);if(subtitle.isNotEmpty())QuietText(subtitle)}
   Icon(Icons.Rounded.ChevronRight,null,Modifier.size(20.dp))
  }
 }
}
@Composable fun ExpandSection(title:String,icon:ImageVector,content:@Composable ColumnScope.()->Unit){
 var expanded by rememberSaveable{mutableStateOf(false)}
 Surface(shape=RoundedCornerShape(16.dp),color=MaterialTheme.colorScheme.surface,modifier=Modifier.fillMaxWidth()){
  Column{
   Row(Modifier.fillMaxWidth().clickable{expanded=!expanded}.semantics{stateDescription=if(expanded)"Açık" else "Kapalı"}.heightIn(min=56.dp).padding(14.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){
    Icon(icon,null,tint=MaterialTheme.colorScheme.primary,modifier=Modifier.size(22.dp));Text(title,Modifier.weight(1f),fontSize=15.sp,fontWeight=FontWeight.Medium);Icon(if(expanded)Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,null)
   }
   if(expanded)Column(Modifier.padding(start=16.dp,end=16.dp,bottom=16.dp),verticalArrangement=Arrangement.spacedBy(14.dp),content=content)
  }
 }
}

@Composable fun GrowthOnboarding(store:Store)=TrainingOnboarding(store)
@Composable fun TrialPreview(onFinish:(Int)->Unit,onSkip:()->Unit,onBack:(()->Unit)?=null){
 Column(Modifier.fillMaxSize()){
  Row(Modifier.padding(horizontal=20.dp,vertical=8.dp),verticalAlignment=Alignment.CenterVertically){if(onBack!=null)IconButton(onClick=onBack){Icon(Icons.Rounded.ArrowBack,"Geri")};Text("Elevare Pro",Modifier.weight(1f),fontSize=22.sp,fontWeight=FontWeight.Bold);Tag("DEMO")}
  Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(24.dp),verticalArrangement=Arrangement.spacedBy(22.dp)){
   Box(Modifier.size(64.dp).background(MaterialTheme.colorScheme.secondaryContainer,RoundedCornerShape(20.dp)),contentAlignment=Alignment.Center){Icon(Icons.Rounded.AutoAwesome,null,Modifier.size(30.dp),tint=MaterialTheme.colorScheme.primary)}
   Text("3 gün\nkendin için.",fontSize=36.sp,lineHeight=42.sp,fontWeight=FontWeight.Bold);QuietText("Rutinini keşfet, ilk antrenmanına başla.")
   Column(verticalArrangement=Arrangement.spacedBy(18.dp)){listOf("Kısa antrenmanlar","Çizimli hareket rehberi","Kaynaklı bilgi kartları").forEach{label->Row(horizontalArrangement=Arrangement.spacedBy(12.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Rounded.Check,null,tint=MaterialTheme.colorScheme.primary,modifier=Modifier.size(20.dp));Text(label,fontSize=15.sp)}}}
  }
  Column(Modifier.padding(horizontal=24.dp,vertical=14.dp),verticalArrangement=Arrangement.spacedBy(10.dp),horizontalAlignment=Alignment.CenterHorizontally){QuietText("Demo: ücret alınmaz, abonelik başlamaz.");BigButton("3 gün ücretsiz dene",{onFinish(TRIAL_DAYS)},icon=Icons.Rounded.AutoAwesome);TextButton(onClick=onSkip){Text("Şimdilik atla")}}
 }
}

@Composable fun GrowthHome(s:UserState,onWorkout:(String)->Unit,onStart:(String)->Unit,onSleep:()->Unit,onArticle:(String)->Unit,onResumePlan:()->Unit,onReview:()->Unit){
 val plan=routinePlan(s.answers(),s.gentle)
 val completed=s.sessions.lastOrNull{it.date==java.time.LocalDate.now().toString()&&it.type=="workout"}
 val doneToday=completed!=null&&s.active==null
 val completedId=if(doneToday)Content.workouts.find{it.title==completed?.title}?.id else null
 val w=Content.workout(s.active?.workoutId?:completedId?:plan.workoutId)
 PageColumn{
  TopBar("elevare")
  QuietText(if(s.name.isBlank())"Bugün harekete yer aç." else "Hazır mısın, ${s.name}?")
  Surface(color=Mint,shape=RoundedCornerShape(16.dp)){
   Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
    Text(if(doneToday)"BUGÜN TAMAMLANDI" else "BUGÜNKÜ SEANS",color=Ink,fontSize=11.sp,fontWeight=FontWeight.Bold)
    Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(14.dp)){
     Column(Modifier.weight(1f)){Text(shortWorkoutTitle(w),color=Ink,fontSize=24.sp,lineHeight=29.sp,fontWeight=FontWeight.Bold);Text("${minutesText(w.seconds)} · ${w.movementCount} hareket",color=Ink,fontSize=13.sp)}
     Pose(if(doneToday)"reach" else w.steps.first().moveId,Modifier.size(96.dp),Ink,true)
    }
    BigButton(if(plan.blocked)"Yanıtlarını gözden geçir" else if(s.pausedOn!=null)"Planıma devam et" else if(s.active!=null)"Seansa devam et" else if(doneToday)"Seansı incele" else "Antrenmana başla",{if(plan.blocked)onReview()else if(s.pausedOn!=null)onResumePlan()else if(doneToday)onWorkout(w.id)else onStart(w.id)},icon=if(doneToday)Icons.Rounded.Check else Icons.Rounded.PlayArrow)
    if(plan.blocked)Text(plan.note,color=Ink,fontSize=13.sp)else if(!doneToday)TextButton(onClick={onWorkout(w.id)}){Text("Hareketleri gör",color=Ink)}
   }
  }
  QuietText(if(doneToday)"Bugünkü antrenmanın kaydedildi." else if(s.active!=null)"Kaldığın hareketten devam edebilirsin." else if(!plan.blocked)plan.note else "Bilgi içerikleri her zaman açık.")
  TodayScience(s,onArticle)
  MenuRow("Uyku planı",s.bed,Icons.Rounded.Bedtime,onSleep)
 }
}

@Composable fun EvidenceCard(e:Evidence,onClick:()->Unit){
 Surface(onClick=onClick,shape=RoundedCornerShape(16.dp),color=MaterialTheme.colorScheme.secondaryContainer,modifier=Modifier.fillMaxWidth()){
  Row(Modifier.padding(16.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(14.dp)){
   Icon(Icons.Rounded.MenuBook,null,tint=MaterialTheme.colorScheme.primary,modifier=Modifier.size(24.dp));Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(5.dp)){Text(evidenceLabel(e.id),fontSize=16.sp,fontWeight=FontWeight.Medium);Text("Bilgi ve kaynak",fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)};Icon(Icons.Rounded.ChevronRight,null,Modifier.size(20.dp))
  }
 }
}
@Composable fun EvidenceLibrary(onArticle:(String)->Unit){
 var filter by rememberSaveable{mutableStateOf("Tümü")}
 PageColumn{TopBar("Bilgi");Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)){listOf("Tümü","Egzersiz","Beslenme","Nefes","Kanıt sınırı").forEach{c->FilterChip(selected=filter==c,onClick={filter=c},label={Text(c)})}};Research.articles.filter{filter=="Tümü"||it.category==filter}.forEach{e->EvidenceCard(e){onArticle(e.id)}}}
}
@Composable fun EvidenceDetail(e:Evidence,onBack:()->Unit){
 val c=LocalContext.current
 PageColumn{
  TopBar("Bilgi",onBack);Text(e.title,fontSize=25.sp,lineHeight=31.sp,fontWeight=FontWeight.Bold);Tag("PMID ${e.pmid}");QuietText(e.population)
  BlockTitle("Araştırmanın sonucu");Text(e.finding,style=MaterialTheme.typography.bodyLarge);BlockTitle("Sınırları");Text(e.limit,style=MaterialTheme.typography.bodyLarge)
  ExpandSection("Uygulamadaki karşılığı",Icons.Rounded.Info){Text(e.takeaway);QuietText("Yayın özetine dayanır; kişisel tıbbi tavsiye veya ürün onayı değildir.")}
  OutlinedButton(onClick={try{c.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("https://pubmed.ncbi.nlm.nih.gov/${e.pmid}/")))}catch(_:Exception){}},modifier=Modifier.fillMaxWidth()){Text("PubMed kaynağını aç");Spacer(Modifier.width(8.dp));Icon(Icons.Rounded.OpenInNew,null,Modifier.size(18.dp))}
 }
}
@Composable fun GrowthProfileHeader(store:Store,notify:(String)->Unit){
 val s=store.state;val c=LocalContext.current
 val permission=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){granted->store.update{it.copy(reminders=granted)};notify(if(granted)"Uyku hatırlatıcısı etkin." else "Bildirim izni verilmedi.")}
 SettingToggle("Uyku hatırlatıcısı","${s.bed} öncesinde hatırlat",s.reminders){enabled->if(!enabled)store.update{it.copy(reminders=false)} else if(Build.VERSION.SDK_INT>=33&&!Reminder.allowed(c))permission.launch(Manifest.permission.POST_NOTIFICATIONS) else if(Reminder.allowed(c)){store.update{it.copy(reminders=true)};Reminder.schedule(c,store.state)}else notify("Bildirimler Android ayarlarında kapalı.")}
 QuietText("Yaklaşık 30 dakika önce. Pil tasarrufu geciktirebilir.")
 TextButton(onClick={c.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE,c.packageName))}){Text("Android bildirim ayarları")}
}
