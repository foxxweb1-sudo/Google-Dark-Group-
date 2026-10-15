package com.example.audio

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.abs
import kotlin.math.sqrt

class VoiceEngine(private val context: Context, private val scope: CoroutineScope) {
    companion object {
        private const val TAG = "VoiceEngine"
        const val SAMPLE_RATE = 8000 // 8kHz 16-bit mono
        const val FRAME_SIZE = 320 // 320 samples = 40ms frame
    }

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    val udpRelay = UdpAudioRelay(scope)
    val agora = AgoraRtcService

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _isDeafened = MutableStateFlow(false)
    val isDeafened: StateFlow<Boolean> = _isDeafened.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    // 3 animated sound wave amplitudes (normalized 0.15f to 1.0f)
    private val _soundWaveAmplitudes = MutableStateFlow(listOf(0.2f, 0.4f, 0.25f))
    val soundWaveAmplitudes: StateFlow<List<Float>> = _soundWaveAmplitudes.asStateFlow()

    var onAudioChunkCaptured: ((ShortArray, Float) -> Unit)? = null

    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null
    private var recordJob: Job? = null
    private var playbackJob: Job? = null
    private var simulationJob: Job? = null

    private val playbackQueue = ConcurrentLinkedQueue<ShortArray>()

    @SuppressLint("MissingPermission")
    fun connectVoice(hasAudioPermission: Boolean, channelName: String = "", userId: String = "") {
        _isConnected.value = true
        _isMuted.value = false
        _isDeafened.value = false

        try {
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
            audioManager.isSpeakerphoneOn = true
        } catch (_: Exception) {}

        // 1. Initialize & Join Agora RTC channel if provided
        if (channelName.isNotBlank()) {
            scope.launch {
                agora.joinChannel(context, channelName, userId.hashCode())
            }
            agora.onVolumeIndicationCallback = { totalVolume, isLocalSpeaking ->
                if (!_isMuted.value) {
                    _isSpeaking.value = isLocalSpeaking
                    if (isLocalSpeaking) {
                        val norm = (totalVolume / 255f).coerceIn(0.2f, 1f)
                        _soundWaveAmplitudes.value = listOf(
                            (0.3f + norm * 0.7f).coerceIn(0.2f, 1f),
                            (0.4f + norm * 0.8f).coerceIn(0.3f, 1f),
                            (0.25f + norm * 0.75f).coerceIn(0.2f, 1f)
                        )
                    } else {
                        _soundWaveAmplitudes.value = listOf(0.18f, 0.22f, 0.15f)
                    }
                }
            }
        } else {
            // Local test mode without Agora room
            startAudioPlayback()
            if (hasAudioPermission) {
                startRealAudioCapture()
            } else {
                startSimulatedAudioCapture()
            }
        }
    }

    fun disconnectVoice() {
        _isConnected.value = false
        _isMuted.value = true
        _isDeafened.value = false
        _isSpeaking.value = false
        _soundWaveAmplitudes.value = listOf(0.15f, 0.15f, 0.15f)

        onAudioChunkCaptured = null
        agora.leaveChannel()
        udpRelay.stop()
        stopAudioCapture()
        stopAudioPlayback()

        try {
            audioManager.mode = AudioManager.MODE_NORMAL
        } catch (_: Exception) {}
    }

    fun toggleMute(): Boolean {
        if (!_isConnected.value) return true
        val newMute = !_isMuted.value
        _isMuted.value = newMute
        agora.muteLocalAudio(newMute)
        if (newMute) {
            _isSpeaking.value = false
            _soundWaveAmplitudes.value = listOf(0.15f, 0.15f, 0.15f)
        }
        return newMute
    }

    fun toggleDeafen(): Boolean {
        val newDeafen = !_isDeafened.value
        _isDeafened.value = newDeafen
        agora.muteAllRemoteAudio(newDeafen)
        try {
            if (newDeafen) {
                audioTrack?.setVolume(0f)
            } else {
                audioTrack?.setVolume(1.0f)
            }
        } catch (_: Exception) {}
        return newDeafen
    }

    fun playIncomingAudio(data: ShortArray) {
        if (_isDeafened.value || !_isConnected.value || data.isEmpty()) return
        playbackQueue.offer(data)
    }

    private fun startAudioPlayback() {
        stopAudioPlayback()
        playbackJob = scope.launch(Dispatchers.IO) {
            try {
                val minBufSize = AudioTrack.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                val bufferSize = maxOf(minBufSize, 4096)

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                audioTrack = track
                track.play()

                while (isActive && _isConnected.value) {
                    val chunk = playbackQueue.poll()
                    if (chunk != null && chunk.isNotEmpty() && !_isDeafened.value) {
                        track.write(chunk, 0, chunk.size)
                    } else {
                        delay(12)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "AudioTrack error: ${e.message}")
            }
        }
    }

    private fun stopAudioPlayback() {
        playbackJob?.cancel()
        playbackJob = null
        playbackQueue.clear()
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
    }

    @SuppressLint("MissingPermission")
    private fun startRealAudioCapture() {
        stopAudioCapture()
        recordJob = scope.launch(Dispatchers.IO) {
            try {
                val minBufSize = AudioRecord.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                val bufferSize = maxOf(minBufSize, FRAME_SIZE * 2)

                // Try Audio Sources in order: MIC -> DEFAULT -> VOICE_COMMUNICATION
                val sources = listOf(
                    MediaRecorder.AudioSource.MIC,
                    MediaRecorder.AudioSource.DEFAULT,
                    MediaRecorder.AudioSource.VOICE_COMMUNICATION
                )

                var initializedRecord: AudioRecord? = null
                for (source in sources) {
                    try {
                        val record = AudioRecord(
                            source,
                            SAMPLE_RATE,
                            AudioFormat.CHANNEL_IN_MONO,
                            AudioFormat.ENCODING_PCM_16BIT,
                            bufferSize
                        )
                        if (record.state == AudioRecord.STATE_INITIALIZED) {
                            initializedRecord = record
                            break
                        } else {
                            record.release()
                        }
                    } catch (_: Exception) {}
                }

                if (initializedRecord == null) {
                    Log.w(TAG, "All AudioRecord sources failed, fallback to simulated capture")
                    startSimulatedAudioCapture()
                    return@launch
                }

                audioRecord = initializedRecord
                initializedRecord.startRecording()

                val audioBuffer = ShortArray(FRAME_SIZE)
                while (isActive && _isConnected.value) {
                    if (_isMuted.value) {
                        _isSpeaking.value = false
                        _soundWaveAmplitudes.value = listOf(0.15f, 0.15f, 0.15f)
                        delay(60)
                        continue
                    }

                    val read = initializedRecord.read(audioBuffer, 0, audioBuffer.size)
                    if (read > 0) {
                        var sum = 0.0
                        for (i in 0 until read) {
                            val boosted = (audioBuffer[i] * 1.5f).toInt().coerceIn(-32768, 32767).toShort()
                            audioBuffer[i] = boosted
                            sum += (boosted * boosted).toDouble()
                        }
                        val rms = sqrt(sum / read)
                        val normalized = (rms / 32767.0).toFloat().coerceIn(0f, 1f)
                        val speakingThreshold = 0.015f
                        val speaking = normalized > speakingThreshold
                        _isSpeaking.value = speaking

                        if (speaking) {
                            val w1 = (0.3f + normalized * 2.5f).coerceIn(0.2f, 1f)
                            val w2 = (0.4f + normalized * 3.0f).coerceIn(0.3f, 1f)
                            val w3 = (0.25f + normalized * 2.2f).coerceIn(0.2f, 1f)
                            _soundWaveAmplitudes.value = listOf(w1, w2, w3)

                            val chunk = audioBuffer.copyOf(read)
                            onAudioChunkCaptured?.invoke(chunk, normalized)
                        } else {
                            _soundWaveAmplitudes.value = listOf(0.18f, 0.22f, 0.15f)
                        }
                    }
                    delay(30)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Audio capture loop interrupted: ${e.message}")
                if (isActive && _isConnected.value) {
                    startSimulatedAudioCapture()
                }
            }
        }
    }

    private fun startSimulatedAudioCapture() {
        stopAudioCapture()
        simulationJob = scope.launch(Dispatchers.Default) {
            var tick = 0
            try {
                while (isActive && _isConnected.value) {
                    if (_isMuted.value) {
                        _isSpeaking.value = false
                        _soundWaveAmplitudes.value = listOf(0.15f, 0.15f, 0.15f)
                        delay(100)
                        continue
                    }

                    tick++
                    val isSpeakingSimulation = (tick % 8) in 2..6
                    _isSpeaking.value = isSpeakingSimulation

                    if (isSpeakingSimulation) {
                        val a1 = 0.3f + (0.5f * kotlin.math.sin(tick * 0.4).toFloat().let { abs(it) })
                        val a2 = 0.4f + (0.6f * kotlin.math.sin(tick * 0.6 + 0.3).toFloat().let { abs(it) })
                        val a3 = 0.3f + (0.45f * kotlin.math.sin(tick * 0.3 + 0.7).toFloat().let { abs(it) })
                        _soundWaveAmplitudes.value = listOf(a1.coerceIn(0.2f, 1f), a2.coerceIn(0.3f, 1f), a3.coerceIn(0.2f, 1f))
                    } else {
                        _soundWaveAmplitudes.value = listOf(0.18f, 0.22f, 0.15f)
                    }
                    delay(70)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {}
        }
    }

    private fun stopAudioCapture() {
        recordJob?.cancel()
        recordJob = null
        simulationJob?.cancel()
        simulationJob = null
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (_: Exception) {}
        audioRecord = null
    }

    fun release() {
        disconnectVoice()
        agora.destroy()
    }

    // PCM BASE64 UTILITIES
    fun encodePcmToBase64(shorts: ShortArray, length: Int = shorts.size): String {
        val bytes = ByteArray(length * 2)
        for (i in 0 until length) {
            val s = shorts[i]
            bytes[i * 2] = (s.toInt() and 0xFF).toByte()
            bytes[i * 2 + 1] = ((s.toInt() shr 8) and 0xFF).toByte()
        }
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    fun decodeBase64ToPcm(base64Str: String): ShortArray {
        return try {
            val bytes = Base64.decode(base64Str, Base64.NO_WRAP)
            val shorts = ShortArray(bytes.size / 2)
            for (i in shorts.indices) {
                val b1 = bytes[i * 2].toInt() and 0xFF
                val b2 = bytes[i * 2 + 1].toInt()
                shorts[i] = ((b2 shl 8) or b1).toShort()
            }
            shorts
        } catch (_: Exception) {
            ShortArray(0)
        }
    }
}
