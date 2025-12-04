package com.example.sawit.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sawit.R
import com.google.android.material.textfield.TextInputEditText

class EditProfileActivity : AppCompatActivity() {

    private lateinit var currentFullName: String
    private lateinit var currentEmailAddress: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        supportActionBar?.hide()

        val tietFullName = findViewById<TextInputEditText>(R.id.tiet_full_name_field)
        val tietEmailAddress = findViewById<TextInputEditText>(R.id.tiet_email_address_field)
        val btnSave = findViewById<Button>(R.id.btn_save_profile)
        val ivBack = findViewById<ImageView>(R.id.iv_back)

        currentFullName = intent.getStringExtra("EXTRA_INITIAL_NAME") ?: "John Doe"
        currentEmailAddress = intent.getStringExtra("EXTRA_INITIAL_EMAIL") ?: "john.doe@gmail.com"

        tietFullName.setText(currentFullName)
        tietEmailAddress.setText(currentEmailAddress)

        ivBack.setOnClickListener {
            finish()
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

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}