package com.example.sawit.fragments

import android.os.Bundle
import android.util.Log
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
import com.example.sawit.models.Field
import com.example.sawit.viewmodels.FieldViewModel
import com.example.sawit.viewmodels.PredictionViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PredictionYieldFragment : Fragment(R.layout.fragment_prediction_yield) {

    private var _binding: FragmentPredictionYieldBinding? = null
    private val binding get() = _binding!!
    private val fieldViewModel: FieldViewModel by viewModels()
    private val predictionViewModel: PredictionViewModel by viewModels()

    //    private var fieldIdMap: Map<String, String> = emptyMap()
    private var fieldsList: List<Field> = emptyList()

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
        setupTextWatchers()
        observeFields()
        observePredictionEvents()
    }

    private fun setupTextWatchers() {
        val fields = listOf(
            binding.tietHarvestArea to binding.tilHarvestArea,
            binding.tietRainfall to binding.tilRainfall,
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
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                fieldViewModel.fieldsData.collectLatest { fields ->
//                    fieldIdMap = fields.associate { it.fieldName to it.fieldId }

                    if (fields.isEmpty()) return@collectLatest

                    fieldsList = fields

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

                        setOnItemClickListener { _, _, position, _ ->
                            val selectedName = adapter.getItem(position) as String
                            val field = fieldsList.find { it.fieldName == selectedName }
                            field?.let {
                                binding.tilHarvestArea.helperText = "Max harvest area: ${it.fieldArea} ha"
                                binding.tilHarvestArea.error = null
                            }
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

        val selectedField = fieldsList.find { it.fieldName == fieldName }
        val maxFieldArea = selectedField?.fieldArea?.toFloat() ?: 5000f

        var isValid = true

        if (fieldName.isEmpty() || fieldName == FIELD_PLACEHOLDER || selectedField == null) {
            binding.tilField.error = "Please select a field!"
            isValid = false
        } else {
            binding.tilField.error = null
        }

        if (area == null) {
            binding.tilHarvestArea.error = "Harvest area is required!"
            isValid = false
        } else if (area <= 0) {
            binding.tilHarvestArea.error = "Harvest area must be greater than 0!"
            isValid = false
        } else if (area > maxFieldArea) {
            binding.tilHarvestArea.error = "Harvest area must not exceed $maxFieldArea ha!"
            isValid = false
        } else {
            binding.tilHarvestArea.error = null
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

        if (tmin == null) {
            binding.tilMinTemperature.error = "Min. temperature is required!"
            isValid = false
        } else if (tmin < 10 || tmin > 40) {
            binding.tilMinTemperature.error = "Min. temperature must be between 10 and 40°C!"
            isValid = false
        } else {
            binding.tilMinTemperature.error = null
        }

        if (tmax == null) {
            binding.tilMaxTemperature.error = "Max. temperature is required!"
            isValid = false
        } else if (tmax < 10 || tmax > 40) {
            binding.tilMaxTemperature.error = "Max. temperature must be between 10 and 40°C!"
            isValid = false
        } else {
            binding.tilMaxTemperature.error = null
        }

        if (binding.tilMinTemperature.error == null && binding.tilMaxTemperature.error == null && tmin!! > tmax!!) {
            binding.tilMinTemperature.error = "Invalid min. temperature!"
            isValid = false
        }

        if (!isValid) return

        Log.d(
            "SAWIT_ML_DEBUG", """
        [SENDING TO VM]
        Field: $fieldName (${selectedField!!.fieldId})
        TMin: $tmin
        TMax: $tmax
        Rainfall: $rainfall
        Area: $area
    """.trimIndent()
        )

        predictionViewModel.predictTotalYield(
            selectedField.fieldId,
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

                // 1. Log Hasil Prediksi dari Model ML
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

                            // === LOG DEBUG UNTUK MEMBUKTIKAN ML BEKERJA ===
                            android.util.Log.d(
                                "SAWIT_ML_DEBUG",
                                "--------------------------------------------"
                            )
                            android.util.Log.d("SAWIT_ML_DEBUG", "HASIL PREDIKSI DITERIMA")
                            android.util.Log.d(
                                "SAWIT_ML_DEBUG",
                                "Model Output (Yield): ${it.predictedYield} kg/Ha"
                            )
                            android.util.Log.d(
                                "SAWIT_ML_DEBUG",
                                "Data Input UI - Tmin: $tmin, Tmax: $tmax, Rain: $rainfall, Area: $area"
                            )
                            android.util.Log.d(
                                "SAWIT_ML_DEBUG",
                                "--------------------------------------------"
                            )

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

                // 2. Log Event (Error/Pesan)
                launch {
                    predictionViewModel.events.collect { event ->
                        when (event) {
                            is PredictionViewModel.Event.ShowError -> {
                                android.util.Log.e(
                                    "SAWIT_ML_DEBUG",
                                    "EVENT ERROR: ${event.message}"
                                )
                                Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                            }

                            is PredictionViewModel.Event.PredictionSaved -> {
                                android.util.Log.i(
                                    "SAWIT_ML_DEBUG",
                                    "EVENT: Prediction successfully saved to database"
                                )
                            }

                            is PredictionViewModel.Event.ShowMessage -> {
                                android.util.Log.i(
                                    "SAWIT_ML_DEBUG",
                                    "EVENT MESSAGE: ${event.message}"
                                )
                                Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }

                // 3. Log Status Loading
                launch {
                    predictionViewModel.isLoading.collect { isLoading ->
                        android.util.Log.d("SAWIT_ML_DEBUG", "STATUS: Is Loading = $isLoading")
                        binding.btnPredict.isEnabled = !isLoading
                        binding.btnPredict.text = if (isLoading) "PREDICTING..." else "PREDICT"
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