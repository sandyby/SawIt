package com.example.sawit.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.example.sawit.R
import com.example.sawit.databinding.ActivityEditProfileBinding
import com.example.sawit.databinding.FragmentFieldsBinding
import com.example.sawit.databinding.FragmentHomeBinding
import com.example.sawit.utils.ImageCacheManager
import com.example.sawit.viewmodels.UserViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.io.File

class EditProfileActivity : AppCompatActivity() {

    private var _binding: ActivityEditProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var currentFullName: String
    private lateinit var currentEmailAddress: String
    private var selectedImageUri: Uri? = null
    private val userViewModel: UserViewModel by viewModels()
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                selectedImageUri = uri
                binding.ivProfilePictureEdit.setImageURI(uri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        _binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WindowCompat.setDecorFitsSystemWindows(window, true)

        currentFullName = intent.getStringExtra("EXTRA_INITIAL_NAME") ?: "John Doe"
        currentEmailAddress = intent.getStringExtra("EXTRA_INITIAL_EMAIL") ?: "john.doe@gmail.com"

        binding.tietFullNameField.setText(currentFullName)
        binding.tietEmailAddressField.setText(currentEmailAddress)

        setupListeners()

        userViewModel.listenForUserUpdates()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.btnEditProfilePic.setOnClickListener {
            Log.d("EditProfile", "btn_edit_profile_pic clicked")
            pickImageLauncher.launch("image/*")
        }

        binding.btnSaveProfile.setOnClickListener {
            validateAndSaveProfile()
        }
    }

    private fun validateAndSaveProfile() {
        val newName = binding.tietFullNameField.text.toString().trim()

        if (newName.isEmpty()) {
            binding.tilFullNameField.error = "Full name can't be empty!"
            return
        } else if (newName.length < 3) {
            binding.tilFullNameField.error = "Full name must be at least 3 characters long!"
            return
        } else if (newName.length > 30) {
            binding.tilFullNameField.error = "Full name must be at most 30 characters long!"
            return
        } else if (newName.matches(Regex("^[a-zA-Z]{3,}(?: [a-zA-Z]+){0,2}$"))) {
            binding.tilFullNameField.error = "Full name must not contain invalid symbols!"
            return
        } else {
            binding.tilFullNameField.error = null
        }

        val currentUser = userViewModel.currentUser.value
        if (currentUser == null) {
            Log.e(
                "EditProfileActivity",
                "User not logged in, field data listener cannot be started!"
            )
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            return
        }

        var photoBase64ToSave: String? = null
        var photoLocalPathToSave: String? = null

        if (selectedImageUri != null) {
            photoBase64ToSave = ImageCacheManager.uriToBase64(this, selectedImageUri!!)
            photoLocalPathToSave = photoBase64ToSave?.let { base64 ->
                ImageCacheManager.base64ToLocalCache(this, base64)
            }
            Log.d("EditProfileActivity", "photoLocalPath: $photoLocalPathToSave")

            if (photoBase64ToSave.isNullOrEmpty() || photoLocalPathToSave.isNullOrEmpty()) {
                Toast.makeText(this, "Failed to process profile picture!", Toast.LENGTH_SHORT)
                    .show()
                return
            }
        }
        userViewModel.updateExistingUserProfile(
            newName,
            photoBase64ToSave,
            photoLocalPathToSave
        )
    }

    private fun saveProfilePicToPreferences(uri: String) {
        val prefs = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("PROFILE_PIC_URI", uri)
            apply()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun loadProfilePicture(localPath: String?, base64String: String?) {
        val placeholderId = R.drawable.ic_filled_person_24_secondary_overlay

        if (ImageCacheManager.isCached(localPath)) {
            // 1. Load from local cache (FAST)
            Glide.with(this)
                .load(File(localPath!!))
                .placeholder(placeholderId)
                .error(placeholderId)
                .into(binding.ivProfilePictureEdit)
        } else if (!base64String.isNullOrEmpty()) {
            // 2. Fetch from DB (Base64) -> Save to local cache -> Load from cache
            val newLocalPath = ImageCacheManager.base64ToLocalCache(this, base64String)

            if (newLocalPath != null) {
                Glide.with(this)
                    .load(File(newLocalPath))
                    .placeholder(placeholderId)
                    .error(placeholderId)
                    .into(binding.ivProfilePictureEdit)
            } else {
                // Base64 decoding failed
                binding.ivProfilePictureEdit.setImageResource(placeholderId)
            }
        } else {
            // 3. Load placeholder
            binding.ivProfilePictureEdit.setImageResource(placeholderId)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    userViewModel.isLoading.collect { isLoading ->
                        binding.btnSaveProfile.isEnabled = !isLoading
                        binding.btnSaveProfile.text = if (isLoading) "SAVING..." else "SAVE CHANGES"
                    }
                }

                launch {
                    userViewModel.userProfile.collect { user ->
                        if (user != null) {
                            loadProfilePicture(user.profilePhotoLocalPath, user.profilePhotoBase64)

                            binding.tietFullNameField.setText(user.fullName)
                            binding.tietEmailAddressField.setText(user.email)
                        }
                    }
                }

                launch {
                    userViewModel.authEvents.collect { event ->
                        when (event) {
                            is UserViewModel.AuthEvent.Success -> {
                                Toast.makeText(
                                    this@EditProfileActivity,
                                    "Successfully updated profile!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                                userViewModel.consumeAuthEvent()
                            }

                            is UserViewModel.AuthEvent.Error -> {
                                Toast.makeText(
                                    this@EditProfileActivity,
                                    "Something went wrong while trying to update your profile!!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                userViewModel.consumeAuthEvent()
                            }

                            is UserViewModel.AuthEvent.RegistrationSuccess -> {

                            }

                            null -> {
                                // event consumed
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
