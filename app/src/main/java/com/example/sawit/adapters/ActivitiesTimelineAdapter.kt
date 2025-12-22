package com.example.sawit.adapters

import android.graphics.Color
import android.util.Log
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
    companion object {
        const val VIEW_TYPE_START = 100
        const val VIEW_TYPE_MIDDLE = 101
        const val VIEW_TYPE_END = 102
        const val VIEW_TYPE_ONLY_ONE = 103
    }

    inner class ViewHolder(view: View, viewType: Int) : RecyclerView.ViewHolder(view) {
        private val positionViewType = viewType
        val timelineView: TimelineView = view.findViewById(R.id.timeline_view)
        val tvActivityDate: TextView = view.findViewById(R.id.tv_activity_date)
        val tvFieldName: TextView = view.findViewById(R.id.tv_activity_timeline_field_name)
        val tvActivityTitle: TextView = view.findViewById(R.id.tv_activity_timeline_activity_name)
        val mcvActivityCard: MaterialCardView = view.findViewById(R.id.mcv_activity_card)
        val ibViewDetails: ImageButton = view.findViewById(R.id.iv_activity_view_more_btn)
        fun bind(item: ActivityTimelineItem) {
            tvActivityDate.text = item.date
            tvFieldName.text = item.fieldName
            tvActivityTitle.text = item.activityTitle
            val context = itemView.context
            val upcomingColor = ContextCompat.getColor(context, R.color.bg_primary_400)
            val todayColor = ContextCompat.getColor(context, R.color.bg_primary_500)
            val completedColor = ContextCompat.getColor(context, R.color.text_primary_900)
            val transparentColor = Color.TRANSPARENT

            var desiredStartLineColor = upcomingColor
            var desiredEndLineColor = upcomingColor

            when (item.status) {
                ActivityStatus.UPCOMING -> {
                    timelineView.marker =
                        ContextCompat.getDrawable(context, R.drawable.timeline_marker_ring_upcoming)
                    timelineView.setLineStyle(TimelineView.LineStyle.DASHED)
                    mcvActivityCard.setCardBackgroundColor(
                        ContextCompat.getColor(
                            context,
                            R.color.bg_primary_400
                        )
                    )
                }

                ActivityStatus.TODAY -> {
                    timelineView.marker =
                        ContextCompat.getDrawable(context, R.drawable.timeline_marker_ring_today)
                    desiredStartLineColor = upcomingColor
                    timelineView.setStartLineStyle(TimelineView.LineStyle.DASHED)
                    desiredEndLineColor = todayColor
                    timelineView.setEndLineStyle(TimelineView.LineStyle.NORMAL)
                    mcvActivityCard.setCardBackgroundColor(
                        ContextCompat.getColor(
                            context,
                            R.color.text_primary_500
                        )
                    )
                }

                ActivityStatus.COMPLETED -> {
                    timelineView.marker = ContextCompat.getDrawable(
                        context,
                        R.drawable.timeline_marker_ring_completed
                    )
                    timelineView.setLineStyle(TimelineView.LineStyle.NORMAL)
                    desiredStartLineColor = completedColor
                    desiredEndLineColor = completedColor
                    mcvActivityCard.setCardBackgroundColor(
                        ContextCompat.getColor(
                            context,
                            R.color.text_600
                        )
                    )
                }

                ActivityStatus.OVERDUE -> null
            }

            timelineView.setStartLineColor(desiredStartLineColor, desiredStartLineColor)
            timelineView.setEndLineColor(desiredEndLineColor, desiredEndLineColor)
            timelineView.setLineWidth(2)

            Log.d("ActivitiesTimelineAdapter", "bind: $positionViewType")

            when (positionViewType) {
                VIEW_TYPE_START -> {
                    timelineView.setStartLineColor(transparentColor, transparentColor)
                }

                VIEW_TYPE_END -> {
                    timelineView.setEndLineColor(transparentColor, transparentColor)
                }

                VIEW_TYPE_ONLY_ONE -> {
                    timelineView.setStartLineColor(transparentColor, transparentColor)
                    timelineView.setEndLineColor(transparentColor, transparentColor)
                }

                VIEW_TYPE_MIDDLE -> {
                    //
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
        val itemCount = currentList.size
        Log.d("ActivitiesTimelineAdapter", "itemCount: $itemCount")
        return when {
            itemCount == 1 -> VIEW_TYPE_ONLY_ONE
            position == 0 -> VIEW_TYPE_START
            position == itemCount - 1 -> VIEW_TYPE_END
            else -> VIEW_TYPE_MIDDLE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_timeline_item, parent, false)
        return ViewHolder(view, viewType)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}