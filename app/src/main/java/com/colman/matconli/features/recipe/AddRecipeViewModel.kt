package com.colman.matconli.features.recipe

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.colman.matconli.data.models.StorageModel
import com.colman.matconli.data.repository.RecipeRepository
import com.colman.matconli.model.Recipe
import android.graphics.Bitmap

class AddRecipeViewModel : ViewModel() {
    private val recipeMutable = MutableLiveData<Recipe?>()
    val recipe: LiveData<Recipe?> = recipeMutable

    private val loadingMutable = MutableLiveData(false)
    val loading: LiveData<Boolean> = loadingMutable

    private val actionCompleteMutable = MutableLiveData<Boolean>()
    val actionComplete: LiveData<Boolean> = actionCompleteMutable

    fun loadRecipe(id: String) {
        loadingMutable.postValue(true)
        RecipeRepository.shared.getRecipeById(id) { r ->
            recipeMutable.postValue(r)
            loadingMutable.postValue(false)
        }
    }

    fun addRecipe(storageAPI: StorageModel.StorageAPI, image: Bitmap?, recipe: Recipe) {
        loadingMutable.postValue(true)
        RecipeRepository.shared.addRecipe(storageAPI = storageAPI, image = image, recipe = recipe) {
            loadingMutable.postValue(false)
            actionCompleteMutable.postValue(true)
        }
    }

    fun updateRecipe(storageAPI: StorageModel.StorageAPI, image: Bitmap?, recipe: Recipe) {
        loadingMutable.postValue(true)
        RecipeRepository.shared.updateRecipe(storageAPI = storageAPI, image = image, recipe = recipe) {
            loadingMutable.postValue(false)
            actionCompleteMutable.postValue(true)
        }
    }
}
