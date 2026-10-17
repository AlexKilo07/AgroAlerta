package com.example.agroalerta

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

// Estructura JSON mapeada con Gson
interface AgroApiService {
    @GET("telemetria") // Endpoint REST
    suspend fun obtenerTelemetria(): List<TelemetriaCuartel>
}

object RetrofitClient {
    // Para pruebas puedes usar una URL base de Mocky.io o JSONBin
    private const val BASE_URL = "https://run.mocky.io/v3/"

    val apiService: AgroApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AgroApiService::class.java)
    }
}