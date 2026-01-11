package dev.girlz.drone_app.ui.command

import android.content.Context
import android.speech.tts.TextToSpeech
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

data class TtsEngine(
    val name: String,
    val label: String,
)

data class TtsVoiceOption(
    val name: String,
    val label: String,
    val localeTag: String,
    val countryCode: String,
)

class CommandSpeaker(
    private val context: Context,
) {
    private var tts: TextToSpeech? = null
    private var currentEngine: String? = null
    private var isReady: Boolean = false
    private var pendingText: String? = null
    private var pendingEngine: String? = null
    private var pendingVoiceName: String = ""
    private var pendingPitch: Float = 1.0f
    private var pendingSpeechRate: Float = 1.0f
    private var pendingVolume: Float = 1.0f
    private var pendingPan: Float = 0.0f

    private val _engines = MutableStateFlow<List<TtsEngine>>(emptyList())
    val engines: StateFlow<List<TtsEngine>> = _engines.asStateFlow()
    private val _voices = MutableStateFlow<List<TtsVoiceOption>>(emptyList())
    val voices: StateFlow<List<TtsVoiceOption>> = _voices.asStateFlow()

    fun loadEngines() {
        ensureTts(engineName = null)
    }

    fun prepareEngine(engineName: String) {
        ensureTts(engineName.takeIf { it.isNotBlank() })
    }

    fun speak(
        text: String,
        engineName: String,
        voiceName: String,
        pitch: Float,
        speechRate: Float,
        volume: Float,
        pan: Float,
    ) {
        if (text.isBlank()) return
        val requestedEngine = engineName.takeIf { it.isNotBlank() }
        pendingText = text
        pendingEngine = requestedEngine
        pendingVoiceName = voiceName
        pendingPitch = pitch
        pendingSpeechRate = speechRate
        pendingVolume = volume
        pendingPan = pan
        ensureTts(requestedEngine)
        if (isReady) {
            speakNow(text, voiceName, pitch, speechRate, volume, pan)
            pendingText = null
            pendingEngine = null
        }
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.shutdown()
        tts = null
        isReady = false
    }

    private fun ensureTts(engineName: String?) {
        if (tts != null && currentEngine == engineName) return
        tts?.shutdown()
        tts = null
        isReady = false
        currentEngine = engineName
        tts = TextToSpeech(context, { status ->
            isReady = status == TextToSpeech.SUCCESS
            if (isReady) {
                tts?.language = Locale.getDefault()
            }
            updateEngines()
            updateVoices()
            if (isReady) {
                val text = pendingText
                val engine = pendingEngine
                if (!text.isNullOrBlank() && engine == currentEngine) {
                    speakNow(
                        text,
                        voiceName = pendingVoiceName,
                        pitch = pendingPitch,
                        speechRate = pendingSpeechRate,
                        volume = pendingVolume,
                        pan = pendingPan,
                    )
                    pendingText = null
                    pendingEngine = null
                }
            }
        }, engineName)
    }

    private fun updateEngines() {
        val options = tts?.engines?.map { engineInfo ->
            TtsEngine(name = engineInfo.name, label = engineInfo.label)
        } ?: emptyList()
        _engines.value = listOf(TtsEngine(name = "", label = "System default")) + options
    }

    private fun updateVoices() {
        val options = tts?.voices
            ?.filter { !it.isNetworkConnectionRequired }
            ?.filter { it.locale.language.equals("en", ignoreCase = true) }
            ?.sortedWith(compareBy({ it.locale.toLanguageTag() }, { it.name }))
            ?.map { voice ->
                TtsVoiceOption(
                    name = voice.name,
                    label = "${voice.locale.toLanguageTag()} - ${voice.name}",
                    localeTag = voice.locale.toLanguageTag(),
                    countryCode = voice.locale.country.uppercase(),
                )
            }
            ?: emptyList()
        _voices.value = listOf(
            TtsVoiceOption(
                name = "",
                label = "System default",
                localeTag = "default",
                countryCode = "DEFAULT",
            )
        ) + options
    }

    private fun speakNow(
        text: String,
        voiceName: String,
        pitch: Float,
        speechRate: Float,
        volume: Float,
        pan: Float,
    ) {
        val voice = tts?.voices?.firstOrNull { it.name == voiceName }
        if (voice != null) {
            tts?.voice = voice
        }
        tts?.setPitch(pitch)
        tts?.setSpeechRate(speechRate)
        val params = android.os.Bundle().apply {
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, volume)
            putFloat(TextToSpeech.Engine.KEY_PARAM_PAN, pan)
        }
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "command-${System.currentTimeMillis()}")
    }
}
