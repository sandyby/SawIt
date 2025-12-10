package com.example.sawit.activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
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
import com.example.sawit.R
import com.example.sawit.databinding.ActivityCreateFieldsBinding
import com.example.sawit.models.Field
import com.example.sawit.models.FieldLocation
import com.example.sawit.viewmodels.FieldViewModel
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
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Locale
import java.util.UUID

class CreateFieldActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityCreateFieldsBinding
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

    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCreateFieldsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, true)

        imagePickerLauncher = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                // Display the image in the ImageView
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
        observeViewModelEvents()

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map_fragment_container) as SupportMapFragment
        mapFragment.getMapAsync(this)
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
            }
        }
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

        mMap.setOnMapClickListener { latLng ->
            placeMarker(latLng)
        }

        if (isEditMode && selectedLatLng != null && selectedAddress != null) {
            val initialLatLng = selectedLatLng!!
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLatLng, 12f))
            placeMarker(initialLatLng)
        } else {
            checkLocationPermissionAndCenterMap()
        }

//        if (selectedLatLng != null) {
//            placeMarker(selectedLatLng!!)
//        } else {
//            placeMarker(DEFAULT_LOCATION)
//        }
    }

    private fun checkLocationPermissionAndCenterMap() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
            getDeviceLocationAndCenterMap()
        } else {
            // Permission is NOT granted, request it from the user
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            // Fallback: If permission is denied, use the DEFAULT_LOCATION
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 10f))
//            placeMarker(DEFAULT_LOCATION)
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
                // Center camera on GPS location and place initial marker
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                placeMarker(currentLatLng)
            } else {
                // If current location is null (e.g., GPS off), use the default location
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 10f))
                placeMarker(DEFAULT_LOCATION)
            }
        }.addOnFailureListener {
            // If fetching location fails, use the default location
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
                // Permission granted, re-run the check (which will now succeed)
                checkLocationPermissionAndCenterMap()
            } else {
                // Permission denied, inform the user
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
        try {
            @Suppress("DEPRECATION") val addresses: List<Address>? =
                geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address: Address = addresses[0]
                val addressBuilder = StringBuilder()
                for (i in 0..address.maxAddressLineIndex) {
                    addressBuilder.append(address.getAddressLine(i)).append("\n")
                }

                selectedAddress = addressBuilder.toString().trim()
                binding.tvSelectedFieldLocation.text = selectedAddress
//                    String.format(
//                    Locale("id", "ID"),
//                    "Lat: %.4f, Lng: %.4f",
//                    latLng.latitude,
//                    latLng.longitude
//                )
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun saveImageLocally(context: Context, imageUri: Uri): String? {
        val fileName = "field_photo_${UUID.randomUUID()}.jpg" // Use UUID for unique file name
        val file = File(context.filesDir, fileName)

        return try {
            context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            file.absolutePath // Return the permanent local file path
        } catch (e: Exception) {
            Log.e("FileSave", "Failed to save image locally", e)
            null
        }
    }

    private fun validateAndSaveField() {
        val fieldName = binding.tietFieldNameField.text.toString().trim()
        val areaStr = binding.tietAreaField.text.toString().trim()
        val oilPalmType = binding.tietPalmOilTypeField.text.toString().trim()
        val ageStr = binding.tietAverageAgeField.text.toString().trim()
        val fieldDesc = binding.tietDescriptionField.text.toString().trim()
        val locationAddress = binding.tvSelectedFieldLocation.text.toString().trim()

        var isValid = true

        if (fieldName.isEmpty()) {
            binding.tilFieldNameField.error = "Field Name is required!"
            isValid = false
        } else {
            binding.tilFieldNameField.error = null
        }

        if (selectedLatLng == null || selectedAddress == null) {
            Toast.makeText(this, "Please select a location in the map!", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        val fieldArea = areaStr.toDoubleOrNull()
        val avgOilPalmAge = ageStr.toIntOrNull()

        if (!isValid || fieldArea == null || avgOilPalmAge == null) {
            Toast.makeText(
                this, "Please fill all the required fields correctly!", Toast.LENGTH_LONG
            ).show()
            return
        }

        if (selectedImageUri != null) {
            finalImagePath = saveImageLocally(this, selectedImageUri!!)
            if (finalImagePath == null) {
                Toast.makeText(this, "Failed saving the image file!", Toast.LENGTH_SHORT).show()
                return
            }
        }

        val fieldLocation = FieldLocation(
            latitude = selectedLatLng!!.latitude,
            longitude = selectedLatLng!!.longitude,
            address = selectedAddress!!
        )

        val finalFieldId = if (isEditMode) {
            currentField!!.fieldId
        } else {
            ""
        }

        val fieldToSave = Field(
            fieldId = finalFieldId,
            fieldName = fieldName,
            fieldArea = fieldArea,
            fieldLocation = fieldLocation,
//            latitude = selectedLatLng!!.latitude,
//            longitude = selectedLatLng!!.longitude,
//            fieldLocationName = locationName,
            avgOilPalmAgeInMonths = avgOilPalmAge,
            oilPalmType = oilPalmType,
            fieldDesc = fieldDesc,
            fieldPhotoPath = finalImagePath
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
                                    this@CreateFieldActivity, event.message, Toast.LENGTH_LONG
                                ).show()
                            }

                            is FieldViewModel.Event.FinishActivity -> {
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