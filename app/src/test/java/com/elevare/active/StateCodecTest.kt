package com.elevare.active
import org.junit.Assert.*
import org.junit.Test
import org.json.JSONObject

class StateCodecTest {
 @Test fun newFieldsRoundTrip(){
  val s=UserState(ready=true,start="2026-09-08",age=16,runningExperience="new",equipment=setOf("chair","wall"),
   dailyCheckDate="2026-09-08",dailyEquipmentConfirmed=true,workoutReminders=true,workoutReminderTime="18:20",sleepReminderLeadMinutes=60,
   savedFacts=setOf("fact1"),factHistory=mapOf("2026-09-08" to "fact1"),factSeenAt=mapOf("fact1" to "2026-09-08"),recentFactIds=listOf("fact1"),
   demoStartedAt=1_000,archivedCycles=listOf(ProgramCycleArchive("a","2026-01-01","2026-03-31",0,8,4,30,51,24)),
   progressionConsents=listOf(ProgressionConsent("proposal","cycle",2,listOf("s1","s2"),"profile","review",1_000)))
  val decoded=StateCodec.decode(StateCodec.encode(s))
  assertEquals(s,decoded)
 }
 @Test fun draftPreservesQuestionAndAnswers(){
  val answers=TrainingAnswers(age=16,focus="growth",activity="new",runningExperience="new")
  val draft=OnboardingDraft(answers,5,0,8)
  val s=UserState(start="2026-09-08",onboardingDraft=draft)
  assertEquals(draft,StateCodec.decode(StateCodec.encode(s)).onboardingDraft)
 }
 @Test fun badDraftCannotSkipQuestions(){
  val s=UserState(start="2026-09-08",onboardingDraft=OnboardingDraft(TrainingAnswers(),13,30_000,8))
  assertEquals(1,StateCodec.decode(StateCodec.encode(s)).onboardingDraft!!.step)
 }
 @Test fun restoredSnapshotAlwaysPaused(){
  val w=Workout("private-versioned","Saved","Immutable","Hazırlık",listOf(Step("walk",60,phase="warmup")))
  val a=ActiveSession(w.id,remaining=45,deadline=80_000,running=true,snapshot=w,elapsedRealtimeDeadline=500)
  val decoded=StateCodec.decode(StateCodec.encode(UserState(start="2026-09-08",active=a)){45})
  assertFalse(decoded.active!!.running);assertEquals(45,decoded.active!!.remaining)
  assertEquals(0L,decoded.active!!.elapsedRealtimeDeadline);assertEquals(w,activeWorkout(decoded.active!!))
 }
 @Test fun legacyDataRetainsHistoryButDropsUnusedHeight(){
  val old="""{"version":7,"ready":true,"start":"2026-01-01","heightCm":165,"targetCm":185,
   "sessions":[{"id":"old","title":"Old title","date":"2026-01-02","seconds":60,"feeling":"İyi"}]}"""
  val decoded=StateCodec.decode(old)
  assertEquals("legacy:2026-01-01",decoded.cycleId);assertEquals("old",decoded.sessions.single().id)
  assertEquals(0,decoded.heightCm);assertFalse(JSONObject(StateCodec.encode(decoded)).has("heightCm"))
 }
 @Test fun unknownSchemaFailsWithoutSilentReset(){
  assertTrue(runCatching{StateCodec.decode("""{"version":99,"start":"2026-09-08"}""")}.isFailure)
  assertTrue(runCatching{StateCodec.decode("{broken")}.isFailure)
 }
 @Test fun invalidDateFailsWithoutDroppingHistory(){
  assertTrue(runCatching{StateCodec.decode("""{"version":8,"start":"not-a-date","sessions":[]}""")}.isFailure)
 }
 @Test fun partialSessionNotCompletion(){
  val a=ActiveSession("breath",remaining=80,running=false,planDay=3)
  val s=abandonSession(UserState(start="2026-09-08",active=a))
  assertEquals(40,s.sessions.single().seconds);assertFalse(s.sessions.single().completed)
  assertTrue(s.done.isEmpty());assertNull(s.active)
  assertEquals(s.sessions,StateCodec.decode(StateCodec.encode(s)).sessions)
 }
 @Test fun trialExactly72HoursAndCannotRestart(){
  val s=startDemo(UserState(),1_000)
  assertEquals(72*60*60*1000L,demoRemainingMillis(s,1_000))
  assertEquals(0L,demoRemainingMillis(s,1_000+72*60*60*1000L))
  assertEquals(s,startDemo(s,999_000))
 }
 @Test fun reminderLeadDefaultsAndInvalidValuesMigrateSafely(){
  val old="""{"version":7,"ready":true,"start":"2026-01-01"}"""
  assertEquals(30,StateCodec.decode(old).sleepReminderLeadMinutes)
  assertTrue(StateCodec.decode(old).progressionConsents.isEmpty())
  listOf(-1,0,45,999).forEach{invalid->
   assertEquals(30,StateCodec.decode(JSONObject(old).put("sleepReminderLeadMinutes",invalid).toString()).sleepReminderLeadMinutes)
  }
  listOf(15,30,60).forEach{lead->
   assertEquals(lead,StateCodec.decode(StateCodec.encode(UserState(start="2026-01-01",sleepReminderLeadMinutes=lead))).sleepReminderLeadMinutes)
  }
 }
 @Test fun malformedProgressionConsentDoesNotSilentlyBecomeAuthorization(){
  val valid=ProgressionConsent("proposal","cycle",2,listOf("s1","s2"),"profile","review",1_000)
  val original=StateCodec.encode(UserState(start="2026-01-01",progressionConsents=listOf(valid)))
  val mutations:List<(JSONObject)->Unit> = listOf(
   {it.put("level",0)}, {it.put("profileKey","")}, {it.put("acceptedAtEpochMs",0)},
   {it.put("evidenceSessionIds",org.json.JSONArray(listOf("same","same")))},
   {it.remove("reviewRecord")}
  )
  mutations.forEach{mutate->
   val corrupt=JSONObject(original)
   mutate(corrupt.getJSONArray("progressionConsents").getJSONObject(0))
   assertTrue(runCatching{StateCodec.decode(corrupt.toString())}.isFailure)
  }
 }
 @Test fun equipmentMetadataDescribesActualMoves(){
  assertEquals(setOf("mat"),Workout("y","Yoga","","Yoga",listOf(Step("child",60))).requiredEquipment)
  assertEquals(setOf("chair"),Workout("g","Güç","","Güç",listOf(Step("squat",30))).requiredEquipment)
  val support=Workout("g","Denge","","Güç",listOf(Step("balance",30),Step("calf",30)))
  assertEquals(setOf("support"),support.requiredEquipment)
  assertEquals("Sabit sandalye veya duvar",support.equipment)
  assertEquals(setOf("chair"),support.copy(steps=support.steps+Step("squat",30)).requiredEquipment)
  assertEquals(setOf("wall"),support.copy(steps=support.steps+Step("wall",30)).requiredEquipment)
  assertEquals("Duvar",support.copy(steps=support.steps+Step("wall",30)).equipment)
 }
}
