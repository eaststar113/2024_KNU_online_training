package com.example.wiselife

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MedicineDao {
    @Insert
    fun insertMedicineRecord(record: MedicineRecord)

    @Query("SELECT * FROM medicine_records WHERE date = :date")
    fun getMedicineRecordsByDate(date: String): List<MedicineRecord>

    @Query("SELECT * FROM medicine_records ORDER BY id DESC LIMIT 1") // 가장 최근의 기록을 가져옵니다.
    fun getLatestMedicineRecord(): MedicineRecord?
}
