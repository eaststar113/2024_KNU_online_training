package com.example.wiselife

data class ChatRequest(
    val content: String,
    val user_id: String,
    val signnum: Int
)