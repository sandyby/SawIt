package com.example.sawit.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.sawit.R
import com.example.sawit.activities.CreateFieldActivity
import com.example.sawit.activities.MainActivity
import com.example.sawit.adapters.FieldsDashboardAdapter
import com.example.sawit.databinding.FragmentHomeBinding
import com.example.sawit.ui.NotificationIconWithBadge
import com.example.sawit.utils.HorizontalSpaceItemDecoration
import com.example.sawit.utils.ImageCacheManager
import com.example.sawit.viewmodels.FieldViewModel
import com.example.sawit.viewmodels.NotificationViewModel
import com.example.sawit.viewmodels.UserViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val fieldViewModel: FieldViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()
    private lateinit var fullName: String
    private val notificationViewModel: NotificationViewModel by activityViewModels()
    private lateinit var createFieldLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createFieldLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val mainActivity = activity as? MainActivity
                mainActivity?.navigateToFieldsFragment()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        binding.cvNotification.setContent {
            val notificationCount by notificationViewModel.notificationCount.observeAsState(0)

//            var notificationCount by remember { mutableIntStateOf(0) }

            NotificationIconWithBadge(
                count = notificationCount,
                onClick = {
                    notificationViewModel.increment()
                    Log.d("HomeFragment", "Notification icon clicked — count = $notificationCount")
                }
            )
        }

//        val userSharedPref =
//            requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
//        fullName = userSharedPref.getString("fullName", "User") ?: "User"

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userViewModel.listenForUserUpdates()

        val adapter = FieldsDashboardAdapter(
            onClick = { field ->
                Toast.makeText(context, "clicked: ${field.fieldName}", Toast.LENGTH_SHORT).show()
            },
            onAddClick = {
                val intent = Intent(requireContext(), CreateFieldActivity::class.java)
                createFieldLauncher.launch(intent)
                //                startActivity(intent)
            }
        )

        binding.tvFieldsViewMore.setOnClickListener {
            val mainActivity = activity as? MainActivity
            mainActivity?.navigateToFieldsFragment()
        }
//            val transaction = supportFragmentManager.beginTransaction()
//            transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_in_left)
//            transaction.replace(R.id.fl_scroll_view_content, fragment).commit()
////            val intent = Intent(requireContext(), Fe::class.java)
////            startActivity(intent)
//        }


//        binding.tvFullName.text = fullName

        binding.rvFieldsOverview.apply {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
        val spacingInPx = resources.getDimensionPixelSize(R.dimen.horizontal_item_spacing)
        binding.rvFieldsOverview.addItemDecoration(HorizontalSpaceItemDecoration(spacingInPx))

        observeViewModel(adapter)
    }

    private fun observeViewModel(adapter: FieldsDashboardAdapter) {
        viewLifecycleOwner.lifecycleScope.launch {
            userViewModel.userProfile.collectLatest { user ->
                val placeholderId = R.drawable.placeholder_64 // Ensure this resource exists

                if (user != null) {
                    // Update Full Name
                    binding.tvFullName.text = user.fullName

                    // Load Profile Picture
                    loadProfilePicture(user.profilePhotoLocalPath, user.profilePhotoBase64, placeholderId)
                } else {
                    // Handle case where user data is not yet loaded/available
                    binding.tvFullName.text = "User"
                    binding.civDashboardProfilePicture.setImageResource(placeholderId)
                }
            }
        }

        // --- Fields Overview Observer (Existing Logic) ---
        viewLifecycleOwner.lifecycleScope.launch {
            fieldViewModel.fieldsData.collectLatest { fields ->
                val dashboardList = fields.take(2)

                // For the oversized card fix: Capture state before submitting
                val wasEmptyBefore = adapter.currentList.isEmpty() && fields.isNotEmpty()

                adapter.submitList(dashboardList) {
                    if (dashboardList.isNotEmpty()) {
                        binding.rvFieldsOverview.scrollToPosition(0)
                    }

                    // Fix the width of the Add Field card upon transition from empty to non-empty
                    if (wasEmptyBefore) {
                        binding.rvFieldsOverview.requestLayout()
                    }
                }
                binding.rvFieldsOverview.visibility = View.VISIBLE
            }
        }
    }

    private fun loadProfilePicture(localPath: String?, base64String: String?, placeholderId: Int) {
        val placeholderId = R.drawable.placeholder_64 // Use your specific placeholder for this size

        if (ImageCacheManager.isCached(localPath)) {
            // 1. Load from local cache
            Glide.with(this)
                .load(File(localPath!!))
                .placeholder(placeholderId)
                .error(placeholderId)
                .into(binding.civDashboardProfilePicture)
        }
        else if (!base64String.isNullOrEmpty()) {
            // 2. Fetch from DB (Base64) -> Save to local cache -> Load
            val newLocalPath = ImageCacheManager.base64ToLocalCache(requireContext(), base64String)

            if (newLocalPath != null) {
                Glide.with(this)
                    .load(File(newLocalPath))
                    .placeholder(placeholderId)
                    .error(placeholderId)
                    .into(binding.civDashboardProfilePicture)
            } else {
                // Base64 decoding failed
                binding.civDashboardProfilePicture.setImageResource(placeholderId)
            }
        } else {
            // 3. Load placeholder
            binding.civDashboardProfilePicture.setImageResource(placeholderId)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
