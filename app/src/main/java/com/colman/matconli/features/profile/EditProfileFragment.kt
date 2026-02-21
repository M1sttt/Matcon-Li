package com.colman.matconli.features.profile

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.colman.matconli.base.MainActivity
import com.colman.matconli.R
import com.colman.matconli.databinding.FragmentEditProfileBinding
import com.colman.matconli.data.repository.UserRepository
import com.colman.matconli.data.models.StorageModel
import com.colman.matconli.model.User
import com.colman.matconli.utilis.ImageUtils
import com.google.firebase.auth.FirebaseAuth
import com.colman.matconli.features.profile.EditProfileViewModel
import java.io.File

class EditProfileFragment : Fragment() {

    private var binding: FragmentEditProfileBinding? = null
    private val auth = FirebaseAuth.getInstance()
    private var currentUser: User? = null
    private val viewModel: EditProfileViewModel by viewModels()
    private var selectedImageUri: Uri? = null
    private var photoUri: Uri? = null

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            openCamera()
        } else {
            Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            photoUri?.let { uri ->
                selectedImageUri = uri
                loadImagePreview(uri.toString())
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding?.root ?: inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            setupClickListeners()
            setupObservers()
            loadCurrentUser()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadCurrentUser() {
        val userId = auth.currentUser?.uid ?: return
        binding?.fragmentEditProfileProgressBar?.visibility = View.VISIBLE
        viewModel.getUserLiveData(userId).observe(viewLifecycleOwner) { user ->
            binding?.fragmentEditProfileProgressBar?.visibility = View.GONE
            currentUser = user
            user?.let {
                binding?.fragmentEditProfileEditTextName?.setText(it.name)
                binding?.fragmentEditProfileTextViewEmail?.text = "Email: ${it.email}"
                loadImagePreview(it.avatarUrl)
            }
        }
    }

    private fun setupObservers() {
        viewModel.loading.observe(viewLifecycleOwner) { loading ->
            if (binding == null) return@observe
            binding?.fragmentEditProfileProgressBar?.visibility = if (loading) View.VISIBLE else View.GONE
            binding?.fragmentEditProfileButtonSave?.isEnabled = !loading
            binding?.fragmentEditProfileButtonCancel?.isEnabled = !loading
        }

        viewModel.actionComplete.observe(viewLifecycleOwner) { success ->
            if (success == null) return@observe
            if (binding == null) return@observe
            if (success) {
                Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show()
                (activity as? MainActivity)?.loadUserProfile()
                findNavController().popBackStack()
            } else {
                Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadImagePreview(url: String?) {
        binding?.fragmentEditProfileImageView?.let {
            ImageUtils.loadImage(it, url, R.drawable.ic_profile_placeholder)
        }
    }

    private fun openCamera() {
        val photoFile = File.createTempFile(
            "profile_${System.currentTimeMillis()}",
            ".jpg",
            requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        )

        photoUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            photoFile
        )

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        }
        cameraLauncher.launch(intent)
    }

    private fun setupClickListeners() {
        binding?.let { binding ->
            binding.fragmentEditProfileButtonSave.setOnClickListener {
                saveProfile()
            }

            binding.fragmentEditProfileButtonCancel.setOnClickListener {
                findNavController().popBackStack()
            }

            binding.fragmentEditProfileButtonCamera.setOnClickListener {
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    openCamera()
                } else {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        }
    }

    private fun saveProfile() {
        val name = binding?.fragmentEditProfileEditTextName?.text.toString().trim()

        if (name.isEmpty()) {
            binding?.fragmentEditProfileTextInputLayoutName?.error = "Name is required"
            return
        }

        val user = currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "User not found", Toast.LENGTH_SHORT).show()
            return
        }

        binding?.fragmentEditProfileTextInputLayoutName?.error = null
        binding?.fragmentEditProfileProgressBar?.visibility = View.VISIBLE
        binding?.fragmentEditProfileButtonSave?.isEnabled = false
        binding?.fragmentEditProfileButtonCancel?.isEnabled = false

        val updatedUser = user.copy(name = name)

        try {
            val bitmap = selectedImageUri?.let { getBitmapFromUri(it) }

            viewModel.updateUser(
                StorageModel.StorageAPI.CLOUDINARY,
                bitmap,
                updatedUser
            )
        } catch (e: Exception) {
            binding?.fragmentEditProfileProgressBar?.visibility = View.GONE
            binding?.fragmentEditProfileButtonSave?.isEnabled = true
            binding?.fragmentEditProfileButtonCancel?.isEnabled = true
            Toast.makeText(requireContext(), "Failed to process image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getBitmapFromUri(uri: Uri): Bitmap {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        val maxSize = 800
        return if (bitmap.width > maxSize || bitmap.height > maxSize) {
            val scale = maxSize.toFloat() / Math.max(bitmap.width, bitmap.height)
            val newWidth = (bitmap.width * scale).toInt()
            val newHeight = (bitmap.height * scale).toInt()
            Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        } else {
            bitmap
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}

