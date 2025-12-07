package com.example.sawit.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.sawit.R
import com.example.sawit.databinding.FragmentResultTotalPanenBinding // Gunakan View Binding jika ada

class ResultTotalPanen : Fragment(R.layout.fragment_result_total_panen) { // Nama Fragment Sesuai Upload

    private var binding: FragmentResultTotalPanenBinding? = null

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
        // Asumsi Anda menggunakan View Binding. Sesuaikan jika tidak.
        binding = FragmentResultTotalPanenBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ambil data dari arguments
        val predictedYield = arguments?.getFloat(ARG_PREDICTED_YIELD) ?: 0.0f
        val tmin = arguments?.getFloat(ARG_TMIN) ?: 0.0f
        val tmax = arguments?.getFloat(ARG_TMAX) ?: 0.0f
        val rainfall = arguments?.getFloat(ARG_RAINFALL) ?: 0.0f
        val area = arguments?.getFloat(ARG_AREA) ?: 0.0f

        // 1. Tampilkan Hasil Utama
        binding?.tvPredictedYieldValue?.text = "%.2f".format(predictedYield)

        // 2. Tampilkan Detail Input
        binding?.tvDetailTmin?.text = "%.2f °C".format(tmin)
        binding?.tvDetailTmax?.text = "%.2f °C".format(tmax)
        binding?.tvDetailRainfall?.text = "%.2f mm".format(rainfall)
        binding?.tvDetailArea?.text = "%.2f Ha".format(area)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}