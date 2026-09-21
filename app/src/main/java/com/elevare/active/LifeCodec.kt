package com.elevare.active

import org.json.*
import java.time.*

object LifeCodec {
 fun encode(l:LifeState)=JSONObject().apply{
  put("welcomed",l.welcomed);put("quietEnabled",l.quietEnabled);put("quietStart",l.quietStart);put("quietEnd",l.quietEnd);put("budget",l.budget)
  put("plans",JSONArray().apply{l.plans.forEach{p->put(JSONObject().apply{put("id",p.id);put("enabled",p.enabled);put("days",JSONArray(p.days.sorted()));put("time",p.time);put("remind",p.remind);put("revision",p.revision)})}})
  put("entries",JSONObject(l.entries));put("notificationsMuted",l.notificationsMuted);put("notes",JSONObject(l.notes))
  put("reflections",JSONObject().apply{l.reflections.forEach{(week,r)->put(week,JSONObject().apply{put("load",r.load);put("next",r.next)})}})
 }
 fun decode(j:JSONObject?):LifeState {
  if(j==null)return LifeState()
  require(!j.has("plans")||j.optJSONArray("plans")!=null)
  require(!j.has("entries")||j.optJSONObject("entries")!=null)
  require(!j.has("reflections")||j.optJSONObject("reflections")!=null)
  val a=j.optJSONArray("plans")?:JSONArray()
  val plans=(0 until a.length()).map{i->val p=a.getJSONObject(i);val days=p.getJSONArray("days")
   LifeRoutinePlan(p.getString("id"),p.getBoolean("enabled"),(0 until days.length()).map{days.getInt(it)}.toSet(),LocalTime.parse(p.getString("time")).toString(),p.getBoolean("remind"),p.getLong("revision")).also{
    require(LifeCatalog.find(it.id)!=null&&it.days.isNotEmpty()&&it.days.all{d->d in 1..7}&&it.revision>0)
   }
  }
  require(plans.map{it.id}.distinct().size==plans.size)
  val obj=j.optJSONObject("entries")?:JSONObject()
  val entries=obj.keys().asSequence().associateWith{key->
   val parts=key.split('|');require(parts.size==3);LocalDate.parse(parts[0])
   require(LifeCatalog.find(parts[1])?.steps?.any{it.id==parts[2]}==true)
   obj.getString(key).also{require(it in setOf("done","skip"))}
  }
  val review=j.optJSONObject("reflections")?:JSONObject()
  val reflections=review.keys().asSequence().associateWith{key->LocalDate.parse(key);val r=review.getJSONObject(key)
   WeekReflection(r.getString("load"),r.getString("next")).also{require(it.load in setOf("light","right","heavy")&&it.next in setOf("keep","time","less"))}
  }
  val budget=j.optInt("budget",2);require(budget in 1..2)
  val notes=j.optJSONObject("notes")?.let{n->n.keys().asSequence().filter{LifeCatalog.find(it)!=null}.associateWith{n.getString(it).take(120)}}?:emptyMap()
  return LifeState(j.optBoolean("welcomed"),plans,entries,j.optBoolean("quietEnabled"),LocalTime.parse(j.optString("quietStart","08:00")).toString(),LocalTime.parse(j.optString("quietEnd","16:00")).toString(),budget,reflections,j.optBoolean("notificationsMuted"),notes)
 }
}
