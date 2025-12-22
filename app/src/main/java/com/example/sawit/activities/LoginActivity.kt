package com.example.sawit.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.sawit.R
import com.example.sawit.models.User
import com.example.sawit.ui.theme.BgPrimary400
import com.example.sawit.ui.theme.BgPrimary500
import com.example.sawit.ui.theme.BgSecondaryOverlay2
import com.example.sawit.ui.theme.Text600
import com.example.sawit.ui.theme.TextPrimary500
import com.example.sawit.viewmodels.UserViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.getValue
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.security.MessageDigest

class LoginActivity : AppCompatActivity() {
    /*
    * declaration untuk variable-variable lateinit yang akan digunakan
    * sebagai reference nantinya untuk memanipulasi value seperti textviews,
    * textinputedittexts, button, dan lainnya untuk kebutuhan flow login.
    * */
    private lateinit var tietEmail: TextInputEditText
    private lateinit var tietPassword: TextInputEditText
    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var mBtnLogin: MaterialButton
    private lateinit var tvLoginErrorMsg: TextView
    private lateinit var smRememberMe: SwitchMaterial
    private lateinit var tvSwitchRegister: TextView

    /*
    * declaration untuk variable-variable logical untuk keperluan validation
    * */
    private val userViewModel: UserViewModel by viewModels()
    private var isEmailValid = false
    private var isPasswordValid = false

    override fun onCreate(savedInstanceState: Bundle?) {
        /*
        * disable dark mode secara global agar tidak terjadi hal yang tidak diinginkan seperti berubahnya warna akibat
        * property tint tiba-tiba
        * */
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_login)

        /*
        * deklarasi variable untuk menampung sharedpreferences, yakni segala informasi
        * yang berkaitan dengan user settings, preference tertentu, session atau status login,
        * dan masih banyak lagi yang dapat disimpan dalam tipe data tersebut dalam bentuk key-value pair.
        *
        * contohnya di bawah ini, kami menyimpan email jika sudah ada sebelumnya, berarti menandakan user tersebut
        * sudah login sebelumnya, agar tidak perlu melakukan login lagi. ini bisa dikembangkan lebih jauh lagi kedepannya jika kami
        * memutuskan untuk meningkatkan UX dari segi session control, dan juga keamanan seperti session timeout, dsb
        * */
        val authSharedPref = getSharedPreferences("AuthSession", MODE_PRIVATE)
        val isRememberMeOn = authSharedPref.getBoolean("rememberMe", false)

        /*
        * initialize komponen Ui berupa texteditinputtexts dan juga textinputlayouts dari layout xml activity_login, seperti yang diset
        * oleh method setcontentview di atas sebelumnya
        *
        * ada juga onclicklistener untuk button login dan juga semacam link guna untuk melempar user ke register page
        *
        * dan juga menghandle feedback dari validation berupa error message
        * */

        tilEmail = findViewById(R.id.til_email_field)
        tietEmail = findViewById(R.id.tiet_email_field)
        tilPassword = findViewById(R.id.til_password_field)
        tietPassword = findViewById(R.id.tiet_password_field)
        tvSwitchRegister = findViewById<TextView>(R.id.tv_switchRegister)
        mBtnLogin = findViewById<MaterialButton>(R.id.mBtn_login)
        smRememberMe = findViewById<SwitchMaterial>(R.id.sm_rememberMe)
        tvLoginErrorMsg = findViewById<TextView>(R.id.tv_login_error_msg)

        if (userViewModel.currentUser.value != null) {
            if (isRememberMeOn) {
                mBtnLogin.isEnabled = false
                userViewModel.listenForUserUpdates()

                lifecycleScope.launch {
                    userViewModel.userProfile.filterNotNull().first().let {
                        startMainActivity()
                        finish()
                    }
                }
            } else {
                userViewModel.logout()
            }
        } else {
        }

        WindowCompat.setDecorFitsSystemWindows(window, true)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main))
        { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tvSwitchRegister.setOnClickListener()
        {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
        mBtnLogin.setOnClickListener()
        {
            tvLoginErrorMsg.visibility = View.GONE
            val email = tietEmail.text.toString().trim()
            val password = tietPassword.text.toString().trim()
            Log.d("LoginActivity", "$email $password")
            userViewModel.loginUser(email, password)
        }
        tietEmail.addTextChangedListener(EmailWatcher())
        tietPassword.addTextChangedListener(PasswordWatcher())
        observeViewModel()
    }

    /*
    * kita mengaplikasikan textwatcher untuk custom functions yang kami buat untuk validasi dan pengawasan user input,
    * khususnya pada email dan juga password
    * */

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
                    tilEmail.error = "Invalid email format"
                    false
                }

                else -> {
                    tilEmail.error = null
                    true
                }
            }
            updateLoginButtonState()
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

                else -> {
                    tilPassword.error = null
                    true
                }
            }
            updateLoginButtonState()
        }
    }

    /*
    * logika state button login untuk meningkatkan UX
    * */
    private fun updateLoginButtonState() {
        mBtnLogin.isEnabled = isEmailValid && isPasswordValid && !userViewModel.isLoading.value
    }

    /*
    * custom sha256 hash untuk password agar aman tersimpan di database
    * */
    private fun hashSHA256String(input: String): String {
        val HEX_CHARS = "0123456789ABCDEF"
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return buildString {
            bytes.forEach {
                val i = it.toInt()
                append(HEX_CHARS[i shr 4 and 0x0f])
                append(HEX_CHARS[i and 0x0f])
            }
        }
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun storeUserPrefs(user: User) {
        val authSharedPref = getSharedPreferences("AuthSession", Context.MODE_PRIVATE)
        authSharedPref.edit {
            putString("userId", user.uid)
            putBoolean("rememberMe", smRememberMe.isChecked)
            apply()
        }

        val userSharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        userSharedPref.edit {
            putString("uid", user.uid)
            putString("fullName", user.fullName)
            putString("email", user.email)
            apply()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    userViewModel.isLoading.collect { isLoading ->
                        mBtnLogin.isEnabled = !isLoading && isEmailValid && isPasswordValid
                    }
                }

                launch {
                    userViewModel.authEvents.collect { event ->
                        when (event) {
                            is UserViewModel.AuthEvent.Success -> {
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Successfully logged in!",
                                    Toast.LENGTH_SHORT
                                ).show()

                                storeUserPrefs(
                                    event.user
                                )
                                startMainActivity()
                                userViewModel.consumeAuthEvent()
                            }

                            is UserViewModel.AuthEvent.Error -> {
                                tvLoginErrorMsg.text = event.message
                                tvLoginErrorMsg.visibility = View.VISIBLE
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
}
