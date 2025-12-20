package com.example.sawit.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.sawit.R
import com.example.sawit.databinding.FragmentPredictionYieldResultBinding
import com.example.sawit.models.PredictionHistory

class PredictionYieldResultFragment :
    Fragment(R.layout.fragment_prediction_yield_result) {
    private var _binding: FragmentPredictionYieldResultBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_IS_FROM_HISTORY = "is_from_history"

        fun newInstance(history: PredictionHistory, isFromHistory: Boolean) =
            PredictionYieldResultFragment().apply {
                arguments = Bundle().apply {
                    putFloat("predicted_yield", history.predictedYield)
                    putFloat("tmin", history.tmin)
                    putFloat("tmax", history.tmax)
                    putFloat("rainfall", history.rainfall)
                    putFloat("area", history.area)
                    putBoolean(ARG_IS_FROM_HISTORY, isFromHistory)
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPredictionYieldResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPredictionYieldResultBinding.bind(view)

        val predictedYield = arguments?.getFloat("predicted_yield") ?: 0.0f
        val tmin = arguments?.getFloat("tmin") ?: 0.0f
        val tmax = arguments?.getFloat("tmax") ?: 0.0f
        val rainfall = arguments?.getFloat("rainfall") ?: 0.0f
        val area = arguments?.getFloat("area") ?: 0.0f

        binding.tvPredictedYieldValue.text = "%.2f".format(predictedYield)

        binding.tvDetailTmin.text = "${"%.1f".format(tmin)}°C"
        binding.tvDetailTmax.text = "${"%.1f".format(tmax)}°C"
        binding.tvDetailRainfall.text = "${"%.1f".format(rainfall)} mm"
        binding.tvDetailArea.text = "${"%.2f".format(area)} ha"

        val isFromHistory = arguments?.getBoolean(ARG_IS_FROM_HISTORY) ?: false

        binding.btnContinue.setOnClickListener {
            if (isFromHistory) {
                parentFragmentManager.popBackStack()
            } else {
                parentFragmentManager.popBackStack()
                parentFragmentManager.popBackStack()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}