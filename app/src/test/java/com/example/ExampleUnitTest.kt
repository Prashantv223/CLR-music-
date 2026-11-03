package com.example

import com.example.data.audio.Synthesizer
import com.example.data.audio.WavExporter
import com.example.data.model.EqualizerPreset
import com.example.data.model.VoiceModulationPreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun synthesizer_generatesValidKickSamples() {
        val kick = Synthesizer.generateKick()
        assertTrue("Kick samples should not be empty", kick.isNotEmpty())
        for (sample in kick) {
            assertTrue("Sample should be within 16-bit PCM bounds", sample in Short.MIN_VALUE..Short.MAX_VALUE)
        }
    }

    @Test
    fun synthesizer_containsStandardNoteFrequencies() {
        val c4Freq = Synthesizer.NOTE_FREQUENCIES["C4"]
        assertNotNull(c4Freq)
        assertEquals(261.63, c4Freq!!, 0.1)

        val a4Freq = Synthesizer.NOTE_FREQUENCIES["A4"]
        assertNotNull(a4Freq)
        assertEquals(440.0, a4Freq!!, 0.1)
    }

    @Test
    fun wavExporter_generatesNormalizedWaveformPreview() {
        val samples = ShortArray(1000) { ((it % 100) * 200).toShort() }
        val waveform = WavExporter.generateWaveformPreview(samples, targetPoints = 50)
        assertEquals(50, waveform.size)
        for (point in waveform) {
            assertTrue("Waveform amplitude should be normalized between 0 and 1", point in 0.0f..1.0f)
        }
    }

    @Test
    fun equalizerAndModulationPresets_areValid() {
        assertTrue(EqualizerPreset.entries.isNotEmpty())
        assertTrue(VoiceModulationPreset.entries.isNotEmpty())
        assertEquals(5, EqualizerPreset.BASS_BOOST.bands.size)
    }
}
