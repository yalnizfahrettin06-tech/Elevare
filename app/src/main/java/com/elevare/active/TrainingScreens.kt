@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.elevare.active

import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import kotlinx.coroutines.delay
import kotlin.math.*
import java.time.LocalDate

@Composable fun ExploreScreen(s:UserState,onWorkout:(String)->Unit,onMove:(String)->Unit,onFavorite:(String)->Unit){
 var section by rememberSaveable{mutableStateOf("Programım")}
 val week=currentWeekProgram(s)
 PageColumn{
  TopBar("Antrenman")
  Text("Haftanın planı.",style=MaterialTheme.typography.headlineLarge)
  Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){listOf("Programım","Hareketler").forEach{label->FilterChip(selected=section==label,onClick={section=label},label={Text(label)})}}
  if(section=="Programım"){
   QuietText("${s.trainingDays} gün · ${s.dailyMinutes} dk · ${if(s.environment=="indoor")"Evde" else "Koşu ve tamamlayıcı hareketler"}")
   ProgramList(week,journeyDay(s)-1,onWorkout)
   MenuRow("Sprint nedir?","Isınma, hızlanma ve yürüyüş araları",ArcIcons.Run){onMove("sprint")}
  }else{
   Content.moves.forEach{m->
    Surface(onClick={onMove(m.id)},shape=RoundedCornerShape(16.dp),color=MaterialTheme.colorScheme.surface){
     Row(Modifier.fillMaxWidth().padding(12.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(14.dp)){
      Pose(m.id,Modifier.size(66.dp),Ink,true)
      Column(Modifier.weight(1f)){Text(m.title,fontWeight=FontWeight.Bold,fontSize=16.sp);QuietText(m.category)}
      Icon(ArcIcons.Play,null,tint=Sky,modifier=Modifier.size(24.dp))
     }
    }
   }
  }
 }
}

@Composable fun WorkoutDetail(w:Workout,s:UserState,onBack:()->Unit,onFavorite:(String)->Unit,onStart:(Workout)->Unit,onMove:(String)->Unit) {
    PageColumn {
        TopBar("Antrenman",onBack,action={IconButton(onClick={onFavorite(w.id)}){Icon(if(w.id in s.favorites)ArcIcons.Favorite else ArcIcons.Heart,"Favori durumunu değiştir")}})
        Surface(shape=RoundedCornerShape(24.dp),color=Track){
            Column(Modifier.padding(22.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
                Text(shortWorkoutTitle(w),color=Ink,fontSize=25.sp,fontWeight=FontWeight.Bold)
                ArcStage(w.heroMove(),Modifier.fillMaxWidth().height(178.dp),s.reducedMotion)
                CompositionLocalProvider(LocalContentColor provides Ink){Row(horizontalArrangement=Arrangement.spacedBy(20.dp)){StatPill(ArcIcons.Clock,minutesText(w.seconds));StatPill(ArcIcons.Run,"${w.movementCount} hareket")}}
            }
        }
        QuietText(if(s.age<18)"İlk denemede güvendiğin bir yetişkinden destek al. Ağrı veya baş dönmesinde dur." else "Tekniğinden emin değilsen bir antrenörden destek al. Ağrı veya baş dönmesinde dur.")
        BigButton(if(w.id=="breath")"Nefese başla" else "Antrenmana başla",{onStart(w)},icon=ArcIcons.Play,enabled=s.pausedOn==null)
        ExpandSection("Seans hakkında",ArcIcons.Info){Text(w.subtitle);QuietText(w.equipment);QuietText(w.dayBrief());QuietText("Isınma ve toparlanma toplam süreye dahil.")}
        BlockTitle("Seansın akışı")
        listOf("warmup" to "Isınma","main" to "Ana bölüm","cooldown" to "Soğuma").forEach{(phase,label)->
            val duration=w.steps.filter{if(phase=="main")it.phase !in listOf("warmup","cooldown") else it.phase==phase}.sumOf{it.seconds}
            if(duration>0)Row(Modifier.fillMaxWidth().padding(vertical=8.dp),verticalAlignment=Alignment.CenterVertically){
                Icon(if(phase=="warmup")ArcIcons.Run else if(phase=="cooldown")ArcIcons.Breath else ArcIcons.Strength,null,tint=Sky,modifier=Modifier.size(22.dp))
                Text(label,Modifier.weight(1f).padding(start=12.dp),fontWeight=FontWeight.Bold)
                Text(minutesText(duration),color=MaterialTheme.colorScheme.onSurfaceVariant,fontSize=13.sp)
            }
        }
        if(w.hasSprint())MenuRow("Sprint nedir?","Kontrollü koşu ve yürüyüş araları",ArcIcons.Run){onMove("sprint")}
        ExpandSection("Tüm adımlar · ${w.steps.size}",ArcIcons.Program){
        w.steps.forEachIndexed{i,step->
            val m=Content.move(step.moveId)
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable{onMove(m.id)}.padding(vertical=12.dp,horizontal=6.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(14.dp)){
                Text("%02d".format(i+1),fontSize=16.sp,fontWeight=FontWeight.Bold,color=MaterialTheme.colorScheme.onSurfaceVariant)
                Column(Modifier.weight(1f)){Text(if(step.rest)"Dinlen / rahat yürü" else m.title,fontWeight=FontWeight.Bold,fontSize=15.sp);Text(sessionPhaseLabel(w,i)+(if(step.side=="left")" · Sol taraf" else if(step.side=="right")" · Sağ taraf" else ""),fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)}
                Text("${step.seconds} sn",fontSize=13.sp,fontWeight=FontWeight.Bold);Icon(ArcIcons.Chevron,null,Modifier.size(17.dp))
            }
            if(i!=w.steps.lastIndex)HorizontalDivider(color=MaterialTheme.colorScheme.outlineVariant)
        }
        }
        Text("Bu kısa seans, gün içindeki spor ve oyunlarının tamamının yerine geçmez.",fontSize=13.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable fun MoveDetail(m:Move,reduced:Boolean,onBack:()->Unit){
    var frozen by rememberSaveable{mutableStateOf(false)}
    var slow by rememberSaveable{mutableStateOf(false)}
    var lesson by rememberSaveable(m.id){mutableIntStateOf(0)}
    PageColumn{
        TopBar("HAREKET REHBERİ",onBack)
        Tag(m.category.uppercase())
        Text(m.title.uppercase(java.util.Locale.forLanguageTag("tr")),style=MaterialTheme.typography.headlineLarge)
        Column(horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(8.dp)){
            ArcStage(m.id,Modifier.fillMaxWidth().height(210.dp),reduced,playing=!frozen,speed=if(slow)2f else 1f,decorated=false)
            Text("Hareket rehberi · şematik gösterim",color=ArcMuted,fontSize=12.sp)
        }
        Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){FilterChip(selected=frozen,onClick={frozen=!frozen},label={Text(if(frozen)"Oynat" else "Duraklat")});FilterChip(selected=slow,onClick={slow=!slow},label={Text("Yavaş göster")})}
        QuietText(motionCue(m.id))
        Text(m.hint,fontWeight=FontWeight.Bold,fontSize=19.sp)
        if(m.steps.isNotEmpty()){
         Text("Teknik adım ${lesson+1} / ${m.steps.size}",color=Coral,fontWeight=FontWeight.Bold)
         Text(m.steps[lesson.coerceIn(m.steps.indices)],style=MaterialTheme.typography.bodyLarge)
         Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){
          OutlinedButton(onClick={lesson--},enabled=lesson>0){Text("Önceki adım")}
          OutlinedButton(onClick={lesson++},enabled=lesson<m.steps.lastIndex){Text("Sonraki adım")}
         }
         QuietText("Metin adımları tekniği açıklar; animasyon hareketin genel döngüsünü gösterir.")
        }
        InfoCard("Çizgi figürü basitleştirilmiş bir gösterimdir. Teknik adımları izle; gerektiğinde bir antrenörden destek al.",ArcIcons.Info)
    }
}

@Composable fun SessionScreen(store:Store,onClose:()->Unit,onSaved:()->Unit){
    val a=store.state.active
    if(a==null){PageColumn{TopBar("Seans",onClose);InfoCard("Aktif seans yok.")};return}
    val w=activeWorkout(a)
    val step=w.steps.getOrNull(a.step)
    if(step==null){PageColumn{TopBar("Seans",onClose);InfoCard("Bu oturumun adımı açılamadı. Geçmiş kayıtların korunuyor.")};return}
    val move=Content.move(step.moveId)
    var clockPulse by remember{mutableIntStateOf(0)}
    var stop by remember{mutableStateOf(false)}
    var feeling by rememberSaveable(a.id){mutableStateOf("")}
    var stopForDiscomfort by remember{mutableStateOf(false)}
    val activity=LocalContext.current as? ComponentActivity
    val left=remember(a,clockPulse){remainingRuntime(a)}
    val allDone=left==0&&a.step==w.steps.lastIndex
    val resumeBlock=remember(a,store.state,LocalDate.now()){sessionResumeBlockReason(store.state,a)}
    var resumeNotice by remember{mutableStateOf<String?>(null)}
    val visualRemaining=remember(a,clockPulse){
        if(!a.running)a.remaining*1000L
        else if(a.elapsedRealtimeDeadline>0)(a.elapsedRealtimeDeadline-android.os.SystemClock.elapsedRealtime()).coerceAtLeast(0)
        else (a.deadline-System.currentTimeMillis()).coerceAtLeast(0)
    }
    val progress=(1f-visualRemaining.toFloat()/(step.seconds.coerceAtLeast(1)*1000f)).coerceIn(0f,1f)
    val initialReady=!a.running&&a.step==0&&left==step.seconds&&a.elapsed==0
    val audio=rememberSessionAudio(store,a.copy(running=a.running&&(resumeBlock==null||allDone)),move,left,step.rest)
    var soundSettings by remember{mutableStateOf(false)}
    var audioNoticeHidden by remember(a.id){mutableStateOf(false)}
    var saveError by remember{mutableStateOf(false)}
    LaunchedEffect(a.id,a.step,a.running){
        while(a.running&&remainingRuntime(a)>0){clockPulse++;delay(100)}
        clockPulse++
    }
    LaunchedEffect(a.id,a.step,resumeBlock){
        if(resumeBlock!=null&&a.running&&!allDone){audio.stop();store.pauseTimer()}
    }
    LaunchedEffect(a.id,a.step,left,a.running,store.state.autoAdvance,resumeBlock){
        if(left==0&&a.running&&store.state.autoAdvance&&!allDone&&resumeBlock==null)store.update{state->
            val current=state.active?:return@update state
            if(sessionResumeBlockReason(state,current)==null)advanceSessionRuntime(state) else state
        }
    }
    LaunchedEffect(allDone){
        if(allDone)audio.instruction(a.id+":complete","Antrenman tamamlandı. Nasıl hissettirdi?")
    }
    DisposableEffect(a.running,allDone){
        if(a.running&&!allDone)activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose{activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)}
    }
    fun requestClose(){store.pauseTimer();stop=true}
    androidx.activity.compose.BackHandler{requestClose()}
    fun toggle(){
        val current=store.state.active?:return
        if(current.running){audio.stop();store.pauseTimer()}
        else {
            val reason=sessionResumeBlockReason(store.state,current)
            resumeNotice=reason
            if(reason==null)store.update{state->
                if(sessionResumeBlockReason(state,current)!=null)state else state.copy(active=current.copy(running=true,
                    deadline=System.currentTimeMillis()+current.remaining*1000L,
                    elapsedRealtimeDeadline=android.os.SystemClock.elapsedRealtime()+current.remaining*1000L))
            }
        }
    }
    fun advance(){
        audio.stop()
        store.update{state->
            val active=state.active?:return@update state
            val reason=sessionResumeBlockReason(state,active)
            if(reason!=null){resumeNotice=reason;state}
            else advanceSessionRuntime(state.copy(active=active.copy(running=true,remaining=0,deadline=0,elapsedRealtimeDeadline=0)))
        }
    }
    Column(Modifier.fillMaxSize()){
    Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal=20.dp,vertical=12.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
        TopBar(if(allDone)"Seans özeti" else sessionPhaseLabel(w,a.step),::requestClose,action={
            IconButton(onClick={soundSettings=true}){
                Icon(if(store.state.voiceCoach)ArcIcons.Sound else ArcIcons.Mute,
                    if(store.state.voiceCoach)"Sesli koç ayarlarını aç" else "Sesli koçu aç")
            }
        })
        LinearProgressIndicator(progress={((a.elapsed+step.seconds-left).toFloat()/w.seconds.coerceAtLeast(1)).coerceIn(0f,1f)},
            modifier=Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
            color=if(step.rest)Sky else Coral,trackColor=MaterialTheme.colorScheme.surfaceVariant)
        if(allDone){
            Column(Modifier.fillMaxWidth().padding(vertical=8.dp),horizontalAlignment=Alignment.CenterHorizontally,
                verticalArrangement=Arrangement.spacedBy(8.dp)){
                Box(Modifier.size(44.dp).background(Mint,RoundedCornerShape(12.dp)),contentAlignment=Alignment.Center){
                    Icon(ArcIcons.Check,null,Modifier.size(26.dp),tint=Coral)
                }
                Text("Antrenman tamamlandı",fontSize=27.sp,lineHeight=32.sp,fontWeight=FontWeight.ExtraBold,textAlign=TextAlign.Center)
                Text(w.title,color=MaterialTheme.colorScheme.onSurfaceVariant,textAlign=TextAlign.Center)
                Text(minutesText(w.seconds),color=Coral,fontSize=32.sp,fontWeight=FontWeight.Bold)
                Text("Nasıl hissettirdi?",fontWeight=FontWeight.Bold)
                Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(8.dp)){
                    listOf("Kolay","Uygundu","Zorlayıcı","Ağrı / rahatsızlık").forEach{value->
                        Choice(value,selected=feeling==value){feeling=value}
                    }
                }
                if(feeling=="Ağrı / rahatsızlık")InfoCard(
                    "Şimdilik dur. Bir sonraki yoğun seans açılmaz. Rahatsızlık sürüyorsa sağlık uzmanına danış; 18 yaş altındaysan güvendiğin bir yetişkine haber ver.",
                    ArcIcons.Shield)
                if(saveError)InfoCard("Oturum kaydedilemedi. Bu ekranı kapatmadan tekrar dene.",ArcIcons.Info)
                QuietText("Bu seansla birlikte bu hafta ${weekCompleted(store.state,LocalDate.now())+1} seans. Kaydettiğinde geçmişine eklenecek.")
                QuietText("Sonraki adım: toparlan. Bir sonraki planlı gününü Rutinim'den görebilirsin.")
            }
        }else{
            Text(if(step.rest&&step.phase!="transition")"Yürüyüş arası" else move.title,
                fontSize=26.sp,lineHeight=31.sp,fontWeight=FontWeight.Bold)
            Surface(shape=RoundedCornerShape(24.dp),color=Track){
                Column(Modifier.padding(16.dp),horizontalAlignment=Alignment.CenterHorizontally,
                    verticalArrangement=Arrangement.spacedBy(8.dp)){
                    if(move.id=="breath"){
                        val inhaling=(step.seconds-left)%10<4
                        BreathCircle(inhaling,if(!a.running)"Duraklatıldı" else if(inhaling)"Rahatça nefes al" else "Zorlamadan ver",
                            store.state.reducedMotion||!a.running)
                    }else{
                        ArcStage(move.id,Modifier.fillMaxWidth().height(156.dp),
                            reduced=store.state.reducedMotion,playing=a.running&&left>0&&resumeBlock==null,
                            progress=techniqueProgress(step.phase,progress),side=step.side,decorated=false)
                    }
                    Text(timeText(left),fontSize=52.sp,fontWeight=FontWeight.Bold,color=Ink,
                        letterSpacing=(-1).sp,modifier=Modifier.clearAndSetSemantics{contentDescription="$left saniye kaldı"})
                    Text(when{
                        left==0->"Bu bölüm tamamlandı"
                        resumeBlock!=null->"Uygunluk kontrolü gerekli"
                        initialReady->"Başlamaya hazır"
                        !a.running->"Duraklatıldı"
                        step.side=="left"->"Sol taraf"
                        step.side=="right"->"Sağ taraf"
                        move.id=="sprint"->"Kontrollü hızlan · tam efor değil"
                        step.rest->"Rahatça toparlan"
                        else->poseStage(move.id,techniqueProgress(step.phase,progress))
                    },fontSize=12.sp,color=Sky,textAlign=TextAlign.Center,fontWeight=FontWeight.Bold)
                }
            }
            if(resumeBlock!=null||resumeNotice!=null)InfoCard(
                (resumeBlock?:resumeNotice.orEmpty())+" Oturumun duraklatıldı; kaydın korunuyor.",ArcIcons.Shield)
            else ExpandSection("Teknik ipucu",ArcIcons.Info){Text(sessionCoachInstruction(step,move),fontSize=14.sp,lineHeight=21.sp)}
            if(store.state.voiceCoach&&!audio.initializing&&audio.status.isNotEmpty()&&!audioNoticeHidden){
                Row(verticalAlignment=Alignment.CenterVertically){
                    Text("Ses kullanılamıyor. Yazılı rehberle devam edebilirsin.",Modifier.weight(1f),
                        fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)
                    IconButton(onClick={audioNoticeHidden=true}){Icon(ArcIcons.Close,"Ses bilgisini kapat",Modifier.size(18.dp))}
                }
            }
        }
    }
    Surface(color=Paper,shadowElevation=8.dp){Column(Modifier.fillMaxWidth().padding(horizontal=20.dp,vertical=8.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){
        if(allDone)BigButton("Kaydet ve bitir",{
            val saved=store.update{completeSessionRuntime(it,a,if(feeling=="Ağrı / rahatsızlık")"Rahatsızlık" else feeling)}
            if(saved){audio.stop();onSaved()}else saveError=true
        },icon=ArcIcons.Check,enabled=feeling.isNotBlank())
        else{
            w.steps.getOrNull(a.step+1)?.let{next->Text("Sırada: ${Content.move(next.moveId).title} · ${minutesText(next.seconds)}",fontSize=12.sp,color=Sky)}
            if(left==0)BigButton("Sonraki bölüme geç",::advance,icon=ArcIcons.Next,enabled=resumeBlock==null)
            else BigButton(if(a.running)"Duraklat" else if(initialReady)"Hazırım, başla" else "Devam et",::toggle,
                enabled=a.running||(store.state.pausedOn==null&&resumeBlock==null),icon=if(a.running)ArcIcons.Pause else ArcIcons.Play)
            TextButton(onClick=::requestClose,modifier=Modifier.fillMaxWidth()){Text("Seansı bırak")}
        }
    }}
    }
    if(soundSettings)AlertDialog(onDismissRequest={soundSettings=false},title={Text("Ses ve geçişler")},
        text={Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(12.dp)){
            if(audio.status.isNotBlank()){
                QuietText(audio.status)
                TextButton(onClick={runCatching{activity?.startActivity(android.content.Intent("com.android.settings.TTS_SETTINGS"))}}){Text("Android ses ayarlarını aç")}
            }
            SettingToggle("Türkçe anlatım","Cihazındaki çevrimdışı ses",store.state.voiceCoach){v->store.update{it.copy(voiceCoach=v)}}
            SettingToggle("Otomatik geçiş","Bölüm bitince sıradakine geç",store.state.autoAdvance){v->store.update{it.copy(autoAdvance=v)}}
            SettingToggle("Dokunma titreşimi","Anlatım ve süre sesinden bağımsız",store.state.haptic){v->store.update{it.copy(haptic=v)}}
            listOf("off" to "Süre sesi kapalı","countdown" to "Son 3 saniye","every_second" to "Her saniye").forEach{(v,label)->
                Choice(label,selected=store.state.timerSound==v){store.update{it.copy(timerSound=v)}}
            }
            TextButton(onClick={audio.speak(sessionCoachInstruction(step,move))},
                enabled=a.running&&store.state.voiceCoach&&audio.ready){Text("Yönergeyi tekrar dinle")}
            if(audio.status.isNotEmpty()){
                TextButton(onClick={audio.retryVoice();audioNoticeHidden=false}){Text("Türkçe sesi yeniden dene")}
            }
        }},confirmButton={TextButton(onClick={soundSettings=false}){Text("Tamam")}})
    if(stop)AlertDialog(onDismissRequest={stop=false},
        title={Text(if(allDone)"Sonucun kaydedilmedi" else "Seansa ara verelim mi?")},
        text={Column(verticalArrangement=Arrangement.spacedBy(12.dp)){Text(if(saveError)"Oturum kaydedilemedi. Seansa dönüp yeniden deneyebilirsin."
            else if(allDone)"Tamamlanma ekranına dönerek geri bildirimini ve sonucunu kaydedebilirsin."
            else "Yaptığın bölüm kısmi oturum olarak korunur. Tamamlanan antrenman sayılmaz; telafi borcu oluşmaz.")
            if(!allDone){
                Choice("Şimdilik ara veriyorum",selected=!stopForDiscomfort){stopForDiscomfort=false}
                Choice("Ağrı / rahatsızlık hissettim",selected=stopForDiscomfort){stopForDiscomfort=true}
                if(stopForDiscomfort)QuietText("Şimdilik dur. Yoğun seanslar açılmaz; devam etmeden önce uygunluğunu netleştir.")
            }
        }},
        dismissButton={TextButton(onClick={stop=false}){Text("Seansa dön")}},
        confirmButton={TextButton(onClick={
            val saved=store.update{state->
                val partial=abandonSessionRuntime(state)
                if(stopForDiscomfort)partial.copy(safety="pain",gentle=true,
                    sessions=partial.sessions.map{if(it.id==a.id)it.copy(feeling="Rahatsızlık")else it}) else partial
            }
            if(saved){audio.stop();stop=false;onClose()}else saveError=true
        }){Text(if(allDone)"Kısmi olarak kaydet ve çık" else "Kısmi kaydet ve çık")}})
}

fun sessionPhaseLabel(workout:Workout,index:Int):String{
    val step=workout.steps.getOrNull(index)?:return "Antrenman"
    val phase=when(step.phase){
        "warmup"->"Isınma"
        "cooldown"->"Soğuma"
        "recovery"->"Yürüyüş arası"
        "transition"->"Geçiş"
        "prepare"->"Poza yerleş"
        "exit"->"Pozdan çık"
        else->""
    }
    if(phase.isNotEmpty())return phase
    if(step.moveId=="sprint"){
        val tour=workout.steps.take(index+1).count{it.moveId=="sprint"}
        return "Koşu $tour / "+workout.intervalCount()
    }
    if(step.rest)return "Yürüyüş arası"
    return Content.move(step.moveId).title
}

fun sessionCoachInstruction(step:Step,move:Move):String{
    val side=when(step.side){"left"->"Sol taraf. ";"right"->"Sağ taraf. ";else->""}
    return side+when(step.phase){
        "transition"->"Yavaşça yer değiştir. Sonraki bölüm için hazırlan."
        "prepare"->"Poza yavaşça yerleş. Zorlanırsan dur."
        "exit"->"Pozdan kontrollü çık. Rahatça dinlen."
        "cooldown"->"Temponu düşür. Rahatça yürü ve doğal nefes al."
        "warmup"->"Önce rahat hareketlerle ısınalım. "+motionCue(move.id)+"."
        else->if(step.rest)"Yavaşla ve yürüyüşe geç. Gerekirse duraklat." else move.hint
    }
}

@Composable fun BreathCircle(inhale:Boolean,label:String,reduced:Boolean){
    val motionDisabled=reduced||!systemMotionAllowed()
    val scale by animateFloatAsState(if(motionDisabled)1f else if(inhale)1.06f else .85f,animationSpec=tween(if(motionDisabled)0 else if(inhale)3500 else 5500,easing=LinearEasing),label="Nefes ritmi")
    Box(Modifier.fillMaxWidth().height(220.dp),contentAlignment=Alignment.Center){Box(Modifier.size(172.dp).scale(scale).border(1.dp,Coral.copy(.5f),CircleShape).padding(12.dp).background(Track,CircleShape),contentAlignment=Alignment.Center){Column(horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(10.dp)){Icon(ArcIcons.Breath,null,Modifier.size(34.dp),tint=Coral);Text(label,color=Ink,fontWeight=FontWeight.Bold,fontSize=18.sp)}}}
}

@Composable private fun systemMotionAllowed():Boolean{
    val context=LocalContext.current
    var allowed by remember{mutableStateOf(android.animation.ValueAnimator.areAnimatorsEnabled())}
    DisposableEffect(context){
        val observer=object:android.database.ContentObserver(android.os.Handler(android.os.Looper.getMainLooper())){
            override fun onChange(selfChange:Boolean){allowed=android.animation.ValueAnimator.areAnimatorsEnabled()}
        }
        context.contentResolver.registerContentObserver(android.provider.Settings.Global.getUriFor(
            android.provider.Settings.Global.ANIMATOR_DURATION_SCALE),false,observer)
        onDispose{context.contentResolver.unregisterContentObserver(observer)}
    }
    return allowed
}

@Composable fun Pose(
    id:String,modifier:Modifier=Modifier,color:Color=Ink,reduced:Boolean=false,speed:Float=1f,
    playing:Boolean=true,stepProgress:Float?=null,side:String=""
){
    var phase by remember(id){mutableFloatStateOf(.18f)}
    var renderedProgress by remember(id){mutableFloatStateOf(stepProgress?:0f)}
    val targetProgress by rememberUpdatedState(stepProgress)
    val lifecycle=androidx.compose.ui.platform.LocalLifecycleOwner.current.lifecycle
    var foreground by remember(lifecycle){mutableStateOf(lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED))}
    DisposableEffect(lifecycle){
        val observer=androidx.lifecycle.LifecycleEventObserver{_,_->
            foreground=lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED)
        }
        lifecycle.addObserver(observer)
        onDispose{lifecycle.removeObserver(observer)}
    }
    val motionAllowed=systemMotionAllowed()&&!reduced
    LaunchedEffect(id,motionAllowed,playing,foreground,speed){
        if(motionAllowed&&playing&&foreground){
            val base=phase
            val start=withFrameNanos{it}
            var previous=start
            val duration=motionDuration(id).toDouble()*speed.coerceIn(.25f,4f)*1_000_000.0
            while(true)withFrameNanos{now->
                phase=((base+(now-start)/duration)%1.0).toFloat()
                targetProgress?.let{target->
                    val blend=(1.0-exp(-(now-previous)/80_000_000.0)).toFloat()
                    renderedProgress+=(target-renderedProgress)*blend
                }
                previous=now
            }
        }
    }
    val title=Content.moves.find{it.id==id}?.title?:"Hareket"
    Canvas(modifier.clearAndSetSemantics{
        contentDescription=title+" çizgi rehberi. "+motionCue(id)+
            (if(side=="left")". Sol taraf." else if(side=="right")". Sağ taraf." else "")+
            (if(!motionAllowed)". Sabit gösterim." else if(!playing)". Duraklatıldı." else "")
    }){
        // Read the animation clock in draw phase, not composition/layout.
        val shownProgress=if(!motionAllowed).5f else if(stepProgress!=null)renderedProgress else null
        val pose=motionFrame(id,phase,shownProgress,side)
        val unit=min(size.width,size.height)/112f
        val left=(size.width-100f*unit)/2f
        fun point(x:Float,y:Float)=Offset(left+x*unit,(y+7f)*unit)
        fun p(index:Int)=point(pose.joints[index].x,pose.joints[index].y)
        fun line(a:Offset,b:Offset,tint:Color=color,width:Float=3.2f)=drawLine(tint,a,b,width*unit,StrokeCap.Round)
        line(point(7f,94f),point(96f,94f),color.copy(alpha=.22f),1f)
        if(id=="sprint"||id=="walk"){
            val period=if(id=="sprint")24.375f else 21.774193f
            for(i in -1..5){
                val x=i*period-pose.groundOffset%period
                if(x in -12f..100f)line(point(x.coerceAtLeast(0f),101f),point((x+9f).coerceIn(0f,100f),101f),Sky.copy(alpha=.22f),1f)
            }
            if(pose.frontContact)drawCircle(Coral.copy(alpha=.22f),3f*unit,point(pose.joints[8].x,94f))
            if(pose.rearContact)drawCircle(Sky.copy(alpha=.18f),2.7f*unit,point(pose.joints[10].x,94f))
        }
        when(pose.support){
            "wall"->line(point(84f,8f),point(84f,97f),Sky.copy(alpha=.55f),2f)
            "rail"->{
                val x=if(side=="right")24f else 76f
                line(point(x,28f),point(x,97f),Sky.copy(alpha=.5f),2f)
                line(point(x-6f,28f),point(x+6f,28f),Sky.copy(alpha=.5f),2f)
            }
            "chair"->{
                line(point(48f,72f),point(82f,72f),Sky.copy(alpha=.5f),2f)
                line(point(81f,49f),point(81f,94f),Sky.copy(alpha=.5f),2f)
                line(point(51f,72f),point(51f,94f),Sky.copy(alpha=.5f),2f)
            }
            "mat"->drawRoundRect(Sky.copy(alpha=.12f),point(7f,94.5f),
                androidx.compose.ui.geometry.Size(86f*unit,4f*unit),
                androidx.compose.ui.geometry.CornerRadius(2f*unit))
        }
        val rear=color.copy(alpha=.48f)
        listOf(1 to 5,5 to 6,2 to 9,9 to 10).forEach{(a,b)->line(p(a),p(b),rear)}
        fun foot(index:Int,tint:Color){
            val ankle=p(index)
            if(id=="calf")line(ankle,point(pose.joints[index].x+4.5f,94f),tint)
            else line(ankle,ankle+Offset((if(side=="right")-4f else 4f)*unit,0f),tint)
        }
        foot(10,rear)
        if(id=="catcow"){
            val back=Path().apply{
                moveTo(p(1).x,p(1).y)
                cubicTo(p(1).x-10f*unit,p(1).y+pose.spineCurve*unit,
                    p(2).x+10f*unit,p(2).y+pose.spineCurve*unit,p(2).x,p(2).y)
            }
            drawPath(back,color,style=Stroke(3.7f*unit,cap=StrokeCap.Round))
        }else line(p(1),p(2),color,3.7f)
        val head=p(0);val neck=p(1)
        val dx=neck.x-head.x;val dy=neck.y-head.y
        val distance=hypot(dx,dy).coerceAtLeast(.001f)
        line(head+Offset(dx/distance*6.5f*unit,dy/distance*6.5f*unit),neck)
        drawCircle(color,6.5f*unit,head,style=Stroke(width=2.8f*unit))
        // A short crown stroke is the original Arc athlete's signature, not a face or costume.
        drawArc(Coral,215f,75f,false,head-Offset(6.5f*unit,6.5f*unit),
            androidx.compose.ui.geometry.Size(13f*unit,13f*unit),style=Stroke(2.8f*unit,cap=StrokeCap.Round))
        val accent=if(id=="sprint")Coral else Sky
        listOf(1 to 3,3 to 4,2 to 7,7 to 8).forEach{(a,b)->line(p(a),p(b),accent)}
        drawCircle(color,2f*unit,p(1))
        drawCircle(color,2f*unit,p(2))
        foot(8,accent)
    }
}
