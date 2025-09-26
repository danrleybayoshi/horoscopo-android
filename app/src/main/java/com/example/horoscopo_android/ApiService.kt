package com.example.horoscopo_android

import com.example.horoscopo_android.RewriteRequest
import com.example.horoscopo_android.RewriteResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    // 🛑 MÉTODO POST: Envía un cuerpo (RewriteRequest) y espera un RewriteResponse.
    @POST("rewrite")
    suspend fun rewriteText(@Body request: RewriteRequest): RewriteResponse
}