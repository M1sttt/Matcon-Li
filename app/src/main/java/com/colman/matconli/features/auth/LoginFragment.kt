package com.colman.matconli.features.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.colman.matconli.base.MainActivity
import com.colman.matconli.databinding.FragmentLoginBinding
import com.colman.matconli.utilis.hide
import com.colman.matconli.utilis.show

class LoginFragment : Fragment() {

    var binding: FragmentLoginBinding? = null

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (viewModel.isLoggedIn()) {
            navigateToFeed()
            return
        }

        setupClickListeners()
        observeAuthState()
    }

    private fun setupClickListeners() {
        binding?.fragmentLoginButton?.setOnClickListener {
            val email = binding?.fragmentLoginEditTextEmail?.text.toString().trim()
            val password = binding?.fragmentLoginEditTextPassword?.text.toString()
            viewModel.login(email, password)
        }

        binding?.fragmentLoginTextViewRegister?.setOnClickListener {
            findNavController().navigate(
                LoginFragmentDirections.actionLoginFragmentToRegisterFragment()
            )
        }
    }

    private fun observeAuthState() {
        viewModel.authState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthViewModel.AuthState.Loading -> {
                    binding?.fragmentLoginProgressBar?.show()
                    binding?.fragmentLoginButton?.isEnabled = false
                }
                is AuthViewModel.AuthState.Success -> {
                    binding?.fragmentLoginProgressBar?.hide()
                    navigateToFeed()
                }
                is AuthViewModel.AuthState.Error -> {
                    binding?.fragmentLoginProgressBar?.hide()
                    binding?.fragmentLoginButton?.isEnabled = true
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
                is AuthViewModel.AuthState.Idle -> {
                    binding?.fragmentLoginProgressBar?.hide()
                    binding?.fragmentLoginButton?.isEnabled = true
                }
            }
        }
    }

    private fun navigateToFeed() {
        findNavController().navigate(
            LoginFragmentDirections.actionLoginFragmentToFeedFragment()
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}

