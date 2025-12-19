package com.example.sawit.adapters

import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sawit.databinding.ItemActivityBinding
import com.example.sawit.models.Activity
import java.text.SimpleDateFormat
import java.util.Locale

class ActivitiesAdapter(
    private val onCheckboxClicked: (Activity, Boolean) -> Unit,
    private val onEditClicked: (Activity) -> Unit,
    private val onDeleteClicked: (Activity) -> Unit
) : ListAdapter<Activity, ActivitiesAdapter.ActivityViewHolder>(ActivityDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val binding =
            ItemActivityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ActivityViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        val activity = getItem(position)
        holder.bind(activity)
    }

    inner class ActivityViewHolder(private val binding: ItemActivityBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(activity: Activity) {
            binding.textViewFieldName.text = activity.fieldName
            binding.textViewActivityType.text = activity.activityType
            binding.textViewNotes.text = activity.notes

            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.US)
            binding.textViewDate.text = sdf.format(activity.date)

            if (binding.textViewNotes.text.isNotEmpty()){
                binding.textViewNotes.visibility = View.VISIBLE
                binding.divider.visibility = View.VISIBLE
            } else {
                binding.textViewNotes.visibility = View.GONE
                binding.divider.visibility = View.GONE
            }

            binding.checkboxStatus.setOnCheckedChangeListener(null)
            binding.checkboxStatus.isChecked = activity.status == "completed"
            binding.checkboxStatus.setOnCheckedChangeListener { _, isChecked ->
                binding.checkboxStatus.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                onCheckboxClicked(activity, isChecked)
            }

            if (activity.status == "planned") {
                binding.buttonEdit.visibility = View.VISIBLE
            } else {
                binding.buttonEdit.visibility = View.GONE
            }

            binding.buttonEdit.setOnClickListener {
                onEditClicked(activity)
            }
            binding.buttonDelete.setOnClickListener {
                onDeleteClicked(activity)
            }
        }
    }

    class ActivityDiffCallback : DiffUtil.ItemCallback<Activity>() {
        override fun areItemsTheSame(oldItem: Activity, newItem: Activity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Activity, newItem: Activity): Boolean {
            return oldItem == newItem
        }
    }
}

