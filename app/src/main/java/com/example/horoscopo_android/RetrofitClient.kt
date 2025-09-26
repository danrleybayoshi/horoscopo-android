package com.example.horoscopo_android

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // 🛑 NUEVA URL BASE para la API de Reescribir (POST)
    private const val BASE_URL = "https://rewriter-paraphraser-text-changer-multi-language.p.rapidapi.com/"

    // 🔑 TUS CLAVES DE RAPIDAPI
    private const val X_RAPIDAPI_KEY = "F7ccd53c90msh8b57de3b061ab97p1433d3jsnfb0fcbcae9bc"
    private const val X_RAPIDAPI_HOST = "rewriter-paraphraser-text-changer-multi-language.p.rapidapi.com"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        // Interceptor para añadir las cabeceras requeridas
        .addInterceptor(Interceptor { chain ->
            val newRequest = chain.request().newBuilder()
                .addHeader("x-rapidapi-key", X_RAPIDAPI_KEY)
                .addHeader("x-rapidapi-host", X_RAPIDAPI_HOST)
                .addHeader("Content-Type", "application/json") // La API requiere este header
                .build()
            chain.proceed(newRequest)
        })
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}