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
import com.example.sawit.R
import com.example.sawit.databinding.ActivityEditProfileBinding
import com.example.sawit.databinding.FragmentFieldsBinding
import com.example.sawit.databinding.FragmentHomeBinding
import com.example.sawit.viewmodels.UserViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class EditProfileActivity : AppCompatActivity() {

    private var _binding: ActivityEditProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var currentFullName: String
    private lateinit var currentEmailAddress: String
    private val userViewModel: UserViewModel by viewModels()

    // Launcher untuk memilih gambar dari gallery
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                binding.ivProfilePictureEdit.setImageURI(uri)
                saveProfilePicToPreferences(uri.toString())
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        _binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WindowCompat.setDecorFitsSystemWindows(window, true)

//        ivProfilePic = findViewById(R.id.iv_profile_picture_edit)
//        val btnEditProfilePic = findViewById<ImageButton>(R.id.btn_edit_profile_pic)
//        val tilFullName = findViewById<TextInputLayout>(R.id.til_full_name_field)
//        val tietFullName = findViewById<TextInputEditText>(R.id.tiet_full_name_field)
//        val tietEmailAddress = findViewById<TextInputEditText>(R.id.tiet_email_address_field)
//        val btnSave = findViewById<Button>(R.id.btn_save_profile)
//        val ivBack = findViewById<ImageView>(R.id.iv_back)


        // Load stored image (optional)
//        loadProfilePic()

        currentFullName = intent.getStringExtra("EXTRA_INITIAL_NAME") ?: "John Doe"
        currentEmailAddress = intent.getStringExtra("EXTRA_INITIAL_EMAIL") ?: "john.doe@gmail.com"

        binding.tietFullNameField.setText(currentFullName)
        binding.tietEmailAddressField.setText(currentEmailAddress)

        setupListeners()

//        binding.ivBack.setOnClickListener {
//            finish()
//        }
//
//        binding.btnEditProfilePic.setOnClickListener {
//            Log.d("EditProfile", "btn_edit_profile_pic clicked")
//            pickImageLauncher.launch("image/*")
//        }
//
//        binding.btnSaveProfile.setOnClickListener {
//            val newName = binding.tietFullNameField.text.toString().trim()
//
//            if (newName.isEmpty()) {
//                binding.tietFullNameField.error = "Full name can't be empty!"
//                return@setOnClickListener
//            }
//
//            Toast.makeText(this, "Profile successfully changed!", Toast.LENGTH_SHORT).show()
//
//            val currentUser = userViewModel.currentUser.value!!
//
//            userViewModel.saveNewUserProfile(currentUser.uid, currentEmailAddress, newName)
//
////            val resultIntent = Intent()
////            resultIntent.putExtra("EXTRA_NEW_NAME", newName)
////            resultIntent.putExtra("EXTRA_NEW_EMAIL", currentEmailAddress)
//
////            setResult(Activity.RESULT_OK, resultIntent)
//            finish()
//        }

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
        } else {
            userViewModel.saveNewUserProfile(
                currentUser.uid,
                currentEmailAddress,
                newName
            )
        }
    }

    private fun saveProfilePicToPreferences(uri: String) {
        val prefs = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("PROFILE_PIC_URI", uri)
            apply()
        }
    }
//
//    private fun loadProfilePic() {
//        val prefs = getSharedPreferences("AUTH_PREFS", Context.MODE_PRIVATE)
//        val uri = prefs.getString("PROFILE_PIC_URI", null)
//
//        if (uri != null) {
//            ivProfilePic.setImageURI(Uri.parse(uri))
//        }
//    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
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

                            null -> {
                                // Event consumed
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
