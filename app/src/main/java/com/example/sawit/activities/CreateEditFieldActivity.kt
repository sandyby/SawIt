package com.example.sawit.activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.ColorStateList
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
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import com.example.sawit.viewmodels.ActivityViewModel
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
import kotlinx.coroutines.flow.collectLatest
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
    private val activityViewModel: ActivityViewModel by viewModels()

    private var currentField: Field? = null
    private var currentFieldId: String? = null
    private var isEditMode = false
    private var selectedLatLng: LatLng? = null
    private var selectedAddress: String? = null
    private var locationMarker: Marker? = null
    private var selectedImageUri: Uri? = null
    private var finalImagePath: String? = null

    companion object {
        const val EXTRA_FIELD = "EXTRA_FIELD"
        const val EXTRA_FIELD_ID = "EXTRA_FIELD_ID"
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

        val fieldId = intent.getStringExtra("EXTRA_FIELD_ID")
        if (fieldId != null) {
            lifecycleScope.launch {
                fieldViewModel.fieldsData.collect { fields ->
                    val field = fields.find { it.fieldId == fieldId }
                    if (field != null) {
                        currentField = field
                        isEditMode = true
                        populateForm(field)
                    }
                }
            }
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
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    searchLocation(query)
                    binding.searchViewLocation.clearFocus()
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

    private fun bitmapToBase64(bitmap: android.graphics.Bitmap): String {
        val byteArrayOutputStream = java.io.ByteArrayOutputStream()
        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 60, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)
    }

    private fun uriToBase64(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
            bitmapToBase64(bitmap)
        } catch (e: Exception) {
            null
        }
    }

    private fun captureMapSnapshot(onComplete: (String?) -> Unit) {
        mMap.snapshot { bitmap ->
            if (bitmap != null) {
                val path = saveBitmapLocally(bitmap)
                onComplete(path)
            } else {
                onComplete(null)
            }
        }
    }

    private fun saveBitmapLocally(bitmap: android.graphics.Bitmap): String? {
        val fileName = "map_placeholder_${UUID.randomUUID()}.jpg"
        val file = File(filesDir, fileName)
        return try {
            FileOutputStream(file).use { out ->
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
            }
            file.absolutePath
        } catch (e: IOException) {
            Log.e("MapSnapshot", "Failed to save snapshot", e)
            null
        }
    }

    private fun setupUI() {
        binding.tvHeaderTitle.text = if (isEditMode) "Edit Field" else "New Field"
        binding.buttonSave.text = if (isEditMode) "SAVE" else "CREATE"
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

        binding.fabMapType.setOnClickListener {
            if (mMap.mapType == GoogleMap.MAP_TYPE_NORMAL) {
                mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                binding.fabMapType.setImageResource(R.drawable.ic_filled_road_24_black)
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.text_primary_900))
            } else {
                mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                binding.fabMapType.setImageResource(R.drawable.ic_outline_satelite_24_black)
            }
            binding.fabMapType.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.text_primary_900))
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.apply {
            isZoomControlsEnabled = true
            isCompassEnabled = true
            isRotateGesturesEnabled = true
            isZoomGesturesEnabled = true
            isScrollGesturesEnabled = true
            isScrollGesturesEnabledDuringRotateOrZoom = true
        }

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
            || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            ) {
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                moveMapToLocation(location)
            } else {
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    CancellationTokenSource().token
                ).addOnSuccessListener { freshLocation ->
                    freshLocation?.let { moveMapToLocation(it) }
                }
            }
        }.addOnFailureListener {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 10f))
        }
    }

    private fun moveMapToLocation(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        placeMarker(latLng)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkLocationPermissionAndCenterMap()
            } else {
                Toast.makeText(this, "Permission denied. Using default map location.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun placeMarker(latLng: LatLng) {
        locationMarker?.remove()
        locationMarker = mMap.addMarker(MarkerOptions().position(latLng).title("Field Location"))
        selectedLatLng = latLng
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

        val geocoder = Geocoder(this, Locale.getDefault())
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                launch(Dispatchers.Main) {
                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        selectedAddress = address.getAddressLine(0)
                        binding.tvSelectedFieldLocation.text = selectedAddress
                        binding.searchViewLocation.setQuery(selectedAddress, false)
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

        showLoading(true)

        val isLocationChanged = if (isEditMode && currentField != null && selectedLatLng != null) {
            val oldLat = currentField!!.fieldLocation.latitude
            val oldLng = currentField!!.fieldLocation.longitude
            selectedLatLng!!.latitude != oldLat || selectedLatLng!!.longitude != oldLng
        } else {
            false
        }

        val isPreviousImageSnapshot = currentField?.fieldPhotoPath?.contains("map_placeholder") == true
        val hasNoPreviousImage = currentField?.fieldPhotoPath.isNullOrEmpty()

        when {
            selectedImageUri != null -> {
                val path = saveImageLocally(this, selectedImageUri!!)
                proceedToSave(path)
            }
            isEditMode && isLocationChanged && (isPreviousImageSnapshot || hasNoPreviousImage) -> {
                Log.d("CreateEditField", "Location changed, regenerating map snapshot...")
                captureMapSnapshot { mapPath ->
                    proceedToSave(mapPath)
                }
            }

            isEditMode && !hasNoPreviousImage -> {
                proceedToSave(currentField?.fieldPhotoPath)
            }
            else -> {
                captureMapSnapshot { mapPath ->
                    proceedToSave(mapPath)
                }
            }
        }
    }

    private fun proceedToSave(imagePath: String?) {
        val fieldName = binding.tietFieldNameField.text.toString().trim()
        val fieldArea = binding.tietAreaField.text.toString().toDoubleOrNull() ?: 0.0
        val oilPalmType = binding.tietPalmOilTypeField.text.toString().trim()
        val fieldAverageAge = binding.tietAverageAgeField.text.toString().toIntOrNull() ?: 0
        val fieldDesc = binding.tietDescriptionField.text.toString().trim()

        val fieldLocation = FieldLocation(
            latitude = selectedLatLng!!.latitude,
            longitude = selectedLatLng!!.longitude,
            address = selectedAddress!!
        )

        val base64String = if (selectedImageUri != null) {
            uriToBase64(selectedImageUri!!)
        } else if (imagePath != null) {
            try {
                val bitmap = android.graphics.BitmapFactory.decodeFile(imagePath)
                bitmapToBase64(bitmap)
            } catch (e: Exception) { null }
        } else {
            currentField?.fieldPhotoBase64
        }

        val fieldToSave = Field(
            fieldId = if (isEditMode) currentField!!.fieldId else "",
            fieldName = fieldName,
            fieldArea = fieldArea,
            fieldLocation = fieldLocation,
            avgOilPalmAgeInMonths = fieldAverageAge,
            oilPalmType = oilPalmType,
            fieldDesc = fieldDesc,
            fieldPhotoPath = imagePath,
            fieldPhotoBase64 = base64String,
            userId = if (isEditMode) currentField!!.userId else currentUserId
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
                    fieldViewModel.isLoading.collect { isLoading ->
                        updateButtonState(isLoading)
                        showLoading(isLoading)
                    }
                }

                launch {
                    fieldViewModel.events.collect { event ->
                        when (event) {
                            is FieldViewModel.Event.ShowMessage -> {
                                Toast.makeText(
                                    this@CreateEditFieldActivity, event.message, Toast.LENGTH_LONG
                                ).show()
                            }

                            is FieldViewModel.Event.UpdateSuccess -> {
                                val newName = binding.tietFieldNameField.text.toString()
                                val fieldId = currentField?.fieldId ?: ""
                                if(fieldId.isNotEmpty()) {
                                    activityViewModel.updateActivitiesFieldName(fieldId, newName)
                                }
                            }

                            is FieldViewModel.Event.FinishActivity -> {
                                setResult(RESULT_OK)
                                finish()
                            }
                        }
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