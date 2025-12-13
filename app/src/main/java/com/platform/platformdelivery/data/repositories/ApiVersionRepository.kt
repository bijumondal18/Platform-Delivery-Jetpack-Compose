package com.platform.platformdelivery.data.repositories

import com.platform.platformdelivery.core.network.ApiConfig
import com.platform.platformdelivery.core.network.ApiService
import com.platform.platformdelivery.data.models.ApiVersionResponse
import com.platform.platformdelivery.data.remote.RetrofitClient
import retrofit2.Response

class ApiVersionRepository {
    // Create a simple Retrofit instance without base URL for the api_version call
    private val apiService: ApiService by lazy {
        val retrofit = retrofit2.Retrofit.Builder()
            .baseUrl("https://platformdelivery.app/") // Temporary base URL, will be overridden by @Url
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .client(
                okhttp3.OkHttpClient.Builder()
                    .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .build()
            )
            .build()
        retrofit.create(ApiService::class.java)
    }

    suspend fun getApiVersion(): Response<ApiVersionResponse> {
        return apiService.getApiVersion(ApiConfig.apiVersion)
    }
}

