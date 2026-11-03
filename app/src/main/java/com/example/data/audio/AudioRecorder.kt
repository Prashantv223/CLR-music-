package com.example.data.audio

import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale
import kotlin.math.abs

class AudioRecorder(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val sampleRate = Synthesizer.SAMPLE_RATE
    private val bufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    ).coerceAtLeast(4096)

    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private var recordedSamples = ArrayList<Short>()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _liveAmplitude = MutableStateFlow(0f)
    val liveAmplitude: StateFlow<Float> = _liveAmplitude.asStateFlow()

    private val _waveformPoints = MutableStateFlow<List<Float>>(emptyList())
    val waveformPoints: StateFlow<List<Float>> = _waveformPoints.asStateFlow()

    private val _transcribedText = MutableStateFlow("")
    val transcribedText: StateFlow<String> = _transcribedText.asStateFlow()

    private val _isTranscribing = MutableStateFlow(false)
    val isTranscribing: StateFlow<Boolean> = _isTranscribing.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null

    var noiseSuppressionThreshold: Float = 0.04f

    fun startRecording(onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        if (_isRecording.value) return
        recordedSamples.clear()
        _waveformPoints.value = emptyList()

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize * 2
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                onError("Microphone permission or hardware unavailable")
                return
            }

            audioRecord?.startRecording()
            _isRecording.value = true
            onSuccess()

            recordingJob = scope.launch(Dispatchers.IO) {
                val tempBuffer = ShortArray(bufferSize)
                val liveAmps = ArrayList<Float>()

                while (isActive && _isRecording.value) {
                    val readCount = audioRecord?.read(tempBuffer, 0, tempBuffer.size) ?: 0
                    if (readCount > 0) {
                        var maxAmp = 0
                        for (i in 0 until readCount) {
                            var sample = tempBuffer[i]
                            // Automated Noise Suppression Gate
                            val normalized = abs(sample.toInt()).toFloat() / Short.MAX_VALUE
                            if (normalized < noiseSuppressionThreshold) {
                                sample = 0
                            }
                            recordedSamples.add(sample)
                            if (abs(sample.toInt()) > maxAmp) {
                                maxAmp = abs(sample.toInt())
                            }
                        }

                        val rms = (maxAmp.toFloat() / Short.MAX_VALUE).coerceIn(0.02f, 1.0f)
                        _liveAmplitude.value = rms
                        liveAmps.add(rms)
                        if (liveAmps.size > 80) {
                            liveAmps.removeAt(0)
                        }
                        _waveformPoints.value = liveAmps.toList()
                    }
                }
            }
        } catch (e: SecurityException) {
            onError("Microphone permission required")
        } catch (e: Exception) {
            onError("Recording failed: ${e.message}")
        }
    }

    fun stopRecording(destFile: File? = null): ShortArray {
        _isRecording.value = false
        recordingJob?.cancel()
        recordingJob = null

        try {
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val samplesArray = recordedSamples.toShortArray()
        if (destFile != null && samplesArray.isNotEmpty()) {
            WavExporter.writeWavFile(destFile, samplesArray, sampleRate, 1)
        }
        return samplesArray
    }

    fun startSpeechTranscription(onResult: (String) -> Unit) {
        scope.launch(Dispatchers.Main) {
            if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                _transcribedText.value = "Speech recognition service unavailable on this device."
                return@launch
            }

            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            _isTranscribing.value = true

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }

            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    _isTranscribing.value = false
                }
                override fun onError(error: Int) {
                    _isTranscribing.value = false
                }
                override fun onResults(results: Bundle?) {
                    _isTranscribing.value = false
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull() ?: ""
                    if (text.isNotBlank()) {
                        _transcribedText.value = text
                        onResult(text)
                    }
                }
                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    matches?.firstOrNull()?.let {
                        _transcribedText.value = it
                    }
                }
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            speechRecognizer?.startListening(intent)
        }
    }

    fun stopSpeechTranscription() {
        _isTranscribing.value = false
        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    fun release() {
        stopRecording()
        stopSpeechTranscription()
    }
}
