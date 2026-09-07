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
 val week=weeklyProgram(s.answers(),s.gentle)
 PageColumn{
  TopBar("Antrenman")
  Text("Haftanın planı.",style=MaterialTheme.typography.headlineLarge)
  Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){listOf("Programım","Hareketler").forEach{label->FilterChip(selected=section==label,onClick={section=label},label={Text(label)})}}
  if(section=="Programım"){
   QuietText("${s.trainingDays} gün · ${s.dailyMinutes} dk · ${if(s.environment=="indoor")"Evde" else "Koşu ve tamamlayıcı hareketler"}")
   ProgramList(week,(journeyDay(s)-1)%7,onWorkout)
   MenuRow("Sprint nedir?","Isınma, hızlanma ve yürüyüş araları",Icons.Rounded.DirectionsRun){onMove("sprint")}
  }else{
   Content.moves.forEach{m->
    Surface(onClick={onMove(m.id)},shape=RoundedCornerShape(16.dp),color=MaterialTheme.colorScheme.surface){
     Row(Modifier.fillMaxWidth().padding(12.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(14.dp)){
      Pose(m.id,Modifier.size(66.dp),Ink,true)
      Column(Modifier.weight(1f)){Text(m.title,fontWeight=FontWeight.Bold,fontSize=16.sp);QuietText(m.category)}
      Icon(Icons.Rounded.PlayCircleOutline,null,tint=Sky,modifier=Modifier.size(24.dp))
     }
    }
   }
  }
 }
}

@Composable fun WorkoutDetail(w:Workout,s:UserState,onBack:()->Unit,onFavorite:(String)->Unit,onStart:(Workout)->Unit,onMove:(String)->Unit) {
    PageColumn {
        TopBar("ANTRENMAN",onBack,action={IconButton(onClick={onFavorite(w.id)}){Icon(if(w.id in s.favorites)Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,"Favori durumunu değiştir")}})
        Surface(shape=RoundedCornerShape(24.dp),color=Track){
            Column(Modifier.padding(22.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
                Text(shortWorkoutTitle(w),color=Ink,fontSize=25.sp,fontWeight=FontWeight.Bold)
                Pose(w.heroMove(),Modifier.fillMaxWidth().height(150.dp),Ink,s.reducedMotion)
                CompositionLocalProvider(LocalContentColor provides Ink){Row(horizontalArrangement=Arrangement.spacedBy(20.dp)){StatPill(Icons.Rounded.Timer,minutesText(w.seconds));StatPill(Icons.Rounded.SportsGymnastics,"${w.movementCount} hareket")}}
            }
        }
        QuietText(if(s.age<18)"İlk denemede güvendiğin bir yetişkinden destek al. Ağrı veya baş dönmesinde dur." else "Tekniğinden emin değilsen bir antrenörden destek al. Ağrı veya baş dönmesinde dur.")
        BigButton(if(w.id=="breath")"Nefese başla" else "Antrenmana başla",{onStart(w)},icon=Icons.Rounded.PlayArrow,enabled=s.pausedOn==null)
        ExpandSection("Seans hakkında",Icons.Rounded.Info){Text(w.subtitle);QuietText(w.equipment);QuietText(w.dayBrief());QuietText("Isınma ve toparlanma toplam süreye dahil.")}
        BlockTitle("SEANSIN AKIŞI")
        w.steps.forEachIndexed{i,step->
            val m=Content.move(step.moveId)
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable{onMove(m.id)}.padding(vertical=12.dp,horizontal=6.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(14.dp)){
                Text("%02d".format(i+1),fontSize=16.sp,fontWeight=FontWeight.Bold,color=MaterialTheme.colorScheme.onSurfaceVariant)
                Column(Modifier.weight(1f)){Text(if(step.rest)"Dinlen / rahat yürü" else m.title,fontWeight=FontWeight.Bold,fontSize=15.sp);Text(if(step.rest)"Gerekirse daha uzun dinlen." else m.category,fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)}
                Text("${step.seconds} sn",fontSize=13.sp,fontWeight=FontWeight.Bold);Icon(Icons.Rounded.ChevronRight,null,Modifier.size(17.dp))
            }
            if(i!=w.steps.lastIndex)HorizontalDivider(color=MaterialTheme.colorScheme.outlineVariant)
        }
        Text("Bu kısa seans, gün içindeki spor ve oyunlarının tamamının yerine geçmez.",fontSize=13.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable fun MoveDetail(m:Move,reduced:Boolean,onBack:()->Unit){
    var frozen by rememberSaveable{mutableStateOf(false)}
    var slow by rememberSaveable{mutableStateOf(false)}
    PageColumn{
        TopBar("HAREKET REHBERİ",onBack)
        Tag(m.category.uppercase())
        Text(m.title.uppercase(java.util.Locale.forLanguageTag("tr")),style=MaterialTheme.typography.headlineLarge)
        Surface(shape=RoundedCornerShape(24.dp),color=Mint){Column(Modifier.padding(20.dp),horizontalAlignment=Alignment.CenterHorizontally){Pose(m.id,Modifier.fillMaxWidth().height(245.dp),Ink,reduced||frozen,if(slow)2f else 1f);Text("Şematik hareket rehberi",color=Ink.copy(.65f),fontSize=12.sp)}}
        Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){FilterChip(selected=frozen,onClick={frozen=!frozen},label={Text(if(frozen)"Oynat" else "Duraklat")});FilterChip(selected=slow,onClick={slow=!slow},label={Text("Yavaş göster")})}
        QuietText(motionCue(m.id))
        Text(m.hint,fontWeight=FontWeight.Bold,fontSize=19.sp)
        m.steps.forEachIndexed{i,step->Row(horizontalArrangement=Arrangement.spacedBy(16.dp)){Box(Modifier.size(34.dp).background(Lime,CircleShape),contentAlignment=Alignment.Center){Text((i+1).toString(),fontWeight=FontWeight.Bold,color=Ink)};Text(step,Modifier.weight(1f),style=MaterialTheme.typography.bodyLarge)}}
        InfoCard("Çizgi figürü basitleştirilmiş bir gösterimdir. Teknik adımları izle; gerektiğinde bir antrenörden destek al.",Icons.Rounded.Info)
    }
}

@Composable fun SessionScreen(store:Store,onClose:()->Unit,onSaved:()->Unit){
    val a=store.state.active
    if(a==null){PageColumn{TopBar("SEANS",onClose);InfoCard("Aktif seans yok.")};return}
    val w=Content.workout(a.workoutId)
    val step=w.steps[a.step]
    val move=Content.move(step.moveId)
    var now by remember{mutableLongStateOf(System.currentTimeMillis())}
    var stop by remember{mutableStateOf(false)}
    var feeling by rememberSaveable{mutableStateOf("İyi")}
    val activity=LocalContext.current as? ComponentActivity
    val left=remaining(a,now)
    val allDone=left==0&&a.step==w.steps.lastIndex
    LaunchedEffect(a.id,a.step,left,a.running,store.state.autoAdvance){
        if(left==0&&a.running&&store.state.autoAdvance&&!allDone){delay(350);store.update{advanceSession(it)}}
    }
    val audio=rememberSessionAudio(store,a,move,left,step.rest)
    var soundSettings by remember{mutableStateOf(false)}
    LaunchedEffect(a.id,a.step,a.running){now=System.currentTimeMillis();while(a.running&&remaining(a,now)>0){delay(200);now=System.currentTimeMillis()}}
    DisposableEffect(a.running){if(a.running)activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);onDispose{activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)}}
    fun toggle(){val current=store.state.active?:return;store.update{it.copy(active=if(current.running)current.copy(running=false,remaining=remaining(current),deadline=0) else current.copy(running=true,deadline=System.currentTimeMillis()+current.remaining*1000L))}}
    PageColumn{
        TopBar(if(step.rest)"Dinlenme" else "Antrenman",onClose,action={Row(verticalAlignment=Alignment.CenterVertically){Text("${a.step+1} / ${w.steps.size}",fontSize=13.sp);IconButton(onClick={soundSettings=true}){Icon(Icons.Rounded.VolumeUp,"Sesli koç ayarları")}}})
        LinearProgressIndicator(progress={((a.elapsed+step.seconds-left).toFloat()/w.seconds).coerceIn(0f,1f)},modifier=Modifier.fillMaxWidth().height(7.dp).clip(CircleShape),color=Blue,trackColor=MaterialTheme.colorScheme.surfaceVariant)
        if(allDone){
            Column(Modifier.fillMaxWidth().padding(vertical=18.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(18.dp)){
                Box(Modifier.size(112.dp).background(Lime,CircleShape),contentAlignment=Alignment.Center){Icon(Icons.Rounded.Check,"Tamamlandı",Modifier.size(54.dp),tint=Ink)}
                Text("GÜZEL İŞ!",style=MaterialTheme.typography.displayLarge)
                Text("Bugün kendine hareket alanı açtın.",textAlign=TextAlign.Center,color=MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Nasıl hissettirdi?",fontWeight=FontWeight.Bold)
                Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){listOf("Kolay","İyi","Zorlayıcı").forEach{v->FilterChip(selected=feeling==v,onClick={feeling=v},label={Text(v)})}}
                FilterChip(selected=feeling=="Rahatsızlık",onClick={feeling="Rahatsızlık"},label={Text("Ağrı / rahatsızlık hissettim")})
                if(feeling=="Rahatsızlık")InfoCard("Şimdilik dur. Rahatsızlık sürüyorsa sağlık uzmanına danış; 18 yaş altındaysan güvendiğin bir yetişkine de haber ver.",Icons.Rounded.HealthAndSafety)
                BigButton("SEANSI KAYDET",{store.update{completeSession(it,a,feeling)};onSaved()},icon=Icons.Rounded.Check)
            }
        } else {
            Text(if(step.rest)"Biraz dinlen" else move.title,fontSize=24.sp,lineHeight=29.sp,fontWeight=FontWeight.Bold)
            Surface(shape=RoundedCornerShape(26.dp),color=if(step.rest)Mint else Track){Column(Modifier.padding(16.dp),horizontalAlignment=Alignment.CenterHorizontally){
                if(move.id=="breath"){val elapsed=step.seconds-left;val inhaling=elapsed%10<4;BreathCircle(inhaling,if(!a.running)"Duraklatıldı" else if(inhaling)"Nefes al" else "Nefes ver",store.state.reducedMotion||!a.running)}
                else Pose(move.id,Modifier.fillMaxWidth().height(185.dp),Ink,store.state.reducedMotion||!a.running||left==0)
                Text(timeText(left),color=Ink,fontSize=48.sp,fontWeight=FontWeight.Bold,letterSpacing=(-1).sp,modifier=Modifier.semantics{contentDescription="$left saniye kaldı"})
                Text(if(left==0)"Bu bölüm tamamlandı" else if(a.running&&move.id=="sprint")"KONTROLLÜ HIZLAN" else if(a.running&&step.rest)"YÜRÜYEREK TOPARLAN" else if(a.running)"KENDİ HIZINDA" else "DURAKLATILDI",color=Ink.copy(.7f),fontWeight=FontWeight.Bold,fontSize=12.sp,modifier=Modifier.padding(bottom=12.dp))
            }}
            Text(if(step.rest)"Yürüyerek toparlan. Gerekirse duraklat." else move.hint,fontSize=16.sp,lineHeight=23.sp)
            if(store.state.voiceCoach&&audio.status.isNotEmpty())TextButton(onClick={soundSettings=true}){Text("Sesli koç ayarlarını kontrol et",fontSize=12.sp)}
            if(w.hasSprint()){
                val tour=w.steps.take(a.step+1).count{it.moveId=="sprint"}
                val lastSprint=w.steps.indexOfLast{it.moveId=="sprint"}
                QuietText(if(tour==0)"Isınma · Sırada kısa koşu intervalleri" else if(a.step>lastSprint+1)"Soğuma · Tempoyu yavaşça düşür" else "Tur $tour / ${w.intervalCount()} · Koşu ve yürüyüş")
            }
            if(left==0)BigButton("SONRAKİ HAREKET",{val next=w.steps[a.step+1];store.update{it.copy(active=a.copy(step=a.step+1,elapsed=a.elapsed+step.seconds,remaining=next.seconds,deadline=System.currentTimeMillis()+next.seconds*1000L,running=true))}},icon=Icons.Rounded.SkipNext)
            else BigButton(if(a.running)"DURAKLAT" else "DEVAM ET",::toggle,enabled=store.state.pausedOn==null,icon=if(a.running)Icons.Rounded.Pause else Icons.Rounded.PlayArrow)
            TextButton(onClick={store.pauseTimer();stop=true},modifier=Modifier.fillMaxWidth()){Text("Seansı bırak",color=MaterialTheme.colorScheme.onSurfaceVariant)}
            val next=w.steps.getOrNull(a.step+1)
            if(next!=null)QuietText("Sırada: ${if(next.rest)"Dinlenme" else Content.move(next.moveId).title} · ${next.seconds} sn")
        }
    }
    if(soundSettings)AlertDialog(onDismissRequest={soundSettings=false},title={Text("Sesli koç")},text={Column(verticalArrangement=Arrangement.spacedBy(12.dp)){
        SettingToggle("Otomatik geçiş","Bölüm bitince sıradakine geç",store.state.autoAdvance){v->store.update{it.copy(autoAdvance=v)}}
        SettingToggle("Türkçe anlatım","Çevrimdışı ses",store.state.voiceCoach){v->store.update{it.copy(voiceCoach=v)}}
        listOf("off" to "Süre sesi kapalı","countdown" to "Son 3 saniye","every_second" to "Her saniye").forEach{(v,label)->Choice(label,selected=store.state.timerSound==v){store.update{it.copy(timerSound=v)}}}
        TextButton(onClick={audio.speak(move.title+". "+move.hint)},enabled=a.running&&store.state.voiceCoach&&audio.ready){Text("Yönergeyi tekrar dinle")}
        if(audio.status.isNotEmpty())QuietText(audio.status)
    }},confirmButton={TextButton(onClick={soundSettings=false}){Text("Tamam")}})
    if(stop)AlertDialog(onDismissRequest={stop=false},title={Text("Şimdilik bitirelim mi?")},text={Text("Yarım kalan seans tamamlanmış olarak sayılmayacak. Dilediğinde yeniden başlayabilirsin.")},dismissButton={TextButton(onClick={stop=false}){Text("Seansa dön")}},confirmButton={TextButton(onClick={store.update{it.copy(active=null)};stop=false;onClose()}){Text("Kaydetmeden bitir")}})
}

@Composable fun BreathCircle(inhale:Boolean,label:String,reduced:Boolean){
    val scale by animateFloatAsState(if(reduced)1f else if(inhale)1.06f else .85f,animationSpec=tween(if(reduced)0 else if(inhale)3500 else 5500,easing=LinearEasing),label="Nefes ritmi")
    Box(Modifier.fillMaxWidth().height(220.dp),contentAlignment=Alignment.Center){Box(Modifier.size(172.dp).scale(scale).border(2.dp,Ink.copy(.15f),CircleShape).padding(12.dp).background(Track,CircleShape),contentAlignment=Alignment.Center){Column(horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(10.dp)){Icon(Icons.Rounded.Air,null,Modifier.size(34.dp),tint=Lime);Text(label,color=Color.White,fontWeight=FontWeight.Bold,fontSize=18.sp)}}}
}

@Composable fun Pose(id:String,modifier:Modifier=Modifier,color:Color=Ink,reduced:Boolean=false,speed:Float=1f){
 var phase by remember(id){mutableFloatStateOf(.18f)}
 LaunchedEffect(id,reduced,speed){
  if(!reduced){
   var previous=withFrameNanos{it}
   while(true){withFrameNanos{now->
    phase=(phase+(now-previous).coerceAtMost(50_000_000L)/1_000_000f/(motionDuration(id)*speed))%1f
    previous=now
   }}
  }
 }
 val pose=motionFrame(id,phase)
 Canvas(modifier.semantics{contentDescription=(if(id=="sprint")"Koşu" else Content.moves.find{it.id==id}?.title?:"Hareket")+" çizimi"}){
  val unit=min(size.width,size.height)/112f
  val left=(size.width-100f*unit)/2f
  fun point(x:Float,y:Float)=Offset(left+x*unit,(y+7f)*unit)
  fun p(index:Int)=point(pose.joints[index].x,pose.joints[index].y)
  fun line(a:Offset,b:Offset,tint:Color=color,width:Float=3.2f)=drawLine(tint,a,b,width*unit,StrokeCap.Round)
  line(point(7f,94f),point(96f,94f),color.copy(alpha=.18f),1f)
  if(id in listOf("sprint","walk","march")){
   for(i in 0..3){val x=((i*26f-phase*26f+104f)%104f);line(point(x,103f),point((x+11).coerceAtMost(100f),103f),Sky.copy(alpha=.27f),1f)}
  }
  when(pose.support){
   "wall"->line(point(83f,8f),point(83f,97f),Sky.copy(alpha=.4f),2f)
   "rail"->{line(point(82f,29f),point(82f,97f),Sky.copy(alpha=.4f),2f);line(point(76f,29f),point(91f,29f),Sky.copy(alpha=.4f),2f)}
   "chair"->{line(point(45f,70f),point(82f,70f),Sky.copy(alpha=.4f),2f);line(point(80f,48f),point(80f,94f),Sky.copy(alpha=.4f),2f);line(point(49f,70f),point(49f,94f),Sky.copy(alpha=.4f),2f)}
  }
  // Rear limbs render first; the front chain stays readable when limbs cross.
  listOf(1 to 5,5 to 6,2 to 9,9 to 10).forEach{(a,b)->line(p(a),p(b),color.copy(alpha=.42f))}
  line(p(10),p(10)+Offset(4f*unit,0f),color.copy(alpha=.42f))
  if(id=="catcow"){
   val curve=sin(phase*2f*PI.toFloat())*9f
   val back=Path().apply{moveTo(p(1).x,p(1).y);cubicTo(p(1).x-10*unit,p(1).y+curve*unit,p(2).x+10*unit,p(2).y+curve*unit,p(2).x,p(2).y)}
   drawPath(back,color,style=Stroke(3.7f*unit,cap=StrokeCap.Round))
  }else line(p(1),p(2),color,3.7f)
  val head=pose.joints[0];line(point(head.x,head.y+6f),p(1))
  drawCircle(color,6.5f*unit,p(0),style=Stroke(width=2.8f*unit))
  val accent=if(id=="sprint")Coral else Sky
  listOf(1 to 3,3 to 4,2 to 7,7 to 8).forEach{(a,b)->line(p(a),p(b),accent)}
  line(p(8),p(8)+Offset(4.5f*unit,0f),accent)
 }
}
