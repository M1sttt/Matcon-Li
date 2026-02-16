package com.colman.matconli.data.models

import android.graphics.Bitmap
import com.colman.matconli.model.Recipe

class StorageModel {

    enum class StorageAPI {
        FIREBASE,
        CLOUDINARY
    }

    private val firebaseStorage = FirebaseStorageModel()
    private val cloudinaryStorage = CloudinaryStorageModel()

    fun uploadRecipeImage(api: StorageAPI, image: Bitmap, recipe: Recipe, completion: (String?) -> Unit) {
        when (api) {
            StorageAPI.FIREBASE -> firebaseStorage.uploadRecipeImage(image, recipe, completion)
            StorageAPI.CLOUDINARY -> cloudinaryStorage.uploadRecipeImage(image, recipe, completion)
        }
    }

    fun uploadUserImage(api: StorageAPI, image: Bitmap, userId: String, completion: (String?) -> Unit) {
        when (api) {
            StorageAPI.FIREBASE -> firebaseStorage.uploadUserImage(image, userId, completion)
            StorageAPI.CLOUDINARY -> cloudinaryStorage.uploadUserImage(image, userId, completion)
        }
    }
}
