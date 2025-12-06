package com.example.sawit.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.example.sawit.R
import com.example.sawit.activities.EditPasswordActivity
import com.example.sawit.activities.EditProfileActivity
import com.example.sawit.activities.LoginActivity
import com.google.android.material.button.MaterialButton

class ProfileFragment : Fragment() {

    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView


    // Launcher untuk Edit Profile
    private val editProfileResultLauncher =
        registerForActivityResult(StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data

                val newName = data?.getStringExtra("EXTRA_NEW_NAME")
                val newEmail = data?.getStringExtra("EXTRA_NEW_EMAIL")

                if (newName != null && newEmail != null) {
                    tvUserName.text = newName
                    tvUserEmail.text = newEmail
                    Toast.makeText(requireContext(), "(temporary) update nama berhasil", Toast.LENGTH_SHORT).show()
                }
            }
        }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvUserName = view.findViewById(R.id.tv_user_name)
        tvUserEmail = view.findViewById(R.id.tv_user_email)


        tvUserName.text = "John Doe"
        tvUserEmail.text = "john.doe@gmail.com"

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
        val sharedPref = requireActivity().getSharedPreferences("AUTH_PREFS", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            remove("USER_TOKEN")
            remove("USER_ID")
            apply()
        }
        Toast.makeText(requireContext(), "Successfully logged out!", Toast.LENGTH_SHORT).show()
    }
}
