package com.example.sawit.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sawit.R
import com.example.sawit.adapters.PredictionHistoryAdapter
import com.example.sawit.databinding.FragmentPredictionHistoryBinding
import com.example.sawit.viewmodels.PredictionHistoryViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PredictionHistoryFragment : Fragment(R.layout.fragment_prediction_history) {

    private var _binding: FragmentPredictionHistoryBinding? = null
    private val binding get() = _binding!!

    private val historyViewModel: PredictionHistoryViewModel by viewModels()
    private val historyAdapter = PredictionHistoryAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPredictionHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerViewHistory.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = historyAdapter
        }

        viewLifecycleOwner.lifecycleScope.launch {
            historyViewModel.allHistory.collectLatest { historyList ->
                historyAdapter.submitList(historyList)
                binding.tvEmptyMessage.visibility = if (historyList.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        binding.fabPredict.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fl_scroll_view_content, PredictFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}