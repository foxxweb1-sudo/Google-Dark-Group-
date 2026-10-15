package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.ui.components.CallOverlay
import com.example.ui.components.CyberGridBackground
import com.example.ui.components.ToastBanner
import com.example.ui.components.RoomFloatingOverlay
import com.example.ui.screens.ActiveRoomScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DirectChatScreen
import com.example.ui.screens.ForumScreen
import com.example.ui.screens.FriendsScreen
import com.example.ui.screens.InAppBrowserScreen
import com.example.ui.screens.LandingScreen
import com.example.ui.screens.PermissionsScreen
import com.example.ui.screens.PrivacyPolicyScreen
import com.example.ui.screens.PrivateConversationsScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.UserProfileViewScreen
import com.example.ui.theme.CybeNodeTheme
import com.example.viewmodel.CybeNodeViewModel
import com.example.viewmodel.Screen
import com.example.viewmodel.ToastEvent
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val viewModel: CybeNodeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CybeNodeTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    CybeNodeApp(viewModel)
                }
            }
        }
    }
}

@Composable
fun CybeNodeApp(viewModel: CybeNodeViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    var currentToast by remember { mutableStateOf<ToastEvent?>(null) }

    // Listen for toasts
    LaunchedEffect(Unit) {
        viewModel.toastFlow.collect { toast ->
            currentToast = toast
            delay(3500)
            if (currentToast == toast) {
                currentToast = null
            }
        }
    }

    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        modifier = Modifier.fillMaxSize()
    ) { _ ->
        CyberGridBackground {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = statusBarPadding, bottom = navBarPadding)
            ) {
                // Screen Navigation
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "ScreenTransition",
                    modifier = Modifier.fillMaxSize()
                ) { screen ->
                    when (screen) {
                        Screen.SPLASH -> SplashScreen(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                        Screen.PERMISSIONS -> PermissionsScreen(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                        Screen.LANDING -> LandingScreen(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                        Screen.SIGN_IN -> com.example.ui.screens.SignInScreen(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                        Screen.DASHBOARD -> DashboardScreen(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                        Screen.ACTIVE_ROOM -> ActiveRoomScreen(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                        Screen.PROFILE -> ProfileScreen(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                        Screen.FORUM -> ForumScreen(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                        Screen.PRIVACY -> PrivacyPolicyScreen(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                        Screen.FRIENDS -> FriendsScreen(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                        Screen.PRIVATE_CHATS -> PrivateConversationsScreen(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                        Screen.DIRECT_CHAT -> DirectChatScreen(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                        Screen.USER_PROFILE -> UserProfileViewScreen(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                        Screen.BROWSER -> InAppBrowserScreen(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Minimized Voice Room Floating Overlay
                RoomFloatingOverlay(
                    viewModel = viewModel,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )

                // Voice Call Full-screen Overlay
                CallOverlay(viewModel = viewModel)

                // Toast Notification Overlay
                ToastBanner(
                    toast = currentToast,
                    onDismiss = { currentToast = null },
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }
}
