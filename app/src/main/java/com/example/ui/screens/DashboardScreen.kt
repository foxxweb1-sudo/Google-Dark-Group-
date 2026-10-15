package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RoomInfo
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
import com.example.viewmodel.Screen
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    viewModel: CybeNodeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val downloadUrl by viewModel.downloadUrl.collectAsState()
    val supportUrl by viewModel.supportUrl.collectAsState()
    val mySavedRooms by viewModel.mySavedRooms.collectAsState()
    val friendsRooms by viewModel.friendsRooms.collectAsState()
    val friendRequests by viewModel.friendRequests.collectAsState()

    var newRoomName by remember { mutableStateOf("") }
    var enableRoomPassword by remember { mutableStateOf(false) }
    var newRoomPassword by remember { mutableStateOf("") }

    var joinRoomCode by remember { mutableStateOf("") }
    var joinRoomPasswordAttempt by remember { mutableStateOf("") }
    var showJoinPasswordDialog by remember { mutableStateOf(false) }
    var pendingLockedRoom by remember { mutableStateOf<RoomInfo?>(null) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = CyberCard,
                modifier = Modifier
                    .width(300.dp)
                    .fillMaxHeight()
                    .border(1.dp, CyberBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        // User Profile Header in Drawer
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(CyberCardElevated)
                                .clickable {
                                    scope.launch { drawerState.close() }
                                    viewModel.navigateTo(Screen.PROFILE)
                                }
                                .padding(12.dp)
                        ) {
                            CyberAvatar(
                                avatarUrl = currentUser?.avatarUrl,
                                name = currentUser?.name ?: "مستخدم",
                                size = 48.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = currentUser?.name ?: "المستخدم",
                                    color = CyberTextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "@${currentUser?.username ?: "user"}",
                                    color = CyberCyan,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))
                        HorizontalDivider(color = CyberBorder)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Navigation Items
                        DrawerItemRow(
                            icon = Icons.AutoMirrored.Filled.Chat,
                            iconTint = CyberPurpleLight,
                            title = "الرسائل الخاصة",
                            badgeCount = 0,
                            onClick = {
                                scope.launch { drawerState.close() }
                                viewModel.navigateTo(Screen.PRIVATE_CHATS)
                            }
                        )

                        DrawerItemRow(
                            icon = Icons.Default.Group,
                            iconTint = CyberCyan,
                            title = "الأصدقاء وطلبات الصداقة",
                            badgeCount = friendRequests.size,
                            onClick = {
                                scope.launch { drawerState.close() }
                                viewModel.navigateTo(Screen.FRIENDS)
                            }
                        )

                        DrawerItemRow(
                            icon = Icons.Default.Forum,
                            iconTint = CyberPurpleLight,
                            title = "المنتدى العام",
                            onClick = {
                                scope.launch { drawerState.close() }
                                viewModel.navigateTo(Screen.FORUM)
                            }
                        )

                        DrawerItemRow(
                            icon = Icons.Default.Person,
                            iconTint = CyberEmerald,
                            title = "الملف الشخصي",
                            onClick = {
                                scope.launch { drawerState.close() }
                                viewModel.navigateTo(Screen.PROFILE)
                            }
                        )

                        DrawerItemRow(
                            icon = Icons.Default.Code,
                            iconTint = CyberCyan,
                            title = "معلومات عن المطور",
                            onClick = {
                                scope.launch { drawerState.close() }
                                viewModel.openInAppBrowser("https://owner.cybenode.site/", "معلومات عن المطور")
                            }
                        )

                        DrawerItemRow(
                            icon = Icons.Default.Download,
                            iconTint = CyberCyan,
                            title = "تحميل التطبيق",
                            onClick = {
                                scope.launch { drawerState.close() }
                                viewModel.openInAppBrowser(downloadUrl, "تحميل التطبيق")
                            }
                        )

                        DrawerItemRow(
                            icon = Icons.Default.SupportAgent,
                            iconTint = CyberPurpleLight,
                            title = "الدعم الفني",
                            onClick = {
                                scope.launch { drawerState.close() }
                                viewModel.openInAppBrowser(supportUrl, "الدعم الفني")
                            }
                        )

                        DrawerItemRow(
                            icon = Icons.Default.Policy,
                            iconTint = CyberEmerald,
                            title = "سياسة الخصوصية",
                            onClick = {
                                scope.launch { drawerState.close() }
                                viewModel.navigateTo(Screen.PRIVACY)
                            }
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = CyberBorder)
                        Spacer(modifier = Modifier.height(10.dp))

                        DrawerItemRow(
                            icon = Icons.Default.PowerSettingsNew,
                            iconTint = CyberRed,
                            title = "تسجيل الخروج",
                            onClick = {
                                scope.launch { drawerState.close() }
                                viewModel.performLogout()
                            }
                        )
                    }

                    // Drawer footer brand
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CyberBg)
                            .border(1.dp, CyberBorder, RoundedCornerShape(12.dp))
                            .clickable {
                                scope.launch { drawerState.close() }
                                viewModel.openInAppBrowser("https://owner.cybenode.site/", "معلومات عن المطور")
                            }
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Created by Mahmoud Mostafa",
                            color = CyberCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "owner.cybenode.site",
                            color = CyberTextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // TOP APP BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(CyberCard)
                    .border(1.dp, CyberBorder, RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { scope.launch { drawerState.open() } },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CyberCardElevated)
                            .border(1.dp, CyberBorder, RoundedCornerShape(12.dp))
                            .testTag("btn_open_drawer")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "القائمة الجانبية",
                            tint = CyberCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Dark Group",
                            color = CyberTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "بروتوكول الاتصال الصوتي",
                            color = CyberTextMuted,
                            fontSize = 10.sp
                        )
                    }
                }

                // Profile avatar goes to fixed profile screen
                Box(
                    modifier = Modifier
                        .clickable { viewModel.navigateTo(Screen.PROFILE) }
                        .testTag("btn_dashboard_avatar_profile")
                ) {
                    CyberAvatar(
                        avatarUrl = currentUser?.avatarUrl,
                        name = currentUser?.name ?: "مستخدم",
                        size = 42.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // SLEEK 2X2 PREMIUM GRID FOR QUICK ACTIONS
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PremiumActionCard(
                        icon = Icons.AutoMirrored.Filled.Chat,
                        title = "الرسائل الخاصة",
                        subtitle = "محادثات مشفرة فورية",
                        tint = CyberPurpleLight,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(Screen.PRIVATE_CHATS) }
                    )
                    PremiumActionCard(
                        icon = Icons.Default.Group,
                        title = "قائمة الأصدقاء",
                        subtitle = "إدارة الأصدقاء والطلبات",
                        badgeCount = friendRequests.size,
                        tint = CyberCyan,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(Screen.FRIENDS) }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PremiumActionCard(
                        icon = Icons.Default.Forum,
                        title = "المنتدى العام",
                        subtitle = "مناقشات جماعية مفتوحة",
                        tint = CyberEmerald,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(Screen.FORUM) }
                    )
                    PremiumActionCard(
                        icon = Icons.Default.Person,
                        title = "الملف الشخصي",
                        subtitle = "تعديل البيانات والصور",
                        tint = CyberPurpleLight,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(Screen.PROFILE) }
                    )
                }
            }

            // SAVED / RE-ENTRY ROOMS SECTION
            if (mySavedRooms.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Column(
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "غرفي السابقة",
                                color = CyberTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(mySavedRooms) { room ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(CyberCardElevated)
                                    .border(1.dp, CyberPurple.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                                    .clickable {
                                        viewModel.reEnterSavedRoom(room)
                                    }
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Headset,
                                        contentDescription = null,
                                        tint = CyberPurpleLight,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = room.name,
                                            color = CyberTextPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "رمز: ${room.id}",
                                            color = CyberCyan,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ACTIVE FRIENDS ROOMS SECTION
            if (friendsRooms.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(CyberPurple.copy(alpha = 0.2f), CyberCard)
                            )
                        )
                        .border(1.dp, CyberPurple.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = "غرف الأصدقاء النشطة الآن 🎧",
                        color = CyberTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(friendsRooms) { fRoom ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(CyberCardElevated)
                                    .border(1.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                                    .clickable {
                                        if (fRoom.isLocked) {
                                            pendingLockedRoom = fRoom
                                            showJoinPasswordDialog = true
                                        } else {
                                            viewModel.joinRoomByCode(fRoom.id)
                                        }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = fRoom.name,
                                            color = CyberTextPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (fRoom.isLocked) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = "محمية",
                                                tint = CyberEmerald,
                                                modifier = Modifier.size(11.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "المضيف: ${fRoom.host}",
                                        color = CyberEmerald,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // CREATE ROOM CARD (WITH PASSWORD OPTION)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(CyberPurple.copy(alpha = 0.25f), CyberCard)
                        )
                    )
                    .border(1.dp, CyberPurple.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .padding(18.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CyberPurple.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = CyberPurpleLight,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "إنشاء غرفة صوتية",
                                color = CyberTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "توليد غرفة خاصة مشفرة مع إمكانية قفلها بكلمة سر",
                                color = CyberTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = newRoomName,
                        onValueChange = { newRoomName = it },
                        placeholder = { Text("اسم الغرفة...", fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_new_room_name"),
                        colors = outlinedFieldColors(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Password Lock Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CyberBg)
                            .border(1.dp, CyberBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (enableRoomPassword) CyberEmerald else CyberTextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "حماية الغرفة بكلمة سر",
                                color = CyberTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Switch(
                            checked = enableRoomPassword,
                            onCheckedChange = { enableRoomPassword = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = CyberPurple
                            )
                        )
                    }

                    AnimatedVisibility(visible = enableRoomPassword) {
                        Column {
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = newRoomPassword,
                                onValueChange = { newRoomPassword = it },
                                placeholder = { Text("أدخل كلمة سر للغرفة...", fontSize = 12.sp) },
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth(),
                                colors = outlinedFieldColors(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CyberPurple)
                            .clickable {
                                viewModel.createRoom(
                                    newRoomName,
                                    if (enableRoomPassword) newRoomPassword else ""
                                )
                            }
                            .testTag("btn_create_room"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Headset,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "تشغيل الغرفة الآن",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // JOIN ROOM CARD
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(CyberCard)
                    .border(1.dp, CyberBorder, RoundedCornerShape(20.dp))
                    .padding(18.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CyberCyan.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Login,
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "الانضمام برمز الغرفة",
                                color = CyberTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "ادخل الرمز الفريد المكون من حروف وأرقام",
                                color = CyberTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = joinRoomCode,
                        onValueChange = { joinRoomCode = it.uppercase() },
                        placeholder = { Text("أدخل كود الغرفة...", fontSize = 12.sp) },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_join_room_code"),
                        colors = outlinedFieldColors(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CyberCardElevated)
                            .border(1.dp, CyberBorder, RoundedCornerShape(12.dp))
                            .clickable {
                                viewModel.joinRoomByCode(joinRoomCode, joinRoomPasswordAttempt)
                            }
                            .testTag("btn_join_room"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "دخول مباشر",
                                color = CyberTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // DEVELOPER SECTION CARD (MAHMOUD MOSTAFA)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0x354A154B),
                                CyberCard,
                                Color(0x2506B6D4)
                            )
                        )
                    )
                    .border(1.dp, CyberPurple.copy(alpha = 0.25f), RoundedCornerShape(18.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(listOf(CyberPurple, CyberCyan))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Created by: ",
                                color = CyberTextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Mahmoud Mostafa",
                                color = CyberCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Text(
                            text = "بنية Dark Group التحتية للأنظمة الذكية والخدمات السحابية",
                            color = CyberTextSecondary,
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(CyberCyan.copy(alpha = 0.12f))
                        .border(1.dp, CyberCyan.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .clickable {
                            viewModel.openInAppBrowser("https://owner.cybenode.site/", "معلومات عن المطور")
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("btn_dev_site")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "owner.cybenode.site",
                            color = CyberCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Password input dialog when clicking locked friend room
    if (showJoinPasswordDialog && pendingLockedRoom != null) {
        val room = pendingLockedRoom!!
        var passAttempt by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = {
                showJoinPasswordDialog = false
                pendingLockedRoom = null
            },
            title = {
                Text(
                    text = "الغرفة محمية بكلمة سر 🔒",
                    color = CyberTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "هذه الغرفة تتطلب كلمة سر للدخول:",
                        color = CyberTextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = passAttempt,
                        onValueChange = { passAttempt = it },
                        placeholder = { Text("أدخل كلمة السر...", fontSize = 12.sp) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = outlinedFieldColors(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showJoinPasswordDialog = false
                        viewModel.joinRoomByCode(room.id, passAttempt)
                        pendingLockedRoom = null
                    }
                ) {
                    Text(text = "دخول", color = CyberCyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showJoinPasswordDialog = false
                        pendingLockedRoom = null
                    }
                ) {
                    Text(text = "إلغاء", color = CyberTextMuted)
                }
            },
            containerColor = CyberCard,
            shape = RoundedCornerShape(18.dp)
        )
    }
}

@Composable
fun DrawerItemRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    badgeCount: Int = 0,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                color = CyberTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }

        if (badgeCount > 0) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(CyberRed),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badgeCount.toString(),
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun QuickActionChip(
    icon: ImageVector,
    title: String,
    tint: Color,
    badgeCount: Int = 0,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(CyberCard)
            .border(1.dp, CyberBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.TopEnd) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(tint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(16.dp)
                    )
                }

                if (badgeCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(CyberRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = badgeCount.toString(),
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                color = CyberTextPrimary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@Composable
fun PremiumActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tint: Color,
    badgeCount: Int = 0,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(CyberCardElevated, CyberCard)
                )
            )
            .border(1.dp, CyberBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(contentAlignment = Alignment.TopEnd) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(tint.copy(alpha = 0.12f))
                        .border(1.dp, tint.copy(alpha = 0.35f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (badgeCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(CyberRed)
                            .border(1.dp, CyberBg, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = badgeCount.toString(),
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = CyberTextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = subtitle,
                    color = CyberTextMuted,
                    fontSize = 9.sp,
                    maxLines = 1
                )
            }
        }
    }
}
