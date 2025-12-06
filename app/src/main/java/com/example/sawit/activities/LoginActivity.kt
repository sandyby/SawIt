package com.example.sawit.activities

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
import com.example.sawit.R
import com.example.sawit.ui.theme.BgPrimary400
import com.example.sawit.ui.theme.BgPrimary500
import com.example.sawit.ui.theme.BgSecondaryOverlay2
import com.example.sawit.ui.theme.Text600
import com.example.sawit.ui.theme.TextPrimary500
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.FirebaseDatabase
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

        /*
        * deklarasi variable untuk menampung sharedpreferences, yakni segala informasi
        * yang berkaitan dengan user settings, preference tertentu, session atau status login,
        * dan masih banyak lagi yang dapat disimpan dalam tipe data tersebut dalam bentuk key-value pair.
        *
        * contohnya di bawah ini, kami menyimpan email jika sudah ada sebelumnya, berarti menandakan user tersebut
        * sudah login sebelumnya, agar tidak perlu melakukan login lagi. ini bisa dikembangkan lebih jauh lagi kedepannya jika kami
        * memutuskan untuk meningkatkan UX dari segi session control, dan juga keamanan seperti session timeout, dsb
        * */
        val sharedPrefs = getSharedPreferences("UserSession", MODE_PRIVATE)
        val email = sharedPrefs.getString("email", null)

        if (email != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        setContentView(R.layout.activity_login)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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
        tietEmail.addTextChangedListener(EmailWatcher())
        tietPassword.addTextChangedListener(PasswordWatcher())
        tvSwitchRegister = findViewById<TextView>(R.id.tv_switchRegister)
        tvSwitchRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
        mBtnLogin = findViewById<MaterialButton>(R.id.mBtn_login)
        mBtnLogin.setOnClickListener {
            tvLoginErrorMsg.visibility = View.GONE
            val email = tietEmail.text.toString().trim()
            val password = tietPassword.text.toString().trim()
            Log.d("LoginActivity", "$email $password")
            userLogin(email, password)
        }
        smRememberMe = findViewById<SwitchMaterial>(R.id.sm_rememberMe)
        tvLoginErrorMsg = findViewById<TextView>(R.id.tv_login_error_msg)
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
        mBtnLogin.isEnabled = isEmailValid && isPasswordValid
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

    private fun userLogin(email: String, password: String) {
        mBtnLogin.isEnabled = false
        val hashedPassword = hashSHA256String(password)

        /*
        * retrieve instance dan referensi dengan realtime database dari firebase, sehingga menjalankan query dengan aman
        *
        * untuk url dari database itu sendiri awalnya kami ingin simpan ke dalam file yang aman, entah itu local.properties
        * ataupun secrets, tetapi setelah membaca saran dari beberapa user di forum, seperti tidak terlalu bermasalah 'jika' memang
        * terlanjur dipush secara publik
        * */
        FirebaseDatabase.getInstance(applicationContext.getString(R.string.firebase_database_reference_url))
            .getReference("users").orderByChild("email").equalTo(email).get()
            .addOnSuccessListener { snapshot ->
                Log.d("LoginActivity", "snapshot: $snapshot")
                if (snapshot.exists()) {

                    snapshot.children.forEach { data ->
                        val storedPassword = data.child("password").getValue(String::class.java)

                        /*
                        * di bagian ini sharedpreferences disimpan setelah user berhasil login.
                        *
                        * session tersebut akan expired atau time out jika user menghapus cache atau data dari
                        * aplikasi secara lokal
                        * */
                        if (storedPassword == hashedPassword) {
                            val fullName = data.child("fullName").getValue(String::class.java)
                            val sharedPrefs = getSharedPreferences("UserSession", MODE_PRIVATE)
                            val prefsEditor = sharedPrefs.edit()
                            if (smRememberMe.isChecked) {
                                prefsEditor.putString("email", email)
                                prefsEditor.putString("uid", data.key)
                            }
                            prefsEditor.putString("fullName", fullName)
                            prefsEditor.apply()
                            Log.d("LoginActivity", "tes1")
                            Toast.makeText(this, "Successfully logged in!", Toast.LENGTH_SHORT)
                                .show()

                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            Log.d("LoginActivity", "tes2")
                            finish()
                            return@addOnSuccessListener
                        }
                    }
                    Log.d("LoginActivity", "tes3")
                    tvLoginErrorMsg.visibility = View.VISIBLE
                    mBtnLogin.isEnabled = true
                } else {
                    Log.d("LoginActivity", "tes4")
                    tvLoginErrorMsg.visibility = View.VISIBLE
                    mBtnLogin.isEnabled = true
                }
            }.addOnFailureListener {
                Log.d("LoginActivity", "Error")
                mBtnLogin.isEnabled = true
            }
    }
}
