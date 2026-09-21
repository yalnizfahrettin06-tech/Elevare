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
        // Compose can replace the semantics node between lookup and click.
        // Retry only this stale-reference failure, never an assertion or missing control.
        repeat(3){attempt->
            try{
                node(text).click();device.waitForIdle(1200);Thread.sleep(250);return
            }catch(stale:StaleObjectException){
                if(attempt==2)throw stale
                device.waitForIdle(1200);Thread.sleep(150)
            }
        }
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
            click("Devam et")
            assertEquals(1,Store(context).state.onboardingDraft!!.step)
            shot("02-age");click("18 yaş");click("Devam et")
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
            Thread.sleep(250);shot("05-preparation")
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
            device.pressBack();Thread.sleep(400);click("Alçak hamle")
            shot("10-yoga-guide")
            device.executeShellCommand("screenrecord --time-limit 15 /sdcard/Android/data/com.elevare.active/files/qa/yoga-entry-hold-exit.mp4")
            device.pressBack();Thread.sleep(400);device.pressBack();Thread.sleep(400);click("Bugün")
            click("Antrenmana başla")
            click("Hazırım");click("Alanım ve gerekli destekler hazır");click("Başla")
            assertTrue(device.wait(Until.hasObject(By.text("Duraklat")),8000))
            val pauseBounds=find("Duraklat")!!.visibleBounds
            assertTrue("Pause must be visible without scrolling",pauseBounds.height()>0&&pauseBounds.bottom<device.displayHeight)
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

    @Test fun proPreviewAppliesOnlyConfirmedPreference(){
        clear();assertTrue(Store(context).update{seededState()})
        ActivityScenario.launch(MainActivity::class.java).use{
            assertTrue(device.wait(Until.hasObject(By.text("Antrenmana başla")),10000))
            click("Profil");click("Elevare Pro önizlemesi");click("10 dk")
            assertEquals(15,Store(context).state.dailyMinutes)
            click("Değişikliği incele");shot("21-pro-confirm")
            click("Vazgeç");assertEquals(15,Store(context).state.dailyMinutes)
            click("Değişikliği incele");click("Tercihi uygula")
            assertEquals(10,Store(context).state.dailyMinutes)
            shot("22-pro-applied")
        }
    }

    @Test fun backReturnsToParentAndPreservesItAfterRecreation(){
        clear();assertTrue(Store(context).update{seededState()})
        ActivityScenario.launch(MainActivity::class.java).use{scenario->
            click("Profil");click("Elevare Pro önizlemesi")
            scenario.recreate()
            assertTrue(device.wait(Until.hasObject(By.text("Pro önizlemesi")),10000))
            device.pressBack()
            assertTrue(device.wait(Until.hasObject(By.text("Ayarlar")),6000))
            device.pressBack();click("Rutinim");device.pressBack()
            assertTrue(device.wait(Until.hasObject(By.text("Antrenmana başla")),6000))
            shot("24-parent-navigation")
        }
    }

    @Test fun homeQuickActionAndUndoPreserveOtherRecords(){
        clear();assertTrue(Store(context).update{seededState().copy(life=addRoutine(LifeState(),"water"))})
        ActivityScenario.launch(MainActivity::class.java).use{
            assertTrue(device.wait(Until.hasObject(By.text("Antrenmana başla")),10000))
            click("Su molanı ver: yaptım")
            assertEquals(1,routineDoneCount(Store(context).state.life,"water",java.time.LocalDate.now()))
            click("Son işaretlemeyi geri al")
            assertEquals(0,routineDoneCount(Store(context).state.life,"water",java.time.LocalDate.now()))
        }
    }

    @Test fun largeTextKeepsSessionControlsVisible(){
        clear()
        val fixture=Workout("qa_large_v11","Büyük yazı kontrolü","Yalnız test","Hazırlık",listOf(Step("walk",90,phase="warmup")))
        assertTrue(Store(context).update{seededState(ActiveSession(fixture.id,remaining=90,running=false,snapshot=fixture))})
        device.executeShellCommand("settings put system font_scale 2.0")
        ActivityScenario.launch(MainActivity::class.java).use{
            click("Antrenmana devam et");click("Hazırım, başla")
            assertTrue(device.wait(Until.hasObject(By.text("Duraklat")),8000))
            val bounds=find("Duraklat")!!.visibleBounds
            assertTrue(bounds.height()>0&&bounds.bottom<device.displayHeight)
            shot("23-player-large-text");click("Duraklat")
            assertFalse(Store(context).state.active!!.running)
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
            assertNotNull(node("Kaydet ve bitir"))
            device.swipe(device.displayWidth/2,device.displayHeight*35/100,device.displayWidth/2,device.displayHeight*75/100,20)
            click("Uygundu");shot("16-complete");click("Kaydet ve bitir")
            assertEquals(1,Store(context).state.sessions.count{it.id==active.id&&it.completed})
            assertNull(Store(context).state.active)
        }
    }


    @Test fun renewalPlanAndJournalAreReachable(){
        clear();val seeded=seededState()
        assertTrue(Store(context).update{seeded})
        ActivityScenario.launch(MainActivity::class.java).use{
            click("Profil");click("Kişisel bilgiler");click("Yanıtlarımı düzenle")
            assertNotNull(node("Plan atölyesi"));shot("24-plan-studio")
            click("Haftalık ritim: 3 gün");click("2 gün")
            click("Değişiklikleri incele");click("Planımı güncelle")
            assertEquals(2,Store(context).state.trainingDays)
            assertEquals(seeded.cycleId,Store(context).state.cycleId)
            device.pressBack();Thread.sleep(350)
            repeat(3){device.swipe(device.displayWidth/2,device.displayHeight*30/100,device.displayWidth/2,device.displayHeight*80/100,20)}
            click("İlerlemem");click("Tüm zamanlar");shot("25-progress-journal")
            device.pressBack();Thread.sleep(350)
            click("Gizlilik ve yardım");click("Yedekle ve geri yükle")
            assertNotNull(node("Dosyadan geri yükle"));shot("26-backup")
        }
    }

    @Test fun storageMeasurementsAndRestoreRollback(){
        clear()
        val dir=File(context.getExternalFilesDir(null),"qa").apply{mkdirs()}
        val lines=mutableListOf("records,encoded_bytes,encode_ms,commit_ms,restore_ms")
        for(size in listOf(90,365,1000)){
            val seed=seededState().copy(sessions=(0 until size).map{
                SessionLog("qa-$it","Ölçüm",java.time.LocalDate.now().minusDays(it.toLong()).toString(),300,"Uygundu",cycleId="past")
            })
            val start=System.nanoTime();val raw=StateCodec.encode(seed);val encode=System.nanoTime()-start
            val store=Store(context);val commitAt=System.nanoTime()
            assertTrue(store.update{seed});val commit=System.nanoTime()-commitAt
            val at=System.nanoTime();assertTrue(store.restore(raw));val restore=System.nanoTime()-at
            assertEquals(size,store.state.sessions.size)
            assertTrue(store.undoRestore());assertEquals(size,store.state.sessions.size)
            lines+="$size,${raw.toByteArray().size},${encode/1_000_000.0},${commit/1_000_000.0},${restore/1_000_000.0}"
        }
        File(dir,"storage-measurements.csv").writeText(lines.joinToString("\n"))
    }

    @Test fun largeTextKeepsRequiredAgeAnswerReachable(){
        clear()
        device.executeShellCommand("settings put system font_scale 2.0")
        ActivityScenario.launch(MainActivity::class.java).use{
            assertTrue(device.wait(Until.hasObject(By.text("Hikâyeme başla")),10000))
            click("Hikâyeme başla");click("21 yaş");shot("17-font-200")
            click("Devam et")
            assertTrue(device.wait(Until.hasObject(By.text("Neye odaklanalım?")),8000))
        }
    }
}
