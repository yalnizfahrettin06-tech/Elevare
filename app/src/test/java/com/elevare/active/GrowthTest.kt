package com.elevare.active
import org.junit.Assert.*
import org.junit.Test
class GrowthTest{
 private fun validState()=UserState(ready=true,age=17,focus="performance",recentGrowth="unknown",sleepHabit="8to10",activityHabit="new",dailyMinutes=5,safety="clear",environment="indoor",trainingDays=3,runningExperience="new",equipment=setOf("none"))
 @Test fun onlyThreeDayTrial(){assertEquals(3,TRIAL_DAYS);assertEquals(3,normalizeTrialDays(7));assertEquals(3,normalizeTrialDays(3));assertEquals(0,normalizeTrialDays(0));assertEquals(0,normalizeTrialDays(-1));assertEquals(11,ONBOARDING_STEPS)}
 @Test fun quickStart(){val w=buildVersionedWorkout("P",5);val clockDate=java.time.Instant.ofEpochMilli(1000).atZone(java.time.ZoneId.systemDefault()).toLocalDate();val s=beginWorkout(validState().copy(start=clockDate.toString()),w.id,1000);assertEquals(w.id,s.active!!.workoutId);assertEquals(51000L,s.active!!.deadline);assertEquals(50,s.active!!.remaining)}
 @Test fun quickStartPreservesExistingSession(){val first=beginWorkout(validState(),buildVersionedWorkout("P",5).id);assertNotNull(first.active);assertEquals(first,beginWorkout(first,"breath"))}
 @Test fun pausedPlanCannotStart(){val paused=UserState(safety="clear",pausedOn=java.time.LocalDate.now().toString());assertEquals(paused,beginWorkout(paused,"runprep"));assertNull(resumePlan(paused).pausedOn)}
 @Test fun unknownWorkoutDoesNotStart(){val s=UserState(safety="clear");assertEquals(s,beginWorkout(s,"missing"))}
 @Test fun optionalHeights(){assertTrue(validHeight(""));assertTrue(validHeight("175"));assertFalse(validHeight("0"));assertFalse(validHeight("abc"));assertFalse(validHeight("999"))}
 @Test fun researchIdentity(){assertEquals(6,Research.articles.size);assertEquals(6,Research.articles.map{it.id}.toSet().size);Research.articles.forEach{assertTrue(it.pmid.matches(Regex("[0-9]{7,8}")));assertTrue(it.limit.isNotBlank());assertTrue(it.population.isNotBlank())}}
 @Test fun preparationNotMaximalSprint(){val w=Content.workout("runprep");assertEquals(300,w.seconds);assertTrue(w.steps.all{it.moveId in Content.moves.map{m->m.id}});assertTrue(w.subtitle.contains("Maksimal koşu içermez"))}
 @Test fun migrationDefaults(){val s=UserState(safety="clear");assertEquals(0,s.onboardingVersion);assertEquals(0,s.heightCm);assertEquals(0,s.targetCm);assertFalse(s.reminders);assertEquals(0,s.trialDays)}
}
