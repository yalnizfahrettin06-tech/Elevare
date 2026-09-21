package com.elevare.active

import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.UUID

const val PROGRAM_VERSION = 8
const val PROGRAM_LENGTH = 90

/** Review flags are deliberately false in this release, never inferred from questionnaire answers. */
data class ProgramReviewApprovals(
 val running: Boolean = false,
 val strength: Boolean = false,
 val yoga: Boolean = false,
 val combined: Boolean = false,
 val adultExtendedIntervals: Boolean = false,
 val reviewerRecord: String = ""
) {
 fun allows(code: String): Boolean {
  if (code in setOf("P", "D", "N")) return true
  if (reviewerRecord.isBlank()) return false
  return when {
   code.startsWith("R") -> running
   code.startsWith("G") -> strength
   code == "Y" -> yoga
   code.startsWith("K") -> combined && strength && (code == "K-P" || running)
   else -> false
  }
 }
}
val RELEASE_PROGRAM_APPROVALS = ProgramReviewApprovals()

/** A consent names one level, its evidence and the reviewed profile; never a blanket opt-in. */
data class ProgressionConsent(
 val proposalId:String,
 val cycleId:String,
 val level:Int,
 val evidenceSessionIds:List<String>,
 val profileKey:String,
 val reviewRecord:String,
 val acceptedAtEpochMs:Long
)
data class ProgressionProposal(
 val id:String,
 val fromCode:String,
 val toCode:String,
 val currentIntervals:Int,
 val nextIntervals:Int,
 val evidenceSessionIds:List<String>,
 val profileKey:String,
 val reviewRecord:String,
 val summary:String
)

data class ProgramCycleArchive(
 val id: String,
 val start: String,
 val end: String,
 val pausedDays: Long = 0,
 val version: Int = PROGRAM_VERSION,
 val trainingDays: Int = 3,
 val dailyMinutes: Int = 5,
 val plannedSessions: Int = 0,
 val completedSessions: Int = 0
)
data class ProgramDay(
 val index: Int,
 val kind: String,
 val workout: Workout,
 val training: Boolean,
 val day: Int = index + 1,
 val phase: String = "F1",
 val templateCode: String = "P",
 val reasonCodes: List<String> = emptyList(),
 val reason: String = "",
 val plannedWorkout: Workout = workout,
 val blocked: Boolean = false,
 val plannedTemplateCode: String = templateCode
) {
 val adapted get() = workout.id != plannedWorkout.id
 val reviewPending get() = "CONTENT_REVIEW_REQUIRED" in reasonCodes
}
data class ProgramPhase(val code: String, val title: String, val firstDay: Int, val lastDay: Int)
fun programPhase(day: Int): ProgramPhase {
 require(day in 1..PROGRAM_LENGTH)
 return when (day) {
  in 1..14 -> ProgramPhase("F1", "Tanış ve hazırlan", 1, 14)
  in 15..28 -> ProgramPhase("F2", "Temel ritim", 15, 28)
  in 29..49 -> ProgramPhase("F3", "Düzeni geliştir", 29, 49)
  in 50..77 -> ProgramPhase("F4", "Pekiştir", 50, 77)
  else -> ProgramPhase("F5", "Kapat ve devamı seç", 78, 90)
 }
}
fun programElapsedDay(s: UserState, today: LocalDate = LocalDate.now()): Int {
 val start = runCatching { LocalDate.parse(s.start) }.getOrNull() ?: return 1
 val end = s.pausedOn?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: today
 return (ChronoUnit.DAYS.between(start, end) - s.pausedDays + 1).coerceIn(1, Int.MAX_VALUE.toLong()).toInt()
}
fun isCycleComplete(s: UserState, today: LocalDate = LocalDate.now()) = programElapsedDay(s, today) >= PROGRAM_LENGTH
fun isProgramScheduled(s:UserState,today:LocalDate=LocalDate.now()):Boolean =
 runCatching{LocalDate.parse(s.start)>today}.getOrDefault(false)

/** Initial calendar alignment only; never move an existing/partially completed training history. */
fun canChooseProgramStart(s:UserState,today:LocalDate=LocalDate.now()):Boolean =
 s.active==null && s.pausedOn==null && !isCycleComplete(s,today) &&
  s.sessions.none{it.type=="workout" && (it.cycleId==s.cycleId || it.cycleId.isBlank())}

fun chooseProgramStart(s:UserState,date:LocalDate,today:LocalDate=LocalDate.now()):UserState {
 if(!canChooseProgramStart(s,today) || date<today || date>today.plusDays(6))return s
 return s.copy(start=date.toString(),pausedDays=0,progressionAccepted=false,progressionConsents=emptyList(),
  dailyCheckDate="",dailyReadiness="",dailyEnvironment="",dailyEquipmentConfirmed=false)
}

/** Explicit user action only; archived sessions retain their identity and cycle metadata. */
fun nextCycle(s: UserState, today: LocalDate = LocalDate.now()): UserState {
 if (s.active != null || !isCycleComplete(s, today)) return s
 val planned = ninetyDayProgram(s,today).filter { it.training }.map { it.day }.toSet()
 val completed = s.sessions.filter { it.completed && it.cycleId==s.cycleId && it.type=="workout" && it.programDay in planned }.map { it.programDay }.distinct().size
 val openPause=s.pausedOn?.let { runCatching{ChronoUnit.DAYS.between(LocalDate.parse(it),today).coerceAtLeast(0)}.getOrDefault(0) }?:0
 val archive = ProgramCycleArchive(s.cycleId, s.start, today.toString(), s.pausedDays+openPause,
  trainingDays=s.trainingDays,dailyMinutes=s.dailyMinutes,plannedSessions=planned.size,completedSessions=completed)
 return s.copy(
  start = today.toString(), cycleId = UUID.randomUUID().toString(),
  pausedOn = null, pausedDays = 0, progressionAccepted = false, progressionConsents = emptyList(),
  archivedCycles = s.archivedCycles + archive,
  // Only old unscoped counters are reset; archived cycle-scoped entries remain intact.
  done = s.done.filterKeys { !it.matches(Regex("plan:[0-9]+")) },
  dailyCheckDate = "", dailyReadiness = "", dailyEnvironment = "", dailyEquipmentConfirmed = false
 )
}

fun programReason(a: TrainingAnswers, gentle: Boolean = false): String = when {
 a.safety != "clear" -> "Başlamadan önce hareket uygunluğunu netleştirelim."
 gentle || a.sleep == "under6" -> "Toparlanmaya alan açan hafif bir başlangıç."
 a.environment == "indoor" -> "Alanına ve ekipmanına uygun ev akışı."
 a.minutes == 5 -> "Beş dakikalık, hızlanmasız hazırlık."
 else -> "Önce hazırlık; sonraki adımlar yanıtlarına ve uygunluğa göre."
}

/** Answers-only preview is always the first seven days, not a forever repeating program. */
fun weeklyProgram(a: TrainingAnswers, gentle: Boolean = false): List<ProgramDay> {
 val state = UserState(age=a.age, focus=a.focus, recentGrowth=a.recentGrowth,
  sleepHabit=a.sleep, activityHabit=a.activity, dailyMinutes=a.minutes, safety=a.safety,
  environment=a.environment, trainingDays=a.days, runningExperience=a.runningExperience,
  equipment=a.equipment, gentle=gentle)
 return (1..7).map { programForDay(state, it) }
}
fun currentWeekProgram(s: UserState, today: LocalDate = LocalDate.now()): List<ProgramDay> {
 val first = ((programElapsedDay(s,today).coerceAtMost(90)-1)/7)*7+1
 return (first..minOf(first+6,90)).map { programForDay(s,it,today) }
}
fun ninetyDayProgram(s: UserState, today: LocalDate = LocalDate.now()): List<ProgramDay> =
 (1..PROGRAM_LENGTH).map { programForDay(s,it,today) }

private fun weekRunCode(week: Int) = when(week) {
 1,2 -> "P"
 3,4,7,13 -> "R1"
 5,6,8,10,12 -> "R2"
 else -> "R3"
}
private fun weekStrengthCode(week: Int) =
 (if(week%2==1) "G-A" else "G-B") + if(week in setOf(7,12,13)) "-L" else ""

fun calendarTemplate(a: TrainingAnswers, day: Int): String {
 require(day in 1..90)
 if(day==90) return "D"
 val week=(day-1)/7+1
 val position=(day-1)%7
 val run=weekRunCode(week)
 val strength=weekStrengthCode(week)
 val indoorA=if(week in setOf(7,12,13))"G-A-L" else "G-A"
 val indoorB=if(week in setOf(7,12,13))"G-B-L" else "G-B"
 val indoor=a.environment=="indoor"
 return when(a.days) {
  2 -> when(position) {
   0 -> if(indoor) indoorA else run
   3 -> if(indoor) "Y" else if(a.minutes>=15) if(week<=2) "K-P" else "K" else strength
   else -> "D"
  }
  4 -> when(position) {
   0 -> if(indoor) indoorA else run
   2 -> if(indoor) "Y" else strength
   4 -> if(indoor) indoorB else run
   5 -> "Y"
   else -> "D"
  }
  else -> when(position) {
   0 -> if(indoor) indoorA else run
   2 -> if(indoor) "Y" else strength
   4 -> if(indoor) indoorB else run
   else -> "D"
  }
 }
}

private fun codeOf(w: Workout): String? = w.id.split("_").takeIf { it.size==4 && it[0]=="p8" }?.get(1)
private fun loadIsRun(log: SessionLog): Boolean =
 log.loadKind in setOf("run","combined") ||
 runCatching { Content.workout(log.workoutId).hasSprint() }.getOrDefault(false)

/** Unknown historic loads are retained and treated cautiously; titles never classify a workload. */
fun recentRunningLoad(s: UserState, today: LocalDate = LocalDate.now(), now: Long? = null): Boolean {
 val zone=ZoneId.systemDefault()
 val checkTime=now ?: today.atStartOfDay(zone).toInstant().toEpochMilli()
 val hasRun=s.sessions.any { log ->
  if(!loadIsRun(log)) return@any false
  if(log.completedAtEpochMs>0) {
   val delta=checkTime-log.completedAtEpochMs
   // A future event means clock/data uncertainty, not evidence of zero recent load.
   delta < 48*60*60*1000L
  } else {
   val date=runCatching { LocalDate.parse(log.date) }.getOrNull() ?: return@any true
   // Date-only logs cannot prove 48h. Include day two conservatively.
   ChronoUnit.DAYS.between(date,today) <= 2
  }
 }
 val external=runCatching { LocalDate.parse(s.externalSportDate) }.getOrNull()
 return hasRun || (external!=null && ChronoUnit.DAYS.between(external,today) <= 2)
}
fun unknownRecentLoad(s: UserState, today: LocalDate = LocalDate.now()): Boolean =
 s.sessions.any { log ->
  val date=runCatching { LocalDate.parse(log.date) }.getOrNull() ?: return@any false
  log.type=="workout" && log.workoutId.isBlank() && log.loadKind.isBlank() &&
   ChronoUnit.DAYS.between(date,today) <= 2
 }

private fun relevantCompleted(s:UserState,today:LocalDate):List<SessionLog> =
 s.sessions.filter { log ->
  log.completed && log.type=="workout" && log.cycleId==s.cycleId && log.programDay in 1..90 &&
   runCatching { LocalDate.parse(log.date)<=today }.getOrDefault(false)
 }.sortedWith(compareBy<SessionLog>{it.date}.thenBy{it.completedAtEpochMs}).distinctBy { it.programDay }

private fun tolerated(log:SessionLog)=log.feeling in setOf("Kolay","Uygundu","İyi","easy","appropriate")
private fun logCode(log:SessionLog)=runCatching{codeOf(Content.workout(log.workoutId))}.getOrNull()
private fun progressionProfileKey(s:UserState):String = UUID.nameUUIDFromBytes(listOf(
 s.age,s.focus,s.sleepHabit,s.activityHabit,s.dailyMinutes,s.safety,s.environment,s.trainingDays,
 s.runningExperience,s.equipment.sorted().joinToString(",")
).joinToString("|").toByteArray(Charsets.UTF_8)).toString()
private fun progressionId(s:UserState,level:Int,evidence:List<String>,profileKey:String,reviewRecord:String,date:LocalDate):String =
 UUID.nameUUIDFromBytes(listOf(s.cycleId,level,evidence.joinToString(","),profileKey,reviewRecord,date)
  .joinToString("|").toByteArray(Charsets.UTF_8)).toString()

/** Include partial/negative attempts when selecting the latest two; filtering them out would hide risk. */
private fun progressionLogs(s:UserState,today:LocalDate):List<SessionLog> = s.sessions.filter { log ->
 log.type=="workout" && log.cycleId==s.cycleId && log.programDay in 1..90 &&
  runCatching{LocalDate.parse(log.date)<=today}.getOrDefault(false)
}.sortedWith(compareBy<SessionLog>{it.date}.thenBy{it.completedAtEpochMs})

private fun completedAsPlanned(log:SessionLog):Boolean {
 val workout=runCatching{Content.workout(log.workoutId)}.getOrNull()?:return false
 return log.completed && log.plannedSeconds==workout.seconds && log.seconds>=log.plannedSeconds && tolerated(log)
}
private fun evidenceAfterConsent(log:SessionLog,consent:ProgressionConsent?):Boolean {
 if(consent==null)return true
 return if(log.completedAtEpochMs>0)log.completedAtEpochMs>=consent.acceptedAtEpochMs
 else runCatching{LocalDate.parse(log.date)>=java.time.Instant.ofEpochMilli(consent.acceptedAtEpochMs)
  .atZone(ZoneId.systemDefault()).toLocalDate()}.getOrDefault(false)
}
private fun acceptedConsents(s:UserState,today:LocalDate,approvals:ProgramReviewApprovals):List<ProgressionConsent> {
 if(!approvals.allows("R1"))return emptyList()
 val profileKey=progressionProfileKey(s)
 val logs=progressionLogs(s,today)
 val found=mutableListOf<ProgressionConsent>()
 for(level in 1..3) {
  val priorCode=if(level==1)"P" else "R${level-1}"
  val previous=found.lastOrNull()
  val consent=s.progressionConsents.lastOrNull { c ->
   if(c.cycleId!=s.cycleId || c.level!=level || c.profileKey!=profileKey || c.reviewRecord!=approvals.reviewerRecord ||
    c.acceptedAtEpochMs<=0 || c.evidenceSessionIds.size!=2 || c.evidenceSessionIds.distinct().size!=2)return@lastOrNull false
   val date=java.time.Instant.ofEpochMilli(c.acceptedAtEpochMs).atZone(ZoneId.systemDefault()).toLocalDate()
   if(date>today || c.proposalId!=progressionId(s,level,c.evidenceSessionIds,profileKey,c.reviewRecord,date))return@lastOrNull false
   val evidence=c.evidenceSessionIds.mapNotNull{id->logs.singleOrNull{it.id==id}}
   evidence.size==2 && evidence.map{it.programDay}.distinct().size==2 && evidence.all {
    logCode(it)==priorCode && completedAsPlanned(it) && it.plannedSeconds==s.dailyMinutes*60 && evidenceAfterConsent(it,previous) &&
     LocalDate.parse(it.date)<=date && (it.completedAtEpochMs==0L || it.completedAtEpochMs<=c.acceptedAtEpochMs)
   }
  } ?: break
  found+=consent
 }
 return found
}

/** Nothing is offered until reviewed content, two related successful attempts and current context agree. */
fun progressionProposal(
 s:UserState,today:LocalDate=LocalDate.now(),approvals:ProgramReviewApprovals=RELEASE_PROGRAM_APPROVALS
):ProgressionProposal? {
 if(!approvals.allows("R1") || !s.answers().complete() || s.safety!="clear" || s.active!=null || s.pausedOn!=null ||
  isProgramScheduled(s,today) || isCycleComplete(s,today) || s.gentle || s.sleepHabit=="under6" ||
  s.runningExperience !in setOf("new","easy","interval") || s.environment=="indoor" || s.dailyMinutes<10)return null
 if(s.dailyCheckDate==today.toString() && (s.dailyReadiness in setOf("tired","poor_sleep","pain","restricted","unknown") ||
  s.dailyEnvironment !in setOf("","outdoor","both")))return null
 if(recentRunningLoad(s,today) || unknownRecentLoad(s,today) || (returnGap(s,today)?:0)>=7)return null
 val day=programElapsedDay(s,today)
 val candidate=calendarTemplate(s.answers(),day)
 val target=when {candidate.startsWith("R")->candidate.removePrefix("R").toInt();candidate=="K"->1;else->0}
 val consents=acceptedConsents(s,today,approvals)
 val level=consents.size+1
 if(level>target || level>3 || !approvals.allows(candidate))return null
 val secondaryRunSlot=if(s.trainingDays==2)3 else 4
 if(s.focus in setOf("sleep","recovery") && (day-1)%7==secondaryRunSlot)return null
 if(candidate=="K" && (!(s.dailyCheckDate==today.toString() && s.dailyEquipmentConfirmed) ||
   workoutEquipmentMissing(buildVersionedWorkout("K",s.dailyMinutes),s.equipment)))return null
 val fromCode=if(level==1)"P" else "R${level-1}"
 val evidence=progressionLogs(s,today).filter{logCode(it)==fromCode}.takeLast(2)
 if(evidence.size!=2 || evidence.map{it.programDay}.distinct().size!=2 || evidence.map{it.id}.distinct().size!=2 ||
  evidence.any{!completedAsPlanned(it) || it.plannedSeconds!=s.dailyMinutes*60 || !evidenceAfterConsent(it,consents.lastOrNull())})return null
 val recent=progressionLogs(s,today).filter(::loadIsRun).takeLast(2)
 if(recent.any{!completedAsPlanned(it)})return null
 val from=buildVersionedWorkout(fromCode,s.dailyMinutes).intervalCount()
 val next=buildVersionedWorkout("R$level",s.dailyMinutes).intervalCount()
 if(next<=from)return null // Ten-minute variants have no extra level/cycle to market as progress.
 val ids=evidence.map{it.id}
 val key=progressionProfileKey(s)
 return ProgressionProposal(progressionId(s,level,ids,key,approvals.reviewerRecord,today),fromCode,"R$level",from,next,
  ids,key,approvals.reviewerRecord,
  if(level==1)"İki hazırlık seansın uygundu. Kısa koşu-yürüyüşü denemek ister misin?"
  else "Son iki ilgili seansın uygundu. Hızı artırmadan bir çevrim eklemeyi seçebilirsin.")
}

/** Recompute at click time; stale, altered, declined or unsigned proposals never mutate training state. */
fun acceptProgression(
 s:UserState,proposalId:String,now:Long=System.currentTimeMillis(),approvals:ProgramReviewApprovals=RELEASE_PROGRAM_APPROVALS
):UserState {
 if(now<=0)return s
 val today=java.time.Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault()).toLocalDate()
 val p=progressionProposal(s,today,approvals)?:return s
 if(p.id!=proposalId || s.sessions.any{it.id in p.evidenceSessionIds && it.completedAtEpochMs>now})return s
 val c=ProgressionConsent(p.id,s.cycleId,p.toCode.removePrefix("R").toInt(),p.evidenceSessionIds,p.profileKey,p.reviewRecord,now)
 return s.copy(progressionAccepted=false,progressionConsents=s.progressionConsents+c)
}

private fun runLevel(s:UserState, target:Int, today:LocalDate, approvals:ProgramReviewApprovals):Int {
 if(!approvals.allows("R1") || s.runningExperience !in setOf("new","easy","interval"))return 0
 var level=minOf(acceptedConsents(s,today,approvals).size,target)
 val lastTwo=progressionLogs(s,today).filter(::loadIsRun).takeLast(2)
 if(lastTwo.any{!completedAsPlanned(it)})level=minOf(level,1)
 return level
}
private fun returnGap(s:UserState,today:LocalDate):Long? {
 val last=relevantCompleted(s,today).lastOrNull() ?: return null
 return ChronoUnit.DAYS.between(LocalDate.parse(last.date),today)
}
fun workoutEquipmentMissing(w: Workout, equipment: Set<String>): Boolean {
 val ids=w.steps.map{it.moveId}.toSet()
 return ("squat" in ids && "chair" !in equipment) ||
  ("wall" in ids && "wall" !in equipment) ||
  (ids.any{it in setOf("balance","calf")} && equipment.intersect(setOf("wall","chair")).isEmpty()) ||
  (ids.any{it in setOf("catcow","child","lunge")} && "mat" !in equipment)
}
private fun kindFor(code:String)=when {
 code.startsWith("R")->"run"
 code.startsWith("K")->"combined"
 code.startsWith("G")->"strength"
 code=="Y"->"yoga"
 code=="D"->"recovery"
 else->"prep"
}

fun programForDay(
 s: UserState,
 day: Int,
 today: LocalDate = LocalDate.now(),
 approvals: ProgramReviewApprovals = RELEASE_PROGRAM_APPROVALS
): ProgramDay {
 require(day in 1..90)
 val a=s.answers()
 val minutes=s.dailyMinutes.takeIf{it in ProfileChoices.minutes} ?: 5
 val candidate=calendarTemplate(a,day)
 val reasons=mutableListOf<String>()
 val isToday=day==programElapsedDay(s,today)
 val phase=programPhase(day)
 if(candidate=="D") {
  val optional=buildVersionedWorkout("P",5)
  return ProgramDay((day-1)%7,"recovery",optional,false,day,phase.code,"D",
   listOf(if(day==90)"CYCLE_CLOSE" else "REST_DAY"),
   if(day==90)"90 günlük yolculuğun özeti. Yeni döngüyü sen seçersin." else "Bugün zorunlu seans yok.",
   optional, false,"D")
 }
 var selected=candidate
 if(minutes==5 && selected.startsWith("R")) { selected="P";reasons+="SHORT_TIME_BUDGET" }
 if(minutes<15 && selected.startsWith("K")) { selected=weekStrengthCode((day-1)/7+1);reasons+="SHORT_TIME_BUDGET" }
 val planned=buildVersionedWorkout(selected,minutes)
 val plannedCode=codeOf(planned)?:selected
 val scheduled=isToday && isProgramScheduled(s,today)
 val blocking=s.safety!="clear" || (isToday && s.dailyCheckDate==today.toString() &&
  s.dailyReadiness in setOf("pain","restricted","unknown"))
 if(scheduled)reasons+="PROGRAM_NOT_STARTED"
 if(blocking) reasons+="SAFETY_REVIEW"
 if(!a.complete()) reasons+="PROFILE_REVIEW"
 if(s.environment=="indoor") reasons+="INDOOR_ONLY"
 if(selected=="Y" && minutes==30) reasons+="YOGA_VARIETY_LIMIT"
 val dailyIndoor=isToday && s.dailyCheckDate==today.toString() && s.dailyEnvironment !in setOf("","outdoor","both")
 if(dailyIndoor && (selected.startsWith("R")||selected.startsWith("K"))) {
  selected="P";reasons+="DAILY_ENVIRONMENT"
 }
 if(s.gentle||s.sleepHabit=="under6"||
  (isToday&&s.dailyCheckDate==today.toString()&&s.dailyReadiness in setOf("tired","poor_sleep"))) {
  selected="P";reasons+="GENTLE_DAY"
 }
 val secondaryRunSlot=if(s.trainingDays==2)3 else 4
 if(s.focus in setOf("sleep","recovery") && (day-1)%7==secondaryRunSlot &&
  (selected.startsWith("R") || selected.startsWith("K"))) {
  selected="P";reasons+="RECOVERY_FOCUS"
 }
 val gap=returnGap(s,today)
 if((gap?:0)>=7) { selected="P";reasons+=if(gap!!>=14)"RETURN_REASSESSMENT" else "RETURN_PREPARATION" }
 if(selected.startsWith("K") && !(isToday && s.dailyCheckDate==today.toString() && s.dailyEquipmentConfirmed)) {
  selected="P";reasons+="COMBINED_AREA_UNCONFIRMED"
 }
 if(selected.startsWith("R")||selected=="K") {
  if(recentRunningLoad(s,today)) { selected="P";reasons+="RECENT_RUNNING_LOAD" }
  else if(unknownRecentLoad(s,today)) { selected="P";reasons+="UNKNOWN_RECENT_LOAD" }
  else {
   val target=if(selected=="K")1 else selected.removePrefix("R").toInt()
   val allowed=runLevel(s,target,today,approvals)
   if(allowed==0) { selected=if(selected=="K")"K-P" else "P";reasons+="HOLD_LEVEL" }
   else if(selected!="K"&&allowed<target) { selected="R$allowed";reasons+="HOLD_LEVEL" }
  }
 }
 // An unsigned review flag never becomes an approval, regardless of answers or calendar day.
 if(!approvals.allows(plannedCode) || !approvals.allows(selected)) {
  reasons+="CONTENT_REVIEW_REQUIRED";selected="P"
 }
 val candidateWithEquipment=if(selected=="P")planned else buildVersionedWorkout(selected,minutes)
 if(workoutEquipmentMissing(candidateWithEquipment,s.equipment)) {
  reasons+="EQUIPMENT_MISSING";selected="P"
 }
 if(blocking || !a.complete()) selected="P"
 val actual=buildVersionedWorkout(selected,minutes)
 val reason=when {
  scheduled->"İlk antrenmanın ${s.start} tarihinde."
  blocking->"Başlamadan önce hareket uygunluğunu netleştirelim."
  "PROFILE_REVIEW" in reasons->"Yaş ve eksik plan tercihlerini tamamlayalım."
  "RECENT_RUNNING_LOAD" in reasons->"Son sporundan sonra rahat hazırlık ve toparlanma."
  "UNKNOWN_RECENT_LOAD" in reasons->"Eski seansın yükü belirsiz; bugün hazırlıkta kalalım."
  "COMBINED_AREA_UNCONFIRMED" in reasons->"Koşu ve güç için aynı alandaki destekler henüz doğrulanmadı."
  "RETURN_REASSESSMENT" in reasons->"Ara sonrası hazır oluşunu yeniden gözden geçirelim."
  "EQUIPMENT_MISSING" in reasons->"Gerekli destek yok; ekipmansız hazırlık seçildi."
  "CONTENT_REVIEW_REQUIRED" in reasons->"Yeni şablon inceleme bekliyor; şimdilik hazırlık akışı."
  "GENTLE_DAY" in reasons->"Bugün rahat bir hazırlık akışı."
  "HOLD_LEVEL" in reasons->"Uygun düzeyi pekiştiriyoruz; takvim tek başına yük artırmaz."
  "YOGA_VARIETY_LIMIT" in reasons->"Yoga akışı 15 dakika; kalan yürüyüş isteğe bağlı."
  "SHORT_TIME_BUDGET" in reasons->"Beş dakikalık hızlanmasız hazırlık."
  day<=14->"İlk iki hafta hareketleri ve ritmini tanıyoruz."
  else->"Planın, sürene ve haftalık ritmine göre düzenlendi."
 }
 return ProgramDay((day-1)%7,kindFor(selected),actual,true,day,phase.code,selected,
  reasons.distinct(),reason,planned,blocking||scheduled||!a.complete(),plannedCode)
}
fun todayProgram(s:UserState,today:LocalDate=LocalDate.now()):ProgramDay =
 programForDay(s,programElapsedDay(s,today).coerceAtMost(90),today)

/** Existing ids and restored snapshots are not proof of expert review. */
private fun workoutReviewAllowed(w:Workout):Boolean {
 val code=codeOf(w)
 if(code!=null && !RELEASE_PROGRAM_APPROVALS.allows(code))return false
 if(w.hasSprint()&&!RELEASE_PROGRAM_APPROVALS.allows("R1"))return false
 if(w.steps.any{it.moveId in setOf("squat","wall","calf","balance")}&&!RELEASE_PROGRAM_APPROVALS.allows("G-A"))return false
 if(w.steps.any{it.moveId in setOf("catcow","child","lunge")}&&!RELEASE_PROGRAM_APPROVALS.allows("Y"))return false
 return true
}

private fun workoutContextBlockReason(s:UserState,w:Workout,now:Long):String? {
 val today=java.time.Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault()).toLocalDate()
 val knownMoves=Content.moves.map{it.id}.toSet()
 if(w.steps.isEmpty() || w.steps.size>300 || w.steps.any{it.seconds !in 1..3600 || it.moveId !in knownMoves})
  return "Seans adımları doğrulanamadı; kayıtların korunuyor."
 if(s.safety!="clear") return "Önce hareket uygunluğunu netleştirelim."
 if(!s.answers().complete()) return "Eksik plan tercihlerini tamamlayalım."
 if(s.dailyCheckDate==today.toString() && s.dailyReadiness in setOf("pain","restricted","unknown"))
  return "Bugünkü yanıtın için önce uygunluk değerlendirmesi gerekiyor."
 if(workoutEquipmentMissing(w,s.equipment)) return "Bu hareket için gerekli zemin veya destek seçilmedi."
 if(!workoutReviewAllowed(w))return "Bu hareket şablonu uzman incelemesi bekliyor."
 if(w.hasSprint()) {
  if(s.gentle||s.sleepHabit=="under6")return "Bugün daha hafif bir akış seçelim."
  if(s.environment=="indoor" || (s.dailyCheckDate==today.toString() && s.dailyEnvironment !in setOf("","outdoor","both")))
   return "Koşu için uygun dış alan gerekli."
  if(recentRunningLoad(s,today,now)||unknownRecentLoad(s,today)) return "Koşu yükünden sonra toparlanmaya zaman ayıralım."
 }
 val code=codeOf(w)
 if(code?.startsWith("K")==true && !(s.dailyCheckDate==today.toString() && s.dailyEquipmentConfirmed))
  return "Koşu ve güç için aynı güvenli alandaki destekleri doğrulayalım."
 return null
}

/** Defense in depth: library/preview/deep-link cannot bypass daily safety and review gates. */
fun programStartBlockReason(s:UserState,w:Workout,now:Long=System.currentTimeMillis()):String? {
 workoutContextBlockReason(s,w,now)?.let{return it}
 val today=java.time.Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault()).toLocalDate()
 if(isProgramScheduled(s,today) && w.steps.any{it.moveId!="breath"})return "İlk antrenmanın ${s.start} tarihinde başlayacak."
 if(w.hasSprint() && w.id!=todayProgram(s,today).workout.id)return "Bu koşu bugünkü uygun planında değil."
 return null
}

/** Revalidate changing safety, but never regenerate an already started immutable recipe. */
fun sessionResumeBlockReason(s:UserState,a:ActiveSession,now:Long=System.currentTimeMillis()):String? {
 if(s.pausedOn!=null)return "Planın dinlenmede. Önce planına devam et."
 val w=runCatching{activeWorkout(a)}.getOrNull()?:return "Yarım kalan seans okunamadı; kayıtların korunuyor."
 return workoutContextBlockReason(s,w,now)
}

/** A versioned recipe is a deterministic preview, not medical/content approval. */
fun buildVersionedWorkout(code:String,minutes:Int,fastSeconds:Int=10):Workout {
 require(code in setOf("P","R1","R2","R3","G-A","G-B","G-A-L","G-B-L","Y","K","K-P","N"))
 require(minutes in ProfileChoices.minutes || (code=="N" && minutes==2))
 require(fastSeconds in setOf(10,15))
 if(code.startsWith("R")&&minutes==5) return buildVersionedWorkout("P",5)
 if(code.startsWith("K")&&minutes<15) return buildVersionedWorkout("G-A",minutes)
 if(code=="Y"&&minutes==30) return buildVersionedWorkout("Y",15)
 val steps=mutableListOf<Step>()
 fun add(move:String,seconds:Int,rest:Boolean=false,phase:String="main",side:String="") {
  require(seconds>0)
  steps+=Step(move,seconds,rest,phase,side)
 }
 fun easy(seconds:Int,phase:String="main") { if(seconds>0)add("walk",seconds,false,phase) }
 fun station(symbol:Char) {
  when(symbol) {
   'E'->{easy(50);add("walk",10,true,"transition")}
   'T'->{
    add("balance",8,false,"main","left");add("walk",4,true,"transition")
    add("balance",8,false,"main","right")
    add("walk",30,true,"recovery");add("walk",10,true,"transition")
   }
   else->{
    add(when(symbol){'A'->"squat";'B'->"wall";else->"calf"},20)
    add("walk",30,true,"recovery");add("walk",10,true,"transition")
   }
  }
 }
 fun strengthStations(sequence:String,light:Boolean=false) {
  var strength=0
  sequence.forEach { symbol ->
   if(symbol in "ABC") strength++
   station(if(light&&symbol in "ABC"&&strength%2==0)'E' else symbol)
  }
 }
 fun prep5() {
  add("walk",50,false,"warmup");add("walk",10,true,"transition")
  add("step",50);add("walk",10,true,"transition")
  add("shoulder",50);add("walk",10,true,"transition")
  add("step",25,false,"main","left");add("step",25,false,"main","right")
  add("walk",10,true,"transition")
  add("walk",50,false,"cooldown");add("walk",10,true,"transition")
 }
 fun runWarmup() {add("walk",180,false,"warmup");add("march",60,false,"warmup");add("step",60,false,"warmup")}
 fun intervals(cycles:Int,running:Boolean=true) {
  repeat(cycles) {
   if(running) {add("sprint",fastSeconds);add("walk",60-fastSeconds,true,"recovery")}
   else easy(60)
  }
 }
 fun yogaBlock(move:String,side:String="") {
  add(move,15,false,"prepare",side);add(move,30,false,"main",side);add(move,15,false,"exit",side)
 }
 when {
  code=="N" -> { require(minutes==2);add("breath",120) }
  code=="P" -> {
   when(minutes){10->easy(120,"warmup");15->easy(300,"warmup");30->easy(600,"warmup")}
   prep5()
   when(minutes){10->easy(180,"cooldown");15->easy(300,"cooldown");30->easy(900,"cooldown")}
  }
  code.startsWith("R") -> {
   val level=code.last().digitToInt()
   val cycles=when(minutes){10->3;15->3+level;else->5+level}
   runWarmup();intervals(cycles)
   easy(minutes*60-300-cycles*60-120)
   easy(120,"cooldown")
  }
  code.startsWith("G") -> {
   val b=code.startsWith("G-B")
   val light=code.endsWith("-L")
   val sequence=when(minutes){
    5->if(b)"BTA" else "ABC"
    10->if(b)"BETAEB" else "AEBCEA"
    15->if(b)"BETEAEBECET" else "AEBECEAEBEC"
    else->"" // G30 is specified below rather than padded by repeating moves.
   }
   // G30 is specified explicitly: 23 stations, at most eight strength sets.
   val chosen=if(minutes==30) {
    if(b)listOf('B','E','T','A','E','E','C','E','T','B','E','E','A','E','T','C','E','E','B','E','T','A','E').joinToString("")
    else listOf('A','E','E','B','E','T','C','E','E','A','E','E','B','E','T','C','E','E','A','E','E','B','E').joinToString("")
   } else sequence
   val warm=when(minutes){5->60;30->180;else->120}
   val cool=when(minutes){5->60;30->240;else->120}
   easy(warm,"warmup");strengthStations(chosen,light);easy(cool,"cooldown")
  }
  code=="Y" -> {
   val warm=if(minutes==5)60 else 120
   easy(warm,"warmup")
   yogaBlock("catcow");yogaBlock("child")
   if(minutes>=10){yogaBlock("lunge","left");yogaBlock("lunge","right");yogaBlock("child")}
   add("breath",60)
   if(minutes==15){easy(120);yogaBlock("catcow");yogaBlock("child");add("breath",60)}
   easy(if(minutes==5)60 else 120,"cooldown")
  }
  code.startsWith("K") -> {
   runWarmup();intervals(if(minutes==15)3 else 6,code=="K")
   strengthStations(if(minutes==15)"AEBET" else "AEBETEC EAETEBEE".replace(" ",""))
   easy(if(minutes==15)120 else 240,"cooldown")
  }
 }
 val title=when {
  code=="N"->"Doğal nefes molası"
  code=="P"->"Koşuya hazırlık"
  code.startsWith("R")->"Koşu-yürüyüş · $code"
  code.startsWith("G")->if(code.endsWith("-L"))"Hafif güç ve denge" else "Güç ve denge"
  code=="Y"->"Rahat yoga akışı"
  code=="K-P"->"Hazırlık ve kısa güç"
  else->"Koşu ve kısa güç"
 }
 val category=when {
  code.startsWith("R")||code.startsWith("K")||code=="P"->"Koşu"
  code.startsWith("G")->"Güç"
  code=="Y"->"Yoga"
  else->"Toparlanma"
 }
 val subtitle=when {
  code.startsWith("R")->"Kısa kontrollü hızlanma · yürüyüş araları"
  code=="Y"->"Yerleş · rahat hareket et · yavaşça çık"
  code.startsWith("G")->"Kontrollü hareket · dinlenme · geçiş"
  code.startsWith("K")->"Aynı alanda hazırlık ve dinlenmeli hareket"
  else->"Rahat adımlar · hızlanma yok"
 }
 require(steps.sumOf{it.seconds}==minutes*60) { "Recipe duration mismatch: $code/$minutes" }
 return Workout("p8_${code}_${minutes}_$fastSeconds",title,subtitle,category,steps.toList())
}
