package com.example.sawit.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sawit.databinding.ItemPredictionHistoryCardBinding
import com.example.sawit.models.PredictionHistory
import java.text.SimpleDateFormat
import java.util.*

class PredictionHistoryAdapter : ListAdapter<PredictionHistory, PredictionHistoryAdapter.HistoryViewHolder>(HistoryDiffCallback()) {

    var onItemClicked: ((PredictionHistory) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemPredictionHistoryCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClicked)
    }

    class HistoryViewHolder(private val binding: ItemPredictionHistoryCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(history: PredictionHistory, clickListener: ((PredictionHistory) -> Unit)?) {
            val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())

            binding.tvHistoryDate.text = dateFormat.format(history.date)
            binding.tvFieldName.text = history.fieldName
            binding.tvPredictionType.text = history.predictionType
            binding.tvPredictedYield.text = String.format("%,.2f kg", history.predictedYield)

            if (history.predictionType?.contains("Condition", ignoreCase = true) == true) {
                setupConditionUI(history.conditionLabel)
            } else {
                setupHarvestUI()
            }

            binding.root.setOnClickListener { clickListener?.invoke(history) }
        }

        private fun setupConditionUI(label: String?) {
            binding.cardConditionChip.visibility = View.VISIBLE

            val statusColor = when (label?.lowercase()) {
                "good" -> Color.parseColor("#7D8657")
                "moderate" -> Color.parseColor("#FFC107")
                "bad" -> Color.parseColor("#D00000")
                else -> Color.parseColor("#ADB6BD")
            }

            binding.tvCondition.text = label?.uppercase() ?: "N/A"
            binding.tvCondition.setTextColor(statusColor)
            binding.viewStatusIndicator.setBackgroundColor(statusColor)

            val alphaColor = ColorUtils.setAlphaComponent(statusColor, 38)
            binding.cardConditionChip.setCardBackgroundColor(alphaColor)
        }

        private fun setupHarvestUI() {
            binding.cardConditionChip.visibility = View.GONE
            binding.viewStatusIndicator.setBackgroundColor(Color.parseColor("#273617"))
        }
    }
}

class HistoryDiffCallback : DiffUtil.ItemCallback<PredictionHistory>() {
    override fun areItemsTheSame(oldItem: PredictionHistory, newItem: PredictionHistory): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: PredictionHistory, newItem: PredictionHistory): Boolean =
        oldItem == newItem
}