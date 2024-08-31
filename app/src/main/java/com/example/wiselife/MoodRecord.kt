package com.example.wiselife

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mood_records")
data class MoodRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,
    val moodColor: Int,
    val stressLevel: String,          // 스트레스 수준
    val symptoms: List<String>,       // 증상
    val sleepHours: Float,            // 수면 시간
    val waterIntake: Float,           // 물 섭취량
    val exerciseTime: Float           // 운동 시간
)
