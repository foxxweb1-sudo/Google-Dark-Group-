package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import com.example.data.model.Friend
import com.example.data.model.FriendRequest
import com.example.data.model.User
import com.example.ui.components.CyberAvatar
import com.example.ui.components.CyberAvatarWithPresence
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
fun FriendsScreen(
    viewModel: CybeNodeViewModel,
    modifier: Modifier = Modifier
) {
    val friends by viewModel.friends.collectAsState()
    val friendRequests by viewModel.friendRequests.collectAsState()
    val searchResult by viewModel.searchUserResult.collectAsState()
    val isSearching by viewModel.isSearchingUser.collectAsState()

    var activeTab by remember { mutableStateOf("friends") }
    var searchQuery by remember { mutableStateOf("") }

    BackHandler {
        viewModel.navigateTo(Screen.DASHBOARD)
    }

    LaunchedEffect(Unit) {
        viewModel.refreshFriendsAndRequests()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBg)
    ) {
        // TOP APP BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberCard)
                .border(1.dp, CyberBorder)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(Screen.DASHBOARD) },
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CyberCardElevated)
                    .border(1.dp, CyberBorder, RoundedCornerShape(12.dp))
                    .testTag("btn_friends_back")
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
                    .background(CyberCyan.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = null,
                    tint = CyberCyan,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = "الأصدقاء والتواصل",
                color = CyberTextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // TABS STRIP
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberCardElevated)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FriendTabButton(
                title = "الأصدقاء",
                icon = Icons.Default.Group,
                isSelected = activeTab == "friends",
                modifier = Modifier.weight(1f),
                onClick = { activeTab = "friends" }
            )

            FriendTabButton(
                title = "الطلبات",
                icon = Icons.Default.Mail,
                isSelected = activeTab == "requests",
                badgeCount = friendRequests.size,
                modifier = Modifier.weight(1f),
                onClick = { activeTab = "requests" }
            )

            FriendTabButton(
                title = "بحث",
                icon = Icons.Default.Search,
                isSelected = activeTab == "search",
                modifier = Modifier.weight(0.9f),
                onClick = { activeTab = "search" }
            )
        }

        // TAB CONTENTS
        when (activeTab) {
            "friends" -> {
                if (friends.isEmpty()) {
                    EmptyFriendsPlaceholder(
                        title = "لا يوجد أصدقاء مضافين حتى الآن",
                        desc = "انتقل إلى تبويب \"بحث\" للبحث عن أصدقائك عبر اسم المستخدم وإضافتهم.",
                        onAction = { activeTab = "search" }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(friends, key = { it.userId }) { friend ->
                            FriendItemCard(
                                friend = friend,
                                onCallClick = {
                                    viewModel.startVoiceCall(
                                        User(
                                            id = friend.userId,
                                            username = friend.username,
                                            name = friend.name,
                                            avatarUrl = friend.avatarUrl
                                        )
                                    )
                                },
                                onChatClick = {
                                    viewModel.openDirectChat(
                                        User(
                                            id = friend.userId,
                                            username = friend.username,
                                            name = friend.name,
                                            avatarUrl = friend.avatarUrl
                                        )
                                    )
                                },
                                onProfileClick = {
                                    viewModel.viewUserProfile(
                                        User(
                                            id = friend.userId,
                                            username = friend.username,
                                            name = friend.name,
                                            avatarUrl = friend.avatarUrl
                                        )
                                    )
                                },
                                onToggleBlock = { viewModel.toggleBlockFriend(friend) }
                            )
                        }
                    }
                }
            }

            "requests" -> {
                if (friendRequests.isEmpty()) {
                    EmptyFriendsPlaceholder(
                        title = "لا توجد طلبات صداقة معلقة",
                        desc = "عندما يرسل لك شخص طلب صداقة سيظهر هنا مباشرة لتأكيده.",
                        onAction = null
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(friendRequests, key = { it.id }) { req ->
                            FriendRequestItemCard(
                                request = req,
                                onAccept = { viewModel.respondToFriendRequest(req, true) },
                                onReject = { viewModel.respondToFriendRequest(req, false) },
                                onProfileClick = {
                                    viewModel.viewUserProfile(
                                        User(
                                            id = req.fromUserId,
                                            username = req.fromUsername,
                                            name = req.fromName,
                                            avatarUrl = req.fromAvatarUrl
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
            }

            "search" -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Search field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("ابحث عن طريق اسم المستخدم...", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = CyberCyan)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.searchUser(searchQuery) }) {
                                    Icon(Icons.Default.Search, contentDescription = "بحث", tint = CyberCyan)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_search_user"),
                        colors = outlinedFieldColors(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { viewModel.searchUser(searchQuery) })
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isSearching) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = CyberCyan)
                        }
                    } else if (searchResult != null) {
                        val user = searchResult!!
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(CyberCard)
                                .border(1.dp, CyberBorder, RoundedCornerShape(18.dp))
                                .padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { viewModel.viewUserProfile(user) }
                                ) {
                                    CyberAvatar(
                                        avatarUrl = user.avatarUrl,
                                        name = user.name,
                                        size = 50.dp
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = user.name,
                                            color = CyberTextPrimary,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "@${user.username}",
                                            color = CyberCyan,
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(CyberPurple)
                                        .clickable { viewModel.sendFriendRequestTo(user) }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                        .testTag("btn_send_friend_req"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.PersonAdd,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "إضافة صديق",
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "اكتب اسم المستخدم واضغط على بحث لإيجاد أي شخص على الشبكة.",
                                color = CyberTextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FriendItemCard(
    friend: Friend,
    onCallClick: () -> Unit,
    onChatClick: () -> Unit,
    onProfileClick: () -> Unit,
    onToggleBlock: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CyberCard)
            .border(1.dp, CyberBorder, RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onProfileClick() }
            ) {
                CyberAvatarWithPresence(
                    avatarUrl = friend.avatarUrl,
                    name = friend.name,
                    isOnline = friend.isOnline,
                    size = 46.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = friend.name,
                            color = CyberTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (friend.isBlocked) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "محظور",
                                color = CyberRed,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = if (friend.isOnline) "متصل الآن 🟢" else formatLastSeen(friend.lastActive, false),
                        color = if (friend.isOnline) CyberEmerald else CyberTextMuted,
                        fontSize = 10.sp,
                        fontWeight = if (friend.isOnline) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Voice Call button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CyberEmerald.copy(alpha = 0.2f))
                        .border(1.dp, CyberEmerald.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .clickable { onCallClick() }
                        .testTag("btn_call_friend_${friend.userId}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "اتصال صوتي",
                        tint = CyberEmerald,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Direct Chat button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CyberPurple.copy(alpha = 0.2f))
                        .border(1.dp, CyberPurple.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .clickable { onChatClick() }
                        .testTag("btn_chat_friend_${friend.userId}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = "محادثة خاصة",
                        tint = CyberPurpleLight,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Profile button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CyberCardElevated)
                        .border(1.dp, CyberBorder, RoundedCornerShape(10.dp))
                        .clickable { onProfileClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "عرض البروفايل",
                        tint = CyberCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Block/Unblock button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CyberCardElevated)
                        .border(1.dp, CyberBorder, RoundedCornerShape(10.dp))
                        .clickable { onToggleBlock() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = "حظر",
                        tint = if (friend.isBlocked) CyberRed else CyberTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FriendRequestItemCard(
    request: FriendRequest,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onProfileClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CyberCard)
            .border(1.dp, CyberBorder, RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onProfileClick() }
            ) {
                CyberAvatar(
                    avatarUrl = request.fromAvatarUrl,
                    name = request.fromName,
                    size = 46.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = request.fromName,
                        color = CyberTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "@${request.fromUsername}",
                        color = CyberCyan,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Accept
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CyberEmerald)
                        .clickable { onAccept() }
                        .testTag("btn_accept_req"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "قبول",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Reject
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CyberCardElevated)
                        .border(1.dp, CyberBorder, RoundedCornerShape(10.dp))
                        .clickable { onReject() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "رفض",
                        tint = CyberRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FriendTabButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    badgeCount: Int = 0,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) CyberPurple else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else CyberTextMuted,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                color = if (isSelected) Color.White else CyberTextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun EmptyFriendsPlaceholder(
    title: String,
    desc: String,
    onAction: (() -> Unit)?
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(CyberCardElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = null,
                    tint = CyberCyan,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                color = CyberTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = desc,
                color = CyberTextSecondary,
                fontSize = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 18.sp
            )
            if (onAction != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(CyberPurple)
                        .clickable { onAction() }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "البحث عن أصدقاء الآن",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
