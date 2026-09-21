package com.elevare.active

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import org.json.JSONObject
import java.time.LocalDate

private fun loadScienceCatalog(context:Context):ScienceCatalog = runCatching {
   val root=JSONObject(context.assets.open("science-cards.json").bufferedReader().use{it.readText()})
   val ss=root.getJSONArray("sources");val ff=root.getJSONArray("facts")
   val sources=(0 until ss.length()).map{ss.getJSONObject(it)}.map{
    ScienceSource(it.getString("id"),it.getString("title"),it.optString("pmid"),it.getString("url"),it.getString("studyType"),it.getString("population"),it.getString("summary"),it.getString("limit"),it.optString("checked"))
   }
   val facts=(0 until ff.length()).map{ff.getJSONObject(it)}.map{
    ScienceFact(it.getString("id"),it.getString("sourceId"),it.getString("text"),it.getString("category"),it.getInt("minAge"),it.getInt("maxAge"),it.getString("kind"),it.optInt("version",root.optInt("version",1)),it.optString("status","legacy_source_summary"),it.optString("reviewer"),it.optString("reviewedAt"),it.optString("nextReviewAt"))
   }
   require(facts.isNotEmpty() && facts.map{it.id}.distinct().size==facts.size)
   require(sources.map{it.id}.distinct().size==sources.size)
   require(sources.all{it.url.startsWith("https://") && it.title.isNotBlank() && it.limit.isNotBlank()})
   require(facts.all{fact->fact.text.isNotBlank() && fact.minAge<=fact.maxAge && sources.any{it.id==fact.sourceId}})
   ScienceCatalog(sources,facts)
  }.getOrElse{ScienceCatalog(emptyList(),emptyList())}

@Composable fun TodayScience(s:UserState,onFact:(String)->Unit,store:Store?=null){
 val context=LocalContext.current
 val catalog=remember(context){loadScienceCatalog(context)}
 var fallback by remember{mutableStateOf(s)}
 val state=store?.state?:fallback
 val today=LocalDate.now()
 val fact=selectDailyFact(catalog,state,today)
 if(fact==null){QuietText("Bilgi kartları yüklenemedi. Antrenmanını sürdürebilirsin.");return}
 LaunchedEffect(fact.id,today){
  if(store!=null)store.update{recordFactView(it,fact,today)} else fallback=recordFactView(fallback,fact,today)
 }
 Surface(onClick={onFact(fact.id)},shape=RoundedCornerShape(16.dp),color=MaterialTheme.colorScheme.surface,border=BorderStroke(1.dp,MaterialTheme.colorScheme.outlineVariant),modifier=Modifier.fillMaxWidth()){
  Column(Modifier.padding(horizontal=14.dp).padding(bottom=14.dp),verticalArrangement=Arrangement.spacedBy(4.dp)){
   Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){
    Icon(ArcIcons.Book,null,tint=Sky,modifier=Modifier.size(19.dp))
    Text("SAHA NOTU",Modifier.weight(1f).padding(start=8.dp),fontSize=10.sp,letterSpacing=1.5.sp,color=Sky,fontWeight=FontWeight.Bold)
    IconButton(onClick={
     val next=selectDailyFact(catalog,state,today,true)
     if(next!=null){
      val repeated=factSeenRecently(state,next.id,today)
      if(store!=null)store.update{recordFactView(it,next,today)} else fallback=recordFactView(fallback,next,today)
      if(repeated)Toast.makeText(context,"Son günlerdeki notlara yeniden bakıyorsun.",Toast.LENGTH_SHORT).show()
     }
    }){Icon(ArcIcons.Refresh,"Başka bilgi göster",Modifier.size(21.dp))}
   }
   Text(fact.text,fontSize=15.sp,lineHeight=21.sp,fontWeight=FontWeight.Medium)
   Row(verticalAlignment=Alignment.CenterVertically){Text("Kaynak ve ayrıntı",Modifier.weight(1f),fontSize=13.sp,color=MaterialTheme.colorScheme.primary);Icon(ArcIcons.Chevron,null)}
  }
 }
}
@Composable fun FactLibrary(s:UserState,onFact:(String)->Unit,store:Store?=null,onProfile:()->Unit={}){
 val context=LocalContext.current
 val catalog=remember(context){loadScienceCatalog(context)}
 val state=store?.state?:s
 var category by rememberSaveable{mutableStateOf("Tümü")}
 val available=catalog.forAge(state.age)
 val visible=available.filter{category=="Tümü" || (category=="Kaydedilenler" && it.id in state.savedFacts) || it.category==category}
 LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
  item{TopBar("Rehber",action={IconButton(onClick=onProfile){Icon(ArcIcons.Person,"Profil")}})}
  item{ArcSectionLead("MERAKINI BESLE","Hareketin ötesinde.")}
  item{QuietText("${available.size} kısa not · ${available.map{it.sourceId}.distinct().size} kaynak")}
  item{
   Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)){
    (listOf("Tümü","Kaydedilenler")+available.map{it.category}.distinct()).forEach{c->FilterChip(selected=category==c,onClick={category=c},label={Text(c)})}
   }
  }
  if(available.isEmpty())item{QuietText("Bilgi dosyası yüklenemedi. Antrenmanlar çevrimdışı kullanılabilir.")}
  else if(visible.isEmpty())item{QuietText("Henüz kaydedilmiş bir not yok. Notun içindeki kaydet simgesini kullanabilirsin.")}
  items(visible,key={it.id}){fact->
   Surface(onClick={onFact(fact.id)},shape=RoundedCornerShape(14.dp),color=MaterialTheme.colorScheme.surface,modifier=Modifier.fillMaxWidth()){
    Row(Modifier.padding(start=16.dp,top=12.dp,bottom=12.dp,end=4.dp),verticalAlignment=Alignment.CenterVertically){
     Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(8.dp)){Eyebrow(fact.category);Text(fact.text,fontSize=15.sp,lineHeight=22.sp)}
     IconButton(onClick={store?.update{it.copy(savedFacts=if(fact.id in it.savedFacts)it.savedFacts-fact.id else it.savedFacts+fact.id)}},enabled=store!=null){
      Icon(if(fact.id in state.savedFacts)ArcIcons.Saved else ArcIcons.Bookmark,if(fact.id in state.savedFacts)"Kaydedilenlerden çıkar" else "Bilgiyi kaydet",tint=Sky)
     }
    }
   }
  }
 }
}
@Composable fun FactDetail(id:String,age:Int,onBack:()->Unit,store:Store?=null){
 val context=LocalContext.current
 val catalog=remember(context){loadScienceCatalog(context)}
 val fact=catalog.forAge(age).find{it.id==id}
 if(fact==null){PageColumn{TopBar("Bilgi",onBack);QuietText("Bu kart bulunamadı, geri çekildi veya yaş grubuna uygun değil.")};return}
 val source=catalog.source(fact)
 val saved=store?.state?.savedFacts?.contains(id)==true
 LaunchedEffect(id){store?.update{recordFactView(it,fact,daily=false)}}
 PageColumn{
  Row(verticalAlignment=Alignment.CenterVertically){
   Box(Modifier.weight(1f)){TopBar("Araştırma",onBack)}
   IconButton(onClick={store?.update{it.copy(savedFacts=if(saved)it.savedFacts-id else it.savedFacts+id)}},enabled=store!=null){
    Icon(if(saved)ArcIcons.Saved else ArcIcons.Bookmark,if(saved)"Kaydedilenlerden çıkar" else "Bilgiyi kaydet",tint=Sky)
   }
  }
  Text(fact.text,fontSize=23.sp,lineHeight=30.sp,fontWeight=FontWeight.Bold,modifier=Modifier.semantics{heading()})
  Tag(fact.kind)
  BlockTitle("Ne incelendi?");Text(source.title,fontSize=15.sp)
  QuietText(source.type+" · "+source.population)
  BlockTitle("Ne bulundu?");Text(source.summary,style=MaterialTheme.typography.bodyLarge)
  BlockTitle("Neyi göstermez?");Text(source.limit,style=MaterialTheme.typography.bodyLarge)
  if(source.pmid.isNotEmpty())Tag("PMID "+source.pmid)
  BigButton("Kaynağı aç",{
   try{context.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(source.url)))}catch(_:Exception){Toast.makeText(context,"Bağlantıyı açacak tarayıcı bulunamadı.",Toast.LENGTH_SHORT).show()}
  },icon=ArcIcons.External)
  QuietText("Özet çevrimdışı okunabilir. Dış kaynak için internet gerekir.")
  QuietText("Kart sürümü ${fact.version} · Editoryal kısa özet; kişisel tıbbi tavsiye değildir.")
  if(source.checkedAt.isNotBlank())QuietText("Kaynak dosyasındaki kontrol tarihi: ${source.checkedAt}.")
  if(fact.reviewer.isBlank())QuietText("Bağımsız uzman incelemesi kaydı bulunmuyor.")
  else QuietText("İnceleyen: ${fact.reviewer} · ${fact.reviewedAt}")
 }
}
