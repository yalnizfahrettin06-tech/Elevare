package com.elevare.active

import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.delay

@Composable fun TrainingOnboarding(store:Store){
 LaunchedEffect(Unit){store.pauseTimer()}
 val s=store.state
 var step by rememberSaveable{mutableIntStateOf(0)}
 var age by rememberSaveable{mutableIntStateOf(s.age)}
 var focus by rememberSaveable{mutableStateOf(s.focus)}
 var growth by rememberSaveable{mutableStateOf(s.recentGrowth)}
 var sleep by rememberSaveable{mutableStateOf(s.sleepHabit)}
 var activity by rememberSaveable{mutableStateOf(s.activityHabit)}
 var environment by rememberSaveable{mutableStateOf(s.environment)}
 var days by rememberSaveable{mutableIntStateOf(s.trainingDays)}
 var minutes by rememberSaveable{mutableIntStateOf(if(s.ready)s.dailyMinutes else 0)}
 var safety by rememberSaveable{mutableStateOf(s.safety)}
 var elapsed by rememberSaveable(age,focus,growth,sleep,activity,environment,days,minutes,safety){mutableLongStateOf(0L)}
 val answers=TrainingAnswers(age,focus,growth,sleep,activity,minutes,safety,environment,days)
 fun finish(trial:Int){
  if (!answers.complete()) return
  store.update{it.copy(ready=true,start=java.time.LocalDate.now().toString(),pausedDays=0,pausedOn=null,onboardingVersion=7,age=age,focus=focus,recentGrowth=growth,sleepHabit=sleep,activityHabit=activity,dailyMinutes=minutes,safety=safety,environment=environment,trainingDays=days,dark=true,trialDays=if(s.ready)s.trialDays else normalizeTrialDays(trial),active=null)}
 }
 BackHandler(enabled=step>0){step=if(step>=10)9 else step-1}
 if(step==10){PreparationScreen(answers,elapsed,{elapsed=it},{step=9},{step=11});return}
 if(step==11){ProgramReady(answers,s.ready,{finish(TRIAL_DAYS)},{finish(0)},{step=9});return}
 val valid=when(step){
  0->true;1->age in ProfileChoices.ages;2->focus in ProfileChoices.focus;3->activity in ProfileChoices.activity
  4->environment in ProfileChoices.environment;5->days in ProfileChoices.days;6->sleep in ProfileChoices.sleep
  7->growth in ProfileChoices.growth;8->minutes in ProfileChoices.minutes;else->answers.complete()
 }
 val captions=listOf("BAŞLANGIÇ","SENİ TANIYALIM","ODAK NOKTAN","BAŞLANGIÇ SEVİYEN","ANTRENMAN ALANIN","HAFTALIK RİTMİN","TOPARLANMAN","BÜYÜME GÖZLEMİN","ZAMANIN","SON KONTROL")
 Column(Modifier.fillMaxSize()){
  Row(Modifier.fillMaxWidth().padding(horizontal=20.dp,vertical=8.dp),verticalAlignment=Alignment.CenterVertically){
   if(step>0)IconButton(onClick={step--}){Icon(Icons.Rounded.ArrowBack,"Geri")}
   Text("elevare",Modifier.weight(1f),fontSize=23.sp,fontWeight=FontWeight.Black)
   if(step>0)Text("$step / 9",color=Sky,fontSize=13.sp)
  }
  if(step>0)LinearProgressIndicator(progress={step/9f},modifier=Modifier.fillMaxWidth().padding(horizontal=24.dp).height(3.dp),color=Sky)
  AnimatedContent(targetState=step,modifier=Modifier.weight(1f),transitionSpec={
   val forward=targetState>initialState
   (slideInHorizontally(tween(if(s.reducedMotion)0 else 240)){if(forward)it/5 else -it/5}+fadeIn(tween(220))) togetherWith
    (slideOutHorizontally(tween(200)){if(forward)-it/5 else it/5}+fadeOut(tween(160)))
  },label="Sorular"){
   current->Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){
    if(current>0)Eyebrow(captions[current])
    when(current){
     0->{
      Tag("KOŞU  /  GÜÇ  /  YOGA",Mint,Sky)
      Text("Ritmini bul.\nHarekete geç.",style=MaterialTheme.typography.displayLarge)
      QuietText("Sana göre bir hafta. Adım adım sesli koç.")
      Surface(color=Track,shape=RoundedCornerShape(24.dp)){Pose("sprint",Modifier.fillMaxWidth().height(235.dp),Ink,s.reducedMotion)}
      Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Tag("Kısa intervaller");Tag("Akıcı hareketler")}
      QuietText("Birkaç soruyla programını birlikte hazırlayalım.")
     }
     1 -> {
      QuestionTitle("Kaç yaşındasın?")
      QuietText("Başlangıç temposunu yaşına göre düzenleyelim.")
      ProfileChoices.ages.toList().chunked(3).forEach { row ->
       Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) {
        row.forEach { v ->
         Box(Modifier.weight(1f)) { Choice("$v",selected=age==v) { age=v } }
        }
       }
      }
     }
     2->{QuestionTitle("Neye odaklanalım?");QuietText("Programın ve günlük bilgiler buna göre şekillenecek.");AnswerChoices(ProfileChoices.focus,focus){focus=it}}
     3->{QuestionTitle("Şu an ne kadar aktifsin?");QuietText("Koşu, spor ve aktif yürüyüşleri düşün.");AnswerChoices(ProfileChoices.activity,activity){activity=it}}
     4->{QuestionTitle("Nerede çalışacaksın?");QuietText("Koşu için güvenli, açık bir alan gerekir.");AnswerChoices(ProfileChoices.environment,environment){environment=it}}
     5->{QuestionTitle("Haftada kaç gün?");QuietText("Aralara toparlanma yerleştireceğiz.");ProfileChoices.days.forEach{v->Choice("$v gün",if(v==2)"Sakin başlangıç" else if(v==3)"Dengeli bir hafta" else "Daha düzenli bir ritim",days==v){days=v}}}
     6->{QuestionTitle("Genelde ne kadar uyuyorsun?");AnswerChoices(ProfileChoices.sleep,sleep){sleep=it};QuietText("Az uyuduğunda programı hafifleteceğiz.")}
     7->{QuestionTitle("Son 6 ayda boyunda artış fark ettin mi?");AnswerChoices(ProfileChoices.growth,growth){growth=it};QuietText("Bu gözlem büyüme bilgilerini seçmemize yardım eder. Boy tahmini yapılmaz.")}
     8 -> {
      QuestionTitle("Bir seansa ne kadar ayırırsın?")
      ProfileChoices.minutes.chunked(2).forEach { row ->
       Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) {
        row.forEach { v ->
         Box(Modifier.weight(1f)) { Choice("$v dk",selected=minutes==v) { minutes=v } }
        }
       }
      }
      QuietText("Isınma ve toparlanma bu süreye dahil. 5 dakika seçersen koşuya hazırlıkla başlarız.")
     }
     9->{QuestionTitle("Başlamaya uygun musun?");QuietText("Hareketi etkileyen ağrı veya uzman kısıtlaması var mı?");AnswerChoices(ProfileChoices.safety,safety){safety=it};if(safety.isNotBlank()&&safety!="clear")QuietText("Programını görebilirsin. Başlamadan önce uygunluğunu netleştirelim.")}
    }
   }
  }
  Column(Modifier.padding(horizontal=24.dp,vertical=16.dp)){
   BigButton(if(step==0)"Programımı oluştur" else if(step==9)"Programımı hazırla" else "Devam et",{step++},enabled=valid)
  }
 }
}
@Composable private fun QuestionTitle(text:String){Text(text,style=MaterialTheme.typography.headlineLarge)}
@Composable private fun AnswerChoices(options:Map<String,String>,selected:String,onSelect:(String)->Unit){options.forEach{(id,label)->Choice(label,selected=selected==id){onSelect(id)}}}

@Composable fun PreparationScreen(answers:TrainingAnswers,elapsed:Long,onElapsed:(Long)->Unit,onBack:()->Unit,onReady:()->Unit){
 val current by rememberUpdatedState(elapsed)
 val update by rememberUpdatedState(onElapsed)
 val lifecycle=LocalLifecycleOwner.current.lifecycle
 val week=remember(answers){weeklyProgram(answers)}
 LaunchedEffect(answers){
  var previous=SystemClock.elapsedRealtime()
  while(current<PREPARATION_DURATION_MS){
   delay(100);val now=SystemClock.elapsedRealtime()
   if(lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))update((current+(now-previous).coerceAtMost(1000)).coerceAtMost(PREPARATION_DURATION_MS))
   previous=now
  }
 }
 LaunchedEffect(elapsed){if(elapsed>=PREPARATION_DURATION_MS)onReady()}
 PageColumn{
  TopBar("Program hazırlanıyor",onBack)
  Spacer(Modifier.height(16.dp))
  Box(Modifier.fillMaxWidth().height(230.dp),contentAlignment=Alignment.Center){
   CircularProgressIndicator(progress={elapsed.toFloat()/PREPARATION_DURATION_MS},modifier=Modifier.size(210.dp),color=Sky,strokeWidth=4.dp,trackColor=Mint)
   Pose("sprint",Modifier.size(170.dp),Ink)
  }
  Text(preparationStage(elapsed),style=MaterialTheme.typography.headlineMedium)
  QuietText("Yaklaşık 30 saniye · Sana uygun bir antrenman haftası")
  val labels=listOf("Tercihler değerlendiriliyor","Antrenman günleri hesaplanıyor","Hareket listesi oluşturuluyor","Program düzenleniyor")
  labels.forEachIndexed{i,label->
   val done=elapsed>=(i+1)*7500L;val active=elapsed>=i*7500L
   Row(Modifier.fillMaxWidth().background(if(active)Mint else Paper,RoundedCornerShape(12.dp)).padding(14.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){
    Icon(if(done)Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,null,tint=if(active)Sky else MaterialTheme.colorScheme.outline,modifier=Modifier.size(20.dp))
    Text(label,fontSize=14.sp,color=if(active)Ink else MaterialTheme.colorScheme.onSurfaceVariant)
   }
  }
  QuietText(if(elapsed>=15000)"${week.count{it.training}} antrenman günü · ${answers.minutes} dk · ${focusLabel(answers.focus)}" else "Isınma, hareket ve toparlanma birlikte planlanıyor.")
 }
}

@Composable private fun ProgramReady(answers:TrainingAnswers,editing:Boolean,onFinish:()->Unit,onSkip:()->Unit,onBack:()->Unit){
 val week=remember(answers){weeklyProgram(answers)}
 Column(Modifier.fillMaxSize()){
  Row(Modifier.padding(16.dp)){TopBar("Programın hazır",onBack)}
  Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal=24.dp),verticalArrangement=Arrangement.spacedBy(18.dp)){
   Tag("SANA GÖRE  /  ${answers.days} GÜN",Mint,Sky)
   Text("İlk haftan\nhazır.",style=MaterialTheme.typography.displayLarge)
   QuietText(programReason(answers))
   WeekStrip(week,0)
   week.filter{it.training}.forEach{d->
    Surface(color=MaterialTheme.colorScheme.surface,shape=RoundedCornerShape(16.dp)){
     Row(Modifier.fillMaxWidth().padding(14.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){
      Pose(d.workout.heroMove(),Modifier.size(54.dp),Ink,true)
      Column(Modifier.weight(1f)){Text(d.workout.title,fontWeight=FontWeight.Bold);QuietText("${d.index+1}. gün · ${minutesText(d.workout.seconds)}")}
     }
    }
   }
   QuietText(sleepGuide(answers.age))
  }
  Column(Modifier.padding(24.dp),verticalArrangement=Arrangement.spacedBy(8.dp),horizontalAlignment=Alignment.CenterHorizontally){
   if(!editing)QuietText("3 günlük demo · Ücret veya otomatik yenileme yok.")
   BigButton(if(editing)"Programımı kaydet" else "3 gün ücretsiz dene",onFinish,icon=Icons.Rounded.PlayArrow)
   if(!editing)TextButton(onClick=onSkip){Text("Şimdilik atla")}
  }
 }
}
