package com.colman.matconli.features.recipe

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
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.colman.matconli.R
import com.colman.matconli.databinding.FragmentAddRecipeBinding
import com.colman.matconli.model.Recipe
import com.colman.matconli.data.repository.RecipeRepository
import com.colman.matconli.data.models.StorageModel
import com.colman.matconli.utilis.ImageUtils
import com.colman.matconli.utilis.hide
import com.colman.matconli.utilis.show
import com.colman.matconli.base.BaseFragment
import java.io.File
import java.util.UUID

class AddRecipeFragment : BaseFragment() {

    var binding: FragmentAddRecipeBinding? = null

    private val args: AddRecipeFragmentArgs by navArgs()
    private var existingRecipe: Recipe? = null
    private var selectedImageUri: Uri? = null
    private var photoUri: Uri? = null
    private val storageModel = StorageModel()

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

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                loadImagePreview(uri.toString())
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddRecipeBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        args.recipeId?.let { id ->
            loadExistingRecipe(id)
        }

        setupClickListeners()
    }

    private fun loadExistingRecipe(id: String) {
        binding?.fragmentAddRecipeProgressBar?.show()
        RecipeRepository.shared.getRecipeById(id) { recipe ->
            activity?.runOnUiThread {
                if (binding == null) return@runOnUiThread
                binding?.fragmentAddRecipeProgressBar?.hide()
                recipe?.let {
                    existingRecipe = it
                    binding?.fragmentAddRecipeEditTextTitle?.setText(it.title)
                    binding?.fragmentAddRecipeEditTextDescription?.setText(it.description)
                    loadImagePreview(it.imageUrl)
                }
            }
        }
    }

    private fun loadImagePreview(url: String?) {
        binding?.fragmentAddRecipeImageView?.let {
            ImageUtils.loadImage(it, url, R.drawable.ic_recipe_placeholder)
        }
    }

    private fun openCamera() {
        val photoFile = File.createTempFile(
            "recipe_${System.currentTimeMillis()}",
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

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private fun setupClickListeners() {
        binding?.fragmentAddRecipeButtonSave?.setOnClickListener {
            saveRecipe()
        }
        binding?.fragmentAddRecipeButtonCancel?.setOnClickListener {
            findNavController().popBackStack()
        }

        binding?.fragmentAddRecipeButtonSelectImage?.setOnClickListener {
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

    private fun saveRecipe() {
        val title = binding?.fragmentAddRecipeEditTextTitle?.text.toString().trim()
        val description = binding?.fragmentAddRecipeEditTextDescription?.text.toString().trim()

        if (title.isBlank() || description.isBlank()) {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUserId = getCurrentUserId()
        if (currentUserId == null) {
            Toast.makeText(requireContext(), "Please login first", Toast.LENGTH_SHORT).show()
            return
        }

        val isEditing = existingRecipe != null

        binding?.fragmentAddRecipeProgressBar?.show()
        binding?.fragmentAddRecipeButtonSave?.isEnabled = false

        val recipeId = existingRecipe?.id ?: UUID.randomUUID().toString()

        try {
            val bitmap = selectedImageUri?.let { getBitmapFromUri(it) }
            val recipe = Recipe(
                id = recipeId,
                title = title,
                description = description,
                imageUrl = existingRecipe?.imageUrl,
                ownerId = currentUserId,
                lastUpdated = null
            )

            if (isEditing) {
                RecipeRepository.shared.updateRecipe(
                    storageAPI = StorageModel.StorageAPI.CLOUDINARY,
                    image = bitmap,
                    recipe = recipe
                ) {
                    activity?.runOnUiThread {
                        if (binding == null) return@runOnUiThread
                        binding?.fragmentAddRecipeProgressBar?.hide()
                        binding?.fragmentAddRecipeButtonSave?.isEnabled = true
                        Toast.makeText(requireContext(), "Recipe updated!", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }
                }
            } else {
                RecipeRepository.shared.addRecipe(
                    storageAPI = StorageModel.StorageAPI.CLOUDINARY,
                    image = bitmap,
                    recipe = recipe
                ) {
                    activity?.runOnUiThread {
                        if (binding == null) return@runOnUiThread
                        binding?.fragmentAddRecipeProgressBar?.hide()
                        binding?.fragmentAddRecipeButtonSave?.isEnabled = true
                        Toast.makeText(requireContext(), "Recipe saved!", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }
                }
            }
        } catch (e: Exception) {
            binding?.fragmentAddRecipeProgressBar?.hide()
            binding?.fragmentAddRecipeButtonSave?.isEnabled = true
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

