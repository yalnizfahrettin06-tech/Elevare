package com.elevare.active

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class ScienceSource(val id:String,val title:String,val pmid:String,val url:String,val type:String,val population:String,val summary:String,val limit:String,val checkedAt:String="")
data class ScienceFact(val id:String,val sourceId:String,val text:String,val category:String,val minAge:Int,val maxAge:Int,val kind:String,val version:Int=1,val status:String="legacy_source_summary",val reviewer:String="",val reviewedAt:String="",val nextReviewAt:String="")
data class ScienceCatalog(val sources:List<ScienceSource>,val facts:List<ScienceFact>){
 fun forAge(age:Int)=facts.filter{age in it.minAge..it.maxAge && it.status in setOf("legacy_source_summary","publishable","published")}
 fun source(fact:ScienceFact)=sources.first{it.id==fact.sourceId}
}

fun orderedFacts(catalog:ScienceCatalog,s:UserState):List<ScienceFact>{
 val first=when(s.focus){
  "performance"->setOf("strength","sprint","youth")
  "sleep","recovery"->setOf("teen-sleep","adult-sleep","breath")
  "growth"->setOf("growth","milk")
  else->setOf("youth","sprint","strength")
 }
 return catalog.forAge(s.age).sortedWith(compareBy<ScienceFact>{if(it.sourceId in first)0 else 1}.thenBy{it.id})
}
fun factSeenRecently(s:UserState,id:String,today:LocalDate):Boolean {
 val date=runCatching{LocalDate.parse(s.factSeenAt[id]?:return false)}.getOrNull()?:return false
 return ChronoUnit.DAYS.between(date,today) in 0..6
}
/** Stable per local date; a manual change affects knowledge only, never the workout. */
fun selectDailyFact(catalog:ScienceCatalog,s:UserState,today:LocalDate=LocalDate.now(),change:Boolean=false):ScienceFact? {
 val ordered=orderedFacts(catalog,s)
 if(ordered.isEmpty())return null
 val current=s.factHistory[today.toString()]
 if(!change)ordered.find{it.id==current}?.let{return it}
 val unseen=ordered.filter{!factSeenRecently(s,it.id,today) && (!change || it.id!=current)}
 var candidates=unseen.ifEmpty{ordered.filter{!change || it.id!=current}.ifEmpty{ordered}}
 val lastSources=s.recentFactIds.takeLast(2).mapNotNull{id->catalog.facts.find{it.id==id}?.sourceId}
 if(lastSources.size==2 && lastSources.distinct().size==1){
  candidates=candidates.filter{it.sourceId!=lastSources.first()}.ifEmpty{candidates}
 }
 // Spread practical material during the first week and recovery material on rest days.
 val practical=if(journeyDay(s,today)<=7)setOf("sprint","youth","strength","who")
  else if(runCatching{todayProgram(s,today).training}.getOrDefault(false))setOf("sprint","strength","teen-sleep","adult-sleep")
  else setOf("teen-sleep","adult-sleep","breath","who")
 val preferred=if(s.focus=="growth")candidates else candidates.filter{it.sourceId in practical}.ifEmpty{candidates}
 val pool=if(unseen.isEmpty())preferred.sortedBy{s.factSeenAt[it.id]?:""} else preferred
 return if(unseen.isEmpty())pool.first() else pool[Math.floorMod(today.toEpochDay(),pool.size.toLong()).toInt()]
}
fun recordFactView(s:UserState,fact:ScienceFact,today:LocalDate=LocalDate.now(),daily:Boolean=true):UserState {
 val key=today.toString()
 if(s.factSeenAt[fact.id]==key && (!daily || s.factHistory[key]==fact.id))return s
 val history=if(daily)s.factHistory+(key to fact.id) else s.factHistory
 val trimmed=history.filter{(date,_)->runCatching{ChronoUnit.DAYS.between(LocalDate.parse(date),today) in 0..89}.getOrDefault(false)}
 return s.copy(factHistory=trimmed,factSeenAt=s.factSeenAt+(fact.id to key),recentFactIds=(s.recentFactIds+fact.id).takeLast(2))
}
