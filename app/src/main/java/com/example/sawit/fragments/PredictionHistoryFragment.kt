//// File: com/example/sawit/fragments/PredictionHistoryFragment.kt
//package com.example.sawit.fragments
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.fragment.app.Fragment
//import androidx.fragment.app.viewModels
//import androidx.lifecycle.lifecycleScope
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.example.sawit.adapters.PredictionHistoryAdapter // <--- Menggunakan Adapter Baru
//import com.example.sawit.databinding.FragmentHistoryBinding // Ganti dengan nama layout Anda
//import com.example.sawit.viewmodels.PredictionHistoryViewModel // <--- Menggunakan ViewModel Baru
//import kotlinx.coroutines.flow.collectLatest
//import kotlinx.coroutines.launch
//
//class PredictionHistoryFragment : Fragment() { // <--- Penamaan Baru
//
//    private var _binding: FragmentHistoryBinding? = null
//    private val binding get() = _binding!!
//
//    private val historyViewModel: PredictionHistoryViewModel by viewModels() // <--- Menggunakan ViewModel Baru
//    private val historyAdapter = PredictionHistoryAdapter() // <--- Menggunakan Adapter Baru
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        binding.recyclerViewHistory.apply {
//            layoutManager = LinearLayoutManager(context)
//            adapter = historyAdapter
//        }
//
//        // Ambil data dari ViewModel (yang mengambil dari Firestore secara realtime)
//        viewLifecycleOwner.lifecycleScope.launch {
//            historyViewModel.allHistory.collectLatest { historyList ->
//                historyAdapter.submitList(historyList)
//                binding.tvEmptyMessage.visibility = if (historyList.isEmpty()) View.VISIBLE else View.GONE
//            }
//        }
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//}