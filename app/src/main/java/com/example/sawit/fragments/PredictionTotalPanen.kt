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
import androidx.lifecycle.lifecycleScope
import com.example.sawit.R
import com.example.sawit.databinding.FragmentPredictionTotalPanenBinding
import com.example.sawit.viewmodels.FieldViewModel
import com.example.sawit.viewmodels.PredictionViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PredictionTotalPanen : Fragment(R.layout.fragment_prediction_total_panen) {

    private var _binding: FragmentPredictionTotalPanenBinding? = null
    private val binding get() = _binding!!

    private val fieldViewModel: FieldViewModel by viewModels() // Asumsi ini ada
    private val predictionViewModel: PredictionViewModel by viewModels() // <-- Inisialisasi PredictionViewModel

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

        viewLifecycleOwner.lifecycleScope.launch {
            fieldViewModel.fieldsData.collectLatest { fields ->
                val fieldNames = fields.map { it.fieldName }
                val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, fieldNames)
                binding.inputFieldName.setAdapter(adapter)
            }
        }

        binding.btnPredict.setOnClickListener {
            val selectedField = binding.inputFieldName.text.toString().trim()
            val areaStr = binding.inputFieldArea.text.toString().trim()
            val rainfallStr = binding.inputRainfall.text.toString().trim()
            val tminStr = binding.inputTmin.text.toString().trim()
            val tmaxStr = binding.inputTmax.text.toString().trim()

            if (selectedField.isEmpty() || areaStr.isEmpty() || rainfallStr.isEmpty() ||
                tminStr.isEmpty() || tmaxStr.isEmpty()
            ) {
                Toast.makeText(requireContext(), "Harap isi semua data!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val area = areaStr.toFloatOrNull()
            val rainfall = rainfallStr.toFloatOrNull()
            val tmin = tminStr.toFloatOrNull()
            val tmax = tmaxStr.toFloatOrNull()

            if (area == null || rainfall == null || tmin == null || tmax == null) {
                Toast.makeText(requireContext(), "Input harus berupa angka yang valid!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            predictionViewModel.predictAndSaveTotalPanen(
                fieldName = selectedField,
                tmin = tmin,
                tmax = tmax,
                rainfall = rainfall,
                area = area,
                onSuccess = { predictedYield ->
                    val resultFragment = ResultTotalPanen().apply {
                        arguments = Bundle().apply {
                            putFloat(ARG_PREDICTED_YIELD, predictedYield)
                            putFloat(ARG_TMIN, tmin)
                            putFloat(ARG_TMAX, tmax)
                            putFloat(ARG_RAINFALL, rainfall)
                            putFloat(ARG_AREA, area)
                        }
                    }

                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fl_scroll_view_content, resultFragment)
                        .addToBackStack(null)
                        .commit()
                },
                onError = { message ->
                    Log.e("PredictionsFragment", "Error: $message")
                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}