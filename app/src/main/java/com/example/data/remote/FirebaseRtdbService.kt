package com.example.data.remote

import android.util.Log
import com.example.data.model.CallSession
import com.example.data.model.ChatMessage
import com.example.data.model.Friend
import com.example.data.model.FriendRequest
import com.example.data.model.PrivateChatMessage
import com.example.data.model.PrivateConversation
import com.example.data.model.RoomInfo
import com.example.data.model.RoomUser
import com.example.data.model.User
import com.example.data.model.UserPresence
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object FirebaseRtdbService {
    private const val TAG = "FirebaseRtdbService"
    private const val BASE_URL = "https://group-afc70-default-rtdb.firebaseio.com"

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val sseClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    suspend fun getUser(username: String): User? = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/users/$username.json"
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()
            val body = response.body?.string()
            if (response.isSuccessful && !body.isNullOrEmpty() && body != "null") {
                val json = JSONObject(body)
                val uId = json.optString("id", username)
                val isOnlineVal = json.optBoolean("isOnline", false)
                val lastAct = json.optLong("lastActive", 0L)
                val isActuallyOnline = isOnlineVal && (System.currentTimeMillis() - lastAct < 65_000)
                return@withContext User(
                    id = uId,
                    username = json.optString("username", username),
                    name = json.optString("name", username),
                    avatarUrl = if (json.isNull("avatarUrl")) null else json.optString("avatarUrl"),
                    isAnon = json.optBoolean("isAnon", false),
                    password = json.optString("password", ""),
                    createdAt = json.optLong("createdAt", System.currentTimeMillis()),
                    isOnline = isActuallyOnline,
                    lastActive = lastAct
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user $username", e)
        }
        null
    }

    suspend fun getUserById(userId: String): User? = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/users.json"
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()
            val body = response.body?.string()
            if (response.isSuccessful && !body.isNullOrEmpty() && body != "null") {
                val json = JSONObject(body)
                val keys = json.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    val u = json.optJSONObject(k) ?: continue
                    if (u.optString("id") == userId) {
                        val isOnlineVal = u.optBoolean("isOnline", false)
                        val lastAct = u.optLong("lastActive", 0L)
                        val isActuallyOnline = isOnlineVal && (System.currentTimeMillis() - lastAct < 65_000)
                        return@withContext User(
                            id = u.optString("id"),
                            username = u.optString("username"),
                            name = u.optString("name"),
                            avatarUrl = if (u.isNull("avatarUrl")) null else u.optString("avatarUrl"),
                            isAnon = u.optBoolean("isAnon", false),
                            password = u.optString("password"),
                            isOnline = isActuallyOnline,
                            lastActive = lastAct
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error finding user by id", e)
        }
        null
    }

    suspend fun saveUser(user: User): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/users/${user.username}.json"
            val json = JSONObject().apply {
                put("id", user.id)
                put("username", user.username)
                put("name", user.name)
                put("avatarUrl", user.avatarUrl ?: JSONObject.NULL)
                put("isAnon", user.isAnon)
                put("password", user.password)
                put("createdAt", user.createdAt)
            }
            val request = Request.Builder()
                .url(url)
                .put(json.toString().toRequestBody(jsonMediaType))
                .build()
            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user", e)
            false
        }
    }

    suspend fun updateUser(username: String, name: String, password: String?, avatarUrl: String?): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/users/$username.json"
            val json = JSONObject().apply {
                put("name", name)
                if (!password.isNullOrEmpty()) put("password", password)
                if (avatarUrl != null) put("avatarUrl", avatarUrl)
            }
            val request = Request.Builder()
                .url(url)
                .patch(json.toString().toRequestBody(jsonMediaType))
                .build()
            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user", e)
            false
        }
    }

    suspend fun createRoom(room: RoomInfo): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/rooms/${room.id}.json"
            val json = JSONObject().apply {
                put("id", room.id)
                put("name", room.name)
                put("host", room.host)
                put("hostId", room.hostId)
                put("password", room.password ?: JSONObject.NULL)
                put("isLocked", room.isLocked)
                put("createdAt", room.createdAt)
            }
            val request = Request.Builder()
                .url(url)
                .put(json.toString().toRequestBody(jsonMediaType))
                .build()
            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            Log.e(TAG, "Error creating room", e)
            false
        }
    }

    suspend fun deleteRoom(roomCode: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/rooms/$roomCode.json"
            val request = Request.Builder().url(url).delete().build()
            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting room", e)
            false
        }
    }

    suspend fun getRoom(roomCode: String): RoomInfo? = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/rooms/$roomCode.json"
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()
            val body = response.body?.string()
            if (response.isSuccessful && !body.isNullOrEmpty() && body != "null") {
                val json = JSONObject(body)
                val pass = if (json.isNull("password")) null else json.optString("password")
                return@withContext RoomInfo(
                    id = json.optString("id", roomCode),
                    name = json.optString("name", "غرفة $roomCode"),
                    host = json.optString("host", "مشترك"),
                    hostId = json.optString("hostId", ""),
                    password = pass,
                    isLocked = !pass.isNullOrBlank() || json.optBoolean("isLocked", false),
                    createdAt = json.optLong("createdAt", System.currentTimeMillis())
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching room $roomCode", e)
        }
        null
    }

    suspend fun joinRoomUser(roomCode: String, user: RoomUser): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/rooms/$roomCode/users/${user.id}.json"
            val json = JSONObject().apply {
                put("id", user.id)
                put("peerId", user.peerId)
                put("name", user.name)
                put("username", user.username)
                put("avatarUrl", user.avatarUrl ?: JSONObject.NULL)
                put("isVoice", user.isVoice)
                put("isMuted", user.isMuted)
                put("joinedAt", System.currentTimeMillis())
            }
            val request = Request.Builder()
                .url(url)
                .put(json.toString().toRequestBody(jsonMediaType))
                .build()
            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            Log.e(TAG, "Error joining room user", e)
            false
        }
    }

    suspend fun leaveRoomUser(roomCode: String, userId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/rooms/$roomCode/users/$userId.json"
            val request = Request.Builder().url(url).delete().build()
            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            Log.e(TAG, "Error leaving room user", e)
            false
        }
    }

    suspend fun updateVoiceState(roomCode: String, userId: String, isVoice: Boolean, isMuted: Boolean): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/rooms/$roomCode/users/$userId.json"
            val json = JSONObject().apply {
                put("isVoice", isVoice)
                put("isMuted", isMuted)
            }
            val request = Request.Builder()
                .url(url)
                .patch(json.toString().toRequestBody(jsonMediaType))
                .build()
            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            Log.e(TAG, "Error updating voice state", e)
            false
        }
    }

    suspend fun fetchRoomUsers(roomCode: String): List<RoomUser> = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/rooms/$roomCode/users.json"
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()
            val body = response.body?.string()
            if (response.isSuccessful && !body.isNullOrEmpty() && body != "null") {
                val json = JSONObject(body)
                val list = mutableListOf<RoomUser>()
                val keys = json.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    val u = json.optJSONObject(k) ?: continue
                    list.add(
                        RoomUser(
                            id = u.optString("id", k),
                            peerId = u.optString("peerId", "peer_$k"),
                            name = u.optString("name", "مستخدم"),
                            username = u.optString("username", k),
                            avatarUrl = if (u.isNull("avatarUrl")) null else u.optString("avatarUrl"),
                            isVoice = u.optBoolean("isVoice", false),
                            isMuted = u.optBoolean("isMuted", true),
                            isSpeaking = false
                        )
                    )
                }
                return@withContext list
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching room users", e)
        }
        emptyList()
    }

    suspend fun sendMessage(roomCode: String, msg: ChatMessage): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/rooms/$roomCode/messages.json"
            val json = JSONObject().apply {
                put("senderId", msg.senderId)
                put("senderName", msg.senderName)
                put("text", msg.text)
                put("time", msg.time)
                put("avatarUrl", msg.avatarUrl ?: JSONObject.NULL)
                put("timestamp", msg.timestamp)
            }
            val request = Request.Builder()
                .url(url)
                .post(json.toString().toRequestBody(jsonMediaType))
                .build()
            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            Log.e(TAG, "Error sending message", e)
            false
        }
    }

    suspend fun fetchMessages(roomCode: String): List<ChatMessage> = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/rooms/$roomCode/messages.json?orderBy=\"timestamp\"&limitToLast=60"
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()
            val body = response.body?.string()
            if (response.isSuccessful && !body.isNullOrEmpty() && body != "null") {
                val json = JSONObject(body)
                val list = mutableListOf<ChatMessage>()
                val keys = json.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    val m = json.optJSONObject(k) ?: continue
                    list.add(
                        ChatMessage(
                            id = k,
                            roomCode = roomCode,
                            senderId = m.optString("senderId"),
                            senderName = m.optString("senderName"),
                            text = m.optString("text"),
                            time = m.optString("time"),
                            avatarUrl = if (m.isNull("avatarUrl")) null else m.optString("avatarUrl"),
                            timestamp = m.optLong("timestamp", System.currentTimeMillis()),
                            isMe = false
                        )
                    )
                }
                list.sortBy { it.timestamp }
                return@withContext list
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching messages", e)
        }
        emptyList()
    }

    // FRIEND REQUESTS
    suspend fun sendFriendRequest(req: FriendRequest): Boolean = withContext(Dispatchers.IO) {
        try {
            val cleanTargetUsername = req.toUsername.lowercase().trim()
            val url = "$BASE_URL/friend_requests/$cleanTargetUsername/${req.fromUserId}.json"
            val json = JSONObject().apply {
                put("id", req.id)
                put("fromUserId", req.fromUserId)
                put("fromUsername", req.fromUsername)
                put("fromName", req.fromName)
                put("fromAvatarUrl", req.fromAvatarUrl ?: JSONObject.NULL)
                put("toUsername", cleanTargetUsername)
                put("timestamp", req.timestamp)
            }
            val request = Request.Builder().url(url).put(json.toString().toRequestBody(jsonMediaType)).build()
            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            Log.e(TAG, "Error sending friend request", e)
            false
        }
    }

    suspend fun fetchFriendRequests(myUsername: String): List<FriendRequest> = withContext(Dispatchers.IO) {
        try {
            val cleanUsername = myUsername.lowercase().trim()
            val url = "$BASE_URL/friend_requests/$cleanUsername.json"
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()
            val body = response.body?.string()
            if (response.isSuccessful && !body.isNullOrEmpty() && body != "null") {
                val json = JSONObject(body)
                val list = mutableListOf<FriendRequest>()
                val keys = json.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    val req = json.optJSONObject(k) ?: continue
                    list.add(
                        FriendRequest(
                            id = req.optString("id", k),
                            fromUserId = req.optString("fromUserId"),
                            fromUsername = req.optString("fromUsername"),
                            fromName = req.optString("fromName"),
                            fromAvatarUrl = if (req.isNull("fromAvatarUrl")) null else req.optString("fromAvatarUrl"),
                            toUsername = req.optString("toUsername", cleanUsername),
                            timestamp = req.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
                list.sortByDescending { it.timestamp }
                return@withContext list
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching friend requests", e)
        }
        emptyList()
    }

    suspend fun respondFriendRequest(
        myUser: User,
        request: FriendRequest,
        accept: Boolean
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val cleanUsername = myUser.username.lowercase().trim()
            // Delete request
            val delUrl = "$BASE_URL/friend_requests/$cleanUsername/${request.fromUserId}.json"
            client.newCall(Request.Builder().url(delUrl).delete().build()).execute()

            if (accept) {
                // Add to my friends
                val myFriendsUrl = "$BASE_URL/friends/${myUser.id}/${request.fromUserId}.json"
                val f1 = JSONObject().apply {
                    put("userId", request.fromUserId)
                    put("username", request.fromUsername)
                    put("name", request.fromName)
                    put("avatarUrl", request.fromAvatarUrl ?: JSONObject.NULL)
                    put("isBlocked", false)
                }
                client.newCall(Request.Builder().url(myFriendsUrl).put(f1.toString().toRequestBody(jsonMediaType)).build()).execute()

                // Add to other user's friends
                val otherFriendsUrl = "$BASE_URL/friends/${request.fromUserId}/${myUser.id}.json"
                val f2 = JSONObject().apply {
                    put("userId", myUser.id)
                    put("username", myUser.username)
                    put("name", myUser.name)
                    put("avatarUrl", myUser.avatarUrl ?: JSONObject.NULL)
                    put("isBlocked", false)
                }
                client.newCall(Request.Builder().url(otherFriendsUrl).put(f2.toString().toRequestBody(jsonMediaType)).build()).execute()
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error responding to friend request", e)
            false
        }
    }

    // PRESENCE & ONLINE ACTIVITY
    suspend fun updateUserPresence(username: String, userId: String, isOnline: Boolean): Boolean = withContext(Dispatchers.IO) {
        try {
            val now = System.currentTimeMillis()
            val presenceObj = JSONObject().apply {
                put("isOnline", isOnline)
                put("lastActive", now)
                put("username", username)
                put("userId", userId)
            }
            // Update presence node
            val pUrl = "$BASE_URL/presence/$userId.json"
            client.newCall(Request.Builder().url(pUrl).put(presenceObj.toString().toRequestBody(jsonMediaType)).build()).execute()

            // Update user node
            val uUrl = "$BASE_URL/users/$username.json"
            val uPatch = JSONObject().apply {
                put("isOnline", isOnline)
                put("lastActive", now)
            }
            client.newCall(Request.Builder().url(uUrl).patch(uPatch.toString().toRequestBody(jsonMediaType)).build()).execute()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun fetchAllPresence(): Map<String, UserPresence> = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/presence.json"
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()
            val body = response.body?.string()
            if (response.isSuccessful && !body.isNullOrEmpty() && body != "null") {
                val json = JSONObject(body)
                val map = mutableMapOf<String, UserPresence>()
                val keys = json.keys()
                val now = System.currentTimeMillis()
                while (keys.hasNext()) {
                    val k = keys.next()
                    val p = json.optJSONObject(k) ?: continue
                    val isOnline = p.optBoolean("isOnline", false)
                    val lastActive = p.optLong("lastActive", 0L)
                    val actuallyOnline = isOnline && (now - lastActive < 65_000)
                    map[k] = UserPresence(
                        userId = k,
                        isOnline = actuallyOnline,
                        lastActive = lastActive
                    )
                }
                return@withContext map
            }
        } catch (_: Exception) {}
        emptyMap()
    }

    suspend fun fetchFriends(myUserId: String): List<Friend> = withContext(Dispatchers.IO) {
        try {
            val presenceMap = fetchAllPresence()
            val url = "$BASE_URL/friends/$myUserId.json"
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()
            val body = response.body?.string()
            if (response.isSuccessful && !body.isNullOrEmpty() && body != "null") {
                val json = JSONObject(body)
                val list = mutableListOf<Friend>()
                val keys = json.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    val f = json.optJSONObject(k) ?: continue
                    val targetId = f.optString("userId", k)
                    val presence = presenceMap[targetId]
                    val isOnline = presence?.isOnline ?: false
                    val lastActive = presence?.lastActive ?: 0L

                    list.add(
                        Friend(
                            userId = targetId,
                            username = f.optString("username"),
                            name = f.optString("name"),
                            avatarUrl = if (f.isNull("avatarUrl")) null else f.optString("avatarUrl"),
                            isBlocked = f.optBoolean("isBlocked", false),
                            isOnline = isOnline,
                            lastActive = lastActive
                        )
                    )
                }
                return@withContext list
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching friends", e)
        }
        emptyList()
    }

    suspend fun setBlockUser(myUserId: String, targetUserId: String, block: Boolean): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/friends/$myUserId/$targetUserId.json"
            val json = JSONObject().apply {
                put("isBlocked", block)
            }
            val request = Request.Builder().url(url).patch(json.toString().toRequestBody(jsonMediaType)).build()
            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            Log.e(TAG, "Error setting block user", e)
            false
        }
    }

    // PRIVATE MESSAGING WITH DELIVERY & READ RECEIPTS
    suspend fun sendPrivateMessage(
        convId: String,
        msg: PrivateChatMessage,
        sender: User,
        receiver: User
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            // Check if receiver is online to determine initial delivery status
            val presenceMap = fetchAllPresence()
            val receiverPresence = presenceMap[receiver.id]
            val isReceiverOnline = receiverPresence?.isOnline == true

            val isDelivered = msg.isDelivered || isReceiverOnline
            val isRead = msg.isRead

            val url = "$BASE_URL/private_messages/$convId/${msg.id}.json"
            val json = JSONObject().apply {
                put("id", msg.id)
                put("conversationId", convId)
                put("senderId", msg.senderId)
                put("senderName", msg.senderName)
                put("text", msg.text)
                put("isVoice", false)
                put("voiceDurationSec", 0)
                put("time", msg.time)
                put("timestamp", msg.timestamp)
                put("isDelivered", isDelivered)
                put("isRead", isRead)
                put("deletedForEveryone", msg.deletedForEveryone)
            }
            val request = Request.Builder().url(url).put(json.toString().toRequestBody(jsonMediaType)).build()
            client.newCall(request).execute()

            // Update sender conversations index
            val sUrl = "$BASE_URL/user_conversations/${sender.id}/${receiver.id}.json"
            val sMeta = JSONObject().apply {
                put("id", convId)
                put("otherUserId", receiver.id)
                put("otherUsername", receiver.username)
                put("otherName", receiver.name)
                put("otherAvatarUrl", receiver.avatarUrl ?: JSONObject.NULL)
                put("lastMessage", msg.text)
                put("lastMessageTime", msg.time)
                put("lastTimestamp", msg.timestamp)
                put("lastMessageIsMe", true)
                put("lastMessageIsDelivered", isDelivered)
                put("lastMessageIsRead", isRead)
            }
            client.newCall(Request.Builder().url(sUrl).put(sMeta.toString().toRequestBody(jsonMediaType)).build()).execute()

            // Update receiver conversations index
            val rUrl = "$BASE_URL/user_conversations/${receiver.id}/${sender.id}.json"
            val rMeta = JSONObject().apply {
                put("id", convId)
                put("otherUserId", sender.id)
                put("otherUsername", sender.username)
                put("otherName", sender.name)
                put("otherAvatarUrl", sender.avatarUrl ?: JSONObject.NULL)
                put("lastMessage", msg.text)
                put("lastMessageTime", msg.time)
                put("lastTimestamp", msg.timestamp)
                put("lastMessageIsMe", false)
                put("lastMessageIsDelivered", isDelivered)
                put("lastMessageIsRead", isRead)
            }
            client.newCall(Request.Builder().url(rUrl).put(rMeta.toString().toRequestBody(jsonMediaType)).build()).execute()

            true
        } catch (e: Exception) {
            Log.e(TAG, "Error sending private message", e)
            false
        }
    }

    suspend fun markConversationMessagesAsRead(
        convId: String,
        readerId: String,
        otherUserId: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/private_messages/$convId.json"
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()
            val body = response.body?.string()
            if (response.isSuccessful && !body.isNullOrEmpty() && body != "null") {
                val json = JSONObject(body)
                val keys = json.keys()
                val now = System.currentTimeMillis()
                var updatedAny = false

                while (keys.hasNext()) {
                    val k = keys.next()
                    val m = json.optJSONObject(k) ?: continue
                    val senderId = m.optString("senderId")
                    val isRead = m.optBoolean("isRead", false)
                    val isDelivered = m.optBoolean("isDelivered", false)

                    // If message was sent by other user and is unread, mark as delivered & read
                    if (senderId != readerId && (!isRead || !isDelivered)) {
                        val patchUrl = "$BASE_URL/private_messages/$convId/$k.json"
                        val patchJson = JSONObject().apply {
                            put("isDelivered", true)
                            put("isRead", true)
                            put("readTimestamp", now)
                        }
                        client.newCall(Request.Builder().url(patchUrl).patch(patchJson.toString().toRequestBody(jsonMediaType)).build()).execute()
                        updatedAny = true
                    }
                }

                if (updatedAny) {
                    // Update user conversation read flags
                    val sUrl = "$BASE_URL/user_conversations/$otherUserId/$readerId.json"
                    val patchObj = JSONObject().apply {
                        put("lastMessageIsDelivered", true)
                        put("lastMessageIsRead", true)
                    }
                    client.newCall(Request.Builder().url(sUrl).patch(patchObj.toString().toRequestBody(jsonMediaType)).build()).execute()
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error marking messages as read", e)
            false
        }
    }

    suspend fun fetchUserConversations(userId: String): List<PrivateConversation> = withContext(Dispatchers.IO) {
        try {
            val presenceMap = fetchAllPresence()
            val url = "$BASE_URL/user_conversations/$userId.json"
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()
            val body = response.body?.string()
            if (response.isSuccessful && !body.isNullOrEmpty() && body != "null") {
                val json = JSONObject(body)
                val list = mutableListOf<PrivateConversation>()
                val keys = json.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    val c = json.optJSONObject(k) ?: continue
                    val otherId = c.optString("otherUserId", k)
                    val presence = presenceMap[otherId]
                    val isOnline = presence?.isOnline ?: false
                    val lastActive = presence?.lastActive ?: 0L

                    list.add(
                        PrivateConversation(
                            id = c.optString("id", "${userId}_$k"),
                            otherUserId = otherId,
                            otherUsername = c.optString("otherUsername", ""),
                            otherName = c.optString("otherName", "مستخدم"),
                            otherAvatarUrl = if (c.isNull("otherAvatarUrl")) null else c.optString("otherAvatarUrl"),
                            lastMessage = c.optString("lastMessage", ""),
                            lastMessageTime = c.optString("lastMessageTime", ""),
                            lastTimestamp = c.optLong("lastTimestamp", 0L),
                            isOtherOnline = isOnline,
                            otherLastActive = lastActive,
                            lastMessageIsMe = c.optBoolean("lastMessageIsMe", false),
                            lastMessageIsDelivered = c.optBoolean("lastMessageIsDelivered", false),
                            lastMessageIsRead = c.optBoolean("lastMessageIsRead", false)
                        )
                    )
                }
                list.sortByDescending { it.lastTimestamp }
                return@withContext list
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user conversations", e)
        }
        emptyList()
    }

    suspend fun fetchPrivateMessages(convId: String, myUserId: String): List<PrivateChatMessage> = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/private_messages/$convId.json"
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()
            val body = response.body?.string()
            if (response.isSuccessful && !body.isNullOrEmpty() && body != "null") {
                val json = JSONObject(body)
                val list = mutableListOf<PrivateChatMessage>()
                val keys = json.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    val m = json.optJSONObject(k) ?: continue
                    val forEveryone = m.optBoolean("deletedForEveryone", false)
                    val delUsers = mutableListOf<String>()
                    val delJson = m.optJSONArray("deletedForUserIds")
                    if (delJson != null) {
                        for (i in 0 until delJson.length()) {
                            delUsers.add(delJson.optString(i))
                        }
                    }

                    // Check if deleted for me
                    if (delUsers.contains(myUserId)) {
                        continue
                    }

                    val senderId = m.optString("senderId")
                    val isMe = senderId == myUserId
                    val isDelivered = m.optBoolean("isDelivered", false)
                    val isRead = m.optBoolean("isRead", false)
                    val readTs = if (m.isNull("readTimestamp")) null else m.optLong("readTimestamp")

                    list.add(
                        PrivateChatMessage(
                            id = m.optString("id", k),
                            conversationId = convId,
                            senderId = senderId,
                            senderName = m.optString("senderName"),
                            text = if (forEveryone) "تم حذف هذه الرسالة لدى الجميع" else m.optString("text"),
                            isVoice = false,
                            voiceDurationSec = 0,
                            time = m.optString("time"),
                            timestamp = m.optLong("timestamp", System.currentTimeMillis()),
                            isMe = isMe,
                            isDelivered = isDelivered,
                            isRead = isRead,
                            readTimestamp = readTs,
                            deletedForEveryone = forEveryone,
                            deletedForUserIds = delUsers
                        )
                    )
                }
                list.sortBy { it.timestamp }
                return@withContext list
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching private messages", e)
        }
        emptyList()
    }

    // 1-ON-1 VOICE CALLS
    suspend fun initiateVoiceCall(call: CallSession): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/calls/${call.id}.json"
            val json = JSONObject().apply {
                put("id", call.id)
                put("callerId", call.callerId)
                put("callerName", call.callerName)
                put("callerUsername", call.callerUsername)
                put("callerAvatar", call.callerAvatar ?: JSONObject.NULL)
                put("receiverId", call.receiverId)
                put("receiverName", call.receiverName)
                put("receiverUsername", call.receiverUsername)
                put("receiverAvatar", call.receiverAvatar ?: JSONObject.NULL)
                put("channelId", call.channelId)
                put("status", call.status)
                put("timestamp", call.timestamp)
            }
            val request = Request.Builder().url(url).put(json.toString().toRequestBody(jsonMediaType)).build()
            client.newCall(request).execute().isSuccessful
        } catch (e: Exception) {
            Log.e(TAG, "Error initiating voice call", e)
            false
        }
    }

    suspend fun updateCallStatus(callId: String, status: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/calls/$callId.json"
            val json = JSONObject().apply {
                put("status", status)
            }
            val request = Request.Builder().url(url).patch(json.toString().toRequestBody(jsonMediaType)).build()
            client.newCall(request).execute().isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun fetchCallSession(callId: String): CallSession? = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/calls/$callId.json"
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()
            val body = response.body?.string()
            if (response.isSuccessful && !body.isNullOrEmpty() && body != "null") {
                val json = JSONObject(body)
                return@withContext CallSession(
                    id = json.optString("id", callId),
                    callerId = json.optString("callerId"),
                    callerName = json.optString("callerName"),
                    callerUsername = json.optString("callerUsername"),
                    callerAvatar = if (json.isNull("callerAvatar")) null else json.optString("callerAvatar"),
                    receiverId = json.optString("receiverId"),
                    receiverName = json.optString("receiverName"),
                    receiverUsername = json.optString("receiverUsername"),
                    receiverAvatar = if (json.isNull("receiverAvatar")) null else json.optString("receiverAvatar"),
                    channelId = json.optString("channelId"),
                    status = json.optString("status", "ringing"),
                    timestamp = json.optLong("timestamp", 0L)
                )
            }
        } catch (_: Exception) {}
        null
    }

    suspend fun fetchActiveIncomingCall(myUserId: String): CallSession? = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/calls.json"
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()
            val body = response.body?.string()
            if (response.isSuccessful && !body.isNullOrEmpty() && body != "null") {
                val json = JSONObject(body)
                val now = System.currentTimeMillis()
                val keys = json.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    val c = json.optJSONObject(k) ?: continue
                    val receiverId = c.optString("receiverId")
                    val status = c.optString("status")
                    val ts = c.optLong("timestamp", 0L)
                    if (receiverId == myUserId && status == "ringing" && now - ts < 40000) {
                        return@withContext CallSession(
                            id = c.optString("id", k),
                            callerId = c.optString("callerId"),
                            callerName = c.optString("callerName"),
                            callerUsername = c.optString("callerUsername"),
                            callerAvatar = if (c.isNull("callerAvatar")) null else c.optString("callerAvatar"),
                            receiverId = receiverId,
                            receiverName = c.optString("receiverName"),
                            receiverUsername = c.optString("receiverUsername"),
                            receiverAvatar = if (c.isNull("receiverAvatar")) null else c.optString("receiverAvatar"),
                            channelId = c.optString("channelId"),
                            status = status,
                            timestamp = ts
                        )
                    }
                }
            }
        } catch (_: Exception) {}
        null
    }

    suspend fun deletePrivateMessage(
        convId: String,
        msgId: String,
        forEveryone: Boolean,
        myUserId: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/private_messages/$convId/$msgId.json"
            if (forEveryone) {
                val json = JSONObject().apply {
                    put("deletedForEveryone", true)
                    put("text", "تم حذف هذه الرسالة لدى الجميع")
                }
                val request = Request.Builder().url(url).patch(json.toString().toRequestBody(jsonMediaType)).build()
                client.newCall(request).execute().isSuccessful
            } else {
                // Delete for me
                val getReq = Request.Builder().url(url).get().build()
                val resp = client.newCall(getReq).execute()
                val body = resp.body?.string()
                if (resp.isSuccessful && !body.isNullOrEmpty() && body != "null") {
                    val json = JSONObject(body)
                    val arr = json.optJSONArray("deletedForUserIds")?.let { ja ->
                        (0 until ja.length()).map { ja.getString(it) }.toMutableList()
                    } ?: mutableListOf()
                    if (!arr.contains(myUserId)) arr.add(myUserId)
                    val patchJson = JSONObject().apply {
                        put("deletedForUserIds", org.json.JSONArray(arr))
                    }
                    val request = Request.Builder().url(url).patch(patchJson.toString().toRequestBody(jsonMediaType)).build()
                    client.newCall(request).execute().isSuccessful
                } else false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting private message", e)
            false
        }
    }

    // FRIENDS ROOMS
    suspend fun fetchAllRooms(): List<RoomInfo> = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/rooms.json"
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()
            val body = response.body?.string()
            if (response.isSuccessful && !body.isNullOrEmpty() && body != "null") {
                val json = JSONObject(body)
                val list = mutableListOf<RoomInfo>()
                val keys = json.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    val r = json.optJSONObject(k) ?: continue
                    val pass = if (r.isNull("password")) null else r.optString("password")
                    list.add(
                        RoomInfo(
                            id = r.optString("id", k),
                            name = r.optString("name", "غرفة $k"),
                            host = r.optString("host", "مستخدم"),
                            hostId = r.optString("hostId", ""),
                            password = pass,
                            isLocked = !pass.isNullOrBlank() || r.optBoolean("isLocked", false),
                            createdAt = r.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
                list.sortByDescending { it.createdAt }
                return@withContext list
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching all rooms", e)
        }
        emptyList()
    }

    // DYNAMIC URLS
    suspend fun getDownloadUrl(): String = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/Download/Url.json"
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()
            val body = response.body?.string()?.trim()
            if (response.isSuccessful && !body.isNullOrEmpty() && body != "null") {
                return@withContext body.replace("\"", "")
            }
        } catch (_: Exception) {}
        "https://owner.cybenode.site/"
    }

    suspend fun getSupportUrl(): String = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/support/Url.json"
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()
            val body = response.body?.string()?.trim()
            if (response.isSuccessful && !body.isNullOrEmpty() && body != "null") {
                return@withContext body.replace("\"", "")
            }
        } catch (_: Exception) {}
        "https://owner.cybenode.site/"
    }

    suspend fun getPrivacyPolicyUrl(): String = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/privacy/Url.json"
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()
            val body = response.body?.string()?.trim()
            if (response.isSuccessful && !body.isNullOrEmpty() && body != "null") {
                return@withContext body.replace("\"", "")
            }
        } catch (_: Exception) {}
        "https://owner.cybenode.site/privacy"
    }

    suspend fun getCloudflareWorkerUrl(): String? = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/config/cloudflare_worker_url.json"
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()
            val body = response.body?.string()?.trim()
            if (response.isSuccessful && !body.isNullOrEmpty() && body != "null") {
                return@withContext body.replace("\"", "")
            }
        } catch (_: Exception) {}
        null
    }

    suspend fun sendForumMessage(msg: ChatMessage): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/forum/messages.json"
            val json = JSONObject().apply {
                put("senderId", msg.senderId)
                put("senderName", msg.senderName)
                put("text", msg.text)
                put("time", msg.time)
                put("avatarUrl", msg.avatarUrl ?: JSONObject.NULL)
                put("timestamp", msg.timestamp)
            }
            val request = Request.Builder().url(url).post(json.toString().toRequestBody(jsonMediaType)).build()
            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (_: Exception) {
            false
        }
    }

    suspend fun fetchForumMessages(): List<ChatMessage> = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/forum/messages.json?orderBy=\"timestamp\"&limitToLast=100"
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()
            val body = response.body?.string()
            if (response.isSuccessful && !body.isNullOrEmpty() && body != "null") {
                val json = JSONObject(body)
                val list = mutableListOf<ChatMessage>()
                val keys = json.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    val m = json.optJSONObject(k) ?: continue
                    list.add(
                        ChatMessage(
                            id = k,
                            roomCode = "forum",
                            senderId = m.optString("senderId"),
                            senderName = m.optString("senderName"),
                            text = m.optString("text"),
                            time = m.optString("time"),
                            avatarUrl = if (m.isNull("avatarUrl")) null else m.optString("avatarUrl"),
                            timestamp = m.optLong("timestamp", System.currentTimeMillis()),
                            isMe = false
                        )
                    )
                }
                list.sortBy { it.timestamp }
                return@withContext list
            }
        } catch (_: Exception) {}
        emptyList()
    }

    // FRIEND ROOM CREATION NOTIFICATIONS
    suspend fun sendFriendRoomNotification(
        friendUserId: String,
        friendName: String,
        roomName: String,
        roomId: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val notifId = "rnotif_${System.currentTimeMillis()}"
            val url = "$BASE_URL/friend_notifications/$friendUserId/$notifId.json"
            val json = JSONObject().apply {
                put("id", notifId)
                put("friendName", friendName)
                put("roomName", roomName)
                put("roomId", roomId)
                put("timestamp", System.currentTimeMillis())
            }
            val request = Request.Builder().url(url).put(json.toString().toRequestBody(jsonMediaType)).build()
            client.newCall(request).execute().isSuccessful
        } catch (_: Exception) {
            false
        }
    }

    suspend fun fetchFriendRoomNotifications(userId: String): List<JSONObject> = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/friend_notifications/$userId.json"
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()
            val body = response.body?.string()
            if (response.isSuccessful && !body.isNullOrEmpty() && body != "null") {
                val json = JSONObject(body)
                val list = mutableListOf<JSONObject>()
                val keys = json.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    val item = json.optJSONObject(k) ?: continue
                    item.put("key", k)
                    list.add(item)
                }
                return@withContext list
            }
        } catch (_: Exception) {}
        emptyList()
    }

    suspend fun clearFriendRoomNotification(userId: String, notifKey: String) = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/friend_notifications/$userId/$notifKey.json"
            val request = Request.Builder().url(url).delete().build()
            client.newCall(request).execute()
        } catch (_: Exception) {}
    }

    // REAL-TIME AUDIO STREAMING ENDPOINTS
    suspend fun broadcastRoomAudio(
        roomCode: String,
        userId: String,
        userName: String,
        base64Pcm: String,
        volume: Float
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/rooms/$roomCode/audio_stream/$userId.json"
            val json = JSONObject().apply {
                put("senderId", userId)
                put("senderName", userName)
                put("data", base64Pcm)
                put("volume", volume)
                put("timestamp", System.currentTimeMillis())
            }
            val request = Request.Builder()
                .url(url)
                .put(json.toString().toRequestBody(jsonMediaType))
                .build()
            client.newCall(request).execute().isSuccessful
        } catch (_: Exception) {
            false
        }
    }

    suspend fun fetchRoomAudioStreams(roomCode: String): Map<String, JSONObject> = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/rooms/$roomCode/audio_stream.json"
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()
            val body = response.body?.string()
            if (response.isSuccessful && !body.isNullOrEmpty() && body != "null") {
                val json = JSONObject(body)
                val map = mutableMapOf<String, JSONObject>()
                val keys = json.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    val item = json.optJSONObject(k) ?: continue
                    map[k] = item
                }
                return@withContext map
            }
        } catch (_: Exception) {}
        emptyMap()
    }

    suspend fun clearRoomAudio(roomCode: String, userId: String) = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/rooms/$roomCode/audio_stream/$userId.json"
            val request = Request.Builder().url(url).delete().build()
            client.newCall(request).execute()
        } catch (_: Exception) {}
    }

    suspend fun sendCallAudio(
        callId: String,
        userId: String,
        base64Pcm: String,
        volume: Float
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/voice_calls/$callId/audio/$userId.json"
            val json = JSONObject().apply {
                put("senderId", userId)
                put("data", base64Pcm)
                put("volume", volume)
                put("timestamp", System.currentTimeMillis())
            }
            val request = Request.Builder()
                .url(url)
                .put(json.toString().toRequestBody(jsonMediaType))
                .build()
            client.newCall(request).execute().isSuccessful
        } catch (_: Exception) {
            false
        }
    }

    suspend fun fetchCallAudio(callId: String): Map<String, JSONObject> = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/voice_calls/$callId/audio.json"
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()
            val body = response.body?.string()
            if (response.isSuccessful && !body.isNullOrEmpty() && body != "null") {
                val json = JSONObject(body)
                val map = mutableMapOf<String, JSONObject>()
                val keys = json.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    val item = json.optJSONObject(k) ?: continue
                    map[k] = item
                }
                return@withContext map
            }
        } catch (_: Exception) {}
        emptyMap()
    }

    suspend fun clearCallAudio(callId: String, userId: String) = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/voice_calls/$callId/audio/$userId.json"
            val request = Request.Builder().url(url).delete().build()
            client.newCall(request).execute()
        } catch (_: Exception) {}
    }

    // SERVER-SENT EVENTS (SSE) REAL-TIME LISTENERS
    suspend fun listenRoomAudioSse(
        roomCode: String,
        onPacket: (senderId: String, data: String, volume: Float, timestamp: Long) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/rooms/$roomCode/audio_stream.json"
            val request = Request.Builder()
                .url(url)
                .addHeader("Accept", "text/event-stream")
                .get()
                .build()

            val call = sseClient.newCall(request)
            val response = call.execute()
            val source = response.body?.source() ?: return@withContext

            while (!source.exhausted()) {
                val line = source.readUtf8Line() ?: break
                if (line.startsWith("data: ")) {
                    val jsonStr = line.removePrefix("data: ").trim()
                    if (jsonStr.isNotEmpty() && jsonStr != "null") {
                        try {
                            val obj = JSONObject(jsonStr)
                            val path = obj.optString("path", "")
                            val rawData = obj.opt("data")

                            if (rawData is JSONObject) {
                                if (path == "/" || path.isEmpty()) {
                                    // Root map of all users
                                    val keys = rawData.keys()
                                    while (keys.hasNext()) {
                                        val sId = keys.next()
                                        val userObj = rawData.optJSONObject(sId)
                                        if (userObj != null) {
                                            val audioData = userObj.optString("data", "")
                                            val vol = userObj.optDouble("volume", 0.0).toFloat()
                                            val ts = userObj.optLong("timestamp", System.currentTimeMillis())
                                            if (audioData.isNotEmpty()) {
                                                onPacket(sId, audioData, vol, ts)
                                            }
                                        }
                                    }
                                } else {
                                    // Single user node: path = "/userId"
                                    val sId = path.trimStart('/').split('/').firstOrNull() ?: rawData.optString("senderId", "")
                                    val audioData = rawData.optString("data", "")
                                    val vol = rawData.optDouble("volume", 0.0).toFloat()
                                    val ts = rawData.optLong("timestamp", System.currentTimeMillis())
                                    if (audioData.isNotEmpty() && sId.isNotEmpty()) {
                                        onPacket(sId, audioData, vol, ts)
                                    }
                                }
                            } else if (rawData is String && rawData.isNotEmpty()) {
                                val sId = path.trimStart('/').split('/').firstOrNull() ?: ""
                                if (sId.isNotEmpty()) {
                                    onPacket(sId, rawData, 0.5f, System.currentTimeMillis())
                                }
                            }
                        } catch (_: Exception) {}
                    }
                }
            }
        } catch (_: Exception) {}
    }

    suspend fun listenCallAudioSse(
        callId: String,
        onPacket: (senderId: String, data: String, volume: Float, timestamp: Long) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/voice_calls/$callId/audio.json"
            val request = Request.Builder()
                .url(url)
                .addHeader("Accept", "text/event-stream")
                .get()
                .build()

            val call = sseClient.newCall(request)
            val response = call.execute()
            val source = response.body?.source() ?: return@withContext

            while (!source.exhausted()) {
                val line = source.readUtf8Line() ?: break
                if (line.startsWith("data: ")) {
                    val jsonStr = line.removePrefix("data: ").trim()
                    if (jsonStr.isNotEmpty() && jsonStr != "null") {
                        try {
                            val obj = JSONObject(jsonStr)
                            val path = obj.optString("path", "")
                            val rawData = obj.opt("data")

                            if (rawData is JSONObject) {
                                if (path == "/" || path.isEmpty()) {
                                    val keys = rawData.keys()
                                    while (keys.hasNext()) {
                                        val sId = keys.next()
                                        val userObj = rawData.optJSONObject(sId)
                                        if (userObj != null) {
                                            val audioData = userObj.optString("data", "")
                                            val vol = userObj.optDouble("volume", 0.0).toFloat()
                                            val ts = userObj.optLong("timestamp", System.currentTimeMillis())
                                            if (audioData.isNotEmpty()) {
                                                onPacket(sId, audioData, vol, ts)
                                            }
                                        }
                                    }
                                } else {
                                    val sId = path.trimStart('/').split('/').firstOrNull() ?: rawData.optString("senderId", "")
                                    val audioData = rawData.optString("data", "")
                                    val vol = rawData.optDouble("volume", 0.0).toFloat()
                                    val ts = rawData.optLong("timestamp", System.currentTimeMillis())
                                    if (audioData.isNotEmpty() && sId.isNotEmpty()) {
                                        onPacket(sId, audioData, vol, ts)
                                    }
                                }
                            } else if (rawData is String && rawData.isNotEmpty()) {
                                val sId = path.trimStart('/').split('/').firstOrNull() ?: ""
                                if (sId.isNotEmpty()) {
                                    onPacket(sId, rawData, 0.5f, System.currentTimeMillis())
                                }
                            }
                        } catch (_: Exception) {}
                    }
                }
            }
        } catch (_: Exception) {}
    }
}
