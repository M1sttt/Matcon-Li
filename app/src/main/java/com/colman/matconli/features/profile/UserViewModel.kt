package com.colman.matconli.features.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.colman.matconli.data.repository.UserRepository
import com.colman.matconli.model.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val currentUserMutable = MediatorLiveData<User?>()
    val currentUser: LiveData<User?> = currentUserMutable

    private val isLoadingMutable = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = isLoadingMutable

    private val updateResultMutable = MutableLiveData<Boolean?>()
    val updateResult: LiveData<Boolean?> = updateResultMutable

    private var userSource: LiveData<User?>? = null
    private var isActive = true

    override fun onCleared() {
        super.onCleared()
        isActive = false
        userSource?.let { currentUserMutable.removeSource(it) }
        userSource = null
    }

    fun loadCurrentUser() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            isLoadingMutable.postValue(false)
            currentUserMutable.postValue(null)
            return
        }

        isLoadingMutable.postValue(true)

        UserRepository.shared.ensureUserExists(userId) { success ->
            if (isActive) {
                if (success) {
                    userSource?.let { currentUserMutable.removeSource(it) }

                    val source = UserRepository.shared.getUserByIdLiveData(userId)
                    userSource = source

                    currentUserMutable.addSource(source) { user ->
                        if (isActive) {
                            currentUserMutable.postValue(user)
                            isLoadingMutable.postValue(false)
                        }
                    }
                } else {
                    isLoadingMutable.postValue(false)
                    currentUserMutable.postValue(null)
                }
            }
        }
    }

    fun updateProfile(name: String, avatarUrl: String?) {
        val userId = auth.currentUser?.uid ?: return
        val email = auth.currentUser?.email ?: return

        isLoadingMutable.value = true
        updateResultMutable.value = null

        val updatedUser = User(
            id = userId,
            name = name,
            email = email,
            avatarUrl = avatarUrl,
            lastUpdated = null
        )

        viewModelScope.launch(Dispatchers.IO) {
            UserRepository.shared.updateUser(
                storageAPI = com.colman.matconli.data.models.StorageModel.StorageAPI.CLOUDINARY,
                image = null,
                user = updatedUser
            ) { success ->
                viewModelScope.launch {
                    withContext(Dispatchers.Main) {
                        isLoadingMutable.value = false
                        updateResultMutable.value = success
                        if (success) {
                            currentUserMutable.value = updatedUser
                        }
                    }
                }
            }
        }
    }

    fun clearUpdateResult() {
        updateResultMutable.value = null
    }
}

