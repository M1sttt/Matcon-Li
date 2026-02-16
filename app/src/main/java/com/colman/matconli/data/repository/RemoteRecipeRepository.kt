package com.colman.matconli.data.repository

import com.colman.matconli.data.networking.NetworkClient
import com.colman.matconli.model.ExternalRecipeResponse

class RemoteRecipeRepository : ExternalRecipeRepository {

    companion object {
        val shared = RemoteRecipeRepository()
    }

    override fun searchRecipes(query: String): ExternalRecipeResponse {
        val request = NetworkClient.mealDbClient.searchMeals(query)
        val response = request.execute()

        val recipes = when (response.isSuccessful) {
            true -> {
                response.body() ?: run {
                    ExternalRecipeResponse(
                        meals = emptyList()
                    )
                }
            }
            false -> {
                ExternalRecipeResponse(
                    meals = emptyList()
                )
            }
        }

        return recipes
    }
}
