package com.example.wiselife

import android.content.Intent
import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.wiselife.databinding.ActivityChatbotBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable.start
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ChatbotActivity : AppCompatActivity() {
    private val REQUEST_RECORD_AUDIO_PERMISSION = 200
    private var isPermissionGranted = false
    private var isListening = false
    private lateinit var binding: ActivityChatbotBinding
    private lateinit var speechRecognizer: SpeechRecognizer

    interface TTSApi {
        @Headers("Content-Type: application/json")
        @POST("/opentts")
        suspend fun getTTS(@Body request: Map<String, String>): Response<ResponseBody>
    }
    val retrofit = Retrofit.Builder()
        .baseUrl("https://sanghyun.shop")
        .client(OkHttpClient())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val ttsApi = retrofit.create(TTSApi::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatbotBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonMicrophone.visibility = View.VISIBLE
        binding.buttonHello.visibility = View.VISIBLE
        binding.buttonMicrolisten.visibility = View.GONE
        binding.buttonListen.visibility = View.GONE
        binding.userSpeak.visibility = View.GONE

        checkPermissions()

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {
                Log.e("ChatbotActivity", "Speech recognition error: $error")
                runOnUiThread {
                    //binding.userSpeak.text = "Error recognizing speech: ${getErrorMessage(error)}"
                    binding.userSpeak.text = "~"
                    if (error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY) {
                        binding.userSpeak.text = "Recognizer is busy. Please wait."
                    }
                }
                isListening = false
            }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val spokenText = matches?.get(0) ?: "No speech recognized"

                Log.d("ChatbotActivity", "Speech recognition result: $spokenText")
                if (spokenText == "No speech recognized") {
                    binding.userSpeak.text = "No speech recognized. Please try again."
                    restartListeningAfterDelay()
                } else {
                    binding.userSpeak.text = spokenText
                    sendTextToChatbot(spokenText)
                }
                isListening = false
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        binding.buttonMicrophone.setOnClickListener {
            binding.buttonMicrophone.visibility = View.GONE
            binding.buttonHello.visibility = View.GONE
            binding.buttonMicrolisten.visibility = View.VISIBLE
            binding.buttonListen.visibility = View.VISIBLE
            binding.userSpeak.visibility = View.VISIBLE
            if (isPermissionGranted && !isListening) {
                startListening()
            } else {
                requestPermissions()
            }
        }

        binding.buttonMicrolisten.setOnClickListener {
            binding.buttonMicrophone.visibility = View.VISIBLE
            binding.buttonHello.visibility = View.VISIBLE
            binding.buttonMicrolisten.visibility = View.GONE
            binding.buttonListen.visibility = View.GONE
            binding.userSpeak.visibility = View.GONE
            speechRecognizer.stopListening()
        }

        binding.buttonEndConversation.setOnClickListener {
            binding.buttonMicrophone.visibility = View.VISIBLE
            binding.buttonHello.visibility = View.VISIBLE
            binding.buttonMicrolisten.visibility = View.GONE
            binding.buttonListen.visibility = View.GONE
            binding.userSpeak.visibility = View.GONE
            speechRecognizer.stopListening()
        }

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
    }

    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        speechRecognizer.startListening(intent)
        isListening = true
    }

    private fun restartListeningAfterDelay() {
        Handler(Looper.getMainLooper()).postDelayed({
            if (!isListening) {
                startListening()
            }
        }, 5000)
    }

    private fun sendTextToChatbot(text: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.api.sendChatRaw(
                    ChatRequest(
                        content = text,
                        user_id = "lsh",
                        signnum = 0
                    )
                )

                if (response.isSuccessful) {
                    val responseBody = response.body()?.string()
                    Log.d("ChatbotActivity", "Raw Response: $responseBody")
                    runOnUiThread {
                        val responseText = responseBody ?: "No response"
                        binding.buttonListen.text = responseText
                        //binding.buttonListen.text = responseBody ?: "No response"
                        binding.buttonListen.visibility = View.VISIBLE

                        playTTS(responseText)
                        restartListeningAfterDelay()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("ChatbotActivity", "Error: ${response.code()} - $errorBody")
                    runOnUiThread {
                        binding.buttonListen.text = "Error: ${response.code()}"
                        binding.buttonListen.visibility = View.VISIBLE
                        restartListeningAfterDelay()
                    }
                }
            } catch (e: HttpException) {
                Log.e("ChatbotActivity", "HttpException: ${e.message}")
                runOnUiThread {
                    binding.buttonListen.text = "Error: ${e.message()}"
                    binding.buttonListen.visibility = View.VISIBLE
                    restartListeningAfterDelay()
                }
            } catch (e: Exception) {
                Log.e("ChatbotActivity", "Exception: ${e.message}")
                runOnUiThread {
                    binding.buttonListen.text = "Error: ${e.message}"
                    binding.buttonListen.visibility = View.VISIBLE
                    restartListeningAfterDelay()
                }
            }
        }
    }


    private fun checkPermissions() {
        isPermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO_PERMISSION)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            isPermissionGranted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun playTTS(text: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val requestBody = mapOf("content" to text)

                val response = ttsApi.getTTS(requestBody)

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        val audioFile = File(cacheDir, "received_speech.mp3")
                        FileOutputStream(audioFile).use { outputStream ->
                            outputStream.write(responseBody.bytes())
                        }

                        /*val mediaPlayer = MediaPlayer().apply {
                            setDataSource(audioFile.absolutePath)
                            prepare()
                            start()
                        }*/
                        runOnUiThread {
                            val mediaPlayer = MediaPlayer().apply {
                                setDataSource(audioFile.absolutePath)
                                prepare()
                                start()
                                setOnCompletionListener {
                                    // Restart listening after playback completes
                                    restartListeningAfterDelay()
                                }
                            }
                        }
                    } else {
                        Log.e("ChatbotActivity", "TTS API 응답 본문이 null입니다.")
                    }
                } else {
                    Log.e("ChatbotActivity", "TTS API 오류: ${response.code()} - ${response.message()}")
                }
            } catch (e: IOException) {
                Log.e("ChatbotActivity", "TTS 요청 중 I/O 오류 발생: ${e.message}")
            } catch (e: Exception) {
                Log.e("ChatbotActivity", "TTS 요청 중 오류 발생: ${e.message}")
            }
        }
    }


    private fun getErrorMessage(errorCode: Int): String {
        return when (errorCode) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No match"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy"
            SpeechRecognizer.ERROR_SERVER -> "Server error"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout"
            else -> "Unknown error"
        }
    }
}
