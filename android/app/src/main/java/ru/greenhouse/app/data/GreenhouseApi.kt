package ru.greenhouse.app.data

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface GreenhouseApi {
    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("api/logout")
    suspend fun logout()

    @GET("api/current")
    suspend fun current(): Current

    @GET("api/samples")
    suspend fun samples(@Query("sensor") sensor: String, @Query("minutes") minutes: Int): List<Point>

    @GET("api/stats")
    suspend fun stats(): Stats

    @GET("api/events")
    suspend fun events(): List<LogEntry>

    companion object {
        fun create(baseUrl: String, token: () -> String?): GreenhouseApi {
            val client = OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                    token()?.let { request.header("Authorization", "Bearer $it") }
                    chain.proceed(request.build())
                }
                .build()
            return Retrofit.Builder()
                .baseUrl(if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(GreenhouseApi::class.java)
        }
    }
}
