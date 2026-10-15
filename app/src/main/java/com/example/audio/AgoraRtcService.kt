package com.example.audio

import android.content.Context
import android.util.Log
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AgoraRtcService {
    private const val TAG = "AgoraRtcService"

    // Real Agora Project Credentials - STRICTLY IMMUTABLE
    private const val APP_ID = "38957a365fbe4faf80c1ebac552dd4b9"
    private const val APP_CERTIFICATE = "3b23110d30db45c9910b3c86928428fe"
    private const val APP_CERTIFICATE_SECONDARY = "6beaa95ad33d42afb812338b9b128a31"

    val appId: String get() = APP_ID
    val appCertificate: String get() = APP_CERTIFICATE
    val secondaryCertificate: String get() = APP_CERTIFICATE_SECONDARY

    private var rtcEngine: RtcEngine? = null
    private var isInitialized = false

    private val _isChannelJoined = MutableStateFlow(false)
    val isChannelJoined: StateFlow<Boolean> = _isChannelJoined.asStateFlow()

    private val _isEngineReady = MutableStateFlow(true)
    val isEngineReady: StateFlow<Boolean> = _isEngineReady.asStateFlow()

    private val _currentChannel = MutableStateFlow<String?>(null)
    val currentChannel: StateFlow<String?> = _currentChannel.asStateFlow()

    private val _remoteSpeakingUids = MutableStateFlow<Set<Int>>(emptySet())
    val remoteSpeakingUids: StateFlow<Set<Int>> = _remoteSpeakingUids.asStateFlow()

    var onUserJoinedCallback: ((uid: Int) -> Unit)? = null
    var onUserOfflineCallback: ((uid: Int) -> Unit)? = null
    var onVolumeIndicationCallback: ((totalVolume: Int, isLocalSpeaking: Boolean) -> Unit)? = null

    @Synchronized
    fun initEngine(context: Context): Boolean {
        if (isInitialized && rtcEngine != null) return true

        try {
            val cleanAppId = APP_ID.trim().replace(" ", "")
            Log.d(TAG, "Initializing Agora Global Engine with App ID: [${cleanAppId.take(4)}...${cleanAppId.takeLast(4)}]")

            val handler = object : IRtcEngineEventHandler() {
                override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
                    Log.d(TAG, "Agora successfully joined channel: $channel with UID: $uid")
                    _isChannelJoined.value = true
                    _currentChannel.value = channel
                    // Force speakerphone on channel join
                    try {
                        rtcEngine?.setEnableSpeakerphone(true)
                    } catch (_: Exception) {}
                }

                override fun onUserJoined(uid: Int, elapsed: Int) {
                    Log.d(TAG, "Agora remote user joined: $uid")
                    onUserJoinedCallback?.invoke(uid)
                }

                override fun onUserOffline(uid: Int, reason: Int) {
                    Log.d(TAG, "Agora remote user offline: $uid, reason: $reason")
                    onUserOfflineCallback?.invoke(uid)
                    val set = _remoteSpeakingUids.value.toMutableSet()
                    set.remove(uid)
                    _remoteSpeakingUids.value = set
                }

                override fun onAudioVolumeIndication(
                    speakers: Array<out AudioVolumeInfo>?,
                    totalVolume: Int
                ) {
                    var isMeSpeaking = false
                    val speakingSet = mutableSetOf<Int>()

                    speakers?.forEach { speaker ->
                        if (speaker.volume > 4) {
                            if (speaker.uid == 0) {
                                isMeSpeaking = true
                            } else {
                                speakingSet.add(speaker.uid)
                            }
                        }
                    }

                    _remoteSpeakingUids.value = speakingSet
                    onVolumeIndicationCallback?.invoke(totalVolume, isMeSpeaking)
                }

                override fun onLeaveChannel(stats: RtcStats?) {
                    Log.d(TAG, "Agora left channel")
                    _isChannelJoined.value = false
                    _currentChannel.value = null
                    _remoteSpeakingUids.value = emptySet()
                }

                override fun onAudioRouteChanged(routing: Int) {
                    Log.d(TAG, "Agora audio route changed to: $routing")
                    // If route switched away from speakerphone, restore speakerphone for clear voice
                    try {
                        rtcEngine?.setEnableSpeakerphone(true)
                    } catch (_: Exception) {}
                }

                override fun onConnectionStateChanged(state: Int, reason: Int) {
                    Log.d(TAG, "Agora connection state: $state, reason: $reason")
                }

                override fun onRemoteAudioStateChanged(uid: Int, state: Int, reason: Int, elapsed: Int) {
                    Log.d(TAG, "Agora remote audio state for uid $uid: state=$state, reason=$reason")
                }

                override fun onRequestToken() {
                    Log.w(TAG, "Agora token expired, requesting renewal")
                }

                override fun onError(err: Int) {
                    Log.w(TAG, "Agora RTC Event code: $err")
                }
            }

            val config = RtcEngineConfig()
            config.mContext = context.applicationContext
            config.mAppId = cleanAppId
            config.mEventHandler = handler
            // Enable Global routing across all countries and continents (AREA_CODE_GLOB = -1)
            config.mAreaCode = -1
            
            rtcEngine = RtcEngine.create(config)
            
            rtcEngine?.apply {
                setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING)
                setClientRole(Constants.CLIENT_ROLE_BROADCASTER)
                setAudioProfile(Constants.AUDIO_PROFILE_DEFAULT, Constants.AUDIO_SCENARIO_GAME_STREAMING)
                enableAudio()
                enableLocalAudio(true)
                setDefaultAudioRoutetoSpeakerphone(true)
                setEnableSpeakerphone(true)
                enableAudioVolumeIndication(200, 3, true)
                adjustRecordingSignalVolume(130)
                adjustPlaybackSignalVolume(130)
            }

            isInitialized = true
            _isEngineReady.value = true
            return true
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to initialize Agora RTC Engine. Check if App ID is valid.", e)
            isInitialized = false
            _isEngineReady.value = false
            return false
        }
    }

    suspend fun joinChannel(
        context: Context,
        channelName: String,
        userIdHash: Int,
        customToken: String? = null
    ): Boolean {
        if (!initEngine(context)) return false

        try {
            val engine = rtcEngine ?: return false
            engine.setClientRole(Constants.CLIENT_ROLE_BROADCASTER)
            engine.enableAudio()
            engine.enableLocalAudio(true)
            engine.setDefaultAudioRoutetoSpeakerphone(true)
            engine.setEnableSpeakerphone(true)

            // Safe ASCII channel name for Agora cross-country synchronization
            val cleanChannel = if (channelName.matches(Regex("^[a-zA-Z0-9_]{1,64}$"))) {
                channelName
            } else {
                "room_" + (channelName.hashCode().toLong() and 0xFFFFFFFFL)
            }
            val uid = (userIdHash and 0x7FFFFFFF) // Positive 32-bit int

            // 1. Fetch token from Cloudflare Worker
            var tokenToUse = customToken ?: com.example.data.remote.CloudflareService.fetchAgoraToken(cleanChannel, uid)

            // 2. Fallback to local generation with Primary Cert
            if (tokenToUse.isNullOrBlank() && APP_CERTIFICATE.isNotBlank()) {
                Log.d(TAG, "Worker token empty, using local primary token builder for $cleanChannel")
                tokenToUse = AgoraTokenBuilder.buildTokenWithUid(
                    appId = APP_ID,
                    appCertificate = APP_CERTIFICATE,
                    channelName = cleanChannel,
                    uid = uid
                ).ifBlank { null }
            }

            // 3. Fallback to local generation with Secondary Cert
            if (tokenToUse.isNullOrBlank() && APP_CERTIFICATE_SECONDARY.isNotBlank()) {
                Log.d(TAG, "Primary token empty, using secondary token builder for $cleanChannel")
                tokenToUse = AgoraTokenBuilder.buildTokenWithUid(
                    appId = APP_ID,
                    appCertificate = APP_CERTIFICATE_SECONDARY,
                    channelName = cleanChannel,
                    uid = uid
                ).ifBlank { null }
            }

            Log.d(TAG, "Attempting to join channel '$cleanChannel' with UID $uid (Token length: ${tokenToUse?.length ?: 0})")
            val res = engine.joinChannel(tokenToUse, cleanChannel, "", uid)
            
            if (res == 0) {
                engine.setEnableSpeakerphone(true)
                engine.muteLocalAudioStream(false)
                engine.muteAllRemoteAudioStreams(false)
                Log.d(TAG, "Agora joinChannel initiated successfully for $cleanChannel")
                return true
            } else {
                Log.e(TAG, "Agora joinChannel failed with error code: $res")
                return false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error joining Agora channel", e)
            return false
        }
    }

    fun leaveChannel() {
        try {
            rtcEngine?.leaveChannel()
            _isChannelJoined.value = false
            _currentChannel.value = null
            _remoteSpeakingUids.value = emptySet()
        } catch (e: Exception) {
            Log.e(TAG, "Error leaving Agora channel", e)
        }
    }

    fun muteLocalAudio(mute: Boolean) {
        try {
            rtcEngine?.muteLocalAudioStream(mute)
        } catch (e: Exception) {
            Log.e(TAG, "Error muting local audio", e)
        }
    }

    fun muteAllRemoteAudio(mute: Boolean) {
        try {
            rtcEngine?.muteAllRemoteAudioStreams(mute)
        } catch (e: Exception) {
            Log.e(TAG, "Error muting remote audio", e)
        }
    }

    fun destroy() {
        try {
            rtcEngine?.leaveChannel()
            RtcEngine.destroy()
        } catch (e: Exception) {
            Log.e(TAG, "Error destroying Agora RTC Engine", e)
        }
        rtcEngine = null
        isInitialized = false
        _isChannelJoined.value = false
        _currentChannel.value = null
    }

    // These stubs are kept for compatibility with other files but do nothing now
    fun loadSavedCredentials(context: Context) {}
    fun saveCredentials(context: Context, a: String, b: String) {}
    fun resetToDefaults() {}
    fun setCredentials(a: String, b: String) {}
}
