package com.example.sawit.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sawit.R
import com.example.sawit.databinding.ItemPredictionHistoryCardBinding
import com.example.sawit.models.PredictionHistory
import java.text.SimpleDateFormat
import java.util.Locale

class FieldsPredictionHistoryAdapter :
    ListAdapter<PredictionHistory, FieldsPredictionHistoryAdapter.ViewHolder>(DiffCallback) {
    var onItemClicked: ((PredictionHistory) -> Unit)? = null
    inner class ViewHolder(private val binding: ItemPredictionHistoryCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PredictionHistory, clickListener: ((PredictionHistory) -> Unit)?) {
            binding.tvPredictionType.text = item.predictionType
            binding.tvHistoryDate.text =
                SimpleDateFormat("dd MMM yyyy hh:mm", Locale.getDefault()).format(
                    item.date
                )
            binding.tvPredictedYield.text = "${item.predictedYield} kg"

            if (item.predictionType == "Condition") {
                binding.tvPredictionType.visibility = android.view.View.VISIBLE
                binding.tvCondition.text = item.conditionLabel

                val color = when (item.conditionLabel?.lowercase()) {
                    "good" -> R.color.bg_primary_500
                    "bad" -> R.color.text_fiery_red_sunset_600
                    else -> R.color.text_400
                }
                binding.tvCondition.setTextColor(
                    ContextCompat.getColor(
                        itemView.context,
                        color
                    )
                )
            } else {
                binding.tvCondition.visibility = android.view.View.GONE
            }

            binding.root.setOnClickListener { clickListener?.invoke(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemPredictionHistoryCardBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClicked)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<PredictionHistory>() {
        override fun areItemsTheSame(oldItem: PredictionHistory, newItem: PredictionHistory) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: PredictionHistory, newItem: PredictionHistory) =
            oldItem == newItem
    }
}