package com.elevare.active

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate

@Composable fun WeeklyActionCard(store:Store,onRoutine:(String)->Unit){
 val suggestion=routineSuggestion(store.state.life,LocalDate.now())?:return
 var confirm by remember{mutableStateOf(false)}
 Surface(color=Track,shape=RoundedCornerShape(16.dp)){
  Column(Modifier.fillMaxWidth().padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
   Text("Küçük bir düzenleme?",fontWeight=FontWeight.Bold)
   QuietText("${suggestion.title}: son 7 günün ${suggestion.skippedDays} gününde en az bir adımı atladığını kaydettin. Kayıtsız günleri saymadık.")
   TextButton(onClick={onRoutine(suggestion.id)}){Text("Saatini gözden geçir")}
   TextButton(onClick={confirm=true}){Text("Bu rutine ara ver")}
  }
 }
 if(confirm)AlertDialog(onDismissRequest={confirm=false},title={Text("Yalnız bu rutine ara verilsin mi?")},text={Text("Geçmişi korunur, yeni hatırlatmaları kapanır. Rutin ayrıntısından yeniden açabilirsin. Antrenman planın değişmez.")},confirmButton={TextButton(onClick={if(store.update{s->s.copy(life=s.life.copy(plans=s.life.plans.map{if(it.id==suggestion.id)it.copy(enabled=false,revision=it.revision+1)else it}))})confirm=false}){Text("Rutine ara ver")}},dismissButton={TextButton(onClick={confirm=false}){Text("Vazgeç")}})
}

@Composable fun ProExperienceScreen(store:Store,onBack:()->Unit,onRoutine:(String)->Unit,onStudio:()->Unit={}){
 val s=store.state
 var minutes by rememberSaveable{mutableIntStateOf(s.dailyMinutes)}
 var confirm by remember{mutableStateOf(false)}
 var saved by remember{mutableStateOf(false)}
 val preview=remember(s,minutes){nextTrainingPreview(applyTimePreference(s,minutes),LocalDate.now())}
 PageColumn{
  TopBar("Pro önizlemesi",onBack)
  Text("Planın hayatına uysun.",style=MaterialTheme.typography.headlineMedium)
  BigButton("Haftamı yeniden düzenle",onStudio,icon=ArcIcons.Program)
  QuietText("Gün, süre ve alan tercihlerini birlikte önizle. Değişiklikler yalnız onayınla uygulanır.")
  QuietText("Ücretsiz önizleme. Ödeme ve otomatik yenileme yok.")
  Surface(color=Mint,shape=RoundedCornerShape(18.dp)){
   Column(Modifier.fillMaxWidth().padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
    Eyebrow("DENE · PLANINI UYARLA")
    Text("Antrenmana ayıracağın süre değişti mi?",fontWeight=FontWeight.Bold)
    ProfileChoices.minutes.chunked(2).forEach{pair->
     Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
      pair.forEach{value->FilterChip(selected=minutes==value,onClick={minutes=value;saved=false},label={Text("$value dk")},
       leadingIcon=if(minutes==value){{Icon(ArcIcons.Check,"Seçili",Modifier.size(18.dp))}}else null,
       colors=FilterChipDefaults.filterChipColors(selectedContainerColor=Coral,selectedLabelColor=Paper,selectedLeadingIconColor=Paper),modifier=Modifier.weight(1f).heightIn(min=48.dp))}
     }
    }
    if(s.active!=null)QuietText("Açık seansın korunuyor. Süreyi değiştirmeden önce seansını bitir veya bırak.")
    else preview?.let{p->
     Text("${p.day}. gün · ${p.workout.title} · ${minutesText(p.workout.seconds)}",fontWeight=FontWeight.Bold)
     QuietText("Sıradaki planlı antrenmanın önizlemesi. Dinlenme günlerin ve geçmişin korunur.")
    }
    if(s.active==null&&preview==null)QuietText(if(s.pausedOn!=null)"Planın duraklatılmış. Önce ayarlardan planına devam et." else "Bu döngüde kalan antrenman yok. Yeni döngünü başlattığında süreyi uyarlayabilirsin.")
    BigButton(if(saved)"Tercihin kaydedildi" else "Değişikliği incele",{confirm=true},enabled=s.active==null&&minutes!=s.dailyMinutes&&preview!=null)
   }
  }
  BlockTitle("Kendi kayıtlarından")
  Text("Son 7 gün · ${lastSevenWorkouts(s)} antrenman · ${lifeWeekCount(s.life,LocalDate.now())} rutin adımı")
  QuietText("Bunlar kayıt özeti; sağlık, hormon veya performans ölçümü değil.")
  WeeklyActionCard(store,onRoutine)
  if(routineSuggestion(s.life,LocalDate.now())==null)QuietText("Rutin kayıtların biriktikçe burada düzenleme önerileri görebilirsin.")
  ExpandSection("Ücretsiz temel deneyim",ArcIcons.Shield){
   Text("Başlangıç programı, temel hareket açıklamaları, güvenlik yönlendirmeleri, rutinler, bildirim tercihleri, kendi kayıtların ve dışa aktarma.")
  }
  ExpandSection("Henüz satışa sunulmayanlar",ArcIcons.Info){
   Text("Uzman onaylı ek programlar, profesyonel ses paketleri ve gelişmiş haftalık yeniden planlama henüz hazır değil. Bunlar mevcut Pro faydası olarak vaat edilmez.")
  }
  ExpandSection("3 günlük demo ne anlama geliyor?",ArcIcons.Clock){
   QuietText("Demo ödeme başlatmaz ve süresi dolunca antrenmanlarını kilitlemez. Üç gün içinde bir seansı, plan uyarlamasını ve kayıt özetini deneyimleyebilirsin; fazladan antrenman gerekmez.")
   val remaining=demoRemainingMillis(s)
   Text(if(s.demoStartedAt<=0)"Demo henüz başlamadı." else if(remaining>0)"Yaklaşık ${(remaining+3599999)/3600000} saat kaldı." else "Demo süren tamamlandı; kayıtların açık.")
   if(s.demoStartedAt<=0&&trainingAllowed(s))TextButton(onClick={store.update{startDemo(it)}}){Text("3 günlük demoyu başlat")}
  }
 }
 if(confirm)AlertDialog(onDismissRequest={confirm=false},title={Text("Yeni süre tercihi: $minutes dk")},text={Text("Yalnız bugünü değil, sonraki antrenman planlarını da etkiler. Başlangıç tarihin ve kayıtların korunur; ilerleme için önceki seviye onayları yeniden değerlendirilir. Uzman onay sınırları değişmez.")},confirmButton={TextButton(onClick={if(store.update{applyTimePreference(it,minutes)}){confirm=false;saved=true}}){Text("Tercihi uygula")}},dismissButton={TextButton(onClick={confirm=false}){Text("Vazgeç")}})
}
