package com.elevare.active

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable fun WorkoutHome(store:Store,onStart:(String)->Unit,onWorkout:(String)->Unit,onMove:(String)->Unit,onFact:(String)->Unit,
 onSleep:()->Unit,onReview:()->Unit,onProgress:()->Unit,onProgram:()->Unit,onProfile:()->Unit={},onRoutine:(String)->Unit={},onRoutines:()->Unit={}) {
 val s=store.state
 val today=LocalDate.now()
 val day=todayProgram(s,today)
 val closed=isCycleComplete(s,today)&&s.active==null
 val completed=s.sessions.lastOrNull{it.date==today.toString()&&it.completed&&it.type=="workout"}
 val active=s.active
 val blocked=!trainingAllowed(s)||day.blocked
 var why by remember{mutableStateOf(false)}
 var newCycle by remember{mutableStateOf(false)}
 var chooseStart by remember{mutableStateOf(false)}
 var selectedStart by remember(s.start){mutableStateOf(s.start)}
 var showProgression by remember{mutableStateOf(false)}
 val scheduled=isProgramScheduled(s,today)
 val proposal=remember(s,today){progressionProposal(s,today)}
 val dateFormat=remember{DateTimeFormatter.ofPattern("d MMMM",Locale.forLanguageTag("tr"))}
 val w=active?.let(::activeWorkout)?:day.workout
 PageColumn {
  TopBar("elevare",action={IconButton(onClick=onProfile){Icon(ArcIcons.Person,"Profil",tint=Sky)}})
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){
   Eyebrow("BÖLÜM %02d".format(programPhase(journeyDay(s,today)).code.drop(1).toInt()))
   Text("${journeyDay(s,today)} / 90 gün",fontSize=12.sp,color=ArcMuted)
  }
  when {
   active!=null -> {
    WorkoutHero(w,s.reducedMotion,"YARIM KALAN SEANS","Antrenmana devam et",{onStart(w.id)},{onWorkout(w.id)})
    QuietText("Kaldığın bölüm korunuyor. Hazır olduğunda devam et.")
   }
   s.pausedOn!=null -> {
    HomeStateCard("Hikâyen burada.","Planın dinlenmede. Geçmişin ve kaldığın gün korunuyor.",ArcIcons.Pause){
     BigButton("Planıma devam et",{store.update{resumePlan(it)}},icon=ArcIcons.Play)
    }
   }
   closed -> {
    HomeStateCard("Bu bölümün özeti hazır.","90 günlük programın sona erdi. Kayıtlarına bak; sonraki adımı sen seç.",ArcIcons.Check){
     BigButton("Özetimi gör",onProgress,icon=ArcIcons.Progress)
     TextButton(onClick={newCycle=true}){Text("Yeni bir döngü oluştur")}
    }
   }
   scheduled -> {
    HomeStateCard("İlk antrenman ${LocalDate.parse(s.start).format(dateFormat)}.","Günlerin hazır. Başlangıca kadar hareket rehberlerini inceleyebilirsin.",ArcIcons.Program){
     BigButton("Programımı gör",onProgram,icon=ArcIcons.Program)
     TextButton(onClick={chooseStart=true}){Text("Başlangıç gününü değiştir")}
    }
   }
   blocked -> {
    HomeStateCard("Önce sana uygun olsun.","Başlamadan önce hareket uygunluğunu netleştirelim. Program ve bilgiler burada.",ArcIcons.Shield){
     BigButton("Yanıtlarımı gözden geçir",onReview)
    }
   }
   completed!=null -> {
    HomeStateCard("Bugünün izi kaldı.","${minutesText(completed.seconds)} · Antrenmanın kaydedildi. Şimdi toparlanma zamanı.",ArcIcons.Check){
     BigButton("Seans özetimi gör",onProgress,icon=ArcIcons.Progress)
    }
   }
   !day.training -> {
    HomeStateCard("Bugün toparlanma.","Dinlenme de hikâyenin bir parçası. Sıradaki antrenmanın programında.",ArcIcons.Moon){
     BigButton("Programımı gör",onProgram,icon=ArcIcons.Program)
     TextButton(onClick={onStart("breath")}){Icon(ArcIcons.Breath,null);Spacer(Modifier.width(8.dp));Text("İstersen 2 dakika rahat nefes")}
    }
   }
   else -> {
    WorkoutHero(w,s.reducedMotion,"BUGÜNÜN ANTRENMANI","Antrenmana başla",{onStart(w.id)},{if(w.hasSprint())onMove("sprint")else onWorkout(w.id)})
    if(day.adapted) TextButton(onClick={why=true}){Icon(ArcIcons.Settings,null,Modifier.size(17.dp));Spacer(Modifier.width(8.dp));Text("Bugünkü uyarlama",fontSize=13.sp)}
   }
  }
  if(active==null)LifeHomeSupport(store,onRoutine,onRoutines)
  if(canChooseProgramStart(s,today)&&!scheduled)TextButton(onClick={chooseStart=true}){
   Icon(ArcIcons.Program,null,Modifier.size(18.dp));Spacer(Modifier.width(8.dp));Text("İlk antrenman gününü seç")
  }
  if(proposal!=null)TextButton(onClick={showProgression=true}){
   Icon(ArcIcons.Progress,null,Modifier.size(18.dp));Spacer(Modifier.width(8.dp));Text("Bir sonraki adımı incele")
  }
  TodayScience(s,onFact,store=store)
  ExpandSection("Bu bölümün amacı",ArcIcons.Program){
   Text(chapterPurpose(journeyDay(s,today)))
   QuietText("Bu hafta ${weekCompleted(s,today)} seans kaydettin. Bölüm çizgisi takvimini gösterir; kondisyon veya sağlık puanı değildir.")
  }
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(12.dp)){
   OutlinedButton(onClick=onSleep,modifier=Modifier.weight(1f),contentPadding=PaddingValues(horizontal=10.dp,vertical=12.dp)){
    Icon(ArcIcons.Moon,null,Modifier.size(18.dp));Spacer(Modifier.width(8.dp));Text(s.bed,fontSize=14.sp)
   }
   OutlinedButton(onClick=onProgram,modifier=Modifier.weight(1f),contentPadding=PaddingValues(horizontal=10.dp,vertical=12.dp)){
    Icon(ArcIcons.Program,null,Modifier.size(18.dp));Spacer(Modifier.width(8.dp));Text("Programım",fontSize=14.sp)
   }
  }
 }
 if(why)AlertDialog(onDismissRequest={why=false},title={Text("Bu plan neden?")},text={Column(verticalArrangement=Arrangement.spacedBy(12.dp)){
  Text(day.reason.ifBlank{programReason(s.answers(),s.gentle)})
  if(day.reviewPending)QuietText("Koşu, güç ve yoga dozları bu sürümde uzman incelemesi bekliyor. Takvimde planı inceleyebilir, uygun hazırlık alternatifini kullanabilirsin.")
  QuietText("${s.trainingDays} gün · ${s.dailyMinutes} dakika · Yanıtlarına göre")
 }},confirmButton={TextButton(onClick={why=false}){Text("Anladım")}})
 if(newCycle)AlertDialog(onDismissRequest={newCycle=false},title={Text("Yeni döngü oluşturulsun mu?")},text={Text("Önceki kayıtların saklanır. Yeni 90 gün bugünden başlar; deneme veya ödeme yeniden başlamaz.")},confirmButton={TextButton(onClick={store.update{nextCycle(it)};newCycle=false}){Text("Yeni döngü oluştur")}},dismissButton={TextButton(onClick={newCycle=false}){Text("Vazgeç")}})
 if(chooseStart)AlertDialog(onDismissRequest={chooseStart=false},title={Text("İlk antrenman günün")},text={
  Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(8.dp)){
   QuietText("Haftada ${s.trainingDays} gün; aradaki toparlanma günleri korunur.")
   (0L..6L).forEach{offset->val date=today.plusDays(offset)
    Choice(if(offset==0L)"Bugün · ${date.format(dateFormat)}" else date.format(DateTimeFormatter.ofPattern("d MMMM EEEE",Locale.forLanguageTag("tr"))),selected=selectedStart==date.toString()){selectedStart=date.toString()}
   }
  }
 },confirmButton={TextButton(onClick={
  if(store.update{chooseProgramStart(it,LocalDate.parse(selectedStart),today)}&&store.state.start==selectedStart)chooseStart=false
 }){Text("Başlangıcı kaydet")}},dismissButton={TextButton(onClick={chooseStart=false}){Text("Vazgeç")}})
 if(showProgression&&proposal!=null)AlertDialog(onDismissRequest={showProgression=false},title={Text("Sıradaki adım")},text={
  Column(verticalArrangement=Arrangement.spacedBy(12.dp)){
   Text(proposal.summary)
   QuietText("${proposal.fromCode} → ${proposal.toCode} · ${proposal.currentIntervals} → ${proposal.nextIntervals} çevrim")
   QuietText("Yalnız bu seviye için onay verirsin. Günlük uygunluk kontrolü yine yapılır; istersen aynı seviyede kalabilirsin.")
  }
 },confirmButton={TextButton(onClick={if(store.update{acceptProgression(it,proposal.id)})showProgression=false}){Text("Bu adımı kabul et")}},dismissButton={TextButton(onClick={showProgression=false}){Text("Aynı seviyede kal")}})
}

@Composable private fun HomeStateCard(title:String,note:String,icon:androidx.compose.ui.graphics.vector.ImageVector,content:@Composable ColumnScope.()->Unit) {
 Surface(shape=RoundedCornerShape(22.dp),color=Track,border=BorderStroke(1.dp,MaterialTheme.colorScheme.outlineVariant)){
  Column(Modifier.fillMaxWidth().padding(20.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
   Icon(icon,null,tint=Coral,modifier=Modifier.size(34.dp))
   Text(title,fontSize=27.sp,lineHeight=32.sp,fontWeight=FontWeight.Bold)
   QuietText(note);content()
  }
 }
}

@Composable fun ProgramBrowser(s:UserState,onMove:(String)->Unit,onWorkout:(String)->Unit,onBack:(()->Unit)?=null,onEdit:()->Unit={}) {
 var section by rememberSaveable{mutableStateOf("Bu hafta")}
 var selectedWeek by rememberSaveable{mutableIntStateOf((journeyDay(s)-1)/7)}
 var filter by rememberSaveable{mutableStateOf("Tümü")}
 var preview by remember{mutableStateOf<ProgramDay?>(null)}
 val day=journeyDay(s)
 val scheduled=isProgramScheduled(s)
 val all=remember(s){ninetyDayProgram(s)}
 PageColumn {
  TopBar("Programım",onBack,action={IconButton(onClick=onEdit){Icon(ArcIcons.Settings,"Plan tercihlerini düzenle")}})
  ArcSectionLead("KENDİ HİKÂYEN",if(scheduled)"İlk bölümün hazır." else "Antrenman günlüğün.")
  ArcChapterStrip(day,scheduled)
  if(scheduled)QuietText("Başlangıç · ${LocalDate.parse(s.start).format(DateTimeFormatter.ofPattern("d MMMM EEEE",Locale.forLanguageTag("tr")))}")
  QuietText("${programPhase(day).title} · Haftada ${s.trainingDays} gün · ${s.dailyMinutes} dakika")
  Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)){
   listOf("Bu hafta","90 gün","Hareketler").forEach{label->FilterChip(section==label,{section=label},label={Text(label)})}
  }
  when(section) {
   "Bu hafta" -> {
    currentWeekProgram(s).forEach{d->ProgramDayRow(d,s){preview=d}}
    MenuRow("Sprint nedir?","Koşu ve yürüyüş farkı",ArcIcons.Run){onMove("sprint")}
   }
   "90 gün" -> {
    val completed=s.sessions.count{it.completed&&it.type=="workout"&&it.cycleId==s.cycleId}
    Text("$completed tamamlanan seans",fontWeight=FontWeight.Bold)
    QuietText("Takvim ilerlemesi, egzersiz seviyen veya başarı puanın değildir.")
    LinearProgressIndicator(progress={if(scheduled)0f else day/90f},modifier=Modifier.fillMaxWidth(),color=Sky)
    listOf(1,15,29,50,78).map(::programPhase).forEach{phase->
     Surface(shape=RoundedCornerShape(16.dp),color=if(day in phase.firstDay..phase.lastDay)Mint else MaterialTheme.colorScheme.surface){
      Column(Modifier.fillMaxWidth().padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
       Text(phase.title,fontWeight=FontWeight.Bold)
       QuietText("Gün ${phase.firstDay}–${phase.lastDay}")
      }
     }
    }
    Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(6.dp)){
     (0..12).forEach{week->FilterChip(selectedWeek==week,{selectedWeek=week},label={Text("${week+1}. hafta")})}
    }
    all.drop(selectedWeek*7).take(7).forEach{d->ProgramDayRow(d,s){preview=d}}
   }
   else -> {
    Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(6.dp)){
     listOf("Tümü","Koşu","Güç","Yoga","Toparlanma").forEach{label->FilterChip(filter==label,{filter=label},label={Text(label)})}
    }
    Content.moves.filter{filter=="Tümü"||it.category==filter||(filter=="Güç"&&it.category in listOf("Temel güç","Denge"))}.forEach{m->
     Surface(onClick={onMove(m.id)},shape=RoundedCornerShape(16.dp),color=MaterialTheme.colorScheme.surface){
      Row(Modifier.fillMaxWidth().padding(12.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){
       Pose(m.id,Modifier.size(68.dp),Ink,true)
       Column(Modifier.weight(1f)){Text(m.title,fontWeight=FontWeight.Bold);QuietText(m.category)}
       Icon(ArcIcons.Chevron,"Hareket rehberi")
      }
     }
    }
   }
  }
 }
 preview?.let{d->
  val records=programDayRecords(s,d.day)
  val recorded=records.firstOrNull()
  val unrecordedPast=recorded==null&&d.day<day
  val title=recorded?.title?:if(unrecordedPast)"Gün kaydı" else if(d.training)d.plannedWorkout.title else "Dinlenme"
  AlertDialog(onDismissRequest={preview=null},title={Text("${d.day}. gün · $title")},text={
  Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(10.dp)){
   when {
    recorded!=null -> {
     QuietText("Kaydedilen sonuçlar; yeni tercihler geçmişini değiştirmez.")
     records.forEach{log->
      Surface(shape=RoundedCornerShape(14.dp),color=MaterialTheme.colorScheme.surface){
       Column(Modifier.fillMaxWidth().padding(14.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){
        Text(log.title,fontWeight=FontWeight.Bold)
        QuietText("${if(log.completed)"Tamamlandı" else "Kısmi"} · ${minutesText(log.seconds.coerceAtLeast(0))} yapıldı")
        if(!log.completed)QuietText("Oturumun planlanan süresi: ${minutesText(log.plannedSeconds.coerceAtLeast(0))}")
        if(log.feeling.isNotBlank()&&log.feeling!="Kısmi oturum")QuietText("Geri bildirimin: ${log.feeling}")
       }
      }
     }
    }
    unrecordedPast -> QuietText("Bu gün için kaydedilmiş bir seans yok. Geçmiş program, bugünkü tercihlerle yeniden oluşturulmaz.")
    else -> {
     QuietText(if(d.training)"Planlanan · ${minutesText(d.plannedWorkout.seconds)}" else "Zorunlu seans yok.")
     if(d.adapted)Text("Bugünkü uygun alternatif: ${d.workout.title}")
     if(d.reason.isNotBlank())QuietText(d.reason)
     if(d.reviewPending)QuietText("İnceleme bekleyen şablon. Önizleme, antrenman başlatmaz.")
     d.plannedWorkout.steps.filter{!it.rest}.map{it.moveId}.distinct().forEach{id->
      TextButton(onClick={preview=null;onMove(id)}){Text(Content.move(id).title)}
     }
    }
   }
  }
 },confirmButton={TextButton(onClick={preview=null}){Text("Kapat")}},dismissButton={if(!scheduled&&records.isEmpty()&&d.day==day&&d.training&&!d.blocked)TextButton(onClick={preview=null;onWorkout(d.workout.id)}){Text("Bugünkü seans")}})}
}

// Completion wins over an earlier partial attempt. Otherwise newest immutable
// log wins, with append order retained as the fallback for legacy timestamps.
private fun programDayRecords(s:UserState,day:Int):List<SessionLog> =
 if(s.cycleId.isBlank())emptyList() else s.sessions.asReversed()
  .filter{it.type=="workout"&&it.cycleId==s.cycleId&&it.programDay==day}
  .sortedWith(compareByDescending<SessionLog>{it.completed}.thenByDescending{it.completedAtEpochMs})

@Composable private fun ProgramDayRow(d:ProgramDay,s:UserState,onClick:()->Unit) {
 val current=journeyDay(s)
 val isToday=!isProgramScheduled(s)&&d.day==current
 val recorded=programDayRecords(s,d.day).firstOrNull()
 val completed=recorded?.completed==true
 val unrecordedPast=recorded==null&&d.day<current
 val status=when{completed->"Tamamlandı";recorded!=null->"Kısmi";unrecordedPast->"Kayıt yok";!d.training->"Dinlenme";isToday->"Bugün";else->"Planlanan"}
 val title=recorded?.title?:if(unrecordedPast)"Gün kaydı" else if(d.training)d.plannedWorkout.title else "Dinlenme günü"
 val duration=when {
  recorded!=null -> " · ${minutesText(recorded.seconds.coerceAtLeast(0))}${if(recorded.completed)"" else " yapıldı"}"
  !unrecordedPast&&d.training -> " · ${minutesText(d.plannedWorkout.seconds)}"
  else -> ""
 }
 Surface(onClick=onClick,shape=RoundedCornerShape(16.dp),color=if(isToday)Mint else MaterialTheme.colorScheme.surface){
  Row(Modifier.fillMaxWidth().padding(14.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(14.dp)){
   Text("%02d".format(d.day),fontSize=23.sp,fontWeight=FontWeight.Bold,color=if(isToday)Coral else Sky)
   Column(Modifier.weight(1f)){
    Text(title,fontWeight=FontWeight.Bold,fontSize=15.sp)
    QuietText(status+duration)
   }
   Icon(if(completed)ArcIcons.Checked else if(recorded!=null||unrecordedPast||d.training)ArcIcons.Chevron else ArcIcons.Moon,null,tint=Sky)
  }
 }
}
