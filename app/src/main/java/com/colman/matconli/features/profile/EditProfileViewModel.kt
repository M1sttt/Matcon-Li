package com.colman.matconli.features.profile

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.colman.matconli.data.models.StorageModel
import com.colman.matconli.data.repository.UserRepository
import com.colman.matconli.model.User

class EditProfileViewModel : ViewModel() {
    private val loadingMutable = MutableLiveData(false)
    val loading: LiveData<Boolean> = loadingMutable

    private val actionCompleteMutable = MutableLiveData<Boolean?>()
    val actionComplete: LiveData<Boolean?> = actionCompleteMutable

    fun getUserLiveData(userId: String): LiveData<User?> {
        return UserRepository.shared.getUserByIdLiveData(userId)
    }

    fun updateUser(storageAPI: StorageModel.StorageAPI, image: Bitmap?, user: User) {
        loadingMutable.postValue(true)
        UserRepository.shared.updateUser(storageAPI = storageAPI, image = image, user = user) { success ->
            loadingMutable.postValue(false)
            actionCompleteMutable.postValue(success)
        }
    }
}
