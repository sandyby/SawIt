package com.example.sawit.fragments;

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.sawit.R
import com.example.sawit.databinding.FragmentFieldDetailBinding
import com.example.sawit.models.Field
import com.example.sawit.viewmodels.FieldViewModel
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FieldDetailFragment : Fragment(R.layout.fragment_field_detail) {
    private var _binding: FragmentFieldDetailBinding? = null
    private val binding get() = _binding!!
    private val fieldViewModel: FieldViewModel by viewModels()
    private var fieldId: String? = null
    private var fieldName: String? = null

    companion object {
        fun newInstance(fieldId: String, fieldName: String) = FieldDetailFragment().apply {
            arguments = Bundle().apply {
                putString("fieldId", fieldId)
                putString("fieldName", fieldName)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFieldDetailBinding.bind(view)
        fieldId = arguments?.getString("fieldId")
        fieldName = arguments?.getString("fieldName")

        observeData()
    }

    private fun observeData(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                fieldViewModel.fieldsData.collectLatest { fields ->
                    val field = fields.find { it.fieldId == fieldId }

                    if (field != null) {
                        populateUI(field)
                        binding.pbLoading.visibility = View.GONE
                        binding.nsvContent.visibility = View.VISIBLE
                    } else if (fields.isNotEmpty()) {
                        binding.pbLoading.visibility = View.GONE
                        binding.tvDetailDesc.text = "Field data unavailable!"
                    }
                }
            }
        }
    }

    private fun populateUI(field: Field){
        binding.tvDetailPalmType.text = field.oilPalmType.takeIf { !it.isNullOrBlank() } ?: "Unknown"
        binding.tvDetailArea.text = field.fieldArea?.let { "$it ha" } ?: "Unknown"
        binding.tvDetailAge.text = field.avgOilPalmAgeInMonths?.let { "$it months" } ?: "Unknown"
        binding.tvDetailAddress.text = field.fieldLocation?.address.takeIf { !it.isNullOrBlank() } ?: "No address provided"

        val description = field.fieldDesc
        if (description.isNullOrBlank()) {
            binding.tvDetailDesc.text = "No description provided"
            binding.tvDetailDesc.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_400))
        } else {
            binding.tvDetailDesc.text = description
            binding.tvDetailDesc.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_600))
        }

        setupMap(field)
    }

    private fun setupMap(field: Field) {
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map_detail_container) as? SupportMapFragment
        mapFragment?.getMapAsync { googleMap ->
            val location = LatLng(
                field.fieldLocation.latitude,
                field.fieldLocation.longitude
            )
            googleMap.clear()
            googleMap.addMarker(
                com.google.android.gms.maps.model.MarkerOptions().position(location)
            )
            googleMap.moveCamera(
                com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(
                    location,
                    15f
                )
            )
            googleMap.uiSettings.isScrollGesturesEnabled = false
            googleMap.uiSettings.isZoomControlsEnabled = false
            googleMap.uiSettings.isRotateGesturesEnabled = false
            googleMap.uiSettings.isScrollGesturesEnabledDuringRotateOrZoom = false
            googleMap.uiSettings.isTiltGesturesEnabled = false
            googleMap.uiSettings.isZoomGesturesEnabled = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
