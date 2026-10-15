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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PrivateConversation
import com.example.data.model.User
import com.example.ui.components.CyberAvatar
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

@Composable
fun PrivateConversationsScreen(
    viewModel: CybeNodeViewModel,
    modifier: Modifier = Modifier
) {
    val conversations by viewModel.conversations.collectAsState()
    val friends by viewModel.friends.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showNewChatDialog by remember { mutableStateOf(false) }

    BackHandler {
        viewModel.navigateTo(Screen.DASHBOARD)
    }

    LaunchedEffect(Unit) {
        viewModel.refreshConversations()
        viewModel.refreshFriendsAndRequests()
    }

    val filteredConversations = remember(conversations, searchQuery) {
        if (searchQuery.isBlank()) conversations
        else conversations.filter {
            it.otherName.contains(searchQuery, ignoreCase = true) ||
            it.otherUsername.contains(searchQuery, ignoreCase = true) ||
            it.lastMessage.contains(searchQuery, ignoreCase = true)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CyberBg)
        ) {
            // TOP BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberCard)
                    .border(1.dp, CyberBorder)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.DASHBOARD) },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CyberCardElevated)
                            .border(1.dp, CyberBorder, RoundedCornerShape(12.dp))
                            .testTag("btn_conversations_back")
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
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = null,
                            tint = CyberPurpleLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "الرسائل الخاصة",
                            color = CyberTextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${conversations.size} محادثة نشطة",
                            color = CyberTextMuted,
                            fontSize = 10.sp
                        )
                    }
                }

                // New Chat action button
                IconButton(
                    onClick = { showNewChatDialog = true },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CyberPurple)
                        .testTag("btn_new_chat_top")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "محادثة جديدة",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // SEARCH BAR
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("بحث في الرسائل والمحادثات...", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "مسح", tint = CyberTextMuted, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_search_conversations"),
                    colors = outlinedFieldColors(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )
            }

            // CONVERSATIONS LIST
            if (filteredConversations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(CyberCardElevated)
                                .border(1.dp, CyberBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Chat,
                                contentDescription = null,
                                tint = CyberPurpleLight,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (searchQuery.isNotEmpty()) "لا توجد نتائج مطابقة لبحثك" else "لا توجد رسائل خاصة بعد",
                            color = CyberTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (searchQuery.isNotEmpty()) "جرب البحث باسم مستخدم أو كلمة أخرى" else "ابدأ محادثة خاصة وسرية مع أحد أصدقائك الآن",
                            color = CyberTextSecondary,
                            fontSize = 12.sp
                        )

                        if (searchQuery.isEmpty()) {
                            Spacer(modifier = Modifier.height(18.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(CyberPurple)
                                    .clickable { showNewChatDialog = true }
                                    .padding(horizontal = 18.dp, vertical = 10.dp)
                                    .testTag("btn_start_new_chat_empty"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "بدء محادثة جديدة",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredConversations, key = { it.id }) { conv ->
                        ConversationItemCard(
                            conversation = conv,
                            onClick = {
                                val targetUser = User(
                                    id = conv.otherUserId,
                                    username = conv.otherUsername,
                                    name = conv.otherName,
                                    avatarUrl = conv.otherAvatarUrl
                                )
                                viewModel.openDirectChat(targetUser)
                            }
                        )
                    }
                }
            }
        }
    }

    // DIALOG: PICK FRIEND TO START NEW CHAT
    if (showNewChatDialog) {
        AlertDialog(
            onDismissRequest = { showNewChatDialog = false },
            title = {
                Text(
                    text = "بدء محادثة خاصة جديدة",
                    color = CyberTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                if (friends.isEmpty()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "ليس لديك أصدقاء مضافون حالياً. يمكنك الذهاب لصفحة الأصدقاء والبحث عن مستخدم لإضافته.",
                            color = CyberTextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(friends) { friend ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(CyberCardElevated)
                                    .border(1.dp, CyberBorder, RoundedCornerShape(12.dp))
                                    .clickable {
                                        showNewChatDialog = false
                                        val targetUser = User(
                                            id = friend.userId,
                                            username = friend.username,
                                            name = friend.name,
                                            avatarUrl = friend.avatarUrl
                                        )
                                        viewModel.openDirectChat(targetUser)
                                    }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CyberAvatar(
                                    avatarUrl = friend.avatarUrl,
                                    name = friend.name,
                                    size = 40.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = friend.name,
                                        color = CyberTextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "@${friend.username}",
                                        color = CyberCyan,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Chat,
                                    contentDescription = "محادثة",
                                    tint = CyberPurpleLight,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNewChatDialog = false }) {
                    Text("إغلاق", color = CyberCyan)
                }
            },
            dismissButton = {
                if (friends.isEmpty()) {
                    TextButton(onClick = {
                        showNewChatDialog = false
                        viewModel.navigateTo(Screen.FRIENDS)
                    }) {
                        Text("إضافة أصدقاء", color = CyberPurpleLight)
                    }
                }
            },
            containerColor = CyberCard,
            shape = RoundedCornerShape(18.dp)
        )
    }
}

@Composable
fun ConversationItemCard(
    conversation: PrivateConversation,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CyberCard)
            .border(1.dp, CyberBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(12.dp)
            .testTag("conversation_item_${conversation.otherUserId}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CyberAvatarWithPresence(
            avatarUrl = conversation.otherAvatarUrl,
            name = conversation.otherName,
            isOnline = conversation.isOtherOnline,
            size = 48.dp
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = conversation.otherName,
                        color = CyberTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    if (conversation.isOtherOnline) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "🟢",
                            fontSize = 8.sp
                        )
                    }
                }

                if (conversation.lastMessageTime.isNotEmpty()) {
                    Text(
                        text = conversation.lastMessageTime,
                        color = CyberTextMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (conversation.lastMessageIsMe) {
                        MessageStatusTick(
                            isMe = true,
                            isDelivered = conversation.lastMessageIsDelivered,
                            isRead = conversation.lastMessageIsRead
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    Text(
                        text = if (conversation.lastMessage.isNotEmpty()) conversation.lastMessage else "بدء المحادثة...",
                        color = if (conversation.lastMessage.isNotEmpty()) CyberTextSecondary else CyberCyan,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }

                if (conversation.isBlocked) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CyberRed.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "محظور",
                            color = CyberRed,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
