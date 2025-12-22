package com.example.sawit.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import com.example.sawit.R
import com.example.sawit.databinding.FragmentPredictionConditionResultBinding
import com.example.sawit.models.PredictionHistory
import ir.ehsannarmani.compose_charts.ColumnChart
import ir.ehsannarmani.compose_charts.models.BarProperties
import ir.ehsannarmani.compose_charts.models.Bars

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
                    putFloat("actual_yield", history.actualYield ?: 0f)
                    putFloat("gap_percentage", history.gapPercentage ?: 0f)
                    putBoolean(ARG_IS_FROM_HISTORY, isFromHistory)
                }
            }

        fun newInstance(
            conditionLabel: String,
            predictedYield: Float,
            actualYield: Float,
            gapPercentage: Float,
            isFromHistory: Boolean
        ) = PredictionConditionResultFragment().apply {
            arguments = Bundle().apply {
                putString("condition_label", conditionLabel)
                putFloat("predicted_yield", predictedYield)
                putFloat("actual_yield", actualYield)
                putFloat("gap_percentage", gapPercentage)
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
        val isFromHistory = arguments?.getBoolean(ARG_IS_FROM_HISTORY) ?: false

        val safePredicted = maxOf(0f, predictedYield)
        val safeActual = maxOf(0f, actualYield)

        val safeGap = if (safePredicted == 0f && safeActual > 0f) -100f
        else if (safePredicted == 0f && safeActual == 0f) 0f
        else gapPercentage

        binding.tvConditionLabel.text = conditionLabel
        binding.tvYieldGapValue.text = "${"%.2f".format(safeGap)} %"
        binding.tvActualYieldValue.text = "${"%.2f".format(safeActual)} kg"
        binding.tvPredictedYieldValue.text = "${"%.2f".format(safePredicted)} kg"

        val colorInt = when (conditionLabel.lowercase()) {
            "good" -> "#7D8657".toColorInt()
            "moderate" -> "#ADB6BD".toColorInt()
            "bad" -> "#D00000".toColorInt()
            else -> android.graphics.Color.GRAY
        }
        binding.cardConditionResult.setCardBackgroundColor(colorInt)

        binding.composeViewChart.setContent {
            if (safePredicted > 0f || safeActual > 0f) {
                YieldComparisonChart(safePredicted, safeActual)
            } else {
                NoDataPlaceholder()
            }
        }

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
    fun NoDataPlaceholder() {
        Box(modifier = Modifier.fillMaxWidth().height(250.dp), contentAlignment = Alignment.Center) {
            Text(text = "No yield data available", color = colorResource(R.color.text_primary_900), fontSize = 14.sp)
        }
    }

    @Composable
    fun YieldComparisonChart(predicted: Float, actual: Float) {

        val maxYValue = maxOf(predicted, actual)
        val calculatedMax = if (maxYValue < 10f) 10.0 else (maxYValue * 1.25).toDouble()

        val predictedColor = Color("#14FF4B".toColorInt())
        val actualColor = Color("#2196F3".toColorInt())

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
                            color = Brush.verticalGradient(
                                listOf(
                                    actualColor,
                                    actualColor.copy(alpha = 0.6f)
                                )
                            )
                        )
                    )
                )
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
                minValue = 0.0,
                maxValue = calculatedMax,
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