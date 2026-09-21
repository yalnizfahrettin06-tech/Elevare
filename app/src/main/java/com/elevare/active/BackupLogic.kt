package com.elevare.active

import org.json.JSONObject

/** Import is intentionally stricter than legacy on-device migration. */
fun validatedBackup(raw:String):UserState {
 require(raw.toByteArray(Charsets.UTF_8).size<=4*1024*1024){"Dosya en fazla 4 MB olabilir."}
 val j=JSONObject(raw)
 require(j.has("version")&&j.has("start")&&j.has("ready")){"Elevare yedeği bulunamadı."}
 listOf("sessions","sleep").forEach{require(j.optJSONArray(it)!=null){"Eksik kayıt listesi: $it"}}
 val state=StateCodec.decode(raw)
 require(!state.ready||state.answers().complete()){ "Yedekteki zorunlu profil yanıtları eksik; mevcut kayıtların değiştirilmedi." }
 require(state.sessions.map{it.id}.distinct().size==state.sessions.size){"Yinelenen seans kimliği."}
 require(state.sleeps.map{it.date}.distinct().size==state.sleeps.size){"Yinelenen uyku tarihi."}
 require(state.sessions.all{it.id.isNotBlank()&&it.seconds in 0..3600&&it.type in setOf("workout","breath")})
 require(state.cycleId.isNotBlank())
 return state.copy(active=state.active?.copy(running=false,deadline=0,elapsedRealtimeDeadline=0),
  reminders=false,workoutReminders=false,life=state.life.copy(notificationsMuted=true,
   plans=state.life.plans.map{it.copy(remind=false)}))
}
