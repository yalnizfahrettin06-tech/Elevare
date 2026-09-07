package com.elevare.active
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
class ModelTest {
 @Test fun overnightSleep(){assertEquals(540,sleepDuration("22:00","07:00"));assertEquals(0,sleepDuration("07:00","07:00"));assertEquals(-1,sleepDuration("x","y"))}
 @Test fun pausedCalendar(){val s=UserState(start="2026-09-01",pausedOn="2026-09-04");assertEquals(4,journeyDay(s,LocalDate.parse("2026-09-10")));assertEquals(4,journeyDay(s.copy(pausedOn=null,pausedDays=6),LocalDate.parse("2026-09-10")))}
 @Test fun bounds(){assertEquals(90,journeyDay(UserState(start="2020-01-01")));assertEquals(1,journeyDay(UserState(start="2026-09-10"),LocalDate.parse("2026-09-01")))}
 @Test fun timer(){assertEquals(2,remaining(ActiveSession("start",deadline=2200),201));assertEquals(0,remaining(ActiveSession("start",deadline=200),400));assertEquals(12,remaining(ActiveSession("start",remaining=12,running=false),99999))}
 @Test fun contentValid(){assertEquals(8,Content.workouts.size);Content.workouts.forEach{w->assertTrue(w.seconds>0);w.steps.forEach{assertTrue(it.seconds>0);assertNotNull(Content.move(it.moveId))}}}
 @Test fun cannotCompleteEarly(){val a=ActiveSession("start",remaining=0,running=false);val s=UserState(active=a);assertEquals(s,completeSession(s,a,"İyi"))}
 @Test fun completeOnce(){val a=ActiveSession("breath",remaining=0,running=false,planDay=7);val s=completeSession(UserState(active=a),a,"İyi");assertEquals(1,s.sessions.size);assertEquals(120,s.sessions.first().seconds);assertTrue("breath" in s.done["plan:7"]!!);assertEquals(1,completeSession(s,a,"İyi").sessions.size);assertNull(s.active)}
 @Test fun discomfortLightens(){val a=ActiveSession("breath",remaining=0,running=false);assertTrue(completeSession(UserState(active=a),a,"Rahatsızlık").gentle)}
 @Test fun manualTasksAreNotSessions(){val s=markDone(UserState(),"move");assertTrue(s.sessions.isEmpty());assertTrue("move" in s.done["plan:1"]!!);assertTrue(unmark(s,"move").done["plan:1"]!!.isEmpty())}
}
