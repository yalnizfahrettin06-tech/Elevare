package com.elevare.active

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject

class Store(private val context: Context) {
    private val prefs = context.getSharedPreferences("elevare_active_v2", Context.MODE_PRIVATE)
    var state by mutableStateOf(read())
        private set
    var error by mutableStateOf("")
        private set
    private fun read(): UserState = try {
        val raw = prefs.getString("state", null)
        if(raw == null) UserState() else decode(JSONObject(raw))
    } catch (_: Exception) { UserState() }
    fun update(change: (UserState) -> UserState) {
        val changed = change(state)
        val next = changed.copy(trialDays=normalizeTrialDays(changed.trialDays))
        state = next
        prefs.edit().putString("state", encode(next).toString()).apply()
        if(next.reminders!=stateBeforeReminder || next.bed!=bedBeforeReminder){Reminder.schedule(context,next);stateBeforeReminder=next.reminders;bedBeforeReminder=next.bed}
    }
    private var stateBeforeReminder = state.reminders
    private var bedBeforeReminder = state.bed
    fun reset() { Reminder.cancel(context);prefs.edit().clear().apply(); state=UserState();stateBeforeReminder=false }
    fun pauseTimer() { state.active?.takeIf { it.running }?.let { a -> update { it.copy(active=a.copy(remaining=remaining(a),running=false,deadline=0)) } } }
    fun export(): String = encode(state.copy(active=null)).toString(2)
    private fun encode(s: UserState): JSONObject = JSONObject().apply {
        put("version",7);put("ready",s.ready);put("name",s.name);put("start",s.start)
        put("onboardingVersion",s.onboardingVersion);put("age",s.age);put("heightCm",s.heightCm);put("targetCm",s.targetCm);put("dailyMinutes",s.dailyMinutes);put("trialDays",s.trialDays);put("reminders",s.reminders)
        put("focus",s.focus);put("recentGrowth",s.recentGrowth);put("sleepHabit",s.sleepHabit);put("activityHabit",s.activityHabit);put("safety",s.safety);put("voiceCoach",s.voiceCoach);put("timerSound",s.timerSound)
        put("environment",s.environment);put("trainingDays",s.trainingDays);put("autoAdvance",s.autoAdvance)
        put("favorites",JSONArray(s.favorites.toList()))
        put("done",JSONObject().apply { s.done.forEach { (k,v) -> put(k,JSONArray(v.toList())) } })
        put("sessions",JSONArray().apply { s.sessions.forEach { l -> put(JSONObject().apply {put("id",l.id);put("title",l.title);put("date",l.date);put("seconds",l.seconds);put("feeling",l.feeling);put("type",l.type);put("workoutId",l.workoutId)}) } })
        put("sleep",JSONArray().apply { s.sleeps.forEach { l -> put(JSONObject().apply {put("date",l.date);put("bed",l.bed);put("wake",l.wake)}) } })
        put("bed",s.bed);put("wake",s.wake);put("reduced",s.reducedMotion);put("dark",s.dark);put("haptic",s.haptic);put("gentle",s.gentle)
        put("pausedOn",s.pausedOn?:JSONObject.NULL);put("pausedDays",s.pausedDays)
        put("active",s.active?.let { a -> JSONObject().apply {put("planDay",a.planDay);put("workoutId",a.workoutId);put("step",a.step);put("remaining",remaining(a));put("running",a.running);put("deadline",a.deadline);put("elapsed",a.elapsed);put("date",a.startedDate);put("id",a.id)} }?:JSONObject.NULL)
    }
    private fun decode(j: JSONObject): UserState {
        fun list(a: JSONArray?) = if(a==null) emptyList() else (0 until a.length()).map { a.getJSONObject(it) }
        fun strings(a: JSONArray?) = if(a==null) emptySet() else (0 until a.length()).map {a.getString(it)}.toSet()
        val rawDone = j.optJSONObject("done")?:JSONObject()
        val active=j.optJSONObject("active")?.let { a ->
            val w=runCatching{Content.workout(a.optString("workoutId"))}.getOrNull()?:return@let null
            val index=a.optInt("step").coerceIn(0,w.steps.lastIndex)
            ActiveSession(w.id,index,a.optInt("remaining").coerceIn(0,w.steps[index].seconds),0,false,a.optInt("elapsed").coerceAtLeast(0),a.optString("date"),a.optString("id"),a.optInt("planDay",1))
        }
        return UserState(
            ready=j.optBoolean("ready"),name=j.optString("name").take(24),start=j.optString("start").let {java.time.LocalDate.parse(it).toString()},
            favorites=strings(j.optJSONArray("favorites")).filter { id -> runCatching{Content.workout(id)}.isSuccess }.toSet(),
            done=rawDone.keys().asSequence().associateWith {strings(rawDone.optJSONArray(it))},
            sessions=list(j.optJSONArray("sessions")).map { SessionLog(it.getString("id"),it.getString("title"),it.getString("date"),it.getInt("seconds"),it.optString("feeling"),it.optString("type","workout"),it.optString("workoutId")) },
            sleeps=list(j.optJSONArray("sleep")).map { SleepLog(it.getString("date"),it.getString("bed"),it.getString("wake")) }.filter { it.minutes>0 },
            bed=j.optString("bed","22:00"),wake=j.optString("wake","07:00"),reducedMotion=j.optBoolean("reduced"),dark=true,haptic=j.optBoolean("haptic",true),gentle=j.optBoolean("gentle"),
            pausedOn=if(j.isNull("pausedOn"))null else j.optString("pausedOn"),pausedDays=j.optLong("pausedDays"),active=active,
            onboardingVersion=j.optInt("onboardingVersion"),age=j.optInt("age"),heightCm=j.optInt("heightCm"),targetCm=j.optInt("targetCm"),dailyMinutes=j.optInt("dailyMinutes",5),trialDays=normalizeTrialDays(j.optInt("trialDays")),reminders=j.optBoolean("reminders"),focus=j.optString("focus"),recentGrowth=j.optString("recentGrowth"),sleepHabit=j.optString("sleepHabit"),activityHabit=j.optString("activityHabit"),safety=j.optString("safety"),voiceCoach=j.optBoolean("voiceCoach",true),environment=j.optString("environment"),trainingDays=j.optInt("trainingDays"),autoAdvance=j.optBoolean("autoAdvance",true),timerSound=j.optString("timerSound","countdown").takeIf{it in listOf("off","countdown","every_second")}?:"countdown"
        )
    }
}
