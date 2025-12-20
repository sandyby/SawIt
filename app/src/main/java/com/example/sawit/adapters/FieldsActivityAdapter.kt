package com.example.sawit.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sawit.R
import com.example.sawit.databinding.ItemActivitySmallCardBinding
import com.example.sawit.models.Activity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FieldsActivityAdapter : ListAdapter<Activity, FieldsActivityAdapter.ViewHolder>(
    DiffCallback
) {
    inner class ViewHolder(private val binding: ItemActivitySmallCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(activity: Activity) {
            binding.tvActivityTitle.text = activity.activityType
            binding.tvActivityDate.text =
                SimpleDateFormat("dd MMM", Locale.getDefault()).format(activity.date)
            val iconRes = getIconForActivity(activity.activityType)
            binding.ivActivityIcon.setImageResource(iconRes)
            binding.ivActivityIcon.setColorFilter(
                ContextCompat.getColor(itemView.context, R.color.bg_primary_500)
            )
        }

        private fun getIconForActivity(type: String?): Int {
            return when (type?.lowercase(Locale.ROOT)) {
                "harvest" -> R.drawable.ic_filled_sickle_24_black
                "fertilizing" -> R.drawable.ic_filled_fertilizer_24_black
                "watering" -> R.drawable.ic_filled_watering_can_24_black
                "inspection" -> R.drawable.ic_filled_magnifying_glass_24_black
                "pruning" -> R.drawable.ic_filled_pruning_shears_24_black
                "pest control" -> R.drawable.ic_filled_pest_control_24_black
                else -> R.drawable.ic_filled_question_mark_24_black
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemActivitySmallCardBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Activity>() {
        override fun areItemsTheSame(oldItem: Activity, newItem: Activity) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Activity, newItem: Activity) = oldItem == newItem
    }
}