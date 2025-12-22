package com.example.sawit.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.sawit.R
import com.example.sawit.models.User
import com.example.sawit.viewmodels.UserViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RegisterActivity : AppCompatActivity() {
    /*
     * activity ini digunakan untuk menghandle proses register user baru, mirip flownya dengan loginactivity
     * tujuannya agar user dapat meninput data seperti nama lengkap, email, dan password, lalu disimpan ke Firebase Realtime Database.
     *
     * validasi input dilakukan langsung di tiap textinputedittext lewat TextWatcher,
     * jadi user langsung tahu kalau ada kesalahan input sebelum klik tombol register.
     *
     * untuk keamanan, password kami memakai hash SHA-256 sebelum dikirim ke database.
     * ini penting agar password tidak tersimpan dalam bentuk asli dan terjadi data leak, dsb.
     *
     * ketika register sukses, user langsung diarahkan ke halaman login.
     * */
    private lateinit var tietFullName: TextInputEditText
    private lateinit var tietEmail: TextInputEditText
    private lateinit var tietPassword: TextInputEditText
    private lateinit var tietConfirmPassword: TextInputEditText
    private lateinit var tilFullName: TextInputLayout
    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var tilConfirmPassword: TextInputLayout
    private lateinit var mBtnRegister: MaterialButton
    private val userViewModel: UserViewModel by viewModels()
    private var isFullNameValid = false
    private var isEmailValid = false
    private var isPasswordValid = false
    private var isConfirmPasswordValid = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tilFullName = findViewById(R.id.til_fullname_field)
        tietFullName = findViewById(R.id.tiet_fullname_field)

        tilEmail = findViewById(R.id.til_email_field)
        tietEmail = findViewById(R.id.tiet_email_field)

        tilPassword = findViewById(R.id.til_password_field)
        tietPassword = findViewById(R.id.tiet_password_field)

        tilConfirmPassword = findViewById(R.id.til_confirm_password_field)
        tietConfirmPassword = findViewById(R.id.tiet_confirm_password_field)

        mBtnRegister = findViewById<MaterialButton>(R.id.mBtn_register)

        tietFullName.addTextChangedListener(FullNameWatcher())
        tietEmail.addTextChangedListener(EmailWatcher())
        tietPassword.addTextChangedListener(PasswordWatcher())
        tietConfirmPassword.addTextChangedListener(ConfirmPasswordWatcher())

        /*
        * untuk saat ini, kami lebih banyak menggunakan intent dan belum menggunakan jetpack nav atau
        * component navigation lainnya demi kepraktisan. namun kedepannya akan digunakan.
        * */
        val tvSwitchLogin = findViewById<TextView>(R.id.tv_switchLogin)
        tvSwitchLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        mBtnRegister.setOnClickListener {
            if (isFullNameValid && isEmailValid && isPasswordValid && isConfirmPasswordValid) {
                mBtnRegister.isEnabled = false
                val fullName = tietFullName.text.toString().trim()
                val email = tietEmail.text.toString().trim()
                val password = tietPassword.text.toString().trim()

                userViewModel.registerUser(email, password, fullName)
            }
        }
        observeViewModel()
    }

    private inner class FullNameWatcher : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            val fullName = tietFullName.text.toString().trim()
            isFullNameValid = when {
                fullName.isEmpty() -> {
                    tilFullName.error = "Full name is required"
                    false
                }

                fullName.length < 3 -> {
                    tilFullName.error = "Full name must be at least 3 characters long"
                    false
                }

                fullName.length > 30 -> {
                    tilFullName.error = "Full name must be at most 30 characters long"
                    false
                }

                !fullName.matches(Regex("^[a-zA-Z]{3,}(?: [a-zA-Z]+){0,2}$")) -> {
                    tilFullName.error = "Full name must not contain invalid symbols"
                    false
                }

                else -> {
                    tilFullName.error = null
                    tilFullName.isErrorEnabled = false
                    true
                }
            }
            updateRegisterButtonState()
        }
    }

    private inner class EmailWatcher : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            val email = tietEmail.text.toString().trim()
            isEmailValid = when {
                email.isEmpty() -> {
                    tilEmail.error = "E-mail is required"
                    false
                }

                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    tilEmail.error = "Invalid e-mail format"
                    false
                }

                else -> {
                    tilEmail.error = null
                    tilEmail.isErrorEnabled = false
                    true
                }
            }
            updateRegisterButtonState()
        }
    }

    private inner class PasswordWatcher : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            val password = tietPassword.text.toString().trim()
            isPasswordValid = when {
                password.isEmpty() -> {
                    tilPassword.error = "Password is required"
                    false
                }

                password.length < 8 -> {
                    tilPassword.error = "Password must be at least 8 characters long"
                    false
                }

                else -> {
                    tilPassword.error = null
                    tilPassword.isErrorEnabled = false
                    true
                }
            }
            validateConfirmPassword()
            updateRegisterButtonState()
        }
    }

    private inner class ConfirmPasswordWatcher : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            validateConfirmPassword()
            updateRegisterButtonState()
        }
    }

    private fun validateConfirmPassword() {
        val confirmPassword = tietConfirmPassword.text.toString().trim()
        val password = tietPassword.text.toString().trim()
        isConfirmPasswordValid =
            if ((!password.isEmpty() && confirmPassword.isEmpty()) || confirmPassword != password) {
                tilConfirmPassword.error = "Passwords do not match"
                false
            } else {
                tilConfirmPassword.error = null
                tilConfirmPassword.isErrorEnabled = false
                true
            }
    }

    private fun updateRegisterButtonState() {
        mBtnRegister.isEnabled =
            isFullNameValid && isEmailValid && isPasswordValid && isConfirmPasswordValid && !userViewModel.isLoading.value
    }

    private fun startLoginActivity() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    userViewModel.isLoading.collect { isLoading ->
                        updateRegisterButtonState()
                    }
                }
                launch {
                    userViewModel.authEvents.collect { event ->
                        when (event) {
                            is UserViewModel.AuthEvent.Success -> {
                                userViewModel.consumeAuthEvent()
                            }

                            is UserViewModel.AuthEvent.Error -> {
                                Log.e("RegisterActivity", event.message)
                                Toast.makeText(
                                    this@RegisterActivity,
                                    event.message,
                                    Toast.LENGTH_LONG
                                ).show()

                                if (event.message.contains(
                                        "email address is already in use",
                                        ignoreCase = true
                                    )
                                ) {
                                    tilEmail.error = "E-mail is already registered!"
                                }

                                userViewModel.consumeAuthEvent()
                            }

                            is UserViewModel.AuthEvent.RegistrationSuccess -> {
                                Toast.makeText(
                                    this@RegisterActivity,
                                    "Registration Successful! Please log in.",
                                    Toast.LENGTH_SHORT
                                ).show()

                                startLoginActivity()
                                userViewModel.consumeAuthEvent()
                            }

                            null -> {
                            }
                        }
                    }
                }
            }
        }
    }
}