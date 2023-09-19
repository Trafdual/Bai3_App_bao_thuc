package tranhph26979.fpoly.app_bao_thuc.BroadcastReciever

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.SystemClock
import android.preference.PreferenceManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import tranhph26979.fpoly.app_bao_thuc.R
import tranhph26979.fpoly.app_bao_thuc.Service.AlarmService

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val isRepeat = intent?.getBooleanExtra("isRepeat", false)
            if (isRepeat == true) {
                val isCheckboxChecked = sharedPreferences.getBoolean("checkboxKey", true)
                if (isCheckboxChecked) {
                    Toast.makeText(context, "Alarm is triggered (repeat)", Toast.LENGTH_SHORT)
                        .show()
                    val alarmManager =
                        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
                    val triggerTime = SystemClock.elapsedRealtime() + (5 * 60 * 1000)
                    alarmManager.setExact(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    Toast.makeText(
                        context,
                        "Alarm is triggered (non-repeat)",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(context, "ko chạy", Toast.LENGTH_SHORT).show()
            }
        }
        val serviceIntent = Intent(context, AlarmService::class.java)
        serviceIntent.action = "StartAlarm"
        ContextCompat.startForegroundService(context!!, serviceIntent)
    }

}


