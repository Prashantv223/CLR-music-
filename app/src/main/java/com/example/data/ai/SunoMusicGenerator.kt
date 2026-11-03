package com.example.data.ai

import android.content.Context
import com.example.data.audio.Synthesizer
import com.example.data.audio.WavExporter
import com.example.data.model.AudioClip
import com.example.data.model.AudioTrackType
import com.example.data.model.LyricLine
import com.example.data.model.StudioTrack
import com.example.data.model.SunoGeneratedSong
import com.example.data.model.TrackEffects
import com.example.data.model.VoiceModulationPreset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.random.Random

object SunoMusicGenerator {

    val PRESET_GENRES = listOf(
        "Synthwave & Cyberpunk",
        "Bollywood Melodic",
        "Lo-Fi Chillhop",
        "Trap & 808 Drill",
        "Modern Pop Anthem",
        "EDM Club Festival",
        "Acoustic Indie",
        "R&B Smooth Soul"
    )

    val PRESET_MOODS = listOf(
        "Energetic & Hype",
        "Dreamy & Chill",
        "Romantic & Emotional",
        "Dark & Moody",
        "Inspiring & Uplifting",
        "Nostalgic Retro"
    )

    val PRESET_VOCAL_STYLES = listOf(
        "Autotuned Melodic",
        "Male Smooth Tenor",
        "Female Airy Pop",
        "Cyber Robotic Vocoder",
        "Deep Baritone",
        "Acoustic Natural"
    )

    suspend fun generateSunoSong(
        context: Context,
        promptTitle: String,
        promptLyrics: String,
        genre: String,
        mood: String,
        vocalStyle: String
    ): Pair<SunoGeneratedSong, List<StudioTrack>> = withContext(Dispatchers.Default) {
        val songId = "suno_${System.currentTimeMillis()}"
        val bpm = when (genre) {
            "Synthwave & Cyberpunk" -> 118
            "Bollywood Melodic" -> 105
            "Lo-Fi Chillhop" -> 85
            "Trap & 808 Drill" -> 140
            "EDM Club Festival" -> 128
            "Acoustic Indie" -> 95
            else -> 120
        }

        // Parse or structure lyrics into sections
        val formattedLyrics = formatLyricsWithTimestamps(promptLyrics, bpm)

        // Musical scales according to mood & genre
        val scaleNotes = when {
            genre.contains("Synthwave") -> listOf("C3", "D#3", "F3", "G3", "A#3", "C4", "D#4")
            genre.contains("Bollywood") -> listOf("C3", "D3", "D#3", "F#3", "G3", "G#3", "B3", "C4")
            genre.contains("Lo-Fi") -> listOf("D3", "F3", "G3", "A3", "C4", "D4", "F4")
            genre.contains("Trap") -> listOf("C2", "D#2", "F2", "F#2", "G2", "A#2", "C3")
            else -> listOf("C3", "D3", "E3", "G3", "A3", "C4", "E4")
        }

        val beatDurationSec = 60.0 / bpm
        val loopBeats = 16
        val loopDurationSec = loopBeats * beatDurationSec
        val sampleRate = Synthesizer.SAMPLE_RATE
        val totalSamples = (sampleRate * loopDurationSec).toInt()

        // 1. Synthesize Beat Track
        val beatSamples = ShortArray(totalSamples)
        for (beat in 0 until loopBeats) {
            val beatSampleOffset = (beat * beatDurationSec * sampleRate).toInt()
            
            // Kick on beats 0, 4, 8, 12 (standard 4-on-the-floor) + syncopations
            if (beat % 4 == 0 || (genre.contains("Trap") && beat == 10)) {
                val kick = Synthesizer.generateKick()
                mixIn(beatSamples, kick, beatSampleOffset)
            }
            // Snare / Clap on beats 2, 6, 10, 14
            if (beat % 4 == 2) {
                val snare = if (genre.contains("Lo-Fi")) Synthesizer.generateClap() else Synthesizer.generateSnare()
                mixIn(beatSamples, snare, beatSampleOffset)
            }
            // Hi-Hats on every beat & half beat
            val hat = Synthesizer.generateHiHat(open = (beat % 4 == 3))
            mixIn(beatSamples, hat, beatSampleOffset)
            val halfBeatOffset = beatSampleOffset + (beatDurationSec * 0.5 * sampleRate).toInt()
            if (halfBeatOffset < totalSamples) {
                mixIn(beatSamples, Synthesizer.generateHiHat(open = false), halfBeatOffset)
            }
        }

        // 2. Synthesize Bassline Track
        val bassSamples = ShortArray(totalSamples)
        val bassRoot = scaleNotes[0]
        val bassFifth = scaleNotes.getOrElse(4) { scaleNotes[0] }
        val bassFreq1 = Synthesizer.NOTE_FREQUENCIES[bassRoot] ?: 65.41
        val bassFreq2 = Synthesizer.NOTE_FREQUENCIES[bassFifth] ?: 98.00

        for (bar in 0 until 4) {
            val barStartSample = (bar * 4 * beatDurationSec * sampleRate).toInt()
            val freq = if (bar % 2 == 0) bassFreq1 else bassFreq2
            val bassHit = Synthesizer.generate808Bass(freq, durationMs = (beatDurationSec * 2500).toInt())
            mixIn(bassSamples, bassHit, barStartSample)
        }

        // 3. Synthesize Melody & Chords Track
        val synthSamples = ShortArray(totalSamples)
        for (beat in 0 until loopBeats step 2) {
            val noteName = scaleNotes[Random.nextInt(scaleNotes.size)]
            val freq = Synthesizer.NOTE_FREQUENCIES[noteName] ?: 261.63
            val tone = Synthesizer.generateTone(
                freq,
                durationMs = (beatDurationSec * 1600).toInt(),
                waveform = if (genre.contains("Synthwave")) com.example.data.audio.WaveformType.SAWTOOTH else com.example.data.audio.WaveformType.SINE,
                volume = 0.55f
            )
            val noteOffset = (beat * beatDurationSec * sampleRate).toInt()
            mixIn(synthSamples, tone, noteOffset)
        }

        // 4. Synthesize Vocal Melodic Formant / Guide Track
        val vocalSamples = ShortArray(totalSamples)
        val vocalMod = when {
            vocalStyle.contains("Autotune") -> VoiceModulationPreset.AUTOTUNE
            vocalStyle.contains("Robotic") -> VoiceModulationPreset.ROBOT
            vocalStyle.contains("Deep") -> VoiceModulationPreset.DEEP_BASS
            else -> VoiceModulationPreset.AUTOTUNE
        }

        for (beat in 1 until loopBeats step 3) {
            val vocalNote = scaleNotes[Random.nextInt(scaleNotes.size)]
            val vFreq = (Synthesizer.NOTE_FREQUENCIES[vocalNote] ?: 329.63) * 1.5
            val vTone = Synthesizer.generateTone(
                vFreq,
                durationMs = (beatDurationSec * 1200).toInt(),
                waveform = com.example.data.audio.WaveformType.SINE,
                volume = 0.6f
            )
            val vOffset = (beat * beatDurationSec * sampleRate).toInt()
            mixIn(vocalSamples, vTone, vOffset)
        }

        // Master Mixdown for the song audio preview
        val masterMixed = ShortArray(totalSamples)
        for (i in 0 until totalSamples) {
            val sum = (beatSamples[i] * 0.75f + bassSamples[i] * 0.85f + synthSamples[i] * 0.65f + vocalSamples[i] * 0.7f).toInt()
            masterMixed[i] = sum.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }

        val outputDir = File(context.filesDir, "suno_exports").apply { mkdirs() }
        val songWavFile = File(outputDir, "${songId}_master.wav")
        WavExporter.writeWavFile(songWavFile, masterMixed, sampleRate, 1)

        val beatClip = AudioClip(
            id = "c_beat_${songId}",
            name = "$genre Beat",
            startBeat = 0f,
            durationBeats = loopBeats.toFloat(),
            waveformAmplitudes = WavExporter.generateWaveformPreview(beatSamples),
            audioSamples = beatSamples
        )
        val bassClip = AudioClip(
            id = "c_bass_${songId}",
            name = "808 Subline",
            startBeat = 0f,
            durationBeats = loopBeats.toFloat(),
            waveformAmplitudes = WavExporter.generateWaveformPreview(bassSamples),
            audioSamples = bassSamples
        )
        val synthClip = AudioClip(
            id = "c_synth_${songId}",
            name = "Chords & Arp",
            startBeat = 0f,
            durationBeats = loopBeats.toFloat(),
            waveformAmplitudes = WavExporter.generateWaveformPreview(synthSamples),
            audioSamples = synthSamples
        )
        val vocalClip = AudioClip(
            id = "c_vocal_${songId}",
            name = "$vocalStyle Guide",
            startBeat = 0f,
            durationBeats = loopBeats.toFloat(),
            waveformAmplitudes = WavExporter.generateWaveformPreview(vocalSamples),
            audioSamples = vocalSamples
        )

        val studioTracks = listOf(
            StudioTrack(
                id = "trk_vocal_$songId",
                name = "Vocal Lead",
                type = AudioTrackType.VOCAL,
                volume = 0.85f,
                colorHex = 0xFFFF4081,
                clips = listOf(vocalClip),
                effects = TrackEffects(modulationPreset = vocalMod, reverbLevel = 0.35f, delayLevel = 0.2f)
            ),
            StudioTrack(
                id = "trk_beat_$songId",
                name = "Drum Rhythm",
                type = AudioTrackType.BEAT,
                volume = 0.9f,
                colorHex = 0xFF00E5FF,
                clips = listOf(beatClip),
                effects = TrackEffects(distortionLevel = 0.05f)
            ),
            StudioTrack(
                id = "trk_bass_$songId",
                name = "808 Bass",
                type = AudioTrackType.BASS,
                volume = 0.85f,
                colorHex = 0xFF7C4DFF,
                clips = listOf(bassClip)
            ),
            StudioTrack(
                id = "trk_synth_$songId",
                name = "Melodic Synth",
                type = AudioTrackType.SYNTH,
                volume = 0.75f,
                colorHex = 0xFFFFB300,
                clips = listOf(synthClip),
                effects = TrackEffects(reverbLevel = 0.4f, delayLevel = 0.3f)
            )
        )

        val title = if (promptTitle.isNotBlank()) promptTitle else "$genre Vibe #1"
        val generatedSong = SunoGeneratedSong(
            id = songId,
            title = title,
            genre = genre,
            mood = mood,
            vocalStyle = vocalStyle,
            bpm = bpm,
            lyrics = formattedLyrics,
            promptPrompt = promptLyrics,
            audioWavPath = songWavFile.absolutePath,
            durationSec = 90,
            tags = listOf(genre, mood, "$bpm BPM", vocalStyle),
            albumCoverColor = when (genre) {
                "Synthwave & Cyberpunk" -> 0xFF00E5FF
                "Bollywood Melodic" -> 0xFFFF2A85
                "Lo-Fi Chillhop" -> 0xFF8E24AA
                "Trap & 808 Drill" -> 0xFFFF1744
                else -> 0xFFFFB300
            }
        )

        Pair(generatedSong, studioTracks)
    }

    private fun mixIn(target: ShortArray, source: ShortArray, offset: Int) {
        val count = minOf(source.size, target.size - offset)
        for (i in 0 until count) {
            val sum = target[offset + i] + source[i]
            target[offset + i] = sum.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
    }

    private fun formatLyricsWithTimestamps(rawLyrics: String, bpm: Int): List<LyricLine> {
        val lines = if (rawLyrics.isNotBlank()) {
            rawLyrics.lines().map { it.trim() }.filter { it.isNotEmpty() }
        } else {
            listOf(
                "[Intro] Neon pulse starts in the midnight glow",
                "[Verse 1] Walking through the electric street tonight",
                "Bass reverberates, the city shines so bright",
                "[Chorus] Oh feel the soundwave take us away",
                "CLR Music making beats into the light of day",
                "[Bridge] Lost inside the rhythm, echo in the air",
                "[Outro] Pure frequency, nothing can compare"
            )
        }

        var currentTime = 1.0f
        val secondsPerBar = (4 * 60.0f) / bpm
        val result = mutableListOf<LyricLine>()

        for (line in lines) {
            var section = ""
            var cleanText = line
            if (line.startsWith("[")) {
                val endBracket = line.indexOf(']')
                if (endBracket != -1) {
                    section = line.substring(0, endBracket + 1)
                    cleanText = line.substring(endBracket + 1).trim()
                }
            }
            result.add(
                LyricLine(
                    timestampSec = currentTime,
                    text = if (cleanText.isNotBlank()) cleanText else section,
                    section = section
                )
            )
            currentTime += secondsPerBar * 0.75f
        }
        return result
    }
}
