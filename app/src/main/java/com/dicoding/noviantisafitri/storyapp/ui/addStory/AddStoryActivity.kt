package com.dicoding.noviantisafitri.storyapp.ui.addStory

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.dicoding.noviantisafitri.storyapp.data.di.ViewModelFactory
import com.dicoding.noviantisafitri.storyapp.data.di.compressImageSize
import com.dicoding.noviantisafitri.storyapp.data.di.createCustomTempFile
import com.dicoding.noviantisafitri.storyapp.data.di.rotateBitmap
import com.dicoding.noviantisafitri.storyapp.data.di.uriToFile
import com.dicoding.noviantisafitri.storyapp.databinding.ActivityAddStoryBinding
import com.dicoding.noviantisafitri.storyapp.ui.main.MainActivity
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import android.net.ConnectivityManager

class AddStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddStoryBinding
    private lateinit var factory: ViewModelFactory
    private lateinit var currentPhotoPath: String
    private var getFile: File? = null
    private val addStoryViewModel: AddStoryViewModel by viewModels { factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupView()
        setupViewModel()
        setupPermission()
        setupAction()
    }

    private fun setupView() {
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        supportActionBar?.apply {
            title = "Add Story"
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun setupViewModel() {
        factory = ViewModelFactory.getInstance(this)
    }

    private fun setupPermission() {
        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this@AddStoryActivity,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }
    }

    private fun setupAction() {
        binding.apply {
            btnCamera.setOnClickListener { startCamera() }
            btnGallery.setOnClickListener { startGallery() }
            buttonAdd.setOnClickListener { uploadStory() }
        }
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            val myFile = File(currentPhotoPath)
            getFile = myFile

            val result = rotateBitmap(
                BitmapFactory.decodeFile(getFile?.path),
                true
            )
            binding.ivAddStory.setImageBitmap(result)
        }
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg: Uri = result.data?.data as Uri
            val myFile = uriToFile(selectedImg, this)

            getFile = myFile
            binding.ivAddStory.setImageURI(selectedImg)
        }
    }

    private fun startCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.resolveActivity(packageManager)

        createCustomTempFile(application).also {
            val photoURI: Uri = FileProvider.getUriForFile(
                this@AddStoryActivity,
                "com.dicoding.noviantisafitri.storyapp",
                it
            )
            currentPhotoPath = it.absolutePath

            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            launcherIntentCamera.launch(intent)
        }
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"

        val chooser = Intent.createChooser(intent, "Choose a Picture")
        launcherIntentGallery.launch(chooser)
    }

//    private fun uploadStory() {
//        val description = binding.edAddDescription.text.toString().trim()
//        if (description.isEmpty()) {
//            Toast.makeText(this, "Please fill in the description!", Toast.LENGTH_SHORT).show()
//            return
//        }
//        showLoading()
//        addStoryViewModel.getSession().observe(this@AddStoryActivity) {
//            if (getFile != null) {
//                val file = compressImageSize(getFile as File)
//                val requestImageFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
//                val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
//                    "photo",
//                    file.name,
//                    requestImageFile
//                )
//                uploadResponse(
//                    it.token,
//                    imageMultipart,
//                    binding.edAddDescription.text.toString().toRequestBody("text/plain".toMediaType())
//                )
//            } else {
//                Toast.makeText(
//                    this@AddStoryActivity,
//                    "Please upload an image file first!",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//        }
//    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnected
    }

    private fun uploadStory() {
        val description = binding.edAddDescription.text.toString().trim()
        if (description.isEmpty()) {
            Toast.makeText(this, "Please fill in the description!", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No internet connection. Please try again later.", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading()
        addStoryViewModel.getSession().observe(this@AddStoryActivity) {
            if (getFile != null) {
                val file = compressImageSize(getFile as File)
                val requestImageFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
                    "photo",
                    file.name,
                    requestImageFile
                )
                uploadResponse(
                    it.token,
                    imageMultipart,
                    binding.edAddDescription.text.toString().toRequestBody("text/plain".toMediaType())
                )
            } else {
                Toast.makeText(
                    this@AddStoryActivity,
                    "Please upload an image file first!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun uploadResponse(
        token: String,
        file: MultipartBody.Part,
        description: RequestBody
    ) {
        addStoryViewModel.uploadStory(token, file, description)
        addStoryViewModel.uploadResponse.observe(this@AddStoryActivity) { response ->
            if (response.error) {
                // Menampilkan pesan kesalahan jika upload gagal
                Toast.makeText(this@AddStoryActivity, "Upload failed. Please try again.", Toast.LENGTH_SHORT).show()
            } else {
                // Jika upload berhasil, pindah ke MainActivity
                moveActivity()
            }
        }
        showToast()
    }


    private fun showLoading() {
        addStoryViewModel.isLoading.observe(this@AddStoryActivity) {
//            binding.buttonAdd.visibility = if (it) View.VISIBLE else View.GONE
        }
    }

    private fun showToast() {
        addStoryViewModel.toastText.observe(this@AddStoryActivity) {
            it.getContentIfNotHandled()?.let { toastText ->
                Toast.makeText(
                    this@AddStoryActivity, toastText, Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun moveActivity() {
        val intent = Intent(this@AddStoryActivity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}
