package dev.girlz.drone_app.ui.noise

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.AudioGenerator
import be.tarsos.dsp.AudioProcessor
import be.tarsos.dsp.synthesis.NoiseGenerator
import kotlin.math.max

data class NoiseSettings(
    val gain: Double,
    val sampleRate: Int,
    val bufferSize: Int,
)

class NoiseEngine {
    private var generator: AudioGenerator? = null
    private var track: AudioTrack? = null
    private var worker: Thread? = null

    fun start(settings: NoiseSettings) {
        stop()
        val audioGenerator = AudioGenerator(settings.bufferSize, 0, settings.sampleRate)
        val audioTrack = buildAudioTrack(settings, audioGenerator)
        val noiseSource = NoiseSourceProcessor(settings.gain)
        val audioWriter = AudioTrackProcessor(audioTrack)

        audioGenerator.addAudioProcessor(noiseSource)
        audioGenerator.addAudioProcessor(audioWriter)

        audioTrack.play()
        val thread = Thread(audioGenerator, "NoiseGenerator")
        thread.start()

        generator = audioGenerator
        track = audioTrack
        worker = thread
    }

    fun stop() {
        generator?.stop()
        generator = null
        worker = null
        track?.let { audioTrack ->
            try {
                audioTrack.stop()
            } catch (_: IllegalStateException) {
                // Ignore when already stopped.
            }
            audioTrack.flush()
            audioTrack.release()
        }
        track = null
    }

    private fun buildAudioTrack(
        settings: NoiseSettings,
        audioGenerator: AudioGenerator,
    ): AudioTrack {
        val format = audioGenerator.format
        val channelConfig = AudioFormat.CHANNEL_OUT_MONO
        val encoding = AudioFormat.ENCODING_PCM_16BIT
        val minBufferSize = AudioTrack.getMinBufferSize(settings.sampleRate, channelConfig, encoding)
        val desiredBufferSize = settings.bufferSize * format.frameSize
        val bufferSize = max(minBufferSize, desiredBufferSize)

        return AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(settings.sampleRate)
                    .setEncoding(encoding)
                    .setChannelMask(channelConfig)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()
    }
}

private class NoiseSourceProcessor(
    gain: Double,
) : AudioProcessor {
    private val amplitude = gain.toFloat()
    private val noiseGenerator = NoiseGenerator(gain * 2.0)

    override fun process(audioEvent: AudioEvent): Boolean {
        audioEvent.clearFloatBuffer()
        noiseGenerator.process(audioEvent)
        val buffer = audioEvent.floatBuffer
        for (i in buffer.indices) {
            buffer[i] -= amplitude
        }
        return true
    }

    override fun processingFinished() = Unit
}

private class AudioTrackProcessor(
    private val audioTrack: AudioTrack,
) : AudioProcessor {
    override fun process(audioEvent: AudioEvent): Boolean {
        val byteBuffer = audioEvent.byteBuffer
        audioTrack.write(byteBuffer, 0, byteBuffer.size, AudioTrack.WRITE_BLOCKING)
        return true
    }

    override fun processingFinished() = Unit
}
