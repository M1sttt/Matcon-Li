package com.colman.matconli.data.networking

import com.colman.matconli.model.ExternalRecipeResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface MealDbClient {

    @GET("search.php")
    fun searchMeals(
        @Query("s") query: String
    ): Call<ExternalRecipeResponse>
}
