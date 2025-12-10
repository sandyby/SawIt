package com.example.sawit.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.example.sawit.R
import com.google.android.material.textfield.TextInputEditText

class EditProfileActivity : AppCompatActivity() {

    private lateinit var currentFullName: String
    private lateinit var currentEmailAddress: String
    private lateinit var ivProfilePic: ImageView

    // Launcher untuk memilih gambar dari gallery
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                ivProfilePic.setImageURI(uri)
                saveProfilePicToPreferences(uri.toString())
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile)
        WindowCompat.setDecorFitsSystemWindows(window, true)

        ivProfilePic = findViewById(R.id.iv_profile_picture_edit)
        val btnEditProfilePic = findViewById<ImageButton>(R.id.btn_edit_profile_pic)
        val tietFullName = findViewById<TextInputEditText>(R.id.tiet_full_name_field)
        val tietEmailAddress = findViewById<TextInputEditText>(R.id.tiet_email_address_field)
        val btnSave = findViewById<Button>(R.id.btn_save_profile)
        val ivBack = findViewById<ImageView>(R.id.iv_back)


        // Load stored image (optional)
//        loadProfilePic()

        currentFullName = intent.getStringExtra("EXTRA_INITIAL_NAME") ?: "John Doe"
        currentEmailAddress = intent.getStringExtra("EXTRA_INITIAL_EMAIL") ?: "john.doe@gmail.com"

        tietFullName.setText(currentFullName)
        tietEmailAddress.setText(currentEmailAddress)

        ivBack.setOnClickListener {
            finish()
        }

        btnEditProfilePic.setOnClickListener {
            Log.d("EditProfile", "btn_edit_profile_pic clicked")
            pickImageLauncher.launch("image/*")
        }

        btnSave.setOnClickListener {
            val newName = tietFullName.text.toString().trim()

            if (newName.isEmpty()) {
                Toast.makeText(this, "Please enter your full name!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Profile successfully changed!", Toast.LENGTH_SHORT).show()

            val resultIntent = Intent()
            resultIntent.putExtra("EXTRA_NEW_NAME", newName)
            resultIntent.putExtra("EXTRA_NEW_EMAIL", currentEmailAddress)

            setResult(Activity.RESULT_OK, resultIntent)
            finish()
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
}
