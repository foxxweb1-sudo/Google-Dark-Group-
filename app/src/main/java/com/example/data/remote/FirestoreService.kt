package com.example.data.remote

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreService(context: Context) {
    private val TAG = "FirestoreService"
    private val db = FirebaseFirestore.getInstance(
        context.applicationContext.getString(R.string.firestore_database_id)
    )

    // USERS
    suspend fun saveUser(user: FUser) {
        try {
            db.collection("users").document(user.userId).set(user, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.w(TAG, "saveUser failed for ${user.userId}: ${e.message}")
        }
    }

    suspend fun getUser(userId: String): FUser? {
        return try {
            db.collection("users").document(userId).get().await().toObject(FUser::class.java)
        } catch (e: Exception) {
            Log.w(TAG, "getUser failed for $userId: ${e.message}")
            null
        }
    }

    fun observeUser(userId: String): Flow<FUser?> {
        return db.collection("users").document(userId).snapshots()
            .map { it.toObject(FUser::class.java) }
            .catch { e ->
                Log.w(TAG, "observeUser failed: ${e.message}")
                emit(null)
            }
    }

    suspend fun searchUserByUsername(username: String): FUser? {
        return try {
            val result = db.collection("users")
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .await()
            result.documents.firstOrNull()?.toObject(FUser::class.java)
        } catch (e: Exception) {
            Log.w(TAG, "searchUserByUsername failed: ${e.message}")
            null
        }
    }

    // ROOMS
    suspend fun createRoom(room: FRoom) {
        try {
            db.collection("rooms").document(room.id).set(room).await()
        } catch (e: Exception) {
            Log.w(TAG, "createRoom failed: ${e.message}")
        }
    }

    suspend fun deleteRoom(roomId: String) {
        try {
            db.collection("rooms").document(roomId).delete().await()
        } catch (e: Exception) {
            Log.w(TAG, "deleteRoom failed: ${e.message}")
        }
    }

    fun observeRooms(): Flow<List<FRoom>> {
        return db.collection("rooms")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .snapshots()
            .map { it.toObjects(FRoom::class.java) }
            .catch { e ->
                Log.w(TAG, "observeRooms failed: ${e.message}")
                emit(emptyList())
            }
    }

    suspend fun getRoom(roomId: String): FRoom? {
        return try {
            db.collection("rooms").document(roomId).get().await().toObject(FRoom::class.java)
        } catch (e: Exception) {
            Log.w(TAG, "getRoom failed: ${e.message}")
            null
        }
    }

    suspend fun joinRoom(roomId: String, member: FRoomMember, voiceChannelId: String) {
        try {
            val roomRef = db.collection("rooms").document(roomId)
            // Register member in subcollection
            roomRef.collection("members").document(member.userId).set(member, SetOptions.merge()).await()
            // Update room voice channel ID, add to participants list, and update count
            val updates = mutableMapOf<String, Any>(
                "voiceChannelId" to voiceChannelId,
                "participants" to com.google.firebase.firestore.FieldValue.arrayUnion(member.userId)
            )
            roomRef.set(updates, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.w(TAG, "joinRoom failed for ${member.userId} in $roomId: ${e.message}")
        }
    }

    suspend fun leaveRoom(roomId: String, userId: String) {
        try {
            val roomRef = db.collection("rooms").document(roomId)
            // Remove member from subcollection
            roomRef.collection("members").document(userId).delete().await()
            // Remove from room participants list
            val updates = mapOf(
                "participants" to com.google.firebase.firestore.FieldValue.arrayRemove(userId)
            )
            roomRef.set(updates, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.w(TAG, "leaveRoom failed for $userId in $roomId: ${e.message}")
        }
    }

    fun observeRoomMembers(roomId: String): Flow<List<FRoomMember>> {
        return db.collection("rooms").document(roomId).collection("members")
            .orderBy("joinedAt", com.google.firebase.firestore.Query.Direction.ASCENDING)
            .snapshots()
            .map { it.toObjects(FRoomMember::class.java) }
            .catch { e ->
                Log.w(TAG, "observeRoomMembers failed for $roomId: ${e.message}")
                emit(emptyList())
            }
    }

    suspend fun updateMemberVoiceState(roomId: String, userId: String, isMuted: Boolean, isSpeaking: Boolean) {
        try {
            db.collection("rooms").document(roomId).collection("members").document(userId)
                .update(
                    mapOf(
                        "isMuted" to isMuted,
                        "isSpeaking" to isSpeaking
                    )
                ).await()
        } catch (e: Exception) {
            Log.w(TAG, "updateMemberVoiceState failed: ${e.message}")
        }
    }

    // MESSAGES
    suspend fun sendMessage(roomId: String, message: FMessage) {
        try {
            db.collection("rooms").document(roomId).collection("messages").add(message).await()
        } catch (e: Exception) {
            Log.w(TAG, "sendMessage failed: ${e.message}")
        }
    }

    fun observeMessages(roomId: String): Flow<List<FMessage>> {
        return db.collection("rooms").document(roomId).collection("messages")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
            .limitToLast(100)
            .snapshots()
            .map { it.toObjects(FMessage::class.java) }
            .catch { e ->
                Log.w(TAG, "observeMessages failed: ${e.message}")
                emit(emptyList())
            }
    }

    // FORUM (Public Room)
    fun observeForumMessages(): Flow<List<FMessage>> {
        return db.collection("forum")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
            .limitToLast(100)
            .snapshots()
            .map { it.toObjects(FMessage::class.java) }
            .catch { e ->
                Log.w(TAG, "observeForumMessages failed: ${e.message}")
                emit(emptyList())
            }
    }

    suspend fun sendForumMessage(message: FMessage) {
        try {
            db.collection("forum").add(message).await()
        } catch (e: Exception) {
            Log.w(TAG, "sendForumMessage failed: ${e.message}")
        }
    }

    suspend fun deleteForumMessage(messageId: String) {
        try {
            db.collection("forum").document(messageId).delete().await()
        } catch (e: Exception) {
            Log.w(TAG, "deleteForumMessage failed: ${e.message}")
        }
    }

    // CALLS
    suspend fun initiateCall(call: FCall) {
        try {
            db.collection("calls").document(call.id).set(call).await()
        } catch (e: Exception) {
            Log.w(TAG, "initiateCall failed: ${e.message}")
        }
    }

    suspend fun updateCallStatus(callId: String, status: String) {
        try {
            db.collection("calls").document(callId).update("status", status).await()
        } catch (e: Exception) {
            Log.w(TAG, "updateCallStatus failed: ${e.message}")
        }
    }

    fun observeCall(callId: String): Flow<FCall?> {
        return db.collection("calls").document(callId).snapshots()
            .map { it.toObject(FCall::class.java) }
            .catch { e ->
                Log.w(TAG, "observeCall failed: ${e.message}")
                emit(null)
            }
    }

    suspend fun getIncomingCallsOnce(myUserId: String): List<FCall> {
        return try {
            db.collection("calls")
                .whereEqualTo("receiverId", myUserId)
                .whereEqualTo("status", "ringing")
                .get()
                .await()
                .toObjects(FCall::class.java)
        } catch (e: Exception) {
            Log.w(TAG, "getIncomingCallsOnce failed: ${e.message}")
            emptyList()
        }
    }
}
