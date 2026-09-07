package com.elevare.active

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import org.json.JSONObject
import java.time.LocalDate

data class ScienceSource(val id:String,val title:String,val pmid:String,val url:String,val type:String,val population:String,val summary:String,val limit:String)
data class ScienceFact(val id:String,val sourceId:String,val text:String,val category:String,val minAge:Int,val maxAge:Int,val kind:String)
data class ScienceCatalog(val sources:List<ScienceSource>,val facts:List<ScienceFact>){
 fun forAge(age:Int)=facts.filter{age in it.minAge..it.maxAge}
 fun source(fact:ScienceFact)=sources.first{it.id==fact.sourceId}
 companion object{
  fun load(context:Context):ScienceCatalog {
   return runCatching {
    val root=JSONObject(context.assets.open("science-cards.json").bufferedReader().use{it.readText()})
    val ss=root.getJSONArray("sources");val ff=root.getJSONArray("facts")
    val sources=(0 until ss.length()).map{ss.getJSONObject(it)}.map{ScienceSource(it.getString("id"),it.getString("title"),it.getString("pmid"),it.getString("url"),it.getString("studyType"),it.getString("population"),it.getString("summary"),it.getString("limit"))}
    val facts=(0 until ff.length()).map{ff.getJSONObject(it)}.map{ScienceFact(it.getString("id"),it.getString("sourceId"),it.getString("text"),it.getString("category"),it.getInt("minAge"),it.getInt("maxAge"),it.getString("kind"))}
    require(facts.size==60&&facts.map{it.id}.distinct().size==60)
    require(facts.all{fact->sources.any{it.id==fact.sourceId}})
    ScienceCatalog(sources,facts)
   }.getOrElse{ScienceCatalog(emptyList(),emptyList())}
  }
 }
}
fun orderedFacts(catalog:ScienceCatalog,s:UserState):List<ScienceFact>{
 val first=when(s.focus){"performance"->setOf("strength","sprint","youth");"sleep","recovery"->setOf("teen-sleep","adult-sleep","breath");else->if(s.recentGrowth=="unknown")setOf("growth")else setOf("growth","milk")}
 return catalog.forAge(s.age).sortedBy{if(it.sourceId in first)0 else 1}
}
@Composable fun TodayScience(s:UserState,onFact:(String)->Unit){
 val context=LocalContext.current
 val catalog=remember(context){ScienceCatalog.load(context)}
 var extra by rememberSaveable{mutableIntStateOf(0)}
 val facts=remember(catalog,s.age,s.focus,s.recentGrowth){orderedFacts(catalog,s).filter{it.sourceId in setOf("sprint","strength","youth","milk","teen-sleep","adult-sleep","breath")}.ifEmpty{orderedFacts(catalog,s)}}
 if(facts.isEmpty()){QuietText("Bilgi kartları yüklenemedi.");return}
 val fact=facts[Math.floorMod(LocalDate.now().toEpochDay()+extra,facts.size.toLong()).toInt()]
 Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Text("Bugün bir bilgi",Modifier.weight(1f),fontWeight=FontWeight.SemiBold);TextButton(onClick={extra=(extra+1)%facts.size}){Icon(Icons.Rounded.Refresh,"Başka bilgi",Modifier.size(20.dp))}}
 Surface(onClick={onFact(fact.id)},shape=RoundedCornerShape(16.dp),color=MaterialTheme.colorScheme.surface,border=BorderStroke(1.dp,MaterialTheme.colorScheme.outlineVariant),modifier=Modifier.fillMaxWidth()){
  Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
   Eyebrow(fact.category)
   Text(fact.text,fontSize=14.sp,lineHeight=20.sp,fontWeight=FontWeight.Medium)
   Row(verticalAlignment=Alignment.CenterVertically){Text("Bilimsel kaynağı gör",Modifier.weight(1f),fontSize=12.sp,color=MaterialTheme.colorScheme.primary);Icon(Icons.Rounded.ChevronRight,null)}
  }
 }
}
@Composable fun FactLibrary(s:UserState,onFact:(String)->Unit){
 val context=LocalContext.current;val catalog=remember(context){ScienceCatalog.load(context)}
 var category by rememberSaveable{mutableStateOf("Tümü")}
 PageColumn{
  TopBar("Bilgi")
  QuietText("60 kısa not · 12 kaynak · Yaşına uygun içerikler")
  Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)){
   (listOf("Tümü")+catalog.forAge(s.age).map{it.category}.distinct()).forEach{c->FilterChip(selected=category==c,onClick={category=c},label={Text(c)})}
  }
  if(catalog.facts.isEmpty())QuietText("Bilgi dosyası yüklenemedi.")
  catalog.forAge(s.age).filter{category=="Tümü"||it.category==category}.forEach{fact->
   Surface(onClick={onFact(fact.id)},shape=RoundedCornerShape(14.dp),color=MaterialTheme.colorScheme.surface,modifier=Modifier.fillMaxWidth()){
    Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){Eyebrow(fact.category);Text(fact.text,fontSize=15.sp,lineHeight=22.sp)}
   }
  }
 }
}
@Composable fun FactDetail(id:String,age:Int,onBack:()->Unit){
 val context=LocalContext.current;val catalog=remember(context){ScienceCatalog.load(context)}
 val fact=catalog.forAge(age).find{it.id==id}
 if(fact==null){PageColumn{TopBar("Bilgi",onBack);QuietText("Bu kart bulunamadı veya yaş grubuna uygun değil.")};return}
 val source=catalog.source(fact)
 PageColumn{
  TopBar("Araştırma",onBack)
  Text(fact.text,fontSize=23.sp,lineHeight=30.sp,fontWeight=FontWeight.Bold)
  Tag(fact.kind)
  Text(source.population,fontSize=14.sp)
  BlockTitle("Ne bulundu?");Text(source.summary,style=MaterialTheme.typography.bodyLarge)
  BlockTitle("Neyi göstermez?");Text(source.limit,style=MaterialTheme.typography.bodyLarge)
  QuietText(source.title+" · "+source.type)
  if(source.pmid.isNotEmpty())Tag("PMID "+source.pmid)
  BigButton("Kaynağı aç",{
   try{context.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(source.url)))}catch(_:Exception){Toast.makeText(context,"Bağlantıyı açacak tarayıcı bulunamadı.",Toast.LENGTH_SHORT).show()}
  },icon=Icons.Rounded.OpenInNew)
  QuietText("7 Eylül 2026'da incelendi. Kısa editoryal özet; kişisel tıbbi tavsiye değildir.")
 }
}
