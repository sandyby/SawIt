package com.example.sawit.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import com.example.sawit.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.example.sawit.ml.PredictionUtils // Import fungsi prediksi

class PredictionKondisiTanaman : Fragment(R.layout.fragment_prediction_kondisi_tanaman) {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_prediction_kondisi_tanaman, container, false)

        // ==========================
        // AMBIL SEMUA VIEW DARI XML
        // ==========================
        val inputLahan: AutoCompleteTextView = view.findViewById(R.id.input_lahan)
        val etLuasField: TextInputEditText = view.findViewById(R.id.etLuasField)
        val etCurahHujan: TextInputEditText = view.findViewById(R.id.etCurahHujan)
        val etHasilPanen: TextInputEditText = view.findViewById(R.id.etHasilPanen) // HASIL PANEN AKTUAL
        val etTmin: TextInputEditText = view.findViewById(R.id.etTmin)
        val etTmax: TextInputEditText = view.findViewById(R.id.etTmax)

        val btnPredict: MaterialButton = view.findViewById(R.id.btn_predict)

        // ==========================
        // LOGIKA DROPDOWN (LAHAN LIST)
        // ==========================
        val lahanList = arrayOf("Lahan 1", "Lahan Manjur Sukses") // Data Dropdown
        val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, lahanList)
        inputLahan.setAdapter(adapter)

        // ==========================
        // BUTTON PREDICT
        // ==========================
        btnPredict.setOnClickListener {
            val lahan = inputLahan.text.toString().trim()
            val luasStr = etLuasField.text.toString().trim()
            val curahHujanStr = etCurahHujan.text.toString().trim()
            val hasilPanenActualStr = etHasilPanen.text.toString().trim()
            val tminStr = etTmin.text.toString().trim()
            val tmaxStr = etTmax.text.toString().trim()

            // Validasi Input Kosong
            if (lahan.isEmpty() || luasStr.isEmpty() || curahHujanStr.isEmpty() ||
                hasilPanenActualStr.isEmpty() || tminStr.isEmpty() || tmaxStr.isEmpty()
            ) {
                Toast.makeText(requireContext(), "Semua field harus diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Konversi ke Float dan Validasi Angka
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

            try {
                // 1. Prediksi Yield (menggunakan Regressor ONNX)
                val predictedYield = PredictionUtils.predictYield(
                    requireContext(),
                    tmin,
                    tmax,
                    curahHujan,
                    luas
                )

                // 2. Hitung Gap Persentase: ((Aktual - Prediksi) / Prediksi) * 100
                // Tambahkan 1e-6f untuk menghindari pembagian dengan nol
                val gapPercentage = ((hasilPanenActual - predictedYield) / (predictedYield + 1e-6f)) * 100

                // 3. Klasifikasi Kondisi berdasarkan Aturan Gap
                val conditionLabel = when {
                    gapPercentage >= 15 -> "Baik"       // Aktual jauh lebih tinggi dari prediksi
                    gapPercentage <= -15 -> "Buruk"     // Aktual jauh lebih rendah dari prediksi
                    else -> "Cukup"                     // Aktual mendekati prediksi
                }

                // 4. Tampilkan Hasil Klasifikasi dalam Toast
                Toast.makeText(
                    requireContext(),
                    "Yield Prediksi: ${"%.2f".format(predictedYield)}\nKondisi Tanaman: $conditionLabel (Gap: ${"%.2f".format(gapPercentage)}%)",
                    Toast.LENGTH_LONG
                ).show()

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Gagal melakukan prediksi: ${e.message}", Toast.LENGTH_LONG).show()
            }

            // Logika navigasi ke ResultFragment (dihapus/dikomentari untuk fokus pada Toast)
        }

        return view
    }
}