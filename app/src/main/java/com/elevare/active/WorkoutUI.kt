package com.elevare.active

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable fun WeekStrip(week:List<ProgramDay>,current:Int,start:LocalDate?=null){
 Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(6.dp)){
  week.forEach{d->
   val selected=d.index==current
   val label=start?.plusDays(d.index.toLong())?.format(DateTimeFormatter.ofPattern("EE",Locale.forLanguageTag("tr")))?:("${d.index+1}")
   Column(Modifier.weight(1f).background(if(selected)Mint else Color.Transparent,RoundedCornerShape(12.dp)).padding(vertical=9.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(6.dp)){
    Text(label,fontSize=11.sp,color=if(selected)Ink else MaterialTheme.colorScheme.onSurfaceVariant,fontWeight=FontWeight.Bold)
    Box(Modifier.size(6.dp).background(if(d.kind=="run")Coral else if(d.training)Sky else MaterialTheme.colorScheme.outlineVariant,CircleShape))
   }
  }
 }
}
@Composable fun WorkoutHero(w:Workout,reduced:Boolean=false,label:String="BUGÜNÜN ANTRENMANI",button:String="Antrenmana başla",onStart:()->Unit,onGuide:()->Unit){
 Surface(shape=RoundedCornerShape(24.dp),color=Track,border=BorderStroke(1.dp,MaterialTheme.colorScheme.outlineVariant)){
  Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
   Row(verticalAlignment=Alignment.CenterVertically){Text(label,Modifier.weight(1f),fontSize=10.sp,letterSpacing=1.1.sp,color=Sky,fontWeight=FontWeight.Bold);Icon(Icons.Rounded.Bolt,null,tint=Coral,modifier=Modifier.size(20.dp))}
   Row(verticalAlignment=Alignment.CenterVertically){
    Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(8.dp)){
     Text(w.title,fontSize=28.sp,lineHeight=31.sp,fontWeight=FontWeight.ExtraBold)
     Text(minutesText(w.seconds),fontSize=20.sp,color=Coral,fontWeight=FontWeight.Bold)
    }
    Pose(w.heroMove(),Modifier.size(135.dp),Ink,reduced)
   }
   Text(w.dayBrief(),fontSize=13.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)
   BigButton(button,onStart,icon=Icons.Rounded.PlayArrow)
   TextButton(onClick=onGuide,modifier=Modifier.fillMaxWidth()){Icon(Icons.Rounded.Info,null,Modifier.size(17.dp));Spacer(Modifier.width(6.dp));Text(if(w.hasSprint())"Sprint nedir?" else "Antrenmanın akışı")}
  }
 }
}
@Composable fun ProgramList(week:List<ProgramDay>,current:Int,onWorkout:(String)->Unit){
 week.forEach{d->
  Surface(onClick={onWorkout(d.workout.id)},color=if(d.index==current)Mint else MaterialTheme.colorScheme.surface,shape=RoundedCornerShape(16.dp)){
   Row(Modifier.fillMaxWidth().padding(14.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){
    Column(horizontalAlignment=Alignment.CenterHorizontally){Text("${d.index+1}",fontSize=20.sp,fontWeight=FontWeight.Bold);Text("GÜN",fontSize=9.sp,color=Sky)}
    Column(Modifier.weight(1f)){Text(d.workout.title,fontSize=15.sp,fontWeight=FontWeight.Bold);QuietText(if(d.training)minutesText(d.workout.seconds) else "Dinlenme günü · İstersen 5 dk")}
    Icon(if(d.kind=="run")Icons.Rounded.DirectionsRun else if(d.training)Icons.Rounded.FitnessCenter else Icons.Rounded.Bedtime,null,tint=if(d.kind=="run")Coral else Sky,modifier=Modifier.size(22.dp))
   }
  }
 }
}
