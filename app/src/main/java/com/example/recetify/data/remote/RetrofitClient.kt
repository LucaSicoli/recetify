package com.example.recetify.data.remote

import com.example.recetify.data.remote.model.SessionManager
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
<<<<<<< HEAD

    private const val BASE_URL = "http://192.168.1.36:8080/"


=======
    const val BASE_URL = "http://192.168.0.25:8080/"
>>>>>>> f3a2f39289142bb7129c048f0120c43d21a17bbb

    private val gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
        .create()

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val builder = original.newBuilder()
            .header("Content-Type", "application/json")
        SessionManager.authToken?.let {
            builder.header("Authorization", "Bearer $it")
        }
        chain.proceed(builder.method(original.method, original.body).build())
    }

    private val okHttp = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .build()


    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }
}
