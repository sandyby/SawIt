package com.example.sawit.fragments

import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.compose.ui.text.font.Typeface
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.alpha
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sawit.R
import com.example.sawit.adapters.PredictionHistoryAdapter
import com.example.sawit.adapters.PredictionsFooterAdapter
import com.example.sawit.databinding.FragmentPredictionHistoryBinding
import com.example.sawit.models.PredictionHistory
import com.example.sawit.viewmodels.PredictionHistoryViewModel
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PredictionHistoryFragment : Fragment(R.layout.fragment_prediction_history) {

    private var _binding: FragmentPredictionHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PredictionHistoryViewModel by viewModels()
    private lateinit var historyAdapter: PredictionHistoryAdapter
    private lateinit var footerAdapter: PredictionsFooterAdapter

    override fun onStart() {
        super.onStart()
        viewModel.listenForHistoryUpdates()
    }

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
        setupSwipeToDelete()
        setupListeners()
        observeViewModel()
    }

    private fun setupSwipeToDelete() {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                rv: RecyclerView, vh: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                val deletedItem = historyAdapter.currentList[position]
                val deletedId = deletedItem.id ?: return

                val snackbar = Snackbar.make(
                    binding.root,
                    "Successfuly deleted ${deletedItem.fieldName} history!",
                    Snackbar.LENGTH_LONG
                )

                val snackbarView = snackbar.view

                snackbarView.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.bg_snackbar_rounded)
                snackbar.setActionTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.text_primary_900
                    )
                )

                val tvText =
                    snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                tvText.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                tvText.textSize = 14f

                tvText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_filled_delete_24_text_primary_900,
                    0,
                    0,
                    0
                )
                tvText.compoundDrawablePadding = 16

                val tvAction =
                    snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_action)
                tvAction.isAllCaps = false
                tvAction.setTypeface(tvAction.typeface, android.graphics.Typeface.BOLD)

                snackbar.setAction("UNDO") {
                    historyAdapter.notifyItemChanged(position)
                }

                snackbar.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                        if (event != DISMISS_EVENT_ACTION) {
                            viewModel.deleteHistory(deletedId)
                        }
                    }
                })

                snackbar.show()
            }

            override fun getSwipeDirs(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                if (viewHolder is PredictionHistoryAdapter.HistoryViewHolder) {
                    return super.getSwipeDirs(recyclerView, viewHolder)
                }
                return 0
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val itemHeight = itemView.bottom - itemView.top

                val paint = android.graphics.Paint().apply {
                    color =
                        ContextCompat.getColor(requireContext(), R.color.text_fiery_red_sunset_600)
                    alpha = 128
                }
                val background = android.graphics.RectF(
                    itemView.right.toFloat() + dX,
                    itemView.top.toFloat(),
                    itemView.right.toFloat(),
                    itemView.bottom.toFloat()
                )
                c.drawRect(background, paint)

                val icon = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_filled_delete_24_text_primary_900
                )
                icon?.setTint(ContextCompat.getColor(requireContext(), R.color.white))
                val iconMargin = (itemHeight - (icon?.intrinsicHeight ?: 0)) / 2
                val iconTop = itemView.top + (itemHeight - (icon?.intrinsicHeight ?: 0)) / 2
                val iconBottom = iconTop + (icon?.intrinsicHeight ?: 0)

                val iconLeft = itemView.right - iconMargin - (icon?.intrinsicWidth ?: 0)
                val iconRight = itemView.right - iconMargin

                icon?.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                icon?.draw(c)

                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewHistory)
    }

    private fun setupRecyclerView() {
        historyAdapter = PredictionHistoryAdapter()

        footerAdapter = PredictionsFooterAdapter()

        val concatAdapter = ConcatAdapter(historyAdapter, footerAdapter)

        historyAdapter.onItemClicked = { history ->
            navigateToResult(history)
        }

        binding.recyclerViewHistory.apply {
            adapter = concatAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun navigateToResult(history: PredictionHistory) {
        Log.d("PredictionHistoryFragment", "navigateToResult: $history ")
        val fragment =
            if (history.predictionType?.contains("Condition", ignoreCase = true) == true) {
                PredictionConditionResultFragment.newInstance(history, isFromHistory = true)
            } else {
                Log.d("PredictionHistoryFragment", "navigateToResult: $history")
                PredictionYieldResultFragment.newInstance(history, isFromHistory = true)
            }

        parentFragmentManager.beginTransaction()
            .replace(R.id.fl_scroll_view_content, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun setupListeners() {
        binding.fabPredict.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fl_scroll_view_content, PredictionFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.predictionHistoriesData.collectLatest { histories ->
                        historyAdapter.submitList(histories) {
                            if (histories.isNotEmpty()) {
                                binding.recyclerViewHistory.scrollToPosition(0)
                            }
                        }
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