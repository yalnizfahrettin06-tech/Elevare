package com.elevare.active

import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable fun TrainingOnboarding(store:Store){
 LaunchedEffect(Unit){store.pauseTimer()}
 val s=store.state
 val editing=s.ready
 var draft by remember {
  mutableStateOf(s.onboardingDraft?.restored() ?: OnboardingDraft(
   answers=if(editing)s.answers() else TrainingAnswers(),
   step=if(editing)(1..ONBOARDING_QUESTION_COUNT).firstOrNull{!onboardingStepValid(s.answers(),it)}?:1 else 0
  ))
 }
 var elapsed by remember{mutableLongStateOf(draft.preparingElapsedMs)}
 var saveError by remember{mutableStateOf(false)}
 var finishing by remember{mutableStateOf(false)}
 var showAgeScope by remember{mutableStateOf(false)}
 val answers=draft.answers
 val step=draft.step
 val canCancel=editing && s.answers().complete()
 fun save(next:OnboardingDraft):Boolean {
  val success=store.update{it.copy(onboardingDraft=next)}
  saveError=!success
  return success
 }
 fun answer(next:TrainingAnswers){
  elapsed=0L
  draft=draft.copy(answers=next,preparingElapsedMs=0L)
  save(draft)
 }
 fun navigate(next:Int){
  val candidate=draft.copy(step=next,preparingElapsedMs=elapsed)
  if(save(candidate))draft=candidate
 }
 fun cancel(){
  if(canCancel)store.update{it.copy(onboardingVersion=TRAINING_ONBOARDING_VERSION,onboardingDraft=null)}
 }
 fun goBack(){
  when {
   step>=PREPARATION_STEP->navigate(ONBOARDING_QUESTION_COUNT)
   step==1 && canCancel->cancel()
   step>0->navigate(step-1)
  }
 }
 fun finish(startDemo:Boolean){
  if(!answers.complete() || finishing)return
  finishing=true
  val success=store.update{completeOnboarding(it,answers,startDemo)}
  saveError=!success
  if(!success)finishing=false
 }
 BackHandler(enabled=step>0 || canCancel){goBack()}
 if(showAgeScope)AlertDialog(
  onDismissRequest={showAgeScope=false},
  title={Text("Bu program 13–21 yaş için")},
  text={Text("Farklı bir yaşta olduğunda yanlış yaş seçmeni istemiyoruz. Bu prototip henüz yaş grubuna uygun bir antrenman programı sunmuyor.")},
  confirmButton={TextButton(onClick={showAgeScope=false}){Text("Anladım")}}
 )
 if(step==PREPARATION_STEP){
  PreparationScreen(
   answers,elapsed,
   onElapsed={next->
    val previousSecond=elapsed/1000
    elapsed=next
    if(next/1000!=previousSecond){draft=draft.copy(preparingElapsedMs=next);save(draft)}
   },
   onBack={goBack()},
   onReady={navigate(PROGRAM_READY_STEP)},
   reducedMotion=s.reducedMotion,
   saveError=saveError,
   onCheckpoint={next->elapsed=next;draft=draft.copy(preparingElapsedMs=next);save(draft)}
  )
  return
 }
 if(step==PROGRAM_READY_STEP){
  ProgramReady(
   answers,editing,
   onFinish={finish(true)},onSkip={finish(false)},onBack={navigate(1)},
   saveError=saveError,saving=finishing
  )
  return
 }
 val valid=if(step==ONBOARDING_QUESTION_COUNT)answers.complete() else onboardingStepValid(answers,step)
 val captions=listOf("BAŞLANGIÇ","SENİ TANIYALIM","ODAK NOKTAN","AKTİVİTE RİTMİN","KOŞU DENEYİMİN","ANTRENMAN ALANIN","ELİNDEKİ DESTEKLER","HAFTALIK RİTMİN","ZAMANIN","TOPARLANMAN","BÜYÜME GÖZLEMİN","SON KONTROL")
 Column(Modifier.fillMaxSize()){
  Row(Modifier.fillMaxWidth().padding(horizontal=20.dp,vertical=8.dp),verticalAlignment=Alignment.CenterVertically){
   if(step>0)IconButton(onClick={goBack()}){Icon(ArcIcons.Back,"Geri")}
   ArcBrand(Modifier.weight(1f),compact=true)
   if(step>0)Text("%02d / %02d".format(step,ONBOARDING_QUESTION_COUNT),Modifier.semantics{contentDescription="$ONBOARDING_QUESTION_COUNT sorudan $step. soru"},color=ArcMuted,fontSize=12.sp)
   if(canCancel)IconButton(onClick={cancel()}){Icon(ArcIcons.Close,"Değişiklikleri iptal et")}
  }
  if(step>0)Row(Modifier.fillMaxWidth().padding(horizontal=24.dp,vertical=12.dp).clearAndSetSemantics{},horizontalArrangement=Arrangement.spacedBy(4.dp)){
   (1..ONBOARDING_QUESTION_COUNT).forEach{index->Box(Modifier.weight(1f).height(3.dp).background(if(index<=step)Coral else ArcLine,RoundedCornerShape(3.dp)))}
  }
  AnimatedContent(targetState=step,modifier=Modifier.weight(1f),transitionSpec={
   val forward=targetState>initialState
   val duration=if(s.reducedMotion)0 else 200
   (slideInHorizontally(tween(duration)){if(forward)it/8 else -it/8}+fadeIn(tween(duration))) togetherWith
    (slideOutHorizontally(tween(duration)){if(forward)-it/8 else it/8}+fadeOut(tween(duration)))
  },label="Sorular"){current->
   Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=24.dp,vertical=20.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
    if(current>0)Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)){
     Icon(when(current){1,2,3->ArcIcons.Person;4,5,6->ArcIcons.Run;7,8->ArcIcons.Clock;9->ArcIcons.Moon;10->ArcIcons.Book;else->ArcIcons.Shield},null,Modifier.size(22.dp),tint=Coral)
     Eyebrow(captions[current])
    }
    when(current){
     0->{
      Eyebrow("KENDİ HİKÂYENİN BAŞLANGICI")
      Text("Hikâyen\nhareketle başlar.",fontSize=37.sp,lineHeight=42.sp,letterSpacing=(-1.2).sp,fontWeight=FontWeight.ExtraBold,modifier=Modifier.semantics{heading()})
      QuietText("Hazırlık akışları ve günlük desteklerin. Kendi ritmini kur.")
      ArcStage("sprint",Modifier.fillMaxWidth().height(216.dp),s.reducedMotion)
      FlowRow(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(18.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
       StatPill(ArcIcons.Program,"90 günlük plan")
       StatPill(ArcIcons.Sound,"Sesli koç")
      }
      QuietText("11 kısa soruyla başlayalım. Yanıtların yalnızca cihazında.")
     }
     1->{
      QuestionTitle("Kaç yaşındasın?")
      QuietText("Yaşın tek başına antrenman seviyeni belirlemez.")
      ProfileChoices.ages.toList().chunked(3).forEach{row->
       Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){row.forEach{value->
        Box(Modifier.weight(1f)){OnboardingChoice("$value",answers.age==value,description="$value yaş"){answer(answers.copy(age=value))}}
       }}
      }
      TextButton(onClick={answer(answers.copy(age=0));showAgeScope=true}){Text("Başka bir yaştayım")}
     }
     2->{
      QuestionTitle("Neye odaklanalım?")
      QuietText("Programın ve günlük bilgilerin buna göre şekillenecek.")
      AnswerChoices(ProfileChoices.focus,answers.focus){answer(answers.copy(focus=it))}
     }
     3->{
      QuestionTitle("Şu an ne kadar aktifsin?")
      QuietText("Spor dersi, takım sporu ve yürüyüş de dahil.")
      AnswerChoices(ProfileChoices.activity,answers.activity){answer(answers.copy(activity=it))}
     }
     4->{
      QuestionTitle("Koşu deneyimin nasıl?")
      QuietText("Rahat koşu ve interval farklı deneyimlerdir.")
      AnswerChoices(ProfileChoices.runningExperience,answers.runningExperience){answer(answers.copy(runningExperience=it))}
     }
     5->{
      QuestionTitle("Nerede çalışacaksın?")
      AnswerChoices(ProfileChoices.environment,answers.environment){answer(answers.copy(environment=it))}
      if(answers.environment in setOf("outdoor","both"))QuietText("Koşu için düz, açık ve güvenli bir alan.")
     }
     6->{
      QuestionTitle("Hangi destekler var?")
      QuietText("Birden fazla seçebilirsin. Satın alman gerekmez.")
      ProfileChoices.equipment.forEach{(id,label)->
       OnboardingChoice(label,id in answers.equipment,multiple=true){answer(answers.copy(equipment=toggleEquipment(answers.equipment,id)))}
      }
     }
     7->{
      QuestionTitle("Haftada kaç gün?")
      QuietText("Dinlenme günleri programına dahil.")
      ProfileChoices.days.forEach{value->OnboardingChoice("$value gün",answers.days==value){answer(answers.copy(days=value))}}
     }
     8->{
      QuestionTitle("Bir seansa ne kadar ayırırsın?")
      ProfileChoices.minutes.chunked(2).forEach{row->
       Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){row.forEach{value->
        Box(Modifier.weight(1f)){OnboardingChoice("$value dk",answers.minutes==value,description="$value dakika"){answer(answers.copy(minutes=value))}}
       }}
      }
      QuietText("Toplam süre; ısınma ve toparlanma dahil.")
     }
     9->{
      QuestionTitle("Uykun çoğunlukla nasıl?")
      AnswerChoices(ProfileChoices.sleep,answers.sleep){answer(answers.copy(sleep=it))}
     }
     10->{
      QuestionTitle("Son 6 ayda boyunda artış fark ettin mi?")
      QuietText("Yanıtın, Rehber bölümündeki büyüme açıklamasını seçer. Boy tahmini veya yük hesabı yapılmaz.")
      AnswerChoices(ProfileChoices.growth,answers.recentGrowth){answer(answers.copy(recentGrowth=it))}
     }
     11->{
      QuestionTitle("Hareketi etkileyen bir durum var mı?")
      QuietText("Ağrı, rahatsızlık veya uzman kısıtlamasını düşün.")
      AnswerChoices(ProfileChoices.safety,answers.safety){answer(answers.copy(safety=it))}
      if(answers.safety.isNotBlank() && answers.safety!="clear")QuietText("Planını görebilirsin. Başlamadan önce uygunluğunu bir uzmanla netleştir.")
     }
    }
   }
  }
  Column(Modifier.fillMaxWidth().background(Paper).padding(horizontal=24.dp,vertical=14.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
   HorizontalDivider(color=ArcLine.copy(alpha=.55f),modifier=Modifier.padding(bottom=6.dp))
   if(saveError)SaveError()
   if(!valid)Text("Devam etmek için bir yanıt seç.",fontSize=13.sp,color=MaterialTheme.colorScheme.onSurfaceVariant,modifier=Modifier.semantics{liveRegion=LiveRegionMode.Polite})
   BigButton(
    if(step==0)"Hikâyeme başla" else if(step==ONBOARDING_QUESTION_COUNT)if(editing)"Programı önizle" else "Programımı hazırla" else "Devam et",
    onClick={
     if(valid){
      if(step==ONBOARDING_QUESTION_COUNT && editing){elapsed=PREPARATION_DURATION_MS;navigate(PROGRAM_READY_STEP)}
      else navigate(step+1)
     }
    },
    enabled=valid
   )
  }
 }
}

@Composable private fun QuestionTitle(text:String){
 Text(text,fontSize=30.sp,lineHeight=36.sp,letterSpacing=(-.6).sp,fontWeight=FontWeight.Bold,modifier=Modifier.semantics{heading()})
}

@Composable private fun AnswerChoices(options:Map<String,String>,selected:String,onSelect:(String)->Unit){
 Column(Modifier.selectableGroup(),verticalArrangement=Arrangement.spacedBy(12.dp)){
  options.forEach{(id,label)->OnboardingChoice(label,selected==id){onSelect(id)}}
 }
}

@Composable private fun OnboardingChoice(title:String,selected:Boolean,multiple:Boolean=false,description:String=title,onClick:()->Unit){
 val selectionColor by animateColorAsState(if(selected)Mint else MaterialTheme.colorScheme.surface,animationSpec=tween(140),label="Yanıt yüzeyi")
 Surface(
  onClick=onClick,shape=RoundedCornerShape(topStart=16.dp,topEnd=16.dp,bottomEnd=16.dp,bottomStart=if(selected)5.dp else 16.dp),
  color=selectionColor,
  border=BorderStroke(1.dp,if(selected)Coral else ArcLine),
  modifier=Modifier.fillMaxWidth().semantics{
   this.selected=selected
   role=if(multiple)Role.Checkbox else Role.RadioButton
   contentDescription=description
  }
 ){
  Row(Modifier.heightIn(min=58.dp).padding(horizontal=14.dp,vertical=14.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)){
   Text(title,Modifier.weight(1f),fontSize=16.sp,lineHeight=22.sp,fontWeight=if(selected)FontWeight.SemiBold else FontWeight.Medium)
   if(selected)Box(Modifier.size(24.dp).background(Coral,if(multiple)RoundedCornerShape(6.dp) else CircleShape),contentAlignment=Alignment.Center){Icon(ArcIcons.Check,null,tint=Paper,modifier=Modifier.size(17.dp))}
   else Icon(if(multiple)ArcIcons.Square else ArcIcons.Circle,null,tint=MaterialTheme.colorScheme.outline,modifier=Modifier.size(24.dp))
  }
 }
}

@Composable private fun SaveError(){
 Text("Kaydedilemedi. Seçimlerin burada; devam ederek tekrar dene.",fontSize=13.sp,color=MaterialTheme.colorScheme.error,modifier=Modifier.semantics{liveRegion=LiveRegionMode.Polite})
}

@Composable fun PreparationScreen(
 answers:TrainingAnswers,elapsed:Long,onElapsed:(Long)->Unit,onBack:()->Unit,onReady:()->Unit,
 reducedMotion:Boolean=false,saveError:Boolean=false,onCheckpoint:(Long)->Unit={}
){
 val current by rememberUpdatedState(elapsed)
 val update by rememberUpdatedState(onElapsed)
 val checkpoint by rememberUpdatedState(onCheckpoint)
 val ready by rememberUpdatedState(onReady)
 val lifecycle=LocalLifecycleOwner.current.lifecycle
 var retry by remember{mutableIntStateOf(0)}
 val result=remember(answers,retry){runCatching{require(answers.complete());weeklyProgram(answers).also{require(it.size==7)}}}
 var lastTick by remember{mutableLongStateOf(SystemClock.elapsedRealtime())}
 var resumed by remember{mutableStateOf(lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))}
 DisposableEffect(lifecycle){
  val observer=LifecycleEventObserver{_,_->
   val now=SystemClock.elapsedRealtime()
   val next=advancePreparation(current,now-lastTick,resumed && result.isSuccess)
   lastTick=now
   resumed=lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
   update(next)
   if(!resumed)checkpoint(next)
  }
  lifecycle.addObserver(observer)
  onDispose{lifecycle.removeObserver(observer)}
 }
 LaunchedEffect(answers,retry){
  lastTick=SystemClock.elapsedRealtime()
  while(current<PREPARATION_DURATION_MS && result.isSuccess){
   delay(100)
   val now=SystemClock.elapsedRealtime()
   update(advancePreparation(current,now-lastTick,resumed))
   lastTick=now
  }
 }
 LaunchedEffect(elapsed,result.isSuccess){if(elapsed>=PREPARATION_DURATION_MS && result.isSuccess)ready()}
 Column(Modifier.fillMaxSize()){
  Row(Modifier.padding(horizontal=20.dp,vertical=8.dp)){TopBar("İlk bölüm hazırlanıyor",onBack)}
  Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal=24.dp,vertical=12.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
   ArcStage("sprint",Modifier.fillMaxWidth().height(180.dp),reducedMotion)
   LinearProgressIndicator(progress={(elapsed.toFloat()/PREPARATION_DURATION_MS).coerceIn(0f,1f)},modifier=Modifier.fillMaxWidth().height(3.dp),color=Coral,trackColor=ArcLine)
   Text(if(result.isFailure)"Program hazırlanamadı" else preparationStage(elapsed),fontSize=25.sp,lineHeight=30.sp,fontWeight=FontWeight.Bold,modifier=Modifier.semantics{heading();liveRegion=LiveRegionMode.Polite})
   QuietText(if(result.isFailure)"Yanıtların korunuyor. Yeniden deneyebilirsin." else "Yanıtlarından oluşturulan planın kısa özeti")
   val labels=listOf("Tercihler değerlendiriliyor","Antrenman günleri düzenleniyor","Hareket listesi oluşturuluyor","İlk hafta hazırlanıyor")
   Column(verticalArrangement=Arrangement.spacedBy(6.dp)){labels.forEachIndexed{index,label->
    val done=elapsed>=(index+1)*1000L
    val active=elapsed>=index*1000L
    Row(Modifier.fillMaxWidth().background(if(active)Mint else Paper,RoundedCornerShape(12.dp)).padding(12.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){
     Icon(if(done)ArcIcons.Checked else ArcIcons.Circle,null,tint=if(active)Sky else MaterialTheme.colorScheme.outline,modifier=Modifier.size(20.dp))
     Text(label,fontSize=14.sp,color=if(active)Ink else MaterialTheme.colorScheme.onSurfaceVariant)
    }
   }}
   QuietText("${answers.days} antrenman günü · ${answers.minutes} dk")
   if(saveError)SaveError()
  }
  if(result.isFailure || saveError)Column(Modifier.padding(24.dp)){
   BigButton("Tekrar dene",{if(result.isFailure)retry++ else if(elapsed>=PREPARATION_DURATION_MS)ready() else checkpoint(elapsed)})
  }
 }
}

@Composable private fun ProgramReady(
 answers:TrainingAnswers,editing:Boolean,onFinish:()->Unit,onSkip:()->Unit,onBack:()->Unit,saveError:Boolean=false,saving:Boolean=false
){
 val result=remember(answers){runCatching{weeklyProgram(answers)}}
 val week=result.getOrNull()
 val blocked=answers.safety!="clear"
 Column(Modifier.fillMaxSize()){
  Row(Modifier.padding(horizontal=20.dp,vertical=8.dp)){TopBar("Programın hazır",onBack)}
  Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal=24.dp,vertical=14.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
   Eyebrow("TRAINING ARC  /  90 GÜN")
   Text(if(editing)"Hikâyen sana uysun." else "İlk bölümün hazır.",fontSize=32.sp,lineHeight=37.sp,fontWeight=FontWeight.ExtraBold,modifier=Modifier.semantics{heading()})
   QuietText("Haftada ${answers.days} gün · ${answers.minutes} dakika tercihinle")
   QuietText("Bu önizlemede koşu, güç ve yoga yerine hazırlık alternatifleri açık; diğer programlar uzman incelemesini bekliyor.")
   if(week!=null){
    WeekStrip(week,0)
    week.filter{it.training}.groupBy{it.workout.id}.values.forEach{days->
     val day=days.first()
     Surface(color=MaterialTheme.colorScheme.surface,shape=RoundedCornerShape(16.dp)){
      Row(Modifier.fillMaxWidth().padding(14.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){
       Pose(day.workout.heroMove(),Modifier.size(50.dp),Ink,true)
       Column(Modifier.weight(1f)){
        Text(day.workout.title,fontWeight=FontWeight.Bold,fontSize=16.sp)
        QuietText("Gün ${days.joinToString(", "){(it.index+1).toString()}} · ${minutesText(day.workout.seconds)}")
       }
      }
     }
    }
   }else QuietText("Önizleme oluşturulamadı. Yanıtlarını düzenleyip tekrar dene.")
   planReasons(answers).take(3).forEach{reason->Text("• $reason",fontSize=14.sp,color=Sky)}
   week?.firstOrNull{it.training}?.let{Text(workoutDistribution(it.workout),fontSize=13.sp,color=ArcMuted)}
   ExpandSection("Seçimlerim planı nasıl etkiledi?",ArcIcons.Settings){
    QuietText("Alan ve ekipman yanıtları kullanılabilir hareketleri sınırlar. Güncel hazırlık alternatifi farklı hedeflerde benzer olabilir; kişiye özel sağlık hesabı yapılmaz.")
    QuietText(programReason(answers))
   }
   if(blocked)Surface(color=MaterialTheme.colorScheme.errorContainer,shape=RoundedCornerShape(14.dp)){
    Text("Programı inceleyebilirsin. Ağrı, kısıtlama veya belirsizlik netleşmeden antrenman başlamaz.",Modifier.padding(14.dp),fontSize=14.sp,color=MaterialTheme.colorScheme.onErrorContainer)
   }
   if(editing)QuietText("Geçmişin korunur. Bu tercihler sonraki seanslar için uygulanır.")
   TextButton(onClick=onBack){Text("Programı düzenle")}
  }
  Column(Modifier.fillMaxWidth().background(Paper).padding(horizontal=24.dp,vertical=16.dp),verticalArrangement=Arrangement.spacedBy(8.dp),horizontalAlignment=Alignment.CenterHorizontally){
   if(saveError)SaveError()
   if(!editing && !blocked)QuietText("3 gün: uygun bir seansı dene, planını düzenle, kaydını gör. Ücret ve otomatik yenileme yok.")
   BigButton(
    if(saving)"Kaydediliyor…" else if(editing)"Programımı kaydet" else if(blocked)"Programı görüntüle" else "3 gün ücretsiz dene",
    onClick=if(blocked)onSkip else onFinish,icon=ArcIcons.Arrow,enabled=!saving && week!=null
   )
   if(!editing && !blocked)TextButton(onClick=onSkip,enabled=!saving && week!=null){Text("Şimdilik atla")}
  }
 }
}
