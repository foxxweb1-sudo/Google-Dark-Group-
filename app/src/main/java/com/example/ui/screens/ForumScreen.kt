package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.ui.components.ChatMessageBubble
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

private val forumTopics = listOf("الكل", "نقاش عام 💬", "غرف صوتية 🎙️", "تقنية وألعاب ⚡", "ترحيب وتعارف 🤝")
private val quickForumEmojis = listOf("🔥", "❤️", "👏", "😂", "⚡", "👑", "🎙️", "💯", "👍", "🤝", "🚀", "😎")

@Composable
fun ForumScreen(
    viewModel: CybeNodeViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val forumMessages by viewModel.forumMessages.collectAsState()
    var messageText by remember { mutableStateOf("") }
    var selectedTopic by remember { mutableStateOf("الكل") }
    var showEmojiBar by remember { mutableStateOf(false) }
    var messageToDelete by remember { mutableStateOf<ChatMessage?>(null) }
    val listState = rememberLazyListState()

    BackHandler {
        viewModel.navigateTo(Screen.DASHBOARD)
    }

    DisposableEffect(Unit) {
        viewModel.startForumSync()
        onDispose {
            viewModel.stopForumSync()
        }
    }

    LaunchedEffect(forumMessages.size) {
        if (forumMessages.isNotEmpty()) {
            listState.animateScrollToItem(forumMessages.size - 1)
        }
    }

    // Filter messages based on selected topic tag if any
    val displayedMessages = remember(forumMessages, selectedTopic) {
        if (selectedTopic == "الكل") {
            forumMessages
        } else {
            val tagKeyword = selectedTopic.substringBefore(" ").trim()
            forumMessages.filter { it.text.contains(tagKeyword, ignoreCase = true) }
        }
    }

    // Delete confirmation dialog
    if (messageToDelete != null) {
        AlertDialog(
            onDismissRequest = { messageToDelete = null },
            title = { Text("حذف الرسالة", color = CyberTextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("هل تريد بالتأكيد حذف هذه الرسالة من المنتدى العام؟", color = CyberTextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    messageToDelete?.let { viewModel.deleteForumMessage(it.id) }
                    messageToDelete = null
                }) {
                    Text("حذف", color = CyberRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { messageToDelete = null }) {
                    Text("إلغاء", color = CyberTextMuted)
                }
            },
            containerColor = CyberCard,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBg)
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberCard)
                .border(1.dp, CyberBorder)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(Screen.DASHBOARD) },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CyberCardElevated)
                        .border(1.dp, CyberBorder, RoundedCornerShape(12.dp))
                        .testTag("btn_forum_back")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "الرجوع للرئيسية",
                        tint = CyberCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CyberPurple.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Forum,
                        contentDescription = null,
                        tint = CyberPurpleLight,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "المنتدى العام",
                        color = CyberTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${forumMessages.size} مشاركة نشطة // مجتمع مفتوح",
                        color = CyberTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            // Actions: Refresh + Live status badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        viewModel.startForumSync()
                        viewModel.showToast("تم تحديث المنتدى 🔄", ToastType.INFO)
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "تحديث",
                        tint = CyberCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(CyberBg)
                        .border(1.dp, CyberBorder, RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(CyberEmerald)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "مباشر",
                        color = CyberEmerald,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Topic Filter Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberCardElevated)
                .border(1.dp, CyberBorder)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(forumTopics) { topic ->
                val isSelected = selectedTopic == topic
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) CyberPurple else CyberBg)
                        .border(
                            1.dp,
                            if (isSelected) CyberPurpleLight else CyberBorder,
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { selectedTopic = topic }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = topic,
                        color = if (isSelected) androidx.compose.ui.graphics.Color.White else CyberTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        // Messages List
        if (displayedMessages.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Forum,
                        contentDescription = null,
                        tint = CyberPurple.copy(alpha = 0.5f),
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "مرحباً بك في المنتدى العام!",
                        color = CyberTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "كن أول من ينشر فكرة أو يشارك موضوعاً مع الجميع.",
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
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(displayedMessages, key = { it.id }) { msg ->
                    val isMyMsg = msg.senderId == (currentUser?.id ?: "")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            ChatMessageBubble(
                                msg = msg,
                                myId = currentUser?.id ?: "",
                                onCopyClick = {
                                    viewModel.showToast("تم نسخ رسالة المنتدى بنجاح 📋", ToastType.SUCCESS)
                                }
                            )
                        }

                        // Allow author to delete own message
                        if (isMyMsg) {
                            IconButton(
                                onClick = { messageToDelete = msg },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "حذف الرسالة",
                                    tint = CyberRed.copy(alpha = 0.7f),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Emoji Picker Bar
        AnimatedVisibility(visible = showEmojiBar) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberCardElevated)
                    .border(1.dp, CyberBorder)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(quickForumEmojis) { emoji ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CyberBg)
                            .border(1.dp, CyberBorder, CircleShape)
                            .clickable {
                                messageText += emoji
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, fontSize = 16.sp)
                    }
                }
            }
        }

        // Input bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberCard)
                .border(1.dp, CyberBorder)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { showEmojiBar = !showEmojiBar },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEmotions,
                    contentDescription = "رموز تعبيرية",
                    tint = if (showEmojiBar) CyberPurpleLight else CyberTextSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                placeholder = { Text("اكتب مشاركتك في المنتدى العام...", fontSize = 12.sp) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("input_forum_message"),
                colors = outlinedFieldColors(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (messageText.isNotBlank()) {
                            viewModel.sendForumMessage(messageText)
                            messageText = ""
                        }
                    }
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(CyberPurple)
                    .clickable {
                        if (messageText.isNotBlank()) {
                            viewModel.sendForumMessage(messageText)
                            messageText = ""
                        }
                    }
                    .testTag("btn_send_forum_message"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "إرسال",
                    tint = androidx.compose.ui.graphics.Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
