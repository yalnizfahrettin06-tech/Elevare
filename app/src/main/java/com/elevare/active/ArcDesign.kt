package com.elevare.active

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*

/** Original 24-unit line icons. Cached once, no downloaded icon/font dependency. */
object ArcIcons {
 private fun icon(name:String,data:String)=ImageVector.Builder(name,24.dp,24.dp,24f,24f).addPath(
  pathData=PathParser().parsePathString(data).toNodes(),fill=null,stroke=SolidColor(Color.Black),
  strokeLineWidth=1.7f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round
 ).build()
 val Mark=icon("Arc.Mark","M4,18 C4,9 8,4 17,4 M7,20 L17,7 M13,20 L20,10")
 val Sun=icon("Arc.Sun","M16,12 A4,4 0 1,1 8,12 A4,4 0 1,1 16,12 M12,2 L12,4 M12,20 L12,22 M2,12 L4,12 M20,12 L22,12 M5,5 L7,7 M17,17 L19,19 M5,19 L7,17 M17,7 L19,5")
 val Water=icon("Arc.Water","M12,2 C10,6 5,10 5,15 A7,7 0 0,0 19,15 C19,10 14,6 12,2 Z M9,15 C9,17 10,18 12,18")
 val Meal=icon("Arc.Meal","M4,3 L4,9 C4,13 10,13 10,9 L10,3 M7,3 L7,21 M18,3 C14,7 14,12 18,12 L20,12 M20,3 L20,21")
 val Home=icon("Arc.Home","M3,11 L12,4 L21,11 M5,10 L5,21 L10,21 L10,15 L14,15 L14,21 L19,21 L19,10")
 val Program=icon("Arc.Program","M5,5 L19,5 L19,21 L5,21 Z M8,3 L8,7 M16,3 L16,7 M5,10 L19,10 M8,14 L11,14 M8,18 L15,18")
 val Book=icon("Arc.Book","M12,6 C9,4 6,4 3,5 L3,20 C6,19 9,19 12,21 C15,19 18,19 21,20 L21,5 C18,4 15,4 12,6 Z M12,6 L12,21 M6,9 L9,10 M15,10 L18,9")
 val Person=icon("Arc.Person","M16,7 A4,4 0 1,1 8,7 A4,4 0 1,1 16,7 M4,21 C4,13 20,13 20,21")
 val Arrow=icon("Arc.Arrow","M4,12 L20,12 M14,6 L20,12 L14,18")
 val Chevron=icon("Arc.Chevron","M9,5 L16,12 L9,19")
 val Play=icon("Arc.Play","M8,4 L20,12 L8,20 Z")
 val Pause=icon("Arc.Pause","M8,5 L8,19 M16,5 L16,19")
 val Clock=icon("Arc.Clock","M21,12 A9,9 0 1,1 3,12 A9,9 0 1,1 21,12 M12,7 L12,12 L16,14")
 val Check=icon("Arc.Check","M5,12 L10,17 L20,6")
 val Moon=icon("Arc.Moon","M19,16 C10,18 6,10 10,3 C1,6 2,18 10,21 C15,23 20,19 21,15")
 val Spark=icon("Arc.Spark","M12,3 L14,10 L21,12 L14,14 L12,21 L10,14 L3,12 L10,10 Z")
 val Run=icon("Arc.Run","M16,4 A2,2 0 1,1 12,4 A2,2 0 1,1 16,4 M14,8 L10,13 L15,16 L17,21 M10,13 L8,18 L3,20 M12,10 L17,12 L21,11 M12,9 L8,7 L5,11")
 val Strength=icon("Arc.Strength","M3,8 L3,16 M6,5 L6,19 M18,5 L18,19 M21,8 L21,16 M6,12 L18,12")
 val Breath=icon("Arc.Breath","M3,8 L14,8 C20,8 20,2 16,3 M3,12 L19,12 C23,12 23,17 19,17 M3,16 L10,16 C16,16 16,22 12,21")
 val Progress=icon("Arc.Progress","M4,20 L4,13 M10,20 L10,9 M16,20 L16,4 M22,20 L22,11")
 val Sound=icon("Arc.Sound","M3,9 L7,9 L12,5 L12,19 L7,15 L3,15 Z M16,8 C19,10 19,14 16,16 M19,5 C24,9 24,15 19,19")
 val Settings=icon("Arc.Settings","M3,6 L9,6 M15,6 L21,6 M3,12 L15,12 M21,12 L21,12 M3,18 L6,18 M12,18 L21,18 M12,3 L12,9 M18,9 L18,15 M9,15 L9,21")
 val Shield=icon("Arc.Shield","M12,3 L21,7 L19,15 C17,19 14,21 12,22 C10,21 7,19 5,15 L3,7 Z M8,12 L11,15 L16,9")
 val Back=icon("Arc.Back","M20,12 L4,12 M10,6 L4,12 L10,18")
 val Close=icon("Arc.Close","M5,5 L19,19 M19,5 L5,19")
 val Info=icon("Arc.Info","M21,12 A9,9 0 1,1 3,12 A9,9 0 1,1 21,12 M12,11 L12,17 M12,7 L12,7.1")
 val Download=icon("Arc.Download","M12,3 L12,16 M7,11 L12,16 L17,11 M4,17 L4,21 L20,21 L20,17")
 val External=icon("Arc.External","M10,5 L4,5 L4,20 L19,20 L19,14 M13,3 L21,3 L21,11 M10,14 L21,3")
 val Up=icon("Arc.Up","M5,15 L12,8 L19,15")
 val Down=icon("Arc.Down","M5,9 L12,16 L19,9")
 val Refresh=icon("Arc.Refresh","M20,8 C17,1 5,2 3,11 M20,3 L20,8 L15,8 M4,16 C7,23 19,22 21,13 M4,21 L4,16 L9,16")
 val Bookmark=icon("Arc.Bookmark","M6,3 L18,3 L18,21 L12,17 L6,21 Z")
 val Saved=icon("Arc.Saved","M6,3 L18,3 L18,21 L12,17 L6,21 Z M9,9 L11,11 L15,7")
 val Heart=icon("Arc.Heart","M12,21 C9,18 2,13 2,7 C2,1 10,1 12,6 C14,1 22,1 22,7 C22,13 15,18 12,21 Z")
 val Favorite=icon("Arc.Favorite","M12,21 C9,18 2,13 2,7 C2,1 10,1 12,6 C14,1 22,1 22,7 C22,13 15,18 12,21 Z M8,11 L11,14 L17,8")
 val Mute=icon("Arc.Mute","M3,9 L7,9 L12,5 L12,19 L7,15 L3,15 Z M16,9 L22,15 M22,9 L16,15")
 val Next=icon("Arc.Next","M5,5 L16,12 L5,19 Z M20,5 L20,19")
 val Lock=icon("Arc.Lock","M6,10 L18,10 L18,21 L6,21 Z M8,10 L8,6 C8,1 16,1 16,6 L16,10 M12,14 L12,17")
 val History=icon("Arc.History","M3,10 C4,1 19,1 21,10 C24,20 9,25 5,18 M3,4 L3,10 L9,10 M12,7 L12,13 L16,15")
 val Delete=icon("Arc.Delete","M3,6 L21,6 M9,6 L9,3 L15,3 L15,6 M6,6 L7,21 L17,21 L18,6 M10,10 L10,17 M14,10 L14,17")
 val Circle=icon("Arc.Circle","M21,12 A9,9 0 1,1 3,12 A9,9 0 1,1 21,12")
 val Square=icon("Arc.Square","M4,4 L20,4 L20,20 L4,20 Z")
 val Checked=icon("Arc.Checked","M21,12 A9,9 0 1,1 3,12 A9,9 0 1,1 21,12 M7,12 L11,16 L17,8")
}

@Composable fun ArcBrand(modifier:Modifier=Modifier,compact:Boolean=false){
 Row(modifier,verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(9.dp)){
  Icon(ArcIcons.Mark,null,tint=Coral,modifier=Modifier.size(if(compact)24.dp else 30.dp))
  Column{
   Text("elevare",fontSize=if(compact)22.sp else 25.sp,fontWeight=FontWeight.ExtraBold,letterSpacing=(-1).sp)
   if(!compact)Text("T R A I N I N G  A R C",fontSize=8.sp,fontWeight=FontWeight.Medium,color=ArcMuted)
  }
 }
}

/** Decorative geometry is static. Pose alone runs its lifecycle-aware motion clock. */
@Composable fun ArcStage(id:String,modifier:Modifier=Modifier,reduced:Boolean=false,playing:Boolean=true,
 progress:Float?=null,side:String="",speed:Float=1f){
 Box(modifier.clip(RoundedCornerShape(22.dp)).background(Brush.verticalGradient(listOf(Track,Paper)))){
  Canvas(Modifier.matchParentSize().clearAndSetSemantics{}){
   val w=size.width;val h=size.height
   drawArc(Sky.copy(alpha=.14f),205f,235f,false,Offset(w*.22f,h*.05f),androidx.compose.ui.geometry.Size(w*.60f,h*.87f),style=Stroke(1.dp.toPx()))
   drawLine(Coral.copy(alpha=.25f),Offset(w*.12f,h*.82f),Offset(w*.89f,h*.82f),1.dp.toPx())
   for(i in 0..3){val x=w*(.09f+i*.052f);drawLine(ArcLine.copy(alpha=.5f),Offset(x,h*.66f),Offset(x+w*.13f,h*.39f),1.dp.toPx())}
   drawLine(Sky.copy(alpha=.42f),Offset(w*.85f,h*.13f),Offset(w*.91f,h*.13f),1.dp.toPx())
   drawLine(Sky.copy(alpha=.42f),Offset(w*.91f,h*.13f),Offset(w*.91f,h*.22f),1.dp.toPx())
  }
  Pose(id,Modifier.fillMaxSize().padding(horizontal=16.dp,vertical=8.dp),Ink,reduced,speed,playing,progress,side)
 }
}

@Composable fun ArcChapterStrip(day:Int,scheduled:Boolean=false){
 val phase=programPhase(day.coerceIn(1,90))
 val starts=listOf(1,15,29,50,78)
 val current=starts.indexOfLast{it<=day.coerceIn(1,90)}
 Column(verticalArrangement=Arrangement.spacedBy(9.dp),modifier=Modifier.clearAndSetSemantics{
  contentDescription=if(scheduled)"Program henüz başlamadı" else "90 günlük program. Gün $day. ${phase.title}"
 }){
  Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){
   Text(if(scheduled)"İLK BÖLÜMÜN HAZIR" else "BÖLÜM %02d".format(current+1),Modifier.weight(1f),fontSize=10.sp,letterSpacing=1.5.sp,color=Coral,fontWeight=FontWeight.Bold)
   Text(if(scheduled)"90 günlük yolculuk" else "$day / 90 gün",fontSize=12.sp,color=ArcMuted)
  }
  Row(horizontalArrangement=Arrangement.spacedBy(5.dp)){
   starts.forEachIndexed{index,_->Box(Modifier.weight(1f).height(3.dp).background(if(!scheduled&&index<=current)Coral else ArcLine,RoundedCornerShape(3.dp)))}
  }
 }
}

@Composable fun ArcSectionLead(kicker:String,title:String,detail:String?=null){
 Column(verticalArrangement=Arrangement.spacedBy(7.dp)){
  Eyebrow(kicker)
  Text(title,style=MaterialTheme.typography.headlineLarge,modifier=Modifier.semantics{heading()})
  if(detail!=null)QuietText(detail)
 }
}
