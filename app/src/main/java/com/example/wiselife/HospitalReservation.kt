package com.example.wiselife

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hospital_reservations")
data class HospitalReservation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val hospitalName: String,
    val reservationTime: String,
    val reservationDate: String // 날짜를 String 형식으로 저장 (예: "2024-08-27")
)
