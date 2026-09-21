package com.elevare.active

// Pure timing rule shared by the local coach and JVM boundary tests.
fun shouldBeep(mode:String,remaining:Int)=remaining>=0&&
    (mode=="every_second"||(mode=="countdown"&&remaining<=3))
