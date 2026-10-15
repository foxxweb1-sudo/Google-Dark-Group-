package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PrivateChatMessage
import com.example.ui.components.CyberAvatarWithPresence
import com.example.ui.components.MessageStatusTick
import com.example.ui.components.OnlineStatusIndicator
import com.example.ui.components.formatLastSeen
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
import com.example.viewmodel.Screen
import com.example.viewmodel.ToastType

private val directQuickEmojis = listOf("😀", "😂", "🔥", "👑", "🎙️", "🎧", "❤️", "👏", "👍", "⚡", "🚀", "💯", "😎", "🤝", "✨", "🎉")

@Composable
fun DirectChatScreen(
    viewModel: CybeNodeViewModel,
    modifier: Modifier = Modifier
) {
    val activeUser by viewModel.activeDirectUser.collectAsState()
    val privateMessages by viewModel.privateMessages.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val friends by viewModel.friends.collectAsState()
    val isBlocked = friends.find { it.userId == activeUser?.id }?.isBlocked == true

    var messageText by remember { mutableStateOf("") }
    var showEmojiBar by remember { mutableStateOf(false) }
    var selectedMessageForActions by remember { mutableStateOf<PrivateChatMessage?>(null) }
    var showSelectionDialog by remember { mutableStateOf<PrivateChatMessage?>(null) }
    val listState = rememberLazyListState()
    val clipboardManager = LocalClipboardManager.current

    BackHandler {
        viewModel.navigateTo(Screen.PRIVATE_CHATS)
    }

    LaunchedEffect(privateMessages.size) {
        if (privateMessages.isNotEmpty()) {
            listState.animateScrollToItem(privateMessages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBg)
    ) {
        // TOP APP BAR WITH ONLINE PRESENCE
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberCard)
                .border(1.dp, CyberBorder)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        activeUser?.let { viewModel.viewUserProfile(it) }
                    }
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(Screen.PRIVATE_CHATS) },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CyberCardElevated)
                        .border(1.dp, CyberBorder, RoundedCornerShape(10.dp))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "رجوع",
                        tint = CyberCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                CyberAvatarWithPresence(
                    avatarUrl = activeUser?.avatarUrl,
                    name = activeUser?.name ?: "مستخدم",
                    isOnline = activeUser?.isOnline == true,
                    size = 42.dp
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = activeUser?.name ?: "محادثة خاصة",
                        color = CyberTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (activeUser?.isOnline == true) {
                            OnlineStatusIndicator(isOnline = true, size = 8.dp)
                            Text(
                                text = "متصل الآن",
                                color = CyberEmerald,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else {
                            Text(
                                text = formatLastSeen(activeUser?.lastActive ?: 0L, false),
                                color = CyberTextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            // Top action buttons: Voice Call & Block
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Voice Call Button
                IconButton(
                    onClick = {
                        activeUser?.let { viewModel.startVoiceCall(it) }
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CyberEmerald.copy(alpha = 0.18f))
                        .border(1.dp, CyberEmerald.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .testTag("btn_direct_call")
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "مكالمة صوتية",
                        tint = CyberEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Block User Button
                IconButton(
                    onClick = { viewModel.toggleBlockDirectUser() },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isBlocked) CyberRed else CyberCardElevated)
                        .border(1.dp, if (isBlocked) CyberRed else CyberBorder, RoundedCornerShape(10.dp))
                        .testTag("btn_direct_block")
                ) {
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = if (isBlocked) "إلغاء الحظر" else "حظر المستخدم",
                        tint = if (isBlocked) Color.White else CyberRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // MESSAGES TIMELINE
        if (privateMessages.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CyberAvatarWithPresence(
                        avatarUrl = activeUser?.avatarUrl,
                        name = activeUser?.name ?: "",
                        isOnline = activeUser?.isOnline == true,
                        size = 64.dp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "محادثة خاصة مع ${activeUser?.name}",
                        color = CyberTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "يمكنك إرسال رسائل نصية مع علامات القراءة، أو إجراء مكالمة صوتية مباشرة.",
                        color = CyberTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(privateMessages, key = { it.id }) { msg ->
                    PrivateMessageItem(
                        msg = msg,
                        isMe = msg.isMe || msg.senderId == currentUser?.id,
                        onMessageClick = {
                            selectedMessageForActions = msg
                        },
                        onCopyClick = {
                            clipboardManager.setText(AnnotatedString(msg.text))
                            viewModel.showToast("تم نسخ الرسالة إلى الحافظة بنجاح 📋", ToastType.SUCCESS)
                        }
                    )
                }
            }
        }

        // QUICK EMOJI BAR
        AnimatedVisibility(visible = showEmojiBar && !isBlocked) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberCardElevated)
                    .border(1.dp, CyberBorder)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(directQuickEmojis) { emoji ->
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberBg)
                            .clickable {
                                messageText += emoji
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, fontSize = 18.sp)
                    }
                }
            }
        }

        // CHAT INPUT BAR OR BLOCKED BANNER
        if (isBlocked) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberCard)
                    .border(1.dp, CyberBorder)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = null,
                        tint = CyberRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "هذا المستخدم محظور حالياً",
                        color = CyberRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(CyberRed)
                        .clickable { viewModel.toggleBlockDirectUser() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "إلغاء الحظر",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberCard)
                    .border(1.dp, CyberBorder)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Emoji toggle button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (showEmojiBar) CyberPurple.copy(alpha = 0.3f) else CyberCardElevated)
                        .border(1.dp, CyberBorder, RoundedCornerShape(12.dp))
                        .clickable { showEmojiBar = !showEmojiBar },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "😀", fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text("اكتب رسالة خاصة...", fontSize = 12.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_direct_message"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = CyberTextPrimary,
                        unfocusedTextColor = CyberTextPrimary,
                        focusedBorderColor = CyberPurple,
                        unfocusedBorderColor = CyberBorder,
                        focusedPlaceholderColor = CyberTextMuted,
                        unfocusedPlaceholderColor = CyberTextMuted
                    ),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (messageText.isNotBlank()) {
                                viewModel.sendDirectTextMessage(messageText)
                                messageText = ""
                            }
                        }
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CyberPurple)
                        .clickable {
                            if (messageText.isNotBlank()) {
                                viewModel.sendDirectTextMessage(messageText)
                                messageText = ""
                            }
                        }
                        .testTag("btn_send_direct_message"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "إرسال",
                        tint = Color.White,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }

    // Message Actions Dialog (Copy / Select / Delete)
    if (selectedMessageForActions != null) {
        val msg = selectedMessageForActions!!
        val isMe = msg.isMe || msg.senderId == currentUser?.id

        AlertDialog(
            onDismissRequest = { selectedMessageForActions = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "خيارات الرسالة",
                        color = CyberTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Preview message snippet
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyberBg)
                            .border(1.dp, CyberBorder, RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = msg.text,
                            color = CyberTextSecondary,
                            fontSize = 12.sp,
                            maxLines = 3
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // 1. Copy Message button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyberPurple.copy(alpha = 0.2f))
                            .border(1.dp, CyberPurple.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .clickable {
                                clipboardManager.setText(AnnotatedString(msg.text))
                                viewModel.showToast("تم نسخ الرسالة إلى الحافظة 📋", ToastType.SUCCESS)
                                selectedMessageForActions = null
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "نسخ",
                            tint = CyberPurpleLight,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "نسخ نص الرسالة",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    // 2. Select Text button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyberCyan.copy(alpha = 0.15f))
                            .border(1.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .clickable {
                                showSelectionDialog = msg
                                selectedMessageForActions = null
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.SelectAll,
                            contentDescription = "تحديد",
                            tint = CyberCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "تحديد واختيار جزء من النص",
                            color = CyberCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    // 3. Delete for Everyone (if my message)
                    if (isMe) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(CyberRed.copy(alpha = 0.15f))
                                .border(1.dp, CyberRed.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .clickable {
                                    viewModel.deletePrivateMessage(msg, forEveryone = true)
                                    selectedMessageForActions = null
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "حذف لدى الجميع",
                                tint = CyberRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "حذف لدى الجميع",
                                color = CyberRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    // 4. Delete for me
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyberCardElevated)
                            .border(1.dp, CyberBorder, RoundedCornerShape(10.dp))
                            .clickable {
                                viewModel.deletePrivateMessage(msg, forEveryone = false)
                                selectedMessageForActions = null
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "حذف لدي فقط",
                            tint = CyberTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "حذف لدي فقط",
                            color = CyberTextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { selectedMessageForActions = null }) {
                    Text(text = "إلغاء", color = CyberTextMuted)
                }
            },
            containerColor = CyberCard,
            shape = RoundedCornerShape(18.dp)
        )
    }

    // Text Selection Dialog
    if (showSelectionDialog != null) {
        val msg = showSelectionDialog!!
        AlertDialog(
            onDismissRequest = { showSelectionDialog = null },
            title = {
                Text(
                    text = "تحديد النص",
                    color = CyberTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "اضغط مطولاً على أي كلمة لتحديدها ونسخها بسهولة:",
                        color = CyberTextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CyberBg)
                            .border(1.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        SelectionContainer {
                            Text(
                                text = msg.text,
                                color = Color.White,
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(msg.text))
                        viewModel.showToast("تم نسخ كامل النص بنجاح 📋", ToastType.SUCCESS)
                        showSelectionDialog = null
                    }
                ) {
                    Text(text = "نسخ الكل", color = CyberCyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSelectionDialog = null }) {
                    Text(text = "إغلاق", color = CyberTextMuted)
                }
            },
            containerColor = CyberCard,
            shape = RoundedCornerShape(18.dp)
        )
    }
}

@Composable
fun PrivateMessageItem(
    msg: PrivateChatMessage,
    isMe: Boolean,
    onMessageClick: () -> Unit,
    onCopyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = if (isMe) "أنت" else msg.senderName,
                color = if (isMe) CyberCyan else CyberTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
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
                    RoundedCornerShape(16.dp)
                )
                .clickable { onMessageClick() }
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

                // Bubble footer: Quick Copy button + Timestamp + Delivery status ticks
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Quick 1-tap Copy action button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.35f))
                            .clickable { onCopyClick() }
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

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = msg.time,
                            color = if (isMe) Color(0xFFE0E7FF) else Color(0xFF94A3B8),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium
                        )

                        // Message Status Tick: ✓ (grey), ✓✓ (brown/grey), ✓✓ (blue)
                        if (isMe) {
                            MessageStatusTick(
                                isMe = true,
                                isDelivered = msg.isDelivered,
                                isRead = msg.isRead
                            )
                        }
                    }
                }
            }
        }
    }
}
