package com.example.sawit.fragments

import android.app.Activity
import android.content.Context
import android.content.Context.MODE_PRIVATE
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
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.example.sawit.R
import com.example.sawit.activities.EditPasswordActivity
import com.example.sawit.activities.EditProfileActivity
import com.example.sawit.activities.LoginActivity
import com.example.sawit.databinding.FragmentProfileBinding
import com.example.sawit.utils.ImageCacheManager
import com.example.sawit.viewmodels.UserViewModel
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.io.File


class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
//    private lateinit var tvUserName: TextView
//    private lateinit var tvUserEmail: TextView
//    private lateinit var ivProfilePic: ImageView
//    private lateinit var fullName: String
//    private lateinit var email: String
//    private lateinit var profilePicUri: String

    // Launcher untuk Edit Profile
    private val userViewModel: UserViewModel by viewModels()
//    private lateinit var editProfileResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        editProfileResultLauncher = registerForActivityResult(StartActivityForResult()) { result ->
//            if (result.resultCode == Activity.RESULT_OK) {
//                val data: Intent? = result.data
//
//                val newName = data?.getStringExtra("EXTRA_NEW_NAME")
//                val newEmail = data?.getStringExtra("EXTRA_NEW_EMAIL")
//
//                if (newName != null && newEmail != null) {
//                    tvUserName.text = newName
//                    tvUserEmail.text = newEmail
//                    Toast.makeText(
//                        requireContext(),
//                        "(temporary) update nama berhasil",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//            }
//        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
        //        val userSharedPref =
//            requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
//        fullName = userSharedPref.getString("fullName", "User") ?: "User"
//        email = userSharedPref.getString("email", "user@gmail.com") ?: "user@gmail.com"
////        TODO:
////        val profilePicUri = userSharedPref.getString("PROFILE_PIC_URI", null) ?: "nanti ganti ke path placeholder"
//
//        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userViewModel.listenForUserUpdates()
        observeViewModel()

        setupListeners()

//        tvUserName = view.findViewById(R.id.tv_user_name)
//        tvUserEmail = view.findViewById(R.id.tv_user_email)
//        ivProfilePic = view.findViewById(R.id.iv_profilePic)
//
//        tvUserName.text = fullName
//        tvUserEmail.text = email
//
//        val itemEditProfile = view.findViewById<ConstraintLayout>(R.id.item_edit_profile)
//        val itemEditPassword = view.findViewById<ConstraintLayout>(R.id.item_edit_password)
//        val mBtnLogOut = view.findViewById<MaterialButton>(R.id.mBtn_logout)
//        val btnEditPic = view.findViewById<ImageButton>(R.id.btn_edit_profile_pic)
//
//        // Edit Profile
//        itemEditProfile.setOnClickListener {
//            val intent = Intent(requireContext(), EditProfileActivity::class.java)
//            intent.putExtra("EXTRA_INITIAL_NAME", tvUserName.text.toString())
//            intent.putExtra("EXTRA_INITIAL_EMAIL", tvUserEmail.text.toString())
//            editProfileResultLauncher.launch(intent)
//        }
//
//        // Edit Password
//        itemEditPassword.setOnClickListener {
//            val intent = Intent(requireContext(), EditPasswordActivity::class.java)
//            startActivity(intent)
//        }
//
//        // Logout
//        mBtnLogOut.setOnClickListener {
//            userViewModel.logout()
//            clearUserSession(requireContext())
//
//            Toast.makeText(requireContext(), "Successfully logged out!", Toast.LENGTH_SHORT).show()
//
//            val intent = Intent(requireContext(), LoginActivity::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            startActivity(intent)
//            requireActivity().finish()
//
////            val intent = Intent(requireContext(), LoginActivity::class.java)
////            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
////            startActivity(intent)
////            requireActivity().finish()
//        }
    }

    private fun setupListeners() {
        // Edit Profile
        binding.itemEditProfile.setOnClickListener {
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            // Pass the current *observed* values from the ViewModel, not SharedPreferences
            intent.putExtra("EXTRA_INITIAL_NAME", binding.tvUserName.text.toString())
            intent.putExtra("EXTRA_INITIAL_EMAIL", binding.tvUserEmail.text.toString())
            startActivity(intent)
        }

        // Edit Password
        binding.itemEditPassword.setOnClickListener {
            val intent = Intent(requireContext(), EditPasswordActivity::class.java)
            startActivity(intent)
        }

        // Logout
        binding.mBtnLogout.setOnClickListener {
            userViewModel.logout()
            clearUserSession(requireContext())

            Toast.makeText(requireContext(), "Successfully logged out!", Toast.LENGTH_SHORT).show()

            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }

        // Edit profile picture button
        binding.btnEditProfilePic.setOnClickListener {
            // Placeholder for opening image picker or photo logic
            Toast.makeText(requireContext(), "Edit picture functionality coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    userViewModel.userProfile.collect { user ->
                        if (user != null) {
                            binding.tvUserName.text = user.fullName
                            binding.tvUserEmail.text = user.email

                            val localPath = user.profilePhotoLocalPath
                            val base64String = user.profilePhotoBase64

                            if (ImageCacheManager.isCached(localPath)) {
                                // 1a. Load from local cache (FAST)
                                Glide.with(this@ProfileFragment)
                                    .load(File(localPath!!))
                                    .placeholder(R.drawable.ic_filled_person_24_secondary_overlay)
                                    .into(binding.ivProfilePic)
                            }
// 2. If no local cache, check the database Base64 string
                            else if (!base64String.isNullOrEmpty()) {
                                // 2a. Fetch from DB (Base64) -> Save to local cache -> Load from cache

                                // Convert Base64 back to file and save locally
                                val newLocalPath = ImageCacheManager.base64ToLocalCache(requireContext(), base64String)

                                if (newLocalPath != null) {
                                    // After successfully caching, update the database with the new local path
                                    // (Optional but ensures persistence if app data is cleared, avoiding Base64 decode next time)
                                    // userViewModel.updateLocalPathInDb(newLocalPath) // Requires a new VM function

                                    // Load image from the newly created local path
                                    Glide.with(this@ProfileFragment)
                                        .load(File(newLocalPath))
                                        .placeholder(R.drawable.ic_filled_person_24_secondary_overlay)
                                        .into(binding.ivProfilePic)
                                } else {
                                    // Base64 decoding failed
                                    binding.ivProfilePic.setImageResource(R.drawable.ic_filled_person_24_secondary_overlay)
                                }
                            } else {
                                // 3. Load placeholder
                                binding.ivProfilePic.setImageResource(R.drawable.ic_filled_person_24_secondary_overlay)
                            }

                            // Load image if path exists (assuming you save the path to the DB/Prefs)
//                            user.profilePhotoPath?.let { path ->
//                                Glide.with(this@ProfileFragment)
//                                    .load(Uri.parse(path)) // Assuming path is a local URI/file path
//                                    .placeholder(R.drawable.ic_profile_placeholder)
//                                    .into(binding.ivProfilePic)
//                            }

                            // Update SharedPreferences in the background (optional, but good for cold start persistence)
                            updateUserPrefs(user.fullName, user.email)
                        } else {
                            // Handle case where user profile data is not found (e.g., failed DB read)
                            binding.tvUserName.text = "User"
                        }
                    }
                }

                // Optional: Observe the FirebaseUser for authentication state changes (if needed)
                launch {
                    userViewModel.currentUser.collect { firebaseUser ->
                        if (firebaseUser == null) {
                            userViewModel.logout()
                            clearUserSession(requireContext())

                            Toast.makeText(requireContext(), "Please log in first!", Toast.LENGTH_SHORT).show()

                            val intent = Intent(requireContext(), LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            requireActivity().finish()
                        }
                    }
                }
            }
        }
    }

//    fun userLogout() {
//        userViewModel.logout()
//
//        val authSharedPref =
//            requireActivity().getSharedPreferences("AuthSession", Context.MODE_PRIVATE)
//        authSharedPref.edit { clear(); apply() }
//        requireActivity().getSharedPreferences("UserPrefs", MODE_PRIVATE).edit { clear(); apply() }
//
//        val intent = Intent(requireActivity(), LoginActivity::class.java)
//        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        startActivity(intent)
//        finish()
//    }

    private fun updateUserPrefs(fullName: String, email: String) {
        requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE).edit {
            putString("fullName", fullName)
            putString("email", email)
            apply()
        }
    }

    private fun clearUserSession(context: Context) {
        val authSharedPref =
            context.getSharedPreferences("AuthSession", Context.MODE_PRIVATE)
        authSharedPref.edit {
            remove("userId")
            putBoolean("rememberMe", false)
            apply()
        }
        val userSharedPref =
            requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        userSharedPref.edit {
            clear()
            apply()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
