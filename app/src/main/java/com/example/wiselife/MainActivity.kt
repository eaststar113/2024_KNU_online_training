package com.example.wiselife

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.example.wiselife.databinding.ActivityHealthBinding
import com.example.wiselife.databinding.ActivityKioskPracticeBinding
import com.example.wiselife.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val currentDate = getCurrentDate()
        binding.tvGreetingTime.text = currentDate

        lifecycleScope.launch(Dispatchers.IO) {
            val latestRecord = AppDatabase.getDatabase(applicationContext).medicineDao().getLatestMedicineRecord()
            withContext(Dispatchers.Main) {
                updateMedicineScheduleUI(latestRecord)
            }
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val latestReservation = AppDatabase.getDatabase(applicationContext).hospitalReservationDao().getLatestReservation()
            withContext(Dispatchers.Main) {
                updateHospitalScheduleUI(latestReservation)
            }
        }

        binding.bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.activity_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.activity_health -> {
                    val intent = Intent(this, HealthActivity::class.java)
                    //healthActivityLauncher.launch(intent)
                    startActivity(intent)
                    true
                }

                R.id.activity_learning -> {
                    val intent = Intent(this, LearningActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.activiy_chatbot -> {
                    val intent = Intent(this, ChatbotActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy.MM.dd.(EEE)", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun updateMedicineScheduleUI(record: MedicineRecord?) {
        val medicationTimeTextView = findViewById<TextView>(R.id.tvMedicationTime)
        val countdownTextView = findViewById<TextView>(R.id.tvMedicationCountdown)

        if (record != null) {
            medicationTimeTextView.text = "${record.time} ${record.name} 복용"
            val remainingTime = calculateRemainingTime(record.time)
            countdownTextView.text = remainingTime
        } else {
            medicationTimeTextView.text = "일정 없음"
            countdownTextView.text = ""
        }
    }

    private fun updateHospitalScheduleUI(record: HospitalReservation?) {
        val hospitalTimeTextView = findViewById<TextView>(R.id.tvHospitalTime)
        val countdownhosTextView = findViewById<TextView>(R.id.tvHospitalCountdown)

        if (record != null) {
            hospitalTimeTextView.text = "${record.reservationTime} ${record.hospitalName} 예약"
            val remainingTime = calculateRemainingTime(record.reservationTime)
            countdownhosTextView.text = remainingTime
        } else {
            hospitalTimeTextView.text = "일정 없음"
            countdownhosTextView.text = ""
        }
    }

    private fun calculateRemainingTime(time: String): String {
        // 두 가지 시간 형식을 파싱하기 위한 정규식 패턴
        val timePattern24Hour = Regex("\\d{2}:\\d{2}")  // "HH:mm" 형식
        val timePatternKorean = Regex("(\\d{1,2})시\\s*(\\d{2})분")  // "HH시 mm분" 형식

        val match24Hour = timePattern24Hour.find(time)
        val matchKorean = timePatternKorean.find(time)

        // 추출된 시간 문자열
        val parsedTime: String? = when {
            match24Hour != null -> match24Hour.value
            matchKorean != null -> {
                val (hour, minute) = matchKorean.destructured
                String.format("%02d:%02d", hour.toInt(), minute.toInt())
            }
            else -> null
        }

        return if (parsedTime != null) {
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            try {
                // 현재 날짜와 시간을 기준으로 설정된 targetTime을 파싱
                val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val targetTime = format.parse("$currentDate $parsedTime")
                val currentTime = Calendar.getInstance().time
                val diff = targetTime.time - currentTime.time

                if (diff > 0) {
                    val hours = diff / (1000 * 60 * 60)
                    val minutes = (diff / (1000 * 60)) % 60
                    String.format("%02d시간 %02d분 남음", hours, minutes)
                } else {
                    "시간 초과"
                }
            } catch (e: ParseException) {
                "시간 계산 오류"
            }
        } else {
            "시간 형식 오류"
        }
    }
}