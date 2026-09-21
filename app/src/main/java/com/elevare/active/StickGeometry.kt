package com.elevare.active

import kotlin.math.*

private val TAU = (2.0 * PI).toFloat()
private const val FLOOR = 94f
private const val THIGH = 25f
private const val SHIN = 25f
private const val UPPER_ARM = 16f
private const val FOREARM = 15f
private const val TORSO = 28f
private fun smooth(t: Float): Float = t.coerceIn(0f, 1f).let { it * it * (3f - 2f * it) }
private fun normalizedCycle(t: Float) = if (t.isFinite()) ((t % 1f) + 1f) % 1f else 0f
private fun mix(a: Float, b: Float, t: Float) = a + (b - a) * t
private fun at(p: Joint, length: Float, angle: Float) = Joint(p.x + length * cos(angle), p.y + length * sin(angle))
private fun lerp(a: Joint, b: Joint, t: Float) = Joint(mix(a.x, b.x, t), mix(a.y, b.y, t))

data class Joint(val x: Float, val y: Float)
// Head, neck, hip, front elbow/hand, rear elbow/hand, front knee/foot, rear knee/foot.
// Metadata is decorative only: these drawings are not validated biomechanical measurements.
data class StickFrame(
    val joints: List<Joint>,
    val support: String = "",
    val frontContact: Boolean = false,
    val rearContact: Boolean = false,
    val groundOffset: Float = 0f,
    val spineCurve: Float = 0f,
    val stage: String = ""
)

private data class Chain(val middle: Joint, val end: Joint)
private fun chain(root: Joint, target: Joint, first: Float, second: Float, bend: Float = 1f): Chain {
    val dx = target.x - root.x
    val dy = target.y - root.y
    val original = hypot(dx, dy).coerceAtLeast(.001f)
    val distance = original.coerceIn(abs(first - second) + .001f, first + second - .001f)
    val direction = Joint(dx / original, dy / original)
    val end = Joint(root.x + direction.x * distance, root.y + direction.y * distance)
    val along = (first * first - second * second + distance * distance) / (2f * distance)
    val height = sqrt((first * first - along * along).coerceAtLeast(0f))
    return Chain(
        Joint(root.x + direction.x * along + direction.y * height * bend,
            root.y + direction.y * along - direction.x * height * bend), end)
}
private fun arm(root: Joint, angle: Float, bend: Float = -1.5f): Chain {
    val elbow = at(root, UPPER_ARM, angle)
    return Chain(elbow, at(elbow, FOREARM, angle + bend))
}
private fun pose(
    hip: Joint, torsoAngle: Float = -PI.toFloat() / 2,
    frontFoot: Joint = Joint(38f, FLOOR), rearFoot: Joint = Joint(62f, FLOOR),
    frontArmAngle: Float = 2.1f, rearArmAngle: Float = 1.04f,
    frontHand: Joint? = null, rearHand: Joint? = null,
    frontKneeBend: Float = 1f, rearKneeBend: Float = -1f,
    support: String = "", floorHead: Boolean = false, spine: Float = 0f, stage: String = ""
): StickFrame {
    val neck = at(hip, TORSO, torsoAngle)
    val head = if (floorHead) at(neck, 12f, -.4f) else at(neck, 12f, -PI.toFloat() / 2)
    val fa = frontHand?.let { chain(neck, it, UPPER_ARM, FOREARM, -1f) } ?: arm(neck, frontArmAngle, .15f)
    val ra = rearHand?.let { chain(neck, it, UPPER_ARM, FOREARM, 1f) } ?: arm(neck, rearArmAngle, -.15f)
    val fl = chain(hip, frontFoot, THIGH, SHIN, frontKneeBend)
    val rl = chain(hip, rearFoot, THIGH, SHIN, rearKneeBend)
    return StickFrame(listOf(head, neck, hip, fa.middle, fa.end, ra.middle, ra.end,
        fl.middle, fl.end, rl.middle, rl.end), support, spineCurve = spine, stage = stage)
}

fun motionDuration(id: String) = when (id) {
    "sprint" -> 860
    "walk" -> 1500
    "march" -> 1800
    "catcow" -> 10000
    "child", "lunge" -> 14000
    "squat", "wall" -> 5500
    "calf", "reach" -> 6500
    else -> 6000
}
fun sprintFrames(): List<StickFrame> = (0..7).map { runningFrame(it / 8f) }

/** Stance feet move exactly with the track, then fold for recovery and advance.
 * Cubic swing endpoints inherit the contact velocity, avoiding puppet-like pose lerping. */
fun runningFrame(t: Float, walking: Boolean = false): StickFrame {
    val c = normalizedCycle(t)
    val stance = if (walking) .62f else .4f
    val travel = if (walking) 27f else 39f
    fun contact(p: Float) = normalizedCycle(p) <= stance
    val frontContact = contact(c)
    val rearContact = contact(c + .5f)
    val flight = if (walking || frontContact || rearContact) 0f else {
        val p = if (c < .5f) (c - stance) / (.5f - stance) else (c - .5f - stance) / (.5f - stance)
        sin(p.coerceIn(0f, 1f) * PI.toFloat()).pow(2f)
    }
    val hip = Joint(49f, if (walking) 51f - .6f * cos(c * TAU * 2f) else 52f - 2.8f * flight)
    val neck = at(hip, TORSO, if (walking) -1.52f else -1.36f)
    val head = at(neck, 12f, -1.48f)
    fun leg(offset: Float): Chain {
        val p = normalizedCycle(c + offset)
        val leading = 49f + travel / 2f
        val trailing = leading - travel
        val foot = if (p <= stance) {
            Joint(leading - travel * p / stance, FLOOR)
        } else {
            val u = (p - stance) / (1f - stance)
            val u2 = u * u; val u3 = u2 * u
            val velocity = -travel / stance * (1f - stance)
            val x = (2f*u3-3f*u2+1f)*trailing + (u3-2f*u2+u)*velocity +
                (-2f*u3+3f*u2)*leading + (u3-u2)*velocity
            val lift = if (walking) 8f else 32f
            Joint(x, FLOOR - lift * sin(PI.toFloat() * u).pow(2f))
        }
        return chain(hip, foot, THIGH, SHIN, 1f)
    }
    val front = leg(0f); val rear = leg(.5f)
    val swing = sin(c * TAU - .65f)
    val amplitude = if (walking) .34f else .85f
    val fa = arm(neck, 1.6f - swing * amplitude, if (walking) -1.05f else -1.8f)
    val ra = arm(neck, 1.6f + swing * amplitude, if (walking) -1.05f else -1.8f)
    return StickFrame(listOf(head, neck, hip, fa.middle, fa.end, ra.middle, ra.end,
        front.middle, front.end, rear.middle, rear.end), frontContact = frontContact,
        rearContact = rearContact, groundOffset = c * travel / stance,
        stage = if (walking) "Rahat adımlar" else "Kontrollü koşu")
}

fun holdAmount(progress: Float): Float {
    val p = if (progress.isFinite()) progress.coerceIn(0f, 1f) else 0f
    return when {
        p < .2f -> smooth(p / .2f)
        p > .84f -> 1f - smooth((p - .84f) / .16f)
        else -> 1f
    }
}
fun techniqueProgress(phase: String, progress: Float): Float = when (phase) {
    "prepare" -> progress.coerceIn(0f, 1f) * .2f
    "exit" -> .84f + progress.coerceIn(0f, 1f) * .16f
    "main" -> .2f + progress.coerceIn(0f, 1f) * .64f
    else -> progress.coerceIn(0f, 1f)
}
fun poseStage(id: String, progress: Float, side: String = ""): String {
    val p = progress.coerceIn(0f, 1f)
    val suffix = when (side) { "left" -> " · Sol taraf"; "right" -> " · Sağ taraf"; else -> "" }
    return if (id in listOf("child", "lunge", "balance", "reach")) when {
        p < .2f -> "Yavaşça yerleş$suffix"
        p > .84f -> "Kontrollü çık$suffix"
        else -> "Rahatça tut$suffix"
    } else motionCue(id)
}

/** Whole-step progress gives yoga one entrance, a genuine hold, then one exit.
 * The preview uses this same deliberate, slow storyboard, not a pumping loop. */
fun motionFrame(id: String, cycle: Float, stepProgress: Float? = null, side: String = ""): StickFrame {
    val c = normalizedCycle(cycle)
    if (id == "sprint" || id == "walk") return runningFrame(c, id == "walk")
    val wave = (1f - cos(c * TAU)) / 2f
    val progress = stepProgress?.coerceIn(0f, 1f) ?: c
    val hold = holdAmount(progress)
    val scene = when (id) {
        "march" -> {
            val s = sin(c * TAU)
            pose(Joint(50f, 51f), frontFoot = Joint(42f, FLOOR - max(0f, s) * 16f),
                rearFoot = Joint(58f, FLOOR - max(0f, -s) * 16f),
                frontArmAngle = 2.1f + s * .3f, rearArmAngle = 1.04f + s * .3f,
                stage = "Küçük, rahat adımlar")
        }
        "catcow" -> pose(Joint(40f, 64f), torsoAngle = 0f,
            frontFoot = Joint(15f, FLOOR), rearFoot = Joint(15f, FLOOR),
            frontHand = Joint(70f, FLOOR), rearHand = Joint(67f, FLOOR),
            frontKneeBend = 1f, rearKneeBend = 1f, support = "mat", floorHead = true,
            spine = sin(c * TAU) * 6f * (if(stepProgress==null)1f else hold), stage = if (c < .5f) "Sırtını rahatça yuvarla" else "Nötre dön, hafifçe aç")
        "child" -> pose(lerp(Joint(40f, 64f), Joint(27f, 72f), hold), torsoAngle = .52f * hold,
            frontFoot = Joint(15f, FLOOR), rearFoot = Joint(15f, FLOOR),
            frontHand = lerp(Joint(70f, FLOOR), Joint(80f, FLOOR), hold),
            rearHand = lerp(Joint(67f, FLOOR), Joint(77f, FLOOR), hold),
            frontKneeBend = 1f, rearKneeBend = 1f, support = "mat", floorHead = true,
            stage = poseStage(id, progress, side))
        "lunge" -> pose(lerp(Joint(43f, 66f), Joint(49f, 72f), hold),
            torsoAngle = -1.48f,
            frontFoot = lerp(Joint(62f, FLOOR), Joint(77f, FLOOR), hold),
            rearFoot = lerp(Joint(17f, FLOOR), Joint(10f, FLOOR), hold),
            frontArmAngle = 1.05f, rearArmAngle = 1.9f,
            frontKneeBend = 1f, rearKneeBend = 1f, support = "mat", stage = poseStage(id, progress, side))
        "squat" -> {
            val amount = smooth(wave)
            pose(lerp(Joint(48f, 49f), Joint(60f, 69f), amount),
                torsoAngle = mix(-1.57f, -1.94f, amount),
                frontFoot = Joint(41f, FLOOR), rearFoot = Joint(43f, FLOOR),
                frontArmAngle = mix(1.7f, 2.8f, amount), rearArmAngle = mix(1.6f, 2.7f, amount),
                frontKneeBend = -1f, rearKneeBend = -1f, support = "chair",
                stage = if (c < .5f) "Kalça geriye · kontrollü otur" else "Yavaşça kalk")
        }
        "wall" -> pose(lerp(Joint(46f, 58f), Joint(50f, 58f), wave), torsoAngle = mix(-1.36f, -1.08f, wave),
            frontFoot = Joint(28f, FLOOR), rearFoot = Joint(31f, FLOOR),
            frontHand = Joint(82f, 33f), rearHand = Joint(82f, 36f),
            frontKneeBend = -1f, rearKneeBend = -1f, support = "wall",
            stage = if (c < .5f) "Duvara kontrollü yaklaş" else "Duvarı yavaşça it")
        "calf" -> {
            val lift = holdAmount(c) * 4f
            pose(Joint(49f, 49f - lift), frontFoot = Joint(39f, FLOOR - lift), rearFoot = Joint(59f, FLOOR - lift),
                rearHand = Joint(76f, 30f), support = "rail", stage = "Desteği tut · yavaşça yüksel ve in")
        }
        "balance" -> pose(Joint(49f, 48f), frontFoot = Joint(42f, FLOOR),
            rearFoot = lerp(Joint(59f, FLOOR), Joint(64f, 79f), hold),
            rearHand = Joint(76f, 28f), support = "rail", stage = poseStage(id, progress, side))
        "step" -> {
            val s = sin(c * TAU)
            pose(Joint(50f + s * 5f, 49f),
                frontFoot = Joint(38f - max(0f, -s) * 7f, FLOOR),
                rearFoot = Joint(62f + max(0f, s) * 7f, FLOOR), stage = "Küçük yana adım · zıplama yok")
        }
        "reach" -> pose(Joint(50f, 52f),
            frontArmAngle = mix(2.1f, 3.86f, hold), rearArmAngle = mix(1.04f, -.72f, hold),
            stage = poseStage(id, progress, side))
        "shoulder" -> pose(Joint(50f, 49f),
            frontArmAngle = 1.95f + sin(c * TAU) * .1f,
            rearArmAngle = 1.18f - sin(c * TAU) * .1f, stage = "Omuzlarda küçük, yavaş çember")
        "breath" -> pose(Joint(50f, 49f), stage = "Doğal nefes · tutma yok")
        else -> pose(Joint(50f, 49f))
    }
    return if (side == "right" && id in listOf("lunge", "balance")) {
        scene.copy(joints = scene.joints.map { Joint(100f - it.x, it.y) })
    } else scene
}
fun stickFrame(id: String, phase: Float): StickFrame =
    motionFrame(id, phase.coerceIn(0f, 1f), phase.coerceIn(0f, 1f))
fun stickFrames(id: String) = stickFrame(id, 0f) to stickFrame(id, .5f)
fun motionCue(id: String) = when (id) {
    "sprint" -> "Rahat omuzlar · kontrollü hızlan · tam efora çıkma"
    "walk" -> "Kısa, rahat adımlar · temponu düşür"
    "march" -> "Küçük adımlar · doğal nefes"
    "catcow" -> "Yavaşça yuvarla · nötre dön · boynunu rahat tut"
    "child" -> "Yavaşça yerleş · rahat tut · kontrollü çık"
    "lunge" -> "Diz yumuşak zeminde · gövdeni rahat uzat"
    "squat" -> "Kalça geriye · kontrollü otur ve kalk"
    "wall" -> "Eller duvarda · kontrollü it"
    "calf" -> "Desteği tut · topukları yavaşça kaldır"
    "balance" -> "Desteğe yakın kal · zorlamadan dene"
    "reach" -> "Rahatça uzan · belini zorlamadan dön"
    "shoulder" -> "Omuzlarda küçük ve yavaş çember"
    "step" -> "Yana adım · diğer ayağını yaklaştır"
    "breath" -> "Doğal nefes · tutma yok"
    else -> "Kendi hızında hareket et"
}
