package com.example.sawit.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.example.sawit.R
import com.example.sawit.activities.EditPasswordActivity
import com.example.sawit.activities.EditProfileActivity
import com.example.sawit.activities.LoginActivity
import com.google.android.material.button.MaterialButton


class ProfileFragment : Fragment() {

    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var ivProfilePic: ImageView
    private lateinit var fullName: String
    private lateinit var email: String
    private lateinit var profilePicUri: String

    // Launcher untuk Edit Profile
    private lateinit var editProfileResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        editProfileResultLauncher = registerForActivityResult(StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data

                val newName = data?.getStringExtra("EXTRA_NEW_NAME")
                val newEmail = data?.getStringExtra("EXTRA_NEW_EMAIL")

                if (newName != null && newEmail != null) {
                    tvUserName.text = newName
                    tvUserEmail.text = newEmail
                    Toast.makeText(
                        requireContext(),
                        "(temporary) update nama berhasil",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val userSharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        fullName = userSharedPref.getString("fullName", "User") ?: "User"
        email = userSharedPref.getString("email", "user@gmail.com") ?: "user@gmail.com"
//        TODO:
//        val profilePicUri = userSharedPref.getString("PROFILE_PIC_URI", null) ?: "nanti ganti ke path placeholder"

        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvUserName = view.findViewById(R.id.tv_user_name)
        tvUserEmail = view.findViewById(R.id.tv_user_email)
        ivProfilePic = view.findViewById(R.id.iv_profilePic)

        tvUserName.text = fullName
        tvUserEmail.text = email

        val itemEditProfile = view.findViewById<ConstraintLayout>(R.id.item_edit_profile)
        val itemEditPassword = view.findViewById<ConstraintLayout>(R.id.item_edit_password)
        val mBtnLogOut = view.findViewById<MaterialButton>(R.id.mBtn_logout)
        val btnEditPic = view.findViewById<ImageButton>(R.id.btn_edit_profile_pic)

        // Edit Profile
        itemEditProfile.setOnClickListener {
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            intent.putExtra("EXTRA_INITIAL_NAME", tvUserName.text.toString())
            intent.putExtra("EXTRA_INITIAL_EMAIL", tvUserEmail.text.toString())
            editProfileResultLauncher.launch(intent)
        }

        // Edit Password
        itemEditPassword.setOnClickListener {
            val intent = Intent(requireContext(), EditPasswordActivity::class.java)
            startActivity(intent)
        }

        // Logout
        mBtnLogOut.setOnClickListener {
            clearUserSession()

            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }
    }


    private fun clearUserSession() {
        val authSharedPref =
            requireActivity().getSharedPreferences("AuthSession", Context.MODE_PRIVATE)
        authSharedPref.edit {
            remove("userId")
            putBoolean("rememberMe", false)
            apply()
        }
        val userSharedPref =
            requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        userSharedPref.edit {
            remove("fullName")
            remove("email")
        }
        Toast.makeText(requireContext(), "Successfully logged out!", Toast.LENGTH_SHORT).show()
    }
}
