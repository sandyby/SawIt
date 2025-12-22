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
import androidx.fragment.app.activityViewModels
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
    private val userViewModel: UserViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userViewModel.listenForUserUpdates()
        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.itemEditProfile.setOnClickListener {
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            intent.putExtra("EXTRA_INITIAL_NAME", binding.tvUserName.text.toString())
            intent.putExtra("EXTRA_INITIAL_EMAIL", binding.tvUserEmail.text.toString())
            startActivity(intent)
        }

        binding.itemEditPassword.setOnClickListener {
            val intent = Intent(requireContext(), EditPasswordActivity::class.java)
            startActivity(intent)
        }

        binding.mBtnLogout.setOnClickListener {
            userViewModel.logout()
            clearUserSession(requireContext())

            Toast.makeText(requireContext(), "Successfully logged out!", Toast.LENGTH_SHORT).show()

            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
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

                            loadProfilePicture(user.profilePhotoLocalPath, user.profilePhotoBase64)

                            updateUserPrefs(user.fullName, user.email)
                        } else {
                            binding.tvUserName.text = "User"
                            binding.ivProfilePic.setImageResource(R.drawable.ic_filled_person_24_secondary_overlay)
                        }
                    }
                }

                launch {
                    userViewModel.currentUser.collect { firebaseUser ->
                        if (firebaseUser == null) {
                            userViewModel.logout()
                            clearUserSession(requireContext())

                            Toast.makeText(
                                requireContext(),
                                "Session has expired, please log in again!",
                                Toast.LENGTH_SHORT
                            ).show()

                            val intent = Intent(requireContext(), LoginActivity::class.java)
                            intent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            requireActivity().finish()
                        }
                    }
                }
            }
        }
    }

    private fun loadProfilePicture(localPath: String?, base64String: String?) {
        val placeholderId = R.drawable.ic_filled_person_24_secondary_overlay
        if (ImageCacheManager.isCached(localPath)) {
            Glide.with(this)
                .load(File(localPath!!))
                .placeholder(placeholderId)
                .error(placeholderId)
                .into(binding.ivProfilePic)
            return
        }
        if (!base64String.isNullOrEmpty()) {
            val newLocalPath = ImageCacheManager.base64ToLocalCache(requireContext(), base64String)
            if (newLocalPath != null){
                val currentUserUid = userViewModel.currentUser.value?.uid
                if (currentUserUid != null) {
                    userViewModel.updateImageLocalPath(newLocalPath)
                }
                Glide.with(this)
                    .load(File(newLocalPath))
                    .placeholder(placeholderId)
                    .error(placeholderId)
                    .into(binding.ivProfilePic)
                return
            }
        }
        binding.ivProfilePic.setImageResource(placeholderId)
    }

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
