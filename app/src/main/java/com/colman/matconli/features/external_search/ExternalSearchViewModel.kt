package com.colman.matconli.features.external_search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.colman.matconli.data.repository.RemoteRecipeRepository
import com.colman.matconli.model.ExternalRecipe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExternalSearchViewModel : ViewModel() {

    private val recipesMutable = MutableLiveData<List<ExternalRecipe>>()
    val recipes: LiveData<List<ExternalRecipe>> = recipesMutable

    private val isLoadingMutable = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = isLoadingMutable

    private val errorMutable = MutableLiveData<String?>()
    val error: LiveData<String?> = errorMutable

    fun searchRecipes(query: String) {
        if (query.isBlank()) {
            recipesMutable.value = emptyList()
            return
        }

        isLoadingMutable.value = true
        errorMutable.value = null

        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    RemoteRecipeRepository.shared.searchRecipes(query)
                }

                recipesMutable.value = result.meals ?: emptyList()
                isLoadingMutable.value = false
            } catch (e: Exception) {
                errorMutable.value = "Failed to search recipes: ${e.message}"
                recipesMutable.value = emptyList()
                isLoadingMutable.value = false
            }
        }
    }

    fun clearError() {
        errorMutable.value = null
    }
}
