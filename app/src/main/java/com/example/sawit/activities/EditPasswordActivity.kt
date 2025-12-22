package com.example.sawit.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.sawit.R
import com.example.sawit.databinding.ActivityEditPasswordBinding
import com.example.sawit.ui.EditPasswordScreen
import com.example.sawit.ui.theme.SawItTheme
import com.example.sawit.viewmodels.UserViewModel
import kotlinx.coroutines.launch

class EditPasswordActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_OLD_PASSWORD = "EXTRA_OLD_PASSWORD"
        const val EXTRA_NEW_PASSWORD = "EXTRA_NEW_PASSWORD"
        private const val MIN_PASSWORD_LENGTH = 8
    }

    private var _binding: ActivityEditPasswordBinding? = null
    private val binding get() = _binding!!
    private val userViewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        _binding = ActivityEditPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WindowCompat.setDecorFitsSystemWindows(window, true)
        supportActionBar?.hide()

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.btnConfirmChangePassword.setOnClickListener {
            validateAndUpdatePassword()
        }
    }

    private fun validateAndUpdatePassword() {
        val etOldPassword = findViewById<EditText>(R.id.tiet_current_password)
        val etNewPassword = findViewById<EditText>(R.id.tiet_new_password)
        val etConfirmPassword = findViewById<EditText>(R.id.tiet_confirm_password)
        val btnChangePassword = findViewById<Button>(R.id.btn_confirm_change_password)
        val ivBack = findViewById<ImageView>(R.id.iv_back)

        ivBack.setOnClickListener {
            finish()
        }

        btnChangePassword.setOnClickListener {
            val oldPass = etOldPassword.text.toString()
            val newPass = etNewPassword.text.toString()
            val confirmPass = etConfirmPassword.text.toString()

            binding.tilCurrentPassword.error = null
            binding.tilNewPassword.error = null
            binding.tilConfirmPassword.error = null

            var isValid = true

            if (oldPass.isEmpty()) {
                binding.tilCurrentPassword.error = "Current password can't be empty!"
                isValid = false
            }

            if (newPass.isEmpty()) {
                binding.tilNewPassword.error = "New password can't be empty!"
                isValid = false
            } else if (newPass.length < MIN_PASSWORD_LENGTH) {
                binding.tilNewPassword.error = "New password must be at lesat $MIN_PASSWORD_LENGTH characters long!"
                isValid = false
            }

            if (confirmPass.isEmpty()) {
                binding.tilConfirmPassword.error = "Confirm password can't be empty!"
                isValid = false
            }

            if (newPass != confirmPass) {
                binding.tilConfirmPassword.error = "Passwords do not match!"
                isValid = false
            }

            if (isValid) {
                userViewModel.updatePassword(oldPass, newPass)
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    userViewModel.isLoading.collect { isLoading ->
                        binding.btnConfirmChangePassword.isEnabled = !isLoading
                        binding.btnConfirmChangePassword.text =
                            if (isLoading) "CONFIRMING..." else "CONFIRM CHANGES"
                    }
                }

                launch {
                    userViewModel.authEvents.collect { event ->
                        when (event) {
                            is UserViewModel.AuthEvent.Success -> {
                                Toast.makeText(
                                    this@EditPasswordActivity,
                                    "Successfully changed password!",
                                    Toast.LENGTH_LONG
                                ).show()

                                finish()
                                userViewModel.consumeAuthEvent()
                            }

                            is UserViewModel.AuthEvent.Error -> {
                                Log.e("EditPasswordActivity", "Error: ${event.message}")
                                binding.tilCurrentPassword.error = "Incorrect credential! Please check again, or try to contact the developer if this error persists!"
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

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}