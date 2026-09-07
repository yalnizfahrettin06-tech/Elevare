package com.elevare.active

import android.content.Intent
import android.graphics.Bitmap
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.After
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class WorkoutJourneyTest {
 private val instrumentation=InstrumentationRegistry.getInstrumentation()
 private val context=instrumentation.targetContext
 private val device=UiDevice.getInstance(instrumentation)
 @After fun exportEvidence(){
  device.executeShellCommand("mkdir -p /sdcard/elevare-qa; cp -R /sdcard/Android/data/com.elevare.active/files/qa/. /sdcard/elevare-qa/")
 }
 private fun click(text:String){
  val node=device.wait(Until.findObject(By.text(text)),8000)?:error("Missing: $text")
  node.click();Thread.sleep(450)
 }
 private fun shot(name:String){
  val dir=File(context.getExternalFilesDir(null),"qa").apply{mkdirs()}
  val bitmap=instrumentation.uiAutomation.takeScreenshot()
  File(dir,"$name.png").outputStream().use{bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}
  bitmap.recycle()
 }
 @Test fun fullOnboardingAndWorkout(){
  context.getSharedPreferences("elevare_active_v2",0).edit().clear().commit()
  ActivityScenario.launch(MainActivity::class.java).use{
   assertTrue(device.wait(Until.hasObject(By.text("Programımı oluştur")),10000))
   shot("01-welcome");click("Programımı oluştur")
   shot("02-age");click("18");click("Devam et")
   click("Güç ve kondisyon");click("Devam et")
   click("Haftada 3 gün veya daha fazla");click("Devam et")
   click("Dışarıda · park veya pist");click("Devam et")
   click("3 gün");click("Devam et")
   click("8–10 saat");click("Devam et")
   click("Ölçmedim / bilmiyorum");click("Devam et")
   shot("03-time");click("15 dk");click("Devam et")
   click("Hayır, bildiğim bir engel yok");click("Programımı hazırla")
   Thread.sleep(2000);shot("04-preparation")
   assertTrue(device.wait(Until.hasObject(By.text("3 gün ücretsiz dene")),38000))
   shot("05-program-ready")
   click("3 gün ücretsiz dene")
   assertTrue(device.wait(Until.hasObject(By.text("Antrenmana başla")),8000))
   assertFalse(device.hasObject(By.text("3 gün ücretsiz dene")))
   shot("06-home")
   click("Sprint nedir?")
   assertTrue(device.wait(Until.hasObject(By.text("Yavaş göster")),6000))
   shot("07-running-guide")
   device.executeShellCommand("screenrecord --time-limit 8 /sdcard/Android/data/com.elevare.active/files/qa/running.mp4")
   device.pressBack();Thread.sleep(500)
   click("Antrenmana başla");click("Başla")
   assertTrue(device.wait(Until.hasObject(By.text("DURAKLAT")),6000))
   shot("08-player");click("DURAKLAT")
   assertTrue(device.wait(Until.hasObject(By.text("DEVAM ET")),3000))
   device.pressBack();Thread.sleep(400)
   click("Antrenman");click("Hareketler")
   click("Alçak hamle");shot("09-yoga-guide")
   device.executeShellCommand("screenrecord --time-limit 8 /sdcard/Android/data/com.elevare.active/files/qa/yoga.mp4")
  }
 }
 @Test fun generatedSessionRestoresAndAdvances(){
  val store=Store(context)
  val w=buildProgramWorkout("run",15,2)
  store.update{UserState(ready=true,onboardingVersion=7,age=18,safety="clear",environment="outdoor",trainingDays=3,dailyMinutes=15,focus="performance",recentGrowth="unknown",sleepHabit="8to10",activityHabit="regular",voiceCoach=false,
   active=ActiveSession(w.id,step=3,remaining=1,running=false,elapsed=300))}
  ActivityScenario.launch(MainActivity::class.java).use{
   assertTrue(device.wait(Until.hasObject(By.text("Antrenmana devam et")),8000))
   click("Antrenmana devam et");click("DEVAM ET")
   assertTrue(device.wait(Until.hasObject(By.text("Biraz dinlen")),6000))
   shot("10-auto-recovery")
  }
 }
}
