package com.example.recetify.data.remote

import com.example.recetify.data.remote.model.AuthInterceptor
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

object RetrofitClient {

    const val BASE_URL = "http://192.168.1.42:8080/"

    private val gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
        .create()

    private val okHttp = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor()) // 👈 Aca lo usás
        .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttp)
            // 1️⃣ Scalars para respuestas de tipo String
            .addConverterFactory(ScalarsConverterFactory.create())
            // 2️⃣ Luego Gson para los endpoints JSON
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }
}
