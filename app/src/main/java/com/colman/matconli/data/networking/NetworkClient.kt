package com.colman.matconli.data.networking

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkClient {

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .build()

    val mealDbClient: MealDbClient = run {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.themealdb.com/api/json/v1/1/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(MealDbClient::class.java)
    }
}
