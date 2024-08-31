package com.example.wiselife

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.TextView
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.wiselife.databinding.ActivityHealthcheckBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HealthcheckActivity : AppCompatActivity() {
    private var selectedMoodColor: Int = Color.TRANSPARENT
    private var selectedDate: String? = null
    private var stressLevel: String = ""
    private var symptoms: List<String> = emptyList()
    private var sleepHours: Float = 0f
    private var waterIntake: Float = 0f
    private var exerciseTime: Float = 0f
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityHealthcheckBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "건강 기록"

        binding.stressLow.setOnClickListener {
            stressLevel = "낮음"
            Log.d("HealthcheckActivity", "Stress Level: 낮음")
        }

        binding.stressMedium.setOnClickListener {
            stressLevel = "보통"
            Log.d("HealthcheckActivity", "Stress Level: 보통")
        }

        binding.stressHigh.setOnClickListener {
            stressLevel = "높음"
            Log.d("HealthcheckActivity", "Stress Level: 높음")
        }

        //프로그레스바 3개
        val sleepTimeSlider = findViewById<SeekBar>(R.id.sleep_time_slider)
        val sleepTimeValue = findViewById<TextView>(R.id.sleep_time_value)
        sleepTimeValue.text = "${sleepTimeSlider.progress}시간"

        sleepTimeSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                sleepTimeValue.text = "${progress}시간"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        val waterTimeSlider = findViewById<SeekBar>(R.id.water_intake_slider)
        val waterTimeValue = findViewById<TextView>(R.id.water_time_value)
        waterTimeValue.text = "${waterTimeSlider.progress}%(퍼센트)"

        waterTimeSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                waterTimeValue.text = "${progress}%(퍼센트)"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        val exerciseTimeSlider = findViewById<SeekBar>(R.id.exercise_time_slider)
        val exerciseTimeValue = findViewById<TextView>(R.id.exercise_time_value)
        exerciseTimeValue.text = "${exerciseTimeSlider.progress}시간"

        exerciseTimeSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                exerciseTimeValue.text = "${progress}시간"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        selectedDate = intent.getStringExtra("selected_date")
        if (selectedDate == null) {
            Log.d("HealthcheckActivity", "Error: selectedDate is null")
            // 필요시 기본 값 설정 또는 사용자에게 오류 알림
        }
        binding.moodBad.setOnClickListener {
            selectedMoodColor = Color.RED
            Log.d("HealthcheckActivity", "Selected Mood Color: RED")
        }

        binding.moodNeutral.setOnClickListener {
            selectedMoodColor = Color.YELLOW
            Log.d("HealthcheckActivity", "Selected Mood Color: YELLOW")
        }

        binding.moodGood.setOnClickListener {
            selectedMoodColor = Color.GREEN
            Log.d("HealthcheckActivity", "Selected Mood Color: GREEN")
        }

        val symptomsSection = findViewById<GridLayout>(R.id.symptoms_section)

        binding.saveButton.setOnClickListener {
            // 증상 리스트 초기화
            val selectedSymptoms = mutableListOf<String>()

            // GridLayout에 있는 모든 ToggleButton을 순회하면서 상태 확인
            for (i in 0 until symptomsSection.childCount) {
                val child = symptomsSection.getChildAt(i)
                if (child is ToggleButton && child.isChecked) {
                    // ToggleButton이 선택된 경우, 해당 텍스트를 리스트에 추가
                    selectedSymptoms.add(child.text.toString())
                }
            }

            // 업데이트된 증상 리스트를 변수에 할당
            symptoms = selectedSymptoms
            sleepHours = sleepTimeSlider.progress.toFloat()
            waterIntake = waterTimeSlider.progress.toFloat()
            exerciseTime = exerciseTimeSlider.progress.toFloat()
            Log.d("HealthcheckActivity", "Selected Symptoms: $symptoms")
            val db = AppDatabase.getDatabase(applicationContext)
            val record = MoodRecord(
                date = selectedDate ?: "",
                moodColor = selectedMoodColor,
                stressLevel = stressLevel,
                symptoms = symptoms,
                sleepHours = sleepHours,
                waterIntake = waterIntake,
                exerciseTime = exerciseTime
            )

            // DB에 기록 저장
            lifecycleScope.launch(Dispatchers.IO) {
                db.moodDao().insertHealthCheckRecord(record)
            }

            // HealthActivity로 데이터 전송
            val returnIntent = Intent().apply {
                putExtra("selected_date", selectedDate)
                putExtra(HealthActivity.EXTRA_MOOD_COLOR, selectedMoodColor)
                putExtra("stress_level", stressLevel)
                putExtra("symptoms", symptoms.toTypedArray())
                putExtra("sleep_hours", sleepHours)
                putExtra("water_intake", waterIntake)
                putExtra("exercise_time", exerciseTime)
            }

            Log.d(
                "HealthcheckActivity",
                "Returning data: selectedDate=$selectedDate, moodColor=$selectedMoodColor"
            )

            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }
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