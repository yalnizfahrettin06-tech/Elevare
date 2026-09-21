package com.elevare.active

import org.junit.Assert.*
import org.junit.Test
import kotlin.math.hypot
import kotlin.math.abs

class StickGeometryTest {
    private val moves=listOf("sprint","walk","march","catcow","child","lunge","squat","wall",
        "calf","balance","step","reach","shoulder","breath")
    private val bones=listOf(1 to 2,0 to 1,1 to 3,3 to 4,1 to 5,5 to 6,2 to 7,7 to 8,2 to 9,9 to 10)
    private fun length(f:StickFrame,b:Pair<Int,Int>)=
        hypot(f.joints[b.first].x-f.joints[b.second].x,f.joints[b.first].y-f.joints[b.second].y)

    @Test fun fourteenMovesKeepEverySegmentLengthAcrossWholeCycle(){
        moves.forEach{id->
            val base=motionFrame(id,0f)
            for(frame in 0..300){
                val f=motionFrame(id,frame/300f)
                assertEquals(id,11,f.joints.size)
                bones.forEach{b->assertEquals("$id bone $b frame $frame",length(base,b),length(f,b),.001f)}
                f.joints.forEachIndexed{i,p->
                    assertTrue("$id joint $i x="+p.x,p.x.isFinite()&&p.x in 0f..100f)
                    assertTrue("$id joint $i y="+p.y,p.y.isFinite()&&p.y in 0f..100f)
                }
            }
        }
    }
    @Test fun loopsJoinAndDoNotTeleport(){
        moves.forEach{id->
            assertEquals(id,motionFrame(id,0f),motionFrame(id,1f))
            val end=motionFrame(id,.99999f);val start=motionFrame(id,.00001f)
            end.joints.zip(start.joints).forEach{(a,b)->
                assertTrue("$id seam",hypot(a.x-b.x,a.y-b.y)<.05f)
            }
        }
    }
    @Test fun walkingHasNoArtificialFlightAndRunningDoes(){
        var runFlight=0
        for(i in 0..1000){
            val walk=runningFrame(i/1000f,true)
            assertTrue(walk.frontContact||walk.rearContact)
            val run=runningFrame(i/1000f)
            if(!run.frontContact&&!run.rearContact)runFlight++
            listOf(walk,run).forEach{f->
                if(f.frontContact)assertEquals(94f,f.joints[8].y,.001f)
                if(f.rearContact)assertEquals(94f,f.joints[10].y,.001f)
            }
        }
        assertTrue(runFlight>100)
    }
    @Test fun stanceFeetAreLockedToTheDisplayedTrack(){
        for(walking in listOf(false,true)){
            val a=runningFrame(.1f,walking);val b=runningFrame(.2f,walking)
            assertEquals(a.joints[8].x+a.groundOffset,b.joints[8].x+b.groundOffset,.001f)
        }
    }
    @Test fun frontArmOpposesFrontLeg(){
        for(c in listOf(0f,.25f)){
            val f=runningFrame(c)
            val armX=f.joints[3].x-f.joints[1].x
            val legX=f.joints[8].x-f.joints[2].x
            assertTrue(armX*legX<0f)
        }
    }
    @Test fun yogaHasAnActualStillHoldAndSeparateEntranceExit(){
        for(id in listOf("child","lunge","balance","reach")){
            assertEquals(motionFrame(id,.3f,.3f),motionFrame(id,.7f,.7f))
            assertNotEquals(motionFrame(id,.05f,.05f).joints,motionFrame(id,.5f,.5f).joints)
            assertEquals(motionFrame(id,0f,0f).joints,motionFrame(id,1f,1f).joints)
        }
        assertEquals(0f,holdAmount(0f),0f)
        assertEquals(1f,holdAmount(.5f),0f)
        assertEquals(0f,holdAmount(1f),0f)
    }
    @Test fun splitYogaPhasesJoinWithoutStartingAnotherPump(){
        assertEquals(techniqueProgress("prepare",1f),techniqueProgress("main",0f),.001f)
        assertEquals(techniqueProgress("main",1f),techniqueProgress("exit",0f),.001f)
        assertEquals(1f,holdAmount(techniqueProgress("main",.1f)),.001f)
        assertEquals(1f,holdAmount(techniqueProgress("main",.9f)),.001f)
    }
    @Test fun sidesAreExplicitAndMirrorGeometry(){
        for(id in listOf("lunge","balance")){
            val left=motionFrame(id,.5f,.5f,"left")
            val right=motionFrame(id,.5f,.5f,"right")
            left.joints.zip(right.joints).forEach{(a,b)->
                assertEquals(100f,a.x+b.x,.001f);assertEquals(a.y,b.y,.001f)
            }
            assertTrue(right.stage.contains("Sağ"))
        }
    }
    @Test fun wallHandsStayAtTheWall(){
        for(i in 0..100){
            val f=motionFrame("wall",i/100f)
            assertEquals(82f,f.joints[4].x,.001f)
            assertEquals(82f,f.joints[6].x,.001f)
        }
    }
    @Test fun runningAndMarchingAreNotTheSameScene(){
        assertNotEquals(motionFrame("sprint",.3f).joints,motionFrame("walk",.3f).joints)
        assertEquals(0f,motionFrame("march",.3f).groundOffset,0f)
        assertNotEquals(motionDuration("sprint"),motionDuration("walk"))
        assertEquals(8,sprintFrames().size)
    }
    @Test fun invalidCycleIsFiniteAndClampedHelpersStayCompatible(){
        moves.forEach{id->
            assertEquals(motionFrame(id,0f),motionFrame(id,Float.NaN))
            assertEquals(stickFrame(id,0f),stickFrame(id,-1f))
            assertEquals(stickFrame(id,1f),stickFrame(id,2f))
        }
    }
}

