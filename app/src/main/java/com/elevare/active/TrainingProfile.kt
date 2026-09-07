package com.elevare.active

data class TrainingAnswers(val age:Int=0,val focus:String="",val recentGrowth:String="",val sleep:String="",val activity:String="",val minutes:Int=0,val safety:String="",val environment:String="",val days:Int=0)
object ProfileChoices {
 val ages=13..21
 val minutes=listOf(5,10,15,30)
 val focus=linkedMapOf("performance" to "Güç ve kondisyon","recovery" to "Esneklik ve toparlanma","sleep" to "Enerji ve uyku düzeni","growth" to "Büyümeyi daha iyi anlamak")
 val growth=linkedMapOf("yes" to "Evet, ölçümlerimde artış var","no" to "Hayır, fark etmedim","unknown" to "Ölçmedim / bilmiyorum")
 val sleep=linkedMapOf("under6" to "6 saatten az","6to8" to "6–8 saat","8to10" to "8–10 saat","over10" to "10 saatten fazla","unknown" to "Düzenim değişiyor")
 val activity=linkedMapOf("new" to "Yeni başlıyorum","some" to "Haftada 1–2 gün","regular" to "Haftada 3 gün veya daha fazla")
 val environment=linkedMapOf("outdoor" to "Dışarıda · park veya pist","indoor" to "Evde · küçük bir alan","both" to "Hem evde hem dışarıda")
 val days=listOf(2,3,4)
 val safety=linkedMapOf("clear" to "Hayır, bildiğim bir engel yok","pain" to "Şu anda ağrı / rahatsızlık var","restricted" to "Bir uzman hareketimi kısıtladı","unknown" to "Emin değilim")
}
fun TrainingAnswers.complete()=age in ProfileChoices.ages&&focus in ProfileChoices.focus&&recentGrowth in ProfileChoices.growth&&sleep in ProfileChoices.sleep&&activity in ProfileChoices.activity&&minutes in ProfileChoices.minutes&&safety in ProfileChoices.safety&&environment in ProfileChoices.environment&&days in ProfileChoices.days
fun UserState.answers()=TrainingAnswers(age,focus,recentGrowth,sleepHabit,activityHabit,dailyMinutes,safety,environment,trainingDays)
fun focusLabel(value:String)=ProfileChoices.focus[value]?:"Antrenman ritmim"
fun trainingAllowed(s:UserState)=s.safety=="clear"
fun sleepGuide(age:Int)=if(age<18)"Gençler için genel aralık: 8–10 saat." else "Genç yetişkinler için genel aralık: 7–9 saat."
data class RoutinePlan(val workoutId:String,val headline:String,val note:String,val optionalMinutes:Int,val blocked:Boolean)
fun routinePlan(a:TrainingAnswers,gentle:Boolean=false):RoutinePlan {
 val w=weeklyProgram(a,gentle).first().workout
 return RoutinePlan(w.id,w.title,programReason(a,gentle),0,a.safety!="clear")
}
const val PREPARATION_DURATION_MS=30_000L
fun preparationStage(elapsed:Long)=when {
 elapsed<7_500L->"Tercihlerin değerlendiriliyor"
 elapsed<15_000L->"Antrenman günlerin hesaplanıyor"
 elapsed<22_500L->"Hareket listen oluşturuluyor"
 elapsed<PREPARATION_DURATION_MS->"Programın hazırlanıyor"
 else->"Programın hazır"
}
