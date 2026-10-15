package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class FUser(
    @DocumentId val userId: String = "",
    val username: String = "",
    val name: String = "",
    val email: String? = null,
    val avatarUrl: String? = null,
    val isOnline: Boolean = false,
    val lastActive: Timestamp? = null,
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    fun toUser(): User = User(
        id = userId,
        username = username,
        name = name,
        avatarUrl = avatarUrl,
        isOnline = isOnline,
        lastActive = lastActive?.toDate()?.time ?: 0L
    )
}

data class FRoom(
    @DocumentId val id: String = "",
    val name: String = "",
    val hostId: String = "",
    val hostName: String = "",
    val voiceChannelId: String = "",
    val participantCount: Int = 0,
    val participants: List<String> = emptyList(),
    val isLocked: Boolean = false,
    val password: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null
)

data class FRoomMember(
    @DocumentId val userId: String = "",
    val username: String = "",
    val name: String = "",
    val avatarUrl: String? = null,
    val voiceChannelId: String = "",
    val isVoiceConnected: Boolean = true,
    val isMuted: Boolean = false,
    val isSpeaking: Boolean = false,
    val role: String = "member", // host / member
    @ServerTimestamp val joinedAt: Timestamp? = null
) {
    fun toRoomUser(): RoomUser = RoomUser(
        id = userId,
        peerId = "peer_$username",
        name = name,
        username = username,
        avatarUrl = avatarUrl,
        isVoice = isVoiceConnected,
        isMuted = isMuted,
        isSpeaking = isSpeaking
    )
}

data class FMessage(
    @DocumentId val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val avatarUrl: String? = null,
    val isSystem: Boolean = false,
    val eventType: String = "", // "join", "leave", "chat"
    @ServerTimestamp val timestamp: Timestamp? = null
)

data class FCall(
    @DocumentId val id: String = "",
    val callerId: String = "",
    val receiverId: String = "",
    val status: String = "ringing", // ringing, connected, ended, rejected
    val channelId: String = "",
    @ServerTimestamp val timestamp: Timestamp? = null
)
