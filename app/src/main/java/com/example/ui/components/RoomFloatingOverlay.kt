package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
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
import com.example.viewmodel.CybeNodeViewModel

@Composable
fun RoomFloatingOverlay(
    viewModel: CybeNodeViewModel,
    modifier: Modifier = Modifier
) {
    val isMinimized by viewModel.isRoomMinimized.collectAsState()
    val currentRoom by viewModel.currentRoom.collectAsState()
    val isMuted by viewModel.voiceEngine.isMuted.collectAsState()
    val isDeafened by viewModel.voiceEngine.isDeafened.collectAsState()
    val roomUsers by viewModel.roomUsers.collectAsState()

    AnimatedVisibility(
        visible = isMinimized && currentRoom != null,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                CyberCard.copy(alpha = 0.95f),
                                CyberCardElevated.copy(alpha = 0.95f)
                            )
                        )
                    )
                    .border(1.dp, CyberPurple.copy(alpha = 0.8f), RoundedCornerShape(18.dp))
                    .clickable { viewModel.maximizeRoom() }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left side: Active state and room details
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberPurple.copy(alpha = 0.15f))
                            .border(1.dp, CyberPurple.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        OnlineStatusIndicator(isOnline = true, size = 10.dp)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = currentRoom?.name ?: "الغرفة الصوتية",
                            color = CyberTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "شغالة بالخلفية • ${roomUsers.size} متصل",
                            color = CyberEmerald,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Right side: Audio Quick Controls (Mute Mic, Deafen, Leave, Maximize)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // 1. Mic mute
                    IconButton(
                        onClick = { viewModel.toggleMicrophone() },
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (isMuted) CyberRed.copy(alpha = 0.2f) else CyberCardElevated)
                            .border(
                                1.dp,
                                if (isMuted) CyberRed.copy(alpha = 0.5f) else CyberBorder,
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "كتم المايك",
                            tint = if (isMuted) CyberRed else CyberCyan,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // 2. Sound deafen
                    IconButton(
                        onClick = { viewModel.toggleAudioOutput() },
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (isDeafened) CyberRed.copy(alpha = 0.2f) else CyberCardElevated)
                            .border(
                                1.dp,
                                if (isDeafened) CyberRed.copy(alpha = 0.5f) else CyberBorder,
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = if (isDeafened) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                            contentDescription = "كتم الصوت",
                            tint = if (isDeafened) CyberRed else CyberCyan,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // 3. Leave Room
                    IconButton(
                        onClick = { viewModel.leaveRoom() },
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(CyberRed.copy(alpha = 0.15f))
                            .border(1.dp, CyberRed.copy(alpha = 0.4f), CircleShape)
                            .testTag("btn_overlay_leave")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "مغادرة الغرفة",
                            tint = CyberRed,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // 4. Maximize Room
                    IconButton(
                        onClick = { viewModel.maximizeRoom() },
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(CyberPurple.copy(alpha = 0.15f))
                            .border(1.dp, CyberPurple.copy(alpha = 0.4f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInFull,
                            contentDescription = "تكبير الغرفة",
                            tint = CyberPurpleLight,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}
