package com.example.koistory.ui.view.add_story

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.example.koistory.R
import com.example.koistory.data.ResultState
import com.example.koistory.databinding.ActivityAddStoryBinding
import com.example.koistory.ui.view.CombinedViewModel
import com.example.koistory.ui.view.ViewModelFactory
import com.example.koistory.ui.view.add_story.CameraActivity.Companion.CAMERAX_RESULT
import com.example.koistory.ui.view.main.MainActivity
import com.example.koistory.utils.getImageUri
import com.example.koistory.utils.reduceFileImage
import com.example.koistory.utils.uriToFile

class AddStoryActivity : AppCompatActivity() {
    private val viewModel by viewModels<CombinedViewModel> {
        ViewModelFactory.getInstance(application,this)
    }
    private lateinit var binding: ActivityAddStoryBinding
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                showToast(getString(R.string.akses_diberikan))
            } else {
                showToast(getString(R.string.akses_ditolak))
            }
        }

    private fun allPermissionsGranted() =
        ContextCompat.checkSelfPermission(
            this,
            REQUIRED_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!allPermissionsGranted()) {
            requestPermissionLauncher.launch(REQUIRED_PERMISSION)
        }

        viewModel.currentImageUri.observe(this) { uri ->
            uri?.let { binding.previewImageView.setImageURI(it) }
        }

        viewModel.errorMessage.observe(this) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }

        binding.galleryButton.setOnClickListener { startGallery() }
        binding.cameraButton.setOnClickListener { startCamera() }
        binding.cameraXButton.setOnClickListener { startCameraX() }
        binding.buttonAdd.setOnClickListener { uploadImage() }
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.setCurrentImageUri(uri)
            showImage()
        } else {
            showToast(getString(R.string.gagal_ambil_gambar))
        }
    }

    private fun startCamera() {
        val uri = getImageUri(this)
        viewModel.setCurrentImageUri(uri)
        launcherIntentCamera.launch(viewModel.currentImageUri.value!!)
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            showImage()
        } else {
            showToast(getString(R.string.gagal_ambil_gambar))
            viewModel.setCurrentImageUri(null)
        }
    }

    private fun startCameraX() {
        val intent = Intent(this, CameraActivity::class.java)
        launcherIntentCameraX.launch(intent)
    }

    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == CAMERAX_RESULT) {
            val uri = it.data?.getStringExtra(CameraActivity.EXTRA_CAMERAX_IMAGE)?.toUri()
            viewModel.setCurrentImageUri(uri)
        }
    }

    private fun showImage() {
        viewModel.currentImageUri.value?.let{
            binding.previewImageView.setImageURI(it)
        }
    }

    private fun uploadImage() {
        val uri = viewModel.currentImageUri.value
        if (uri != null && uri.toString().isNotEmpty()) {
            try {
                val imageFile = uriToFile(uri, this).reduceFileImage()
                val description = binding.edAddDescription.text.toString()

                viewModel.uploadImage(imageFile, description).observe(this) { result ->
                    if (result != null) {
                        when (result) {
                            is ResultState.Loading -> {
                                showLoading(true)
                            }

                            is ResultState.Success -> {
                                showToast(result.data.message)
                                showLoading(false)
                                val intent = Intent(this, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                            }

                            is ResultState.Error -> {
                                showToast(result.error)
                                showLoading(false)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                showToast(getString(R.string.masukan_gambar))
            }
        } else {
            showToast(getString(R.string.masukan_gambar))
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val REQUIRED_PERMISSION = Manifest.permission.CAMERA
    }
}