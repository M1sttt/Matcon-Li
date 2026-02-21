package com.colman.matconli.features.recipe

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.colman.matconli.data.repository.RecipeRepository
import com.colman.matconli.model.Recipe

class RecipeDetailViewModel : ViewModel() {
    private val recipeMutable = MutableLiveData<Recipe?>()
    val recipe: LiveData<Recipe?> = recipeMutable

    private val loadingMutable = MutableLiveData(false)
    val loading: LiveData<Boolean> = loadingMutable

    private val deleteCompleteMutable = MutableLiveData<Boolean>()
    val deleteComplete: LiveData<Boolean> = deleteCompleteMutable

    fun loadRecipe(id: String) {
        loadingMutable.postValue(true)
        RecipeRepository.shared.getRecipeById(id) { r ->
            recipeMutable.postValue(r)
            loadingMutable.postValue(false)
        }
    }

    fun deleteRecipe() {
        val r = recipeMutable.value ?: return
        loadingMutable.postValue(true)
        RecipeRepository.shared.deleteRecipe(r)
        loadingMutable.postValue(false)
        deleteCompleteMutable.postValue(true)
    }
}
