package com.example.data.audio

import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

enum class WaveformType {
    SINE, SQUARE, SAWTOOTH, TRIANGLE
}

object Synthesizer {
    const val SAMPLE_RATE = 44100

    // Standard note frequencies (Hz)
    val NOTE_FREQUENCIES = mapOf(
        "C2" to 65.41, "C#2" to 69.30, "D2" to 73.42, "D#2" to 77.78, "E2" to 82.41, "F2" to 87.31,
        "F#2" to 92.50, "G2" to 98.00, "G#2" to 103.83, "A2" to 110.00, "A#2" to 116.54, "B2" to 123.47,
        "C3" to 130.81, "C#3" to 138.59, "D3" to 146.83, "D#3" to 155.56, "E3" to 164.81, "F3" to 174.61,
        "F#3" to 185.00, "G3" to 196.00, "G#3" to 207.65, "A3" to 220.00, "A#3" to 233.08, "B3" to 246.94,
        "C4" to 261.63, "C#4" to 277.18, "D4" to 293.66, "D#4" to 311.13, "E4" to 329.63, "F4" to 349.23,
        "F#4" to 369.99, "G4" to 392.00, "G#4" to 415.30, "A4" to 440.00, "A#4" to 466.16, "B4" to 493.88,
        "C5" to 523.25, "C#5" to 554.37, "D5" to 587.33, "D#5" to 622.25, "E5" to 659.25, "F5" to 698.46,
        "G5" to 783.99, "A5" to 880.00, "C6" to 1046.50
    )

    fun generateTone(
        frequency: Double,
        durationMs: Int,
        waveform: WaveformType = WaveformType.SINE,
        volume: Float = 0.8f
    ): ShortArray {
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val samples = ShortArray(numSamples)
        val attackSamples = (numSamples * 0.1).toInt().coerceAtLeast(1)
        val releaseSamples = (numSamples * 0.25).toInt().coerceAtLeast(1)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val raw = when (waveform) {
                WaveformType.SINE -> sin(2.0 * PI * frequency * t)
                WaveformType.SQUARE -> if (sin(2.0 * PI * frequency * t) >= 0) 0.8 else -0.8
                WaveformType.SAWTOOTH -> (2.0 * (t * frequency - kotlin.math.floor(t * frequency + 0.5)))
                WaveformType.TRIANGLE -> (2.0 * kotlin.math.abs(2.0 * (t * frequency - kotlin.math.floor(t * frequency + 0.5))) - 1.0)
            }

            // Envelope ADSR
            val env = when {
                i < attackSamples -> i.toFloat() / attackSamples
                i > numSamples - releaseSamples -> (numSamples - i).toFloat() / releaseSamples
                else -> 1.0f
            }

            val sampleVal = (raw * env * volume * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            samples[i] = sampleVal.toShort()
        }
        return samples
    }

    fun generateKick(durationMs: Int = 220): ShortArray {
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val samples = ShortArray(numSamples)
        var phase = 0.0

        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            // Rapid pitch drop from 160Hz to 42Hz
            val freq = 42.0 + (160.0 - 42.0) * exp(-progress * 12.0)
            phase += 2.0 * PI * freq / SAMPLE_RATE
            val envelope = exp(-progress * 6.0)

            // Add soft distortion click at start
            val click = if (i < 80) (Random.nextDouble() * 2.0 - 1.0) * 0.4 else 0.0
            val raw = sin(phase) * envelope + click
            val clamped = (raw.coerceIn(-1.0, 1.0) * 0.95 * Short.MAX_VALUE).toInt()
            samples[i] = clamped.toShort()
        }
        return samples
    }

    fun generateSnare(durationMs: Int = 180): ShortArray {
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val samples = ShortArray(numSamples)
        var phase = 0.0

        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val bodyFreq = 185.0 * exp(-progress * 8.0)
            phase += 2.0 * PI * bodyFreq / SAMPLE_RATE
            val body = sin(phase) * exp(-progress * 10.0) * 0.5
            val noise = (Random.nextDouble() * 2.0 - 1.0) * exp(-progress * 6.5) * 0.6
            val raw = body + noise
            samples[i] = (raw.coerceIn(-1.0, 1.0) * 0.9 * Short.MAX_VALUE).toInt().toShort()
        }
        return samples
    }

    fun generateHiHat(durationMs: Int = 80, open: Boolean = false): ShortArray {
        val actualDuration = if (open) 260 else durationMs
        val numSamples = (SAMPLE_RATE * (actualDuration / 1000.0)).toInt()
        val samples = ShortArray(numSamples)
        val decayRate = if (open) 5.0 else 24.0

        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            // Metallic ring + noise
            val ring = sin(2.0 * PI * 8500.0 * i / SAMPLE_RATE) * 0.3 + sin(2.0 * PI * 11200.0 * i / SAMPLE_RATE) * 0.2
            val noise = (Random.nextDouble() * 2.0 - 1.0) * 0.6
            val env = exp(-progress * decayRate)
            val raw = (ring + noise) * env
            samples[i] = (raw.coerceIn(-1.0, 1.0) * 0.8 * Short.MAX_VALUE).toInt().toShort()
        }
        return samples
    }

    fun generate808Bass(freq: Double = 55.0, durationMs: Int = 450): ShortArray {
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val samples = ShortArray(numSamples)
        var phase = 0.0

        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val curFreq = if (progress < 0.08) freq * (1.5 - progress * 6.0) else freq
            phase += 2.0 * PI * curFreq / SAMPLE_RATE
            val env = exp(-progress * 3.5)
            // Soft tube saturation
            var raw = sin(phase) * 1.4 * env
            if (raw > 1.0) raw = 1.0 - exp(-raw)
            if (raw < -1.0) raw = -1.0 + exp(raw)
            samples[i] = (raw.coerceIn(-1.0, 1.0) * 0.95 * Short.MAX_VALUE).toInt().toShort()
        }
        return samples
    }

    fun generateMetronomeClick(accent: Boolean): ShortArray {
        val freq = if (accent) 2200.0 else 1400.0
        val durationMs = 35
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val raw = sin(2.0 * PI * freq * i / SAMPLE_RATE) * exp(-progress * 25.0)
            samples[i] = (raw * 0.9 * Short.MAX_VALUE).toInt().toShort()
        }
        return samples
    }

    fun generateClap(durationMs: Int = 160): ShortArray {
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            // Clap consists of 3 micro-bursts followed by diffuse reverb tail
            val burstMultiplier = when {
                i < 200 -> 0.8
                i in 300..500 -> 0.9
                i in 600..800 -> 1.0
                else -> exp(-(progress - 0.15).coerceAtLeast(0.0) * 9.0)
            }
            val noise = (Random.nextDouble() * 2.0 - 1.0) * burstMultiplier
            samples[i] = (noise.coerceIn(-1.0, 1.0) * 0.85 * Short.MAX_VALUE).toInt().toShort()
        }
        return samples
    }
}
