package com.example.sawit.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.sawit.R
import com.example.sawit.databinding.FragmentPredictionYieldBinding
import com.example.sawit.viewmodels.FieldViewModel
import com.example.sawit.viewmodels.PredictionViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PredictionYieldFragment : Fragment(R.layout.fragment_prediction_yield) {

    private var _binding: FragmentPredictionYieldBinding? = null
    private val binding get() = _binding!!
    private val fieldViewModel: FieldViewModel by viewModels()
    private val predictionViewModel: PredictionViewModel by viewModels()

    private var fieldIdMap: Map<String, String> = emptyMap()

    companion object {
        const val ARG_PREDICTED_YIELD = "predicted_yield"
        const val ARG_TMIN = "tmin"
        const val ARG_TMAX = "tmax"
        const val ARG_RAINFALL = "rainfall"
        const val ARG_AREA = "area"
        const val ARG_IS_FROM_HISTORY = "is_from_history"
        private const val FIELD_PLACEHOLDER = "Choose a field"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPredictionYieldBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPredictionYieldBinding.bind(view)

        setupListeners()
        observeFields()
        observePredictionEvents()
    }

    private fun observeFields() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                fieldViewModel.fieldsData.collectLatest { fields ->
                    fieldIdMap = fields.associate { it.fieldName to it.fieldId }
                    val fieldNames = mutableListOf(FIELD_PLACEHOLDER)
                    fieldNames.addAll(fields.map { it.fieldName })

                    val adapter = ArrayAdapter(
                        requireContext(),
                        R.layout.autocompleteview_dropdown_item,
                        fieldNames
                    )
                    binding.actvField.apply {
                        setDropDownBackgroundDrawable(
                            ResourcesCompat.getDrawable(
                                resources,
                                R.drawable.autocomplete_background,
                                null
                            )
                        )
                        setAdapter(adapter)

                        if (text.toString().isEmpty()) {
                            setText(FIELD_PLACEHOLDER, false)
                        }
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        binding.btnPredict.setOnClickListener {
            validateAndPredict()
        }
    }

    private fun validateAndPredict() {
        val fieldName = binding.actvField.text.toString().trim()
        val area = binding.tietHarvestArea.text.toString().toFloatOrNull()
        val rainfall = binding.tietRainfall.text.toString().toFloatOrNull()
        val tmin = binding.tietMinTemperature.text.toString().toFloatOrNull()
        val tmax = binding.tietMaxTemperature.text.toString().toFloatOrNull()

        var isValid = true

        if (fieldName.isEmpty() || !fieldIdMap.containsKey(fieldName)) {
            binding.tilField.error = "Please select a field!"
            isValid = false
        } else {
            binding.tilField.error = null
        }

        if (area == null) {
            binding.tilHarvestArea.error = "Harvest area is required!"
            isValid = false
        } else if (area < 0) {
            binding.tilHarvestArea.error = "Invalid harvest area!"
            isValid = false
        } else {
            binding.tilHarvestArea.error = null
        }

        if (rainfall == null) {
            binding.tilRainfall.error = "Rainfall is required!"
            isValid = false
        } else {
            binding.tilRainfall.error = null
        }

        if (tmin == null) {
            binding.tilMinTemperature.error = "Min. temperature is required!"
            isValid = false
        } else {
            binding.tilMinTemperature.error = null
        }

        if (tmax == null) {
            binding.tilMaxTemperature.error = "Max. temperature is required!"
            isValid = false
        } else {
            binding.tilMaxTemperature.error = null
        }

        if (tmin != null && tmax != null && tmin > tmax) {
            binding.tilMinTemperature.error = "Invalid min. temperature!"
            binding.tilMaxTemperature.error = null
            isValid = false
        } else if (tmin != null && tmax != null && tmax < tmin) {
            binding.tilMaxTemperature.error = "Invalid max. temperature!"
            binding.tilMinTemperature.error = null
            isValid = false
        } else {
            binding.tilMinTemperature.error = null
            binding.tilMaxTemperature.error = null
        }

        if (!isValid) return

        val selectedFieldId = fieldIdMap[fieldName]

        predictionViewModel.predictTotalYield(
            selectedFieldId!!,
            fieldName,
            tmin!!,
            tmax!!,
            rainfall!!,
            area!!
        )
    }

    private fun observePredictionEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    predictionViewModel.predictionResult.collect { result ->
                        result?.let {
                            val area = binding.tietHarvestArea.text.toString().toFloatOrNull() ?: 0f
                            val rainfall =
                                binding.tietRainfall.text.toString().toFloatOrNull() ?: 0f
                            val tmin =
                                binding.tietMinTemperature.text.toString().toFloatOrNull() ?: 0f
                            val tmax =
                                binding.tietMaxTemperature.text.toString().toFloatOrNull() ?: 0f

                            val resultFragment = PredictionYieldResultFragment().apply {
                                arguments = Bundle().apply {
                                    putFloat(ARG_PREDICTED_YIELD, it.predictedYield)
                                    putFloat(ARG_TMIN, tmin)
                                    putFloat(ARG_TMAX, tmax)
                                    putFloat(ARG_RAINFALL, rainfall)
                                    putFloat(ARG_AREA, area)
                                    putBoolean(ARG_IS_FROM_HISTORY, false)
                                }
                            }
                            navigateToResult(resultFragment)
                            predictionViewModel.clearPredictionResult()
                        }
                    }
                }

                launch {
                    predictionViewModel.events.collect { event ->
                        when (event) {
                            is PredictionViewModel.Event.ShowError ->
                                Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()

                            is PredictionViewModel.Event.PredictionSaved -> {
                                //
                            }

                            is PredictionViewModel.Event.ShowMessage -> {
                                Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }

                launch {
                    predictionViewModel.isLoading.collect { isLoading ->
                        binding.btnPredict.isEnabled = !isLoading
                        binding.btnPredict.text = if (isLoading) "PREDICTING..." else "PREDICT"
                    }
                }
            }
        }
    }

    private fun navigateToResult(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_in_left,
                R.anim.slide_in_left,
                R.anim.slide_in_right
            )
            .replace(R.id.fl_scroll_view_content, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}