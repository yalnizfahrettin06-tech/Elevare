@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.elevare.active

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

class MainActivity: ComponentActivity() {
    private lateinit var store: Store
    private var incomingReminder by mutableStateOf<Pair<String,Long>?>(null)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        store=Store(applicationContext)
        intent.getStringExtra("reminder_target")?.let{incomingReminder=it to System.nanoTime()}
        setContent { ElevareTheme(store.state.dark) { ActiveApp(store,incomingReminder){incomingReminder=null} } }
    }
    override fun onNewIntent(intent:Intent){super.onNewIntent(intent);setIntent(intent);intent.getStringExtra("reminder_target")?.let{incomingReminder=it to System.nanoTime()}}
    override fun onStop() { if(::store.isInitialized)store.pauseTimer();super.onStop() }
    override fun onResume() { super.onResume();if(::store.isInitialized)Reminder.schedule(applicationContext,store.state) }
}
private data class Destination(val label:String,val icon:ImageVector)
private val destinations=listOf(Destination("Bugün",ArcIcons.Home),Destination("Rutinim",ArcIcons.Program),Destination("Rehber",ArcIcons.Book))

@Composable fun ActiveApp(store:Store,incomingReminder:Pair<String,Long>?=null,onReminderConsumed:()->Unit={}) {
    val s=store.state
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var page by rememberSaveable { mutableStateOf("") }
    var notice by remember { mutableStateOf("") }
    var showDelete by remember { mutableStateOf(false) }
    var prepareWorkout by rememberSaveable { mutableStateOf("") }
    var dateTick by remember { mutableStateOf(LocalDate.now()) }
    val context=LocalContext.current
    val haptic=LocalHapticFeedback.current
    val unavailablePage=remember(page){when {
        page.startsWith("workout:")->runCatching{Content.workout(page.substringAfter(":"))}.isFailure
        page.startsWith("move:")->runCatching{Content.move(page.substringAfter(":"))}.isFailure
        page.startsWith("article:")->runCatching{Research.article(page.substringAfter(":"))}.isFailure
        else->false
    }}
    fun go(value:String){page=value}
    fun message(value:String){notice=value}
    fun startWorkout(id:String){
        if(store.state.pausedOn!=null||!trainingAllowed(store.state)){message("Önce plan uygunluğunu gözden geçir.");return}
        val ok=store.update{st->beginWorkout(st,id).let{next->next.copy(active=next.active?.let{a->a.copy(elapsedRealtimeDeadline=android.os.SystemClock.elapsedRealtime()+a.remaining*1000L)})}}
        if(!ok){message(store.error);return}
        prepareWorkout="";if(store.state.active!=null)go("session")else message("Bugün toparlanma zamanı. Ana ekrandaki programına dön.")
    }
    val export=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")){uri->
        if(uri!=null) try { val stream=context.contentResolver.openOutputStream(uri)?:error("Dosya açılamadı");stream.use{it.write(store.export().toByteArray(Charsets.UTF_8))};message("Kayıt dosyan hazır.") }catch(_:Exception){message("Dosya kaydedilemedi. Tekrar deneyebilirsin.")}
    }
    LaunchedEffect(notice){if(notice.isNotBlank()){delay(4500);notice=""}}
    LaunchedEffect(Unit){while(true){delay(30_000);dateTick=LocalDate.now()}}
    LaunchedEffect(store.error){if(store.error.isNotBlank())message(store.error)}
    LaunchedEffect(incomingReminder,s.ready,s.onboardingVersion){
        if(s.ready&&s.onboardingVersion>=8&&incomingReminder!=null){
            store.pauseTimer()
            page=if(incomingReminder.first=="sleep")"sleep" else if(incomingReminder.first.startsWith("routine:"))incomingReminder.first else ""
            if(incomingReminder.first=="home")tab=0
            onReminderConsumed()
        }
    }
    BackHandler(enabled=page.isNotBlank()){ if(page=="session") store.pauseTimer();page="" }
    Scaffold(
        containerColor=MaterialTheme.colorScheme.background,
        bottomBar={ if(!store.recoveryRequired&&s.ready&&s.onboardingVersion>=8&&page.isBlank()) Column{
            HorizontalDivider(color=ArcLine.copy(alpha=.65f))
            NavigationBar(containerColor=Paper,tonalElevation=0.dp) { destinations.forEachIndexed { i,d -> NavigationBarItem(selected=tab==i,onClick={tab=i;if(s.haptic)haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)},icon={Icon(d.icon,null,Modifier.size(23.dp))},label={Text(d.label,fontSize=11.sp,fontWeight=if(tab==i)FontWeight.Bold else FontWeight.Medium)},colors=NavigationBarItemDefaults.colors(selectedIconColor=Coral,indicatorColor=Mint,selectedTextColor=Ink,unselectedIconColor=ArcMuted,unselectedTextColor=ArcMuted)) } }
        } },
        snackbarHost={if(notice.isNotBlank()) Snackbar(Modifier.padding(16.dp)){Text(notice)}}
    ){padding->
        Box(Modifier.fillMaxSize().padding(padding)){key(page,tab,if(page=="session")null else dateTick){
            if(store.recoveryRequired) PageColumn{
                TopBar("Kayıtlarını koruyoruz");InfoCard(store.error)
                BigButton("Kurtarma dosyasını kaydet",{export.launch("elevare-kurtarma.json")},icon=ArcIcons.Download)
                OutlinedButton(onClick={store.retryRead()},modifier=Modifier.fillMaxWidth()){Text("Yeniden dene")}
                TextButton(onClick={showDelete=true}){Text("Verileri silip yeniden başla")}
            }
            else if(!s.ready||s.onboardingVersion<8) GrowthOnboarding(store)
            else if(unavailablePage) PageColumn{
                TopBar("İçerik bulunamadı",{page=""})
                QuietText("Bu eski bağlantı artık kullanılamıyor. Kayıtların korunuyor.")
                BigButton("Programıma dön",{page="plan"},icon=ArcIcons.Program)
            }
            else if(page=="session"&&!trainingAllowed(s)) SourcesScreen(onBack={page=""},onLink={message("Bilgi sekmesinden kaynakları okuyabilirsin.")})
            else if(page=="session") SessionScreen(store,onClose={store.pauseTimer();page=""},onSaved={page="";tab=0;message("Antrenman tamamlandı. Kaydın hazır!")})
            else if(page.startsWith("workout:")) WorkoutDetail(Content.workout(page.substringAfter(":")),s,onBack={page=""},onFavorite={id->store.update{it.copy(favorites=if(id in it.favorites)it.favorites-id else it.favorites+id)}},onStart={w->
                if(!trainingAllowed(s)){message("Önce Profil bölümünden antrenman uygunluğunu gözden geçir.")}
                else if(s.pausedOn!=null){message("Planın dinlenmede. Profilinden devam edebilirsin.")}
                else if(s.active!=null){go("session");message("Açık seansına dönüyorsun.")}
                else{prepareWorkout=w.id}
            },onMove={go("move:"+it)})
            else if(page.startsWith("move:")) MoveDetail(if(page.substringAfter(":")=="sprint")SprintGuide else Content.move(page.substringAfter(":")),s.reducedMotion,onBack={page=""})
            else if(page=="sleep") SleepScreen(store,onBack={page=""},notify=::message)
            else if(page.startsWith("routine:")) RoutineDetailScreen(store,page.substringAfter(":"),onBack={page=""})
            else if(page=="profile") Column{TopBar("Ayarlar",{page=""});Box(Modifier.weight(1f)){CompactProfileScreen(store,onSources={go("sources")},onSleep={go("sleep")},onExport={export.launch("elevare-kayitlarim.json")},onDelete={showDelete=true},onPlan={go("plan")},onProgress={go("progress")},onTrial={go("trial")},notify=::message)}}
            else if(page=="plan") ProgramBrowser(s,onMove={go("move:"+it)},onWorkout={go("workout:"+it)},onBack={page=""})
            else if(page=="progress") Column{TopBar("İlerleme",{page=""});Box(Modifier.weight(1f)){ProgressScreen(s,onSleep={go("sleep")})}}
            else if(page=="trial") DemoStatusScreen(store,onBack={page=""})
            else if(page.startsWith("fact:")) FactDetail(page.substringAfter(":"),s.age,onBack={page=""},store=store)
            else if(page.startsWith("article:")) EvidenceDetail(Research.article(page.substringAfter(":")),onBack={page=""})
            else if(page=="sources") SourcesScreen(onBack={page=""},onLink={url->try{context.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(url)))}catch(_:Exception){message("Bağlantıyı açacak bir tarayıcı bulunamadı.")}})
            else {
                Column(Modifier.fillMaxSize()){
                    if(s.active!=null&&tab!=0) Row(Modifier.fillMaxWidth().background(Mint).clickable{go("session")}.padding(horizontal=20.dp,vertical=14.dp),verticalAlignment=Alignment.CenterVertically){Icon(ArcIcons.Play,null,tint=Coral);Text("Seansa devam et",Modifier.weight(1f).padding(start=10.dp),color=Ink,fontWeight=FontWeight.Bold,fontSize=13.sp);Icon(ArcIcons.Arrow,null,tint=Coral)}
                    when(tab){
                        0->WorkoutHome(store,onWorkout={go("workout:"+it)},onMove={go("move:"+it)},onStart={if(s.active!=null)go("session")else prepareWorkout=it},onSleep={go("sleep")},onFact={go("fact:"+it)},onReview={store.pauseTimer();store.update{it.copy(onboardingVersion=0)}},onProgress={go("progress")},onProgram={go("plan")},onProfile={go("profile")},onRoutine={go("routine:"+it)},onRoutines={tab=1})
                        1->RoutinesScreen(store,onRoutine={go("routine:"+it)},onProgram={go("plan")},onSleep={go("sleep")},onProgress={go("progress")},onProfile={go("profile")})
                        2->FactLibrary(s,onFact={go("fact:"+it)},store=store,onProfile={go("profile")})
                        3->CompactProfileScreen(store,onSources={go("sources")},onSleep={go("sleep")},onExport={export.launch("elevare-kayitlarim.json")},onDelete={showDelete=true},onPlan={go("plan")},onProgress={go("progress")},onTrial={go("trial")},notify=::message)
                    }
                }
            }
        }
    }
    }
    if(showDelete) AlertDialog(onDismissRequest={showDelete=false},title={Text("Kayıtlar silinsin mi?")},text={Text("Bu cihazdaki seanslar, uyku kayıtları ve tercihler silinir. Bu işlem geri alınamaz.")},confirmButton={TextButton(onClick={if(store.reset()){showDelete=false;prepareWorkout="";page="";tab=0}else message(store.error)}){Text("Kayıtları sil")}},dismissButton={TextButton(onClick={showDelete=false}){Text("Vazgeç")}})
    if(prepareWorkout.isNotBlank()) DailyReadinessDialog(store,prepareWorkout,onDismiss={prepareWorkout=""},onReady={startWorkout(it)})
}

@Composable fun PageColumn(content:@Composable ColumnScope.()->Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=22.dp).padding(top=14.dp,bottom=28.dp),verticalArrangement=Arrangement.spacedBy(18.dp),content=content)
}
@Composable fun TopBar(title:String,onBack:(()->Unit)?=null,action:(@Composable ()->Unit)?=null) {
    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){
        if(onBack!=null)IconButton(onClick=onBack){Icon(ArcIcons.Back,"Geri")}
        if(title=="elevare")ArcBrand(Modifier.weight(1f))
        else Text(title,fontSize=if(onBack==null)24.sp else 19.sp,fontWeight=FontWeight.Bold,letterSpacing=(-.6).sp,modifier=Modifier.weight(1f).semantics{heading()})
        action?.invoke()
    }
}
@Composable fun Tag(text:String,color:Color=Lime,textColor:Color=Ink) { Box(Modifier.background(color,RoundedCornerShape(6.dp)).padding(horizontal=9.dp,vertical=5.dp)){Text(text,color=textColor,fontWeight=FontWeight.Bold,fontSize=10.sp,letterSpacing=.7.sp)} }
@Composable fun BigButton(text:String,onClick:()->Unit,modifier:Modifier=Modifier,icon:ImageVector=ArcIcons.Arrow,enabled:Boolean=true,dark:Boolean=true) {
    Button(onClick=onClick,enabled=enabled,modifier=modifier.fillMaxWidth().heightIn(min=56.dp),shape=RoundedCornerShape(topStart=18.dp,topEnd=18.dp,bottomEnd=18.dp,bottomStart=6.dp),colors=ButtonDefaults.buttonColors(containerColor=Coral,contentColor=Paper,disabledContainerColor=MaterialTheme.colorScheme.surfaceVariant,disabledContentColor=ArcMuted),contentPadding=PaddingValues(horizontal=20.dp,vertical=16.dp)){
        Text(text,Modifier.weight(1f),fontWeight=FontWeight.ExtraBold,fontSize=15.sp,textAlign=TextAlign.Start);Icon(icon,null,Modifier.size(23.dp))
    }
}
@Composable fun BlockTitle(title:String,action:String?=null,onAction:()->Unit={}){Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Text(title,Modifier.weight(1f),style=MaterialTheme.typography.titleLarge);if(action!=null) TextButton(onClick=onAction){Text(action,fontSize=12.sp);Icon(ArcIcons.Chevron,null,Modifier.size(16.dp))}}}
@Composable fun StatPill(icon:ImageVector,text:String,modifier:Modifier=Modifier){Row(modifier,verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(6.dp)){Icon(icon,null,Modifier.size(15.dp));Text(text,fontSize=12.sp,fontWeight=FontWeight.Medium)}}
@Composable fun InfoCard(text:String,icon:ImageVector=ArcIcons.Info,onClick:(()->Unit)?=null){Surface(color=MaterialTheme.colorScheme.surfaceVariant,shape=RoundedCornerShape(14.dp),modifier=Modifier.fillMaxWidth().then(if(onClick!=null)Modifier.clickable(onClick=onClick) else Modifier)){Row(Modifier.padding(16.dp),horizontalArrangement=Arrangement.spacedBy(12.dp)){Icon(icon,null,Modifier.size(20.dp));Text(text,style=MaterialTheme.typography.bodyMedium,modifier=Modifier.weight(1f));if(onClick!=null)Icon(ArcIcons.External,null,Modifier.size(18.dp))}}}
fun timeText(seconds:Int):String = "%02d:%02d".format(seconds/60,seconds%60)
fun minutesText(seconds:Int):String = if(seconds%60==0) "${seconds/60} dk" else "${seconds/60} dk ${seconds%60} sn"

@Composable fun RoutineRow(id:String,title:String,sub:String,icon:ImageVector,done:Boolean,color:Color,onOpen:()->Unit,onDone:()->Unit){
    Surface(shape=RoundedCornerShape(16.dp),color=MaterialTheme.colorScheme.surface){Row(Modifier.fillMaxWidth().padding(12.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){
        Box(Modifier.size(48.dp).clip(RoundedCornerShape(13.dp)).background(color).clickable(onClick=onOpen),contentAlignment=Alignment.Center){Icon(icon,null,tint=Ink)}
        Column(Modifier.weight(1f).clickable(onClick=onOpen).padding(vertical=6.dp)){Text(title,fontWeight=FontWeight.Bold,fontSize=15.sp);Text(sub,fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant,lineHeight=17.sp)}
        IconButton(onClick=onDone){Icon(if(done)ArcIcons.Checked else ArcIcons.Circle,if(done)"Tamamlamayı geri al" else "$title tamamlandı olarak işaretle",tint=if(done)Blue else MaterialTheme.colorScheme.outline)}
    }}
}
@Composable fun MiniWorkout(w:Workout,modifier:Modifier=Modifier,color:Color,textColor:Color,onClick:(String)->Unit){
    Surface(onClick={onClick(w.id)},modifier=modifier,shape=RoundedCornerShape(18.dp),color=color){Column(Modifier.padding(17.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){Icon(if(w.category=="Mobilite")ArcIcons.Run else ArcIcons.Spark,null,tint=textColor);Text(w.title,color=textColor,fontWeight=FontWeight.Black,fontSize=18.sp,lineHeight=21.sp);Row(verticalAlignment=Alignment.CenterVertically){Text(minutesText(w.seconds),Modifier.weight(1f),fontSize=12.sp,color=textColor.copy(.8f));Icon(ArcIcons.External,null,tint=textColor,modifier=Modifier.size(18.dp))}}}
}
