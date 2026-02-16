package com.colman.matconli.base

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import com.colman.matconli.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.colman.matconli.data.repository.UserRepository
import com.colman.matconli.model.User
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity() {

    private var profileImageView: ImageView? = null
    private val auth = FirebaseAuth.getInstance()

    private var currentUserObserver: Observer<User?>? = null
    private var currentUserLiveData: LiveData<User?>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        setupToolbar()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_main_constraint_layout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.activity_main_toolbar)
        setSupportActionBar(toolbar)
    }

    fun loadUserProfile() {
        val userId = auth.currentUser?.uid
        val imageView = profileImageView ?: return

        currentUserLiveData?.let { liveData ->
            currentUserObserver?.let { observer ->
                liveData.removeObserver(observer)
            }
        }

        if (userId != null) {
            imageView.visibility = View.VISIBLE
            try {
                val liveData = UserRepository.shared.getUserByIdLiveData(userId)
                val observer = Observer<User?> { user ->
                    user?.let {
                        if (!it.avatarUrl.isNullOrBlank()) {
                            Picasso.get()
                                .load(it.avatarUrl)
                                .placeholder(R.drawable.ic_profile_placeholder)
                                .error(R.drawable.ic_profile_placeholder)
                                .into(imageView)
                        } else {
                            imageView.setImageResource(R.drawable.ic_profile_placeholder)
                        }
                    } ?: run {
                        imageView.setImageResource(R.drawable.ic_profile_placeholder)
                    }
                }

                currentUserLiveData = liveData
                currentUserObserver = observer
                liveData.observe(this, observer)

            } catch (e: Exception) {
                e.printStackTrace()
                imageView.setImageResource(R.drawable.ic_profile_placeholder)
            }
        } else {
            imageView.visibility = View.VISIBLE
            imageView.setImageResource(R.drawable.ic_profile_placeholder)
        }
    }

    fun clearUserProfile() {
        currentUserLiveData?.let { liveData ->
            currentUserObserver?.let { observer ->
                liveData.removeObserver(observer)
            }
        }
        currentUserLiveData = null
        currentUserObserver = null

        profileImageView?.setImageResource(R.drawable.ic_profile_placeholder)
    }

    override fun onDestroy() {
        super.onDestroy()
        currentUserLiveData?.let { liveData ->
            currentUserObserver?.let { observer ->
                liveData.removeObserver(observer)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.activity_main_nav_host_fragment) as NavHostFragment
        return navHostFragment.navController.navigateUp() || super.onSupportNavigateUp()
    }
}


