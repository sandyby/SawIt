package com.example.sawit.activities

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.work.WorkManager
import com.example.sawit.R
import com.example.sawit.databinding.ActivityDetailBottomSheetBinding
import com.example.sawit.databinding.FragmentPredictionConditionBinding
import com.example.sawit.models.Activity
import com.example.sawit.ui.ActivityCountdownTimer
import com.example.sawit.viewmodels.ActivityViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetailBottomSheetActivity(val activity: Activity) : BottomSheetDialogFragment() {
    private var _binding: ActivityDetailBottomSheetBinding? = null
    private val binding get() = _binding!!
    private val activityViewModel: ActivityViewModel by activityViewModels()
    private val workManager by lazy { WorkManager.getInstance(requireContext()) }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityDetailBottomSheetBinding.inflate(inflater, container, false)

        binding.tvModalTitle.text = activity.activityType
        binding.tvDetailFieldName.text = activity.fieldName
        binding.tvModalNotes.text = activity.notes.ifBlank { "No additional notes provided" }

        val fullDateFormatter = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
        binding.tvDetailDate.text = fullDateFormatter.format(activity.date)

        setupStatusBadge(binding.tvStatusBadge, activity.status)

        binding.composeTimerContainer.setContent {
            val now = Date()
            when {
                activity.date.after(now) && activity.status.equals(
                    "planned",
                    ignoreCase = true
                ) -> {
                    ActivityCountdownTimer(activity.date)
                }

                isSameDay(activity.date, now) && activity.status.equals(
                    "planned",
                    ignoreCase = true
                ) -> {
                    TimerStatusMessage(
                        message = "Task is due today!",
                        color = colorResource(R.color.text_primary_500),
                        icon = R.drawable.ic_outline_clock_marked_48_black
                    )
                }

                else -> {
                    val isCompleted = activity.status.equals("completed", ignoreCase = true)

                    val message =
                        if (isCompleted) "Activity Completed" else "Activity timeframe has passed"
                    val color =
                        if (isCompleted) colorResource(R.color.bg_primary_500) else colorResource(R.color.bg_primary_overlay_2)

                    val icon = if (isCompleted) {
                        R.drawable.ic_filled_task_list_48_black
                    } else {
                        R.drawable.ic_outline_exclamation_clipboard_48_black
                    }

                    TimerStatusMessage(message, color, icon)
                }
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val isPlanned = activity.status.equals("planned", ignoreCase = true)
        val isOverdue = activity.status.equals("overdue", ignoreCase = true)

        binding.layoutActions.visibility = if (isPlanned || isOverdue) View.VISIBLE else View.GONE

        if (isOverdue) {
            binding.btnReschedule.text = "RESCHEDULE NOW"
            binding.btnReschedule.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.text_primary_900))
        }

        if (isPlanned || isOverdue) {
            binding.btnCompleteTask.setOnClickListener {
                val activityId = activity.id ?: return@setOnClickListener
                activityViewModel.updateActivityStatus(activityId, "completed")

                workManager.cancelUniqueWork("demo_$activityId")
                workManager.cancelUniqueWork("reminder_$activityId")

                Toast.makeText(context, "Activity completed!", Toast.LENGTH_SHORT).show()
                dismiss()
            }

            binding.btnReschedule.setOnClickListener {
                val intent = android.content.Intent(requireContext(), CreateEditActivityActivity::class.java).apply {
                    putExtra(CreateEditActivityActivity.EXTRA_ACTIVITY, activity)
                }
                startActivity(intent)
                dismiss()
            }
        }
    }

    private fun setupStatusBadge(view: TextView, status: String) {
        view.text = status.uppercase()

        when (status.lowercase()) {
            "planned" -> view.setBackgroundResource(R.drawable.badge_upcoming)
            "completed" -> view.setBackgroundResource(R.drawable.badge_completed)
            else -> view.setBackgroundResource(R.drawable.badge_overdue)
        }
    }

    @Composable
    fun TimerStatusMessage(message: String, color: Color, icon: Int) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    painter = androidx.compose.ui.res.painterResource(id = icon),
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = message,
                    color = color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        }
    }

    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val fmt = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return fmt.format(date1) == fmt.format(date2)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}