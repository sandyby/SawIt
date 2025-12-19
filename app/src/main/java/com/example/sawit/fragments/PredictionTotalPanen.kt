package com.example.sawit.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.sawit.R
import com.example.sawit.databinding.FragmentPredictionTotalPanenBinding
import com.example.sawit.viewmodels.FieldViewModel
import com.example.sawit.viewmodels.PredictionViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PredictionTotalPanen : Fragment(R.layout.fragment_prediction_total_panen) {

    private var _binding: FragmentPredictionTotalPanenBinding? = null
    private val binding get() = _binding!!

    private val fieldViewModel: FieldViewModel by viewModels()
    private val predictionViewModel: PredictionViewModel by viewModels()

    companion object {
        const val ARG_PREDICTED_YIELD = "predicted_yield"
        const val ARG_TMIN = "tmin"
        const val ARG_TMAX = "tmax"
        const val ARG_RAINFALL = "rainfall"
        const val ARG_AREA = "area"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPredictionTotalPanenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPredictionTotalPanenBinding.bind(view)

        setupDropdown()
        setupListeners()
        observeViewModel()
    }

    private fun setupDropdown() {
        viewLifecycleOwner.lifecycleScope.launch {
            fieldViewModel.fieldsData.collectLatest { fields ->
                val fieldNames = fields.map { it.fieldName }
                val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, fieldNames)
                binding.inputFieldName.setAdapter(adapter)
            }
        }
    }

    private fun setupListeners() {
        binding.btnPredict.setOnClickListener {
            val selectedField = binding.inputFieldName.text.toString().trim()
            val area = binding.inputFieldArea.text.toString().toFloatOrNull()
            val rainfall = binding.inputRainfall.text.toString().toFloatOrNull()
            val tmin = binding.inputTmin.text.toString().toFloatOrNull()
            val tmax = binding.inputTmax.text.toString().toFloatOrNull()

            if (selectedField.isEmpty() || area == null || rainfall == null || tmin == null || tmax == null) {
                Toast.makeText(requireContext(), "Please fill in the required fields!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            predictionViewModel.predictTotalYield(selectedField, tmin, tmax, rainfall, area)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    predictionViewModel.predictionResult.collect { result ->
                        result?.let {
                            val resultFragment = ResultTotalPanen().apply {
                                arguments = Bundle().apply {
                                    putFloat("predicted_yield", it.predictedYield)
                                }
                            }
                            navigateToResult(resultFragment)
                        }
                    }
                }

                launch {
                    predictionViewModel.events.collect { event ->
                        when (event) {
                            is PredictionViewModel.Event.ShowError ->
                                Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                            is PredictionViewModel.Event.PredictionSaved ->
                                Toast.makeText(context, "Saved to History!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                launch {
                    predictionViewModel.isLoading.collect { isLoading ->
                        binding.btnPredict.isEnabled = !isLoading
                        binding.btnPredict.text = if (isLoading) "Processing..." else "Predict"
                    }
                }
            }
        }
    }

    private fun navigateToResult(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fl_scroll_view_content, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        viewLifecycleOwner.lifecycleScope.launch {
//            fieldViewModel.fieldsData.collectLatest { fields ->
//                val fieldNames = fields.map { it.fieldName }
//                val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, fieldNames)
//                binding.inputFieldName.setAdapter(adapter)
//            }
//        }
//
//        binding.btnPredict.setOnClickListener {
//            val selectedField = binding.inputFieldName.text.toString().trim()
//            val areaStr = binding.inputFieldArea.text.toString().trim()
//            val rainfallStr = binding.inputRainfall.text.toString().trim()
//            val tminStr = binding.inputTmin.text.toString().trim()
//            val tmaxStr = binding.inputTmax.text.toString().trim()
//
//            if (selectedField.isEmpty() || areaStr.isEmpty() || rainfallStr.isEmpty() ||
//                tminStr.isEmpty() || tmaxStr.isEmpty()
//            ) {
//                Toast.makeText(requireContext(), "Harap isi semua data!", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            val area = areaStr.toFloatOrNull()
//            val rainfall = rainfallStr.toFloatOrNull()
//            val tmin = tminStr.toFloatOrNull()
//            val tmax = tmaxStr.toFloatOrNull()
//
//            if (area == null || rainfall == null || tmin == null || tmax == null) {
//                Toast.makeText(requireContext(), "Input harus berupa angka yang valid!", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            predictionViewModel.predictAndSaveTotalPanen(
//                fieldName = selectedField,
//                tmin = tmin,
//                tmax = tmax,
//                rainfall = rainfall,
//                area = area,
//                onSuccess = { predictedYield ->
//                    val resultFragment = ResultTotalPanen().apply {
//                        arguments = Bundle().apply {
//                            putFloat(ARG_PREDICTED_YIELD, predictedYield)
//                            putFloat(ARG_TMIN, tmin)
//                            putFloat(ARG_TMAX, tmax)
//                            putFloat(ARG_RAINFALL, rainfall)
//                            putFloat(ARG_AREA, area)
//                        }
//                    }
//
//                    parentFragmentManager.beginTransaction()
//                        .replace(R.id.fl_scroll_view_content, resultFragment)
//                        .addToBackStack(null)
//                        .commit()
//                },
//                onError = { message ->
//                    Log.e("PredictionsFragment", "Error: $message")
//                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
//                }
//            )
//        }
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
}