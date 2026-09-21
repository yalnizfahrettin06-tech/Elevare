package com.elevare.active

import java.time.LocalDate
import java.time.temporal.ChronoUnit

fun beginWorkout(s:UserState,id:String,now:Long=System.currentTimeMillis()):UserState {
 if(s.active!=null||s.pausedOn!=null||!trainingAllowed(s))return s
 val w=runCatching{Content.workout(id)}.getOrNull()?:return s
 if(programStartBlockReason(s,w,now)!=null)return s
 val today=java.time.Instant.ofEpochMilli(now).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
 val already=s.sessions.any{it.date==today.toString()&&it.type=="workout"&&it.completed}
 if(already&&w.id!="breath")return s
 return s.copy(active=ActiveSession(w.id,remaining=w.steps.first().seconds,deadline=now+w.steps.first().seconds*1000L,
  startedDate=today.toString(),planDay=journeyDay(s,today),snapshot=w))
}
fun resumePlan(s:UserState,today:LocalDate=LocalDate.now()):UserState=if(s.pausedOn==null)s else s.copy(
 pausedDays=s.pausedDays+ChronoUnit.DAYS.between(LocalDate.parse(s.pausedOn),today).coerceAtLeast(0),pausedOn=null)
fun demoRemainingMillis(s:UserState,now:Long=System.currentTimeMillis()):Long =
 if(s.demoStartedAt<=0)0 else (s.demoStartedAt+72*60*60*1000L-now).coerceIn(0,72*60*60*1000L)
fun startDemo(s:UserState,now:Long=System.currentTimeMillis())=
 if(s.trialDays>0||s.demoStartedAt>0)s else s.copy(trialDays=3,demoStartedAt=now)
