package com.example.data.model

enum class AudioTrackType(val displayName: String) {
    VOCAL("Vocal / Mic"),
    BEAT("Beats & Drums"),
    BASS("Bassline 808"),
    SYNTH("Melody & Keys"),
    FX("FX & Atmosphere")
}

data class AudioClip(
    val id: String,
    val name: String,
    val startBeat: Float,
    val durationBeats: Float,
    val waveformAmplitudes: List<Float>,
    val audioSamples: ShortArray? = null,
    val sourcePath: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AudioClip) return false
        return id == other.id && name == other.name && startBeat == other.startBeat && durationBeats == other.durationBeats
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + startBeat.hashCode()
        result = 31 * result + durationBeats.hashCode()
        return result
    }
}

data class TrackEffects(
    val reverbLevel: Float = 0.2f,        // 0..1
    val delayLevel: Float = 0.15f,        // 0..1
    val distortionLevel: Float = 0f,      // 0..1
    val pitchShiftSemitones: Int = 0,    // -12..12
    val noiseGateThreshold: Float = 0.05f,// 0..1
    val eqBands: List<Float> = listOf(0f, 0f, 0f, 0f, 0f), // -12dB to +12dB
    val modulationPreset: VoiceModulationPreset = VoiceModulationPreset.NONE
)

data class StudioTrack(
    val id: String,
    val name: String,
    val type: AudioTrackType,
    val volume: Float = 0.8f,
    val pan: Float = 0.0f, // -1.0 to +1.0
    val isMuted: Boolean = false,
    val isSolo: Boolean = false,
    val isArmed: Boolean = false,
    val colorHex: Long = 0xFF00E5FF,
    val clips: List<AudioClip> = emptyList(),
    val effects: TrackEffects = TrackEffects()
)

enum class EqualizerPreset(val displayName: String, val bands: List<Float>) {
    FLAT("Flat", listOf(0f, 0f, 0f, 0f, 0f)),
    BASS_BOOST("Bass Boost", listOf(6f, 4.5f, 1f, 0f, -1f)),
    VOCAL_BOOST("Vocal Boost", listOf(-2f, 2f, 5f, 3.5f, 1f)),
    CLUB("Club 808", listOf(5f, 3f, -1f, 2f, 4f)),
    ELECTRONIC("Electronic", listOf(4.5f, 2f, 0f, 3f, 5f)),
    ROCK("Rock Arena", listOf(4f, -1f, 2f, 4f, 3f)),
    POP("Modern Pop", listOf(2f, 3f, 4f, 2f, 1f)),
    HIP_HOP("Hip Hop Heat", listOf(6f, 3.5f, 0f, 1.5f, 3f))
}

enum class VoiceModulationPreset(val displayName: String, val description: String) {
    NONE("Natural Vocal", "Clean studio acoustic transparency"),
    AUTOTUNE("Pitch Correct (C Major)", "Hard auto-tuned pop & trap lead"),
    ROBOT("Cyborg Vocoder", "Metallic robotic modulation"),
    DEEP_BASS("Sub Deep Voice", "Low-octave commanding rumble"),
    CHIPMUNK("Hyper Pitch", "High-octave upbeat pitch shift"),
    RADIO("Vintage AM Radio", "Bandpass lo-fi transmission"),
    TELEPHONE("Telephone Filter", "Narrow frequency speech punch"),
    SPACE_ECHO("Infinite Space", "Ethereal lush hall & stereo ping-pong delay")
}

data class LyricLine(
    val timestampSec: Float,
    val text: String,
    val section: String = ""
)

data class SunoGeneratedSong(
    val id: String,
    val title: String,
    val genre: String,
    val mood: String,
    val vocalStyle: String,
    val bpm: Int,
    val lyrics: List<LyricLine>,
    val promptPrompt: String,
    val audioWavPath: String? = null,
    val durationSec: Int = 90,
    val tags: List<String> = emptyList(),
    val albumCoverColor: Long = 0xFF8E24AA,
    val createdAt: Long = System.currentTimeMillis()
)

data class CollabUser(
    val id: String,
    val name: String,
    val avatarColor: Long,
    val role: String,
    val isLive: Boolean = true
)

data class CollabSession(
    val roomId: String,
    val roomName: String,
    val bpm: Int = 120,
    val collaborators: List<CollabUser> = emptyList(),
    val isSyncing: Boolean = false,
    val lastStemUpdate: String = "All stems synced"
)

data class StudioProject(
    val id: String,
    val title: String,
    val bpm: Int = 120,
    val timeSignature: String = "4/4",
    val tracks: List<StudioTrack> = emptyList(),
    val lyrics: String = "",
    val masterVolume: Float = 0.9f,
    val lastModified: Long = System.currentTimeMillis(),
    val isCloudSynced: Boolean = true
)
