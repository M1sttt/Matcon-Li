package com.colman.matconli.data.models

import android.graphics.Bitmap
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.colman.matconli.base.MyApplication
import com.colman.matconli.model.Recipe
import java.io.File
import java.io.FileOutputStream

class CloudinaryStorageModel {

    fun uploadRecipeImage(image: Bitmap, recipe: Recipe, completion: (String?) -> Unit) {
        val fileName = "recipe_${recipe.id}_${System.currentTimeMillis()}"
        uploadImage(image, fileName, "recipes", completion)
    }

    fun uploadUserImage(image: Bitmap, userId: String, completion: (String?) -> Unit) {
        val fileName = "user_${userId}_${System.currentTimeMillis()}"
        uploadImage(image, fileName, "users", completion)
    }

    private fun uploadImage(image: Bitmap, fileName: String, folder: String, completion: (String?) -> Unit) {
        val context = MyApplication.Globals.appContext ?: return completion(null)
        val file = bitmapToFile(image, context.cacheDir, fileName)

        MediaManager.get().upload(file.path)
            .option("folder", folder)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {
                }

                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                }

                override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                    val url = resultData?.get("secure_url") as? String
                    completion(url)
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    completion(null)
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                }
            })
            .dispatch()
    }

    private fun bitmapToFile(bitmap: Bitmap, cacheDir: File, fileName: String): File {
        val file = File(cacheDir, "$fileName.jpg")
        FileOutputStream(file).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        }
        return file
    }
}