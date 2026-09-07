package com.elevare.active

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Ink=Color(0xFFEAF0F8)
val Lime=Color(0xFF294B72)
val Blue=Color(0xFF2867CF)
val Sky=Color(0xFF98C4FF)
val Coral=Color(0xFFFFAA91)
val Mint=Color(0xFF203650)
val Lilac=Color(0xFF30334E)
val Paper=Color(0xFF101D2D)
val Track=Color(0xFF172B43)
private val WorkoutColors=darkColorScheme(
 primary=Sky,onPrimary=Paper,primaryContainer=Blue,onPrimaryContainer=Ink,
 secondary=Coral,onSecondary=Paper,secondaryContainer=Mint,onSecondaryContainer=Ink,
 background=Paper,onBackground=Ink,surface=Color(0xFF1B2D44),onSurface=Ink,
 surfaceVariant=Color(0xFF243A53),onSurfaceVariant=Color(0xFFB1C0D2),
 outline=Color(0xFF8297B1),outlineVariant=Color(0xFF354D68),error=Color(0xFFFFB4AE))

@Composable fun ElevareTheme(dark:Boolean=true,content:@Composable ()->Unit){
 val context=androidx.compose.ui.platform.LocalContext.current
 SideEffect { (context as? android.app.Activity)?.window?.let { w ->
  w.statusBarColor=0xFF101D2D.toInt();w.navigationBarColor=w.statusBarColor
  androidx.core.view.WindowCompat.getInsetsController(w,w.decorView).apply{isAppearanceLightStatusBars=false;isAppearanceLightNavigationBars=false}
 } }
 MaterialTheme(colorScheme=WorkoutColors,typography=Typography(
  displayLarge=TextStyle(fontFamily=FontFamily.SansSerif,fontWeight=FontWeight.Black,fontSize=42.sp,lineHeight=44.sp,letterSpacing=(-1.2).sp),
  headlineLarge=TextStyle(fontWeight=FontWeight.ExtraBold,fontSize=30.sp,lineHeight=35.sp,letterSpacing=(-.6).sp),
  headlineMedium=TextStyle(fontWeight=FontWeight.Bold,fontSize=25.sp,lineHeight=30.sp),
  titleLarge=TextStyle(fontWeight=FontWeight.Bold,fontSize=21.sp,lineHeight=26.sp),
  titleMedium=TextStyle(fontWeight=FontWeight.Bold,fontSize=16.sp,lineHeight=22.sp),
  bodyLarge=TextStyle(fontSize=16.sp,lineHeight=24.sp),bodyMedium=TextStyle(fontSize=14.sp,lineHeight=21.sp),
  labelLarge=TextStyle(fontWeight=FontWeight.Bold,fontSize=14.sp,lineHeight=20.sp)),content=content)
}
