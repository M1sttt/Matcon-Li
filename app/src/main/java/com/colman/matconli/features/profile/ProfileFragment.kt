package com.colman.matconli.features.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.colman.matconli.base.MainActivity
import com.colman.matconli.R
import com.colman.matconli.databinding.FragmentProfileBinding
import com.colman.matconli.base.BaseFragment
import com.colman.matconli.utilis.hide
import com.colman.matconli.utilis.show
import com.colman.matconli.utilis.toggleVisibility
import com.squareup.picasso.Picasso

class ProfileFragment : BaseFragment() {

    private var binding: FragmentProfileBinding? = null

    private val viewModel: UserViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding?.root ?: inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMenu()
        setupObservers()
        setupClickListeners()
        viewModel.loadCurrentUser()
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_profile, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_back -> {
                        findNavController().popBackStack()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setupObservers() {
        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
            binding?.let { binding ->
                if (user != null) {
                    binding.fragmentProfileTextViewName.text = user.name
                    binding.fragmentProfileTextViewEmail.text = user.email
                    if (!user.avatarUrl.isNullOrBlank()) {
                        Picasso.get()
                            .load(user.avatarUrl)
                            .placeholder(R.drawable.ic_profile_placeholder)
                            .error(R.drawable.ic_profile_placeholder)
                            .into(binding.fragmentProfileImageView)
                    } else {
                        binding.fragmentProfileImageView.setImageResource(R.drawable.ic_profile_placeholder)
                    }
                } else {
                    binding.fragmentProfileTextViewName.text = ""
                    binding.fragmentProfileTextViewEmail.text = ""
                    binding.fragmentProfileImageView.setImageResource(R.drawable.ic_profile_placeholder)
                }
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding?.let { binding ->
                binding.fragmentProfileProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }
    }

    private fun setupClickListeners() {
        binding?.let { binding ->
            binding.fragmentProfileButtonEdit.setOnClickListener {
                findNavController().navigate(
                    ProfileFragmentDirections.actionProfileFragmentToEditProfileFragment()
                )
            }

            binding.fragmentProfileButtonLogout.setOnClickListener {
                performLogout(ProfileFragmentDirections.actionProfileFragmentToLoginFragment())
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}

