package com.elevare.active
import kotlin.math.*
fun main(){
 val bones=listOf(1 to 3,3 to 4,1 to 5,5 to 6,2 to 7,7 to 8,2 to 9,9 to 10)
 fun length(f:StickFrame,b:Pair<Int,Int>)=hypot(f.joints[b.first].x-f.joints[b.second].x,f.joints[b.first].y-f.joints[b.second].y)
 val first=motionFrame("sprint",0f)
 for(i in 0..1000){
  val f=motionFrame("sprint",i/1000f)
  check(f.joints.size==11)
  f.joints.forEach{check(it.x.isFinite()&&it.y.isFinite()&&it.x in 0f..100f&&it.y in 0f..100f){"Out of scene at $i: $it"}}
  bones.forEach{check(abs(length(f,it)-length(first,it))<.001f){"Changing bone at $i"}}
 }
 check(first==motionFrame("sprint",1f))
 check(motionFrame("sprint",Float.NaN)==first)
 Content.moves.forEach{m->
  check(motionFrame(m.id,0f)==motionFrame(m.id,1f))
  for(i in 0..100)motionFrame(m.id,i/100f).joints.forEach{check(it.x.isFinite()&&it.y.isFinite())}
 }
 val a=TrainingAnswers(18,"performance","unknown","8to10","regular",10,"clear")
 check(a.complete());check(!a.copy(age=22).complete());check(!a.copy(safety="").complete())
 check(routinePlan(a.copy(safety="pain")).blocked)
 check(PREPARATION_DURATION_MS==45000L)
 println("PASS: 1001 sprint frames, finite bounds, fixed limb lengths, loop seam, invalid phase.")
 println("PASS: 10 existing movement loops, required answers, safety block, 45 second preparation.")
 for(i in 0..7)println("FRAME|"+i+"|"+motionFrame("sprint",i/8f).joints.joinToString(";"){it.x.toString()+","+it.y})
}
