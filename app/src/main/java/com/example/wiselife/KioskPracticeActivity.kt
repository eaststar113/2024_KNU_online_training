package com.example.wiselife

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.cardview.widget.CardView
import com.example.wiselife.databinding.ActivityKioskPracticeBinding

class KioskPracticeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityKioskPracticeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "키오스크 연습하기"

        val cardOrderFood = findViewById<CardView>(R.id.card_order_food)
        cardOrderFood.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://duckjoeun.github.io/Kiosk_food"))
            startActivity(browserIntent)
        }
        val trainOrderPrac = findViewById<CardView>(R.id.train_order_prac)
        trainOrderPrac.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://duckjoeun.github.io/Learn_Kiosk_train"))
            startActivity(browserIntent)
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