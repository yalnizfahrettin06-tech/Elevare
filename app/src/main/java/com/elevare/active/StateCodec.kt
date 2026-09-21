package com.elevare.active

import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalTime

/** Versioned local format. Invalid data is preserved for recovery, never reset silently. */
object StateCodec {
 private fun strings(a:JSONArray?)=if(a==null) emptySet<String>() else (0 until a.length()).map{a.getString(it)}.toSet()
 private fun objects(a:JSONArray?)=if(a==null) emptyList<JSONObject>() else (0 until a.length()).map{a.getJSONObject(it)}
 private fun date(value:String)=LocalDate.parse(value).toString()
 private fun time(value:String)=LocalTime.parse(value).toString()
 private fun map(j:JSONObject?)=j?.keys()?.asSequence()?.associateWith{j.getString(it)}?:emptyMap()
 private fun answers(a:TrainingAnswers)=JSONObject().apply{
  put("age",a.age);put("focus",a.focus);put("recentGrowth",a.recentGrowth);put("sleep",a.sleep)
  put("activity",a.activity);put("minutes",a.minutes);put("safety",a.safety);put("environment",a.environment)
  put("days",a.days);put("runningExperience",a.runningExperience);put("equipment",JSONArray(a.equipment.toList()))
 }
 private fun readAnswers(j:JSONObject)=TrainingAnswers(j.optInt("age"),j.optString("focus"),j.optString("recentGrowth"),
  j.optString("sleep"),j.optString("activity"),j.optInt("minutes"),j.optString("safety"),j.optString("environment"),
  j.optInt("days"),j.optString("runningExperience"),strings(j.optJSONArray("equipment")))
 private fun workout(w:Workout)=JSONObject().apply{
  put("id",w.id);put("title",w.title);put("subtitle",w.subtitle);put("category",w.category);put("color",w.color)
  put("steps",JSONArray().apply{w.steps.forEach{s->put(JSONObject().apply{put("move",s.moveId);put("seconds",s.seconds);put("rest",s.rest);put("phase",s.phase);put("side",s.side)})}})
 }
 private fun readWorkout(j:JSONObject):Workout {
  val steps=objects(j.getJSONArray("steps")).map{Step(it.getString("move"),it.getInt("seconds"),it.optBoolean("rest"),it.optString("phase"),it.optString("side"))}
  require(steps.isNotEmpty()&&steps.all{it.seconds in 1..1800&&Content.moves.any{m->m.id==it.moveId}}&&steps.sumOf{it.seconds}<=3600)
  return Workout(j.getString("id"),j.getString("title"),j.getString("subtitle"),j.getString("category"),steps,j.optInt("color"))
 }
 fun encode(s:UserState,remainingSeconds:(ActiveSession)->Int={remaining(it)}):String=JSONObject().apply{
  put("version",9);put("life",LifeCodec.encode(s.life));put("ready",s.ready);put("name",s.name);put("start",s.start);put("cycleId",s.cycleId)
  put("onboardingVersion",s.onboardingVersion);put("age",s.age);put("dailyMinutes",s.dailyMinutes)
  put("trialDays",s.trialDays);put("demoStartedAt",s.demoStartedAt);put("reminders",s.reminders)
  put("sleepReminderLeadMinutes",normalizedSleepReminderLead(s.sleepReminderLeadMinutes))
  put("focus",s.focus);put("recentGrowth",s.recentGrowth);put("sleepHabit",s.sleepHabit);put("activityHabit",s.activityHabit)
  put("safety",s.safety);put("voiceCoach",s.voiceCoach);put("timerSound",s.timerSound)
  put("environment",s.environment);put("trainingDays",s.trainingDays);put("autoAdvance",s.autoAdvance)
  put("runningExperience",s.runningExperience);put("equipment",JSONArray(s.equipment.toList()));put("progressionAccepted",s.progressionAccepted)
  put("progressionConsents",JSONArray().apply{s.progressionConsents.forEach{c->put(JSONObject().apply{
   put("proposalId",c.proposalId);put("cycleId",c.cycleId);put("level",c.level);put("evidenceSessionIds",JSONArray(c.evidenceSessionIds))
   put("profileKey",c.profileKey);put("reviewRecord",c.reviewRecord);put("acceptedAtEpochMs",c.acceptedAtEpochMs)
  })}})
  put("dailyCheckDate",s.dailyCheckDate);put("dailyReadiness",s.dailyReadiness);put("dailyEnvironment",s.dailyEnvironment);put("dailyEquipmentConfirmed",s.dailyEquipmentConfirmed)
  put("externalSportDate",s.externalSportDate);put("savedFacts",JSONArray(s.savedFacts.toList()));put("factHistory",JSONObject(s.factHistory))
  put("factSeenAt",JSONObject(s.factSeenAt));put("recentFactIds",JSONArray(s.recentFactIds))
  put("workoutReminders",s.workoutReminders);put("workoutReminderTime",s.workoutReminderTime);put("lastReminderDate",s.lastReminderDate)
  put("favorites",JSONArray(s.favorites.toList()))
  put("done",JSONObject().apply{s.done.forEach{(k,v)->put(k,JSONArray(v.toList()))}})
  put("sessions",JSONArray().apply{s.sessions.forEach{l->put(JSONObject().apply{
   put("id",l.id);put("title",l.title);put("date",l.date);put("seconds",l.seconds);put("feeling",l.feeling);put("type",l.type)
   put("workoutId",l.workoutId);put("completedAtEpochMs",l.completedAtEpochMs);put("cycleId",l.cycleId);put("completed",l.completed)
   put("loadKind",l.loadKind);put("programDay",l.programDay);put("plannedSeconds",l.plannedSeconds)
  })}})
  put("sleep",JSONArray().apply{s.sleeps.forEach{l->put(JSONObject().apply{put("date",l.date);put("bed",l.bed);put("wake",l.wake)})}})
  put("bed",s.bed);put("wake",s.wake);put("reduced",s.reducedMotion);put("dark",true);put("haptic",s.haptic);put("gentle",s.gentle)
  put("pausedOn",s.pausedOn?:JSONObject.NULL);put("pausedDays",s.pausedDays)
  put("onboardingDraft",s.onboardingDraft?.let{d->JSONObject().apply{
   put("answers",answers(d.answers));put("step",d.step);put("preparingElapsedMs",d.preparingElapsedMs);put("version",d.version)
  }}?:JSONObject.NULL)
  put("archivedCycles",JSONArray().apply{s.archivedCycles.forEach{c->put(JSONObject().apply{
   put("id",c.id);put("start",c.start);put("end",c.end);put("pausedDays",c.pausedDays);put("version",c.version)
   put("trainingDays",c.trainingDays);put("dailyMinutes",c.dailyMinutes);put("plannedSessions",c.plannedSessions);put("completedSessions",c.completedSessions)
  })}})
  put("active",s.active?.let{a->JSONObject().apply{
   put("workout",workout(activeWorkout(a)));put("workoutId",a.workoutId);put("step",a.step);put("remaining",remainingSeconds(a))
   put("elapsed",a.elapsed);put("date",a.startedDate);put("id",a.id);put("planDay",a.planDay)
  }}?:JSONObject.NULL)
 }.toString(2)

 fun decode(raw:String):UserState {
  val j=JSONObject(raw);require(j.optInt("version",2) in 2..9){"Unsupported schema"}
  val start=date(j.getString("start"))
  val done=j.optJSONObject("done")?:JSONObject()
  val active=j.optJSONObject("active")?.let{a->
   val w=a.optJSONObject("workout")?.let(::readWorkout)?:Content.workout(a.getString("workoutId"))
   val index=a.getInt("step");require(index in w.steps.indices)
   ActiveSession(w.id,index,a.getInt("remaining").coerceIn(0,w.steps[index].seconds),0,false,
    a.optInt("elapsed").coerceAtLeast(0),date(a.getString("date")),a.getString("id"),a.optInt("planDay",1).coerceIn(1,90),w,0)
  }
  val draft=j.optJSONObject("onboardingDraft")?.let{d->OnboardingDraft(readAnswers(d.getJSONObject("answers")),d.optInt("step").coerceIn(0,13),d.optLong("preparingElapsedMs").coerceIn(0,30_000),d.optInt("version",8))}
  return UserState(
   ready=j.optBoolean("ready"),name=j.optString("name").take(24),start=start,life=LifeCodec.decode(j.optJSONObject("life")),
   favorites=strings(j.optJSONArray("favorites")),
   done=done.keys().asSequence().associateWith{strings(done.optJSONArray(it))},
   sessions=objects(j.optJSONArray("sessions")).map{l->SessionLog(l.getString("id"),l.getString("title"),date(l.getString("date")),
    l.getInt("seconds").coerceAtLeast(0),l.optString("feeling"),l.optString("type","workout"),l.optString("workoutId"),
    l.optLong("completedAtEpochMs"),l.optString("cycleId"),l.optBoolean("completed",true),l.optString("loadKind"),
    l.optInt("programDay"),l.optInt("plannedSeconds",l.getInt("seconds")))},
   sleeps=objects(j.optJSONArray("sleep")).map{SleepLog(date(it.getString("date")),time(it.getString("bed")),time(it.getString("wake")))}.filter{it.minutes>0},
   bed=time(j.optString("bed","22:00")),wake=time(j.optString("wake","07:00")),reducedMotion=j.optBoolean("reduced"),dark=true,
   haptic=j.optBoolean("haptic",true),gentle=j.optBoolean("gentle"),
   pausedOn=if(j.isNull("pausedOn"))null else date(j.getString("pausedOn")),pausedDays=j.optLong("pausedDays").coerceAtLeast(0),active=active,
   onboardingVersion=j.optInt("onboardingVersion"),age=j.optInt("age"),dailyMinutes=j.optInt("dailyMinutes",5),
   trialDays=normalizeTrialDays(j.optInt("trialDays")),demoStartedAt=j.optLong("demoStartedAt"),reminders=j.optBoolean("reminders"),
   sleepReminderLeadMinutes=normalizedSleepReminderLead(j.optInt("sleepReminderLeadMinutes",30)),
   focus=j.optString("focus"),recentGrowth=j.optString("recentGrowth"),sleepHabit=j.optString("sleepHabit"),activityHabit=j.optString("activityHabit"),
   safety=j.optString("safety"),voiceCoach=j.optBoolean("voiceCoach",true),environment=j.optString("environment"),trainingDays=j.optInt("trainingDays"),
   autoAdvance=j.optBoolean("autoAdvance",true),timerSound=j.optString("timerSound","countdown").takeIf{it in listOf("off","countdown","every_second")}?:"countdown",
   runningExperience=j.optString("runningExperience"),equipment=strings(j.optJSONArray("equipment")),progressionAccepted=j.optBoolean("progressionAccepted"),
   progressionConsents=objects(j.optJSONArray("progressionConsents")).map{c->
    val evidence=c.getJSONArray("evidenceSessionIds").let{a->(0 until a.length()).map{a.getString(it)}}
    ProgressionConsent(proposalId=c.getString("proposalId"),cycleId=c.getString("cycleId"),level=c.getInt("level"),
     evidenceSessionIds=evidence,profileKey=c.getString("profileKey"),reviewRecord=c.getString("reviewRecord"),acceptedAtEpochMs=c.getLong("acceptedAtEpochMs"))
     .also{require(it.proposalId.isNotBlank()&&it.cycleId.isNotBlank()&&it.profileKey.isNotBlank()&&it.reviewRecord.isNotBlank()&&
      it.level in 1..3&&it.evidenceSessionIds.size==2&&it.evidenceSessionIds.distinct().size==2&&it.evidenceSessionIds.all(String::isNotBlank)&&it.acceptedAtEpochMs>0)}
   },
   cycleId=j.optString("cycleId").ifEmpty{"legacy:$start"},schemaVersion=9,onboardingDraft=draft?.restored(),
   dailyCheckDate=j.optString("dailyCheckDate"),dailyReadiness=j.optString("dailyReadiness"),dailyEnvironment=j.optString("dailyEnvironment"),dailyEquipmentConfirmed=j.optBoolean("dailyEquipmentConfirmed"),
   externalSportDate=j.optString("externalSportDate"),savedFacts=strings(j.optJSONArray("savedFacts")),factHistory=map(j.optJSONObject("factHistory")),
   factSeenAt=map(j.optJSONObject("factSeenAt")),recentFactIds=j.optJSONArray("recentFactIds")?.let{a->(0 until a.length()).map{a.getString(it)}}?:emptyList(),
   workoutReminders=j.optBoolean("workoutReminders"),workoutReminderTime=time(j.optString("workoutReminderTime","17:00")),lastReminderDate=j.optString("lastReminderDate"),
   archivedCycles=objects(j.optJSONArray("archivedCycles")).map{ProgramCycleArchive(it.getString("id"),date(it.getString("start")),date(it.getString("end")),it.optLong("pausedDays"),it.optInt("version",8),it.optInt("trainingDays",3),it.optInt("dailyMinutes",5),it.optInt("plannedSessions"),it.optInt("completedSessions"))}
  )
 }
}
