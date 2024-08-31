package com.example.wiselife

import android.app.TimePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.wiselife.databinding.ActivityHealthHospitalBinding
import com.example.wiselife.databinding.ActivityHealthcheckBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class HealthHospitalActivity : AppCompatActivity() {
    private lateinit var database: AppDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityHealthHospitalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(this)

        val selectedDate = intent.getStringExtra("selectedDate")
        Log.d("HealthHospitalActivity", "Received selectedDate: $selectedDate")

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "병원 기록"

        binding.submitButton.setOnClickListener {
            val hospitalName = binding.hospitalName.text.toString()
            val reservationTime = binding.reservationTime.text.toString()
            Log.d("HealthHospitalActivity", "Submitting reservation: Name = $hospitalName, Time = $reservationTime, Date = $selectedDate")

            if (hospitalName.isNotEmpty() && reservationTime.isNotEmpty() && selectedDate != null) {
                val reservation = HospitalReservation(
                    hospitalName = hospitalName,
                    reservationTime = reservationTime,
                    reservationDate = selectedDate
                )

                // 데이터베이스 작업을 IO 스레드에서 수행
                lifecycleScope.launch(Dispatchers.IO) {
                    database.hospitalReservationDao().insertReservation(reservation)
                    // UI 업데이트는 Main 스레드에서 수행
                    withContext(Dispatchers.Main) {
                        Log.d("HealthHospitalActivity", "Data saved to Room DB: $reservation")
                        Toast.makeText(this@HealthHospitalActivity, "예약이 저장되었습니다.", Toast.LENGTH_SHORT).show()
                        finish() // 데이터 저장 후 액티비티 종료
                    }
                }
            } else {
                Toast.makeText(this, "모든 필드를 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.reservationTime.setOnClickListener {
            showTimePickerDialog()
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
    private fun showTimePickerDialog() {
        // 현재 시간 설정
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        // TimePickerDialog 생성
        val timePickerDialog = TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                // 선택한 시간 업데이트
                val timeString = String.format("%02d:%02d", selectedHour, selectedMinute)
                findViewById<TextView>(R.id.reservation_time).text = timeString
            },
            hour,
            minute,
            true // 24시간 형식으로 표시
        )

        timePickerDialog.show()
    }
    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}