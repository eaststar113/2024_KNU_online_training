package com.example.wiselife

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.wiselife.databinding.ActivityHealthBinding
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HealthActivity : AppCompatActivity() {
    private var isScheduleVisible = false
    private var isEmotionVisible = false
    private var isHospitalVisible = false
    private val scheduledDates = mutableMapOf<String, MutableList<Pair<String, String>>>()
    private var currentSelectedDate: String? = null
    private lateinit var binding : ActivityHealthBinding
    private val emotionColors = mutableMapOf<String, Int>()

    companion object {
        const val EXTRA_MEDICINE_NAME = "medicine_name"
        const val EXTRA_SELECTED_TIME = "selected_time"
        const val EXTRA_MOOD_COLOR = "mood_color"
    }

    private val pillActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        Log.d("HealthActivity", "pillActivityLauncher called with result code: ${result.resultCode}")
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            if (data != null) {
                Log.d("HealthActivity", "Received data from HealthpillActivity")
                currentSelectedDate?.let { selectedDate ->
                    loadMedicineRecords(selectedDate)
                    findViewById<RelativeLayout>(R.id.llTodaySchedule).visibility = View.VISIBLE
                    isScheduleVisible = true
                }
            }
        }
    }

    private val healthCheckLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        Log.d("HealthActivity", "healthCheckLauncher called with result code: ${result.resultCode}")
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            if (data != null && currentSelectedDate != null) {
                val moodColor = data.getIntExtra(EXTRA_MOOD_COLOR, Color.TRANSPARENT)
                val stressLevel = data.getStringExtra("stress_level") ?: ""
                val symptoms = data.getStringArrayListExtra("symptoms") ?: arrayListOf()
                val sleepHours = data.getFloatExtra("sleep_hours", 0f)
                val waterIntake = data.getFloatExtra("water_intake", 0f)
                val exerciseTime = data.getFloatExtra("exercise_time", 0f)
                Log.d("HealthActivity", "Received Symptoms: ${symptoms.joinToString(",")}")

                emotionColors[currentSelectedDate!!] = moodColor
                Log.d("HealthActivity", "Emotion color map updated: $emotionColors")
                updateEmotionSchedule(MoodRecord(
                    date = currentSelectedDate!!,
                    moodColor = moodColor,
                    stressLevel = stressLevel,
                    symptoms = symptoms,
                    sleepHours = sleepHours,
                    waterIntake = waterIntake,
                    exerciseTime = exerciseTime
                ))
            }
            else {
                Log.d("HealthActivity", "Data or currentSelectedDate is null")
            }
        }
        else {
            Log.d("HealthActivity", "Result is not OK")
        }
    }

    private val hospitalActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            if (data != null && data.getBooleanExtra("reservationSaved", false)) {
                currentSelectedDate?.let { selectedDate ->
                    loadHospitalRecords(selectedDate)
                    findViewById<RelativeLayout>(R.id.llhospitalSchedule).visibility = View.VISIBLE
                    isScheduleVisible = true
                }
            }
        }
    }

    private fun saveMedicineSchedule(selectedDate: String, time: String, name: String) {
        Log.d("HealthActivity", "saveMedicineSchedule: $selectedDate, $time, $name")
        val sharedPreferences = getSharedPreferences("MedicineSchedule", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val standardizedTime = convertToStandardTimeFormat(time)
        val scheduleKey = "$selectedDate:$standardizedTime"
        editor.putString(scheduleKey, name)
        editor.apply()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("HealthActivity", "onCreate called")
        binding = ActivityHealthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.calendarView.visibility = View.VISIBLE
        binding.calendarViewEmotion.visibility = View.GONE
        binding.calendarViewMood.visibility = View.GONE

        binding.llUpcomingSchedule.visibility = View.VISIBLE
        binding.llTodaySchedule.visibility = View.GONE
        binding.llemotionSchedule.visibility = View.GONE
        binding.llhospitalSchedule.visibility = View.GONE

        binding.btnGeneral.setOnClickListener {
            binding.calendarView.visibility = View.VISIBLE
            binding.calendarViewEmotion.visibility = View.GONE
            binding.calendarViewMood.visibility = View.GONE

            binding.llemotionSchedule.visibility = View.GONE
            binding.llhospitalSchedule.visibility = View.GONE
            if (isScheduleVisible) {
                binding.llTodaySchedule.visibility = View.VISIBLE
            } else {
                binding.llTodaySchedule.visibility = View.GONE
            }
        }

        binding.btnEmotion.setOnClickListener {
            binding.calendarView.visibility = View.GONE
            binding.calendarViewEmotion.visibility = View.VISIBLE
            binding.calendarViewMood.visibility = View.GONE

            binding.llTodaySchedule.visibility = View.GONE
            binding.llhospitalSchedule.visibility = View.GONE
            if (isEmotionVisible) {
                binding.llemotionSchedule.visibility = View.VISIBLE
            } else {
                binding.llemotionSchedule.visibility = View.GONE
            }
        }

        binding.btnMood.setOnClickListener {
            binding.calendarView.visibility = View.GONE
            binding.calendarViewEmotion.visibility = View.GONE
            binding.calendarViewMood.visibility = View.VISIBLE

            binding.llTodaySchedule.visibility = View.GONE
            binding.llemotionSchedule.visibility = View.GONE
            if (isHospitalVisible) {
                binding.llhospitalSchedule.visibility = View.VISIBLE
            } else {
                binding.llhospitalSchedule.visibility = View.GONE
            }
        }

        binding.llUpcomingSchedule.visibility = View.VISIBLE
        binding.llTodaySchedule.visibility = View.GONE

        //선택한 날짜 표기
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = String.format("%d-%02d-%02d", year, month + 1, dayOfMonth)
            Log.d("HealthActivity", "Date selected: $selectedDate")
            currentSelectedDate = selectedDate

            loadMedicineRecords(selectedDate)
            if (isScheduleVisible) {
                binding.llTodaySchedule.visibility = View.VISIBLE
            } else {
                binding.llTodaySchedule.visibility = View.GONE
            }
        }

        binding.calendarViewEmotion.setOnDateChangedListener { widget, date, selected ->
            val selectedDate = "${date.year}-${date.month}-${date.day}"
            Log.d("HealthActivity", "Emotion Calendar Date selected: $selectedDate")
            currentSelectedDate = selectedDate
            loadMoodRecords(selectedDate)
        }

        binding.calendarViewMood.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = String.format("%d-%02d-%02d", year, month + 1, dayOfMonth)
            Log.d("HealthActivity", "Mood Calendar Date selected: $selectedDate")
            currentSelectedDate = selectedDate

            // currentSelectedDate가 null인지 확인하고 처리
            if (currentSelectedDate != null) {
                loadHospitalRecords(currentSelectedDate!!)
                if (isHospitalVisible) {
                    binding.llhospitalSchedule.visibility = View.VISIBLE
                } else {
                    binding.llhospitalSchedule.visibility = View.GONE
                }
            } else {
                Log.e("HealthActivity", "currentSelectedDate is null")
            }
        }

        scheduledDates["2024-08-15"] = mutableListOf(Pair("11:00", "병원 방문"))
        scheduledDates["2024-08-20"] = mutableListOf(Pair("16:00", "약 섭취"))

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

        val healthPillCheck = findViewById<Button>(R.id.btnMedicineRecord)
        healthPillCheck.setOnClickListener {
            val intent = Intent(this, HealthpillActivity::class.java)
            intent.putExtra("selected_date", currentSelectedDate)
            pillActivityLauncher.launch(intent)
        }
        val healthCheck = findViewById<Button>(R.id.btnHealthRecord)
        healthCheck.setOnClickListener {
            val intent = Intent(this, HealthcheckActivity::class.java)
            intent.putExtra("selected_date", currentSelectedDate)
            healthCheckLauncher.launch(intent)
        }
        val healthHospitalCheck = findViewById<Button>(R.id.btnHospitalRecord)
        healthHospitalCheck.setOnClickListener {
            val intent = Intent(this, HealthHospitalActivity::class.java)
            intent.putExtra("selectedDate", currentSelectedDate)
            hospitalActivityLauncher.launch(intent)
        }
    }

    private fun loadMedicineRecords(date: String) {
        Log.d("HealthActivity", "loadMedicineRecords called for date: $date")
        val db = AppDatabase.getDatabase(applicationContext)

        lifecycleScope.launch(Dispatchers.IO) {
            val medicineRecords = db.medicineDao().getMedicineRecordsByDate(date)
            Log.d("HealthActivity", "Medicine records retrieved: ${medicineRecords.size}")

            withContext(Dispatchers.Main) {
                updateScheduleList(medicineRecords)
                if (medicineRecords.isNotEmpty()) {
                    binding.llTodaySchedule.visibility = View.VISIBLE
                    isScheduleVisible = true
                } else {
                    binding.llTodaySchedule.visibility = View.GONE
                    isScheduleVisible = false
                }
            }
        }
    }

    private fun loadMoodRecords(date: String) {
        Log.d("HealthActivity", "loadMoodRecords called for date: $date")
        val db = AppDatabase.getDatabase(applicationContext)

        lifecycleScope.launch(Dispatchers.IO) {
            val moodRecord = db.moodDao().getHealthCheckRecordsByDate(date)
            Log.d("HealthActivity", "Mood record retrieved: $moodRecord")

            withContext(Dispatchers.Main) {
                if (moodRecord.isNotEmpty()) {
                    updateEmotionSchedule(moodRecord[0])
                    binding.llemotionSchedule.visibility = View.VISIBLE
                } else {
                    binding.llemotionSchedule.visibility = View.GONE
                }
            }
        }
    }

    private fun updateEmotionSchedule(record: MoodRecord) {
        val emotionTextView = findViewById<TextView>(R.id.llEmotionScheduleItems)

        val moodDescription = when (record.moodColor) {
            Color.RED -> "기분이 나빴어요"
            Color.YELLOW -> "기분이 보통이었어요"
            Color.GREEN -> "기분이 좋았어요"
            else -> "기록이 없어요"
        }

        val symptomsDescription = record.symptoms.joinToString(", ") { it }

        emotionTextView.text = """
        날짜: ${record.date}
        기분: $moodDescription
        스트레스 수준: ${record.stressLevel}
        증상: $symptomsDescription
        수면시간: ${record.sleepHours}시간
        물 섭취량: ${record.waterIntake}%
        운동 시간: ${record.exerciseTime}시간
    """.trimIndent()

        binding.llemotionSchedule.visibility = View.VISIBLE
    }

    private fun loadHospitalRecords(date: String) {
        Log.d("HealthActivity", "loadHospitalRecords called for date: $date")
        val db = AppDatabase.getDatabase(applicationContext)

        lifecycleScope.launch(Dispatchers.IO) {
            val hospitalRecords = db.hospitalReservationDao().getReservationsByDate(date)
            Log.d("HealthActivity", "Loaded hospital records: $hospitalRecords")
            withContext(Dispatchers.Main) {
                updateHospitalScheduleList(hospitalRecords)
                if (hospitalRecords.isNotEmpty()) {
                    binding.llhospitalSchedule.visibility = View.VISIBLE
                    isScheduleVisible = true
                } else {
                    binding.llhospitalSchedule.visibility = View.GONE
                    isScheduleVisible = false
                }
            }
        }
    }

    private fun updateHospitalScheduleList(hospitalRecords: List<HospitalReservation>) {
        val hospitalScheduleTextView = findViewById<TextView>(R.id.llHospitalScheduleItems)
        val scheduleText = StringBuilder()

        for (record in hospitalRecords) {
            scheduleText.append("${record.reservationTime} ${record.hospitalName} 방문\n")
        }

        hospitalScheduleTextView.text = scheduleText.toString().trim()
    }

    private fun updateScheduleList(medicineRecords: List<MedicineRecord>) {
        val scheduleTextView = findViewById<TextView>(R.id.llTodayScheduleItems)
        val scheduleText = StringBuilder()
        //val medicineList = scheduledDates[selectedDate] ?: return
        for (record in medicineRecords) {
            scheduleText.append("${record.time} ${record.name} 복용\n")
        }

        scheduleTextView.text = scheduleText.toString().trim()
    }
    private fun convertToStandardTimeFormat(time: String): String {
        return time.replace("시", ":").replace("분", "").trim()
    }
}

