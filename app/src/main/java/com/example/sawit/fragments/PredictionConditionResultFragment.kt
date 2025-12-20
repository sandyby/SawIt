package com.example.sawit.fragments

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import com.example.sawit.R
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.ehsannarmani.compose_charts.ColumnChart
import ir.ehsannarmani.compose_charts.models.BarProperties
import ir.ehsannarmani.compose_charts.models.Bars
import androidx.core.graphics.toColorInt
import com.example.sawit.databinding.FragmentPredictionConditionResultBinding
import com.example.sawit.models.PredictionHistory

class PredictionConditionResultFragment : Fragment(R.layout.fragment_prediction_condition_result) {
    private var _binding: FragmentPredictionConditionResultBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_IS_FROM_HISTORY = "is_from_history"

        fun newInstance(history: PredictionHistory, isFromHistory: Boolean) =
            PredictionConditionResultFragment().apply {
                arguments = Bundle().apply {
                    putString("condition_label", history.conditionLabel)
                    putFloat("predicted_yield", history.predictedYield)
                    putFloat("actual_yield", history.actualYield!!)
                    putFloat("gap_percentage", history.gapPercentage!!)
                    putBoolean(ARG_IS_FROM_HISTORY, isFromHistory)
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPredictionConditionResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val conditionLabel = arguments?.getString("condition_label") ?: "N/A"
        val predictedYield = arguments?.getFloat("predicted_yield") ?: 0.0f
        val actualYield = arguments?.getFloat("actual_yield") ?: 0.0f
        val gapPercentage = arguments?.getFloat("gap_percentage") ?: 0.0f

        binding.tvConditionLabel.text = conditionLabel

        val colorInt = when (conditionLabel) {
            "Good" -> "#7D8657".toColorInt()
            "Moderate" -> "#ADB6BD".toColorInt()
            "Bad" -> "#d00000".toColorInt()
            else -> Color.GRAY
        }
        binding.cardConditionResult.setCardBackgroundColor(colorInt)

        binding.tvYieldGapValue.text = "${"%.2f".format(gapPercentage)} %"
        binding.tvActualYieldValue.text = "${"%.2f".format(actualYield)} kg"
        binding.tvPredictedYieldValue.text = "${"%.2f".format(predictedYield)} kg"

        binding.composeViewChart.setContent {
            YieldComparisonChart(predictedYield, actualYield)
        }

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

    @Composable
    fun YieldComparisonChart(predicted: Float, actual: Float) {

        val predictedColor = androidx.compose.ui.graphics.Color("#2196F3".toColorInt()) // Biru
        val actualColor = androidx.compose.ui.graphics.Color("#FF9800".toColorInt()) // Orange

        val chartData = remember {
            listOf(
                Bars(
                    label = "Yield",
                    values = listOf(
                        Bars.Data(
                            label = "Predicted",
                            value = predicted.toDouble(),
                            color = Brush.verticalGradient(
                                listOf(
                                    predictedColor,
                                    predictedColor.copy(alpha = 0.6f)
                                )
                            )
                        ),
                        Bars.Data(
                            label = "Actual",
                            value = actual.toDouble(),
                            color = SolidColor(actualColor)
                        )
                    ),
                ),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(top = 16.dp)
        ) {

            Text(
                text = "Yield Comparison (kg)",
                style = androidx.compose.ui.text.TextStyle(
                    fontSize = 16.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                ),
                modifier = Modifier.padding(start = 22.dp, bottom = 8.dp)
            )

            ColumnChart(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 22.dp),
                data = chartData,
                barProperties = BarProperties(
                    thickness = 20.dp,
                    spacing = 16.dp,
                    cornerRadius = Bars.Data.Radius.Rectangle(topRight = 6.dp, topLeft = 6.dp)
                ),
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}