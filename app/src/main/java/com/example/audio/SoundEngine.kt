package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/**
 * Lightweight 8-bit chiptune synthesizer using Android AudioTrack.
 * Generates retro RPG blips, slashes, healing chimes, and save star arpeggios without external audio assets.
 */
class SoundEngine {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    var isMuted: Boolean = false

    private val sampleRate = 22050

    fun playBlip(freqHz: Float = 440f) {
        if (isMuted) return
        playToneSequence(listOf(ToneStep(freqHz, 24, WaveType.SQUARE, 0.14f)))
    }

    fun playMenuMove() {
        if (isMuted) return
        playToneSequence(listOf(ToneStep(587f, 25, WaveType.SQUARE, 0.16f)))
    }

    fun playMenuSelect() {
        if (isMuted) return
        playToneSequence(
            listOf(
                ToneStep(659f, 35, WaveType.SQUARE, 0.18f),
                ToneStep(880f, 55, WaveType.SQUARE, 0.18f)
            )
        )
    }

    fun playSlash() {
        if (isMuted) return
        playToneSequence(
            listOf(
                ToneStep(900f, 35, WaveType.NOISE, 0.25f),
                ToneStep(450f, 45, WaveType.SQUARE, 0.22f),
                ToneStep(220f, 60, WaveType.NOISE, 0.18f)
            )
        )
    }

    fun playHit() {
        if (isMuted) return
        playToneSequence(
            listOf(
                ToneStep(160f, 45, WaveType.SQUARE, 0.30f),
                ToneStep(95f, 80, WaveType.NOISE, 0.28f)
            )
        )
    }

    fun playSoulHurt() {
        if (isMuted) return
        playToneSequence(
            listOf(
                ToneStep(190f, 45, WaveType.SQUARE, 0.28f),
                ToneStep(130f, 65, WaveType.SQUARE, 0.28f)
            )
        )
    }

    fun playHeal() {
        if (isMuted) return
        playToneSequence(
            listOf(
                ToneStep(523.25f, 50, WaveType.TRIANGLE, 0.22f),
                ToneStep(659.25f, 50, WaveType.TRIANGLE, 0.22f),
                ToneStep(783.99f, 65, WaveType.TRIANGLE, 0.22f),
                ToneStep(1046.5f, 110, WaveType.TRIANGLE, 0.24f)
            )
        )
    }

    fun playSaveStar() {
        if (isMuted) return
        playToneSequence(
            listOf(
                ToneStep(587.33f, 70, WaveType.TRIANGLE, 0.24f),
                ToneStep(739.99f, 70, WaveType.TRIANGLE, 0.24f),
                ToneStep(880.00f, 70, WaveType.TRIANGLE, 0.24f),
                ToneStep(1174.66f, 150, WaveType.TRIANGLE, 0.26f)
            )
        )
    }

    fun playSpare() {
        if (isMuted) return
        playToneSequence(
            listOf(
                ToneStep(440f, 65, WaveType.TRIANGLE, 0.20f),
                ToneStep(554.37f, 65, WaveType.TRIANGLE, 0.20f),
                ToneStep(659.25f, 120, WaveType.TRIANGLE, 0.22f)
            )
        )
    }

    fun playEncounter() {
        if (isMuted) return
        playToneSequence(
            listOf(
                ToneStep(784f, 45, WaveType.SQUARE, 0.22f),
                ToneStep(1046f, 90, WaveType.SQUARE, 0.24f)
            )
        )
    }

    private fun playToneSequence(steps: List<ToneStep>) {
        scope.launch {
            runCatching {
                val totalSamples = steps.sumOf { (sampleRate * it.durationMs) / 1000 }
                if (totalSamples <= 0) return@runCatching
                val buffer = ShortArray(totalSamples)
                var offset = 0
                for (step in steps) {
                    val count = (sampleRate * step.durationMs) / 1000
                    for (i in 0 until count) {
                        val t = i.toFloat() / sampleRate
                        val envelope = 1f - (i.toFloat() / count) * 0.35f
                        val raw = when (step.wave) {
                            WaveType.SQUARE -> if (sin(2.0 * PI * step.freqHz * t) >= 0.0) 1f else -1f
                            WaveType.TRIANGLE -> {
                                val phase = (t * step.freqHz) % 1f
                                4f * kotlin.math.abs(phase - 0.5f) - 1f
                            }
                            WaveType.NOISE -> (Random.nextFloat() * 2f - 1f)
                        }
                        val sample = (raw * step.volume * envelope * Short.MAX_VALUE)
                            .toInt()
                            .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                            .toShort()
                        if (offset < buffer.size) {
                            buffer[offset++] = sample
                        }
                    }
                }

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .setBufferSizeInBytes(buffer.size * 2)
                    .build()

                track.write(buffer, 0, buffer.size)
                track.play()
                val totalMs = steps.sumOf { it.durationMs }.toLong() + 40L
                kotlinx.coroutines.delay(totalMs)
                track.stop()
                track.release()
            }
        }
    }

    private enum class WaveType { SQUARE, TRIANGLE, NOISE }
    private data class ToneStep(
        val freqHz: Float,
        val durationMs: Int,
        val wave: WaveType,
        val volume: Float
    )
}
