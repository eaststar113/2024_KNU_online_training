package com.example.wiselife

import android.app.Activity
import android.content.Intent
import android.app.TimePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.lifecycleScope
import com.example.wiselife.databinding.ActivityHealthpillBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class HealthpillActivity : AppCompatActivity() {
    private lateinit var tvTime: TextView
    private var selectedHour: Int = 0
    private var selectedMinute: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityHealthpillBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "약 기록"

        tvTime = findViewById(R.id.tv_time)

        // 초기 시간 표시
        tvTime.text = String.format("%02d시 %02d분", selectedHour, selectedMinute)

        // 시간 선택 텍스트뷰 클릭 리스너
        tvTime.setOnClickListener {
            showTimePickerDialog()
        }

        val btnSubmit = findViewById<AppCompatButton>(R.id.submit_button)

        btnSubmit.setOnClickListener {
            val medicineName = findViewById<EditText>(R.id.medicine_name).text.toString()
            val selectedTime = findViewById<TextView>(R.id.tv_time).text.toString()
            val selectedDate = intent.getStringExtra("selected_date") ?: ""

            if (medicineName.isNotEmpty() && selectedTime != "시 분" && selectedDate.isNotEmpty()) {
                saveMedicineRecord(selectedDate, selectedTime, medicineName)
                finish()
            } else {
                Toast.makeText(this, "약 이름과 시간을 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
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
    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish() // 현재 액티비티를 종료하여 이전 액티비티로 돌아감
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveMedicineRecord(date: String, time: String, name: String) {
        val medicineRecord = MedicineRecord(date = date, time = time, name = name)
        val db = AppDatabase.getDatabase(applicationContext)

        lifecycleScope.launch(Dispatchers.IO) {
            db.medicineDao().insertMedicineRecord(medicineRecord)

            Log.d("HealthpillActivity", "Saved Record: Date=$date, Time=$time, Name=$name")
        }
    }

    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(this, { _, hourOfDay, minute ->
            // 시간과 분 모두 갱신
            selectedHour = hourOfDay
            selectedMinute = minute
            tvTime.text = String.format("%02d시 %02d분", selectedHour, selectedMinute)
        }, selectedHour, selectedMinute, true)

        timePickerDialog.show()
    }
}