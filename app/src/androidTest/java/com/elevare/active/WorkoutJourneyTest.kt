package com.elevare.active

import android.graphics.Bitmap
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.After
import org.junit.runner.RunWith
import java.io.File

/** Device evidence is collected by CI, never treated as expert movement approval. */
@RunWith(AndroidJUnit4::class)
class WorkoutJourneyTest {
    private val instrumentation=InstrumentationRegistry.getInstrumentation()
    private val context=instrumentation.targetContext
    private val device=UiDevice.getInstance(instrumentation)

    @After fun exportEvidence(){
        device.executeShellCommand("settings put system font_scale 1.0")
        device.executeShellCommand("mkdir -p /sdcard/elevare-qa")
        val dir=File(context.getExternalFilesDir(null),"qa").apply{mkdirs()}
        if(dir.listFiles()?.isNotEmpty()==true){
            val result=device.executeShellCommand("cp -R /sdcard/Android/data/com.elevare.active/files/qa/. /sdcard/elevare-qa/")
            assertTrue("Could not retain screenshots: $result",result.isBlank())
        }
    }
    private fun find(text:String):UiObject2?=
        device.findObject(By.text(text))?:device.findObject(By.desc(text))
    private fun node(text:String):UiObject2{
        repeat(7){
            find(text)?.let{return it}
            device.swipe(device.displayWidth/2,device.displayHeight*72/100,
                device.displayWidth/2,device.displayHeight*34/100,20)
            Thread.sleep(250)
        }
        return device.wait(Until.findObject(By.text(text)),4000)
            ?:device.findObject(By.desc(text))?:error("Missing: $text")
    }
    private fun click(text:String){
        node(text).click();device.waitForIdle(1200);Thread.sleep(250)
    }
    private fun shot(name:String){
        device.waitForIdle(1500);Thread.sleep(500)
        val dir=File(context.getExternalFilesDir(null),"qa").apply{mkdirs()}
        val bitmap=instrumentation.uiAutomation.takeScreenshot()
        File(dir,"$name.png").outputStream().use{bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}
        bitmap.recycle()
    }
    private fun clear(){
        context.getSharedPreferences("elevare_active_v2",0).edit().clear().commit()
    }
    private fun seededState(active:ActiveSession?=null)=UserState(
        ready=true,onboardingVersion=TRAINING_ONBOARDING_VERSION,age=18,
        focus="performance",recentGrowth="unknown",sleepHabit="8to10",
        activityHabit="new",runningExperience="new",environment="both",
        equipment=setOf("none"),trainingDays=3,dailyMinutes=15,safety="clear",
        voiceCoach=false,timerSound="off",active=active
    )

    @Test fun elevenRequiredAnswersOneTrialAndPartialWorkout(){
        clear()
        ActivityScenario.launch(MainActivity::class.java).use{scenario->
            assertTrue(device.wait(Until.hasObject(By.text("Hikâyeme başla")),10000))
            shot("01-welcome");click("Hikâyeme başla")
            assertFalse(node("Devam et").isEnabled)
            shot("02-age");click("18");click("Devam et")
            // The draft survives activity recreation without skipping required responses.
            scenario.recreate()
            assertTrue(device.wait(Until.hasObject(By.text("Neye odaklanalım?")),10000))
            click("Güç ve kondisyon");click("Devam et")
            click("Yeni başlıyorum");click("Devam et")
            click("Yeni başlıyorum");click("Devam et")
            click("Hem evde hem dışarıda");click("Devam et")
            click("Sağlam sandalye");click("Duvar veya sabit destek")
            click("Mat / uygun yumuşak yüzey");shot("03-equipment");click("Devam et")
            click("3 gün");click("Devam et")
            click("15 dk");shot("04-time");click("Devam et")
            click("8–10 saat");click("Devam et")
            click("Belirtmek istemiyorum");click("Devam et")
            click("Hayır, bildiğim bir engel yok");click("Programımı hazırla")
            Thread.sleep(1600);shot("05-preparation")
            assertFalse(device.hasObject(By.text("3 gün ücretsiz dene")))
            assertTrue(device.wait(Until.hasObject(By.text("3 gün ücretsiz dene")),38000))
            assertEquals(1,device.findObjects(By.text("3 gün ücretsiz dene")).size)
            assertFalse(device.hasObject(By.textContains("7 gün ücretsiz")))
            shot("06-program-ready");click("3 gün ücretsiz dene")
            assertTrue(device.wait(Until.hasObject(By.text("Antrenmana başla")),10000))
            assertFalse(device.hasObject(By.text("3 gün ücretsiz dene")))
            shot("07-home")

            click("Rutinim");click("90 günlük antrenman planı");click("Hareketler");click("Kontrollü hızlan")
            assertTrue(device.wait(Until.hasObject(By.text("Yavaş göster")),6000))
            shot("08-running-guide")
            device.executeShellCommand("screenrecord --time-limit 8 /sdcard/Android/data/com.elevare.active/files/qa/running-normal.mp4")
            click("Yavaş göster")
            device.executeShellCommand("screenrecord --time-limit 8 /sdcard/Android/data/com.elevare.active/files/qa/running-slow.mp4")
            click("Duraklat");shot("09-running-paused")
            device.pressBack();Thread.sleep(400);click("Hareketler");click("Alçak hamle")
            shot("10-yoga-guide")
            device.executeShellCommand("screenrecord --time-limit 15 /sdcard/Android/data/com.elevare.active/files/qa/yoga-entry-hold-exit.mp4")
            device.pressBack();Thread.sleep(400);click("Bugün")
            click("Antrenmana başla")
            click("Hazırım");click("Alanım ve gerekli destekler hazır");click("Başla")
            assertTrue(device.wait(Until.hasObject(By.text("Duraklat")),8000))
            shot("11-player");click("Duraklat")
            assertTrue(device.wait(Until.hasObject(By.text("Devam et")),4000))
            shot("12-player-paused");click("Seansı bırak");click("Kısmi kaydet ve çık")
            val persisted=Store(context).state
            assertNull(persisted.active)
            assertTrue(persisted.sessions.isNotEmpty())
            assertFalse(persisted.sessions.last().completed)
            click("Profil");shot("13-profile")
        }
    }

    @Test fun lifestyleOptInCompletionUndoAndPersistence(){
        clear()
        assertTrue(Store(context).update{seededState()})
        ActivityScenario.launch(MainActivity::class.java).use{scenario->
            assertTrue(device.wait(Until.hasObject(By.text("Antrenmana başla")),10000))
            click("Rutinim");click("Rutinlerimi seç")
            click("Güne yer aç");click("Su molası");click("Seçtiklerimi ekle")
            assertEquals(2,Store(context).state.life.plans.size)
            assertTrue(Store(context).state.life.plans.none{it.remind})
            shot("18-routines");click("Güne yer aç")
            click("Bugünün bir önceliğini seç: yaptım")
            assertEquals(1,routineDoneCount(Store(context).state.life,"morning",java.time.LocalDate.now()))
            shot("19-routine-detail")
            scenario.recreate()
            click("Bugünün bir önceliğini seç: kaydı geri al")
            assertEquals(0,routineDoneCount(Store(context).state.life,"morning",java.time.LocalDate.now()))
            device.pressBack();click("Bugün");shot("20-lifestyle-home")
        }
    }

    @Test fun immutableSnapshotRestoresAndBackgroundDoesNotAdvance(){
        clear()
        val fixture=Workout("qa_snapshot_v8","Kayıt geri yükleme kontrolü","Yalnız test","Hazırlık",
            listOf(Step("walk",1,phase="warmup"),Step("walk",20,true,phase="recovery")))
        val active=ActiveSession(fixture.id,remaining=1,running=false,snapshot=fixture)
        assertTrue(Store(context).update{seededState(active)})
        ActivityScenario.launch(MainActivity::class.java).use{scenario->
            assertTrue(device.wait(Until.hasObject(By.text("Antrenmana devam et")),10000))
            click("Antrenmana devam et");click("Hazırım, başla")
            assertTrue(device.wait(Until.hasObject(By.text("Yürüyüş arası")),7000))
            shot("14-auto-recovery")
            scenario.moveToState(Lifecycle.State.CREATED)
            val before=Store(context).state.active!!
            assertFalse(before.running)
            Thread.sleep(1600)
            scenario.moveToState(Lifecycle.State.RESUMED)
            assertTrue(device.wait(Until.hasObject(By.text("Devam et")),6000))
            val after=Store(context).state.active!!
            assertEquals(before.remaining,after.remaining)
            assertEquals(fixture,after.snapshot)
            assertEquals(1,after.step)
            shot("15-restored-paused")
        }
    }

    @Test fun aCompletedSnapshotIsSavedOnce(){
        clear()
        val fixture=Workout("qa_complete_v8","Tamamlanma kontrolü","Yalnız test","Hazırlık",
            listOf(Step("walk",1,phase="cooldown")))
        val active=ActiveSession(fixture.id,remaining=0,running=false,snapshot=fixture)
        assertTrue(Store(context).update{seededState(active)})
        ActivityScenario.launch(MainActivity::class.java).use{
            assertTrue(device.wait(Until.hasObject(By.text("Antrenmana devam et")),10000))
            click("Antrenmana devam et")
            assertTrue(device.wait(Until.hasObject(By.text("Kaydet ve bitir")),6000))
            click("Uygundu");shot("16-complete");click("Kaydet ve bitir")
            assertEquals(1,Store(context).state.sessions.count{it.id==active.id&&it.completed})
            assertNull(Store(context).state.active)
        }
    }

    @Test fun largeTextKeepsRequiredAgeAnswerReachable(){
        clear()
        device.executeShellCommand("settings put system font_scale 2.0")
        ActivityScenario.launch(MainActivity::class.java).use{
            assertTrue(device.wait(Until.hasObject(By.text("Hikâyeme başla")),10000))
            click("Hikâyeme başla");click("21");shot("17-font-200")
            click("Devam et")
            assertTrue(device.wait(Until.hasObject(By.text("Neye odaklanalım?")),8000))
        }
    }
}
