package com.elevare.active

import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.delay

@Composable
fun TrainingOnboarding(store: Store) {
    LaunchedEffect(Unit) { store.pauseTimer() }
    var step by rememberSaveable { mutableIntStateOf(0) }
    var age by rememberSaveable { mutableIntStateOf(store.state.age) }
    var focus by rememberSaveable { mutableStateOf(store.state.focus) }
    var growth by rememberSaveable { mutableStateOf(store.state.recentGrowth) }
    var sleep by rememberSaveable { mutableStateOf(store.state.sleepHabit) }
    var activity by rememberSaveable { mutableStateOf(store.state.activityHabit) }
    var minutes by rememberSaveable { mutableIntStateOf(if (store.state.ready) store.state.dailyMinutes else 0) }
    var safety by rememberSaveable { mutableStateOf(store.state.safety) }
    var preparationElapsed by rememberSaveable(age, focus, growth, sleep, activity, minutes, safety) { mutableLongStateOf(0L) }

    val answers = TrainingAnswers(age, focus, growth, sleep, activity, minutes, safety)
    fun finish(days: Int) {
        if (!answers.complete()) return
        store.pauseTimer()
        store.update {
            it.copy(ready = true, onboardingVersion = 5, age = age, focus = focus, recentGrowth = growth,
                sleepHabit = sleep, activityHabit = activity, dailyMinutes = minutes, safety = safety,
                trialDays = normalizeTrialDays(days))
        }
    }
    BackHandler(enabled = step > 0) { step = if (step == 9) 7 else step - 1 }
    when (step) {
        8 -> {
            PreparationScreen(answers, preparationElapsed, { preparationElapsed = it }, { step = 7 }, { step = 9 })
            return
        }
        9 -> {
            TrialPreview(onFinish = ::finish, onSkip = { finish(0) }, onBack = { step = 7 })
            return
        }
    }
    val valid = when (step) {
        0 -> true
        1 -> age in ProfileChoices.ages
        2 -> focus in ProfileChoices.focus
        3 -> growth in ProfileChoices.growth
        4 -> sleep in ProfileChoices.sleep
        5 -> activity in ProfileChoices.activity
        6 -> minutes in ProfileChoices.minutes
        else -> answers.complete()
    }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            if (step > 0) IconButton(onClick = { step-- }) { Icon(Icons.Rounded.ArrowBack, "Geri") }
            Text("elevare", Modifier.weight(1f), fontSize = 22.sp, fontWeight = FontWeight.Bold)
            if (step > 0) Text("$step / 7", fontSize = 13.sp)
        }
        if (step > 0) LinearProgressIndicator(progress = { step / 7f }, modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp))
        key(step) {
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                when (step) {
                    0 -> WelcomeQuestion()
                    1 -> AgeQuestion(age) { age = it }
                    2 -> { QuestionTitle("Önceliğin ne?"); QuietText("GH yalnızca boyla ilgili değildir. Burada hormon düzeyi değil, rutin ve bilgi tercihlerin belirlenir."); AnswerChoices(ProfileChoices.focus, focus) { focus = it } }
                    3 -> { QuestionTitle("Son 6 ayda boyunda artış fark ettin mi?"); QuietText("Ölçmediysen “bilmiyorum” seç. Bu yanıt büyüme potansiyelini hesaplamaz."); AnswerChoices(ProfileChoices.growth, growth) { growth = it } }
                    4 -> { QuestionTitle("Genellikle ne kadar uyuyorsun?"); AnswerChoices(ProfileChoices.sleep, sleep) { sleep = it } }
                    5 -> { QuestionTitle("Haftada kaç gün hareket ediyorsun?"); QuietText("Spor, yürüyüş ve aktif oyunlar da sayılır."); AnswerChoices(ProfileChoices.activity, activity) { activity = it } }
                    6 -> { QuestionTitle("Günde ne kadar zaman ayırırsın?"); ProfileChoices.minutes.forEach { value -> Choice("$value dakika", selected = minutes == value) { minutes = value } }; QuietText("Bu bir zaman bütçesi; her dakikasını egzersizle doldurmak zorunda değilsin.") }
                    7 -> { QuestionTitle("Başlamadan önce"); QuietText("Antrenmanı etkileyen ağrı veya uzman kısıtlaması var mı?"); AnswerChoices(ProfileChoices.safety, safety) { safety = it }; if (safety.isNotEmpty() && safety != "clear") QuietText("Bilgi içeriklerini açabilirsin. Antrenman uygunluğunu netleştirene kadar sayaçlı seanslar kapalı kalır.") }
                }
            }
        }
        Column(Modifier.padding(horizontal = 24.dp, vertical = 14.dp)) {
            if (!valid) QuietText("Devam etmek için bir yanıt seç.")
            BigButton(if (step == 0) "Başlayalım" else if (step == 7) "Rutinimi hazırla" else "Devam et", { step++ }, enabled = valid)
        }
    }
}

@Composable private fun WelcomeQuestion() {
    Text("Az düşün.\nHarekete başla.", fontSize = 30.sp, lineHeight = 36.sp, fontWeight = FontWeight.Bold)
    QuietText("Kısa antrenman, sesli koç ve her gün bir bilgi.")
    Surface(color = Mint, shape = RoundedCornerShape(16.dp)) { Pose("march", Modifier.fillMaxWidth().height(180.dp), Ink, true) }
    QuietText("7 kısa soru. Yanıtların yalnızca cihazında tutulur; hormon ölçümü yapılmaz.")
}
@Composable private fun AgeQuestion(age: Int, onSelect: (Int) -> Unit) {
    QuestionTitle("Kaç yaşındasın?")
    ProfileChoices.ages.toList().chunked(3).forEach { row ->
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            row.forEach { value -> Box(Modifier.weight(1f)) { Choice("$value", selected = age == value) { onSelect(value) } } }
        }
    }
}
@Composable private fun QuestionTitle(text: String) { Text(text, fontSize = 26.sp, lineHeight = 32.sp, fontWeight = FontWeight.Bold) }
@Composable private fun AnswerChoices(options: Map<String, String>, selected: String, onSelect: (String) -> Unit) {
    options.forEach { (id, label) -> Choice(label, selected = selected == id) { onSelect(id) } }
}

@Composable
fun PreparationScreen(answers: TrainingAnswers, elapsed: Long, onElapsed: (Long) -> Unit, onBack: () -> Unit, onReady: () -> Unit) {
    val currentElapsed by rememberUpdatedState(elapsed)
    val updateElapsed by rememberUpdatedState(onElapsed)
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val plan = remember(answers) { routinePlan(answers) }
    LaunchedEffect(answers) {
        var previous = SystemClock.elapsedRealtime()
        while (currentElapsed < PREPARATION_DURATION_MS) {
            delay(100)
            val now = SystemClock.elapsedRealtime()
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) updateElapsed((currentElapsed + (now - previous).coerceAtMost(1000)).coerceAtMost(PREPARATION_DURATION_MS))
            previous = now
        }
    }
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Yanıtlara dön") }
            Text("Rutin hazırlığı", fontWeight = FontWeight.Bold)
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(22.dp)) {
            Pose("reach", Modifier.fillMaxWidth().height(150.dp), MaterialTheme.colorScheme.onBackground, true)
            QuestionTitle(preparationStage(elapsed))
            LinearProgressIndicator(progress = { elapsed.toFloat() / PREPARATION_DURATION_MS }, modifier = Modifier.fillMaxWidth())
            QuietText("45 saniyelik rutin hazırlığı · Hormon ölçümü yapılmaz.")
            Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(focusLabel(answers.focus), fontWeight = FontWeight.SemiBold)
                    QuietText("Günlük zamanın: ${answers.minutes} dk")
                    if (elapsed >= 15_000L) QuietText(plan.note)
                    if (elapsed >= 30_000L && !plan.blocked) QuietText("İlk seans: ${shortWorkoutTitle(Content.workout(plan.workoutId))} · ${minutesText(Content.workout(plan.workoutId).seconds)}")
                }
            }
            if (elapsed >= PREPARATION_DURATION_MS) {
                Text(if (plan.blocked) "Önce uygunluğunu netleştir" else "Başlangıç rutinin hazır", fontSize = 21.sp, fontWeight = FontWeight.Bold)
                QuietText(sleepGuide(answers.age))
                if (answers.focus == "growth") QuietText("Büyüme gözlemin bilgi seçimini etkiler; kalan santimetre veya GH puanı üretilmez.")
            }
        }
        Column(Modifier.padding(24.dp)) {
            BigButton(if (elapsed < PREPARATION_DURATION_MS) "Hazırlanıyor…" else "3 günlük denemeye geç", onReady, enabled = elapsed >= PREPARATION_DURATION_MS)
        }
    }
}
