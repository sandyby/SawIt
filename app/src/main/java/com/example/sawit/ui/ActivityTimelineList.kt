package com.example.sawit.ui.components

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sawit.R
import com.example.sawit.models.ActivityStatus
import com.example.sawit.models.ActivityTimelineItem
import com.pushpal.jetlime.EventPointType
import com.pushpal.jetlime.ItemsList
import com.pushpal.jetlime.JetLimeColumn
import com.pushpal.jetlime.JetLimeDefaults
import com.pushpal.jetlime.JetLimeEvent
import com.pushpal.jetlime.JetLimeEventDefaults
import com.pushpal.jetlime.PointPlacement
import com.pushpal.jetlime.VerticalAlignment

object TimelineColors {
    val Upcoming = R.color.bg_primary_400
    val Today = R.color.bg_primary_500
    val Completed = R.color.text_primary_900
    val Overdue = R.color.bg_secondary_overlay_2
    val CardUpcoming = R.color.bg_primary_overlay_2
    val CardToday = R.color.bg_primary_500
    val CardCompleted = R.color.text_primary_900
    val CardOverdue = R.color.bg_primary_500
}

@Composable
fun ActivityTimelineList(
    items: List<ActivityTimelineItem>,
    onItemClick: (ActivityTimelineItem) -> Unit
) {
    val backgroundColor = colorResource(id = R.color.bg_secondary_900)
    val primaryLineColor = colorResource(id = TimelineColors.Today)
    val completedLineColor = colorResource(id = TimelineColors.Completed)
    val upcomingLineColor = colorResource(id = TimelineColors.Upcoming)
    val overdueLineColor = colorResource(id = TimelineColors.Overdue)

    val jetLimeStyle = JetLimeDefaults.columnStyle(
        contentDistance = 4.dp,
        itemSpacing = 0.dp,
        lineBrush = JetLimeDefaults.lineSolidBrush(primaryLineColor),
        lineThickness = 2.dp,
        lineVerticalAlignment = VerticalAlignment.LEFT
    )

    JetLimeColumn(
        itemsList = ItemsList(items),
        key = { _, item -> item.id },
        style = jetLimeStyle,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 600.dp)
    ) { index, item, position ->
        val pointColor = when (item.status) {
            ActivityStatus.COMPLETED -> completedLineColor
            ActivityStatus.UPCOMING -> upcomingLineColor
            ActivityStatus.TODAY -> primaryLineColor
            ActivityStatus.OVERDUE -> overdueLineColor
            else -> primaryLineColor
        }

        val pointType = when (item.status) {
            ActivityStatus.COMPLETED -> EventPointType.custom(
                icon = painterResource(R.drawable.ic_filled_completed_marker_24_bg_primary_900),
                tint = colorResource(R.color.text_primary_900)
            )

            ActivityStatus.TODAY -> EventPointType.custom(
                icon = painterResource(R.drawable.ic_filled_today_marker_24_bg_primary_500),
                tint = colorResource(R.color.bg_primary_500)
            )

            ActivityStatus.UPCOMING -> EventPointType.custom(
                icon = painterResource(R.drawable.ic_filled_warning_24_grayed_1),
                tint = colorResource(R.color.bg_primary_overlay_2)
            )

            ActivityStatus.OVERDUE -> EventPointType.custom(
                icon = painterResource(R.drawable.ic_filled_question_marker_24_bg_primary_500),
                tint = colorResource(R.color.bg_primary_500)
            )

            else -> EventPointType.custom(
                icon = painterResource(R.drawable.ic_filled_upcoming_marker_24_secondary_overlay_2),
                tint = colorResource(R.color.bg_secondary_overlay_2)
            )
        }

        val pointAnimation = when (item.status) {
            ActivityStatus.COMPLETED -> null

            ActivityStatus.TODAY -> JetLimeEventDefaults.pointAnimation(
                1.0f,
                1.4f,
                infiniteRepeatable(
                    animation = tween(durationMillis = 800, easing = LinearOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )

            ActivityStatus.UPCOMING -> null

            else -> null
        }

        JetLimeEvent(
            style = JetLimeEventDefaults.eventStyle(
                pointColor = backgroundColor,
                pointRadius = 10.dp,
                pointType = pointType,
                pointStrokeWidth = 0.dp,
                pointAnimation = pointAnimation,
                position = position,
                pointPlacement = PointPlacement.CENTER,
            )
        ) {
            ActivityTimelineCard(item, onItemClick)
        }
    }
}

@Composable
private fun ActivityTimelineCard(
    item: ActivityTimelineItem,
    onItemClick: (ActivityTimelineItem) -> Unit
) {
    val cardColor = when (item.status) {
        ActivityStatus.UPCOMING -> colorResource(id = TimelineColors.CardUpcoming)
        ActivityStatus.TODAY -> colorResource(id = TimelineColors.CardToday)
        ActivityStatus.COMPLETED -> colorResource(id = TimelineColors.CardCompleted)
        ActivityStatus.OVERDUE -> colorResource(id = TimelineColors.CardOverdue)
    }

    val textColor = when (item.status) {
        ActivityStatus.UPCOMING -> colorResource(id = R.color.text_100)
        ActivityStatus.COMPLETED -> colorResource(id = R.color.white)
        ActivityStatus.TODAY -> colorResource(id = R.color.white)
        else -> colorResource(id = R.color.text_primary_900)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = item.date,
            modifier = Modifier
                .widthIn(max = 88.dp)
                .padding(8.dp, 0.dp, 0.dp, 0.dp),
            fontFamily = FontFamily(Font(R.font.lato_bold)),
            fontSize = 12.sp,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            color = colorResource(id = R.color.text_primary_900)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = cardColor),
            shape = MaterialTheme.shapes.medium,
            elevation = CardDefaults.cardElevation(3.dp),
            modifier = Modifier
                .weight(1f)
                .clickable { onItemClick(item) }
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.fieldName,
                        color = textColor.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        fontFamily = FontFamily(Font(R.font.lato_regular))
                    )
                    Text(
                        text = item.activityTitle,
                        color = textColor,
                        fontSize = 18.sp,
                        fontFamily = FontFamily(Font(R.font.lato_bold))
                    )
                }
                Icon(
                    painter = painterResource(id = R.drawable.ic_filled_chevron_right_36_white),
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}