package com.kundaliai.app.data.api

import com.kundaliai.app.BuildConfig
import com.kundaliai.app.data.models.CerebrasRequest
import com.kundaliai.app.data.models.CerebrasResponse
import com.kundaliai.app.utils.Constants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface CerebrasApiService {

    @POST("chat/completions")
    suspend fun getChatCompletion(
        @Header("Authorization") auth: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Body request: CerebrasRequest
    ): Response<CerebrasResponse>

    companion object {
        fun create(): CerebrasApiService {
            val logging = HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG)
                    HttpLoggingInterceptor.Level.BODY
                else
                    HttpLoggingInterceptor.Level.NONE
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(Constants.CEREBRAS_BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(CerebrasApiService::class.java)
        }
    }
}
