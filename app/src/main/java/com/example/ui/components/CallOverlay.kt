package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberBg
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCard
import com.example.ui.theme.CyberCardElevated
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.CyberPurpleLight
import com.example.ui.theme.CyberRed
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.viewmodel.CallUiState
import com.example.viewmodel.CybeNodeViewModel

@Composable
fun CallOverlay(
    viewModel: CybeNodeViewModel,
    modifier: Modifier = Modifier
) {
    val callState by viewModel.callUiState.collectAsState()
    val session by viewModel.activeCallSession.collectAsState()
    val durationSec by viewModel.callDurationSec.collectAsState()
    val isMuted by viewModel.voiceEngine.isMuted.collectAsState()
    val isDeafened by viewModel.voiceEngine.isDeafened.collectAsState()
    val amplitudes by viewModel.voiceEngine.soundWaveAmplitudes.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "callRingingPulse")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "callPulse"
    )

    AnimatedVisibility(
        visible = callState != CallUiState.IDLE,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xF0040711)),
            contentAlignment = Alignment.Center
        ) {
            when (callState) {
                CallUiState.OUTGOING_RINGING -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Box(
                                modifier = Modifier
                                    .size(130.dp)
                                    .scale(pulseGlow)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(CyberPurple.copy(alpha = 0.5f), Color.Transparent)
                                        )
                                    )
                            )
                            CyberAvatar(
                                avatarUrl = session?.receiverAvatar,
                                name = session?.receiverName ?: "مستخدم",
                                size = 96.dp
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = session?.receiverName ?: "مستخدم",
                            color = CyberTextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "@${session?.receiverUsername ?: ""}",
                            color = CyberCyan,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(CyberEmerald)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "جاري الاتصال والرنين...",
                                color = CyberTextSecondary,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(48.dp))

                        // End Call Button
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(CyberRed)
                                .clickable { viewModel.endVoiceCall() }
                                .testTag("btn_end_call_outgoing"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CallEnd,
                                contentDescription = "إنهاء الاتصال",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "إلغاء", color = CyberTextSecondary, fontSize = 12.sp)
                    }
                }

                CallUiState.INCOMING_RINGING -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Box(
                                modifier = Modifier
                                    .size(130.dp)
                                    .scale(pulseGlow)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(CyberEmerald.copy(alpha = 0.5f), Color.Transparent)
                                        )
                                    )
                            )
                            CyberAvatar(
                                avatarUrl = session?.callerAvatar,
                                name = session?.callerName ?: "مستخدم",
                                size = 96.dp
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = session?.callerName ?: "مستخدم",
                            color = CyberTextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "@${session?.callerUsername ?: ""}",
                            color = CyberCyan,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "مكالمة صوتية واردة 📞",
                            color = CyberEmerald,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(48.dp))

                        // Action Buttons: Accept & Decline
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(40.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Decline
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(CyberRed)
                                        .clickable { viewModel.declineIncomingCall() }
                                        .testTag("btn_decline_call"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CallEnd,
                                        contentDescription = "رفض",
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = "رفض", color = CyberTextSecondary, fontSize = 12.sp)
                            }

                            // Accept
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(CyberEmerald)
                                        .clickable { viewModel.acceptIncomingCall() }
                                        .testTag("btn_accept_call"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Call,
                                        contentDescription = "قبول المكالمة",
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = "قبول", color = CyberEmerald, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                CallUiState.CONNECTED -> {
                    val otherName = if (session?.callerId == viewModel.currentUser.collectAsState().value?.id) {
                        session?.receiverName
                    } else {
                        session?.callerName
                    } ?: "مستخدم"

                    val otherAvatar = if (session?.callerId == viewModel.currentUser.collectAsState().value?.id) {
                        session?.receiverAvatar
                    } else {
                        session?.callerAvatar
                    }

                    val mins = durationSec / 60
                    val secs = durationSec % 60
                    val timerStr = String.format("%02d:%02d", mins, secs)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        CyberAvatar(
                            avatarUrl = otherAvatar,
                            name = otherName,
                            size = 100.dp,
                            isSpeaking = amplitudes.any { it > 0.35f }
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = otherName,
                            color = CyberTextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = timerStr,
                            color = CyberCyan,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Soundwaves
                        AnimatedSoundWave(
                            amplitudes = amplitudes,
                            color = CyberEmerald,
                            height = 20.dp
                        )

                        val isAgoraEngineReady by viewModel.isAgoraEngineReady.collectAsState()
                        if (!isAgoraEngineReady) {
                            Text(
                                text = "⚠️ خادم الاتصال معطل (خطأ 101). تم تنشيط الاتصال الاحتياطي تلقائياً.",
                                color = CyberRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 10.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(48.dp))

                        // Controls: Mute, End Call, Speaker
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Mute toggle
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(if (isMuted) CyberRed.copy(alpha = 0.2f) else CyberCardElevated)
                                    .border(1.dp, if (isMuted) CyberRed else CyberBorder, CircleShape)
                                    .clickable { viewModel.toggleMicrophone() }
                                    .testTag("btn_call_mute_toggle"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                    contentDescription = "كتم الصوت",
                                    tint = if (isMuted) CyberRed else CyberTextPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            // End call button
                            Box(
                                modifier = Modifier
                                    .size(66.dp)
                                    .clip(CircleShape)
                                    .background(CyberRed)
                                    .clickable { viewModel.endVoiceCall() }
                                    .testTag("btn_call_end_connected"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CallEnd,
                                    contentDescription = "إنهاء المكالمة",
                                    tint = Color.White,
                                    modifier = Modifier.size(30.dp)
                                )
                            }

                            // Speaker toggle
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(if (isDeafened) CyberRed.copy(alpha = 0.2f) else CyberCardElevated)
                                    .border(1.dp, if (isDeafened) CyberRed else CyberBorder, CircleShape)
                                    .clickable { viewModel.toggleAudioOutput() }
                                    .testTag("btn_call_speaker_toggle"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isDeafened) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                                    contentDescription = "مكبر الصوت",
                                    tint = if (isDeafened) CyberRed else CyberTextPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }

                CallUiState.IDLE -> {}
            }
        }
    }
}
