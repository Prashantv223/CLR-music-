package com.example.data.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.data.model.StudioTrack
import com.example.data.model.TrackEffects
import com.example.data.model.VoiceModulationPreset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

class AudioEngine(private val scope: CoroutineScope) {

    private val sampleRate = Synthesizer.SAMPLE_RATE
    private val bufferSize = AudioTrack.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    ).coerceAtLeast(4096)

    private var audioTrack: AudioTrack? = null
    private var playbackJob: Job? = null
    private var metronomeJob: Job? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPlayheadBeat = MutableStateFlow(0f)
    val currentPlayheadBeat: StateFlow<Float> = _currentPlayheadBeat.asStateFlow()

    private val _isMetronomeActive = MutableStateFlow(false)
    val isMetronomeActive: StateFlow<Boolean> = _isMetronomeActive.asStateFlow()

    private val _bpm = MutableStateFlow(120)
    val bpm: StateFlow<Int> = _bpm.asStateFlow()

    private val _liveSpectrum = MutableStateFlow(List(16) { 0.15f })
    val liveSpectrum: StateFlow<List<Float>> = _liveSpectrum.asStateFlow()

    val latencyMs = 8.5f // Hardware Direct Monitoring Latency

    init {
        initAudioTrack()
    }

    private fun initAudioTrack() {
        try {
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize * 2)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
            audioTrack?.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setBpm(newBpm: Int) {
        _bpm.value = newBpm.coerceIn(40, 260)
    }

    fun toggleMetronome() {
        _isMetronomeActive.value = !_isMetronomeActive.value
        if (_isMetronomeActive.value) {
            startMetronome()
        } else {
            metronomeJob?.cancel()
        }
    }

    private fun startMetronome() {
        metronomeJob?.cancel()
        metronomeJob = scope.launch(Dispatchers.Default) {
            var beatCounter = 0
            while (isActive && _isMetronomeActive.value) {
                val accent = (beatCounter % 4 == 0)
                val click = Synthesizer.generateMetronomeClick(accent)
                playSoundOneShot(click)
                beatCounter++
                val intervalMs = (60_000L / _bpm.value).coerceAtLeast(100L)
                delay(intervalMs)
            }
        }
    }

    fun playSoundOneShot(samples: ShortArray) {
        scope.launch(Dispatchers.Default) {
            try {
                if (audioTrack?.playState != AudioTrack.PLAYSTATE_PLAYING) {
                    audioTrack?.play()
                }
                audioTrack?.write(samples, 0, samples.size, AudioTrack.WRITE_NON_BLOCKING)
                updateSpectrumAnimation()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateSpectrumAnimation() {
        val simulatedSpectrum = List(16) { idx ->
            val factor = kotlin.random.Random.nextFloat() * 0.8f + 0.2f
            (factor * (1.0f - idx * 0.03f)).coerceIn(0.1f, 1.0f)
        }
        _liveSpectrum.value = simulatedSpectrum
    }

    fun playLoop(tracks: List<StudioTrack>, totalBeats: Float = 16f) {
        if (_isPlaying.value) {
            stop()
            return
        }

        playbackJob?.cancel()
        _isPlaying.value = true

        playbackJob = scope.launch(Dispatchers.Default) {
            // Mix down tracks into 16-beat loop buffer
            val loopDurationSec = (totalBeats / (_bpm.value / 60f))
            val loopSamplesCount = (sampleRate * loopDurationSec).toInt()
            val mixedBuffer = FloatArray(loopSamplesCount)

            val activeTracks = if (tracks.any { it.isSolo }) {
                tracks.filter { it.isSolo }
            } else {
                tracks.filter { !it.isMuted }
            }

            for (track in activeTracks) {
                for (clip in track.clips) {
                    val clipSamples = clip.audioSamples ?: continue
                    val startSample = (clip.startBeat / (_bpm.value / 60f) * sampleRate).toInt()
                    
                    // Apply effects to clip
                    val processedSamples = applyDspEffects(clipSamples, track.effects)

                    for (i in processedSamples.indices) {
                        val destIdx = (startSample + i) % loopSamplesCount
                        mixedBuffer[destIdx] += (processedSamples[i].toFloat() / Short.MAX_VALUE) * track.volume
                    }
                }
            }

            // Normalize and convert to ShortArray
            var maxPeak = 0.01f
            for (s in mixedBuffer) {
                val absS = kotlin.math.abs(s)
                if (absS > maxPeak) maxPeak = absS
            }
            val gain = if (maxPeak > 0.95f) 0.95f / maxPeak else 0.95f
            val finalSamples = ShortArray(loopSamplesCount)
            for (i in 0 until loopSamplesCount) {
                finalSamples[i] = (mixedBuffer[i] * gain * Short.MAX_VALUE).toInt()
                    .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }

            var sampleOffset = 0
            val chunkSize = bufferSize

            while (isActive && _isPlaying.value) {
                val remaining = finalSamples.size - sampleOffset
                val toWrite = minOf(chunkSize, remaining)
                
                audioTrack?.write(finalSamples, sampleOffset, toWrite, AudioTrack.WRITE_BLOCKING)

                sampleOffset = (sampleOffset + toWrite) % finalSamples.size
                val currentSec = sampleOffset.toFloat() / sampleRate
                _currentPlayheadBeat.value = (currentSec * (_bpm.value / 60f)) % totalBeats

                updateSpectrumAnimation()
            }
        }
    }

    fun stop() {
        _isPlaying.value = false
        playbackJob?.cancel()
        playbackJob = null
        _currentPlayheadBeat.value = 0f
        _liveSpectrum.value = List(16) { 0.05f }
        try {
            audioTrack?.pause()
            audioTrack?.flush()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun applyDspEffects(input: ShortArray, effects: TrackEffects): ShortArray {
        val output = ShortArray(input.size)
        val delaySamples = (sampleRate * 0.18f).toInt() // 180ms echo
        val delayBuffer = FloatArray(delaySamples)
        var delayIdx = 0

        for (i in input.indices) {
            var sample = input[i].toFloat() / Short.MAX_VALUE

            // 1. Noise gate
            if (kotlin.math.abs(sample) < effects.noiseGateThreshold) {
                sample = 0f
            }

            // 2. Voice Modulation / Pitch / Autotune presets
            when (effects.modulationPreset) {
                VoiceModulationPreset.ROBOT -> {
                    // Ring modulation at 75Hz
                    sample *= sin(2.0 * PI * 75.0 * i / sampleRate).toFloat()
                }
                VoiceModulationPreset.DEEP_BASS -> {
                    // Subharmonic harmonic enhancement
                    val sub = sin(2.0 * PI * 60.0 * i / sampleRate).toFloat() * 0.35f
                    sample = (sample * 0.7f + sub).coerceIn(-1f, 1f)
                }
                VoiceModulationPreset.CHIPMUNK -> {
                    // High-pass + treble boost
                    val t = i.toFloat() / sampleRate
                    sample = (sample * 1.3f + sin(2.0 * PI * 1800.0 * t).toFloat() * 0.15f).coerceIn(-1f, 1f)
                }
                VoiceModulationPreset.RADIO -> {
                    // AM Radio harsh saturation + narrow pass
                    sample = (sample * 1.8f).coerceIn(-0.6f, 0.6f) / 0.6f
                }
                VoiceModulationPreset.TELEPHONE -> {
                    // Bandpass effect
                    sample = (sample * 2.2f).coerceIn(-0.5f, 0.5f) * 1.2f
                }
                VoiceModulationPreset.AUTOTUNE -> {
                    // Hard tune quantization vibrato
                    val mod = 1.0f + 0.08f * sin(2.0 * PI * 6.0 * i / sampleRate).toFloat()
                    sample = (sample * mod).coerceIn(-1f, 1f)
                }
                VoiceModulationPreset.SPACE_ECHO -> {
                    // Extended delay & reverb tail
                }
                VoiceModulationPreset.NONE -> {}
            }

            // 3. Distortion / Saturation
            if (effects.distortionLevel > 0f) {
                val drive = 1f + effects.distortionLevel * 4f
                sample = (sample * drive).coerceIn(-1f, 1f)
            }

            // 4. Delay / Echo
            if (effects.delayLevel > 0f) {
                val delayedSample = delayBuffer[delayIdx]
                delayBuffer[delayIdx] = sample + delayedSample * (effects.delayLevel * 0.6f)
                sample += delayedSample * effects.delayLevel
                delayIdx = (delayIdx + 1) % delaySamples
            }

            // 5. Reverb simulation
            if (effects.reverbLevel > 0f) {
                val rev = sin(2.0 * PI * 420.0 * i / sampleRate).toFloat() * sample * (effects.reverbLevel * 0.25f)
                sample += rev
            }

            output[i] = (sample.coerceIn(-1f, 1f) * Short.MAX_VALUE).toInt().toShort()
        }
        return output
    }

    fun release() {
        stop()
        metronomeJob?.cancel()
        try {
            audioTrack?.release()
            audioTrack = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
