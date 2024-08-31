package com.example.wiselife

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medicine_records")
data class MedicineRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,
    val time: String,
    val name: String
)