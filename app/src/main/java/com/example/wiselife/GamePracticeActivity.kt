package com.example.wiselife

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.cardview.widget.CardView
import com.example.wiselife.databinding.ActivityGamePracticeBinding

class GamePracticeActivity : AppCompatActivity() {
    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityGamePracticeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "인지력 강화 게임"

        val memoryGame = findViewById<CardView>(R.id.memory_game)
        memoryGame.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://duckjoeun.github.io/Memory_CardGame/"))
            startActivity(browserIntent)
        }
        val wordMatching = findViewById<CardView>(R.id.word_match)
        wordMatching.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://duckjoeun.github.io/Word_Matching_Game"))
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