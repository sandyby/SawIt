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
import com.example.sawit.databinding.FragmentPredictionKondisiTanamanBinding
import com.example.sawit.viewmodels.FieldViewModel
import com.example.sawit.viewmodels.PredictionViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PredictionKondisiTanaman : Fragment(R.layout.fragment_prediction_kondisi_tanaman) {

    private var _binding: FragmentPredictionKondisiTanamanBinding? = null
    private val binding get() = _binding!!

    private val fieldViewModel: FieldViewModel by viewModels()
    private val predictionViewModel: PredictionViewModel by viewModels()

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
        _binding = FragmentPredictionKondisiTanamanBinding.bind(view)

        setupDropdown()
        setupListeners()
        observeViewModel()
    }

    private fun setupDropdown() {
        viewLifecycleOwner.lifecycleScope.launch {
            fieldViewModel.fieldsData.collectLatest { fields ->
                val fieldNames = fields.map { it.fieldName }
                val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, fieldNames)
                binding.inputLahan.setAdapter(adapter)
            }
        }
    }

    private fun setupListeners() {
        binding.btnPredict.setOnClickListener {
            val lahan = binding.inputLahan.text.toString().trim()
            val luas = binding.etLuasField.text.toString().toFloatOrNull()
            val curahHujan = binding.etCurahHujan.text.toString().toFloatOrNull()
            val actualYield = binding.etHasilPanen.text.toString().toFloatOrNull()
            val tmin = binding.etTmin.text.toString().toFloatOrNull()
            val tmax = binding.etTmax.text.toString().toFloatOrNull()

            if (lahan.isEmpty() || luas == null || curahHujan == null ||
                actualYield == null || tmin == null || tmax == null
            ) {
                Toast.makeText(
                    requireContext(),
                    "Please fill in the required fields!",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            predictionViewModel.predictPlantCondition(
                lahan,
                tmin,
                tmax,
                curahHujan,
                luas,
                actualYield
            )
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    predictionViewModel.predictionResult.collect { result ->
                        result?.let {
                            val resultFragment = ResultKondisiTanaman().apply {
                                arguments = Bundle().apply {
                                    putString("condition_label", it.conditionLabel)
                                    putFloat("predicted_yield", it.predictedYield)
                                    putFloat("gap_percentage", it.gapPercentage ?: 0f)
                                    putFloat(
                                        "actual_yield",
                                        binding.etHasilPanen.text.toString().toFloat()
                                    )
                                }
                            }
                            navigateToResult(resultFragment)
                        }
                    }
                }

                launch {
                    predictionViewModel.events.collect { event ->
                        if (event is PredictionViewModel.Event.ShowError) {
                            Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                        }
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
//
//        val lahanList = arrayOf("Lahan 1", "Lahan Manjur Sukses") // Data Dropdown
//        val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, lahanList)
//        binding.inputLahan.setAdapter(adapter)
//
//
//        binding.btnPredict.setOnClickListener {
//
//            val lahan = binding.inputLahan.text.toString().trim()
//            val luasStr = binding.etLuasField.text.toString().trim()
//            val curahHujanStr = binding.etCurahHujan.text.toString().trim()
//            val hasilPanenActualStr = binding.etHasilPanen.text.toString().trim()
//            val tminStr = binding.etTmin.text.toString().trim()
//            val tmaxStr = binding.etTmax.text.toString().trim()
//
//            // 1. Validasi Input Kosong
//            if (lahan.isEmpty() || luasStr.isEmpty() || curahHujanStr.isEmpty() ||
//                hasilPanenActualStr.isEmpty() || tminStr.isEmpty() || tmaxStr.isEmpty()
//            ) {
//                Toast.makeText(requireContext(), "Semua field harus diisi!", Toast.LENGTH_SHORT)
//                    .show()
//                return@setOnClickListener
//            }
//
//            // 2. Konversi ke Float dan Validasi Angka
//            val luas = luasStr.toFloatOrNull()
//            val curahHujan = curahHujanStr.toFloatOrNull()
//            val hasilPanenActual = hasilPanenActualStr.toFloatOrNull()
//            val tmin = tminStr.toFloatOrNull()
//            val tmax = tmaxStr.toFloatOrNull()
//
//            if (luas == null || curahHujan == null || hasilPanenActual == null ||
//                tmin == null || tmax == null
//            ) {
//                Toast.makeText(
//                    requireContext(),
//                    "Input harus berupa angka yang valid!",
//                    Toast.LENGTH_SHORT
//                ).show()
//                return@setOnClickListener
//            }
//
//            // Panggil ViewModel untuk Prediksi dan Simpan Data
//            predictionViewModel.predictAndSaveKondisiTanaman(
//                fieldName = lahan,
//                tmin = tmin,
//                tmax = tmax,
//                rainfall = curahHujan,
//                area = luas,
//                actualYield = hasilPanenActual,
//                onSuccess = { conditionLabel, predictedYield, gapPercentage ->
//                    // 3. Navigasi ke ResultKondisiTanaman Fragment
//                    val resultFragment = ResultKondisiTanaman().apply {
//                        arguments = Bundle().apply {
//                            putString(ARG_CONDITION_LABEL, conditionLabel)
//                            putFloat(ARG_PREDICTED_YIELD, predictedYield)
//                            putFloat(ARG_ACTUAL_YIELD, hasilPanenActual)
//                            putFloat(ARG_GAP_PERCENTAGE, gapPercentage)
//                        }
//                    }
//
//                    parentFragmentManager.beginTransaction()
//                        .replace(R.id.fl_scroll_view_content, resultFragment)
//                        .addToBackStack(null)
//                        .commit()
//                },
//                onError = { message ->
//                    Log.e("KondisiTanaman", "Error: $message")
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