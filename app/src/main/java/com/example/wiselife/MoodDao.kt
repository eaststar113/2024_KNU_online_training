package com.example.wiselife

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MoodDao {
    @Insert
    fun insertHealthCheckRecord(record: MoodRecord)

    @Query("SELECT * FROM mood_records WHERE date = :date")
    fun getHealthCheckRecordsByDate(date: String): List<MoodRecord>
}