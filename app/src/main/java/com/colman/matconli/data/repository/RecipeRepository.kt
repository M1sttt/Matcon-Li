package com.colman.matconli.data.repository

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import com.colman.matconli.dao.AppLocalDB
import com.colman.matconli.dao.AppLocalDbRepository
import com.colman.matconli.data.models.FirebaseModel
import com.colman.matconli.data.models.StorageModel
import com.colman.matconli.model.Recipe
import java.util.concurrent.Executors

typealias Completion = () -> Unit

class RecipeRepository private constructor() {

    private val storageModel = StorageModel()
    private val firebaseModel = FirebaseModel()
    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler.createAsync(Looper.getMainLooper())
    private val database: AppLocalDbRepository = AppLocalDB.db

    companion object Companion {
        val shared = RecipeRepository()
    }

    fun getAllRecipes(): LiveData<MutableList<Recipe>> {
        return database.recipeDao().getAll()
    }

    fun refreshRecipes() {
        val lastUpdated = Recipe.Companion.lastUpdated

        firebaseModel.getAllRecipes(lastUpdated) { recipesList ->
            executor.execute {
                var time = lastUpdated
                for (recipe in recipesList) {
                    database.recipeDao().insert(recipe)
                    recipe.lastUpdated?.let { recipeLastUpdated ->
                        if (time < recipeLastUpdated) {
                            time = recipeLastUpdated
                        }
                    }
                }
                if (recipesList.isNotEmpty()) {
                    Recipe.Companion.lastUpdated = time
                }
            }
        }
    }

    fun getRecipeById(id: String, callback: (Recipe?) -> Unit) {
        executor.execute {
            val recipe = database.recipeDao().getById(id)
            mainHandler.post {
                callback(recipe)
            }
        }
    }

    fun addRecipe(storageAPI: StorageModel.StorageAPI, image: Bitmap?, recipe: Recipe, completion: Completion) {
        if (image != null) {
            storageModel.uploadRecipeImage(storageAPI, image, recipe) { imageUrl ->
                val recipeCopy = recipe.copy(
                    imageUrl = imageUrl ?: recipe.imageUrl,
                    lastUpdated = System.currentTimeMillis()
                )
                firebaseModel.saveRecipe(recipeCopy) {
                    executor.execute {
                        database.recipeDao().insert(recipeCopy)
                        mainHandler.post {
                            completion()
                        }
                    }
                }
            }
        } else {
            val recipeCopy = recipe.copy(lastUpdated = System.currentTimeMillis())
            firebaseModel.saveRecipe(recipeCopy) {
                executor.execute {
                    database.recipeDao().insert(recipeCopy)
                    mainHandler.post {
                        completion()
                    }
                }
            }
        }
    }

    fun updateRecipe(storageAPI: StorageModel.StorageAPI, image: Bitmap?, recipe: Recipe, completion: Completion) {
        if (image != null) {
            storageModel.uploadRecipeImage(storageAPI, image, recipe) { imageUrl ->
                val recipeCopy = recipe.copy(
                    imageUrl = imageUrl ?: recipe.imageUrl,
                    lastUpdated = System.currentTimeMillis()
                )
                firebaseModel.updateRecipe(recipeCopy) {
                    executor.execute {
                        database.recipeDao().update(recipeCopy)
                        mainHandler.post {
                            completion()
                        }
                    }
                }
            }
        } else {
            val recipeCopy = recipe.copy(lastUpdated = System.currentTimeMillis())
            firebaseModel.updateRecipe(recipeCopy) {
                executor.execute {
                    database.recipeDao().update(recipeCopy)
                    mainHandler.post {
                        completion()
                    }
                }
            }
        }
    }

    fun deleteRecipe(recipe: Recipe) {
        firebaseModel.deleteRecipe(recipe) {
            executor.execute {
                database.recipeDao().delete(recipe)
            }
        }
    }
}