package com.elevare.active

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Legacy aliases remain source-compatible; all screens share the Arc palette.
// No persisted preference, program rule or entitlement is changed by this skin.
val Ink=Color(0xFFF4EFE8)
val Lime=Color(0xFF49342F)
val Blue=Color(0xFFF28B74)
val Sky=Color(0xFFE8B9A5)
val Coral=Color(0xFFF28B74)
val Mint=Color(0xFF302623)
val Lilac=Color(0xFF302C32)
val Paper=Color(0xFF151414)
val Track=Color(0xFF211D1C)
val ArcMuted=Color(0xFFBDB3AB)
val ArcLine=Color(0xFF4B403C)
private val WorkoutColors=darkColorScheme(
 primary=Coral,onPrimary=Paper,primaryContainer=Lime,onPrimaryContainer=Ink,
 secondary=Coral,onSecondary=Paper,secondaryContainer=Mint,onSecondaryContainer=Ink,
 background=Paper,onBackground=Ink,surface=Color(0xFF201E1D),onSurface=Ink,
 surfaceVariant=Color(0xFF2B2725),onSurfaceVariant=ArcMuted,
 outline=Color(0xFF94867E),outlineVariant=ArcLine,error=Color(0xFFFFB4AE))

@Composable fun ElevareTheme(dark:Boolean=true,content:@Composable ()->Unit){
 val context=androidx.compose.ui.platform.LocalContext.current
 SideEffect { (context as? android.app.Activity)?.window?.let { w ->
  w.statusBarColor=0xFF151414.toInt();w.navigationBarColor=w.statusBarColor
  androidx.core.view.WindowCompat.getInsetsController(w,w.decorView).apply{isAppearanceLightStatusBars=false;isAppearanceLightNavigationBars=false}
 } }
 MaterialTheme(colorScheme=WorkoutColors,typography=Typography(
  displayLarge=TextStyle(fontFamily=FontFamily.SansSerif,fontWeight=FontWeight.ExtraBold,fontSize=42.sp,lineHeight=46.sp,letterSpacing=(-1.5).sp),
  headlineLarge=TextStyle(fontWeight=FontWeight.ExtraBold,fontSize=30.sp,lineHeight=35.sp,letterSpacing=(-.6).sp),
  headlineMedium=TextStyle(fontWeight=FontWeight.Bold,fontSize=25.sp,lineHeight=30.sp),
  titleLarge=TextStyle(fontWeight=FontWeight.Bold,fontSize=21.sp,lineHeight=26.sp),
  titleMedium=TextStyle(fontWeight=FontWeight.Bold,fontSize=16.sp,lineHeight=22.sp),
  bodyLarge=TextStyle(fontSize=16.sp,lineHeight=24.sp),bodyMedium=TextStyle(fontSize=14.sp,lineHeight=21.sp),
  labelLarge=TextStyle(fontWeight=FontWeight.Bold,fontSize=14.sp,lineHeight=20.sp)),content=content)
}
