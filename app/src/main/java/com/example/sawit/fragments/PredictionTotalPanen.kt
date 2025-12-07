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
import com.example.sawit.ml.PredictionUtils // Import fungsi prediksi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PredictionTotalPanen : Fragment(R.layout.fragment_prediction_total_panen) {

    private var _binding: FragmentPredictionTotalPanenBinding? = null
    private val binding get() = _binding!!

    // Menggunakan viewModels untuk FieldViewModel
    private val fieldViewModel: FieldViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPredictionTotalPanenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ==========================
        // LOGIKA DROPDOWN (FIELD NAMES)
        // ==========================
        viewLifecycleOwner.lifecycleScope.launch {
            fieldViewModel.fieldsData.collectLatest { fields ->
                val fieldNames = fields.map { it.fieldName }
                val adapter = ArrayAdapter(
                    requireContext(),
                    R.layout.dropdown_item,
                    fieldNames
                )
                binding.inputFieldName.setAdapter(adapter)
            }
        }

        binding.btnPredict.setOnClickListener {
            Log.d("PredictionsFragment", "Predict clicked")

            val selectedField = binding.inputFieldName.text.toString()
            val fieldAreaStr = binding.inputFieldArea.text.toString()
            val rainfallStr = binding.inputRainfall.text.toString()
            val tminStr = binding.inputTmin.text.toString()
            val tmaxStr = binding.inputTmax.text.toString()

            // Validasi Input Kosong
            if (selectedField.isEmpty() || fieldAreaStr.isEmpty() ||
                rainfallStr.isEmpty() || tminStr.isEmpty() || tmaxStr.isEmpty()
            ) {
                Toast.makeText(requireContext(), "Harap isi semua data!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Konversi ke Float dan Validasi Angka
            val area = fieldAreaStr.toFloatOrNull()
            val rainfall = rainfallStr.toFloatOrNull()
            val tmin = tminStr.toFloatOrNull()
            val tmax = tmaxStr.toFloatOrNull()

            if (area == null || rainfall == null || tmin == null || tmax == null) {
                Toast.makeText(requireContext(), "Input harus berupa angka yang valid!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                // Lakukan Prediksi Yield menggunakan ONNX
                val predictedYield = PredictionUtils.predictYield(
                    requireContext(),
                    tmin,
                    tmax,
                    rainfall,
                    area
                )

                // Tampilkan Hasil Prediksi dalam Toast
                Toast.makeText(
                    requireContext(),
                    "Prediksi Hasil Panen: ${"%.2f".format(predictedYield)}",
                    Toast.LENGTH_LONG
                ).show()

            } catch (e: Exception) {
                Log.e("PredictionsFragment", "Error during ONNX inference", e)
                Toast.makeText(requireContext(), "Gagal melakukan prediksi: ${e.message}", Toast.LENGTH_LONG).show()
            }

            // Logika navigasi ke ResultFragment (dihapus/dikomentari untuk fokus pada Toast)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}