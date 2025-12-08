// File: com/example/sawit/fragments/PredictionHistoryFragment.kt
package com.example.sawit.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sawit.R // Pastikan R di-import (untuk layout ID jika diperlukan)
import com.example.sawit.adapters.PredictionHistoryAdapter
// VVVV KOREKSI UTAMA: Ganti import binding sesuai nama file XML Anda VVVV
import com.example.sawit.databinding.FragmentPredictionHistoryBinding
import com.example.sawit.viewmodels.PredictionHistoryViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// Tambahkan R.layout.fragment_prediction_history untuk konstruktor Fragment
class PredictionHistoryFragment : Fragment(R.layout.fragment_prediction_history) {

    // VVVV KOREKSI UTAMA: Ganti kelas binding di sini VVVV
    private var _binding: FragmentPredictionHistoryBinding? = null
    private val binding get() = _binding!!

    private val historyViewModel: PredictionHistoryViewModel by viewModels()
    private val historyAdapter = PredictionHistoryAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // VVVV KOREKSI UTAMA: Ganti kelas binding di sini VVVV
        _binding = FragmentPredictionHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Akses recyclerViewHistory (ditemukan dari XML Anda)
        binding.recyclerViewHistory.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = historyAdapter
        }

        viewLifecycleOwner.lifecycleScope.launch {
            historyViewModel.allHistory.collectLatest { historyList ->
                historyAdapter.submitList(historyList)
                // Akses tvEmptyMessage (ditemukan dari XML Anda)
                binding.tvEmptyMessage.visibility = if (historyList.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        binding.fabPredict.setOnClickListener {
            // Lakukan navigasi ke Fragment Prediksi (PredictFragment)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fl_scroll_view_content, PredictFragment())
                .addToBackStack(null) // Tambahkan ke back stack agar bisa kembali
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}