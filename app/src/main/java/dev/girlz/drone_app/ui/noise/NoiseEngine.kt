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
    val noiseColor: NoiseColor,
    val fadeInMs: Long,
    val fadeOutMs: Long,
)

enum class NoiseColor {
    WHITE,
    PINK,
    BROWN,
}

class NoiseEngine {
    private var generator: AudioGenerator? = null
    private var track: AudioTrack? = null
    private var worker: Thread? = null
    private var noiseProcessor: NoiseSourceProcessor? = null

    fun start(settings: NoiseSettings) {
        stop()
        val audioGenerator = AudioGenerator(settings.bufferSize, 0, settings.sampleRate)
        val audioTrack = buildAudioTrack(settings, audioGenerator)
        val noiseSource = NoiseSourceProcessor(settings)
        val audioWriter = AudioTrackProcessor(audioTrack)

        audioGenerator.addAudioProcessor(noiseSource)
        audioGenerator.addAudioProcessor(audioWriter)

        audioTrack.play()
        val thread = Thread(audioGenerator, "NoiseGenerator")
        thread.start()

        generator = audioGenerator
        track = audioTrack
        worker = thread
        noiseProcessor = noiseSource
    }

    fun stop() {
        generator?.stop()
        generator = null
        worker = null
        noiseProcessor = null
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

    fun stopWithFade() {
        val processor = noiseProcessor ?: return stop()
        val fadeOutMs = processor.fadeOutMs
        if (fadeOutMs <= 0) {
            stop()
            return
        }
        processor.requestFadeOut()
        val stopper = Thread({
            try {
                Thread.sleep(fadeOutMs)
            } catch (_: InterruptedException) {
                return@Thread
            }
            stop()
        }, "NoiseStopper")
        stopper.start()
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
    settings: NoiseSettings,
) : AudioProcessor {
    private val amplitude = settings.gain.toFloat()
    private val noiseColor = settings.noiseColor
    val fadeOutMs = settings.fadeOutMs
    private val fadeInNs = settings.fadeInMs * 1_000_000L
    private val fadeOutNs = settings.fadeOutMs * 1_000_000L
    private val noiseGenerator = NoiseGenerator(settings.gain * 2.0)
    private val pinkFilter = PinkNoiseFilter()
    private val brownFilter = BrownNoiseFilter()
    private var startTimeNs: Long? = null
    private var fadeOutStartNs: Long? = null

    override fun process(audioEvent: AudioEvent): Boolean {
        audioEvent.clearFloatBuffer()
        noiseGenerator.process(audioEvent)
        val nowNs = System.nanoTime()
        if (startTimeNs == null) {
            startTimeNs = nowNs
        }
        val fadeInGain = if (fadeInNs <= 0L) {
            1f
        } else {
            val elapsed = (nowNs - (startTimeNs ?: nowNs)).coerceAtLeast(0L)
            (elapsed.toFloat() / fadeInNs.toFloat()).coerceIn(0f, 1f)
        }
        val fadeOutGain = fadeOutStartNs?.let { startNs ->
            if (fadeOutNs <= 0L) {
                0f
            } else {
                val elapsed = (nowNs - startNs).coerceAtLeast(0L)
                (1f - (elapsed.toFloat() / fadeOutNs.toFloat())).coerceIn(0f, 1f)
            }
        } ?: 1f
        val envelope = fadeInGain.coerceAtMost(fadeOutGain)
        val buffer = audioEvent.floatBuffer
        for (i in buffer.indices) {
            val sample = buffer[i] - amplitude
            val colored = when (noiseColor) {
                NoiseColor.WHITE -> sample
                NoiseColor.PINK -> pinkFilter.process(sample)
                NoiseColor.BROWN -> brownFilter.process(sample)
            }
            buffer[i] = colored * envelope
        }
        return true
    }

    override fun processingFinished() = Unit

    fun requestFadeOut() {
        if (fadeOutStartNs == null) {
            fadeOutStartNs = System.nanoTime()
        }
    }
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

private class PinkNoiseFilter {
    private var b0 = 0f
    private var b1 = 0f
    private var b2 = 0f
    private var b3 = 0f
    private var b4 = 0f
    private var b5 = 0f
    private var b6 = 0f

    fun process(white: Float): Float {
        b0 = 0.99886f * b0 + white * 0.0555179f
        b1 = 0.99332f * b1 + white * 0.0750759f
        b2 = 0.96900f * b2 + white * 0.1538520f
        b3 = 0.86650f * b3 + white * 0.3104856f
        b4 = 0.55000f * b4 + white * 0.5329522f
        b5 = -0.7616f * b5 - white * 0.0168980f
        val pink = b0 + b1 + b2 + b3 + b4 + b5 + b6 + white * 0.5362f
        b6 = white * 0.115926f
        return pink * 0.11f
    }
}

private class BrownNoiseFilter {
    private var lastOut = 0f

    fun process(white: Float): Float {
        val brown = (lastOut + white * 0.02f) / 1.02f
        lastOut = brown
        return brown * 3.5f
    }
}
