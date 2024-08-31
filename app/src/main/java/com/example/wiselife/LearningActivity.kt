package com.example.wiselife

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.wiselife.databinding.ActivityLearningBinding


class LearningActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityLearningBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "교육"

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

        val cardKioskPractice = findViewById<CardView>(R.id.card_kiosk_practice)
        cardKioskPractice.setOnClickListener {
            val intent = Intent(this, KioskPracticeActivity::class.java)
            startActivity(intent)
        }
        val gamePrac = findViewById<CardView>(R.id.card_game_practice)
        gamePrac.setOnClickListener {
            val intent = Intent(this, GamePracticeActivity::class.java)
            startActivity(intent)
        }
    }
}