package com.example.sawit.fragments

import android.os.Bundle
import android.util.Log // Ditambahkan
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
    private var hasAutoSelected = false

    companion object {
        private const val TAG = "SAWIT_ML_DEBUG"
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

        setupTextWatchers()
        setupListeners()
        observeFields()
        observePredictionEvents()
    }

    private fun setupTextWatchers() {
        val fields = listOf(
            binding.tietHarvestArea to binding.tilHarvestArea,
            binding.tietRainfall to binding.tilRainfall,
            binding.tietActualYield to binding.tilActualYield,
            binding.tietMinTemperature to binding.tilMinTemperature,
            binding.tietMaxTemperature to binding.tilMaxTemperature
        )

        fields.forEach { (editText, layout) ->
            editText.addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (layout.error != null) {
                        layout.error = null
                    }
                }

                override fun afterTextChanged(s: android.text.Editable?) {}
            })
        }
    }

    private fun observeFields() {
        val targetFieldId = parentFragment?.arguments?.getString("fieldId")
        val targetFieldName = parentFragment?.arguments?.getString("fieldName")

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                fieldViewModel.fieldsData.collectLatest { fields ->
                    if (fields.isEmpty()) return@collectLatest

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
                    if (targetFieldId != null && !hasAutoSelected) {
                        val selectedField = fields.find { it.fieldId == targetFieldId }
                        selectedField?.let {
                            binding.actvField.setText(it.fieldName, false)
                            hasAutoSelected = true
                        }
                    } else if (!hasAutoSelected) {
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
        } else if (harvestArea <= 0 || harvestArea > 5000) {
            binding.tilHarvestArea.error =
                "Harvest area must be greater than 0 and at most 5000 ha!"
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
        } else if (rainfall < 0 || rainfall > 1000) {
            binding.tilRainfall.error = "Rainfall must be between 0 and 1000 mm!"
            isValid = false
        } else {
            binding.tilRainfall.error = null
        }

        if (tempMin == null) {
            binding.tilMinTemperature.error = "Min. temperature is required!"
            isValid = false
        } else if (tempMin < 10 || tempMin > 40) {
            binding.tilMinTemperature.error = "Min. temperature must be between 10 and 40°C!"
            isValid = false
        } else {
            binding.tilMinTemperature.error = null
        }

        if (tempMax == null) {
            binding.tilMaxTemperature.error = "Max. temperature is required!"
            isValid = false
        } else if (tempMax < 10 || tempMax > 40) {
            binding.tilMaxTemperature.error = "Max. temperature must be between 10 and 40°C!"
            isValid = false
        } else {
            binding.tilMaxTemperature.error = null
        }

        if (!isValid) {
            return
        }
        if (binding.tilMinTemperature.error == null && binding.tilMaxTemperature.error == null && tempMin!! > tempMax!!) {
            binding.tilMinTemperature.error = "Invalid min. temperature!"
            isValid = false
        }


        if (!isValid) return

        val selectedFieldId = fieldIdMap[fieldName]

        Log.d("SAWIT_ML_DEBUG", "--- START PREDICTION ---")
        Log.d("SAWIT_ML_DEBUG", "Input Data: Field=$fieldName, Area=$harvestArea, Rain=$rainfall, Actual=$actualYield, Tmin=$tempMin, Tmax=$tempMax")

        predictionViewModel.predictPlantCondition(
            fieldId = selectedFieldId!!,
            fieldName = fieldName,
            tmin = tempMin ?: 0f,
            tmax = tempMax ?: 0f,
            rainfall = rainfall ?: 0f,
            area = harvestArea ?: 0f,
            actualYield = actualYield ?: 0f
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

                            // --- LOG HASIL ---
                            Log.i(TAG, "Prediction SUCCESS")
                            Log.i(TAG, "Result: Label=${it.conditionLabel}, Predicted=${it.predictedYield}, Gap=${it.gapPercentage}%")

                            val resultFragment = PredictionConditionResultFragment().apply {
                                arguments = Bundle().apply {
                                    putString("condition_label", it.conditionLabel)
                                    putFloat("predicted_yield", it.predictedYield)
                                    putFloat("actual_yield", actualYield)
                                    putFloat("gap_percentage", it.gapPercentage ?: 0f)
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
                                Log.d(TAG, "Message Event: ${event.message}")
                                Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                            }

                            is PredictionViewModel.Event.PredictionSaved -> {
                                Log.d(TAG, "Prediction saved to database.")
                            }
                        }
                    }
                }

                launch {
                    predictionViewModel.isLoading.collect { isLoading ->
                        Log.v(TAG, "Loading state: $isLoading") // Log Verbose
                        binding.btnPredict.isEnabled = !isLoading
                        binding.btnPredict.text =
                            if (isLoading) "PREDICTING..." else "PREDICT"
                    }
                }
            }
        }
    }

    private fun navigateToResult(fragment: Fragment) {
        parentFragment?.parentFragmentManager?.beginTransaction()?.apply {
            setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_in_left,
                R.anim.slide_in_left,
                R.anim.slide_in_right
            )
            replace(R.id.fl_scroll_view_content, fragment)
            addToBackStack(null)
            commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}