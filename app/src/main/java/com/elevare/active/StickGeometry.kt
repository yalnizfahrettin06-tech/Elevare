package com.elevare.active
import kotlin.math.*

fun sprintFrames():List<StickFrame> = listOf(
 frame(61,16,57,28,47,55,43,31,48,18,66,43,77,36,66,56,76,73,33,75,20,83),
 frame(58,13,54,25,47,52,41,37,31,31,66,30,72,18,53,74,46,92,39,66,24,62),
 frame(61,16,57,28,47,55,66,43,77,36,43,31,48,18,33,75,20,83,66,56,76,73),
 frame(58,13,54,25,47,52,66,30,72,18,41,37,31,31,39,66,24,62,53,74,46,92)
)
fun motionDuration(id:String)=when(id){"sprint"->1800;"walk"->2400;"march"->2800;"squat","wall"->4800;"calf","reach"->4000;else->3600}
fun motionFrame(id:String,cycle:Float):StickFrame{
 val t=if(cycle.isFinite())cycle.coerceIn(0f,1f) else 0f
 if(id!="sprint"){
  val phase=when {t<.12f->0f;t<.45f->(t-.12f)/.33f;t<.57f->1f;t<.90f->1f-(t-.57f)/.33f;else->0f}
  return stickFrame(id,phase)
 }
 val frames=sprintFrames()
 val at=(t%1f)*frames.size
 val index=at.toInt().coerceAtMost(frames.lastIndex)
 val a=frames[index];val z=frames[(index+1)%frames.size];val blend=at-index
 val result=MutableList(11){Joint(0f,0f)}
 result[2]=Joint(a.joints[2].x+(z.joints[2].x-a.joints[2].x)*blend,a.joints[2].y+(z.joints[2].y-a.joints[2].y)*blend)
 // Rotate fixed-length segments so arms and legs do not shrink during the stride.
 for((parent,child) in listOf(2 to 1,1 to 0,1 to 3,3 to 4,1 to 5,5 to 6,2 to 7,7 to 8,2 to 9,9 to 10)){
  fun angle(f:StickFrame)=atan2(f.joints[child].y-f.joints[parent].y,f.joints[child].x-f.joints[parent].x)
  val length=frames.map{f->hypot(f.joints[child].x-f.joints[parent].x,f.joints[child].y-f.joints[parent].y)}.average().toFloat()
  val start=angle(a);val delta=atan2(sin(angle(z)-start),cos(angle(z)-start));val theta=start+delta*blend
  val root=result[parent];result[child]=Joint(root.x+cos(theta)*length,root.y+sin(theta)*length)
 }
 return StickFrame(result)
}
fun motionCue(id:String)=when(id){
 "sprint"->"Karşı kol · karşı bacak"
 "squat"->"Yavaş otur · kontrollü kalk"
 "wall"->"Eller duvarda · kontrollü it"
 "calf"->"Desteği tut · topukları kaldır"
 "balance"->"Desteğe yakın kal"
 "reach"->"Rahatça uzan · geri dön"
 "shoulder"->"Küçük ve yavaş hareket"
 else->"Kendi hızında hareket et"
}


data class Joint(val x:Float,val y:Float)
// Head, neck, hip, left elbow/hand, right elbow/hand, left knee/foot, right knee/foot.
data class StickFrame(val joints:List<Joint>,val support:String="")
private fun frame(vararg xy:Int,support:String="")=StickFrame(xy.toList().chunked(2).map{Joint(it[0].toFloat(),it[1].toFloat())},support)
private val standing=frame(50,16,50,28,50,57,38,41,34,53,62,41,66,53,40,74,35,94,60,74,65,94)
fun stickFrames(id:String):Pair<StickFrame,StickFrame> = when(id){
 "march","walk"->frame(50,16,50,28,50,57,35,36,26,48,65,42,71,29,32,68,32,86,62,75,70,94) to frame(50,16,50,28,50,57,35,42,29,29,65,36,74,48,38,75,30,94,68,68,68,86)
 "shoulder"->standing to frame(50,16,50,28,50,57,32,30,24,43,68,30,76,43,40,74,35,94,60,74,65,94)
 "squat"->frame(47,16,47,28,47,57,32,37,22,35,62,37,72,35,38,75,35,94,59,75,65,94,support="chair") to frame(44,35,46,47,58,68,29,50,17,49,62,50,74,49,35,69,35,94,73,69,75,94,support="chair")
 "wall"->frame(38,17,39,29,32,60,58,33,80,33,58,42,80,42,27,76,22,94,40,76,45,94,support="wall") to frame(55,20,53,32,37,61,63,40,80,33,64,49,80,42,30,77,22,94,41,77,45,94,support="wall")
 "step"->standing to frame(45,16,45,28,45,57,32,41,24,51,60,39,69,49,30,74,17,94,66,75,82,94)
 "balance"->frame(49,16,49,28,49,57,30,34,19,29,66,33,82,29,41,75,36,94,59,75,65,94,support="rail") to frame(49,16,49,28,49,57,30,34,19,29,66,33,82,29,41,75,36,94,64,62,59,78,support="rail")
 "reach"->standing to frame(50,19,50,31,50,59,36,19,30,5,64,19,70,5,40,77,35,94,60,77,65,94)
 "calf"->frame(49,19,49,31,49,60,34,43,29,55,66,31,82,29,40,78,36,94,61,78,65,94,support="rail") to frame(49,14,49,26,49,55,34,38,29,50,66,29,82,29,40,73,36,89,61,73,65,89,support="rail")
 "breath"->frame(50,18,50,30,50,60,35,44,37,59,65,44,63,59,32,74,24,88,68,74,76,88) to frame(50,18,50,30,50,60,34,43,36,58,66,43,64,58,32,74,24,88,68,74,76,88)
 else->standing to standing
}
fun stickFrame(id:String,phase:Float):StickFrame {
 val (a,b)=stickFrames(id);val t=phase.coerceIn(0f,1f)
 return StickFrame(a.joints.zip(b.joints){p,q->Joint(p.x+(q.x-p.x)*t,p.y+(q.y-p.y)*t)},a.support)
}
