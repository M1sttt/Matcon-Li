package com.colman.matconli.data.models

import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestore
import com.colman.matconli.model.Recipe

class FirebaseModel {

    private val db = Firebase.firestore

    private companion object COLLECTIONS {
        const val RECIPES = "recipes"
    }

    fun getAllRecipes(since: Long, completion: (List<Recipe>) -> Unit) {
        if (since <= 0L) {
            db.collection(RECIPES)
                .get()
                .addOnCompleteListener { result ->
                    when (result.isSuccessful) {
                        true -> completion(result.result.map { Recipe.fromJson(it.data) })
                        false -> completion(emptyList())
                    }
                }
        } else {
            val seconds = since / 1000
            val nanos = ((since % 1000) * 1_000_000).toInt()
            db.collection(RECIPES)
                .whereGreaterThan(Recipe.LAST_UPDATED_KEY, Timestamp(seconds, nanos))
                .get()
                .addOnCompleteListener { result ->
                    if (result.isSuccessful) {
                        val list = result.result.map { Recipe.fromJson(it.data) }
                        if (list.isNotEmpty()) {
                            completion(list)
                        } else {
                            db.collection(RECIPES)
                                .get()
                                .addOnCompleteListener { fullResult ->
                                    when (fullResult.isSuccessful) {
                                        true -> completion(fullResult.result.map { Recipe.fromJson(it.data) })
                                        false -> completion(emptyList())
                                    }
                                }
                        }
                    } else {
                        completion(emptyList())
                    }
                }
        }
    }

    fun saveRecipe(recipe: Recipe, completion: () -> Unit) {
        db.collection(RECIPES)
            .document(recipe.id)
            .set(recipe.toJson)
            .addOnSuccessListener {
                completion()
            }
            .addOnFailureListener {
                completion()
            }
    }

    fun updateRecipe(recipe: Recipe, completion: () -> Unit) = saveRecipe(recipe, completion)

    fun deleteRecipe(recipe: Recipe, completion: () -> Unit) {
        db.collection(RECIPES)
            .document(recipe.id)
            .delete()
            .addOnSuccessListener {
                completion()
            }
            .addOnFailureListener {
                completion()
            }
    }
}
