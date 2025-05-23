package com.example.recetify.data.remote.model


import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val reqBuilder = chain.request().newBuilder()
        SessionManager.authToken?.let { token ->
            reqBuilder.addHeader("Authorization", "Bearer $token")
        }
        return chain.proceed(reqBuilder.build())
    }
}
