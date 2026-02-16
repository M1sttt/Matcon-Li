package com.colman.matconli.features.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.colman.matconli.data.models.FirebaseAuthModel
import com.colman.matconli.data.repository.UserRepository
import com.colman.matconli.model.User
import com.google.firebase.auth.FirebaseUser

class AuthViewModel : ViewModel() {

    private val authModel = FirebaseAuthModel()

    private val authStateMutable = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = authStateMutable

    private val currentUserMutable = MutableLiveData<FirebaseUser?>()
    val currentUser: LiveData<FirebaseUser?> = currentUserMutable

    init {
        currentUserMutable.value = authModel.currentUser
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            authStateMutable.value = AuthState.Error("Please fill in all fields")
            return
        }

        authStateMutable.value = AuthState.Loading
        authModel.signIn(
            email = email,
            password = password,
            onSuccess = { user ->
                val userId = user?.uid
                if (userId != null) {
                    UserRepository.shared.ensureUserExists(userId) { success ->
                        currentUserMutable.value = authModel.currentUser
                        authStateMutable.value = AuthState.Success
                    }
                } else {
                    currentUserMutable.value = authModel.currentUser
                    authStateMutable.value = AuthState.Success
                }
            },
            onFailure = { errorMessage ->
                authStateMutable.value = AuthState.Error(errorMessage)
            }
        )
    }

    fun register(name: String, email: String, password: String, confirmPassword: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            authStateMutable.value = AuthState.Error("Please fill in all fields")
            return
        }

        if (password != confirmPassword) {
            authStateMutable.value = AuthState.Error("Passwords do not match")
            return
        }

        if (password.length < 6) {
            authStateMutable.value = AuthState.Error("Password must be at least 6 characters")
            return
        }

        authStateMutable.value = AuthState.Loading
        authModel.signUp(
            email = email,
            password = password,
            onSuccess = { user ->
                val userId = user?.uid ?: return@signUp
                val userEmail = user.email ?: email

                val newUser = User(
                    id = userId,
                    name = name,
                    email = userEmail,
                    avatarUrl = null,
                    lastUpdated = null
                )

                UserRepository.shared.createUser(newUser) { success ->
                    if (success) {
                        currentUserMutable.value = authModel.currentUser
                        authStateMutable.value = AuthState.Success
                    } else {
                        authStateMutable.value = AuthState.Error("Failed to create user profile")
                    }
                }
            },
            onFailure = { errorMessage ->
                authStateMutable.value = AuthState.Error(errorMessage)
            }
        )
    }

    fun logout() {
        authModel.signOut()
        currentUserMutable.value = null
        authStateMutable.value = AuthState.Idle
    }

    fun isLoggedIn(): Boolean = authModel.isLoggedIn()

    fun resetState() {
        authStateMutable.value = AuthState.Idle
    }

    sealed class AuthState {
        data object Idle : AuthState()
        data object Loading : AuthState()
        data object Success : AuthState()
        data class Error(val message: String) : AuthState()
    }
}

