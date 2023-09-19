package tranhph26979.fpoly.app_bao_thuc

import androidx.lifecycle.Observer
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.CheckBox
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.activity_main.*
import tranhph26979.fpoly.app_bao_thuc.BroadcastReciever.AlarmReceiver
import tranhph26979.fpoly.app_bao_thuc.Service.AlarmService
import tranhph26979.fpoly.app_bao_thuc.ViewModel.AlarmViewModel
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var timePicker: TimePicker
    private lateinit var checkBox: CheckBox
    private lateinit var tvgio: TextView
    private lateinit var alarmViewModel: AlarmViewModel
    private lateinit var sharedPreferences: SharedPreferences

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvgio = findViewById(R.id.tvgio)

        btnsetgio.setOnClickListener {
            showTimePickerDialog()
        }

        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        alarmViewModel = ViewModelProvider(this).get(AlarmViewModel::class.java)

        alarmViewModel.alarmTime.observe(this, Observer { time ->
            tvgio.text = time
            saveAlarmTime(time)
            if (tvgio.text == "" && alarmViewModel.alarmTime.value == "") {
                tvgio.text = "00:00"
            }
        })

        alarmViewModel.isRepeat.observe(this, Observer { repeat ->
            checkBox.isChecked = repeat
            saveIsCheckboxChecked(repeat)

        })

        val savedAlarmTime = getSavedAlarmTime()
        tvgio.text = savedAlarmTime
        alarmViewModel.alarmTime.value = savedAlarmTime

        btnreset.setOnClickListener() {
            sharedPreferences.edit().clear().apply()
            if (!tvgio.text.isNullOrEmpty() && !alarmViewModel.alarmTime.value.isNullOrEmpty()) {
                tvgio.text = ""
                alarmViewModel.alarmTime.value = ""
            }
            if (alarmViewModel.isRepeat.value == true) {
                alarmViewModel.isRepeat.value = false
                val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val alarmIntent = Intent(this, AlarmReceiver::class.java)
                val requestCode = 0
                val pendingIntent = PendingIntent.getBroadcast(
                    applicationContext,
                    requestCode,
                    alarmIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
                alarmManager.cancel(pendingIntent)

            }
            Toast.makeText(this, "Reset successful", Toast.LENGTH_SHORT).show()
        }


    }

    private fun getSavedAlarmTime(): String {
        return sharedPreferences.getString("AlarmTime", "") ?: ""
    }


    private fun saveAlarmTime(time: String) {
        sharedPreferences.edit().putString("AlarmTime", time).apply()
    }

    private fun getIsCheckboxChecked(): Boolean {
        return sharedPreferences.getBoolean("checkboxKey", false)
    }

    private fun saveIsCheckboxChecked(isChecked: Boolean) {
        sharedPreferences.edit().putBoolean("checkboxKey", isChecked).apply()
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val dialogview = LayoutInflater.from(this).inflate(R.layout.timepicker_dialog, null)
        timePicker = dialogview.findViewById(R.id.timepicker)
        checkBox = dialogview.findViewById(R.id.checklap)
        checkBox.isChecked = getIsCheckboxChecked()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        timePicker.setIs24HourView(true)
        timePicker.hour = currentHour
        timePicker.minute = currentMinute

        val timeSetListener = TimePicker.OnTimeChangedListener { _, hourOfDay, minute ->
            val timeString = String.format("%02d:%02d", hourOfDay, minute)
            alarmViewModel.alarmTime.value = timeString
        }

        timePicker.setOnTimeChangedListener(timeSetListener)

        val timePickerDialogBuilder = androidx.appcompat.app.AlertDialog.Builder(this)
        timePickerDialogBuilder.setView(dialogview)
        timePickerDialogBuilder.setPositiveButton("OK") { _, _ ->
            val hour = timePicker.hour
            val minute = timePicker.minute
            scheduleAlarm(hour, minute)
            val timeString = String.format("%02d:%02d", hour, minute)
            tvgio.text = timeString
            saveAlarmTime(timeString)
            alarmViewModel.alarmTime.value = timeString


        }
        timePickerDialogBuilder.setNegativeButton("Cancel") { _, _ ->
        }

        checkBox.setOnCheckedChangeListener() { _, isChecked ->
            alarmViewModel.isRepeat.value = isChecked
            saveIsCheckboxChecked(isChecked)
        }
        val timePickerDialog = timePickerDialogBuilder.create()
        timePickerDialog.show()

    }

    private fun scheduleAlarm(hourOfDay: Int, minute: Int) {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("isRepeat", alarmViewModel.isRepeat.value ?: false)
        }
        val requestCode = 0
        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            requestCode,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }

        Toast.makeText(this, "Alarm is set", Toast.LENGTH_SHORT).show()
    }
}