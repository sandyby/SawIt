package com.example.sawit.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
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

    private val viewModel: PredictionHistoryViewModel by activityViewModels()
    private lateinit var historyAdapter: PredictionHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPredictionHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPredictionHistoryBinding.bind(view)

        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        historyAdapter = PredictionHistoryAdapter()

        binding.recyclerViewHistory.apply {
            adapter = historyAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupListeners() {
        binding.fabPredict.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fl_scroll_view_content, PredictFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.predictionHistoriesData.collect { histories ->
                        historyAdapter.submitList(histories)
                        binding.clEmptyStatePredictionHistories.visibility =
                            if (histories.isEmpty()) View.VISIBLE else View.GONE
                        binding.recyclerViewHistory.visibility =
                            if (histories.isEmpty()) View.GONE else View.VISIBLE
                    }
                }

                launch {
                    viewModel.events.collect { event ->
                        when (event) {
                            is PredictionHistoryViewModel.Event.ShowMessage -> {
                                Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT)
                                    .show()
                            }

                            is PredictionHistoryViewModel.Event.FinishActivity -> {
                                requireActivity().onBackPressedDispatcher.onBackPressed()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}