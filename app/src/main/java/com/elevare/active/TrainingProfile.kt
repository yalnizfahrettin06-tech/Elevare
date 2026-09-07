package com.elevare.active

data class TrainingAnswers(val age:Int=0,val focus:String="",val recentGrowth:String="",val sleep:String="",val activity:String="",val minutes:Int=0,val safety:String="")
object ProfileChoices {
 val ages=13..21
 val minutes=listOf(5,10,15,30)
 val focus=linkedMapOf("performance" to "Güç ve kondisyon","recovery" to "Toparlanma ve rutin","sleep" to "Uyku ve günlük enerji","growth" to "Büyüme ve GH hakkında bilgi")
 val growth=linkedMapOf("yes" to "Evet, ölçümlerde artış gördüm","no" to "Hayır, fark etmedim","unknown" to "Ölçmedim / bilmiyorum")
 val sleep=linkedMapOf("under6" to "6 saatten az","6to8" to "6–8 saat","8to10" to "8–10 saat","over10" to "10 saatten fazla","unknown" to "Düzenim değişiyor")
 val activity=linkedMapOf("new" to "Henüz düzenli değil","some" to "Haftada 1–2 gün","regular" to "Haftada 3 gün veya daha fazla")
 val safety=linkedMapOf("clear" to "Hayır, bildiğim bir engel yok","pain" to "Şu anda ağrı / rahatsızlık var","restricted" to "Bir uzman hareketimi kısıtladı","unknown" to "Emin değilim")
}
fun TrainingAnswers.complete()=age in ProfileChoices.ages&&focus in ProfileChoices.focus&&recentGrowth in ProfileChoices.growth&&sleep in ProfileChoices.sleep&&activity in ProfileChoices.activity&&minutes in ProfileChoices.minutes&&safety in ProfileChoices.safety
fun UserState.answers()=TrainingAnswers(age,focus,recentGrowth,sleepHabit,activityHabit,dailyMinutes,safety)
fun focusLabel(value:String)=ProfileChoices.focus[value]?:"Rutinimi oluştur"
fun trainingAllowed(s:UserState)=s.safety=="clear"
fun sleepGuide(age:Int)=if(age<18)"Gençler için genel aralık: 8–10 saat." else "Genç yetişkinler için genel aralık: 7–9 saat."
data class RoutinePlan(val workoutId:String,val headline:String,val note:String,val optionalMinutes:Int,val blocked:Boolean)
fun routinePlan(a:TrainingAnswers,gentle:Boolean=false):RoutinePlan {
 val blocked=a.safety!="clear"
 val id=when {
  gentle||a.sleep=="under6"||a.activity=="new"||a.focus in listOf("sleep","recovery")->"flow"
  a.focus=="performance"&&a.minutes>=10->"strength"
  else->"runprep"
 }
 val duration=Content.workout(id).seconds
 return RoutinePlan(id,if(blocked)"Önce uygunluğunu netleştir" else focusLabel(a.focus),
  if(blocked)"Ağrı, kısıtlama veya belirsizlik bildirdin. Antrenmandan önce bir sağlık uzmanıyla uygunluğunu konuş."
  else if(a.sleep=="under6")"Az uyku bildirdiğin için kısa ve hafif bir başlangıç seçildi."
  else if(a.activity=="new")"Yeni başladığın için kısa hareket şablonu seçildi."
  else "Öncelik ve süre tercihine göre mevcut kısa seanslar eşleştirildi.",
  (a.minutes-(duration+59)/60).coerceAtLeast(0),blocked)
}
const val PREPARATION_DURATION_MS=45_000L
fun preparationStage(elapsed:Long)=when {elapsed<15_000L->"Yanıtların özetleniyor";elapsed<30_000L->"Rutin seçeneklerin eşleştiriliyor";elapsed<PREPARATION_DURATION_MS->"Başlangıç ekranın hazırlanıyor";else->"Rutin analizi tamamlandı"}
