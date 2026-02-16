package com.colman.matconli.base

import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.colman.matconli.data.models.FirebaseAuthModel

abstract class BaseFragment : Fragment() {

    protected val authModel = FirebaseAuthModel()

    protected fun performLogout(loginNavDirection: NavDirections) {
        (activity as? MainActivity)?.clearUserProfile()
        authModel.signOut()
        findNavController().navigate(loginNavDirection)
    }

    protected fun getCurrentUserId(): String? = authModel.currentUser?.uid
}
