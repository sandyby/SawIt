package com.example.sawit.fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.sawit.R
import com.example.sawit.activities.CreateEditActivityActivity
import com.example.sawit.activities.CreateEditFieldActivity
import com.example.sawit.activities.DetailBottomSheetActivity
import com.example.sawit.activities.MainActivity
import com.example.sawit.adapters.FieldsDashboardAdapter
import com.example.sawit.databinding.ActivityDetailBottomSheetBinding
import com.example.sawit.databinding.FragmentHomeBinding
import com.example.sawit.models.ActivityStatus
import com.example.sawit.models.ActivityTimelineItem
import com.example.sawit.models.Field
import com.example.sawit.ui.NotificationIconWithBadge
import com.example.sawit.ui.WeatherCard
import com.example.sawit.ui.components.ActivityTimelineList
import com.example.sawit.utils.HorizontalSpaceItemDecoration
import com.example.sawit.utils.ImageCacheManager
import com.example.sawit.utils.toTimelineItem
import com.example.sawit.viewmodels.ActivityViewModel
import com.example.sawit.viewmodels.FieldViewModel
import com.example.sawit.viewmodels.NotificationViewModel
import com.example.sawit.viewmodels.UserViewModel
import com.example.sawit.viewmodels.WeatherViewModel
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val fieldViewModel: FieldViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()
    private val activityViewModel: ActivityViewModel by activityViewModels()
    private val notificationViewModel: NotificationViewModel by activityViewModels()
    private val weatherViewModel: WeatherViewModel by activityViewModels()
    private lateinit var createFieldLauncher: ActivityResultLauncher<Intent>
    private val requestLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            fetchLocationAndWeather()
        } else {
            Toast.makeText(requireContext(), "Location denied. Showing default weather and location!", Toast.LENGTH_SHORT).show()
            weatherViewModel.fetchWeather(-6.2088, 106.8456)
        }
    }
    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("NotificationPermission", "Permission granted")
        } else {
            Toast.makeText(requireContext(), "Reminders disabled!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStart() {
        super.onStart()
        notificationViewModel.listenForNotifications()
    }

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

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDashboardRecyclerView()
        setupTimeline()
        setupClickListeners()
        setupComposables()

        fetchLocationAndWeather()

        fieldViewModel.listenForFieldsUpdates()
        activityViewModel.listenForActivitiesUpdate()
        userViewModel.listenForUserUpdates()

        observeViewModelData()
        checkAndRequestNotificationPermission()
    }

    private fun setupDashboardRecyclerView() {
        val adapter = FieldsDashboardAdapter(
            onClick = { field ->
                val detailsFragment =
                    FieldsDetailFragment.newInstance(field.fieldId, field.fieldName)

                parentFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        R.anim.slide_in_right, R.anim.slide_in_left,
                        R.anim.slide_in_left, R.anim.slide_in_right
                    )
                    .replace(R.id.fl_scroll_view_content, detailsFragment)
                    .addToBackStack(null)
                    .commit()
            },
            onAddClick = {
                val intent = Intent(requireContext(), CreateEditFieldActivity::class.java)
                createFieldLauncher.launch(intent)
            }
        )

        adapter.submitList(emptyList())

        binding.rvFieldsOverview.apply {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
        val spacingInPx = resources.getDimensionPixelSize(R.dimen.horizontal_item_spacing)
        binding.rvFieldsOverview.addItemDecoration(HorizontalSpaceItemDecoration(spacingInPx))
    }

    private fun setupClickListeners() {
        binding.tvFieldsViewMore.setOnClickListener {
            val mainActivity = activity as? MainActivity
            mainActivity?.navigateToFieldsFragment()
        }

        binding.tvActivitiesViewMore.setOnClickListener {
            val mainActivity = activity as? MainActivity
            mainActivity?.navigateToActivitiesFragment()
        }
    }

    private fun setupTimeline() {
        binding.cvActivitiesTimeline.setContent {
            val activitiesFlow by activityViewModel.activities.collectAsState(initial = emptyList())

            val timelineItems = remember(activitiesFlow) {
                activitiesFlow
                    .map { it.toTimelineItem() }
                    .sortedWith(compareBy<ActivityTimelineItem> {
                        when (it.status) {
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
                        val originalActivity = activitiesFlow.find { it.id == item.id }
                        if (originalActivity != null) {
                            if (item.status == ActivityStatus.OVERDUE) {
                                MaterialAlertDialogBuilder(requireContext(), R.style.CustomAlertDialogTheme)
                                    .setTitle("Activity Overdue")
                                    .setMessage("Would you like to reschedule this task or view details?")
                                    .setPositiveButton("Reschedule") { _, _ ->
                                        val intent = Intent(requireContext(), CreateEditActivityActivity::class.java).apply {
                                            putExtra(CreateEditActivityActivity.EXTRA_ACTIVITY, originalActivity)
                                        }
                                        startActivity(intent)
                                    }
                                    .setNegativeButton("View Details") { _, _ ->
                                        val bottomSheet = DetailBottomSheetActivity(originalActivity)
                                        bottomSheet.show(parentFragmentManager, "ActivityDetailBottomSheet")
                                    }
                                    .show()
                            } else {
                                val bottomSheet = DetailBottomSheetActivity(originalActivity)
                                bottomSheet.show(parentFragmentManager, "ActivityDetailBottomSheet")
                            }
                        }
                    }
                )
            }
        }
    }

    private fun setupComposables() {
//        binding.cvNotification.setContent {
//            val count by notificationViewModel.notificationCount.observeAsState(0)
//            NotificationIconWithBadge(
//                count = count,
//                onClick = {
//                    notificationViewModel.markAllAsRead()
//                }
//            )
//        }

        binding.cvWeatherCard.setContent {
            val weatherState by weatherViewModel.weatherState.collectAsState()
            WeatherCard(state = weatherState)
        }
        weatherViewModel.fetchWeather(-0.026330, 109.342504)
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun fetchLocationAndWeather() {
        if (hasLocationPermission()) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    weatherViewModel.fetchWeather(location.latitude, location.longitude)
                } else {
                    val currentRequestBuilder = CurrentLocationRequest.Builder()
                        .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
                        .setDurationMillis(5000)
                        .build()

                    fusedLocationClient.getCurrentLocation(currentRequestBuilder, null)
                        .addOnSuccessListener { freshLocation ->
                            if (freshLocation != null) {
                                weatherViewModel.fetchWeather(freshLocation.latitude, freshLocation.longitude)
                            } else {
                                weatherViewModel.fetchWeather(-6.2088, 106.8456)
                            }
                        }
                        .addOnFailureListener {
                            weatherViewModel.fetchWeather(-6.2088, 106.8456)
                        }
                }
            }
        } else {
            requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }

    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    //
                }
                else -> {
                    requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    private fun observeViewModelData() {
        val adapter = binding.rvFieldsOverview.adapter as FieldsDashboardAdapter

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

        viewLifecycleOwner.lifecycleScope.launch {
            weatherViewModel.weatherState.collectLatest { weather ->
                weather?.let {
                    //
                }
            }
        }

    //        viewLifecycleOwner.lifecycleScope.launch {
//            userViewModel.userProfile.collectLatest { user ->
//                val placeholderId = R.drawable.placeholder_64
//                if (user != null) {
//                    binding.tvFullName.text = user.fullName
//                    loadProfilePicture(
//                        user.profilePhotoLocalPath,
//                        user.profilePhotoBase64,
//                        placeholderId,
//                        onCacheSuccess = { newLocalPath ->
//                            userViewModel.updateImageLocalPath(newLocalPath)
//                        }
//                    )
//                } else {
//                    binding.tvFullName.text = "User"
//                    binding.civDashboardProfilePicture.setImageResource(placeholderId)
//                }
//            }
//        }
//
//        viewLifecycleOwner.lifecycleScope.launch {
//            fieldViewModel.fieldsData.collectLatest { fields ->
//                val dashboardList = fields.take(2)
//                val finalDashboardList = if (dashboardList.size < 2) {
//                    dashboardList + Field.ADD_PLACEHOLDER
//                } else {
//                    dashboardList
//                }
//
//                val wasEmptyBefore = adapter.currentList.isEmpty() && fields.isNotEmpty()
//
//                adapter.submitList(finalDashboardList) {
//                    if (dashboardList.isNotEmpty()) {
//                        binding.rvFieldsOverview.scrollToPosition(0)
//                    }
//                    if (wasEmptyBefore) {
//                        binding.rvFieldsOverview.requestLayout()
//                    }
//                }
//                binding.rvFieldsOverview.visibility = View.VISIBLE
//            }
//        }
//
//        viewLifecycleOwner.lifecycleScope.launch {
//            weatherViewModel.weatherData.collectLatest { weather ->
//                weather?.let {
//                    updateWeatherUI(it)
//                }
//            }
//        }
    }

    private fun loadProfilePicture(
        localPath: String?,
        base64String: String?,
        placeholderId: Int,
        onCacheSuccess: (String) -> Unit
    ) {
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
