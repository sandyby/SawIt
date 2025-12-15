package com.example.sawit.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sawit.R
import com.example.sawit.models.ActivityStatus
import com.example.sawit.models.ActivityTimelineItem
import com.github.vipulasri.timelineview.TimelineView
import com.google.android.material.card.MaterialCardView

class ActivitiesTimelineAdapter(
    private val onClick: (ActivityTimelineItem) -> Unit
) : ListAdapter<ActivityTimelineItem, ActivitiesTimelineAdapter.ViewHolder>(ActivityDiffCallback()) {
    inner class ViewHolder(view: View, viewType: Int) : RecyclerView.ViewHolder(view) {
        val timelineView: TimelineView = view.findViewById(R.id.timeline_view)
        val tvActivityDate: TextView = view.findViewById(R.id.tv_activity_date)
        val tvFieldName: TextView = view.findViewById(R.id.tv_activity_timeline_field_name)
        val tvActivityTitle: TextView = view.findViewById(R.id.tv_activity_timeline_activity_name)
        val mcvActivityCard: MaterialCardView = view.findViewById(R.id.mcv_activity_card)
        val ibViewDetails: ImageButton = view.findViewById(R.id.iv_activity_view_more_btn)

        init {
            timelineView.initLine(viewType)
        }

        fun bind(item: ActivityTimelineItem, position: Int) {
            tvActivityDate.text = item.date
            tvFieldName.text = item.fieldName
            tvActivityTitle.text = item.activityTitle

            val context = itemView.context

            when (item.status) {
                ActivityStatus.UPCOMING -> {
                    timelineView.marker =
                        ContextCompat.getDrawable(context, R.drawable.timeline_marker_ring_upcoming)
                    mcvActivityCard.setCardBackgroundColor(
                        ContextCompat.getColor(
                            context,
                            R.color.text_primary_900
                        )
                    )
                }

                ActivityStatus.TODAY -> {
                    timelineView.marker =
                        ContextCompat.getDrawable(context, R.drawable.timeline_marker_ring_today)
                    timelineView.setLineStyle(TimelineView.LineStyle.DASHED)
                    mcvActivityCard.setCardBackgroundColor(
                        ContextCompat.getColor(
                            context,
                            R.color.bg_primary_500
                        )
                    )
                }

                ActivityStatus.COMPLETED -> {
                    timelineView.marker = ContextCompat.getDrawable(
                        context,
                        R.drawable.timeline_marker_ring_completed
                    )
                    // Customize card color for COMPLETED
                    mcvActivityCard.setCardBackgroundColor(
                        ContextCompat.getColor(
                            context,
                            R.color.text_600
                        )
                    )
                }
            }

            itemView.setOnClickListener { onClick(item) }
        }
    }

    class ActivityDiffCallback : DiffUtil.ItemCallback<ActivityTimelineItem>() {
        override fun areItemsTheSame(
            oldItem: ActivityTimelineItem,
            newItem: ActivityTimelineItem
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: ActivityTimelineItem,
            newItem: ActivityTimelineItem
        ): Boolean {
            return oldItem == newItem
        }
    }

    override fun getItemViewType(position: Int): Int {
        return TimelineView.getTimeLineViewType(position, itemCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_timeline_item, parent, false)
        return ViewHolder(view, viewType)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }
}