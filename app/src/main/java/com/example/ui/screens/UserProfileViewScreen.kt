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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.PersonAdd
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.viewmodel.CybeNodeViewModel
import com.example.viewmodel.Screen

@Composable
fun UserProfileViewScreen(
    viewModel: CybeNodeViewModel,
    modifier: Modifier = Modifier
) {
    val selectedUser by viewModel.selectedUserProfile.collectAsState()
    val friends by viewModel.friends.collectAsState()
    val friendsRooms by viewModel.friendsRooms.collectAsState()

    val isFriend = friends.any { it.userId == selectedUser?.id }
    val friendObj = friends.firstOrNull { it.userId == selectedUser?.id }
    val isBlocked = friendObj?.isBlocked == true

    // Check if this friend has an active room
    val activeRoom = friendsRooms.firstOrNull { it.hostId == selectedUser?.id }

    BackHandler {
        viewModel.navigateTo(Screen.FRIENDS)
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(Screen.FRIENDS) },
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CyberCardElevated)
                    .border(1.dp, CyberBorder, RoundedCornerShape(12.dp))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "رجوع",
                    tint = CyberCyan,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = "الملف التعريفي للمستخدم",
                color = CyberTextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Profile details
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            CyberAvatarWithPresence(
                avatarUrl = selectedUser?.avatarUrl,
                name = selectedUser?.name ?: "مستخدم",
                isOnline = selectedUser?.isOnline == true,
                size = 90.dp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = selectedUser?.name ?: "",
                color = CyberTextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                text = "@${selectedUser?.username ?: ""}",
                color = CyberCyan,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Online Activity Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selectedUser?.isOnline == true) CyberEmerald.copy(alpha = 0.15f) else CyberCardElevated)
                    .border(
                        1.dp,
                        if (selectedUser?.isOnline == true) CyberEmerald.copy(alpha = 0.5f) else CyberBorder,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OnlineStatusIndicator(isOnline = selectedUser?.isOnline == true, size = 9.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (selectedUser?.isOnline == true) "نشط الآن في التطبيق" else formatLastSeen(selectedUser?.lastActive ?: 0L, false),
                        color = if (selectedUser?.isOnline == true) CyberEmerald else CyberTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Active Room Card (if friend is currently hosting a room)
            if (activeRoom != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.horizontalGradient(listOf(CyberPurple.copy(alpha = 0.3f), CyberCard))
                        )
                        .border(1.dp, CyberPurple, RoundedCornerShape(18.dp))
                        .clickable {
                            viewModel.joinRoomByCode(activeRoom.id)
                        }
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(CyberPurple),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Headset,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "غرفة صوتية جارية: ${activeRoom.name}",
                                    color = CyberTextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "انقر هنا للانضمام فوراً لغرفة صديقك",
                                    color = CyberEmerald,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Direct message button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(listOf(CyberPurple, CyberPurpleLight, CyberCyan))
                        )
                        .clickable {
                            selectedUser?.let { viewModel.openDirectChat(it) }
                        }
                        .testTag("btn_start_direct_chat"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "بدء محادثة خاصة فورية",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Voice Call button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(CyberEmerald.copy(alpha = 0.2f))
                        .border(1.dp, CyberEmerald.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .clickable {
                            selectedUser?.let { viewModel.startVoiceCall(it) }
                        }
                        .testTag("btn_profile_voice_call"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = null,
                            tint = CyberEmerald,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "مكالمة صوتية مباشرة",
                            color = CyberEmerald,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Add Friend button (if not already friend)
                if (!isFriend) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(CyberCard)
                            .border(1.dp, CyberBorder, RoundedCornerShape(14.dp))
                            .clickable {
                                selectedUser?.let { viewModel.sendFriendRequestTo(it) }
                            }
                            .testTag("btn_profile_add_friend"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "إرسال طلب صداقة",
                                color = CyberCyan,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Block / Unblock User (Always available for viewed user)
                if (selectedUser != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(CyberCardElevated)
                            .border(1.dp, if (isBlocked) CyberRed else CyberBorder, RoundedCornerShape(14.dp))
                            .clickable {
                                selectedUser?.let { user -> viewModel.toggleBlockUser(user) }
                            }
                            .testTag("btn_profile_block_toggle"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Block,
                                contentDescription = null,
                                tint = if (isBlocked) CyberRed else CyberTextSecondary,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBlocked) "إلغاء حظر المستخدم" else "حظر المستخدم",
                                color = if (isBlocked) CyberRed else CyberTextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
