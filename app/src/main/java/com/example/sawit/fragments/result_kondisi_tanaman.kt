package com.example.sawit.fragments

import android.R.attr.data
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.ui.platform.ComposeView
import com.example.sawit.R
import com.example.sawit.databinding.FragmentResultKondisiTanamanBinding // Digunakan untuk view non-compose
import com.google.android.material.card.MaterialCardView

// Import Compose Charts
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb

// Import Compose UI Components
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
import ir.ehsannarmani.compose_charts.models.LabelProperties
import androidx.core.graphics.toColorInt

class ResultKondisiTanaman : Fragment(R.layout.fragment_result_kondisi_tanaman) {

    private var binding: FragmentResultKondisiTanamanBinding? = null

    companion object {
        const val ARG_CONDITION_LABEL = "condition_label" // Baik/Cukup/Buruk
        const val ARG_PREDICTED_YIELD = "predicted_yield"
        const val ARG_ACTUAL_YIELD = "actual_yield"
        const val ARG_GAP_PERCENTAGE = "gap_percentage"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Asumsi View Binding terinstal dan layout XML sudah diupdate
        binding = FragmentResultKondisiTanamanBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ambil data dari arguments
        val conditionLabel = arguments?.getString(ARG_CONDITION_LABEL) ?: "N/A"
        val predictedYield = arguments?.getFloat(ARG_PREDICTED_YIELD) ?: 0.0f
        val actualYield = arguments?.getFloat(ARG_ACTUAL_YIELD) ?: 0.0f
        val gapPercentage = arguments?.getFloat(ARG_GAP_PERCENTAGE) ?: 0.0f

        // 1. Tampilkan Hasil Klasifikasi Utama & Sesuaikan Warna Card (Bagian XML Biasa)
        binding?.tvConditionLabel?.text = conditionLabel

        val colorInt = when (conditionLabel) {
            "Baik" -> "#4CAF50".toColorInt() // Hijau
            "Cukup" -> "#FFC107".toColorInt() // Kuning
            "Buruk" -> "#F44336".toColorInt() // Merah
            else -> android.graphics.Color.GRAY
        }
        binding?.cardConditionResult?.setCardBackgroundColor(colorInt)

        // 2. Tampilkan Detail Analisis (Bagian XML Biasa)
        binding?.tvGapPercentageValue?.text = "${"%.2f".format(gapPercentage)} %"
        binding?.tvActualYieldValue?.text = "${"%.2f".format(actualYield)} kg"
        binding?.tvPredictedYieldValueDetail?.text = "${"%.2f".format(predictedYield)} kg"

        // 3. Hosting Compose View untuk Chart
        // Asumsi Anda mengganti BarChart XML (com.github.mikephil...) dengan ComposeView di XML
        // Jika Anda masih menggunakan BarChart XML, ganti elemennya menjadi ComposeView:
        // <androidx.compose.ui.platform.ComposeView
        //     android:id="@+id/compose_view_chart"
        //     android:layout_width="match_parent"
        //     android:layout_height="300dp" />

        val composeView = view.findViewById<ComposeView>(R.id.compose_view_chart) // ID ComposeView di XML
        composeView.setContent {
            YieldComparisonChart(predictedYield, actualYield)
        }
    }

    // ==========================================================
    // FUNGSI COMPOSABLE CHART DENGAN COMPOSE CHARTS LIBRARY
    // ==========================================================
    @Composable
    fun YieldComparisonChart(predicted: Float, actual: Float) {

        // Konversi warna ke Compose Color dan Brush
        val predictedColor = androidx.compose.ui.graphics.Color("#2196F3".toColorInt()) // Biru
        val actualColor = androidx.compose.ui.graphics.Color("#FF9800".toColorInt()) // Orange

        // Gunakan ingat (remember) untuk efisiensi Compose
        val chartData = remember {
            listOf(
                // Kita hanya memiliki satu kategori utama: Yield
                Bars(
                    label = "Yield",
                    values = listOf(
                        // 1. Bar untuk Yield Prediksi
                        Bars.Data(
                            label = "Predicted",
                            value = predicted.toDouble(), // Konversi ke Double
                            color = Brush.verticalGradient(listOf(predictedColor, predictedColor.copy(alpha = 0.6f)))
                        ),
                        // 2. Bar untuk Yield Aktual
                        Bars.Data(
                            label = "Actual",
                            value = actual.toDouble(), // Konversi ke Double
                            color = SolidColor(actualColor)
                        )
                    ),
                ),
            )
        }

        Column(modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(top = 16.dp)) {

            Text(
                text = "Perbandingan Yield (kg)",
                style = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                modifier = Modifier.padding(start = 22.dp, bottom = 8.dp)
            )

            // Mengganti BarChart dengan ColumnChart
            ColumnChart(
                modifier = Modifier.fillMaxSize().padding(horizontal = 22.dp),
                data = chartData,
                barProperties = BarProperties(
                    // Opsi Kustomisasi Bar
                    thickness = 20.dp,
                    spacing = 16.dp, // Spasi antara group bar
                    cornerRadius = Bars.Data.Radius.Rectangle(topRight = 6.dp, topLeft = 6.dp)
                ),
                // Opsional: Menambahkan animasi
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                // Opsi kustomisasi lainnya (Grid, Label, dll.) bisa ditambahkan di sini
            )
        }
    }
//    @Composable
//    fun YieldComparisonChart(predicted: Float, actual: Float) {
//
//        // Definisikan Data untuk Bar Chart
//        val data = listOf(
//            Bars(
//                title = "Prediksi",
//                value = predicted,
//                color = androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor("#2196F3")) // Biru
//            ),
//            Bars(
//                title = "Aktual",
//                value = actual,
//                color = androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor("#FF9800")) // Orange
//            )
//        )
//
//        Column(modifier = Modifier
//            .fillMaxWidth()
//            .height(300.dp)
//            .padding(top = 16.dp)) {
//
//            Text(text = "Perbandingan Yield (kg)", style = androidx.compose.ui.text.TextStyle(fontSize = androidx.compose.ui.unit.sp(14)))
//
//            ColumnChart(
//                data = remember { data },
//                modifier = Modifier.fillMaxSize(),
//                barColor = data.map{ bar -> bar[0] } // Menggunakan warna yang sudah didefinisikan di atas
//            )
//        }
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}