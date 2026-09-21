package com.elevare.active

import org.junit.Assert.*
import org.junit.Test
import java.time.*

class LivingTest {
 @Test fun malformedLifeDataIsNotSilentlyDropped(){assertTrue(runCatching{LifeCodec.decode(org.json.JSONObject("""{"plans":"broken"}"""))}.isFailure)}
 private val day=LocalDate.of(2026,9,21)
 private val now=day.atTime(16,30).atZone(ZoneId.of("Europe/Istanbul"))
 private fun life()=addRoutine(LifeState(),"morning")
 @Test fun newUsersHaveNoAssignedRoutines(){assertTrue(LifeState().plans.isEmpty());assertFalse(LifeState().welcomed)}
 @Test fun unknownRoutineIsIgnored(){assertEquals(LifeState(),addRoutine(LifeState(),"invalid"))}
 @Test fun addIsUniqueAndOptIn(){val l=addRoutine(life(),"morning");assertEquals(1,l.plans.size);assertTrue(l.welcomed);assertFalse(l.plans[0].remind)}
 @Test fun completionIsIdempotent(){val l=recordRoutine(life(),"morning","priority","done",day);assertEquals(l,recordRoutine(l,"morning","priority","done",day));assertEquals(1,lifeWeekCount(l,day))}
 @Test fun skipPreservesCompletedSteps(){val l=skipRoutine(recordRoutine(life(),"morning","priority","done",day),"morning",day);assertTrue(routineRecorded(l,"morning",day));assertEquals(1,routineDoneCount(l,"morning",day))}
 @Test fun undoRemovesOnlySelectedStep(){val l=recordRoutine(skipRoutine(life(),"morning",day),"morning","priority",null,day);assertFalse(routineRecorded(l,"morning",day));assertEquals("skip",routineStatus(l,"morning","ready",day))}
 @Test fun deletePreservesOtherRoutines(){val l=skipRoutine(addRoutine(life(),"water"),"water",day);val deleted=deleteRoutine(l,"morning");assertEquals(1,deleted.plans.size);assertTrue(routineRecorded(deleted,"water",day))}
 @Test fun homeNeverShowsMoreThanTwo(){val l=LifeCatalog.all.fold(LifeState()){s,t->addRoutine(s,t.id)};assertEquals(2,visibleRoutines(l,day,9).size);assertEquals("morning",visibleRoutines(l,day,9)[0].id)}
 @Test fun offAndWrongDaysDoNotSchedule(){assertNull(nextRoutineAt(LifeRoutinePlan("water"),now));assertFalse(routineDue(LifeRoutinePlan("water",days=setOf(2)),day))}
 @Test fun nextAlarmIsFutureAndOnSelectedDay(){val p=LifeRoutinePlan("water",days=setOf(1),time="16:30",remind=true);assertEquals(day.plusDays(7),nextRoutineAt(p,now)!!.toLocalDate())}
 @Test fun commonBudgetAndSpacing(){val t=now.toInstant().toEpochMilli();assertTrue(deliveryBudgetAllows(0,0,t,2));assertFalse(deliveryBudgetAllows(2,0,t,2));assertFalse(deliveryBudgetAllows(1,t-1000,t,2));assertTrue(deliveryBudgetAllows(1,t-10800000,t,2));assertFalse(deliveryBudgetAllows(0,t+1000,t,2))}
 @Test fun quietRangeCrossesMidnight(){val l=LifeState(quietEnabled=true,quietStart="21:00",quietEnd="06:00");assertTrue(lifeQuiet(l,LocalTime.of(23,0)));assertFalse(lifeQuiet(l,LocalTime.NOON))}
 @Test fun oldAndChangedAlarmsAreDiscarded(){val p=LifeRoutinePlan("water",time="16:30",remind=true);val s=UserState(ready=true,onboardingVersion=TRAINING_ONBOARDING_VERSION,life=LifeState(plans=listOf(p)));val epoch=now.toInstant().toEpochMilli();assertTrue(canDeliverLife(s,"water",1,epoch,now));assertFalse(canDeliverLife(s,"water",2,epoch,now));assertFalse(canDeliverLife(s,"water",1,epoch-5400001,now));assertFalse(canDeliverLife(s,"water",1,epoch+1,now))}
 @Test fun completedRoutineDoesNotNotify(){val p=LifeRoutinePlan("water",remind=true);val l=skipRoutine(LifeState(plans=listOf(p)),"water",day);assertFalse(canDeliverLife(UserState(ready=true,onboardingVersion=TRAINING_ONBOARDING_VERSION,life=l),"water",1,now.toInstant().toEpochMilli(),now))}
 @Test fun codecRoundTripAndLegacy(){val l=skipRoutine(life(),"morning",day).copy(reflections=mapOf(reflectionWeek(day) to WeekReflection("right","keep")));assertEquals(l,LifeCodec.decode(LifeCodec.encode(l)));assertEquals(LifeState(),LifeCodec.decode(null))}
 @Test fun weekDoesNotCountFutureOrSkipped(){val l=recordRoutine(skipRoutine(life(),"morning",day),"morning","priority","done",day.plusDays(1));assertEquals(0,lifeWeekCount(l,day))}
}
