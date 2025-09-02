package com.platform.platformdelivery.data.remote

import com.platform.platformdelivery.core.network.ApiConfig
import com.platform.platformdelivery.core.network.ApiService
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // Token provider interface (you’ll implement it with SharedPreferences or DataStore)
    interface TokenProvider {
        fun getAccessToken(): String?
        fun refreshAccessToken(): String?
    }

    // Provide your token manager here
    lateinit var tokenProvider: TokenProvider

    private val authInterceptor = Interceptor { chain ->
        val requestBuilder = chain.request().newBuilder()

        tokenProvider.getAccessToken()?.let { token ->
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        chain.proceed(requestBuilder.build())
    }

    private val retryInterceptor = Interceptor { chain ->
        var request = chain.request()
        var response: Response = chain.proceed(request)

        if (response.code == 401) { // Unauthorized → try refresh
            response.close()

            val newToken = runBlocking { tokenProvider.refreshAccessToken() }
            if (!newToken.isNullOrEmpty()) {
                request = request.newBuilder()
                    .removeHeader("Authorization")
                    .addHeader("Authorization", "Bearer $newToken")
                    .build()
                response = chain.proceed(request)
            }
        }
        response
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        // Choose level according to your needs
        level = HttpLoggingInterceptor.Level.BODY
        // BODY → Logs request + headers + body + response
        // BASIC → Logs request & response line only
        // HEADERS → Logs headers only
        // NONE → No logs
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .addInterceptor(retryInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(ApiConfig.baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(ApiService::class.java)
    }
}