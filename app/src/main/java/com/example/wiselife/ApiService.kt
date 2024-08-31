package com.example.wiselife

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @Multipart
    @POST("whisper")
    suspend fun uploadAudio(@Part file: MultipartBody.Part): Response<ResponseBody>

    @POST("chatcomp")
    suspend fun sendChatRaw(@Body chatRequest: ChatRequest): Response<ResponseBody>
}