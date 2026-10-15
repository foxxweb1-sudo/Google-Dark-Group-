package com.example.data.model

data class User(
    val id: String = "",
    val username: String = "",
    val name: String = "",
    val avatarUrl: String? = null,
    val isAnon: Boolean = false,
    val password: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isOnline: Boolean = false,
    val lastActive: Long = 0L
)

data class RoomInfo(
    val id: String = "",
    val name: String = "",
    val host: String = "",
    val hostId: String = "",
    val voiceChannelId: String = "",
    val participantCount: Int = 0,
    val password: String? = null,
    val isLocked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

enum class RoomEventType { JOIN, LEAVE }

data class RoomEventBanner(
    val id: String = "",
    val type: RoomEventType = RoomEventType.JOIN,
    val userId: String = "",
    val userName: String = "",
    val avatarUrl: String? = null,
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class RoomUser(
    val id: String = "",
    val peerId: String = "",
    val name: String = "",
    val username: String = "",
    val avatarUrl: String? = null,
    val isVoice: Boolean = false,
    val isMuted: Boolean = true,
    val isSpeaking: Boolean = false
)

data class ChatMessage(
    val id: String = "",
    val roomCode: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val time: String = "",
    val avatarUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isMe: Boolean = false,
    val isSystem: Boolean = false,
    val eventType: RoomEventType? = null
)

data class FriendRequest(
    val id: String = "",
    val fromUserId: String = "",
    val fromUsername: String = "",
    val fromName: String = "",
    val fromAvatarUrl: String? = null,
    val toUsername: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class Friend(
    val userId: String = "",
    val username: String = "",
    val name: String = "",
    val avatarUrl: String? = null,
    val isBlocked: Boolean = false,
    val isOnline: Boolean = false,
    val lastActive: Long = 0L,
    val activeRoomCode: String? = null
)

data class UserPresence(
    val userId: String = "",
    val isOnline: Boolean = false,
    val lastActive: Long = 0L
)

data class PrivateConversation(
    val id: String = "",
    val otherUserId: String = "",
    val otherUsername: String = "",
    val otherName: String = "",
    val otherAvatarUrl: String? = null,
    val lastMessage: String = "",
    val lastMessageTime: String = "",
    val lastTimestamp: Long = 0L,
    val isBlocked: Boolean = false,
    val isOtherOnline: Boolean = false,
    val otherLastActive: Long = 0L,
    val unreadCount: Int = 0,
    val lastMessageIsMe: Boolean = false,
    val lastMessageIsDelivered: Boolean = false,
    val lastMessageIsRead: Boolean = false
)

data class PrivateChatMessage(
    val id: String = "",
    val conversationId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val isVoice: Boolean = false,
    val voiceDurationSec: Int = 0,
    val time: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isMe: Boolean = false,
    val isDelivered: Boolean = false,
    val isRead: Boolean = false,
    val readTimestamp: Long? = null,
    val deletedForEveryone: Boolean = false,
    val deletedForUserIds: List<String> = emptyList()
)

data class CallSession(
    val id: String = "",
    val callerId: String = "",
    val callerName: String = "",
    val callerUsername: String = "",
    val callerAvatar: String? = null,
    val receiverId: String = "",
    val receiverName: String = "",
    val receiverUsername: String = "",
    val receiverAvatar: String? = null,
    val channelId: String = "",
    val status: String = "ringing",
    val timestamp: Long = System.currentTimeMillis()
)

