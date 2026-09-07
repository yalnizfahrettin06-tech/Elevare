package com.elevare.active

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable fun PlanScreen(s:UserState,onWorkout:(String)->Unit){
 val week=weeklyProgram(s.answers(),s.gentle);val current=(journeyDay(s)-1)%7
 PageColumn{
  TopBar("Haftalık program")
  Text("Bu hafta,\nsenin ritminde.",style=MaterialTheme.typography.headlineLarge)
  QuietText(programReason(s.answers(),s.gentle))
  WeekStrip(week,current)
  ProgramList(week,current,onWorkout)
  QuietText("Koşu günleri arasında toparlan. İhtiyacın varsa bir gün daha dinlenebilirsin.")
 }
}

@Composable fun ProgressScreen(s:UserState,onSleep:()->Unit){
    val today=LocalDate.now()
    val activity=s.sessions.filter{it.type=="workout"}
    PageColumn{
        TopBar("İLERLEMEN")
        Text("BAŞKASIYLA DEĞİL.\nKENDİNLE.",style=MaterialTheme.typography.headlineLarge)
        Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){
            Metric("${activity.size}","tamamlanan seans",Lime,Modifier.weight(1f))
            Metric("${activity.sumOf{it.seconds}/60}","hareket dakikası",Mint,Modifier.weight(1f))
        }
        Surface(color=MaterialTheme.colorScheme.surface,shape=RoundedCornerShape(20.dp)){Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(18.dp)){
            BlockTitle("SON 7 GÜN")
            val values=(6 downTo 0).map{offset->val d=today.minusDays(offset.toLong()).toString();activity.filter{it.date==d}.sumOf{it.seconds}}
            Row(Modifier.fillMaxWidth().height(150.dp),horizontalArrangement=Arrangement.spacedBy(10.dp),verticalAlignment=Alignment.Bottom){values.forEachIndexed{i,v->Column(Modifier.weight(1f),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(8.dp)){Text("${v/60}",fontSize=11.sp);Box(Modifier.fillMaxWidth().height(if(v==0)5.dp else (v.toFloat()/values.maxOrNull()!!.coerceAtLeast(1)*100).dp).background(if(i==6)Blue else Mint,RoundedCornerShape(7.dp)));Text(today.minusDays((6-i).toLong()).dayOfMonth.toString(),fontSize=11.sp)}}
            }
            Text("Tamamlanan hareket seansları · dakika",fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)
        }}
        if(s.sessions.isEmpty())InfoCard("İlk seansınla başlayacak. Burada örnek veya uydurma ilerleme yok.",Icons.Rounded.Bolt)
        BlockTitle("KÜÇÜK KAZANIMLAR")
        listOf(Triple("İlk adım","Bir hareket seansını tamamla",activity.isNotEmpty()),Triple("Çeşitlilik iyi gelir","3 farklı antrenman dene",activity.map{it.title}.distinct().size>=3),Triple("Geceyi fark et","Bir uyku kaydı ekle",s.sleeps.isNotEmpty())).forEach{(title,desc,earned)->Surface(color=MaterialTheme.colorScheme.surface,shape=RoundedCornerShape(16.dp)){Row(Modifier.fillMaxWidth().padding(16.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(14.dp)){Box(Modifier.size(46.dp).background(if(earned)Lime else Lilac,RoundedCornerShape(12.dp)),contentAlignment=Alignment.Center){Icon(if(earned)Icons.Rounded.Stars else Icons.Rounded.Lock,null,tint=Ink)};Column(Modifier.weight(1f)){Text(title,fontWeight=FontWeight.Bold);Text(if(earned)"Kazandın · $desc" else desc,fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)}}}}
        BlockTitle("UYKU GÜNLÜĞÜ","Aç",onSleep)
        Text(if(s.sleeps.isEmpty())"Henüz uyku kaydı yok." else "${s.sleeps.size} gece kaydı · Son kayıt ${s.sleeps.maxBy{it.date}.date}",color=MaterialTheme.colorScheme.onSurfaceVariant)
        BlockTitle("SEANS GEÇMİŞİ")
        s.sessions.asReversed().take(30).forEach{log->Surface(color=MaterialTheme.colorScheme.surface,shape=RoundedCornerShape(14.dp)){Row(Modifier.padding(16.dp),horizontalArrangement=Arrangement.spacedBy(14.dp),verticalAlignment=Alignment.CenterVertically){Icon(if(log.type=="breath")Icons.Rounded.Air else Icons.Rounded.FitnessCenter,null,tint=Blue);Column(Modifier.weight(1f)){Text(log.title,fontWeight=FontWeight.Bold);Text("${log.date} · ${log.feeling}",fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)};Text(minutesText(log.seconds),fontSize=12.sp)}}}
        Text("Elle işaretlenen rutinler seans süresine eklenmez. Sayılar bir yarış veya sağlık puanı değildir.",fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
@Composable fun Metric(value:String,label:String,color:Color,modifier:Modifier){Surface(modifier=modifier,shape=RoundedCornerShape(20.dp),color=color){Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){Text(value,color=Ink,fontSize=38.sp,fontWeight=FontWeight.Black);Text(label,color=Ink,fontSize=12.sp)}}}

@Composable fun SleepScreen(store:Store,onBack:()->Unit,notify:(String)->Unit){
    val s=store.state;val context=LocalContext.current
    var date by rememberSaveable{mutableStateOf(LocalDate.now().toString())}
    var bed by rememberSaveable{mutableStateOf(s.bed)}
    var wake by rememberSaveable{mutableStateOf(s.wake)}
    var delete by remember{mutableStateOf<SleepLog?>(null)}
    fun pickTime(value:String,change:(String)->Unit){val parts=value.split(":");TimePickerDialog(context,{_,h,m->change("%02d:%02d".format(h,m))},parts[0].toInt(),parts[1].toInt(),true).show()}
    val duration=sleepDuration(bed,wake)
    PageColumn{
        TopBar("UYKU GÜNLÜĞÜ",onBack)
        Tag("TOPARLANMAYA YER AÇ",Lilac)
        Text("GÜZEL BİR GÜN,\nİYİ BİR GECE.",style=MaterialTheme.typography.headlineLarge)
        InfoCard(sleepGuide(s.age)+" Bu günlük uykunu ölçmez veya tanı koymaz.",Icons.Rounded.Bedtime)
        OutlinedButton(onClick={val d=LocalDate.parse(date);DatePickerDialog(context,{_,y,m,day->date=LocalDate.of(y,m+1,day).toString()},d.year,d.monthValue-1,d.dayOfMonth).apply{datePicker.maxDate=System.currentTimeMillis()}.show()},modifier=Modifier.fillMaxWidth()){Icon(Icons.Rounded.CalendarMonth,null);Spacer(Modifier.width(10.dp));Text("Uyandığın tarih: $date")}
        Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){Surface(onClick={pickTime(bed){bed=it}},color=Lilac,shape=RoundedCornerShape(20.dp),modifier=Modifier.weight(1f)){Column(Modifier.padding(20.dp)){Text("YATIŞ",color=Ink,fontSize=12.sp);Text(bed,color=Ink,fontSize=30.sp,fontWeight=FontWeight.Black)}};Surface(onClick={pickTime(wake){wake=it}},color=Mint,shape=RoundedCornerShape(20.dp),modifier=Modifier.weight(1f)){Column(Modifier.padding(20.dp)){Text("KALKIŞ",color=Ink,fontSize=12.sp);Text(wake,color=Ink,fontSize=30.sp,fontWeight=FontWeight.Black)}}}
        Text(if(duration>0)"Yatakta geçen tahmini süre: ${duration/60} sa ${duration%60} dk" else "Yatış ve kalkış saatleri farklı olmalı.",fontWeight=FontWeight.Bold)
        Text("Gece yarısını geçen saatler ertesi gün olarak hesaplanır. Gece içindeki uyanıklık bu süreye dahildir.",fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)
        BigButton(if(s.sleeps.any{it.date==date})"BU GECEYİ GÜNCELLE" else "GECEYİ KAYDET",{store.update{val next=it.copy(sleeps=(it.sleeps.filterNot{l->l.date==date}+SleepLog(date,bed,wake)).sortedBy{l->l.date});if(date==LocalDate.now().toString())markDone(next,"sleep")else next};notify("Uyku kaydın kaydedildi.")},enabled=duration>0&&LocalDate.parse(date)<=LocalDate.now(),icon=Icons.Rounded.Check)
        OutlinedButton(onClick={store.update{it.copy(bed=bed,wake=wake)};notify("Uyku planın güncellendi.")},modifier=Modifier.fillMaxWidth(),enabled=duration>0){Text("Bu saatleri uyku planım yap")}
        BlockTitle("KAYITLI GECELER")
        if(s.sleeps.isEmpty())Text("Henüz kayıt yok. İlk geceni ekleyebilirsin.",color=MaterialTheme.colorScheme.onSurfaceVariant)
        s.sleeps.asReversed().forEach{log->Surface(color=MaterialTheme.colorScheme.surface,shape=RoundedCornerShape(16.dp)){Row(Modifier.padding(14.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f).clickable{date=log.date;bed=log.bed;wake=log.wake}){Text(log.date,fontWeight=FontWeight.Bold);Text("${log.bed} → ${log.wake} · ${log.minutes/60} sa ${log.minutes%60} dk",fontSize=13.sp)};IconButton(onClick={delete=log}){Icon(Icons.Rounded.DeleteOutline,"${log.date} uyku kaydını sil")}}}}
    }
    delete?.let{log->AlertDialog(onDismissRequest={delete=null},title={Text("Bu gece silinsin mi?")},text={Text("${log.date} tarihli uyku kaydı kaldırılacak.")},confirmButton={TextButton(onClick={store.update{it.copy(sleeps=it.sleeps.filterNot{l->l.date==log.date})};delete=null;notify("Uyku kaydı silindi.")}){Text("Sil")}},dismissButton={TextButton(onClick={delete=null}){Text("Vazgeç")}})}
}

@Composable fun ProfileScreen(store:Store,onSources:()->Unit,onSleep:()->Unit,onExport:()->Unit,onDelete:()->Unit,notify:(String)->Unit){
    val s=store.state
    var name by rememberSaveable{mutableStateOf(s.name)}
    var pause by remember{mutableStateOf(false)}
    PageColumn{
        TopBar("Profil")
        GrowthProfileHeader(store,notify)
        OutlinedTextField(value=name,onValueChange={name=it.take(18)},label={Text("İsim veya takma ad")},singleLine=true,modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(14.dp),trailingIcon={IconButton(onClick={store.update{it.copy(name=name.trim())};notify("İsmin güncellendi.")}){Icon(Icons.Rounded.Check,"İsmi kaydet")}})
        BlockTitle("Görünüm ve tercihler")
        SettingToggle("Koyu görünüm","Daha loş bir arayüz",s.dark){v->store.update{it.copy(dark=v)}}
        SettingToggle("Hareketleri azalt","Nefes animasyonunu sabitle",s.reducedMotion){v->store.update{it.copy(reducedMotion=v)}}
        SettingToggle("Dokunma hissi","Sekmelerde hafif titreşim",s.haptic){v->store.update{it.copy(haptic=v)}}
        SettingToggle("Daha hafif öneri","Ana ekranda mobilite akışı",s.gentle){v->store.update{it.copy(gentle=v)}}
        BlockTitle("RUTİNİN")
        InfoCard("Uyku planın · ${s.bed} → ${s.wake}",Icons.Rounded.Bedtime,onSleep)
        BigButton(if(s.pausedOn==null)"PLANA ARA VER" else "PLANA DEVAM ET",{pause=true},icon=if(s.pausedOn==null)Icons.Rounded.Pause else Icons.Rounded.PlayArrow)
        Text("Ara verdiğinde 90 günlük takvim durur. Geçmiş kayıtların korunur; geri döndüğünde telafi borcun olmaz.",fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)
        BlockTitle("GİZLİLİK VE BİLGİ")
        InfoCard("Bilimsel kaynaklar ve uygulama sınırları",Icons.Rounded.MenuBook,onSources)
        InfoCard("Kayıtlarımı dosya olarak kaydet",Icons.Rounded.Download,onExport)
        InfoCard("Hesap, reklam ve takip yok. Kayıtların yalnızca cihazında. Uygulamayı kaldırmak kayıtlarını siler.",Icons.Rounded.Lock)
        OutlinedButton(onClick=onDelete,modifier=Modifier.fillMaxWidth()){Text("Bu cihazdaki tüm kayıtları sil",color=MaterialTheme.colorScheme.error)}
        Text("ELEVARE / 0.3.0\nTest imzalı prototip · Yerel uyku hatırlatıcısı\nFirebase, ödeme ve sensör ölçümü bağlı değil.",fontSize=12.sp,lineHeight=19.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)
    }
    if(pause)AlertDialog(onDismissRequest={pause=false},title={Text(if(s.pausedOn==null)"Dinlenme zamanı mı?" else "Devam edelim mi?")},text={Text(if(s.pausedOn==null)"Takvim duracak; açık seansın duraklatılacak. İstediğin zaman devam edebilirsin." else "Takvimin kaldığı yerden devam edecek.")},confirmButton={TextButton(onClick={store.pauseTimer();store.update{if(it.pausedOn==null)it.copy(pausedOn=LocalDate.now().toString())else it.copy(pausedDays=it.pausedDays+ChronoUnit.DAYS.between(LocalDate.parse(it.pausedOn),LocalDate.now()).coerceAtLeast(0),pausedOn=null)};pause=false}){Text(if(s.pausedOn==null)"Ara ver" else "Devam et")}},dismissButton={TextButton(onClick={pause=false}){Text("Vazgeç")}})
}
@Composable fun SettingToggle(title:String,description:String,checked:Boolean,change:(Boolean)->Unit){Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(16.dp)){Column(Modifier.weight(1f)){Text(title,fontWeight=FontWeight.Medium,fontSize=15.sp);if(description.isNotBlank())Text(description,fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)};Switch(checked=checked,onCheckedChange=change)}}

@Composable fun SourcesScreen(onBack:()->Unit,onLink:(String)->Unit){PageColumn{
    InfoCard("Elevare tıbbi cihaz değildir. Hastalık tanısı, tedavisi, önlenmesi veya hormon ölçümü yapmaz. Sağlık kararları için bir sağlık uzmanına danış.")
    TopBar("BİLGİ VE KAYNAKLAR",onBack)
    Text("HAREKET İÇİN.\nMUCİZE VAADİ DEĞİL.",style=MaterialTheme.typography.headlineLarge)
    Text("Elevare, 13–21 yaş için genel hareket ve rutin farkındalığı prototipidir. Boy uzaması, büyüme hormonu artışı, kilo değişimi veya tıbbi sonuç vaat etmez. Kişisel egzersiz reçetesi değildir.")
    InfoCard("Dünya Sağlık Örgütü · 5–17 yaş için hafta boyunca günde ortalama en az 60 dakika orta-yüksek şiddette, çoğunlukla aerobik hareket önerir. Bu kısa seanslar günlük hareketin tamamı değildir.",Icons.Rounded.Public){onLink("https://www.who.int/publications/i/item/9789240014886")}
    InfoCard("CDC · Oyun, yürüyüş ve spor dahil yaşa uygun, çeşitli ve keyifli hareket seçenekleri.",Icons.Rounded.SportsSoccer){onLink("https://www.cdc.gov/physical-activity-basics/adding-children-adolescents/what-counts.html")}
    InfoCard("Yaşa göre uyku: ergenler 8–10, genç yetişkinler 7–9 saat. Genel uzlaşı aralıklarıdır.",Icons.Rounded.Bedtime){onLink("https://pubmed.ncbi.nlm.nih.gov/29073398/")}
    Text("Hareket içerikleri bu prototip için hazırlanmış düşük zorlayıcılıktaki örneklerdir; bu kuruluşların onayladığı bir program değildir. Ağrı, baş dönmesi veya rahatsızlıkta dur; gerekiyorsa destek al. Sağlık durumuna özel uygunluğu sağlık uzmanıyla değerlendir.",fontSize=14.sp,lineHeight=22.sp)
    Text("Hareket figürleri çizgilerden oluşan şematik gösterimlerdir; profesyonel teknik kontrolün yerine geçmez. Yeni bilgi kartları: 7 Eylül 2026.",fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)
}}
