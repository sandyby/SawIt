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
        val etHasilPanen: TextInputEditText = view.findViewById(R.id.etHasilPanen)
        val etTmin: TextInputEditText = view.findViewById(R.id.etTmin)
        val etTmax: TextInputEditText = view.findViewById(R.id.etTmax)

        val btnPredict: MaterialButton = view.findViewById(R.id.btn_predict)

        // Dropdown data example
        val lahanList = arrayOf("Lahan 1", "Lahan Manjur Sukses")
        val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, lahanList)
        inputLahan.setAdapter(adapter)

        // ==========================
        // BUTTON PREDICT
        // ==========================
        btnPredict.setOnClickListener {
            val lahan = inputLahan.text.toString().trim()
            val luas = etLuasField.text.toString().trim()
            val curahHujan = etCurahHujan.text.toString().trim()
            val hasilPanen = etHasilPanen.text.toString().trim()
            val tmin = etTmin.text.toString().trim()
            val tmax = etTmax.text.toString().trim()

            // Validasi
            if (lahan.isEmpty() || luas.isEmpty() || curahHujan.isEmpty() ||
                hasilPanen.isEmpty() || tmin.isEmpty() || tmax.isEmpty()
            ) {
                Toast.makeText(requireContext(), "Semua field harus diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Selanjutnya, kirim input ke model ML / ONNX
            val resultFragment = ConditionResultFragment()

            parentFragmentManager.beginTransaction()
                .replace(R.id.fl_scroll_view_content, resultFragment)
                .addToBackStack(null)
                .commit()
        }

        return view
    }
}
