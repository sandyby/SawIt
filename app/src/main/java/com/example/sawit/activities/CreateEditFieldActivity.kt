package com.example.sawit.activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.example.sawit.BuildConfig
import com.example.sawit.R
import com.example.sawit.databinding.ActivityCreateEditFieldBinding
import com.example.sawit.models.Field
import com.example.sawit.models.FieldLocation
import com.example.sawit.viewmodels.FieldViewModel
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Locale
import java.util.UUID

class CreateEditFieldActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityCreateEditFieldBinding
    private val fieldViewModel: FieldViewModel by viewModels()

    private var currentField: Field? = null
    private var isEditMode = false
    private var selectedLatLng: LatLng? = null
    private var selectedAddress: String? = null
    private var locationMarker: Marker? = null
    private var selectedImageUri: Uri? = null
    private var finalImagePath: String? = null

    companion object {
        const val EXTRA_FIELD = "EXTRA_FIELD"
        private val DEFAULT_LOCATION = LatLng(-0.789275, 113.921327)
        private const val LOCATION_PERMISSION_REQUEST_CODE = 100
    }

    private val currentUserId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCreateEditFieldBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        showLoading(true)

        imagePickerLauncher = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                Glide.with(this).load(it).into(binding.ivFieldPhoto)
            }
        }

        if (intent.hasExtra(EXTRA_FIELD)) {
            currentField = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(EXTRA_FIELD, Field::class.java)
            } else {
                @Suppress("DEPRECATION") intent.getParcelableExtra(EXTRA_FIELD)
            }
            isEditMode = currentField != null
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setupUI()
        setupListeners()
        setupMap()
        observeViewModelEvents()

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map_fragment_container) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupSearchView()

        if (isEditMode) {
            currentField?.let {
                populateForm(it)
            }
        }
    }

    private fun setupSearchView() {
        binding.searchViewLocation.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    searchLocation(query)
                    binding.searchViewLocation.clearFocus() // Hide keyboard
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean = false
        })
    }

    private fun searchLocation(locationName: String) {
        val geocoder = Geocoder(this, Locale.getDefault())

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val addresses = geocoder.getFromLocationName(locationName, 1)

                launch(Dispatchers.Main) {
                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        val latLng = LatLng(address.latitude, address.longitude)

                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                        placeMarker(latLng)

                        selectedAddress = address.getAddressLine(0)
                        binding.tvSelectedFieldLocation.text = selectedAddress
                    } else {
                        Toast.makeText(
                            this@CreateEditFieldActivity,
                            "Location not found!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    Toast.makeText(
                        this@CreateEditFieldActivity,
                        "Error while trying to find the location! Please try again later",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                Log.e("Geocoder", "Search error: ${e.message}")
            }
        }
    }

    private fun setupUI() {
        binding.tvHeaderTitle.text = if (isEditMode) "Edit Field" else "New Field"
        binding.buttonSave.text = if (isEditMode) "SAVE" else "CREATE"

        if (isEditMode) {
            currentField?.let { populateForm(it) }
        }
    }

    private fun populateForm(field: Field) {
        binding.tietFieldNameField.setText(field.fieldName)
        binding.tietAreaField.setText(field.fieldArea?.toString())
        binding.tietPalmOilTypeField.setText(field.oilPalmType)
        binding.tietAverageAgeField.setText(field.avgOilPalmAgeInMonths.toString())
        binding.tietDescriptionField.setText(field.fieldDesc)
        binding.tvSelectedFieldLocation.setText(field.fieldLocation.address)
        selectedLatLng = LatLng(field.fieldLocation.latitude, field.fieldLocation.longitude)
        selectedAddress = field.fieldLocation.address

        field.fieldPhotoPath?.let { path ->
            finalImagePath = path
            val imageFile = File(path)
            if (imageFile.exists()) {
                Glide.with(this).load(imageFile).into(binding.ivFieldPhoto)
            } else {
                Log.d("CreateEditFieldActivity", "Local file is not found at path: $path")
                Glide.with(this).load(R.drawable.placeholder_200x100).into(binding.ivFieldPhoto)
            }
        } ?: run {
            Glide.with(this).load(R.drawable.placeholder_200x100).into(binding.ivFieldPhoto)
        }
    }

    private fun setupMap() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_fragment_container) as? SupportMapFragment

        mapFragment?.getMapAsync { googleMap ->
            lifecycleScope.launch {
                delay(1200)
                showLoading(false)
            }
        }
    }

    private fun showLoading(show: Boolean) {
        val loadingOverlay = binding.cpiLoadingOverlay
        loadingOverlay.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun setupListeners() {
        binding.ivBack.setOnClickListener { finish() }
        binding.buttonSave.setOnClickListener { validateAndSaveField() }
        binding.mBtnSelectProfile.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isRotateGesturesEnabled = true
        mMap.uiSettings.isZoomGesturesEnabled = true
        mMap.uiSettings.isScrollGesturesEnabled = true
        mMap.uiSettings.isScrollGesturesEnabledDuringRotateOrZoom = true

        mMap.setOnMapClickListener { latLng ->
            placeMarker(latLng)
        }

        mMap.setOnCameraMoveStartedListener { reason ->
            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                binding.root.requestDisallowInterceptTouchEvent(true)
            }
        }

        if (isEditMode && selectedLatLng != null && selectedAddress != null) {
            val initialLatLng = selectedLatLng!!
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLatLng, 12f))
            placeMarker(initialLatLng)
        } else {
            checkLocationPermissionAndCenterMap()
        }
    }

    private fun checkLocationPermissionAndCenterMap() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
            getDeviceLocationAndCenterMap()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 10f))
        }
    }

    private fun getDeviceLocationAndCenterMap() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token
        ).addOnSuccessListener { location: Location? ->
            if (location != null) {
                val currentLatLng = LatLng(location.latitude, location.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                placeMarker(currentLatLng)
            } else {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 10f))
                placeMarker(DEFAULT_LOCATION)
            }
        }.addOnFailureListener {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 10f))
            placeMarker(DEFAULT_LOCATION)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkLocationPermissionAndCenterMap()
            } else {
                Toast.makeText(
                    this,
                    "Location permission denied. Using default map location.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun placeMarker(latLng: LatLng) {
        locationMarker?.remove()

        locationMarker = mMap.addMarker(
            MarkerOptions().position(latLng).title("Field Location")
        )

        selectedLatLng = latLng
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))

        val geocoder = Geocoder(this, Locale.getDefault())
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                launch(Dispatchers.Main) {
                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        val fullAddress = address.getAddressLine(0)

                        selectedAddress = fullAddress
                        binding.tvSelectedFieldLocation.text = selectedAddress

                        binding.searchViewLocation.setQuery(fullAddress, false)
                    }
                }
            } catch (e: Exception) {
                Log.e("Geocoder", "Reverse geocode error")
            }
        }
    }

    private fun saveImageLocally(context: Context, imageUri: Uri): String? {
        val fileName = "field_photo_${UUID.randomUUID()}.jpg"
        val file = File(context.filesDir, fileName)

        return try {
            context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            Log.e("FileSave", "Failed to save image locally", e)
            null
        }
    }

    private fun validateAndSaveField() {
        val fieldName = binding.tietFieldNameField.text.toString().trim()
        val fieldArea = binding.tietAreaField.text.toString().trim()
        val oilPalmType = binding.tietPalmOilTypeField.text.toString().trim()
        val fieldAverageAge = binding.tietAverageAgeField.text.toString().trim()
        val fieldDesc = binding.tietDescriptionField.text.toString().trim()
        val locationAddress = binding.tvSelectedFieldLocation.text.toString().trim()

        var isValid = true

        if (fieldName.isEmpty()) {
            binding.tilFieldNameField.error = "Field name is required!"
            isValid = false
        } else {
            binding.tilFieldNameField.error = null
        }

        if (fieldArea.isEmpty() || fieldArea.isNullOrBlank()) {
            binding.tilAreaField.error = "Field's area is required!"
            isValid = false
        } else if (fieldArea.toDouble() <= 0.0) {
            binding.tilAreaField.error = "Field's area can't possibly be 0 or less!"
            isValid = false
        } else {
            binding.tilAreaField.error = null
        }

        if (fieldAverageAge.isEmpty() || fieldAverageAge.isNullOrBlank()) {
            binding.tilAverageAgeField.error = "Average palm oil's age is required!"
            isValid = false
        } else if (fieldAverageAge.toDouble() <= 0.0) {
            binding.tilAverageAgeField.error = "Average palm oil's age can't possibly be 0 or less!"
            isValid = false
        } else {
            binding.tilAverageAgeField.error = null
        }

        if (selectedLatLng == null || selectedAddress == null || locationAddress.isEmpty()) {
            Toast.makeText(this, "Please select a location in the map!", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        if (!isValid) {
            Toast.makeText(
                this, "Please fill all the required fields correctly!", Toast.LENGTH_LONG
            ).show()
            return
        }

        if (selectedImageUri != null) {
            finalImagePath = saveImageLocally(this, selectedImageUri!!)
            Log.d("CreateEditFieldActivity", "validateAndSaveField: $finalImagePath")
        } else if (isEditMode) {
            finalImagePath = currentField?.fieldPhotoPath
        }

        val fieldLocation = FieldLocation(
            latitude = selectedLatLng!!.latitude,
            longitude = selectedLatLng!!.longitude,
            address = selectedAddress!!
        )

        val fieldUserId = if (isEditMode) {
            currentField?.userId ?: ""
        } else {
            currentUserId
        }

        val fieldToSave = Field(
            fieldId = if (isEditMode) currentField!!.fieldId else "",
            fieldName = fieldName,
            fieldArea = fieldArea.toDoubleOrNull() ?: 0.0,
            fieldLocation = fieldLocation,
            avgOilPalmAgeInMonths = fieldAverageAge.toIntOrNull() ?: 0,
            oilPalmType = oilPalmType,
            fieldDesc = fieldDesc,
            fieldPhotoPath = finalImagePath,
            userId = fieldUserId
        )

        if (isEditMode) {
            fieldViewModel.updateField(fieldToSave)
        } else {
            fieldViewModel.createNewField(fieldToSave)
        }
    }

    private fun observeViewModelEvents() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    fieldViewModel.events.collect { event ->
                        when (event) {
                            is FieldViewModel.Event.ShowMessage -> {
                                Toast.makeText(
                                    this@CreateEditFieldActivity, event.message, Toast.LENGTH_LONG
                                ).show()
                            }

                            is FieldViewModel.Event.FinishActivity -> {
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                        }
                    }
                }
                launch {
                    fieldViewModel.isLoading.collect { isLoading ->
                        updateButtonState(isLoading)
                    }
                }
            }
        }
    }

    private fun updateButtonState(isLoading: Boolean) {
        binding.buttonSave.apply {
            isEnabled = !isLoading
            if (isLoading && isEditMode) {
                text = "SAVING..."
            } else if (isLoading && !isEditMode) {
                text = "CREATING..."
            } else {
                text = if (isEditMode) "SAVE" else "CREATE"
            }
        }
    }
}