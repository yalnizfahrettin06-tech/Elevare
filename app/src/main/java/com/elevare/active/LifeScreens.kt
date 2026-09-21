@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package com.elevare.active

import android.Manifest
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Build
import android.provider.Settings
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
import java.time.*

fun routineIcon(id:String)=when(id){"morning"->ArcIcons.Sun;"evening"->ArcIcons.Moon;"water"->ArcIcons.Water;"meal"->ArcIcons.Meal;"focus"->ArcIcons.Progress;else->ArcIcons.Spark}
fun slotLabel(slot:String)=when(slot){"morning"->"Sabah";"day"->"Gün içinde";else->"Akşam"}

@Composable fun LifeHomeSupport(store:Store,onRoutine:(String)->Unit,onHub:()->Unit){
 val life=store.state.life;val now=LocalDateTime.now();val visible=visibleRoutines(life,now.toLocalDate(),now.hour)
 if(!life.welcomed){
  Surface(color=Track,shape=RoundedCornerShape(18.dp),border=BorderStroke(1.dp,ArcLine)){
   Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
    Eyebrow("ANTRENMANIN ÖTESİNDE")
    Text("Gününe küçük bir ritim ekle.",fontWeight=FontWeight.Bold,fontSize=18.sp)
    QuietText("Sabah, akşam veya sana uyan bir alışkanlık. Seçim senin.")
    FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp)){
     TextButton(onClick=onHub){Text("Rutinlerimi seç")}
     TextButton(onClick={store.update{it.copy(life=it.life.copy(welcomed=true))}}){Text("Sadece antrenman")}
    }
   }
  }
 }else if(visible.isNotEmpty()){
  Column(verticalArrangement=Arrangement.spacedBy(8.dp)){
   BlockTitle("Ritmini destekle","Tümü",onHub)
   visible.forEach{p->RoutineRow(p,life,now.toLocalDate()){onRoutine(p.id)}}
  }
 }
}
@Composable private fun RoutineRow(p:LifeRoutinePlan,life:LifeState,date:LocalDate,onOpen:()->Unit){
 val t=LifeCatalog.find(p.id)?:return
 val done=routineDoneCount(life,p.id,date);val finished=routineRecorded(life,p.id,date)
 Surface(onClick=onOpen,color=Track,shape=RoundedCornerShape(16.dp),modifier=Modifier.fillMaxWidth()){
  Row(Modifier.heightIn(min=64.dp).padding(14.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){
   Icon(routineIcon(p.id),null,tint=if(finished)ArcMuted else Coral,modifier=Modifier.size(23.dp))
   Column(Modifier.weight(1f)){
    Text(t.title,fontSize=15.sp,fontWeight=FontWeight.SemiBold)
    Text(if(!p.enabled)"Kapalı" else if(finished)"$done / ${t.steps.size} adım yapıldı" else "${p.time} · ${t.steps.size} küçük adım",fontSize=12.sp,color=ArcMuted)
   }
   Icon(if(finished)ArcIcons.Checked else ArcIcons.Chevron,null,tint=Sky,modifier=Modifier.size(20.dp))
  }
 }
}

@Composable fun RoutinesScreen(store:Store,onRoutine:(String)->Unit,onProgram:()->Unit,onSleep:()->Unit,onProgress:()->Unit,onProfile:()->Unit){
 val s=store.state;val life=s.life;val today=LocalDate.now()
 var catalog by rememberSaveable{mutableStateOf(false)}
 var review by rememberSaveable{mutableStateOf(false)}
 PageColumn{
  TopBar("elevare",action={IconButton(onClick=onProfile){Icon(ArcIcons.Person,"Profil")}})
  ArcSectionLead("KENDİ RİTMİNİ KUR","Az ama sana göre.","Antrenmanın merkezde. Küçük desteklerini sen seç.")
  MenuRow("90 günlük antrenman planı","${s.trainingDays} gün / hafta · ${s.dailyMinutes} dk tercihi",ArcIcons.Run,onProgram)
  if(!life.welcomed){
   InfoCard("Başlangıç için en fazla iki rutin seç. İstersen yalnızca antrenmanla devam et.",ArcIcons.Spark)
   BigButton("Rutinlerimi seç",{catalog=true},icon=ArcIcons.Spark)
   TextButton(onClick={store.update{it.copy(life=it.life.copy(welcomed=true))}}){Text("Şimdilik sadece antrenman")}
  }else{
   if(life.plans.isEmpty())QuietText("Henüz destek rutinin yok. İstersen küçük bir adımla başla.")
   listOf("morning","day","evening").forEach{slot->
    val plans=life.plans.filter{LifeCatalog.find(it.id)?.slot==slot}
    if(plans.isNotEmpty()){
     Eyebrow(slotLabel(slot).uppercase(java.util.Locale.forLanguageTag("tr")))
     plans.forEach{p->RoutineRow(p,life,today){onRoutine(p.id)}}
    }
   }
   OutlinedButton(onClick={catalog=true},modifier=Modifier.fillMaxWidth()){Icon(ArcIcons.Spark,null,Modifier.size(18.dp));Spacer(Modifier.width(8.dp));Text("Rutin ekle")}
  }
  MenuRow("Uyku düzenim","${s.bed} → ${s.wake}",ArcIcons.Moon,onSleep)
  if(life.entries.isNotEmpty()){
   Surface(color=Mint,shape=RoundedCornerShape(18.dp)){
    Column(Modifier.fillMaxWidth().padding(16.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){
     Eyebrow("SON 7 GÜN")
     Text("${lifeWeekCount(life,today)} küçük adım kaydettin.",fontSize=19.sp,fontWeight=FontWeight.Bold)
     QuietText("Kendi kayıtların. Atlanan veya kayıtsız günler başarısızlık değildir.")
     TextButton(onClick={review=true}){Text(if(reflectionWeek(today) in life.reflections)"Haftalık değerlendirmemi düzenle" else "Bu hafta sana nasıl geldi?")}
     life.reflections[reflectionWeek(today)]?.let{r->QuietText(when(r.next){"time"->"Sonraki adımın: rutin saatini gözden geçir.";"less"->"Sonraki adımın: istersen bir rutini kapat.";else->"Sonraki adımın: aynı ritimde devam."})}
    }
   }
  }
  MenuRow("Antrenman geçmişim",icon=ArcIcons.Progress,onClick=onProgress)
  ExpandSection("Hatırlatma dengesi",ArcIcons.Sound){LifeNotificationSettings(store)}
  QuietText("Antrenman planına ara vermek destek rutinlerini kapatmaz. Her rutini ayrı yönetebilirsin.")
 }
 if(catalog)RoutinePicker(store){catalog=false}
 if(review)ReflectionDialog(store){review=false}
}

@Composable private fun RoutinePicker(store:Store,onClose:()->Unit){
 var selected by remember{mutableStateOf(emptySet<String>())}
 val first=!store.state.life.welcomed
 val available=LifeCatalog.all.filter{t->store.state.life.plans.none{it.id==t.id&&it.enabled}}
 AlertDialog(onDismissRequest=onClose,title={Text(if(first)"Küçük bir başlangıç" else "Sana uyanı ekle")},text={
  Column(Modifier.heightIn(max=420.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(10.dp)){
   QuietText(if(first)"En fazla iki rutin. Hatırlatmalar başlangıçta kapalı." else "Yeni rutin eklemek zorunda değilsin. Hatırlatmalar kapalı başlar.")
   available.forEach{t->
    Surface(onClick={if(t.id in selected)selected=selected-t.id else if(!first||selected.size<2)selected=selected+t.id},color=if(t.id in selected)Mint else Track,shape=RoundedCornerShape(14.dp),modifier=Modifier.semantics{this.selected=t.id in selected;role=Role.Checkbox}){
     Row(Modifier.fillMaxWidth().padding(14.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){
      Icon(routineIcon(t.id),null,tint=Coral,modifier=Modifier.size(22.dp))
      Column(Modifier.weight(1f)){Text(t.title,fontWeight=FontWeight.Bold);QuietText("${slotLabel(t.slot)} · ${t.steps.size} adım")}
      Icon(if(t.id in selected)ArcIcons.Checked else ArcIcons.Circle,null,tint=Sky)
     }
    }
   }
   if(available.isEmpty())QuietText("Tüm rutinler zaten ekli. Dilersen birini düzenleyebilirsin.")
  }
 },confirmButton={TextButton(enabled=selected.isNotEmpty(),onClick={
  if(store.update{it.copy(life=selected.fold(it.life){l,id->addRoutine(l,id)})})onClose()
 }){Text("Seçtiklerimi ekle")}},dismissButton={TextButton(onClick=onClose){Text("Vazgeç")}})
}

@Composable fun RoutineDetailScreen(store:Store,id:String,onBack:()->Unit){
 val life=store.state.life;val plan=life.plans.find{it.id==id};val template=LifeCatalog.find(id)
 val date=LocalDate.now();val context=LocalContext.current
 var deleting by rememberSaveable{mutableStateOf(false)}
 var permissionDenied by remember{mutableStateOf(false)}
 var previousEntries by remember{mutableStateOf<Map<String,String>?>(null)}
 fun edit(change:(LifeRoutinePlan)->LifeRoutinePlan){store.update{s->s.copy(life=s.life.copy(plans=s.life.plans.map{if(it.id==id)change(it).copy(revision=it.revision+1)else it}))}}
 val request=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){granted->if(granted)edit{it.copy(remind=true)}else permissionDenied=true}
 if(plan==null||template==null){PageColumn{TopBar("Rutin",onBack);QuietText("Bu rutin artık ekli değil.")};return}
 PageColumn{
  TopBar(slotLabel(template.slot),onBack)
  Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){
   Icon(routineIcon(id),null,Modifier.size(35.dp),tint=Coral)
   Text(template.title,style=MaterialTheme.typography.headlineLarge,modifier=Modifier.weight(1f))
  }
  QuietText(template.summary)
  Tag(if(routineDue(plan,date))"BUGÜNÜN KÜÇÜK ADIMLARI" else "RUTİN ÖNİZLEMESİ")
  if(!routineDue(plan,date))QuietText("Bugün planlı değil. Günlerini aşağıdan düzenleyebilirsin.")
  template.steps.forEachIndexed{index,step->
   val status=routineStatus(life,id,step.id,date)
   Surface(color=if(status=="done")Mint else Track,shape=RoundedCornerShape(18.dp),modifier=Modifier.fillMaxWidth()){
    Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
     Row(verticalAlignment=Alignment.CenterVertically){
      Text("%02d".format(index+1),fontSize=11.sp,color=Coral)
      Text(step.title,Modifier.weight(1f).padding(horizontal=10.dp),fontWeight=FontWeight.Bold,fontSize=17.sp)
      IconButton(enabled=routineDue(plan,date),onClick={previousEntries=null;store.update{it.copy(life=recordRoutine(it.life,id,step.id,if(status=="done")null else "done",date))}}){Icon(if(status=="done")ArcIcons.Checked else ArcIcons.Circle,if(status=="done")"${step.title}: kaydı geri al" else "${step.title}: yaptım",tint=Coral)}
     }
     QuietText(step.detail)
     Row(verticalAlignment=Alignment.CenterVertically){
      Text(when(status){"done"->"Yaptım olarak kaydedildi";"skip"->"Bugün atlandı";else->""},Modifier.weight(1f),fontSize=12.sp,color=ArcMuted)
      TextButton(enabled=routineDue(plan,date),onClick={previousEntries=null;store.update{it.copy(life=recordRoutine(it.life,id,step.id,if(status=="skip")null else "skip",date))}}){Text(if(status=="skip")"Geri al" else "Bugün atla")}
     }
    }
   }
  }
  if(routineDue(plan,date)&&!routineRecorded(life,id,date))TextButton(onClick={
   val prefix="$date|$id|";val before=store.state.life.entries.filterKeys{it.startsWith(prefix)}
   if(store.update{it.copy(life=skipRoutine(it.life,id,date))})previousEntries=before
  }){Text("Kalan adımları bugün atla")}
  previousEntries?.let{before->TextButton(onClick={val prefix="$date|$id|";if(store.update{it.copy(life=it.life.copy(entries=it.life.entries.filterKeys{key->!key.startsWith(prefix)}+before))})previousEntries=null}){Text("Son toplu atlamayı geri al")}}
  ExpandSection("Günler ve saat",ArcIcons.Program){
   SettingToggle("Rutin açık","Kapatmak geçmişini silmez",plan.enabled){v->edit{it.copy(enabled=v)}}
   FlowRow(horizontalArrangement=Arrangement.spacedBy(5.dp)){
    listOf("Pt","Sa","Ça","Pe","Cu","Ct","Pa").forEachIndexed{index,label->val day=index+1
     FilterChip(selected=day in plan.days,onClick={if(day !in plan.days||plan.days.size>1)edit{it.copy(days=if(day in it.days)it.days-day else it.days+day)}},label={Text(label)})
    }
   }
   OutlinedButton(onClick={val t=LocalTime.parse(plan.time);TimePickerDialog(context,{_,h,m->edit{it.copy(time=LocalTime.of(h,m).toString())}},t.hour,t.minute,true).show()}){Text("Rutin saati: ${plan.time}")}
   SettingToggle("Hatırlat","Yalnız seçtiğin günlerde; kesin saatli alarm değil",plan.remind){v->
    if(!v)edit{it.copy(remind=false)}else if(Reminder.allowed(context))edit{it.copy(remind=true)}
    else if(Build.VERSION.SDK_INT>=33)request.launch(Manifest.permission.POST_NOTIFICATIONS)else permissionDenied=true
   }
   if(plan.remind&&(lifeQuiet(life,LocalTime.parse(plan.time))||isQuietTime(LocalTime.parse(plan.time),store.state.bed,store.state.wake)))QuietText("Bu saat sessiz aralığında. Bildirim gönderilmez; saati veya sessiz aralığı değiştirebilirsin.")
   if(permissionDenied||plan.remind&&!Reminder.allowed(context)){
    QuietText("Bildirim izni kapalı. Rutinlerin uygulamada çalışmaya devam eder.")
    TextButton(onClick={runCatching{context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE,context.packageName))}}){Text("Sistem bildirim ayarları")}
   }
  }
  TextButton(onClick={deleting=true}){Text("Rutini ve kayıtlarını kaldır",color=MaterialTheme.colorScheme.error)}
 }
 if(deleting)AlertDialog(onDismissRequest={deleting=false},title={Text("Bu rutin kaldırılsın mı?")},text={Text("Yalnız bu rutinin adım kayıtları silinir. Antrenman, uyku ve diğer rutinlerin korunur. Geri alınamaz.")},confirmButton={TextButton(onClick={if(store.update{it.copy(life=deleteRoutine(it.life,id))}){deleting=false;onBack()}}){Text("Bu rutini kaldır")}},dismissButton={TextButton(onClick={deleting=false}){Text("Vazgeç")}})
}

@Composable fun LifeNotificationSettings(store:Store){
 val life=store.state.life;val context=LocalContext.current
 QuietText("Uyku, antrenman ve rutinler ortak bütçeyi paylaşır: bildirimler arasında en az 3 saat. Uykuda ve aktif antrenmanda sessiz.")
 Choice("Günde en fazla 1",selected=life.budget==1){store.update{it.copy(life=it.life.copy(budget=1))}}
 Choice("Günde en fazla 2",selected=life.budget==2){store.update{it.copy(life=it.life.copy(budget=2))}}
 SettingToggle("Okul / iş sessizliği","Bu aralıkta hatırlatma gönderilmez",life.quietEnabled){v->store.update{it.copy(life=it.life.copy(quietEnabled=v))}}
 if(life.quietEnabled){
  listOf(true,false).forEach{start->val value=if(start)life.quietStart else life.quietEnd
   OutlinedButton(onClick={val t=LocalTime.parse(value);TimePickerDialog(context,{_,h,m->val selected=LocalTime.of(h,m).toString();store.update{it.copy(life=if(start)it.life.copy(quietStart=selected)else it.life.copy(quietEnd=selected))}},t.hour,t.minute,true).show()}){Text((if(start)"Başlangıç: " else "Bitiş: ")+value)}
  }
  if(life.quietStart==life.quietEnd)QuietText("Aynı başlangıç ve bitiş, ek sessiz aralık oluşturmaz.")
 }
 QuietText("Geciken hatırlatmalar birikmez. Uygulama tıbbi alarm değildir.")
}

@Composable private fun ReflectionDialog(store:Store,onClose:()->Unit){
 val week=reflectionWeek(LocalDate.now());val old=store.state.life.reflections[week]
 var load by rememberSaveable{mutableStateOf(old?.load?:"")};var next by rememberSaveable{mutableStateOf(old?.next?:"")}
 AlertDialog(onDismissRequest=onClose,title={Text("Bu hafta sana nasıl geldi?")},text={Column(Modifier.heightIn(max=420.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(8.dp)){
  QuietText("Küçük destek rutinlerini düşün. Sağlık puanı veya otomatik yük artışı yok.")
  listOf("light" to "Hafifti","right" to "Bana uygundu","heavy" to "Fazlaydı").forEach{(id,label)->Choice(label,selected=load==id){load=id}}
  Text("Gelecek hafta?",fontWeight=FontWeight.Bold)
  listOf("keep" to "Aynı devam","time" to "Saati gözden geçir","less" to "Biraz sadeleştir").forEach{(id,label)->Choice(label,selected=next==id){next=id}}
 }},confirmButton={TextButton(enabled=load.isNotEmpty()&&next.isNotEmpty(),onClick={if(store.update{it.copy(life=it.life.copy(reflections=it.life.reflections+(week to WeekReflection(load,next))))})onClose()}){Text("Değerlendirmeyi kaydet")}},dismissButton={TextButton(onClick=onClose){Text("Şimdi değil")}})
}
