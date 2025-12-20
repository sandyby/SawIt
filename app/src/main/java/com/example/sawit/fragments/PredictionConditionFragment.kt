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
import com.example.sawit.databinding.FragmentPredictionConditionBinding
import com.example.sawit.models.PredictionHistory
import com.example.sawit.viewmodels.FieldViewModel
import com.example.sawit.viewmodels.PredictionViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PredictionConditionFragment : Fragment(R.layout.fragment_prediction_condition) {

    private var _binding: FragmentPredictionConditionBinding? = null
    private val binding get() = _binding!!

    private val fieldViewModel: FieldViewModel by viewModels()
    private val predictionViewModel: PredictionViewModel by viewModels()
    private var fieldIdMap: Map<String, String> = emptyMap()

    companion object {
        const val ARG_CONDITION_LABEL = "condition_label"
        const val ARG_PREDICTED_YIELD = "predicted_yield"
        const val ARG_ACTUAL_YIELD = "actual_yield"
        const val ARG_GAP_PERCENTAGE = "gap_percentage"
        const val ARG_IS_FROM_HISTORY = "is_from_history"
        private const val FIELD_PLACEHOLDER = "Choose a field"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPredictionConditionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        observeFields()
        observePredictionEvents()
    }

    private fun observeFields() {
        val targetFieldId = parentFragment?.arguments?.getString("fieldId")

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
                    }
                    if (targetFieldId != null) {
                        val selectedField = fields.find { it.fieldId == targetFieldId }
                        selectedField?.let { field ->
                            binding.actvField.setText(field.fieldName, false)
                        }
                    } else {
                        binding.actvField.setText(FIELD_PLACEHOLDER, false)
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
        val harvestArea = binding.tietHarvestArea.text.toString().toFloatOrNull()
        val rainfall = binding.tietRainfall.text.toString().toFloatOrNull()
        val actualYield = binding.tietActualYield.text.toString().toFloatOrNull()
        val tempMin = binding.tietMinTemperature.text.toString().toFloatOrNull()
        val tempMax = binding.tietMaxTemperature.text.toString().toFloatOrNull()

        var isValid = true

        if (fieldName.isEmpty() || fieldName == FIELD_PLACEHOLDER || !fieldIdMap.containsKey(
                fieldName
            )
        ) {
            binding.tilField.error = "Please select a field!"
            isValid = false
        } else {
            binding.tilField.error = null
        }

        if (harvestArea == null) {
            binding.tilHarvestArea.error = "Harvest area is required!"
            isValid = false
        } else if (harvestArea <= 0) {
            binding.tilHarvestArea.error = "Invalid harvest area!"
            isValid = false
        } else {
            binding.tilHarvestArea.error = null
        }

        if (actualYield == null) {
            binding.tilActualYield.error = "Total yield is required!"
            isValid = false
        } else if (actualYield < 0) {
            binding.tilActualYield.error = "Invalid total yield!"
            isValid = false
        } else {
            binding.tilActualYield.error = null
        }

        if (rainfall == null) {
            binding.tilRainfall.error = "Rainfall is required!"
            isValid = false
        } else {
            binding.tilRainfall.error = null
        }

        if (tempMin == null) {
            binding.tilMinTemperature.error = "Min. temperature is required!"
            isValid = false
        } else {
            binding.tilMinTemperature.error = null
        }

        if (tempMax == null) {
            binding.tilMaxTemperature.error = "Max. temperature is required!"
            isValid = false
        } else {
            binding.tilMaxTemperature.error = null
        }

        if (!isValid) return

        val selectedFieldId = fieldIdMap[fieldName]

        predictionViewModel.predictPlantCondition(
            selectedFieldId!!,
            fieldName,
            tempMin!!,
            tempMax!!,
            rainfall!!,
            harvestArea!!,
            actualYield!!
        )
    }

    private fun observePredictionEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    predictionViewModel.predictionResult.collect { result ->
                        result?.let {
                            val actualYield =
                                binding.tietActualYield.text.toString().toFloatOrNull() ?: 0f

                            val resultFragment = PredictionConditionResultFragment().apply {
                                arguments = Bundle().apply {
                                    putString(ARG_CONDITION_LABEL, it.conditionLabel)
                                    putFloat(ARG_PREDICTED_YIELD, it.predictedYield)
                                    putFloat(ARG_GAP_PERCENTAGE, it.gapPercentage ?: 0f)
                                    putFloat(ARG_ACTUAL_YIELD, actualYield)
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
                            is PredictionViewModel.Event.ShowError -> {
                                Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                            }

                            is PredictionViewModel.Event.ShowMessage -> {
                                Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                            }

                            is PredictionViewModel.Event.PredictionSaved -> {
                                //
                            }
                        }
                    }
                }

                launch {
                    predictionViewModel.isLoading.collect { isLoading ->
                        binding.btnPredict.isEnabled = !isLoading
                        binding.btnPredict.text =
                            if (isLoading) "PREDICTING..." else "PREDICT"
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