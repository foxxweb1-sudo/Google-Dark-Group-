package com.example.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.components.AnimatedSoundWave
import com.example.ui.components.ChatMessageBubble
import com.example.ui.components.CyberAvatar
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

val quickEmojis = listOf("😀", "😂", "🔥", "👑", "🎙️", "🎧", "❤️", "👏", "👍", "⚡", "🚀", "💯", "😎", "🤝", "✨", "🎉")

@Composable
fun ActiveRoomScreen(
    viewModel: CybeNodeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentRoom by viewModel.currentRoom.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val roomUsers by viewModel.roomUsers.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val roomEventBanner by viewModel.roomEventBanner.collectAsState()

    val isVoiceConnected by viewModel.voiceEngine.isConnected.collectAsState()
    val isMuted by viewModel.voiceEngine.isMuted.collectAsState()
    val isDeafened by viewModel.voiceEngine.isDeafened.collectAsState()
    val soundAmplitudes by viewModel.voiceEngine.soundWaveAmplitudes.collectAsState()
    val isAgoraEngineReady by viewModel.isAgoraEngineReady.collectAsState()

    var messageText by remember { mutableStateOf("") }
    var showEmojiBar by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    val isHost = currentUser?.id == currentRoom?.hostId

    BackHandler {
        viewModel.leaveRoom()
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.toggleVoiceConnection(isGranted)
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "roomPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "roomPulseAlpha"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBg)
    ) {
        // 1. UNIFIED SLEEK TOP HEADER BAR
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(CyberCard, CyberBg)
                    )
                )
                .border(1.dp, CyberBorder)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // START: Leave Button + Room Name & IDs
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Back / Leave Room Button
                    IconButton(
                        onClick = { viewModel.leaveRoom() },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyberCardElevated)
                            .border(1.dp, CyberBorder, RoundedCornerShape(10.dp))
                            .testTag("btn_leave_room")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "خروج من الغرفة",
                            tint = CyberTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Room Status Avatar/Icon
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyberPurple.copy(alpha = 0.15f))
                            .border(1.dp, CyberPurple.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Headset,
                            contentDescription = null,
                            tint = CyberPurpleLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = currentRoom?.name ?: "غرفة صوتية",
                                color = CyberTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            if (currentRoom?.isLocked == true) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "محمية",
                                    tint = CyberEmerald,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }

                        // ID Chips
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            // Copy Room ID
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(CyberCardElevated)
                                    .clickable {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Room Code", currentRoom?.id ?: ""))
                                        viewModel.showToast("تم نسخ الرمز: ${currentRoom?.id}")
                                    }
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "ID: ${currentRoom?.id ?: "0000"}",
                                    color = CyberCyan,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    tint = CyberCyan.copy(alpha = 0.7f),
                                    modifier = Modifier.size(9.dp)
                                )
                            }

                            // Voice Channel Badge
                            val voiceId = currentRoom?.voiceChannelId?.ifBlank { "dark_room_${currentRoom?.id}" } ?: "audio_global"
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(CyberPurple.copy(alpha = 0.2f))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = voiceId,
                                    color = CyberPurpleLight,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // END ACTIONS: Members Count, Delete (Host), Minimize
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Online Count Pill
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(CyberEmerald.copy(alpha = 0.12f))
                            .border(1.dp, CyberEmerald.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(CyberEmerald.copy(alpha = pulseAlpha))
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "${roomUsers.size} متواجد",
                            color = CyberEmerald,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Delete Room Button (Host only)
                    if (isHost) {
                        IconButton(
                            onClick = { showDeleteConfirmDialog = true },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyberRed.copy(alpha = 0.15f))
                                .border(1.dp, CyberRed.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteForever,
                                contentDescription = "حذف الغرفة",
                                tint = CyberRed,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Minimize Button
                    IconButton(
                        onClick = { viewModel.minimizeRoom() },
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberCardElevated)
                            .border(1.dp, CyberBorder, RoundedCornerShape(8.dp))
                            .testTag("btn_minimize_room")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = "تصغير",
                            tint = CyberTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // 2. FLOATING JOIN / LEAVE BANNER (شعار دخول وخروج المستخدمين)
        AnimatedVisibility(
            visible = roomEventBanner != null,
            enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.slideInVertically { -it },
            exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.slideOutVertically { -it }
        ) {
            val banner = roomEventBanner
            if (banner != null) {
                val isJoin = banner.type == com.example.data.model.RoomEventType.JOIN
                val borderColor = if (isJoin) CyberEmerald else CyberRed
                val bgColor = if (isJoin) CyberEmerald.copy(alpha = 0.18f) else CyberRed.copy(alpha = 0.18f)
                val iconTint = if (isJoin) CyberEmerald else CyberRed

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(bgColor)
                            .border(1.5.dp, borderColor.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, borderColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                CyberAvatar(banner.avatarUrl, banner.userName, 32.dp)
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (isJoin) "🟢 إشعار دخول:" else "🔴 إشعار خروج:",
                                        color = iconTint,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = banner.userName,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = banner.message,
                                    color = Color(0xFFE2E8F0),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Status Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(borderColor.copy(alpha = 0.3f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (isJoin) "انضم الآن 🎧" else "غادر الآن 🚪",
                                color = borderColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 3. BALANCED & HARMONIOUS VOICE CONSOLE (لوحة التحكم الصوتي المنظمة)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF0F172A), CyberCard)
                    )
                )
                .border(1.dp, CyberBorder)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Top Voice Status Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (isVoiceConnected) CyberEmerald else CyberTextMuted)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isVoiceConnected) "المحادثة الصوتية المباشرة (نشطة)" else "الصوت غير متصل",
                        color = if (isVoiceConnected) CyberEmerald else CyberTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (isVoiceConnected) {
                    AnimatedSoundWave(
                        amplitudes = soundAmplitudes,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // MAIN SYMMETRICAL VOICE CONTROL TOOLBAR (الأزرار المرتبة والمتناسقة)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. Primary Voice Enter/Leave Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isVoiceConnected) Brush.horizontalGradient(listOf(Color(0xFFDC2626), Color(0xFFEF4444)))
                            else Brush.horizontalGradient(listOf(Color(0xFF059669), Color(0xFF10B981)))
                        )
                        .clickable {
                            if (!isVoiceConnected) {
                                val granted = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED
                                if (granted) {
                                    viewModel.toggleVoiceConnection(true)
                                } else {
                                    micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            } else {
                                viewModel.toggleVoiceConnection(false)
                            }
                        }
                        .testTag("btn_toggle_voice"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhoneInTalk,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isVoiceConnected) "مغادرة الصوت" else "دخول الصوت المباشر",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                // 2. Mic Mute Toggle (Square 42dp)
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (!isVoiceConnected) CyberCardElevated
                            else if (isMuted) Color(0xFF7F1D1D)
                            else Color(0xFF064E3B)
                        )
                        .border(
                            1.dp,
                            if (!isVoiceConnected) CyberBorder
                            else if (isMuted) CyberRed
                            else CyberEmerald,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable(enabled = isVoiceConnected) {
                            viewModel.toggleMicrophone()
                        }
                        .testTag("btn_toggle_mic"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Mute",
                        tint = if (!isVoiceConnected) CyberTextMuted else if (isMuted) CyberRed else CyberEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // 3. Speaker Deafen Toggle (Square 42dp)
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (!isVoiceConnected) CyberCardElevated
                            else if (isDeafened) Color(0xFF7F1D1D)
                            else Color(0xFF0E7490)
                        )
                        .border(
                            1.dp,
                            if (!isVoiceConnected) CyberBorder
                            else if (isDeafened) CyberRed
                            else CyberCyan,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable(enabled = isVoiceConnected) {
                            viewModel.toggleAudioOutput()
                        }
                        .testTag("btn_toggle_deafen"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isDeafened) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                        contentDescription = "Deafen",
                        tint = if (!isVoiceConnected) CyberTextMuted else if (isDeafened) CyberRed else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Voice Participants Strip
            val voiceParticipants = roomUsers.filter { it.isVoice }
            if (voiceParticipants.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(voiceParticipants) { user ->
                        val isMe = user.id == currentUser?.id
                        val speaking = if (isMe) (!isMuted && viewModel.voiceEngine.isSpeaking.collectAsState().value) else user.isSpeaking

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(62.dp)
                        ) {
                            Box(contentAlignment = Alignment.BottomEnd) {
                                CyberAvatar(
                                    avatarUrl = user.avatarUrl,
                                    name = user.name,
                                    size = 42.dp,
                                    isSpeaking = speaking
                                )

                                Box(
                                    modifier = Modifier
                                        .size(15.dp)
                                        .clip(CircleShape)
                                        .background(if (if (isMe) isMuted else user.isMuted) CyberRed else CyberEmerald)
                                        .border(1.dp, CyberBg, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (if (isMe) isMuted else user.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(9.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(3.dp))

                            Text(
                                text = if (isMe) "أنت" else user.name,
                                color = if (isMe) CyberCyan else Color(0xFFCBD5E1),
                                fontSize = 10.sp,
                                fontWeight = if (isMe) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // 4. CHAT MESSAGES FEED (HIGH CONTRAST & CLEAR READABILITY)
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatMessageBubble(
                    msg = msg,
                    myId = currentUser?.id ?: "",
                    onCopyClick = {
                        viewModel.showToast("تم نسخ رسالة الغرفة بنجاح 📋", com.example.viewmodel.ToastType.SUCCESS)
                    }
                )
            }
        }

        // 5. QUICK EMOJI BAR
        AnimatedVisibility(visible = showEmojiBar) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0B1329))
                    .border(1.dp, CyberBorder)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(quickEmojis) { emoji ->
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberCardElevated)
                            .clickable {
                                messageText += emoji
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, fontSize = 17.sp)
                    }
                }
            }
        }

        // 6. SYMMETRICAL CHAT INPUT BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberCard)
                .border(1.dp, CyberBorder)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji Picker Toggle
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (showEmojiBar) CyberPurple.copy(alpha = 0.3f) else CyberCardElevated)
                    .border(1.dp, if (showEmojiBar) CyberPurpleLight else CyberBorder, RoundedCornerShape(12.dp))
                    .clickable { showEmojiBar = !showEmojiBar },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEmotions,
                    contentDescription = "رموز تعبيرية",
                    tint = if (showEmojiBar) CyberPurpleLight else Color(0xFFCBD5E1),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                placeholder = { Text("اكتب رسالتك في الغرفة...", fontSize = 13.sp, color = CyberTextMuted) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("input_chat_message"),
                colors = outlinedFieldColors(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (messageText.isNotBlank()) {
                            viewModel.sendMessage(messageText)
                            messageText = ""
                        }
                    }
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Send Button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF6D28D9), Color(0xFF8B5CF6))))
                    .clickable {
                        if (messageText.isNotBlank()) {
                            viewModel.sendMessage(messageText)
                            messageText = ""
                        }
                    }
                    .testTag("btn_send_chat"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "إرسال",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }

    // Delete Room Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = "حذف الغرفة نهائياً؟",
                    color = CyberTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "هل أنت متأكد من حذف هذه الغرفة؟ سيتم إغلاق الجلسة لجميع المتواجدين.",
                    color = CyberTextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        viewModel.deleteCurrentRoom()
                    }
                ) {
                    Text(text = "تأكيد الحذف", color = CyberRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(text = "إلغاء", color = CyberTextMuted)
                }
            },
            containerColor = CyberCard,
            shape = RoundedCornerShape(18.dp)
        )
    }
}
