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
import com.colman.matconli.databinding.FragmentRegisterBinding
import com.colman.matconli.utilis.hide
import com.colman.matconli.utilis.show

class RegisterFragment : Fragment() {

    var binding: FragmentRegisterBinding? = null

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        observeAuthState()
    }

    private fun setupClickListeners() {
        binding?.fragmentRegisterButton?.setOnClickListener {
            val name = binding?.fragmentRegisterEditTextName?.text.toString().trim()
            val email = binding?.fragmentRegisterEditTextEmail?.text.toString().trim()
            val password = binding?.fragmentRegisterEditTextPassword?.text.toString() ?: ""
            val confirmPassword = binding?.fragmentRegisterEditTextConfirmPassword?.text.toString() ?: ""
            viewModel.register(name, email, password, confirmPassword)
        }

        binding?.fragmentRegisterTextViewLogin?.setOnClickListener {
            findNavController().navigate(
                RegisterFragmentDirections.actionRegisterFragmentToLoginFragment()
            )
        }
    }

    private fun observeAuthState() {
        viewModel.authState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthViewModel.AuthState.Loading -> {
                    binding?.fragmentRegisterProgressBar?.show()
                    binding?.fragmentRegisterButton?.isEnabled = false
                }
                is AuthViewModel.AuthState.Success -> {
                    binding?.fragmentRegisterProgressBar?.hide()
                    navigateToFeed()
                }
                is AuthViewModel.AuthState.Error -> {
                    binding?.fragmentRegisterProgressBar?.hide()
                    binding?.fragmentRegisterButton?.isEnabled = true
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
                is AuthViewModel.AuthState.Idle -> {
                    binding?.fragmentRegisterProgressBar?.hide()
                    binding?.fragmentRegisterButton?.isEnabled = true
                }
            }
        }
    }

    private fun navigateToFeed() {
        findNavController().navigate(
            RegisterFragmentDirections.actionRegisterFragmentToFeedFragment()
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}

