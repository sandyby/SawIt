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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.sawit.R
import com.example.sawit.activities.CreateFieldActivity
import com.example.sawit.activities.MainActivity
import com.example.sawit.adapters.ActivitiesTimelineAdapter
import com.example.sawit.adapters.FieldsDashboardAdapter
import com.example.sawit.databinding.FragmentHomeBinding
import com.example.sawit.models.ActivityStatus
import com.example.sawit.models.ActivityTimelineItem
import com.example.sawit.models.Field
import com.example.sawit.ui.NotificationIconWithBadge
import com.example.sawit.ui.components.ActivityTimelineList
import com.example.sawit.utils.HorizontalSpaceItemDecoration
import com.example.sawit.utils.ImageCacheManager
import com.example.sawit.utils.toTimelineItem
import com.example.sawit.viewmodels.ActivityViewModel
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
    private val activityViewModel: ActivityViewModel by activityViewModels()
//    private lateinit var activitiesAdapter: ActivitiesTimelineAdapter
//    private lateinit var fullName: String
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

            NotificationIconWithBadge(
                count = notificationCount,
                onClick = {
                    notificationViewModel.increment()
                    Log.d("HomeFragment", "Notification icon clicked — count = $notificationCount")
                }
            )
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = FieldsDashboardAdapter(
            onClick = { field ->
                Toast.makeText(context, "clicked: ${field.fieldName}", Toast.LENGTH_SHORT).show()
            },
            onAddClick = {
                val intent = Intent(requireContext(), CreateFieldActivity::class.java)
                createFieldLauncher.launch(intent)
            }
        )

        adapter.submitList(emptyList())

        binding.tvFieldsViewMore.setOnClickListener {
            val mainActivity = activity as? MainActivity
            mainActivity?.navigateToFieldsFragment()
        }

        binding.tvActivitiesViewMore.setOnClickListener {
            val mainActivity = activity as? MainActivity
            mainActivity?.navigateToActivitiesFragment()
        }

        binding.cvActivitiesTimeline.setContent {
            val activitiesFlow by activityViewModel.activities.collectAsState(initial = emptyList())

            val timelineItems = remember(activitiesFlow) {
                activitiesFlow
                    .map { it.toTimelineItem() }
                    .sortedWith(compareBy<ActivityTimelineItem> {
                        when(it.status) {
                            ActivityStatus.UPCOMING -> 0
                            ActivityStatus.TODAY -> 1
                            ActivityStatus.COMPLETED -> 2
                            ActivityStatus.OVERDUE -> 3
                        }
                    }.thenByDescending { it.date })
                    .take(3)
            }

            MaterialTheme {
                ActivityTimelineList(
                    items = timelineItems,
                    onItemClick = { item ->
                        Toast.makeText(requireContext(), "Clicked on ${item.activityTitle}", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        binding.rvFieldsOverview.apply {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
        val spacingInPx = resources.getDimensionPixelSize(R.dimen.horizontal_item_spacing)
        binding.rvFieldsOverview.addItemDecoration(HorizontalSpaceItemDecoration(spacingInPx))

        fieldViewModel.listenForFieldsUpdates()
        activityViewModel.listenForActivitiesUpdate()
        userViewModel.listenForUserUpdates()
//        setupActivitiesTimeline()
        observeViewModel(adapter)
    }

//    private fun setupActivitiesTimeline() {
//        activitiesAdapter = ActivitiesTimelineAdapter { item ->
//            Toast.makeText(requireContext(), "Clicked on ${item.activityTitle}", Toast.LENGTH_SHORT)
//                .show()
//        }
//
//        binding.rvActivitiesTimeline.adapter = activitiesAdapter
//        binding.rvActivitiesTimeline.isNestedScrollingEnabled = false
//
////        val dummyData = listOf(
////            ActivityTimelineItem(
////                "1",
////                "Lahan Manjur Sukses",
////                "Harvesting Crops",
////                "27/10/2025",
////                ActivityStatus.UPCOMING
////            ),
////            ActivityTimelineItem("2", "Lahan 1", "Watering Crops", "07/10/2025", ActivityStatus.TODAY),
////            ActivityTimelineItem("3", "Lahan Manjur Sukses", "Fertilizing", "20/09/2025", ActivityStatus.COMPLETED),
////            ActivityTimelineItem("4", "Lahan 2", "Soil Testing", "15/09/2025", ActivityStatus.COMPLETED)
////        )
////        activitiesAdapter.submitList(dummyData)
//    }

    private fun observeViewModel(adapter: FieldsDashboardAdapter) {
//        viewLifecycleOwner.lifecycleScope.launch {
//            activityViewModel.activities.collectLatest { activityList ->
//                val filteredList = activityList
//                    .filter { it.status.lowercase() != "completed" }
//
//                val sortedList = filteredList.sortedBy { it.date }
//
//                val dashboardList = sortedList.take(3)
//
//                val timelineItems = dashboardList.map { it.toTimelineItem() }
//
//                activitiesAdapter.submitList(timelineItems)
//
//                binding.rvActivitiesTimeline.visibility =
//                    if (timelineItems.isEmpty()) View.GONE else View.VISIBLE
//            }
//        }

        viewLifecycleOwner.lifecycleScope.launch {
            userViewModel.userProfile.collectLatest { user ->
                val placeholderId = R.drawable.placeholder_64
                if (user != null) {
                    binding.tvFullName.text = user.fullName
                    loadProfilePicture(
                        user.profilePhotoLocalPath,
                        user.profilePhotoBase64,
                        placeholderId,
                        onCacheSuccess = { newLocalPath ->
                            userViewModel.updateImageLocalPath(newLocalPath)
                        }
                    )
                } else {
                    binding.tvFullName.text = "User"
                    binding.civDashboardProfilePicture.setImageResource(placeholderId)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            fieldViewModel.fieldsData.collectLatest { fields ->
                val dashboardList = fields.take(2)
                val finalDashboardList = if (dashboardList.size < 2) {
                    // This assumes your Field model has a special, distinguishable object
                    // (e.g., Field.ADD_PLACEHOLDER) that the FieldsDashboardAdapter
                    // recognizes in getItemViewType() to draw the button.
                    // If you don't have this, you MUST create it!
                    dashboardList + Field.ADD_PLACEHOLDER
                } else {
                    dashboardList
                }

                val wasEmptyBefore = adapter.currentList.isEmpty() && fields.isNotEmpty()

                adapter.submitList(finalDashboardList) {
                    if (dashboardList.isNotEmpty()) {
                        binding.rvFieldsOverview.scrollToPosition(0)
                    }
                    if (wasEmptyBefore) {
                        binding.rvFieldsOverview.requestLayout()
                    }
                }
                binding.rvFieldsOverview.visibility = View.VISIBLE
            }
        }
    }

    private fun loadProfilePicture(
        localPath: String?,
        base64String: String?,
        placeholderId: Int,
        onCacheSuccess: (String) -> Unit
    ) {
        val placeholderId = R.drawable.placeholder_64

        if (ImageCacheManager.isCached(localPath)) {
            Glide.with(this)
                .load(File(localPath!!))
                .placeholder(placeholderId)
                .error(placeholderId)
                .into(binding.civDashboardProfilePicture)
        } else if (!base64String.isNullOrEmpty()) {
            val newLocalPath = ImageCacheManager.base64ToLocalCache(requireContext(), base64String)

            if (newLocalPath != null) {
                Glide.with(this)
                    .load(File(newLocalPath))
                    .placeholder(placeholderId)
                    .error(placeholderId)
                    .into(binding.civDashboardProfilePicture)
                if (localPath.isNullOrEmpty()) {
                    onCacheSuccess(newLocalPath)
                }
            } else {
                binding.civDashboardProfilePicture.setImageResource(placeholderId)
            }
        } else {
            binding.civDashboardProfilePicture.setImageResource(placeholderId)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
