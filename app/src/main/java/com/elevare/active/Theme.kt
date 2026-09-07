package com.elevare.active
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
val Ink=Color(0xFF171820)
val Lime=Color(0xFFC8E0FF)
val Blue=Color(0xFF155FBA)
val Mint=Color(0xFFE9F1FB)
val Lilac=Color(0xFFE5EDFA)
val Paper=Color(0xFFF4F7FB)
private val Light=lightColorScheme(primary=Blue,onPrimary=Color.White,primaryContainer=Lime,onPrimaryContainer=Ink,secondary=Ink,onSecondary=Color.White,secondaryContainer=Mint,onSecondaryContainer=Ink,background=Paper,onBackground=Ink,surface=Color.White,onSurface=Ink,surfaceVariant=Color(0xFFEAF0F7),onSurfaceVariant=Color(0xFF626A74),outline=Color(0xFF7A817D),outlineVariant=Color(0xFFDCE4EE),error=Color(0xFF9E4535))
private val Dark=darkColorScheme(primary=Lime,onPrimary=Ink,primaryContainer=Blue,onPrimaryContainer=Color.White,secondary=Lime,onSecondary=Ink,secondaryContainer=Color(0xFF20344B),onSecondaryContainer=Color.White,background=Color(0xFF10161E),onBackground=Color(0xFFF4F4FA),surface=Color(0xFF171922),onSurface=Color(0xFFF4F4FA),surfaceVariant=Color(0xFF222430),onSurfaceVariant=Color(0xFFAFB0C0),outline=Color(0xFF8C8EA1),outlineVariant=Color(0xFF303242),error=Color(0xFFFFB4AE))
@Composable fun ElevareTheme(dark:Boolean,content:@Composable ()->Unit){
 val context=androidx.compose.ui.platform.LocalContext.current
 androidx.compose.runtime.SideEffect { (context as? android.app.Activity)?.window?.let { w->w.statusBarColor=if(dark)0xFF10161E.toInt() else 0xFFF4F7FB.toInt();w.navigationBarColor=w.statusBarColor;androidx.core.view.WindowCompat.getInsetsController(w,w.decorView).apply{isAppearanceLightStatusBars=!dark;isAppearanceLightNavigationBars=!dark} } }
 MaterialTheme(colorScheme=if(dark)Dark else Light,typography=Typography(
 displayLarge=TextStyle(fontFamily=FontFamily.SansSerif,fontWeight=FontWeight.Black,fontSize=46.sp,lineHeight=47.sp,letterSpacing=(-1.5).sp),
 headlineLarge=TextStyle(fontFamily=FontFamily.SansSerif,fontWeight=FontWeight.Bold,fontSize=29.sp,lineHeight=34.sp,letterSpacing=(-.6).sp),
 headlineMedium=TextStyle(fontWeight=FontWeight.ExtraBold,fontSize=26.sp,lineHeight=30.sp),
 titleLarge=TextStyle(fontWeight=FontWeight.Bold,fontSize=21.sp,lineHeight=25.sp),
 titleMedium=TextStyle(fontWeight=FontWeight.Bold,fontSize=16.sp,lineHeight=22.sp),
 bodyLarge=TextStyle(fontSize=16.sp,lineHeight=23.sp),bodyMedium=TextStyle(fontSize=14.sp,lineHeight=21.sp),
 labelLarge=TextStyle(fontWeight=FontWeight.Bold,fontSize=14.sp,lineHeight=20.sp)
 ),content=content)
}
