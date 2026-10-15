package com.example.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.audio.VoiceEngine
import com.example.data.local.AppDatabase
import com.example.data.local.ChatMessageEntity
import com.example.data.local.UserEntity
import com.example.data.model.*
import com.example.data.remote.FirebaseRtdbService
import com.example.data.remote.VercelBlobService
import com.example.util.CallRingtoneManager
import com.example.util.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class Screen {
    SPLASH,
    PERMISSIONS,
    LANDING,
    DASHBOARD,
    ACTIVE_ROOM,
    PROFILE,
    FORUM,
    PRIVACY,
    FRIENDS,
    PRIVATE_CHATS,
    DIRECT_CHAT,
    USER_PROFILE,
    BROWSER,
    SIGN_IN
}

enum class ToastType {
    INFO,
    SUCCESS,
    ERROR
}

data class ToastEvent(
    val message: String,
    val type: ToastType = ToastType.INFO
)

enum class CallUiState {
    IDLE,
    OUTGOING_RINGING,
    INCOMING_RINGING,
    CONNECTED
}

class CybeNodeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Room.databaseBuilder(
        application.applicationContext,
        AppDatabase::class.java,
        "darkgroup_db"
    ).fallbackToDestructiveMigration().build()

    private val authService = com.example.data.remote.AuthService(application.applicationContext)
    private val firestoreService = com.example.data.remote.FirestoreService(application.applicationContext)

    val voiceEngine = VoiceEngine(application.applicationContext, viewModelScope)

    private val _currentScreen = MutableStateFlow(Screen.SPLASH)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isUploadingAvatar = MutableStateFlow(false)
    val isUploadingAvatar: StateFlow<Boolean> = _isUploadingAvatar.asStateFlow()

    private val _stagedAvatarUrl = MutableStateFlow<String?>(null)
    val stagedAvatarUrl: StateFlow<String?> = _stagedAvatarUrl.asStateFlow()

    // ROOMS
    private val _currentRoom = MutableStateFlow<RoomInfo?>(null)
    val currentRoom: StateFlow<RoomInfo?> = _currentRoom.asStateFlow()

    private val _isRoomMinimized = MutableStateFlow(false)
    val isRoomMinimized: StateFlow<Boolean> = _isRoomMinimized.asStateFlow()

    private val _roomUsers = MutableStateFlow<List<RoomUser>>(emptyList())
    val roomUsers: StateFlow<List<RoomUser>> = _roomUsers.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _mySavedRooms = MutableStateFlow<List<RoomInfo>>(emptyList())
    val mySavedRooms: StateFlow<List<RoomInfo>> = _mySavedRooms.asStateFlow()

    private val _friendsRooms = MutableStateFlow<List<RoomInfo>>(emptyList())
    val friendsRooms: StateFlow<List<RoomInfo>> = _friendsRooms.asStateFlow()

    // PUBLIC FORUM
    private val _forumMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val forumMessages: StateFlow<List<ChatMessage>> = _forumMessages.asStateFlow()

    // FRIENDS & REQUESTS
    private val _friends = MutableStateFlow<List<Friend>>(emptyList())
    val friends: StateFlow<List<Friend>> = _friends.asStateFlow()

    private val _friendRequests = MutableStateFlow<List<FriendRequest>>(emptyList())
    val friendRequests: StateFlow<List<FriendRequest>> = _friendRequests.asStateFlow()

    private val _searchUserResult = MutableStateFlow<User?>(null)
    val searchUserResult: StateFlow<User?> = _searchUserResult.asStateFlow()

    private val _isSearchingUser = MutableStateFlow(false)
    val isSearchingUser: StateFlow<Boolean> = _isSearchingUser.asStateFlow()

    private val _selectedUserProfile = MutableStateFlow<User?>(null)
    val selectedUserProfile: StateFlow<User?> = _selectedUserProfile.asStateFlow()

    // USER PRESENCE & ACTIVITY
    private val _usersPresence = MutableStateFlow<Map<String, UserPresence>>(emptyMap())
    val usersPresence: StateFlow<Map<String, UserPresence>> = _usersPresence.asStateFlow()

    // PRIVATE MESSAGING
    private val _activeDirectUser = MutableStateFlow<User?>(null)
    val activeDirectUser: StateFlow<User?> = _activeDirectUser.asStateFlow()

    private val _privateMessages = MutableStateFlow<List<PrivateChatMessage>>(emptyList())
    val privateMessages: StateFlow<List<PrivateChatMessage>> = _privateMessages.asStateFlow()

    private val _conversations = MutableStateFlow<List<PrivateConversation>>(emptyList())
    val conversations: StateFlow<List<PrivateConversation>> = _conversations.asStateFlow()

    // 1-ON-1 CALLS
    private val _callUiState = MutableStateFlow(CallUiState.IDLE)
    val callUiState: StateFlow<CallUiState> = _callUiState.asStateFlow()

    private val _activeCallSession = MutableStateFlow<CallSession?>(null)
    val activeCallSession: StateFlow<CallSession?> = _activeCallSession.asStateFlow()

    private val _callDurationSec = MutableStateFlow(0)
    val callDurationSec: StateFlow<Int> = _callDurationSec.asStateFlow()

    // AGORA VOICE CONFIGURATION
    val isAgoraEngineReady = com.example.audio.AgoraRtcService.isEngineReady

    // DYNAMIC URLS
    private val _downloadUrl = MutableStateFlow("https://owner.cybenode.site/")
    val downloadUrl: StateFlow<String> = _downloadUrl.asStateFlow()

    private val _supportUrl = MutableStateFlow("https://owner.cybenode.site/")
    val supportUrl: StateFlow<String> = _supportUrl.asStateFlow()

    private val _privacyUrl = MutableStateFlow("https://owner.cybenode.site/privacy")
    val privacyUrl: StateFlow<String> = _privacyUrl.asStateFlow()

    // IN-APP BROWSER
    private val _browserUrl = MutableStateFlow("https://owner.cybenode.site/")
    val browserUrl: StateFlow<String> = _browserUrl.asStateFlow()

    private val _browserTitle = MutableStateFlow("معلومات المطور")
    val browserTitle: StateFlow<String> = _browserTitle.asStateFlow()

    private var previousScreenBeforeBrowser: Screen = Screen.DASHBOARD

    fun openInAppBrowser(url: String = "https://owner.cybenode.site/", title: String = "معلومات المطور") {
        if (_currentScreen.value != Screen.BROWSER) {
            previousScreenBeforeBrowser = _currentScreen.value
        }
        _browserUrl.value = url
        _browserTitle.value = title
        _currentScreen.value = Screen.BROWSER
    }

    fun closeInAppBrowser() {
        _currentScreen.value = previousScreenBeforeBrowser
    }

    fun updateBrowserTitle(title: String) {
        if (title.isNotBlank()) {
            _browserTitle.value = title
        }
    }

    private val _toastFlow = MutableSharedFlow<ToastEvent>()
    val toastFlow: SharedFlow<ToastEvent> = _toastFlow.asSharedFlow()

    private var roomSyncJob: Job? = null
    private var roomAudioJob: Job? = null
    private var forumSyncJob: Job? = null
    private var directSyncJob: Job? = null
    private var globalNotificationJob: Job? = null
    private var callPollingJob: Job? = null
    private var callAudioJob: Job? = null
    private var callTimerJob: Job? = null

    init {
        NotificationHelper.initChannel(application.applicationContext)

        viewModelScope.launch {
            // Attempt auto sign-in first
            val signedIn = authService.attemptAutoSignIn()
            if (signedIn) {
                val fbUser = authService.getCurrentUser()
                if (fbUser != null) {
                    // Fetch profile from Firestore
                    val fUser = firestoreService.getUser(fbUser.uid)
                    if (fUser != null) {
                        val user = User(
                            id = fUser.userId,
                            username = fUser.username,
                            name = fUser.name,
                            avatarUrl = fUser.avatarUrl,
                            isAnon = false,
                            password = ""
                        )
                        _currentUser.value = user
                        
                        // Sync to local DB for compatibility
                        db.userDao().saveUser(UserEntity(
                            id = user.id,
                            username = user.username,
                            name = user.name,
                            avatarUrl = user.avatarUrl,
                            isAnon = false,
                            password = "",
                            isCurrent = true
                        ))
                        
                        startGlobalListener()
                        startBackgroundService()
                        startGlobalRoomsObserver()
                    } else {
                        // User exists in Auth but not in Firestore? Create it.
                        val newUser = FUser(
                            userId = fbUser.uid,
                            username = fbUser.email?.substringBefore("@") ?: "user_${fbUser.uid.take(5)}",
                            name = fbUser.displayName ?: "مستخدم Dark Group",
                            avatarUrl = fbUser.photoUrl?.toString()
                        )
                        firestoreService.saveUser(newUser)
                        val user = User(
                            id = newUser.userId,
                            username = newUser.username,
                            name = newUser.name,
                            avatarUrl = newUser.avatarUrl,
                            isAnon = false
                        )
                        _currentUser.value = user
                        startGlobalListener()
                        startBackgroundService()
                        startGlobalRoomsObserver()
                    }
                }
            } else {
                // Not signed in; stay on Splash or Landing then to SignIn
            }
            refreshDynamicUrls()
        }
    }

    fun signInWithGoogle(activity: android.app.Activity, onComplete: () -> Unit) {
        viewModelScope.launch {
            val success = authService.signInWithGoogle(activity)
            if (success) {
                val fbUser = authService.getCurrentUser()
                if (fbUser != null) {
                    // Check if user exists in Firestore
                    var fUser = firestoreService.getUser(fbUser.uid)
                    if (fUser == null) {
                        // Create new user profile
                        fUser = com.example.data.model.FUser(
                            userId = fbUser.uid,
                            username = fbUser.email?.substringBefore("@") ?: "user_${fbUser.uid.take(5)}",
                            name = fbUser.displayName ?: "مستخدم جديد",
                            avatarUrl = fbUser.photoUrl?.toString()
                        )
                        firestoreService.saveUser(fUser)
                    }
                    
                    val user = User(
                        id = fUser.userId,
                        username = fUser.username,
                        name = fUser.name,
                        avatarUrl = fUser.avatarUrl,
                        isAnon = false,
                        password = ""
                    )
                    _currentUser.value = user
                    
                    // Sync to local DB
                    db.userDao().clearCurrentSession()
                    db.userDao().saveUser(com.example.data.local.UserEntity(
                        id = user.id,
                        username = user.username,
                        name = user.name,
                        avatarUrl = user.avatarUrl,
                        isAnon = user.isAnon,
                        password = "",
                        isCurrent = true
                    ))
                    
                    startGlobalListener()
                    startBackgroundService()
                    startGlobalRoomsObserver()
                    _currentScreen.value = Screen.DASHBOARD
                    showToast("تم تسجيل الدخول بنجاح! 🚀", ToastType.SUCCESS)
                }
            } else {
                showToast("فشل تسجيل الدخول. يرجى المحاولة مرة أخرى.", ToastType.ERROR)
            }
            onComplete()
        }
    }

    private fun startBackgroundService() {
        val context = getApplication<Application>().applicationContext
        com.example.util.GroupCallService.startService(context)
    }

    private fun stopBackgroundService() {
        val context = getApplication<Application>().applicationContext
        com.example.util.GroupCallService.stopService(context)
    }

    fun showToast(message: String, type: ToastType = ToastType.INFO) {
        viewModelScope.launch {
            _toastFlow.emit(ToastEvent(message, type))
        }
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun refreshDynamicUrls() {
        viewModelScope.launch {
            _downloadUrl.value = FirebaseRtdbService.getDownloadUrl()
            _supportUrl.value = FirebaseRtdbService.getSupportUrl()
            _privacyUrl.value = FirebaseRtdbService.getPrivacyPolicyUrl()
            
            val workerUrl = FirebaseRtdbService.getCloudflareWorkerUrl()
            if (workerUrl != null) {
                com.example.data.remote.CloudflareService.setWorkerUrl(workerUrl)
            }
        }
    }

    fun onSplashFinish(needsPermissions: Boolean) {
        if (needsPermissions) {
            _currentScreen.value = Screen.PERMISSIONS
        } else if (_currentUser.value != null) {
            _currentScreen.value = Screen.DASHBOARD
        } else {
            _currentScreen.value = Screen.SIGN_IN
        }
    }

    fun onPermissionsFinished() {
        if (_currentUser.value != null) {
            _currentScreen.value = Screen.DASHBOARD
        } else {
            _currentScreen.value = Screen.SIGN_IN
        }
    }

    // AUTH
    fun uploadAvatarFile(uri: Uri) {
        viewModelScope.launch {
            _isUploadingAvatar.value = true
            try {
                val context = getApplication<Application>().applicationContext
                val bytes = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                }

                if (bytes != null && bytes.isNotEmpty()) {
                    val fileName = "avatar_${System.currentTimeMillis()}.jpg"
                    val result = VercelBlobService.uploadImage(bytes, fileName)
                    result.fold(
                        onSuccess = { url ->
                            _stagedAvatarUrl.value = url
                            showToast("تم حفظ الصورة بنجاح", ToastType.SUCCESS)
                        },
                        onFailure = {
                            _stagedAvatarUrl.value = uri.toString()
                            showToast("تم حفظ الصورة بنجاح", ToastType.SUCCESS)
                        }
                    )
                } else {
                    showToast("تعذر قراءة ملف الصورة", ToastType.ERROR)
                }
            } catch (e: Exception) {
                showToast("تعذر تحديد ملف الصورة", ToastType.ERROR)
            } finally {
                _isUploadingAvatar.value = false
            }
        }
    }

    fun performLogout() {
        val user = _currentUser.value
        viewModelScope.launch {
            if (user != null) {
                // Update presence to offline in Firestore
                firestoreService.saveUser(FUser(userId = user.id, isOnline = false))
            }
            leaveRoom()
            globalNotificationJob?.cancel()
            authService.signOut()
            db.userDao().clearCurrentSession()
            stopBackgroundService()
            _currentUser.value = null
            _stagedAvatarUrl.value = null
            _friends.value = emptyList()
            _friendRequests.value = emptyList()
            _currentScreen.value = Screen.SIGN_IN
            showToast("تم تسجيل الخروج بنجاح", ToastType.INFO)
        }
    }

    fun signInWithEmail(email: String, pass: String, onComplete: (Boolean) -> Unit) {
        if (email.isBlank() || pass.isBlank()) {
            showToast("يرجى إدخال البريد الإلكتروني وكلمة المرور", ToastType.ERROR)
            onComplete(false)
            return
        }
        viewModelScope.launch {
            val res = authService.signInWithEmail(email, pass)
            res.fold(
                onSuccess = { fbUser ->
                    var fUser = firestoreService.getUser(fbUser.uid)
                    if (fUser == null) {
                        val defaultUser = FUser(
                            userId = fbUser.uid,
                            username = fbUser.email?.substringBefore("@")?.lowercase() ?: "user_${fbUser.uid.take(5)}",
                            name = fbUser.displayName ?: "مستخدم Dark Group",
                            email = fbUser.email,
                            avatarUrl = fbUser.photoUrl?.toString(),
                            isOnline = true
                        )
                        firestoreService.saveUser(defaultUser)
                        fUser = defaultUser
                    }
                    val user = User(
                        id = fUser.userId,
                        username = fUser.username,
                        name = fUser.name,
                        avatarUrl = fUser.avatarUrl,
                        isAnon = false,
                        password = ""
                    )
                    _currentUser.value = user
                    db.userDao().clearCurrentSession()
                    db.userDao().saveUser(UserEntity(
                        id = user.id,
                        username = user.username,
                        name = user.name,
                        avatarUrl = user.avatarUrl,
                        isAnon = false,
                        password = "",
                        isCurrent = true
                    ))
                    startGlobalListener()
                    startBackgroundService()
                    startGlobalRoomsObserver()
                    _currentScreen.value = Screen.DASHBOARD
                    showToast("مرحباً بعودتك ${user.name}! 🚀", ToastType.SUCCESS)
                    onComplete(true)
                },
                onFailure = { err ->
                    val msg = when {
                        err is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException ||
                        err.message?.contains("credential", ignoreCase = true) == true ||
                        err.message?.contains("password", ignoreCase = true) == true -> 
                            "بيانات الدخول غير صحيحة أو الحساب غير مسجل. يرجى التأكد من كلمة المرور أو إنشاء حساب جديد."
                        err is com.google.firebase.auth.FirebaseAuthInvalidUserException ||
                        err.message?.contains("user-not-found", ignoreCase = true) == true -> 
                            "البريد الإلكتروني غير مسجل. يرجى إنشاء حساب جديد أولاً."
                        err.message?.contains("badly formatted", ignoreCase = true) == true -> 
                            "صيغة البريد الإلكتروني غير صحيحة (مثال: name@domain.com)"
                        err.message?.contains("network", ignoreCase = true) == true -> 
                            "تعذر الاتصال بالشبكة، يرجى التحقق من اتصال الإنترنت"
                        err.message?.contains("too-many-requests", ignoreCase = true) == true -> 
                            "تم حظر المحاولات مؤقتاً لكثرة المحاولات. يرجى الانتظار قليلاً أو الدخول بحساب Google."
                        else -> "فشل تسجيل الدخول: يرجى التحقق من صحة البيانات أو استخدام الدخول عبر Google."
                    }
                    showToast(msg, ToastType.ERROR)
                    onComplete(false)
                }
            )
        }
    }

    fun signUpWithEmail(
        name: String,
        rawUsername: String,
        email: String,
        pass: String,
        onComplete: (Boolean) -> Unit
    ) {
        val cleanName = name.trim()
        val cleanUsername = rawUsername.trim().lowercase().replace(Regex("[^a-z0-9_]"), "")
        val cleanEmail = email.trim()

        if (cleanName.isBlank() || cleanUsername.isBlank() || cleanEmail.isBlank() || pass.length < 6) {
            showToast("يرجى ملء جميع الحقول (كلمة المرور 6 أحرف على الأقل)", ToastType.ERROR)
            onComplete(false)
            return
        }

        if (cleanUsername.length < 3) {
            showToast("يجب أن يكون اسم المستخدم 3 أحرف على الأقل", ToastType.ERROR)
            onComplete(false)
            return
        }

        viewModelScope.launch {
            // 1. Check if username already taken in Firestore
            val existing = firestoreService.searchUserByUsername(cleanUsername)
            if (existing != null) {
                showToast("اسم المستخدم @$cleanUsername مأخوذ بالفعل! اختر اسماً آخر للبحث", ToastType.ERROR)
                onComplete(false)
                return@launch
            }

            // 2. Create user with Firebase Auth
            val res = authService.signUpWithEmail(cleanEmail, pass, cleanName)
            res.fold(
                onSuccess = { fbUser ->
                    val fUser = FUser(
                        userId = fbUser.uid,
                        username = cleanUsername,
                        name = cleanName,
                        email = cleanEmail,
                        avatarUrl = _stagedAvatarUrl.value,
                        isOnline = true
                    )
                    firestoreService.saveUser(fUser)
                    val user = User(
                        id = fUser.userId,
                        username = fUser.username,
                        name = fUser.name,
                        avatarUrl = fUser.avatarUrl,
                        isAnon = false,
                        password = ""
                    )
                    _currentUser.value = user
                    db.userDao().clearCurrentSession()
                    db.userDao().saveUser(UserEntity(
                        id = user.id,
                        username = user.username,
                        name = user.name,
                        avatarUrl = user.avatarUrl,
                        isAnon = false,
                        password = "",
                        isCurrent = true
                    ))
                    startGlobalListener()
                    startBackgroundService()
                    startGlobalRoomsObserver()
                    _currentScreen.value = Screen.DASHBOARD
                    showToast("تم إنشاء الحساب بنجاح! مرحباً بك يا $cleanName 🎉", ToastType.SUCCESS)
                    onComplete(true)
                },
                onFailure = { err ->
                    val msg = when {
                        err is com.google.firebase.auth.FirebaseAuthUserCollisionException ||
                        err.message?.contains("email-already-in-use", ignoreCase = true) == true -> 
                            "هذا البريد الإلكتروني مسجل بالفعل! يمكنك تسجيل الدخول بدلاً من ذلك."
                        err is com.google.firebase.auth.FirebaseAuthWeakPasswordException ||
                        err.message?.contains("weak-password", ignoreCase = true) == true -> 
                            "كلمة المرور ضعيفة جداً. يرجى إدخال 6 أحرف أو أرقام على الأقل."
                        err.message?.contains("badly formatted", ignoreCase = true) == true -> 
                            "صيغة البريد الإلكتروني غير صحيحة (مثال: name@domain.com)"
                        err.message?.contains("network", ignoreCase = true) == true -> 
                            "تعذر الاتصال بالشبكة، يرجى التحقق من اتصال الإنترنت"
                        else -> "فشل إنشاء الحساب: ${err.localizedMessage ?: "يرجى التأكد من البيانات أو التسجيل بحساب Google"}"
                    }
                    showToast(msg, ToastType.ERROR)
                    onComplete(false)
                }
            )
        }
    }

    fun sendPasswordReset(email: String) {
        val clean = email.trim()
        if (clean.isBlank()) {
            showToast("يرجى إدخال البريد الإلكتروني", ToastType.INFO)
            return
        }
        viewModelScope.launch {
            authService.sendPasswordReset(clean).fold(
                onSuccess = {
                    showToast("تم إرسال رابط إعادة ضبط كلمة المرور إلى $clean", ToastType.SUCCESS)
                },
                onFailure = {
                    showToast("تعذر إرسال الرابط، تأكد من صحة البريد", ToastType.ERROR)
                }
            )
        }
    }

    fun updateProfile(name: String, newUsername: String = "", onComplete: (Boolean) -> Unit = {}) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val targetUsername = if (newUsername.isNotBlank()) {
                val cleaned = newUsername.trim().lowercase().replace(Regex("[^a-z0-9_]"), "")
                if (cleaned.length < 3) {
                    showToast("يجب أن يكون اسم المستخدم 3 أحرف على الأقل", ToastType.ERROR)
                    onComplete(false)
                    return@launch
                }
                if (cleaned != user.username) {
                    val existing = firestoreService.searchUserByUsername(cleaned)
                    if (existing != null && existing.userId != user.id) {
                        showToast("اسم المستخدم @$cleaned محجوز بالفعل! اختر اسماً آخر", ToastType.ERROR)
                        onComplete(false)
                        return@launch
                    }
                }
                cleaned
            } else {
                user.username
            }

            val newAvatar = _stagedAvatarUrl.value ?: user.avatarUrl
            val updated = user.copy(
                name = name.ifBlank { user.name },
                username = targetUsername,
                avatarUrl = newAvatar
            )

            val fUser = FUser(
                userId = updated.id,
                username = updated.username,
                name = updated.name,
                avatarUrl = updated.avatarUrl,
                isOnline = true
            )
            firestoreService.saveUser(fUser)
            
            db.userDao().saveUser(
                UserEntity(
                    username = updated.username,
                    id = updated.id,
                    name = updated.name,
                    avatarUrl = updated.avatarUrl,
                    isAnon = updated.isAnon,
                    password = "",
                    isCurrent = true
                )
            )

            _currentUser.value = updated
            _stagedAvatarUrl.value = null
            showToast("تم تحديث الملف الشخصي بنجاح! ✅", ToastType.SUCCESS)
            onComplete(true)
        }
    }

    // ROOMS
    private val _globalRooms = MutableStateFlow<List<FRoom>>(emptyList())
    val globalRooms: StateFlow<List<FRoom>> = _globalRooms.asStateFlow()

    private val _roomEventBanner = MutableStateFlow<RoomEventBanner?>(null)
    val roomEventBanner: StateFlow<RoomEventBanner?> = _roomEventBanner.asStateFlow()

    private var bannerDismissJob: Job? = null
    fun triggerRoomBanner(banner: RoomEventBanner) {
        _roomEventBanner.value = banner
        bannerDismissJob?.cancel()
        bannerDismissJob = viewModelScope.launch {
            delay(4000)
            if (_roomEventBanner.value?.id == banner.id) {
                _roomEventBanner.value = null
            }
        }
    }

    fun createRoom(nameInput: String, passwordInput: String = "") {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val roomName = nameInput.ifBlank { "غرفة ${user.name}" }
            val roomCode = "ROOM" + (1000..9999).random()
            val pass = passwordInput.trim().ifEmpty { null }
            val voiceChannelId = "dark_room_${roomCode.lowercase(Locale.ROOT)}"

            val room = FRoom(
                id = roomCode,
                name = roomName,
                hostId = user.id,
                hostName = user.name,
                voiceChannelId = voiceChannelId,
                participantCount = 1,
                participants = listOf(user.id),
                password = pass,
                isLocked = !pass.isNullOrBlank()
            )

            firestoreService.createRoom(room)

            val roomInfo = RoomInfo(
                id = room.id,
                name = room.name,
                host = user.name,
                hostId = room.hostId,
                voiceChannelId = voiceChannelId,
                participantCount = 1,
                password = room.password,
                isLocked = room.isLocked,
                createdAt = System.currentTimeMillis()
            )

            // Add to saved rooms list
            val currentSaved = _mySavedRooms.value.toMutableList()
            if (currentSaved.none { it.id == roomInfo.id }) {
                currentSaved.add(0, roomInfo)
                _mySavedRooms.value = currentSaved
            }

            enterRoom(roomInfo)
        }
    }

    private var globalRoomsJob: Job? = null
    private fun startGlobalRoomsObserver() {
        globalRoomsJob?.cancel()
        globalRoomsJob = viewModelScope.launch {
            try {
                firestoreService.observeRooms().collect { rooms ->
                    _globalRooms.value = rooms
                }
            } catch (e: Exception) {
                android.util.Log.w("CybeNodeViewModel", "globalRoomsObserver error: ${e.message}")
            }
        }
    }

    fun joinRoomByCode(codeInput: String, passwordAttempt: String = "") {
        val code = codeInput.trim().uppercase(Locale.ROOT)
        if (code.isEmpty()) {
            showToast("يرجى إدخال كود الغرفة", ToastType.ERROR)
            return
        }

        viewModelScope.launch {
            val fRoom = firestoreService.getRoom(code)
            if (fRoom == null) {
                showToast("هذه الغرفة غير موجودة", ToastType.ERROR)
                return@launch
            }

            // Check if password protected
            if (fRoom.isLocked && !fRoom.password.isNullOrBlank()) {
                val user = _currentUser.value
                val isHost = user?.id == fRoom.hostId
                if (!isHost && fRoom.password != passwordAttempt.trim()) {
                    showToast("كلمة سر الغرفة غير صحيحة!", ToastType.ERROR)
                    return@launch
                }
            }

            val voiceChannelId = fRoom.voiceChannelId.ifBlank { "dark_room_${fRoom.id.lowercase(Locale.ROOT)}" }
            val roomInfo = RoomInfo(
                id = fRoom.id,
                name = fRoom.name,
                host = fRoom.hostName,
                hostId = fRoom.hostId,
                voiceChannelId = voiceChannelId,
                participantCount = fRoom.participantCount,
                password = fRoom.password,
                isLocked = fRoom.isLocked
            )
            saveAndEnterRoom(roomInfo)
        }
    }

    fun reEnterSavedRoom(room: RoomInfo) {
        viewModelScope.launch {
            val refreshed = firestoreService.getRoom(room.id)
            if (refreshed != null) {
                val voiceChannelId = refreshed.voiceChannelId.ifBlank { "dark_room_${refreshed.id.lowercase(Locale.ROOT)}" }
                enterRoom(RoomInfo(
                    id = refreshed.id,
                    name = refreshed.name,
                    host = refreshed.hostName,
                    hostId = refreshed.hostId,
                    voiceChannelId = voiceChannelId,
                    participantCount = refreshed.participantCount,
                    password = refreshed.password,
                    isLocked = refreshed.isLocked
                ))
            } else {
                enterRoom(room)
            }
        }
    }

    private fun saveAndEnterRoom(room: RoomInfo) {
        val currentSaved = _mySavedRooms.value.toMutableList()
        if (currentSaved.none { it.id == room.id }) {
            currentSaved.add(0, room)
            _mySavedRooms.value = currentSaved
        }
        enterRoom(room)
    }

    private fun enterRoom(room: RoomInfo) {
        val user = _currentUser.value ?: return
        val voiceChannelId = room.voiceChannelId.ifBlank { "dark_room_${room.id.lowercase(Locale.ROOT)}" }
        val updatedRoom = room.copy(voiceChannelId = voiceChannelId)
        _currentRoom.value = updatedRoom
        _currentScreen.value = Screen.ACTIVE_ROOM

        val myRoomUser = RoomUser(
            id = user.id,
            peerId = "peer_" + user.username,
            name = user.name,
            username = user.username,
            avatarUrl = user.avatarUrl,
            isVoice = true,
            isMuted = false
        )

        _roomUsers.value = listOf(myRoomUser)

        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val welcomeMsg = ChatMessage(
            id = "sys_${System.currentTimeMillis()}",
            roomCode = room.id,
            senderId = "system",
            senderName = "النظام",
            text = "تم فتح الغرفة: ${room.name} (${room.id})",
            time = timeStr,
            isSystem = true
        )
        _messages.value = listOf(welcomeMsg)

        // Start Local 0ms UDP Audio Relay
        voiceEngine.udpRelay.start(room.id, user.id)
        voiceEngine.udpRelay.onLocalAudioReceived = { pcm, _ ->
            voiceEngine.playIncomingAudio(pcm)
        }

        // Auto connect voice with Agora WebRTC channel using stored voiceChannelId
        voiceEngine.connectVoice(true, voiceChannelId, user.id)

        // Show welcome banner for entering the room
        triggerRoomBanner(
            RoomEventBanner(
                id = java.util.UUID.randomUUID().toString(),
                type = RoomEventType.JOIN,
                userId = user.id,
                userName = user.name,
                avatarUrl = user.avatarUrl,
                message = "انضممت إلى الغرفة الصوتية"
            )
        )

        viewModelScope.launch {
            // Register user in Firestore room members & persist voiceChannelId
            val fMember = FRoomMember(
                userId = user.id,
                username = user.username,
                name = user.name,
                avatarUrl = user.avatarUrl,
                voiceChannelId = voiceChannelId,
                isVoiceConnected = true,
                isMuted = false,
                role = if (user.id == room.hostId) "host" else "member",
                joinedAt = com.google.firebase.Timestamp.now()
            )
            firestoreService.joinRoom(room.id, fMember, voiceChannelId)
            FirebaseRtdbService.joinRoomUser(room.id, myRoomUser)

            // Post join announcement to Firestore room messages
            val joinMsg = FMessage(
                senderId = user.id,
                senderName = user.name,
                text = "انضم ${user.name} إلى الغرفة 🎧",
                avatarUrl = user.avatarUrl,
                isSystem = true,
                eventType = "join"
            )
            firestoreService.sendMessage(room.id, joinMsg)
            showToast("تم الانضمام للغرفة وحفظ المعرف الصوتي!", ToastType.SUCCESS)
        }

        startRoomSync(room.id)
    }

    private fun startRoomSync(roomCode: String) {
        roomSyncJob?.cancel()
        roomAudioJob?.cancel()

        // Setup voice streaming callback
        var lastAudioSendTime = 0L
        voiceEngine.onAudioChunkCaptured = { pcmChunk, volume ->
            val user = _currentUser.value
            val room = _currentRoom.value
            val now = System.currentTimeMillis()
            if (user != null && room != null && room.id == roomCode) {
                // 1. Direct 0ms Local UDP transmission
                voiceEngine.udpRelay.sendUdpAudio(roomCode, user.id, pcmChunk)

                // 2. Cloud Server Stream
                if (now - lastAudioSendTime > 60) {
                    lastAudioSendTime = now
                    val base64 = voiceEngine.encodePcmToBase64(pcmChunk)
                    viewModelScope.launch(Dispatchers.IO) {
                        FirebaseRtdbService.broadcastRoomAudio(roomCode, user.id, user.name, base64, volume)
                    }
                }
            }
        }

        // Live Audio Stream Receiver: SSE Push + Fast Polling Fallback
        roomAudioJob = viewModelScope.launch(Dispatchers.IO) {
            val lastAudioTimestamps = mutableMapOf<String, Long>()

            launch(Dispatchers.IO) {
                while (isActive && _currentRoom.value?.id == roomCode) {
                    val user = _currentUser.value
                    val myId = user?.id ?: ""
                    try {
                        FirebaseRtdbService.listenRoomAudioSse(roomCode) { senderId, base64Data, _, ts ->
                            if (senderId != myId && voiceEngine.isConnected.value && !voiceEngine.isDeafened.value) {
                                val lastTs = lastAudioTimestamps[senderId] ?: 0L
                                if (ts > lastTs) {
                                    lastAudioTimestamps[senderId] = ts
                                    val pcm = voiceEngine.decodeBase64ToPcm(base64Data)
                                    if (pcm.isNotEmpty()) {
                                        voiceEngine.playIncomingAudio(pcm)
                                    }
                                }
                            }
                        }
                    } catch (_: Exception) {}
                    delay(1000)
                }
            }

            while (isActive && _currentRoom.value?.id == roomCode) {
                val user = _currentUser.value
                val myId = user?.id ?: ""
                try {
                    if (voiceEngine.isConnected.value && !voiceEngine.isDeafened.value) {
                        val streams = FirebaseRtdbService.fetchRoomAudioStreams(roomCode)
                        val now = System.currentTimeMillis()
                        val speakingUserIds = mutableSetOf<String>()

                        streams.forEach { (senderId, obj) ->
                            if (senderId != myId) {
                                val ts = obj.optLong("timestamp", 0L)
                                val lastTs = lastAudioTimestamps[senderId] ?: 0L
                                if (ts > lastTs && (now - ts) < 3000) {
                                    lastAudioTimestamps[senderId] = ts
                                    val base64Data = obj.optString("data", "")
                                    if (base64Data.isNotEmpty()) {
                                        val pcm = voiceEngine.decodeBase64ToPcm(base64Data)
                                        if (pcm.isNotEmpty()) {
                                            voiceEngine.playIncomingAudio(pcm)
                                            speakingUserIds.add(senderId)
                                        }
                                    }
                                }
                            }
                        }

                        val currentList = _roomUsers.value
                        if (currentList.isNotEmpty()) {
                            val updated = currentList.map { rUser ->
                                if (rUser.id != myId) {
                                    val isSpeaking = speakingUserIds.contains(rUser.id)
                                    if (rUser.isSpeaking != isSpeaking) rUser.copy(isSpeaking = isSpeaking) else rUser
                                } else {
                                    rUser
                                }
                            }
                            _roomUsers.value = updated
                        }
                    }
                } catch (_: Exception) {}
                delay(80)
            }
        }

        roomSyncJob = viewModelScope.launch {
            // 1. Observe Room Members in Real-time via Firestore
            launch {
                var previousMembers = emptyMap<String, FRoomMember>()
                firestoreService.observeRoomMembers(roomCode).collect { members ->
                    val myId = _currentUser.value?.id ?: ""
                    val currentMap = members.associateBy { it.userId }

                    if (previousMembers.isNotEmpty()) {
                        // User newly joined
                        val newlyJoined = members.filter { !previousMembers.containsKey(it.userId) && it.userId != myId }
                        newlyJoined.forEach { m ->
                            triggerRoomBanner(
                                RoomEventBanner(
                                    id = java.util.UUID.randomUUID().toString(),
                                    type = RoomEventType.JOIN,
                                    userId = m.userId,
                                    userName = m.name,
                                    avatarUrl = m.avatarUrl,
                                    message = "انضم ${m.name} إلى الغرفة 🟢"
                                )
                            )
                        }

                        // User left
                        val newlyLeft = previousMembers.values.filter { !currentMap.containsKey(it.userId) && it.userId != myId }
                        newlyLeft.forEach { m ->
                            triggerRoomBanner(
                                RoomEventBanner(
                                    id = java.util.UUID.randomUUID().toString(),
                                    type = RoomEventType.LEAVE,
                                    userId = m.userId,
                                    userName = m.name,
                                    avatarUrl = m.avatarUrl,
                                    message = "غادر ${m.name} الغرفة 🔴"
                                )
                            )
                        }
                    }
                    previousMembers = currentMap

                    if (members.isNotEmpty()) {
                        _roomUsers.value = members.map { it.toRoomUser() }
                    }
                }
            }

            // 2. Observe Messages in Real-time via Firestore
            launch {
                try {
                    firestoreService.observeMessages(roomCode).collect { fMessages ->
                        val user = _currentUser.value
                        val myId = user?.id ?: ""
                        _messages.value = fMessages.map { fMsg ->
                            ChatMessage(
                                id = fMsg.id,
                                roomCode = roomCode,
                                senderId = fMsg.senderId,
                                senderName = fMsg.senderName,
                                text = fMsg.text,
                                avatarUrl = fMsg.avatarUrl,
                                timestamp = fMsg.timestamp?.toDate()?.time ?: System.currentTimeMillis(),
                                isMe = fMsg.senderId == myId,
                                isSystem = fMsg.isSystem,
                                eventType = when (fMsg.eventType) {
                                    "join" -> RoomEventType.JOIN
                                    "leave" -> RoomEventType.LEAVE
                                    else -> null
                                },
                                time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(fMsg.timestamp?.toDate() ?: Date())
                            )
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.w("CybeNodeViewModel", "roomMessages observe error: ${e.message}")
                }
            }
        }
    }

    fun minimizeRoom() {
        if (_currentRoom.value != null) {
            _isRoomMinimized.value = true
            _currentScreen.value = Screen.DASHBOARD
        }
    }

    fun maximizeRoom() {
        _isRoomMinimized.value = false
        _currentScreen.value = Screen.ACTIVE_ROOM
    }

    fun leaveRoom() {
        val room = _currentRoom.value
        val user = _currentUser.value
        roomSyncJob?.cancel()
        roomSyncJob = null
        roomAudioJob?.cancel()
        roomAudioJob = null

        voiceEngine.disconnectVoice()

        if (room != null && user != null) {
            viewModelScope.launch {
                // Post leave announcement in Firestore messages
                val leaveMsg = FMessage(
                    senderId = user.id,
                    senderName = user.name,
                    text = "غادر ${user.name} الغرفة 🚪",
                    avatarUrl = user.avatarUrl,
                    isSystem = true,
                    eventType = "leave"
                )
                firestoreService.sendMessage(room.id, leaveMsg)
                firestoreService.leaveRoom(room.id, user.id)
                FirebaseRtdbService.leaveRoomUser(room.id, user.id)
                FirebaseRtdbService.clearRoomAudio(room.id, user.id)
            }
        }

        _isRoomMinimized.value = false
        _currentRoom.value = null
        _roomUsers.value = emptyList()
        _messages.value = emptyList()
        _currentScreen.value = Screen.DASHBOARD
        showToast("تمت مغادرة الغرفة", ToastType.INFO)
    }

    fun deleteCurrentRoom() {
        val room = _currentRoom.value ?: return
        viewModelScope.launch {
            firestoreService.deleteRoom(room.id)
            FirebaseRtdbService.deleteRoom(room.id)
            // Remove from saved rooms
            _mySavedRooms.value = _mySavedRooms.value.filter { it.id != room.id }
            leaveRoom()
            showToast("تم حذف الغرفة نهائياً", ToastType.SUCCESS)
        }
    }

    fun toggleVoiceConnection(hasAudioPermission: Boolean) {
        val isNowConnected = !voiceEngine.isConnected.value
        if (isNowConnected) {
            val room = _currentRoom.value
            val user = _currentUser.value
            val chName = if (room != null) "room_" + room.id else "darkgroup_global"
            voiceEngine.connectVoice(hasAudioPermission, chName, user?.id ?: "")
            showToast("متصل بالمحادثة الصوتية عبر WebRTC!", ToastType.SUCCESS)
        } else {
            voiceEngine.disconnectVoice()
            showToast("تم قطع الاتصال الصوتي", ToastType.INFO)
        }
        updateVoiceStateInFirebase()
    }

    fun toggleMicrophone() {
        val newMuted = voiceEngine.toggleMute()
        showToast(if (newMuted) "تم كتم الميكروفون" else "الميكروفون قيد التشغيل")
        updateVoiceStateInFirebase()
    }

    fun toggleAudioOutput() {
        val deafened = voiceEngine.toggleDeafen()
        showToast(if (deafened) "تم كتم صوت الغرفة" else "تم تفعيل صوت الغرفة")
    }

    private fun updateVoiceStateInFirebase() {
        val room = _currentRoom.value ?: return
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            FirebaseRtdbService.updateVoiceState(
                room.id,
                user.id,
                isVoice = voiceEngine.isConnected.value,
                isMuted = voiceEngine.isMuted.value
            )
        }
    }

    fun sendMessage(textInput: String) {
        val user = _currentUser.value ?: return
        val room = _currentRoom.value ?: return
        val text = textInput.trim()
        if (text.isEmpty()) return

        val msg = FMessage(
            senderId = user.id,
            senderName = user.name,
            text = text,
            avatarUrl = user.avatarUrl
        )

        viewModelScope.launch {
            firestoreService.sendMessage(room.id, msg)
        }
    }

    // PUBLIC FORUM
    fun startForumSync() {
        forumSyncJob?.cancel()
        forumSyncJob = viewModelScope.launch {
            try {
                firestoreService.observeForumMessages().collect { list ->
                    val myId = _currentUser.value?.id ?: ""
                    _forumMessages.value = list.map { fMsg ->
                        ChatMessage(
                            id = fMsg.id,
                            senderId = fMsg.senderId,
                            senderName = fMsg.senderName,
                            text = fMsg.text,
                            avatarUrl = fMsg.avatarUrl,
                            timestamp = fMsg.timestamp?.toDate()?.time ?: System.currentTimeMillis(),
                            isMe = fMsg.senderId == myId,
                            time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(fMsg.timestamp?.toDate() ?: Date())
                        )
                    }
                }
            } catch (e: Exception) {
                android.util.Log.w("CybeNodeViewModel", "forumSync error: ${e.message}")
            }
        }
    }

    fun stopForumSync() {
        forumSyncJob?.cancel()
        forumSyncJob = null
    }

    fun sendForumMessage(textInput: String) {
        val user = _currentUser.value ?: return
        val text = textInput.trim()
        if (text.isEmpty()) return

        val msg = FMessage(
            senderId = user.id,
            senderName = user.name,
            text = text,
            avatarUrl = user.avatarUrl
        )

        viewModelScope.launch {
            firestoreService.sendForumMessage(msg)
        }
    }

    fun deleteForumMessage(messageId: String) {
        viewModelScope.launch {
            firestoreService.deleteForumMessage(messageId)
            showToast("تم حذف رسالتك من المنتدى", ToastType.INFO)
        }
    }

    // FRIENDS & REQUESTS
    fun refreshFriendsAndRequests() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            _friends.value = FirebaseRtdbService.fetchFriends(user.id)
            _friendRequests.value = FirebaseRtdbService.fetchFriendRequests(user.username)

            // Refresh friends active rooms
            val allRooms = FirebaseRtdbService.fetchAllRooms()
            val friendIds = _friends.value.map { it.userId }
            _friendsRooms.value = allRooms.filter { friendIds.contains(it.hostId) }
        }
    }

    fun searchUser(query: String) {
        val clean = query.trim().lowercase().removePrefix("@")
        if (clean.isEmpty()) return
        viewModelScope.launch {
            _isSearchingUser.value = true
            val fUser = firestoreService.searchUserByUsername(clean)
            if (fUser != null) {
                _searchUserResult.value = fUser.toUser()
            } else {
                _searchUserResult.value = FirebaseRtdbService.getUser(clean)
            }
            _isSearchingUser.value = false
            if (_searchUserResult.value == null) {
                showToast("لم يتم العثور على مستخدم باسم @$clean", ToastType.INFO)
            }
        }
    }

    fun sendFriendRequestTo(targetUser: User) {
        val me = _currentUser.value ?: return
        if (targetUser.id == me.id || targetUser.username == me.username) {
            showToast("لا يمكنك إرسال طلب صداقة لنفسك!", ToastType.ERROR)
            return
        }

        viewModelScope.launch {
            val req = FriendRequest(
                id = "req_${System.currentTimeMillis()}",
                fromUserId = me.id,
                fromUsername = me.username,
                fromName = me.name,
                fromAvatarUrl = me.avatarUrl,
                toUsername = targetUser.username,
                timestamp = System.currentTimeMillis()
            )
            val success = FirebaseRtdbService.sendFriendRequest(req)
            if (success) {
                showToast("تم إرسال طلب الصداقة إلى ${targetUser.name}!", ToastType.SUCCESS)
            } else {
                showToast("تعذر إرسال الطلب، تحقق من الاتصال", ToastType.ERROR)
            }
        }
    }

    fun respondToFriendRequest(req: FriendRequest, accept: Boolean) {
        val me = _currentUser.value ?: return
        viewModelScope.launch {
            val fromUser = FirebaseRtdbService.getUserById(req.fromUserId) ?: User(
                id = req.fromUserId,
                username = req.fromUsername,
                name = req.fromName,
                avatarUrl = req.fromAvatarUrl
            )
            FirebaseRtdbService.respondFriendRequest(me, req, accept)
            _friendRequests.value = _friendRequests.value.filter { it.id != req.id }
            if (accept) {
                showToast("تم قبول طلب الصداقة من ${req.fromName}!", ToastType.SUCCESS)
                refreshFriendsAndRequests()
            } else {
                showToast("تم رفض طلب الصداقة", ToastType.INFO)
            }
        }
    }

    fun toggleBlockFriend(friend: Friend) {
        val me = _currentUser.value ?: return
        viewModelScope.launch {
            val newBlockState = !friend.isBlocked
            FirebaseRtdbService.setBlockUser(me.id, friend.userId, newBlockState)
            _friends.value = _friends.value.map {
                if (it.userId == friend.userId) it.copy(isBlocked = newBlockState) else it
            }
            showToast(if (newBlockState) "تم حظر المستخدم" else "تم إلغاء الحظر", ToastType.INFO)
            refreshFriendsAndRequests()
            refreshConversations()
        }
    }

    fun toggleBlockUser(user: User) {
        val me = _currentUser.value ?: return
        val currentlyBlocked = _friends.value.find { it.userId == user.id }?.isBlocked ?: false
        val newBlock = !currentlyBlocked
        viewModelScope.launch {
            FirebaseRtdbService.setBlockUser(me.id, user.id, newBlock)
            _friends.value = _friends.value.map {
                if (it.userId == user.id) it.copy(isBlocked = newBlock) else it
            }
            showToast(if (newBlock) "تم حظر ${user.name}" else "تم إلغاء حظر ${user.name}", ToastType.INFO)
            refreshFriendsAndRequests()
            refreshConversations()
        }
    }

    fun viewUserProfile(user: User) {
        _selectedUserProfile.value = user
        _currentScreen.value = Screen.USER_PROFILE
    }

    // PRIVATE DIRECT MESSAGING
    fun openDirectChat(otherUser: User) {
        _activeDirectUser.value = otherUser
        _currentScreen.value = Screen.DIRECT_CHAT
        startDirectMessagesSync()
    }

    private fun getConversationId(id1: String, id2: String): String {
        return if (id1 < id2) "${id1}_$id2" else "${id2}_$id1"
    }

    fun startDirectMessagesSync() {
        val me = _currentUser.value ?: return
        val other = _activeDirectUser.value ?: return
        val convId = getConversationId(me.id, other.id)

        directSyncJob?.cancel()
        directSyncJob = viewModelScope.launch {
            while (isActive && _currentScreen.value == Screen.DIRECT_CHAT) {
                try {
                    val list = FirebaseRtdbService.fetchPrivateMessages(convId, me.id)
                    _privateMessages.value = list

                    // Automatically mark incoming unread messages as read
                    val hasUnreadFromOther = list.any { !it.isMe && (!it.isRead || !it.isDelivered) }
                    if (hasUnreadFromOther) {
                        FirebaseRtdbService.markConversationMessagesAsRead(convId, me.id, other.id)
                    }

                    // Keep other user's presence updated in real-time
                    val presenceMap = FirebaseRtdbService.fetchAllPresence()
                    _usersPresence.value = presenceMap
                    val currentPresence = presenceMap[other.id]
                    if (currentPresence != null) {
                        _activeDirectUser.value = _activeDirectUser.value?.copy(
                            isOnline = currentPresence.isOnline,
                            lastActive = currentPresence.lastActive
                        )
                    }
                } catch (_: Exception) {}
                delay(1200)
            }
        }
    }

    fun sendDirectTextMessage(textInput: String) {
        val me = _currentUser.value ?: return
        val other = _activeDirectUser.value ?: return
        val text = textInput.trim()
        if (text.isEmpty()) return

        val convId = getConversationId(me.id, other.id)
        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        
        // Check if other user is currently online
        val otherPresence = _usersPresence.value[other.id]
        val isRecipientOnline = otherPresence?.isOnline == true

        val msg = PrivateChatMessage(
            id = "pmsg_" + System.currentTimeMillis() + "_" + (100..999).random(),
            conversationId = convId,
            senderId = me.id,
            senderName = me.name,
            text = text,
            isVoice = false,
            voiceDurationSec = 0,
            time = timeStr,
            timestamp = System.currentTimeMillis(),
            isMe = true,
            isDelivered = isRecipientOnline,
            isRead = false
        )

        _privateMessages.value = _privateMessages.value + msg

        viewModelScope.launch {
            FirebaseRtdbService.sendPrivateMessage(convId, msg, me, other)
            refreshConversations()
        }
    }

    fun refreshConversations() {
        val me = _currentUser.value ?: return
        viewModelScope.launch {
            _conversations.value = FirebaseRtdbService.fetchUserConversations(me.id)
        }
    }

    fun toggleBlockDirectUser() {
        val me = _currentUser.value ?: return
        val other = _activeDirectUser.value ?: return
        val friendObj = _friends.value.find { it.userId == other.id }
        val currentlyBlocked = friendObj?.isBlocked ?: false
        val newBlock = !currentlyBlocked
        viewModelScope.launch {
            FirebaseRtdbService.setBlockUser(me.id, other.id, newBlock)
            _friends.value = _friends.value.map {
                if (it.userId == other.id) it.copy(isBlocked = newBlock) else it
            }
            showToast(if (newBlock) "تم حظر ${other.name}" else "تم إلغاء حظر ${other.name}", ToastType.INFO)
            refreshFriendsAndRequests()
            refreshConversations()
        }
    }

    // 1-ON-1 REAL-TIME VOICE CALLS
    fun startVoiceCall(targetUser: User) {
        val me = _currentUser.value ?: return
        val callId = "call_${System.currentTimeMillis()}"
        val session = CallSession(
            id = callId,
            callerId = me.id,
            callerName = me.name,
            callerUsername = me.username,
            callerAvatar = me.avatarUrl,
            receiverId = targetUser.id,
            receiverName = targetUser.name,
            receiverUsername = targetUser.username,
            receiverAvatar = targetUser.avatarUrl,
            channelId = "call_${me.id}_${targetUser.id}",
            status = "ringing",
            timestamp = System.currentTimeMillis()
        )
        _activeCallSession.value = session
        _callUiState.value = CallUiState.OUTGOING_RINGING
        CallRingtoneManager.startRingbackTone()
        showToast("جاري الاتصال بـ ${targetUser.name}...", ToastType.INFO)

        viewModelScope.launch {
            FirebaseRtdbService.initiateVoiceCall(session)
            monitorOutgoingCall(callId)
        }
    }

    private fun monitorOutgoingCall(callId: String) {
        callPollingJob?.cancel()
        callPollingJob = viewModelScope.launch {
            var checks = 0
            while (isActive && _callUiState.value == CallUiState.OUTGOING_RINGING) {
                delay(1500)
                checks++
                val session = FirebaseRtdbService.fetchCallSession(callId)
                if (session == null || session.status == "declined" || session.status == "ended") {
                    CallRingtoneManager.stopAll(getApplication<Application>().applicationContext)
                    _callUiState.value = CallUiState.IDLE
                    _activeCallSession.value = null
                    showToast("تم إنهاء المكالمة أو رفضها", ToastType.INFO)
                    break
                } else if (session.status == "accepted") {
                    CallRingtoneManager.stopAll(getApplication<Application>().applicationContext)
                    _callUiState.value = CallUiState.CONNECTED
                    _activeCallSession.value = session
                    val me = _currentUser.value
                    voiceEngine.connectVoice(true, "call_" + callId, me?.id ?: "")
                    startCallTimer()
                    showToast("تم بدء المكالمة الصوتية عبر WebRTC!", ToastType.SUCCESS)
                    monitorConnectedCall(callId)
                    break
                }
                if (checks > 25) { // 37s timeout
                    CallRingtoneManager.stopAll(getApplication<Application>().applicationContext)
                    FirebaseRtdbService.updateCallStatus(callId, "ended")
                    _callUiState.value = CallUiState.IDLE
                    _activeCallSession.value = null
                    showToast("لا يوجد رد", ToastType.INFO)
                    break
                }
            }
        }
    }

    fun acceptIncomingCall() {
        val session = _activeCallSession.value ?: return
        val me = _currentUser.value
        CallRingtoneManager.stopAll(getApplication<Application>().applicationContext)
        viewModelScope.launch {
            FirebaseRtdbService.updateCallStatus(session.id, "accepted")
            _callUiState.value = CallUiState.CONNECTED
            voiceEngine.connectVoice(true, "call_" + session.id, me?.id ?: "")
            startCallTimer()
            showToast("تم الرد على المكالمة عبر WebRTC!", ToastType.SUCCESS)
            monitorConnectedCall(session.id)
        }
    }

    fun declineIncomingCall() {
        val session = _activeCallSession.value ?: return
        CallRingtoneManager.stopAll(getApplication<Application>().applicationContext)
        viewModelScope.launch {
            FirebaseRtdbService.updateCallStatus(session.id, "declined")
            _callUiState.value = CallUiState.IDLE
            _activeCallSession.value = null
            showToast("تم رفض المكالمة", ToastType.INFO)
        }
    }

    fun endVoiceCall() {
        val session = _activeCallSession.value
        val user = _currentUser.value
        CallRingtoneManager.stopAll(getApplication<Application>().applicationContext)
        callPollingJob?.cancel()
        callAudioJob?.cancel()
        callTimerJob?.cancel()
        _callDurationSec.value = 0
        voiceEngine.disconnectVoice()
        _callUiState.value = CallUiState.IDLE
        _activeCallSession.value = null
        if (session != null) {
            viewModelScope.launch {
                FirebaseRtdbService.updateCallStatus(session.id, "ended")
                if (user != null) {
                    FirebaseRtdbService.clearCallAudio(session.id, user.id)
                }
            }
        }
        showToast("تم إنهاء المكالمة", ToastType.INFO)
    }

    private fun monitorConnectedCall(callId: String) {
        callPollingJob?.cancel()
        callAudioJob?.cancel()

        val me = _currentUser.value
        if (me != null) {
            voiceEngine.udpRelay.start(callId, me.id)
            voiceEngine.udpRelay.onLocalAudioReceived = { pcm, _ ->
                voiceEngine.playIncomingAudio(pcm)
            }
        }

        // Setup call voice transmission
        var lastCallAudioSendTime = 0L
        voiceEngine.onAudioChunkCaptured = { pcmChunk, volume ->
            val user = _currentUser.value
            val now = System.currentTimeMillis()
            if (user != null && _callUiState.value == CallUiState.CONNECTED) {
                // 1. Direct UDP
                voiceEngine.udpRelay.sendUdpAudio(callId, user.id, pcmChunk)

                // 2. Cloud
                if (now - lastCallAudioSendTime > 60) {
                    lastCallAudioSendTime = now
                    val base64 = voiceEngine.encodePcmToBase64(pcmChunk)
                    viewModelScope.launch(Dispatchers.IO) {
                        FirebaseRtdbService.sendCallAudio(callId, user.id, base64, volume)
                    }
                }
            }
        }

        // Live Audio Receiver: SSE + Fast Polling
        callAudioJob = viewModelScope.launch(Dispatchers.IO) {
            var lastAudioTs = 0L

            // SSE listener
            launch(Dispatchers.IO) {
                while (isActive && _callUiState.value == CallUiState.CONNECTED) {
                    val user = _currentUser.value
                    val myId = user?.id ?: ""
                    try {
                        FirebaseRtdbService.listenCallAudioSse(callId) { senderId, base64Data, _, ts ->
                            if (senderId != myId && voiceEngine.isConnected.value && !voiceEngine.isDeafened.value) {
                                if (ts > lastAudioTs) {
                                    lastAudioTs = ts
                                    val pcm = voiceEngine.decodeBase64ToPcm(base64Data)
                                    if (pcm.isNotEmpty()) {
                                        voiceEngine.playIncomingAudio(pcm)
                                    }
                                }
                            }
                        }
                    } catch (_: Exception) {}
                    delay(1000)
                }
            }

            // Fallback loop
            while (isActive && _callUiState.value == CallUiState.CONNECTED) {
                val user = _currentUser.value
                val myId = user?.id ?: ""
                try {
                    if (voiceEngine.isConnected.value && !voiceEngine.isDeafened.value) {
                        val streams = FirebaseRtdbService.fetchCallAudio(callId)
                        val now = System.currentTimeMillis()
                        streams.forEach { (senderId, obj) ->
                            if (senderId != myId) {
                                val ts = obj.optLong("timestamp", 0L)
                                if (ts > lastAudioTs && (now - ts) < 3000) {
                                    lastAudioTs = ts
                                    val base64Data = obj.optString("data", "")
                                    if (base64Data.isNotEmpty()) {
                                        val pcm = voiceEngine.decodeBase64ToPcm(base64Data)
                                        if (pcm.isNotEmpty()) {
                                            voiceEngine.playIncomingAudio(pcm)
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (_: Exception) {}
                delay(80)
            }
        }

        callPollingJob = viewModelScope.launch {
            while (isActive && _callUiState.value == CallUiState.CONNECTED) {
                delay(1500)
                val session = FirebaseRtdbService.fetchCallSession(callId)
                if (session == null || session.status == "ended") {
                    endVoiceCall()
                    break
                }
            }
        }
    }

    private fun startCallTimer() {
        callTimerJob?.cancel()
        _callDurationSec.value = 0
        callTimerJob = viewModelScope.launch {
            while (isActive && _callUiState.value == CallUiState.CONNECTED) {
                delay(1000)
                _callDurationSec.value += 1
            }
        }
    }

    fun deletePrivateMessage(msg: PrivateChatMessage, forEveryone: Boolean) {
        val me = _currentUser.value ?: return
        val other = _activeDirectUser.value ?: return
        val convId = getConversationId(me.id, other.id)

        viewModelScope.launch {
            FirebaseRtdbService.deletePrivateMessage(convId, msg.id, forEveryone, me.id)
            if (forEveryone) {
                _privateMessages.value = _privateMessages.value.map {
                    if (it.id == msg.id) it.copy(deletedForEveryone = true, text = "تم حذف هذه الرسالة لدى الجميع") else it
                }
                showToast("تم حذف الرسالة لدى الجميع", ToastType.INFO)
            } else {
                _privateMessages.value = _privateMessages.value.filter { it.id != msg.id }
                showToast("تم حذف الرسالة لديك فقط", ToastType.INFO)
            }
        }
    }

    // BACKGROUND NOTIFICATION LISTENER FOR REQUESTS & MESSAGES & CALLS & ROOMS
    private fun startGlobalListener() {
        globalNotificationJob?.cancel()
        globalNotificationJob = viewModelScope.launch {
            var lastRequestCount = 0
            val notifiedMessagesMap = mutableMapOf<String, Long>()

            while (isActive) {
                val me = _currentUser.value ?: break
                val appContext = getApplication<Application>().applicationContext
                try {
                    // Update presence heartbeat for current user in Firestore
                    firestoreService.saveUser(FUser(
                        userId = me.id,
                        username = me.username,
                        name = me.name,
                        avatarUrl = me.avatarUrl,
                        isOnline = true,
                        lastActive = com.google.firebase.Timestamp.now()
                    ))

                    // Fetch global presence from legacy RTDB (still used for some features)
                    val presenceMap = FirebaseRtdbService.fetchAllPresence()
                    _usersPresence.value = presenceMap

                    // Check incoming call if not currently in call via Firestore
                    if (_callUiState.value == CallUiState.IDLE) {
                        // Use a one-time fetch or separate listener for incoming calls
                        // For simplicity in polling loop:
                        val incCalls = firestoreService.getIncomingCallsOnce(me.id)
                        val incCall = incCalls.firstOrNull()
                        if (incCall != null) {
                            val session = CallSession(
                                id = incCall.id,
                                callerId = incCall.callerId,
                                callerName = "صديق", // Need to fetch caller info if not in incCall
                                receiverId = me.id,
                                channelId = incCall.channelId,
                                status = incCall.status,
                                timestamp = incCall.timestamp?.toDate()?.time ?: System.currentTimeMillis()
                            )
                            _activeCallSession.value = session
                            _callUiState.value = CallUiState.INCOMING_RINGING
                            CallRingtoneManager.startIncomingRingtone(appContext)
                            NotificationHelper.showNotification(
                                appContext,
                                777,
                                "مكالمة صوتية واردة 📞",
                                "لديك مكالمة واردة في Dark Group"
                            )
                        }
                    }

                    val reqs = FirebaseRtdbService.fetchFriendRequests(me.username)
                    _friendRequests.value = reqs
                    if (reqs.size > lastRequestCount && lastRequestCount != 0) {
                        val latest = reqs.firstOrNull()
                        if (latest != null) {
                            NotificationHelper.showNotification(
                                appContext,
                                101,
                                "طلب صداقة جديد! 👥",
                                "أرسل لك ${latest.fromName} طلب صداقة جديد في Dark Group"
                            )
                        }
                    }
                    lastRequestCount = reqs.size

                    // Check friends list and conversations
                    _friends.value = FirebaseRtdbService.fetchFriends(me.id)
                    val newConversations = FirebaseRtdbService.fetchUserConversations(me.id)
                    _conversations.value = newConversations

                    newConversations.forEach { conv ->
                        if (conv.lastMessageIsRead) {
                            notifiedMessagesMap.remove(conv.id)
                        } else if (!conv.lastMessageIsMe && conv.lastTimestamp > 0L) {
                            val lastNotified = notifiedMessagesMap[conv.id] ?: 0L
                            if (conv.lastTimestamp > lastNotified) {
                                notifiedMessagesMap[conv.id] = conv.lastTimestamp
                                val inActiveChatWithHim = (_currentScreen.value == Screen.DIRECT_CHAT && _activeDirectUser.value?.id == conv.otherUserId)
                                if (!inActiveChatWithHim) {
                                    NotificationHelper.showNotification(
                                        appContext,
                                        (conv.id.hashCode() and 0x7FFFFFFF),
                                        "رسالة خاصة جديدة من ${conv.otherName} 💬",
                                        conv.lastMessage
                                    )
                                    showToast("رسالة جديدة من ${conv.otherName}: ${conv.lastMessage} 💬", ToastType.SUCCESS)
                                }
                            }
                        }
                    }
                } catch (_: Exception) {}
                delay(2000)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        CallRingtoneManager.stopAll(getApplication<Application>().applicationContext)
        voiceEngine.release()
        roomSyncJob?.cancel()
        roomAudioJob?.cancel()
        forumSyncJob?.cancel()
        directSyncJob?.cancel()
        globalNotificationJob?.cancel()
        callPollingJob?.cancel()
        callAudioJob?.cancel()
        callTimerJob?.cancel()
    }
}
