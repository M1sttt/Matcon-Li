package com.colman.matconli.data.models

import android.graphics.Bitmap
import com.google.firebase.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import com.colman.matconli.model.Recipe
import java.io.ByteArrayOutputStream

class FirebaseStorageModel {
    
    private val storage = Firebase.storage

    fun uploadRecipeImage(image: Bitmap, recipe: Recipe, completion: (String?) -> Unit) {
        val storageRef = storage.reference
        val imagesRecipeRef = storageRef.child("images/recipes/${recipe.id}/image.jpg")
        uploadImage(image, imagesRecipeRef, completion)
    }

    fun uploadUserImage(image: Bitmap, userId: String, completion: (String?) -> Unit) {
        val storageRef = storage.reference
        val imagesUserRef = storageRef.child("images/users/${userId}/avatar.jpg")
        uploadImage(image, imagesUserRef, completion)
    }

    private fun uploadImage(image: Bitmap, ref: StorageReference, completion: (String?) -> Unit) {
        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val uploadTask = ref.putBytes(data)
        uploadTask.addOnFailureListener {
            completion(null)
        }.addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener { uri ->
                completion(uri.toString())
            }.addOnFailureListener {
                completion(null)
            }
        }
    }
}