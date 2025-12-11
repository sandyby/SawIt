// File: com/example/sawit/adapters/PredictionHistoryAdapter.kt
package com.example.sawit.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sawit.models.Prediction
// VVVV KOREKSI UTAMA: Menggunakan nama binding baru VVVV
import com.example.sawit.databinding.FragmentCardPredictionHistoryBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PredictionHistoryAdapter : ListAdapter<Prediction, PredictionHistoryAdapter.HistoryViewHolder>(HistoryDiffCallback()) {

    var onItemClicked: ((Prediction) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        // Menggunakan kelas binding yang baru
        val binding = FragmentCardPredictionHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClicked)
    }

    // Menggunakan kelas binding yang baru di ViewHolder
    class HistoryViewHolder(private val binding: FragmentCardPredictionHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(history: Prediction, clickListener: ((Prediction) -> Unit)?) {
            val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

            binding.tvHistoryDate.text = dateFormat.format(Date(history.date))
            binding.tvFieldName.text = history.fieldName
            binding.tvPredictionType.text = history.predictionType
            binding.tvPredictedYield.text = "Prediksi: %.2f kg".format(history.predictedYield)

            if (history.predictionType == "Kondisi" && history.conditionLabel != null) {
                binding.tvCondition.text = "Kondisi: ${history.conditionLabel}"
                binding.tvCondition.visibility = View.VISIBLE
            } else {
                binding.tvCondition.visibility = View.GONE
            }

            itemView.setOnClickListener { clickListener?.invoke(history) }
        }
    }
}

class HistoryDiffCallback : DiffUtil.ItemCallback<Prediction>() {
    override fun areItemsTheSame(oldItem: Prediction, newItem: Prediction): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Prediction, newItem: Prediction): Boolean {
        return oldItem == newItem
    }
}