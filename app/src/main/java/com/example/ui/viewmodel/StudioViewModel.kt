package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.SunoMusicGenerator
import com.example.data.audio.AudioEngine
import com.example.data.audio.AudioRecorder
import com.example.data.audio.Synthesizer
import com.example.data.audio.WavExporter
import com.example.data.model.AudioClip
import com.example.data.model.AudioTrackType
import com.example.data.model.CollabSession
import com.example.data.model.CollabUser
import com.example.data.model.EqualizerPreset
import com.example.data.model.LyricLine
import com.example.data.model.StudioProject
import com.example.data.model.StudioTrack
import com.example.data.model.SunoGeneratedSong
import com.example.data.model.TrackEffects
import com.example.data.model.VoiceModulationPreset
import com.example.ui.components.DEFAULT_BEAT_PADS
import com.example.ui.components.SequencerTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.random.Random

sealed interface StudioUiState {
    data object Idle : StudioUiState
    data class GeneratingSong(val message: String) : StudioUiState
    data class Success(val message: String) : StudioUiState
    data class Error(val message: String) : StudioUiState
}

class StudioViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    val audioEngine = AudioEngine(viewModelScope)
    val audioRecorder = AudioRecorder(context, viewModelScope)

    private val _uiState = MutableStateFlow<StudioUiState>(StudioUiState.Idle)
    val uiState: StateFlow<StudioUiState> = _uiState.asStateFlow()

    // 1. Multitrack Studio State
    private val _tracks = MutableStateFlow<List<StudioTrack>>(emptyList())
    val tracks: StateFlow<List<StudioTrack>> = _tracks.asStateFlow()

    private val _selectedTrackId = MutableStateFlow<String?>(null)
    val selectedTrackId: StateFlow<String?> = _selectedTrackId.asStateFlow()

    private val _projectLyrics = MutableStateFlow<List<LyricLine>>(emptyList())
    val projectLyrics: StateFlow<List<LyricLine>> = _projectLyrics.asStateFlow()

    private val _currentProject = MutableStateFlow<StudioProject>(
        StudioProject(
            id = "proj_1",
            title = "Midnight Neon Dream",
            bpm = 120,
            timeSignature = "4/4"
        )
    )
    val currentProject: StateFlow<StudioProject> = _currentProject.asStateFlow()

    // 2. Step Sequencer State
    private val _sequencerTracks = MutableStateFlow<List<SequencerTrack>>(emptyList())
    val sequencerTracks: StateFlow<List<SequencerTrack>> = _sequencerTracks.asStateFlow()

    private val _currentSequencerStep = MutableStateFlow(0)
    val currentSequencerStep: StateFlow<Int> = _currentSequencerStep.asStateFlow()

    private val _isSequencerPlaying = MutableStateFlow(false)
    val isSequencerPlaying: StateFlow<Boolean> = _isSequencerPlaying.asStateFlow()
    private var sequencerJob: Job? = null

    // 3. Suno AI Generated Songs Library
    private val _sunoSongs = MutableStateFlow<List<SunoGeneratedSong>>(emptyList())
    val sunoSongs: StateFlow<List<SunoGeneratedSong>> = _sunoSongs.asStateFlow()

    private val _activePlayingSong = MutableStateFlow<SunoGeneratedSong?>(null)
    val activePlayingSong: StateFlow<SunoGeneratedSong?> = _activePlayingSong.asStateFlow()

    // 4. Real-time Collaboration Session
    private val _collabSession = MutableStateFlow(
        CollabSession(
            roomId = "CLR-8492",
            roomName = "Neon Stems Collab Room",
            collaborators = listOf(
                CollabUser("u1", "You (Host)", 0xFF00E5FF, "Producer"),
                CollabUser("u2", "DJ Alex", 0xFFFF2A85, "Beatmaker"),
                CollabUser("u3", "Producer Priya", 0xFFFFB300, "Vocalist")
            )
        )
    )
    val collabSession: StateFlow<CollabSession> = _collabSession.asStateFlow()

    init {
        initDefaultProject()
        initSequencerPatterns()
    }

    private fun initDefaultProject() {
        viewModelScope.launch {
            // Generate initial demo project with Suno generator
            val (demoSong, initialTracks) = SunoMusicGenerator.generateSunoSong(
                context = context,
                promptTitle = "Midnight Neon Dream",
                promptLyrics = "[Intro] Electric bass begins to pulse\n[Verse 1] Driving fast under neon city towers\n[Chorus] CLR Music echoes through midnight hours\n[Outro] Fade out into the cyber night",
                genre = "Synthwave & Cyberpunk",
                mood = "Energetic & Hype",
                vocalStyle = "Autotuned Melodic"
            )
            _tracks.value = initialTracks
            _projectLyrics.value = demoSong.lyrics
            _selectedTrackId.value = initialTracks.firstOrNull()?.id
            _sunoSongs.value = listOf(demoSong)
            _activePlayingSong.value = demoSong
        }
    }

    private fun initSequencerPatterns() {
        val kickSteps = BooleanArray(16) { it % 4 == 0 } // 0, 4, 8, 12
        val snareSteps = BooleanArray(16) { it % 4 == 2 } // 2, 6, 10, 14
        val hatSteps = BooleanArray(16) { true } // every step
        val bassSteps = BooleanArray(16) { it == 0 || it == 6 || it == 10 }

        _sequencerTracks.value = listOf(
            SequencerTrack("KICK 808", androidx.compose.ui.graphics.Color(0xFF00E5FF), kickSteps),
            SequencerTrack("SNARE", androidx.compose.ui.graphics.Color(0xFFFF2A85), snareSteps),
            SequencerTrack("HI-HAT", androidx.compose.ui.graphics.Color(0xFFFFB300), hatSteps),
            SequencerTrack("SUB BASS", androidx.compose.ui.graphics.Color(0xFF7C4DFF), bassSteps)
        )
    }

    // Step Sequencer Playback
    fun toggleSequencerPlay() {
        if (_isSequencerPlaying.value) {
            stopSequencer()
        } else {
            startSequencer()
        }
    }

    private fun startSequencer() {
        _isSequencerPlaying.value = true
        sequencerJob?.cancel()
        sequencerJob = viewModelScope.launch(Dispatchers.Default) {
            var step = 0
            while (isActive && _isSequencerPlaying.value) {
                _currentSequencerStep.value = step

                // Trigger sounds for active tracks at this step
                val currentTracks = _sequencerTracks.value
                for ((index, track) in currentTracks.withIndex()) {
                    if (track.steps.getOrElse(step) { false }) {
                        when (index) {
                            0 -> audioEngine.playSoundOneShot(Synthesizer.generateKick())
                            1 -> audioEngine.playSoundOneShot(Synthesizer.generateSnare())
                            2 -> audioEngine.playSoundOneShot(Synthesizer.generateHiHat(open = (step % 4 == 3)))
                            3 -> audioEngine.playSoundOneShot(Synthesizer.generate808Bass())
                        }
                    }
                }

                step = (step + 1) % 16
                val stepIntervalMs = (60_000L / audioEngine.bpm.value) / 4L
                delay(stepIntervalMs.coerceAtLeast(40L))
            }
        }
    }

    fun stopSequencer() {
        _isSequencerPlaying.value = false
        sequencerJob?.cancel()
        sequencerJob = null
        _currentSequencerStep.value = 0
    }

    fun toggleSequencerStep(trackIndex: Int, stepIndex: Int) {
        val list = _sequencerTracks.value.toMutableList()
        if (trackIndex in list.indices) {
            val track = list[trackIndex]
            val newSteps = track.steps.clone()
            newSteps[stepIndex] = !newSteps[stepIndex]
            list[trackIndex] = track.copy(steps = newSteps)
            _sequencerTracks.value = list
        }
    }

    fun clearSequencer() {
        val cleared = _sequencerTracks.value.map { track ->
            track.copy(steps = BooleanArray(16) { false })
        }
        _sequencerTracks.value = cleared
    }

    fun loadSequencerPreset(presetName: String) {
        when (presetName) {
            "Trap Drill" -> {
                val kick = BooleanArray(16) { it == 0 || it == 6 || it == 10 }
                val snare = BooleanArray(16) { it == 4 || it == 12 }
                val hat = BooleanArray(16) { it % 2 == 0 || it in 9..11 }
                val bass = BooleanArray(16) { it == 0 || it == 8 }
                _sequencerTracks.value = listOf(
                    _sequencerTracks.value[0].copy(steps = kick),
                    _sequencerTracks.value[1].copy(steps = snare),
                    _sequencerTracks.value[2].copy(steps = hat),
                    _sequencerTracks.value[3].copy(steps = bass)
                )
            }
            "Lo-Fi Chill" -> {
                val kick = BooleanArray(16) { it == 0 || it == 7 }
                val snare = BooleanArray(16) { it == 4 || it == 12 }
                val hat = BooleanArray(16) { it % 2 == 0 }
                val bass = BooleanArray(16) { it == 0 || it == 8 }
                _sequencerTracks.value = listOf(
                    _sequencerTracks.value[0].copy(steps = kick),
                    _sequencerTracks.value[1].copy(steps = snare),
                    _sequencerTracks.value[2].copy(steps = hat),
                    _sequencerTracks.value[3].copy(steps = bass)
                )
            }
            else -> initSequencerPatterns()
        }
    }

    // Drum Pad & Virtual Keyboard Trigger
    fun triggerPad(padId: String) {
        when (padId) {
            "pad_kick" -> audioEngine.playSoundOneShot(Synthesizer.generateKick())
            "pad_snare" -> audioEngine.playSoundOneShot(Synthesizer.generateSnare())
            "pad_hihat_c" -> audioEngine.playSoundOneShot(Synthesizer.generateHiHat(open = false))
            "pad_hihat_o" -> audioEngine.playSoundOneShot(Synthesizer.generateHiHat(open = true))
            "pad_clap" -> audioEngine.playSoundOneShot(Synthesizer.generateClap())
            "pad_808" -> audioEngine.playSoundOneShot(Synthesizer.generate808Bass(55.0))
            "pad_synth_c" -> audioEngine.playSoundOneShot(
                Synthesizer.generateTone(261.63, 300, com.example.data.audio.WaveformType.SAWTOOTH)
            )
            "pad_synth_g" -> audioEngine.playSoundOneShot(
                Synthesizer.generateTone(392.00, 300, com.example.data.audio.WaveformType.SAWTOOTH)
            )
            "pad_vocal_yeah" -> audioEngine.playSoundOneShot(
                Synthesizer.generateTone(523.25, 200, com.example.data.audio.WaveformType.SINE, 0.7f)
            )
            "pad_vocal_drop" -> audioEngine.playSoundOneShot(
                Synthesizer.generateTone(440.00, 220, com.example.data.audio.WaveformType.SINE, 0.7f)
            )
            "pad_crash" -> audioEngine.playSoundOneShot(Synthesizer.generateHiHat(durationMs = 350, open = true))
            "pad_rim" -> audioEngine.playSoundOneShot(Synthesizer.generateSnare(durationMs = 90))
        }
    }

    fun triggerKeyboardNote(noteName: String) {
        val freq = Synthesizer.NOTE_FREQUENCIES[noteName] ?: 440.0
        val tone = Synthesizer.generateTone(
            frequency = freq,
            durationMs = 380,
            waveform = com.example.data.audio.WaveformType.SAWTOOTH,
            volume = 0.75f
        )
        audioEngine.playSoundOneShot(tone)
    }

    // Multitrack Controls
    fun selectTrack(trackId: String) {
        _selectedTrackId.value = trackId
    }

    fun toggleMuteTrack(trackId: String) {
        _tracks.value = _tracks.value.map {
            if (it.id == trackId) it.copy(isMuted = !it.isMuted) else it
        }
    }

    fun toggleSoloTrack(trackId: String) {
        _tracks.value = _tracks.value.map {
            if (it.id == trackId) it.copy(isSolo = !it.isSolo) else it
        }
    }

    fun updateTrackVolume(trackId: String, newVolume: Float) {
        _tracks.value = _tracks.value.map {
            if (it.id == trackId) it.copy(volume = newVolume.coerceIn(0f, 1.5f)) else it
        }
    }

    fun updateTrackPan(trackId: String, newPan: Float) {
        _tracks.value = _tracks.value.map {
            if (it.id == trackId) it.copy(pan = newPan.coerceIn(-1f, 1f)) else it
        }
    }

    fun updateTrackEffects(trackId: String, newEffects: TrackEffects) {
        _tracks.value = _tracks.value.map {
            if (it.id == trackId) it.copy(effects = newEffects) else it
        }
    }

    fun playStudioLoop() {
        audioEngine.playLoop(_tracks.value, totalBeats = 16f)
    }

    fun stopStudioLoop() {
        audioEngine.stop()
    }

    // Suno AI Song Generator
    fun generateSunoSong(
        title: String,
        lyrics: String,
        genre: String,
        mood: String,
        vocalStyle: String
    ) {
        viewModelScope.launch {
            _uiState.value = StudioUiState.GeneratingSong("Synthesizing $genre stems with $vocalStyle...")
            try {
                val (newSong, newTracks) = SunoMusicGenerator.generateSunoSong(
                    context = context,
                    promptTitle = title,
                    promptLyrics = lyrics,
                    genre = genre,
                    mood = mood,
                    vocalStyle = vocalStyle
                )
                _sunoSongs.value = listOf(newSong) + _sunoSongs.value
                _tracks.value = newTracks
                _projectLyrics.value = newSong.lyrics
                _activePlayingSong.value = newSong
                _currentProject.value = _currentProject.value.copy(
                    title = newSong.title,
                    bpm = newSong.bpm
                )
                audioEngine.setBpm(newSong.bpm)
                _uiState.value = StudioUiState.Success("Song '${newSong.title}' generated successfully with full stems!")
            } catch (e: Exception) {
                _uiState.value = StudioUiState.Error("Generation failed: ${e.message}")
            }
        }
    }

    // Update Lyrics
    fun updateLyrics(newLyricsText: String) {
        val updatedLines = newLyricsText.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .mapIndexed { idx, line ->
                LyricLine(
                    timestampSec = (idx * 3.5f) + 1.0f,
                    text = line
                )
            }
        _projectLyrics.value = updatedLines
        _activePlayingSong.value?.let { active ->
            val updatedSong = active.copy(lyrics = updatedLines)
            _activePlayingSong.value = updatedSong
            _sunoSongs.value = _sunoSongs.value.map { if (it.id == active.id) updatedSong else it }
        }
    }

    // Save Recorded Vocal to Studio Track
    fun saveVocalRecordingToTrack(samples: ShortArray) {
        if (samples.isEmpty()) return
        val recordingClip = AudioClip(
            id = "clip_vocal_${System.currentTimeMillis()}",
            name = "Recorded Vocal Take",
            startBeat = 0f,
            durationBeats = 16f,
            waveformAmplitudes = WavExporter.generateWaveformPreview(samples),
            audioSamples = samples
        )

        val vocalTrack = _tracks.value.find { it.type == AudioTrackType.VOCAL }
        if (vocalTrack != null) {
            _tracks.value = _tracks.value.map {
                if (it.id == vocalTrack.id) {
                    it.copy(clips = listOf(recordingClip))
                } else it
            }
        } else {
            val newTrack = StudioTrack(
                id = "trk_vocal_custom",
                name = "Vocal Lead (Mic)",
                type = AudioTrackType.VOCAL,
                volume = 0.9f,
                colorHex = 0xFFFF4081,
                clips = listOf(recordingClip)
            )
            _tracks.value = listOf(newTrack) + _tracks.value
        }
        _uiState.value = StudioUiState.Success("Vocal take added to multitrack studio!")
    }

    // Export Master Track / Share via Android Intent
    fun exportAndShareMasterTrack(onComplete: (File?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
                val exportFile = File(exportDir, "CLR_Music_${System.currentTimeMillis()}.wav")

                // Combine clips from current tracks
                val totalSamples = (Synthesizer.SAMPLE_RATE * 8.0).toInt() // 8-second master loop
                val mixedBuffer = FloatArray(totalSamples)

                for (track in _tracks.value.filter { !it.isMuted }) {
                    for (clip in track.clips) {
                        val samples = clip.audioSamples ?: continue
                        val processed = audioEngine.applyDspEffects(samples, track.effects)
                        for (i in 0 until minOf(processed.size, totalSamples)) {
                            mixedBuffer[i] += (processed[i].toFloat() / Short.MAX_VALUE) * track.volume
                        }
                    }
                }

                val finalSamples = ShortArray(totalSamples)
                for (i in 0 until totalSamples) {
                    finalSamples[i] = (mixedBuffer[i].coerceIn(-1f, 1f) * Short.MAX_VALUE).toInt().toShort()
                }

                val success = WavExporter.writeWavFile(exportFile, finalSamples, Synthesizer.SAMPLE_RATE, 1)
                withContext(Dispatchers.Main) {
                    if (success) {
                        shareAudioFile(exportFile)
                        _uiState.value = StudioUiState.Success("High-quality WAV exported & ready to share!")
                        onComplete(exportFile)
                    } else {
                        _uiState.value = StudioUiState.Error("Failed to write WAV file.")
                        onComplete(null)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.value = StudioUiState.Error("Export failed: ${e.message}")
                    onComplete(null)
                }
            }
        }
    }

    private fun shareAudioFile(file: File) {
        try {
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "audio/wav"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "Check out my track created on CLR Music!")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Track").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Collab sync simulation
    fun syncCollabSession() {
        viewModelScope.launch {
            _collabSession.value = _collabSession.value.copy(
                isSyncing = true,
                lastStemUpdate = "Uploading multitrack stems..."
            )
            delay(1200)
            _collabSession.value = _collabSession.value.copy(
                isSyncing = false,
                lastStemUpdate = "Synced with DJ Alex & Producer Priya"
            )
            _uiState.value = StudioUiState.Success("Collab room stems synchronized!")
        }
    }

    fun dismissUiState() {
        _uiState.value = StudioUiState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.release()
        audioRecorder.release()
        stopSequencer()
    }
}
