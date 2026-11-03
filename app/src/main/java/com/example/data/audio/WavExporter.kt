package com.example.data.audio

import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

object WavExporter {

    fun writeWavFile(
        file: File,
        samples: ShortArray,
        sampleRate: Int = Synthesizer.SAMPLE_RATE,
        numChannels: Int = 1
    ): Boolean {
        return try {
            val byteRate = sampleRate * numChannels * 2
            val dataSize = samples.size * 2
            val totalSize = 36 + dataSize

            FileOutputStream(file).use { out ->
                val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)

                // RIFF chunk descriptor
                header.put("RIFF".toByteArray(Charsets.US_ASCII))
                header.putInt(totalSize)
                header.put("WAVE".toByteArray(Charsets.US_ASCII))

                // fmt sub-chunk
                header.put("fmt ".toByteArray(Charsets.US_ASCII))
                header.putInt(16) // Subchunk1Size for PCM
                header.putShort(1.toShort()) // AudioFormat 1 = PCM
                header.putShort(numChannels.toShort())
                header.putInt(sampleRate)
                header.putInt(byteRate)
                header.putShort((numChannels * 2).toShort()) // BlockAlign
                header.putShort(16.toShort()) // BitsPerSample

                // data sub-chunk
                header.put("data".toByteArray(Charsets.US_ASCII))
                header.putInt(dataSize)

                out.write(header.array())

                // Write PCM audio data
                val buffer = ByteBuffer.allocate(dataSize).order(ByteOrder.LITTLE_ENDIAN)
                for (sample in samples) {
                    buffer.putShort(sample)
                }
                out.write(buffer.array())
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun generateWaveformPreview(samples: ShortArray, targetPoints: Int = 120): List<Float> {
        if (samples.isEmpty()) return List(targetPoints) { 0.1f }
        val step = (samples.size / targetPoints).coerceAtLeast(1)
        val amplitudes = ArrayList<Float>(targetPoints)

        for (i in 0 until targetPoints) {
            val startIndex = i * step
            if (startIndex >= samples.size) {
                amplitudes.add(0.05f)
                continue
            }
            val endIndex = (startIndex + step).coerceAtMost(samples.size)
            var maxAmp = 0
            for (j in startIndex until endIndex) {
                val absVal = kotlin.math.abs(samples[j].toInt())
                if (absVal > maxAmp) maxAmp = absVal
            }
            val normalized = (maxAmp.toFloat() / Short.MAX_VALUE).coerceIn(0.05f, 1.0f)
            amplitudes.add(normalized)
        }
        return amplitudes
    }
}
