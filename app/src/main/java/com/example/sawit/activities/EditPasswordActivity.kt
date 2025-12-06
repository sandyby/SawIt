package com.example.sawit.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import com.example.sawit.R
import com.example.sawit.ui.EditPasswordScreen
import com.example.sawit.ui.theme.SawItTheme

class EditPasswordActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_OLD_PASSWORD = "EXTRA_OLD_PASSWORD"
        const val EXTRA_NEW_PASSWORD = "EXTRA_NEW_PASSWORD"
    }

//    override fun onCreate(savedInstanceState: Bundle?) {
//        enableEdgeToEdge()
//        super.onCreate(savedInstanceState)
//
//        setContent {
//            SawItTheme {
//                EditPasswordScreen(
//                    onBack = { finish() },
//                    onPasswordChanged = { old, new ->
//                        Toast.makeText(this, "Password successfully changed!", Toast.LENGTH_LONG)
//                            .show()
//                        val result = Intent().apply {
//                            putExtra(EXTRA_OLD_PASSWORD, old)
//                            putExtra(EXTRA_NEW_PASSWORD, new)
//                        }
//                        setResult(RESULT_OK, result)
//                        finish()
//                    }
//                )
//            }
//        }
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_password)
        WindowCompat.setDecorFitsSystemWindows(window, true)

//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        supportActionBar?.title = "Ubah Kata Sandi"
        supportActionBar?.hide()

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

            if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this, "Semua kolom wajib diisi.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPass != confirmPass) {
                Toast.makeText(this, "Kata Sandi baru dan konfirmasi tidak cocok.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPass.length < 6) {
                Toast.makeText(this, "Kata Sandi minimal 6 karakter.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Kata Sandi berhasil diubah!", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}