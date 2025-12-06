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
import androidx.lifecycle.lifecycleScope
import com.example.sawit.R
import com.example.sawit.databinding.FragmentPredictionTotalPanenBinding
import com.example.sawit.viewmodels.FieldViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.getValue

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PredictionTotalPanen.newInstance] factory method to
 * create an instance of this fragment.
 */
class PredictionTotalPanen : Fragment(R.layout.fragment_prediction_total_panen) {

    private var _binding: FragmentPredictionTotalPanenBinding? = null
    private val binding get() = _binding!!

    private val fieldViewModel: FieldViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPredictionTotalPanenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            fieldViewModel.fieldsData.collectLatest { fields ->
                val fieldNames = fields.map { it.fieldName }
                val adapter = ArrayAdapter(
                    requireContext(),
                    R.layout.dropdown_item,
                    fieldNames
                )
                binding.inputFieldName.setAdapter(adapter)
            }
        }

        binding.btnPredict.setOnClickListener {
            Log.d("PredictionsFragment", "Tes")

            val selectedField = binding.inputFieldName.text.toString()
            val fieldArea = binding.inputFieldArea.text.toString()
            val palmAge = binding.inputPalmAge.text.toString()
            val rainfall = binding.inputRainfall.text.toString()
            val temperature = binding.inputTemperature.text.toString()

            if (selectedField.isEmpty() || fieldArea.isEmpty() ||
                palmAge.isEmpty() || rainfall.isEmpty() || temperature.isEmpty()
            ) {
                Toast.makeText(requireContext(), "Harap isi semua data!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val resultFragment = ResultFragment().apply {
                arguments = Bundle().apply {
                    putString("selectedField", selectedField)
                    putString("fieldArea", fieldArea)
                    putString("palmAge", palmAge)
                    putString("rainfall", rainfall)
                    putString("temperature", temperature)
                }
            }

            parentFragmentManager.beginTransaction()
                .replace(R.id.fl_scroll_view_content, resultFragment)
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}