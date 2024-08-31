package com.example.wiselife

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface HospitalReservationDao {
    @Insert
    fun insertReservation(reservation: HospitalReservation)

    @Query("SELECT * FROM hospital_reservations WHERE reservationDate = :date")
    fun getReservationsByDate(date: String): List<HospitalReservation>

    @Query("SELECT * FROM hospital_reservations ORDER BY id DESC LIMIT 1")
    fun getLatestReservation(): HospitalReservation?
}