package com.example.sawit.fragments;

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sawit.R
import com.example.sawit.activities.CreateEditFieldActivity
import com.example.sawit.adapters.FieldsActivityAdapter
import com.example.sawit.adapters.FieldsPredictionHistoryAdapter
import com.example.sawit.databinding.FragmentFieldLogsBinding
import com.example.sawit.models.PredictionHistory
import com.example.sawit.viewmodels.ActivityViewModel
import com.example.sawit.viewmodels.FieldViewModel
import com.example.sawit.viewmodels.PredictionHistoryViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FieldLogsFragment : Fragment(R.layout.fragment_field_logs) {
    private var _binding: FragmentFieldLogsBinding? = null
    private val binding get() = _binding!!
    private val fieldViewModel: FieldViewModel by viewModels()
    private val activityViewModel: ActivityViewModel by viewModels()
    private val predictionHistoryViewModel: PredictionHistoryViewModel by viewModels()
    private lateinit var activityAdapter: FieldsActivityAdapter
    private lateinit var predictionAdapter: FieldsPredictionHistoryAdapter
    private var fieldId: String? = null
    private var fieldName: String? = null
    private var rootView: View? = null

    companion object {
        fun newInstance(fieldId: String, fieldName: String) = FieldLogsFragment().apply {
            arguments = Bundle().apply {
                putString("fieldId", fieldId)
                putString("fieldName", fieldName)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            _binding = FragmentFieldLogsBinding.inflate(inflater, container, false)
            rootView = binding.root
        }
        return rootView!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFieldLogsBinding.bind(view)
        fieldId = arguments?.getString("fieldId")
        fieldName = arguments?.getString("fieldName")

        setupRecyclerViews()
        setupAddDataBtn()
        observeData()
    }

    private fun setupAddDataBtn() {
        binding.tvCtaData.setOnClickListener {
            val predictionFragment = PredictionFragment.newInstance(fieldId, fieldName)

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fl_scroll_view_content, predictionFragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun setupRecyclerViews() {
        activityAdapter = FieldsActivityAdapter()
        binding.rvCompletedActivities.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = activityAdapter
        }

        predictionAdapter = FieldsPredictionHistoryAdapter().apply {
            stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        }
        predictionAdapter.onItemClicked = { history ->
            navigateToResult(history)
        }
        binding.rvFieldPredictions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = predictionAdapter
        }
    }

    private fun navigateToResult(history: PredictionHistory) {
        val fragment =
            if (history.predictionType?.contains("Condition", ignoreCase = true) == true) {
                PredictionConditionResultFragment.newInstance(history, isFromHistory = true)
            } else {
                PredictionYieldResultFragment.newInstance(history, isFromHistory = true)
            }

        parentFragment?.parentFragmentManager?.beginTransaction()?.apply {
            setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_in_left,
                R.anim.slide_in_left,
                R.anim.slide_in_right
            )
            replace(R.id.fl_scroll_view_content, fragment)
            addToBackStack(null)
            commit()
        }
    }

    private fun observeData() {
        val id = fieldId ?: return

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    predictionHistoryViewModel.predictionHistoriesData.collectLatest { history ->
                        val filtered = history.filter { it.fieldId == id }

                        if (filtered.size >= 2) {
                            binding.yieldChart.visibility = View.VISIBLE
                            binding.tvNoChartData.visibility = View.GONE
                            binding.mcvCtaData.visibility = View.GONE
                            setupChart(filtered)
                        } else {
                            binding.yieldChart.visibility = View.GONE
                            binding.tvNoChartData.visibility = View.VISIBLE
                            binding.mcvCtaData.visibility = View.VISIBLE
                        }

                        if (filtered.isEmpty()) {
                            binding.llEmptyPredictions.visibility = View.VISIBLE
                            binding.rvFieldPredictions.visibility = View.GONE
                        } else {
                            binding.llEmptyPredictions.visibility = View.GONE
                            binding.rvFieldPredictions.visibility = View.VISIBLE
                        }

                        predictionAdapter.submitList(filtered)
                    }
                }

                launch {
                    activityViewModel.activities.collectLatest { activities ->
                        val completed =
                            activities.filter { it.fieldId == id && it.status?.lowercase() == "completed" }

                        if (completed.isEmpty()) {
                            binding.llEmptyActivities.visibility = View.VISIBLE
                            binding.rvCompletedActivities.visibility = View.GONE
                        } else {
                            binding.llEmptyActivities.visibility = View.GONE
                            binding.rvCompletedActivities.visibility = View.VISIBLE
                        activityAdapter.submitList(completed)
                        }
                    }
                }
            }
        }
    }

    private fun setupChart(predictions: List<PredictionHistory>) {
        if (binding.yieldChart.data != null && binding.yieldChart.data.entryCount == predictions.size) {
            return
        }

        val sortedData = predictions.sortedBy { it.date }

        val entries = sortedData.map {
            Entry(it.date.time.toFloat(), it.predictedYield)
        }

        val dataSet = LineDataSet(entries, "Yield Prediction").apply {
            color = ContextCompat.getColor(requireContext(), R.color.bg_primary_500)
            valueTextColor = ContextCompat.getColor(requireContext(), R.color.text_primary_900)
            setCircleColor(ContextCompat.getColor(requireContext(), R.color.bg_primary_500))
            lineWidth = 2f
            circleRadius = 4f
            setDrawFilled(true)
            fillDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.chart_gradient)
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        binding.yieldChart.apply {
            data = LineData(dataSet)
            description.isEnabled = false
            legend.isEnabled = false

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                textColor = ContextCompat.getColor(requireContext(), R.color.text_700)
                valueFormatter = object : ValueFormatter() {
                    private val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
                    override fun getFormattedValue(value: Float): String {
                        return dateFormat.format(Date(value.toLong()))
                    }
                }
            }

            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = ContextCompat.getColor(requireContext(), R.color.text_primary_900)
                textColor = ContextCompat.getColor(requireContext(), R.color.text_700)
            }

            axisRight.isEnabled = false
            animateX(1000)
            invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}
