package com.example.audio

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder

class UdpAudioRelay(private val scope: CoroutineScope) {
    companion object {
        private const val TAG = "UdpAudioRelay"
        const val UDP_PORT = 19876
        private const val MAGIC_HEADER = 0x44475250 // "DGRP" in ASCII
    }

    private var socket: DatagramSocket? = null
    private var listenJob: Job? = null
    private var broadcastAddress: InetAddress? = null

    var onLocalAudioReceived: ((pcm: ShortArray, senderIdHash: Int) -> Unit)? = null

    fun start(myRoomCode: String, myUserId: String) {
        stop()
        try {
            val s = DatagramSocket(UDP_PORT)
            s.broadcast = true
            s.reuseAddress = true
            socket = s
            broadcastAddress = InetAddress.getByName("255.255.255.255")

            val myRoomHash = myRoomCode.hashCode()
            val myUserHash = myUserId.hashCode()

            listenJob = scope.launch(Dispatchers.IO) {
                val buffer = ByteArray(2048)
                val packet = DatagramPacket(buffer, buffer.size)

                while (isActive && socket != null && !s.isClosed) {
                    try {
                        s.receive(packet)
                        val length = packet.length
                        if (length >= 14) { // Magic(4) + RoomHash(4) + UserHash(4) + SamplesCount(2)
                            val bb = ByteBuffer.wrap(buffer, 0, length).order(ByteOrder.LITTLE_ENDIAN)
                            val magic = bb.int
                            if (magic == MAGIC_HEADER) {
                                val roomHash = bb.int
                                val senderHash = bb.int
                                val samplesCount = bb.short.toInt() and 0xFFFF

                                if (roomHash == myRoomHash && senderHash != myUserHash && samplesCount > 0) {
                                    val pcm = ShortArray(samplesCount)
                                    for (i in 0 until samplesCount) {
                                        if (bb.remaining() >= 2) {
                                            pcm[i] = bb.short
                                        }
                                    }
                                    if (pcm.isNotEmpty()) {
                                        onLocalAudioReceived?.invoke(pcm, senderHash)
                                    }
                                }
                            }
                        }
                    } catch (_: Exception) {}
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Local UDP relay socket bind info: ${e.message}")
        }
    }

    fun sendUdpAudio(roomCode: String, userId: String, pcm: ShortArray, length: Int = pcm.size) {
        val s = socket ?: return
        val target = broadcastAddress ?: return
        if (s.isClosed || length <= 0) return

        try {
            val samplesToSend = minOf(length, 480)
            val byteCount = 14 + (samplesToSend * 2)
            val buffer = ByteArray(byteCount)
            val bb = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN)
            bb.putInt(MAGIC_HEADER)
            bb.putInt(roomCode.hashCode())
            bb.putInt(userId.hashCode())
            bb.putShort(samplesToSend.toShort())
            for (i in 0 until samplesToSend) {
                bb.putShort(pcm[i])
            }

            val packet = DatagramPacket(buffer, byteCount, target, UDP_PORT)
            s.send(packet)
        } catch (_: Exception) {}
    }

    fun stop() {
        listenJob?.cancel()
        listenJob = null
        try {
            socket?.close()
        } catch (_: Exception) {}
        socket = null
        onLocalAudioReceived = null
    }
}
