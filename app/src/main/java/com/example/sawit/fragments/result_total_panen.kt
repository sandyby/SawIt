package com.example.sawit.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.sawit.R
import com.example.sawit.databinding.FragmentResultKondisiTanamanBinding
import com.example.sawit.databinding.FragmentResultTotalPanenBinding // Gunakan View Binding jika ada

class ResultTotalPanen :
    Fragment(R.layout.fragment_result_total_panen) { // Nama Fragment Sesuai Upload

    private var _binding: FragmentResultTotalPanenBinding? = null
    private val binding get() = _binding!!

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
    ): View? {
        _binding = FragmentResultTotalPanenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentResultTotalPanenBinding.bind(view)

        val predictedYield = arguments?.getFloat("predicted_yield") ?: 0.0f
        val tmin = arguments?.getFloat("tmin") ?: 0.0f
        val tmax = arguments?.getFloat("tmax") ?: 0.0f
        val rainfall = arguments?.getFloat("rainfall") ?: 0.0f
        val area = arguments?.getFloat("area") ?: 0.0f

        binding.tvPredictedYieldValue.text = "%.2f".format(predictedYield)

        binding.tvDetailTmin.text = "${"%.1f".format(tmin)}°C"
        binding.tvDetailTmax.text = "${"%.1f".format(tmax)}°C"
        binding.tvDetailRainfall.text = "${"%.1f".format(rainfall)} mm"
        binding.tvDetailArea.text = "${"%.2f".format(area)} Ha"

        binding.btnDone.setOnClickListener {
            parentFragmentManager.popBackStack()
            parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}