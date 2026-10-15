package com.example.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.room.Room
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.remote.FirebaseRtdbService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class GroupCallService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var lastActiveCallId: String? = null

    companion object {
        private const val TAG = "GroupCallService"
        private const val FOREGROUND_NOTIF_ID = 8888
        private const val INCOMING_CALL_NOTIF_ID = 9999
        private const val CHANNEL_ID = "group_background_service_channel"
        private const val CHANNEL_NAME = "Dark Group Connection Channel"
        private const val CALL_CHANNEL_ID = "dark_group_calls_channel"
        private const val CALL_CHANNEL_NAME = "Dark Group Calls"

        fun startService(context: Context) {
            try {
                val intent = Intent(context, GroupCallService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start GroupCallService", e)
            }
        }

        fun stopService(context: Context) {
            try {
                val intent = Intent(context, GroupCallService::class.java)
                context.stopService(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to stop GroupCallService", e)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "GroupCallService created")
        createNotificationChannels()
        
        val notification = createForegroundNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                FOREGROUND_NOTIF_ID, 
                notification, 
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                FOREGROUND_NOTIF_ID, 
                notification, 
                ServiceInfo.FOREGROUND_SERVICE_TYPE_NONE
            )
        } else {
            startForeground(FOREGROUND_NOTIF_ID, notification)
        }
        
        startBackgroundMonitoring()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "GroupCallService started command")
        return START_STICKY
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Background connection channel (low importance so it doesn't make sound/annoy user)
            val connectionChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "Dark Group background active connection"
            }
            manager.createNotificationChannel(connectionChannel)

            // High priority call channel (importance high/max for full screen and sound)
            val callChannel = NotificationChannel(
                CALL_CHANNEL_ID,
                CALL_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Dark Group calling notifications"
                enableVibration(true)
                setBypassDnd(true)
            }
            manager.createNotificationChannel(callChannel)
        }
    }

    private fun createForegroundNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Dark Group متصل 🔒")
            .setContentText("مشفر وآمن في الخلفية لتلقي المكالمات")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true)
            .build()
    }

    private fun startBackgroundMonitoring() {
        serviceScope.launch {
            val db = Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java,
                "darkgroup_db"
            ).fallbackToDestructiveMigration().build()
            while (isActive) {
                try {
                    val cachedUser = db.userDao().getCurrentUser()
                    if (cachedUser != null) {
                        val activeCall = FirebaseRtdbService.fetchActiveIncomingCall(cachedUser.id)
                        if (activeCall != null) {
                            if (lastActiveCallId != activeCall.id) {
                                lastActiveCallId = activeCall.id
                                Log.d(TAG, "Incoming call detected in background: ${activeCall.id} from ${activeCall.callerName}")
                                triggerIncomingCall(activeCall.id, activeCall.callerName)
                            }
                        } else {
                            // No active incoming call
                            if (lastActiveCallId != null) {
                                Log.d(TAG, "Call ended or dismissed: $lastActiveCallId")
                                CallRingtoneManager.stopAll(applicationContext)
                                val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                                manager.cancel(INCOMING_CALL_NOTIF_ID)
                                lastActiveCallId = null
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in background call monitoring", e)
                }
                delay(2000)
            }
        }
    }

    private fun triggerIncomingCall(callId: String, callerName: String) {
        // 1. Play Ringtone and Vibrate
        CallRingtoneManager.startIncomingRingtone(applicationContext)

        // 2. Build High Priority full-screen intent Notification
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            1010,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CALL_CHANNEL_ID)
            .setContentTitle("مكالمة واردة من $callerName 📞")
            .setContentText("اضغط للرد أو الرفض")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true) // Crucial to wake screen and pop up full-screen CallOverlay
            .setAutoCancel(true)
            .setOngoing(true)

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(INCOMING_CALL_NOTIF_ID, builder.build())
    }

    override fun onDestroy() {
        Log.d(TAG, "GroupCallService destroyed")
        CallRingtoneManager.stopAll(applicationContext)
        serviceScope.cancel()
        super.onDestroy()
    }
}
