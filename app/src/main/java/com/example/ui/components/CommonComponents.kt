package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.CyberBg
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.CyberRed
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.widthIn
import com.example.data.model.ChatMessage
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.ui.theme.CyberCard
import com.example.ui.theme.CyberCardElevated
import com.example.ui.theme.CyberPurpleLight
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.viewmodel.ToastEvent
import com.example.viewmodel.ToastType

@Composable
fun CyberGridBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBg)
    ) {
        // Draw subtle cyber grid
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridSpacing = 40.dp.toPx()
            val lineColor = Color(0x157C3AED)
            val strokeWidth = 1.dp.toPx()

            var x = 0f
            while (x < size.width) {
                drawLine(
                    color = lineColor,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = strokeWidth
                )
                x += gridSpacing
            }

            var y = 0f
            while (y < size.height) {
                drawLine(
                    color = lineColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = strokeWidth
                )
                y += gridSpacing
            }

            // Ambient top-right glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x307C3AED), Color.Transparent),
                    center = Offset(size.width, 0f),
                    radius = size.width * 0.7f
                )
            )

            // Ambient bottom-left glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x2506B6D4), Color.Transparent),
                    center = Offset(0f, size.height),
                    radius = size.width * 0.7f
                )
            )
        }

        content()
    }
}

@Composable
fun CyberAvatar(
    avatarUrl: String?,
    name: String,
    size: Dp = 48.dp,
    isSpeaking: Boolean = false,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "avatarPulse")
    val pulseBorderWidth by infiniteTransition.animateFloat(
        initialValue = 1.5f,
        targetValue = 3.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "borderWidth"
    )

    val borderModifier = if (isSpeaking) {
        Modifier.border(
            width = pulseBorderWidth.dp,
            brush = Brush.linearGradient(listOf(CyberEmerald, CyberCyan)),
            shape = RoundedCornerShape(size * 0.32f)
        )
    } else {
        Modifier.border(
            width = 1.dp,
            color = CyberBorder,
            shape = RoundedCornerShape(size * 0.32f)
        )
    }

    Box(
        modifier = modifier
            .size(size)
            .then(borderModifier)
            .clip(RoundedCornerShape(size * 0.32f))
            .background(
                Brush.linearGradient(
                    listOf(
                        CyberPurple,
                        CyberCyan
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        if (!avatarUrl.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(avatarUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            val initial = name.trim().firstOrNull()?.uppercase() ?: "C"
            Text(
                text = initial,
                color = Color.White,
                fontSize = (size.value * 0.42f).sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun AnimatedSoundWave(
    amplitudes: List<Float> = listOf(0.3f, 0.7f, 0.4f),
    color: Color = CyberEmerald,
    height: Dp = 14.dp,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.height(height),
        verticalAlignment = Alignment.CenterVertically
    ) {
        amplitudes.forEach { amp ->
            val barHeight = (height * amp.coerceIn(0.2f, 1f))
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(barHeight)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
            Spacer(modifier = Modifier.width(2.dp))
        }
    }
}

@Composable
fun ToastBanner(
    toast: ToastEvent?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = toast != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        if (toast != null) {
            val (bgColor, borderColor, icon) = when (toast.type) {
                ToastType.ERROR -> Triple(Color(0xE6450A0A), CyberRed, Icons.Default.Error)
                ToastType.SUCCESS -> Triple(Color(0xE6064E3B), CyberEmerald, Icons.Default.CheckCircle)
                ToastType.INFO -> Triple(Color(0xE61E1B4B), CyberPurple, Icons.Default.Info)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(bgColor)
                    .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                    .clickable { onDismiss() }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .testTag("toast_banner")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = borderColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = toast.message,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// LAST SEEN & ONLINE STATUS FORMATTER
fun formatLastSeen(lastActive: Long, isOnline: Boolean): String {
    if (isOnline) return "متصل الآن 🟢"
    if (lastActive <= 0L) return "غير متصل"

    val diff = System.currentTimeMillis() - lastActive
    val minutes = diff / (60 * 1000)
    val hours = diff / (60 * 60 * 1000)
    val days = diff / (24 * 60 * 60 * 1000)

    return when {
        diff < 60_000 -> "متصل الآن 🟢"
        minutes < 60 -> "آخر ظهور منذ $minutes دقيقة"
        hours < 24 -> {
            val timeFmt = SimpleDateFormat("hh:mm a", Locale("ar"))
            "آخر ظهور اليوم ${timeFmt.format(Date(lastActive))}"
        }
        days < 2 -> {
            val timeFmt = SimpleDateFormat("hh:mm a", Locale("ar"))
            "آخر ظهور أمس ${timeFmt.format(Date(lastActive))}"
        }
        else -> {
            val dateFmt = SimpleDateFormat("dd/MM/yyyy", Locale("ar"))
            "آخر ظهور في ${dateFmt.format(Date(lastActive))}"
        }
    }
}

// MESSAGE STATUS TICKS (صح واحدة فاصل نت / صحين بني وصل ومش شافه / صحين زرقاء شافه)
@Composable
fun MessageStatusTick(
    isMe: Boolean,
    isDelivered: Boolean,
    isRead: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isMe) return

    val (iconVector, iconTint, desc) = when {
        isRead -> Triple(
            Icons.Default.DoneAll,
            Color(0xFF38BDF8), // Cyan / Sky Blue: شافها وقرأها
            "تمت القراءة"
        )
        isDelivered -> Triple(
            Icons.Default.DoneAll,
            Color(0xFFBCAAA4), // Warm Muted Brown/Grey: فاتح نت ووصلت لكن لم يقرأها
            "تم التسليم"
        )
        else -> Triple(
            Icons.Default.Done,
            CyberTextMuted, // Single Check / Grey: فاصل نت ولم تسلم بعد
            "تم الإرسال"
        )
    }

    Icon(
        imageVector = iconVector,
        contentDescription = desc,
        tint = iconTint,
        modifier = modifier.size(14.dp)
    )
}

// ONLINE PULSE STATUS INDICATOR DOT
@Composable
fun OnlineStatusIndicator(
    isOnline: Boolean,
    size: Dp = 10.dp,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "onlinePulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                if (isOnline) CyberEmerald.copy(alpha = pulseAlpha)
                else Color(0xFF6B7280)
            )
            .border(
                1.5.dp,
                CyberBg,
                CircleShape
            )
    )
}

// CYBER AVATAR WITH ONLINE PRESENCE BADGE
@Composable
fun CyberAvatarWithPresence(
    avatarUrl: String?,
    name: String,
    isOnline: Boolean,
    size: Dp = 48.dp,
    isSpeaking: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        contentAlignment = Alignment.BottomEnd
    ) {
        CyberAvatar(
            avatarUrl = avatarUrl,
            name = name,
            size = size,
            isSpeaking = isSpeaking
        )
        OnlineStatusIndicator(
            isOnline = isOnline,
            size = (size.value * 0.28f).coerceIn(9f, 16f).dp,
            modifier = Modifier.padding(1.dp)
        )
    }
}

@Composable
fun ChatMessageBubble(
    msg: ChatMessage,
    myId: String,
    onCopyClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isMe = msg.isMe || (myId.isNotBlank() && msg.senderId == myId)
    val clipboardManager = LocalClipboardManager.current

    if (msg.isSystem) {
        val isJoin = msg.eventType == com.example.data.model.RoomEventType.JOIN || msg.text.contains("انضم") || msg.text.contains("تم فتح")
        val isLeave = msg.eventType == com.example.data.model.RoomEventType.LEAVE || msg.text.contains("غادر")
        val tintColor = when {
            isJoin -> CyberEmerald
            isLeave -> CyberRed
            else -> CyberCyan
        }
        val bgColor = when {
            isJoin -> CyberEmerald.copy(alpha = 0.12f)
            isLeave -> CyberRed.copy(alpha = 0.12f)
            else -> CyberCardElevated
        }
        val iconText = when {
            isJoin -> "🟢"
            isLeave -> "🔴"
            else -> "⚡"
        }

        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(bgColor)
                    .border(1.dp, tintColor.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = iconText,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(end = 6.dp)
                )
                if (msg.avatarUrl != null) {
                    CyberAvatar(avatarUrl = msg.avatarUrl, name = msg.senderName, size = 18.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = msg.text,
                    color = tintColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        return
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        if (!isMe) {
            CyberAvatar(
                avatarUrl = msg.avatarUrl,
                name = msg.senderName,
                size = 34.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            if (!isMe) {
                Text(
                    text = msg.senderName,
                    color = Color(0xFF38BDF8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(bottom = 3.dp, start = 4.dp)
                )
            }

            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isMe) 16.dp else 4.dp,
                            bottomEnd = if (isMe) 4.dp else 16.dp
                        )
                    )
                    .background(
                        if (isMe) Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF7C3AED)))
                        else Brush.linearGradient(listOf(Color(0xFF1E293B), Color(0xFF192238)))
                    )
                    .border(
                        1.dp,
                        if (isMe) Color(0xFFA78BFA).copy(alpha = 0.6f) else Color(0xFF3B4E7A),
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isMe) 16.dp else 4.dp,
                            bottomEnd = if (isMe) 4.dp else 16.dp
                        )
                    )
                    .clickable {
                        clipboardManager.setText(AnnotatedString(msg.text))
                        onCopyClick?.invoke(msg.text)
                    }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Column {
                    Text(
                        text = msg.text,
                        color = Color.White,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Black.copy(alpha = 0.35f))
                                .clickable {
                                    clipboardManager.setText(AnnotatedString(msg.text))
                                    onCopyClick?.invoke(msg.text)
                                }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "نسخ",
                                tint = if (isMe) Color(0xFFE2E8F0) else Color(0xFF94A3B8),
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "نسخ",
                                color = if (isMe) Color(0xFFE2E8F0) else Color(0xFF94A3B8),
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = msg.time,
                            color = if (isMe) Color(0xFFE0E7FF) else Color(0xFF94A3B8),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        if (isMe) {
            Spacer(modifier = Modifier.width(8.dp))
            CyberAvatar(
                avatarUrl = msg.avatarUrl,
                name = msg.senderName,
                size = 34.dp
            )
        }
    }
}

