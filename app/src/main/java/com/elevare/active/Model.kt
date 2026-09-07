package com.elevare.active
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit
data class Move(val id:String,val title:String,val category:String,val seconds:Int,val hint:String,val steps:List<String>)
data class Step(val moveId:String,val seconds:Int,val rest:Boolean=false)
data class Workout(val id:String,val title:String,val subtitle:String,val category:String,val steps:List<Step>,val color:Int=0){
 val seconds get()=steps.sumOf{it.seconds}
 val movementCount get()=steps.count{!it.rest}
 val equipment get()=if(steps.any{it.moveId=="squat"})"Sabit sandalye + duvar" else if(steps.any{it.moveId in listOf("wall","balance","calf")})"Duvar / sabit destek" else "Ekipmansız"
}
data class SessionLog(val id:String,val title:String,val date:String,val seconds:Int,val feeling:String,val type:String="workout",val workoutId:String="")
data class SleepLog(val date:String,val bed:String,val wake:String){val minutes get()=sleepDuration(bed,wake)}
data class ActiveSession(val workoutId:String,val step:Int=0,val remaining:Int=0,val deadline:Long=0,val running:Boolean=true,val elapsed:Int=0,val startedDate:String=LocalDate.now().toString(),val id:String=java.util.UUID.randomUUID().toString(),val planDay:Int=1)
data class UserState(val ready:Boolean=false,val name:String="",val start:String=LocalDate.now().toString(),val favorites:Set<String> = emptySet(),val done:Map<String,Set<String>> = emptyMap(),val sessions:List<SessionLog> = emptyList(),val sleeps:List<SleepLog> = emptyList(),val bed:String="22:00",val wake:String="07:00",val reducedMotion:Boolean=false,val dark:Boolean=true,val haptic:Boolean=true,val gentle:Boolean=false,val pausedOn:String?=null,val pausedDays:Long=0,val active:ActiveSession?=null,val onboardingVersion:Int=0,val age:Int=0,val heightCm:Int=0,val targetCm:Int=0,val dailyMinutes:Int=5,val trialDays:Int=0,val reminders:Boolean=false,val focus:String="",val recentGrowth:String="",val sleepHabit:String="",val activityHabit:String="",val safety:String="",val voiceCoach:Boolean=true,val timerSound:String="countdown",val environment:String="",val trainingDays:Int=0,val autoAdvance:Boolean=true)
object Content{
 val moves=listOf(
 Move("sprint","Kontrollü hızlan","Koşu",15,"Rahat koşudan hızlan. Tam efora çıkma; adımların kontrollü kalsın.",listOf("Önce programdaki ısınmayı tamamla. Düz, kuru ve açık bir alan seç.","Hızını kademeli artır. Yaklaşık 6/10 eforda kal; nefesin çok zorlanırsa yürü.","Bölüm bitince yavaşlayarak yürüyüşe geç. Kesintisiz uzun sprint yapma.")),
 Move("catcow","Kedi–inek","Yoga",45,"Nefesle birlikte sırtını yavaşça yuvarla ve aç.",listOf("Eller omuzların, dizler kalçaların altında; yumuşak bir zemin seç.","Nefes verirken sırtını rahatça yuvarla.","Nefes alırken göğsünü hafif aç. Boynunu geriye atma.")),
 Move("child","Çocuk pozu","Yoga",45,"Kalçanı topuklarına yaklaştır. Kolların rahatça uzansın.",listOf("Dizlerinin üzerinde başlayıp kalçanı topuklarına doğru götür.","Gövdeni rahatça öne bırak, kollarını ileri uzat.","Diz veya bel rahatsızlığında pozdan çık; zorlayarak derinleşme.")),
 Move("lunge","Alçak hamle","Yoga",45,"Ön ayağın yerde. Arkadaki dizini yumuşak zemine koy.",listOf("Bir ayağını öne al, arkadaki dizini yumuşak zemine yerleştir.","Gövdeni uzat, ön dizini ayağınla aynı yönde tut.","Ortada yavaşça taraf değiştir. Kalçanı zorlamadan ilerle.")),
 Move("march","Yerinde yürü","Isınma",40,"Rahat adımlar. Nefesin doğal kalsın.",listOf("Ayakta, rahat bir duruş al.","Kollarını hafifçe sallayarak yerinde yürü.","Küçük adımlar kullan; hızlanmak zorunda değilsin.")),
 Move("shoulder","Omuz çevirme","Mobilite",30,"Küçük dairelerle omuzlarını hareket ettir.",listOf("Dik dur; omuzlarını gevşet.","Omuzlarını küçük dairelerle yavaşça çevir.","Hareketini ağrısız ve rahat bir aralıkta tut.")),
 Move("squat","Sandalyeye otur–kalk","Temel güç",30,"Sabit sandalye, kontrollü hareket.",listOf("Kaymayan, sağlam bir sandalyenin önünde dur.","Kalçanı geriye götürerek kontrollü otur.","Rahatça ayağa kalk. Gerekirse ellerinden destek al.")),
 Move("wall","Duvara şınav","Temel güç",30,"Duvarı yavaşça it. Nefesini tutma.",listOf("Ellerini omuz hizasında sağlam duvara koy.","Dirseklerini kontrollü bükerek duvara yaklaş.","Yavaşça geri it. Mesafeyi kolaylaştırabilirsin.")),
 Move("step","Yana adım","Hareket",40,"Sağa ve sola, kendi hızında.",listOf("Düz ve kaymayan bir alanda dur.","Bir yana küçük adım at, diğer ayağını yaklaştır.","Diğer tarafa tekrarla. Zıplama gerekli değil.")),
 Move("balance","Denge molası","Denge",30,"Sabit bir destek yanında dene.",listOf("Duvar veya sağlam destek yanında dur.","Bir ayağını çok az kaldır; gerektiğinde tutun.","Ayağını yere koy ve diğer tarafa geç.")),
 Move("reach","Yukarı uzan","Mobilite",30,"Rahat bir aralıkta uzan ve bırak.",listOf("Ayaklarını dengeli yerleştir.","Kollarını zorlamadan yukarı uzat.","Kollarını yavaşça indir. Belini zorlamadan hareket et.")),
 Move("calf","Topuk yükseltme","Temel güç",30,"Yavaşça yüksel, kontrollü in.",listOf("Sabit bir desteğe hafifçe tutun.","Topuklarını yavaşça yerden kaldır.","Kontrollü indir; ayak bileğinde ağrı olursa dur.")),
 Move("walk","Rahat yürüyüş","Toparlanma",60,"Temponu düşür. Rahatça yürü.",listOf("Güvenli, düz bir alan seç.","Kısa adımlarla rahat bir hızda yürü.","Doğal nefesini sürdür, gerekirse dinlen.")),
 Move("breath","Nefes molası","Toparlanma",120,"Rahatça al, zorlamadan ver.",listOf("Rahat otur veya ayakta dengeli dur.","4 saniyede al, 6 saniyede ver; zorlanırsan normal nefesine dön.","Nefes tutma yok. Baş dönmesinde dur.")))
 val workouts=listOf(
 Workout("runprep","Koşuya hazırlık","Sprint öncesi temel hazırlık • Maksimal koşu içermez","Koşu",listOf(Step("march",60),Step("shoulder",30),Step("step",30),Step("walk",60),Step("balance",30),Step("walk",90)),0),
 Workout("start","İLK ADIM","Sabit sandalye + duvar • Isınma, hareket, toparlanma","Tümü",listOf(Step("march",40),Step("shoulder",30),Step("squat",30),Step("walk",30,true),Step("wall",30),Step("walk",30,true),Step("step",40),Step("balance",30),Step("walk",60)),0),
 Workout("break","DERS ARASI","Kısa bir ekran molası","Mobilite",listOf(Step("march",40),Step("shoulder",30),Step("reach",30),Step("step",40),Step("walk",40)),1),
 Workout("strength","TEMEL GÜÇ","Kontrol ve teknik öncelikli","Güç",listOf(Step("march",60),Step("shoulder",30),Step("squat",30),Step("walk",30,true),Step("wall",30),Step("walk",30,true),Step("calf",30),Step("balance",30),Step("walk",60)),2),
 Workout("flow","MOBİLİTE AKIŞI","Yavaşla, hareket et, toparlan","Mobilite",listOf(Step("march",40),Step("shoulder",30),Step("reach",30),Step("balance",30),Step("walk",50)),1),
 Workout("energy","HAREKET MOLASI","Zıplamadan, kendi hızında","Hareket",listOf(Step("march",60),Step("step",40),Step("walk",30,true),Step("reach",30),Step("step",40),Step("walk",60)),0),
 Workout("recovery","RAHATLA","Hafif yürüyüş + mobilite","Toparlanma",listOf(Step("walk",60),Step("shoulder",30),Step("reach",30),Step("walk",60)),3),
 Workout("breath","NEFES MOLASI","Nefes tutmadan 2 dakika","Toparlanma",listOf(Step("breath",120)),3))
 fun move(id:String)=moves.first{it.id==id}
 fun workout(id:String)=programWorkout(id)?:workouts.first{it.id==id}
}
fun sleepDuration(bed:String,wake:String):Int=try{((ChronoUnit.MINUTES.between(LocalTime.parse(bed),LocalTime.parse(wake))+1440)%1440).toInt()}catch(_:Exception){-1}
fun journeyDay(s:UserState,today:LocalDate=LocalDate.now()):Int=(ChronoUnit.DAYS.between(LocalDate.parse(s.start),s.pausedOn?.let(LocalDate::parse)?:today)-s.pausedDays+1).toInt().coerceIn(1,90)
fun markDone(s:UserState,id:String,date:String=LocalDate.now().toString(),day:Int=journeyDay(s))=s.copy(done=s.done+(date to ((s.done[date]?:emptySet())+id))+("plan:$day" to ((s.done["plan:$day"]?:emptySet())+id)))
fun unmark(s:UserState,id:String,date:String=LocalDate.now().toString())=s.copy(done=s.done+(date to ((s.done[date]?:emptySet())-id))+("plan:${journeyDay(s)}" to ((s.done["plan:${journeyDay(s)}"]?:emptySet())-id)))
fun remaining(s:ActiveSession,now:Long=System.currentTimeMillis()):Int=if(!s.running)s.remaining else ((s.deadline-now+999)/1000).toInt().coerceAtLeast(0)
fun completeSession(s:UserState,active:ActiveSession,feeling:String):UserState{
 if(s.sessions.any{it.id==active.id})return s.copy(active=null)
 val w=Content.workout(active.workoutId)
 if(active.step!=w.steps.lastIndex||remaining(active)>0)return s
 val next=markDone(s,if(w.id=="breath")"breath" else "move",active.startedDate,active.planDay)
 return next.copy(active=null,gentle=s.gentle||feeling=="Rahatsızlık",safety=if(feeling=="Rahatsızlık")"pain" else s.safety,sessions=next.sessions+SessionLog(active.id,w.title,active.startedDate,w.seconds,feeling,if(w.id=="breath")"breath" else "workout",w.id))
}
