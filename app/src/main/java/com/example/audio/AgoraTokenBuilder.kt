package com.example.audio

import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.security.MessageDigest
import java.util.zip.CRC32
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object AgoraTokenBuilder {

    private const val VERSION = "006"

    enum class Role(val value: Int) {
        ATTENDEE(0),
        PUBLISHER(1),
        SUBSCRIBER(2),
        ADMIN(101)
    }

    fun buildTokenWithUid(
        appId: String,
        appCertificate: String,
        channelName: String,
        uid: Int,
        role: Role = Role.PUBLISHER,
        privilegeTs: Int = (System.currentTimeMillis() / 1000 + 86400).toInt() // 24 hours validity
    ): String {
        if (appCertificate.isBlank()) {
            return ""
        }

        try {
            val salt = (1..99999999).random()

            val messageBytes = buildPrivilegeMessage(role.value, privilegeTs)

            val toSignStream = ByteArrayOutputStream()
            toSignStream.write(appId.toByteArray())
            toSignStream.write(channelName.toByteArray())
            
            // Pack integer UID (4 bytes, little endian)
            val uidBytes = ByteBufferHelper.packUint32(uid.toLong() and 0xFFFFFFFFL)
            toSignStream.write(uidBytes)
            
            toSignStream.write(ByteBufferHelper.packUint32(salt.toLong()))
            toSignStream.write(ByteBufferHelper.packUint32(privilegeTs.toLong()))
            toSignStream.write(messageBytes)

            val signature = hmacSign(appCertificate.toByteArray(), toSignStream.toByteArray())

            val contentStream = ByteArrayOutputStream()
            contentStream.write(signature)
            
            val crcChannel = (CRC32().apply { update(channelName.toByteArray()) }.value and 0xFFFFFFFFL)
            val crcUid = (uid.toLong() and 0xFFFFFFFFL)

            contentStream.write(ByteBufferHelper.packUint32(crcChannel))
            contentStream.write(ByteBufferHelper.packUint32(crcUid))
            contentStream.write(ByteBufferHelper.packBytes(ByteBufferHelper.packUint32(salt.toLong()) + ByteBufferHelper.packUint32(privilegeTs.toLong()) + ByteBufferHelper.packBytes(messageBytes)))

            val base64Content = Base64.encodeToString(contentStream.toByteArray(), Base64.NO_WRAP)
            return "$VERSION$appId$base64Content"
        } catch (_: Exception) {
            return ""
        }
    }

    private fun buildPrivilegeMessage(role: Int, privilegeTs: Int): ByteArray {
        val stream = ByteArrayOutputStream()
        val dos = DataOutputStream(stream)
        val tsBytes = ByteBufferHelper.packUint32(privilegeTs.toLong())

        // Map of privileges: 1 -> join channel, 2 -> publish audio stream
        val messages = mutableMapOf<Short, ByteArray>()
        messages[1.toShort()] = tsBytes
        messages[2.toShort()] = tsBytes

        dos.writeShort(messages.size)
        for ((k, v) in messages) {
            dos.writeShort(k.toInt())
            dos.write(v)
        }
        return stream.toByteArray()
    }

    private fun hmacSign(key: ByteArray, message: ByteArray): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key, "HmacSHA256"))
        return mac.doFinal(message)
    }

    private object ByteBufferHelper {
        fun packUint32(value: Long): ByteArray {
            return byteArrayOf(
                (value and 0xFF).toByte(),
                ((value shr 8) and 0xFF).toByte(),
                ((value shr 16) and 0xFF).toByte(),
                ((value shr 24) and 0xFF).toByte()
            )
        }

        fun packBytes(bytes: ByteArray): ByteArray {
            val len = bytes.size
            val lenBytes = byteArrayOf(
                (len and 0xFF).toByte(),
                ((len shr 8) and 0xFF).toByte()
            )
            return lenBytes + bytes
        }
    }
}
