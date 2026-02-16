package com.colman.matconli.data.repository

import com.colman.matconli.model.ExternalRecipeResponse

interface ExternalRecipeRepository {
    fun searchRecipes(query: String): ExternalRecipeResponse
}
