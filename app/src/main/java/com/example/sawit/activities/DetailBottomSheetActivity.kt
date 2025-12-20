package com.example.sawit.activities

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import com.example.sawit.R
import com.example.sawit.models.Activity
import com.example.sawit.ui.ActivityCountdownTimer
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.Date

class DetailBottomSheetActivity(val activity: Activity) : BottomSheetDialogFragment() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.activity_detail_bottom_sheet, container, false)

        val tvTitle = view.findViewById<TextView>(R.id.tv_modal_title)
        val tvNotes = view.findViewById<TextView>(R.id.tv_modal_notes)
        val composeView = view.findViewById<ComposeView>(R.id.compose_timer_container)

        tvTitle.text = activity.activityType
        tvNotes.text = if (activity.notes.isBlank()) "No extra notes" else activity.notes

        composeView.setContent {
            if (activity.date.after(Date())) {
                ActivityCountdownTimer(activity.date)
            } else if (activity.date == Date()){
                Text(
                    text = "This activity is scheduled for today!"
                )
            } else {
                Text(
                    text = "This activity is already scheduled for today or completed."
                )
            }
        }

        return view
    }
}