package tranhph26979.fpoly.app_bao_thuc.Service

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import tranhph26979.fpoly.app_bao_thuc.MainActivity
import tranhph26979.fpoly.app_bao_thuc.R

class AlarmService : Service() {
    private var isPlaying = false
    private var mediaPlayer: MediaPlayer? = null
    private val CHANNEL_ID = "AlarmChannel"
    private val NOTIFICATION_ID = 1
    private var notification: Notification? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "StopAlarm") {
            if (isPlaying) {
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null
                isPlaying = false
            }
            stopForeground(true)
            stopSelf()
        } else if (intent?.action == "StartAlarm") {
            if (!isPlaying) {
                playAlarm()
                isPlaying = true
            }
        }

        createNotificationChannel()
        notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alarm Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = Intent(this, AlarmService::class.java)
        stopIntent.action = "StopAlarm"
        val stopPendingIntent =
            PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_ONE_SHOT)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Alarm")
            .setContentText("dậy đi cu!")
            .setSmallIcon(R.drawable.ic_baseline_alarm_24)
            .setContentIntent(pendingIntent)
            .setOngoing(isPlaying)
            .addAction(R.drawable.ic_baseline_stop_24, "Stop", stopPendingIntent)
            .build()

        return notification
    }

    private fun playAlarm() {
        mediaPlayer = MediaPlayer.create(this, R.raw.dayde)
        mediaPlayer!!.isLooping = true
        mediaPlayer!!.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mediaPlayer != null) {
            if (isPlaying) {
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null
            }
        }
    }
}