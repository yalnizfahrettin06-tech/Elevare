package com.elevare.active
import kotlin.math.*

private val TAU=(2.0*PI).toFloat()
private fun smooth(t:Float)=t*t*(3f-2f*t)
fun sprintFrames():List<StickFrame> = (0..3).map{runningFrame(it/4f)}
fun motionDuration(id:String)=when(id){"sprint"->820;"walk","march"->1600;"catcow"->8000;"child"->10000;"lunge"->10000;"squat","wall"->4500;else->5000}
fun motionFrame(id:String,cycle:Float):StickFrame{
 val t=if(cycle.isFinite())((cycle%1f)+1f)%1f else 0f
 if(id in listOf("sprint","walk","march"))return runningFrame(t,id!="sprint")
 val phase=(1f-cos(t*TAU))/2f
 return stickFrame(id,smooth(phase))
}
// A continuous two-contact stride. Feet follow a grounded stance then a lifted swing.
// Inverse kinematics keeps thigh/shin lengths constant, including between rendered frames.
fun runningFrame(t:Float,walking:Boolean=false):StickFrame{
 val cycle=t%1f;val a=cycle*TAU
 val hip=Joint(48f,54f-1.8f*sin(a*2f)*sin(a*2f))
 val neck=Joint(hip.x+if(walking)2f else 6f,hip.y-26f)
 val head=Joint(neck.x+1f,neck.y-12f)
 fun end(root:Joint,length:Float,angle:Float)=Joint(root.x+length*cos(angle),root.y+length*sin(angle))
 fun leg(offset:Float):Pair<Joint,Joint>{
  val phase=(cycle+offset)%1f
  val stance=if(walking).6f else .42f
  val foot=if(phase<stance){
   Joint(61f-29f*(phase/stance),94f)
  }else{
   val u=(phase-stance)/(1f-stance)
   Joint(32f+29f*smooth(u),94f-(if(walking)13f else 31f)*sin(PI.toFloat()*u))
  }
  val dx=foot.x-hip.x;val dy=foot.y-hip.y
  val distance=hypot(dx,dy).coerceIn(.01f,45.99f)
  val h=sqrt((23f*23f-distance*distance/4f).coerceAtLeast(0f))
  val knee=Joint(hip.x+dx/2f+dy/distance*h,hip.y+dy/2f-dx/distance*h)
  return knee to foot
 }
 fun arm(offset:Float):Pair<Joint,Joint>{
  val angle=1.55f+sin(a+offset)*if(walking).45f else .85f
  val elbow=end(neck,16f,angle)
  val hand=end(elbow,15f,angle-1.8f)
  return elbow to hand
 }
 val frontLeg=leg(0f);val rearLeg=leg(.5f)
 val frontArm=arm(PI.toFloat());val rearArm=arm(0f)
 return StickFrame(listOf(head,neck,hip,frontArm.first,frontArm.second,rearArm.first,rearArm.second,frontLeg.first,frontLeg.second,rearLeg.first,rearLeg.second))
}
fun motionCue(id:String)=when(id){
 "sprint"->"Rahat omuzlar · karşı kol ve bacak"
 "catcow"->"Nefes ver, yuvarlan · nefes al, açıl"
 "child"->"Yavaşça geriye otur · rahat nefes al"
 "lunge"->"Gövdeni uzat · ortada taraf değiştir"
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
 "catcow"->frame(78,53,68,59,37,64,69,76,70,94,64,77,64,94,36,94,20,94,42,94,26,94) to frame(77,63,67,57,36,58,68,75,70,94,62,76,64,94,36,94,20,94,42,94,26,94)
 "child"->frame(69,65,59,70,33,78,73,82,87,93,66,83,81,93,41,94,20,94,36,94,16,94) to frame(65,72,54,76,29,82,70,87,87,93,63,87,81,93,41,94,20,94,36,94,16,94)
 "lunge"->frame(51,25,49,37,46,66,61,50,70,63,39,53,43,68,72,72,78,94,26,94,12,94) to frame(56,24,54,36,51,66,66,49,74,62,44,52,48,67,73,72,78,94,26,94,12,94)
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
