package com.example.sawit.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sawit.databinding.FragmentCardPredictionHistoryBinding
import com.example.sawit.models.PredictionHistory
import java.text.SimpleDateFormat
import java.util.*

class PredictionHistoryAdapter : ListAdapter<PredictionHistory, PredictionHistoryAdapter.HistoryViewHolder>(HistoryDiffCallback()) {

    var onItemClicked: ((PredictionHistory) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = FragmentCardPredictionHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClicked)
    }

    class HistoryViewHolder(private val binding: FragmentCardPredictionHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(history: PredictionHistory, clickListener: ((PredictionHistory) -> Unit)?) {
            val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())

            // 1. Basic Information
            binding.tvHistoryDate.text = dateFormat.format(Date(history.date))
            binding.tvFieldName.text = history.fieldName
            binding.tvPredictionType.text = history.predictionType
            binding.tvPredictedYield.text = String.format("%,.2f kg", history.predictedYield)

            // 2. Differentiate Harvest vs Condition
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
                "good" -> Color.parseColor("#7D8657")    // Brand Green
                "enough" -> Color.parseColor("#FFC107")  // Amber
                "bad" -> Color.parseColor("#D00000")     // Fiery Red
                else -> Color.parseColor("#ADB6BD")      // Gray
            }

            binding.tvCondition.text = label?.uppercase() ?: "N/A"
            binding.tvCondition.setTextColor(statusColor)
            binding.viewStatusIndicator.setBackgroundColor(statusColor)

            // Set light background for the chip (15% opacity)
            val alphaColor = ColorUtils.setAlphaComponent(statusColor, 38) // 38 is ~15% alpha
            binding.cardConditionChip.setCardBackgroundColor(alphaColor)
        }

        private fun setupHarvestUI() {
            binding.cardConditionChip.visibility = View.GONE
            // Use your dark primary color for Harvest indicators
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


//// File: com/example/sawit/adapters/PredictionHistoryAdapter.kt
//package com.example.sawit.adapters
//
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.recyclerview.widget.DiffUtil
//import androidx.recyclerview.widget.ListAdapter
//import androidx.recyclerview.widget.RecyclerView
//import com.example.sawit.models.PredictionHistory
//// VVVV KOREKSI UTAMA: Menggunakan nama binding baru VVVV
//import com.example.sawit.databinding.FragmentCardPredictionHistoryBinding
//import java.text.SimpleDateFormat
//import java.util.Date
//import java.util.Locale
//
//class PredictionHistoryAdapter : ListAdapter<PredictionHistory, PredictionHistoryAdapter.HistoryViewHolder>(HistoryDiffCallback()) {
//
//    var onItemClicked: ((PredictionHistory) -> Unit)? = null
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
//        val binding = FragmentCardPredictionHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//        return HistoryViewHolder(binding)
//    }
//
//    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
//        holder.bind(getItem(position), onItemClicked)
//    }
//
//    class HistoryViewHolder(private val binding: FragmentCardPredictionHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
//        fun bind(history: PredictionHistory, clickListener: ((PredictionHistory) -> Unit)?) {
//            val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
//
//            binding.tvHistoryDate.text = dateFormat.format(Date(history.date))
//            binding.tvFieldName.text = history.fieldName
//            binding.tvPredictionType.text = history.predictionType
//            binding.tvPredictedYield.text = "Prediksi: %.2f kg".format(history.predictedYield)
//
//            if (history.predictionType == "Kondisi" && history.conditionLabel != null) {
//                binding.tvCondition.text = "Kondisi: ${history.conditionLabel}"
//                binding.tvCondition.visibility = View.VISIBLE
//            } else {
//                binding.tvCondition.visibility = View.GONE
//            }
//
//            itemView.setOnClickListener { clickListener?.invoke(history) }
//        }
//    }
//}
//
//class HistoryDiffCallback : DiffUtil.ItemCallback<PredictionHistory>() {
//    override fun areItemsTheSame(oldItem: PredictionHistory, newItem: PredictionHistory): Boolean {
//        return oldItem.id == newItem.id
//    }
//
//    override fun areContentsTheSame(oldItem: PredictionHistory, newItem: PredictionHistory): Boolean {
//        return oldItem == newItem
//    }
//}