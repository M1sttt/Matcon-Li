package com.colman.matconli.data.repository

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import com.colman.matconli.dao.AppLocalDB
import com.colman.matconli.dao.AppLocalDbRepository
import com.colman.matconli.data.models.StorageModel
import com.colman.matconli.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.concurrent.Executors

typealias UserCompletion = (User?) -> Unit

class UserRepository private constructor() {

    private val storageModel = StorageModel()
    private val db = Firebase.firestore
    private val usersCollection = db.collection("users")
    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler.createAsync(Looper.getMainLooper())
    private val database: AppLocalDbRepository = AppLocalDB.db

    companion object Companion {
        val shared = UserRepository()
    }

    fun getUserById(userId: String, completion: UserCompletion) {
        executor.execute {
            val user = database.userDao().getById(userId).value
            mainHandler.post {
                completion(user)
            }
        }
    }

    fun getUserByIdLiveData(userId: String): LiveData<User?> {
        refreshUser(userId)
        return database.userDao().getById(userId)
    }

    fun refreshUser(userId: String) {
        usersCollection.document(userId)
            .get()
            .addOnSuccessListener { document ->
                executor.execute {
                    try {
                        if (document.exists()) {
                            val user = User.Companion.fromJson(document.data ?: return@execute)
                            database.userDao().insert(user)
                            user.lastUpdated?.let {
                                if (it > User.Companion.lastUpdated) {
                                    User.Companion.lastUpdated = it
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
    }

    fun updateUser(storageAPI: StorageModel.StorageAPI, image: Bitmap?, user: User, completion: (Boolean) -> Unit) {
        if (image != null) {
            storageModel.uploadUserImage(storageAPI, image, user.id) { imageUrl ->
                val userCopy = user.copy(
                    avatarUrl = imageUrl ?: user.avatarUrl,
                    lastUpdated = System.currentTimeMillis()
                )
                saveUserToFirebase(userCopy, completion)
            }
        } else {
            val userCopy = user.copy(lastUpdated = System.currentTimeMillis())
            saveUserToFirebase(userCopy, completion)
        }
    }

    private fun saveUserToFirebase(user: User, completion: (Boolean) -> Unit) {
        usersCollection.document(user.id)
            .set(user.toJson)
            .addOnSuccessListener {
                executor.execute {
                    try {
                        database.userDao().insert(user)
                        mainHandler.post {
                            completion(true)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        mainHandler.post {
                            completion(false)
                        }
                    }
                }
            }
            .addOnFailureListener {
                mainHandler.post {
                    completion(false)
                }
            }
    }

    fun createUser(user: User, completion: (Boolean) -> Unit) {
        usersCollection.document(user.id)
            .set(user.toJson)
            .addOnSuccessListener {
                executor.execute {
                    try {
                        database.userDao().insert(user.copy(lastUpdated = System.currentTimeMillis()))
                        mainHandler.post {
                            completion(true)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        mainHandler.post {
                            completion(false)
                        }
                    }
                }
            }
            .addOnFailureListener {
                mainHandler.post {
                    completion(false)
                }
            }
    }

    fun ensureUserExists(userId: String, completion: (Boolean) -> Unit) {
        usersCollection.document(userId).get()
            .addOnSuccessListener { document ->
                try {
                    if (!document.exists()) {
                        val firebaseUser = FirebaseAuth.getInstance().currentUser
                        if (firebaseUser != null && firebaseUser.uid == userId) {
                            val newUser = User(
                                id = userId,
                                name = firebaseUser.displayName
                                    ?: firebaseUser.email?.substringBefore("@") ?: "User",
                                email = firebaseUser.email ?: "",
                                avatarUrl = firebaseUser.photoUrl?.toString(),
                                lastUpdated = null
                            )
                            createUser(newUser, completion)
                        } else {
                            mainHandler.post {
                                completion(false)
                            }
                        }
                    } else {
                        mainHandler.post {
                            completion(true)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    mainHandler.post {
                        completion(false)
                    }
                }
            }
            .addOnFailureListener {
                mainHandler.post {
                    completion(false)
                }
            }
    }
}