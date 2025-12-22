package com.example.sawit.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sawit.R
import com.example.sawit.activities.CreateEditActivityActivity
import com.example.sawit.adapters.ActivitiesAdapter
import com.example.sawit.adapters.ActivitiesFooterAdapter
import com.example.sawit.databinding.FragmentActivitiesListBinding
import com.example.sawit.viewmodels.ActivityViewModel
import com.example.sawit.viewmodels.FieldViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class CompletedActivitiesFragment : Fragment(R.layout.fragment_activities_list) {

    private var _binding: FragmentActivitiesListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ActivityViewModel by activityViewModels()
    private val fieldViewModel: FieldViewModel by activityViewModels()
    private lateinit var adapter: ActivitiesAdapter
    private lateinit var footerAdapter: ActivitiesFooterAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentActivitiesListBinding.bind(view)

        binding.tvEmptyActivitiesTitle.text = "No Completed Activities Yet"
        binding.tvEmptyActivitiesMessage.text = "Check your planned tasks to complete it"

        setupRecyclerView()
        observeActivities()
    }

    private fun setupRecyclerView() {
        adapter = ActivitiesAdapter(
            onCheckboxClicked = { activity, isChecked ->
                val newStatus = if (isChecked) "completed" else "planned"
                viewModel.updateActivityStatus(activity.id!!, newStatus)
            },
            onEditClicked = { activity ->
                val intent =
                    Intent(requireContext(), CreateEditActivityActivity::class.java).apply {
                        putExtra(CreateEditActivityActivity.EXTRA_ACTIVITY, activity)
                    }
                startActivity(intent)
            },
            onDeleteClicked = { activity ->
                val dialog = MaterialAlertDialogBuilder(
                    requireContext(),
                    R.style.DeleteDialogTheme
                )
                    .setTitle("Delete Field")
                    .setMessage("Are you sure you want to delete the completed task ${activity.activityType} on field ${activity.fieldName}?")
                    .setPositiveButton("Delete") { dialog, _ ->
                        viewModel.deleteActivity(activity.id)
                        dialog.dismiss()
                    }
                    .setBackground(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.dialog_background
                        )
                    )
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setCancelable(true)
                    .create()

                dialog.show()
                dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                    ?.setTextColor(resources.getColor(R.color.text_fiery_red_sunset_600, null))

                dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE)
                    ?.setTextColor(resources.getColor(R.color.text_500, null))
            }
        )

        footerAdapter = ActivitiesFooterAdapter()

        val concatAdapter = ConcatAdapter(adapter, footerAdapter)

        binding.rvActivities.apply {
            this.adapter = concatAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun observeActivities() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(
                    viewModel.activities,
                    fieldViewModel.fieldsData
                ) { currentActivities, currentFields ->
                    currentActivities.map { activity ->
                        val actualName =
                            currentFields.find { it.fieldId == activity.fieldId }?.fieldName
                        activity.copy(fieldName = actualName ?: activity.fieldName)
                    }
                }.collect { updatedActivities ->
                    val completed = updatedActivities.filter { it.status == "completed" }
                    adapter.submitList(completed)

                    if (completed.isEmpty()) {
                        binding.rvActivities.visibility = View.GONE
                        binding.clEmptyStateActivities.visibility = View.VISIBLE
                    } else {
                        binding.rvActivities.visibility = View.VISIBLE
                        binding.clEmptyStateActivities.visibility = View.GONE
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}