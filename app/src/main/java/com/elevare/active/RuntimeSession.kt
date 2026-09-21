package com.elevare.active

import android.os.SystemClock

// Monotonic time is runtime-only. Recovered snapshots are always paused.
fun remainingRuntime(a:ActiveSession):Int = if(!a.running) a.remaining else
 if(a.elapsedRealtimeDeadline>0) ((a.elapsedRealtimeDeadline-SystemClock.elapsedRealtime()+999)/1000).toInt().coerceAtLeast(0)
 else remaining(a)

fun advanceSessionRuntime(s:UserState):UserState {
 val a=s.active?:return s
 if(!a.running||remainingRuntime(a)>0)return s
 val w=activeWorkout(a)
 if(a.step>=w.steps.lastIndex)return s
 val step=w.steps[a.step+1]
 return s.copy(active=a.copy(step=a.step+1,elapsed=a.elapsed+w.steps[a.step].seconds,remaining=step.seconds,
  deadline=System.currentTimeMillis()+step.seconds*1000L,elapsedRealtimeDeadline=SystemClock.elapsedRealtime()+step.seconds*1000L))
}

fun completeSessionRuntime(s:UserState,a:ActiveSession,feeling:String):UserState =
 completeSession(s,a.copy(remaining=remainingRuntime(a),running=false,deadline=0,elapsedRealtimeDeadline=0),feeling)

fun abandonSessionRuntime(s:UserState):UserState {
 val a=s.active?:return s
 return abandonSession(s.copy(active=a.copy(remaining=remainingRuntime(a),running=false,deadline=0,elapsedRealtimeDeadline=0)))
}
