package com.example.lineofthree.feedback

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

enum class Sound { PLACE, MOVE, LINE, REMOVE, WIN }

/**
 * Restrained, synthesized sound effects. Short tones are rendered once to WAV files in
 * the cache directory and played through a [SoundPool], so no audio assets are shipped.
 */
class SoundEffects(context: Context) {

    private val pool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val loaded = ConcurrentHashMap.newKeySet<Int>()
    private val soundIds: Map<Sound, Int>

    init {
        pool.setOnLoadCompleteListener { _, id, status -> if (status == 0) loaded += id }
        val dir = File(context.cacheDir, "sfx").apply { mkdirs() }
        soundIds = Sound.entries.associateWith { sound ->
            val file = File(dir, "${sound.name.lowercase()}_v1.wav")
            if (!file.exists()) writeWav(file, synthesize(sound))
            pool.load(file.path, 1)
        }
    }

    fun play(sound: Sound) {
        val id = soundIds[sound] ?: return
        if (id in loaded) pool.play(id, 1f, 1f, 1, 0, 1f)
    }

    fun release() = pool.release()

    private fun synthesize(sound: Sound): FloatArray = when (sound) {
        // A soft wooden knock.
        Sound.PLACE -> render(0.09f) { t -> knock(t, 420f, 0.030f) + 0.35f * knock(t, 1180f, 0.012f) + click(t) }
        Sound.MOVE -> render(0.08f) { t -> 0.85f * knock(t, 340f, 0.026f) + 0.25f * knock(t, 900f, 0.010f) + 0.6f * click(t) }
        // Two quiet bell partials a fifth apart.
        Sound.LINE -> render(0.36f) { t -> bell(t, 660f, 0.12f) + bell(t - 0.07f, 990f, 0.14f) }
        // A low, falling thud.
        Sound.REMOVE -> render(0.2f) { t ->
            val f = 190f - 80f * (t / 0.2f)
            0.9f * sin(2f * PI.toFloat() * f * t) * exp(-t / 0.05f) + 0.4f * click(t)
        }
        // A rising three-note chord.
        Sound.WIN -> render(0.9f) { t -> bell(t, 523.25f, 0.28f) + bell(t - 0.11f, 659.25f, 0.28f) + bell(t - 0.22f, 783.99f, 0.34f) }
    }

    private fun knock(t: Float, freq: Float, decay: Float) =
        sin(2f * PI.toFloat() * freq * t) * exp(-t / decay)

    private fun click(t: Float): Float {
        if (t > 0.006f) return 0f
        val i = ((t * SAMPLE_RATE).toInt()) % noise.size
        return noise[i] * 0.3f * (1f - t / 0.006f)
    }

    private fun bell(t: Float, freq: Float, decay: Float): Float {
        if (t < 0f) return 0f
        val attack = (t / 0.004f).coerceAtMost(1f)
        return attack * exp(-t / decay) * (sin(2f * PI.toFloat() * freq * t) + 0.18f * sin(2f * PI.toFloat() * freq * 2.01f * t)) * 0.45f
    }

    private fun render(seconds: Float, fn: (Float) -> Float): FloatArray {
        val n = (seconds * SAMPLE_RATE).toInt()
        return FloatArray(n) { i ->
            val t = i / SAMPLE_RATE.toFloat()
            val tail = ((n - i) / (SAMPLE_RATE * 0.01f)).coerceAtMost(1f) // de-click the ending
            (fn(t) * VOLUME * tail).coerceIn(-1f, 1f)
        }
    }

    private fun writeWav(file: File, samples: FloatArray) {
        val dataSize = samples.size * 2
        val buffer = ByteBuffer.allocate(44 + dataSize).order(ByteOrder.LITTLE_ENDIAN)
        buffer.put("RIFF".toByteArray()).putInt(36 + dataSize).put("WAVE".toByteArray())
        buffer.put("fmt ".toByteArray()).putInt(16).putShort(1).putShort(1)
            .putInt(SAMPLE_RATE).putInt(SAMPLE_RATE * 2).putShort(2).putShort(16)
        buffer.put("data".toByteArray()).putInt(dataSize)
        samples.forEach { buffer.putShort((it * Short.MAX_VALUE).toInt().toShort()) }
        FileOutputStream(file).use { it.write(buffer.array()) }
    }

    private companion object {
        const val SAMPLE_RATE = 22050
        const val VOLUME = 0.5f
        val noise = Random(7).let { r -> FloatArray(256) { r.nextFloat() * 2f - 1f } }
    }
}
