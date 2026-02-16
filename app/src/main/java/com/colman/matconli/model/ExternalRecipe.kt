package com.colman.matconli.model

import com.google.gson.annotations.SerializedName

data class ExternalRecipe(
    @SerializedName("idMeal")
    val id: String,

    @SerializedName("strMeal")
    val title: String,

    @SerializedName("strInstructions")
    val instructions: String?,

    @SerializedName("strMealThumb")
    val imageUrl: String?
)
