// File: com/example/sawit/fragments/PredictionKondisiTanaman.kt
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
import com.example.sawit.R
import com.example.sawit.databinding.FragmentPredictionKondisiTanamanBinding
import com.example.sawit.viewmodels.PredictionViewModel // <-- Import ViewModel Prediksi

class PredictionKondisiTanaman : Fragment(R.layout.fragment_prediction_kondisi_tanaman) {

    private var _binding: FragmentPredictionKondisiTanamanBinding? = null
    private val binding get() = _binding!!

    // Inisialisasi PredictionViewModel
    private val predictionViewModel: PredictionViewModel by viewModels()

    // Kunci argumen untuk navigasi
    companion object {
        const val ARG_CONDITION_LABEL = "condition_label"
        const val ARG_PREDICTED_YIELD = "predicted_yield"
        const val ARG_ACTUAL_YIELD = "actual_yield"
        const val ARG_GAP_PERCENTAGE = "gap_percentage"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPredictionKondisiTanamanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ==========================
        // LOGIKA DROPDOWN (LAHAN LIST)
        // ==========================
        val lahanList = arrayOf("Lahan 1", "Lahan Manjur Sukses") // Data Dropdown
        val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, lahanList)
        binding.inputLahan.setAdapter(adapter)

        // ==========================
        // BUTTON PREDICT
        // ==========================
        binding.btnPredict.setOnClickListener {

            val lahan = binding.inputLahan.text.toString().trim()
            val luasStr = binding.etLuasField.text.toString().trim()
            val curahHujanStr = binding.etCurahHujan.text.toString().trim()
            val hasilPanenActualStr = binding.etHasilPanen.text.toString().trim()
            val tminStr = binding.etTmin.text.toString().trim()
            val tmaxStr = binding.etTmax.text.toString().trim()

            // 1. Validasi Input Kosong
            if (lahan.isEmpty() || luasStr.isEmpty() || curahHujanStr.isEmpty() ||
                hasilPanenActualStr.isEmpty() || tminStr.isEmpty() || tmaxStr.isEmpty()
            ) {
                Toast.makeText(requireContext(), "Semua field harus diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 2. Konversi ke Float dan Validasi Angka
            val luas = luasStr.toFloatOrNull()
            val curahHujan = curahHujanStr.toFloatOrNull()
            val hasilPanenActual = hasilPanenActualStr.toFloatOrNull()
            val tmin = tminStr.toFloatOrNull()
            val tmax = tmaxStr.toFloatOrNull()

            if (luas == null || curahHujan == null || hasilPanenActual == null ||
                tmin == null || tmax == null
            ) {
                Toast.makeText(requireContext(), "Input harus berupa angka yang valid!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Panggil ViewModel untuk Prediksi dan Simpan Data
            predictionViewModel.predictAndSaveKondisiTanaman(
                fieldName = lahan,
                tmin = tmin,
                tmax = tmax,
                rainfall = curahHujan,
                area = luas,
                actualYield = hasilPanenActual,
                onSuccess = { conditionLabel, predictedYield, gapPercentage ->
                    // 3. Navigasi ke ResultKondisiTanaman Fragment
                    val resultFragment = ResultKondisiTanaman().apply {
                        arguments = Bundle().apply {
                            putString(ARG_CONDITION_LABEL, conditionLabel)
                            putFloat(ARG_PREDICTED_YIELD, predictedYield)
                            putFloat(ARG_ACTUAL_YIELD, hasilPanenActual)
                            putFloat(ARG_GAP_PERCENTAGE, gapPercentage)
                        }
                    }

                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fl_scroll_view_content, resultFragment)
                        .addToBackStack(null)
                        .commit()
                },
                onError = { message ->
                    Log.e("KondisiTanaman", "Error: $message")
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