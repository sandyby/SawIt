package com.example.sawit.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sawit.R
import com.example.sawit.activities.CreateEditActivity
import com.example.sawit.adapters.ActivitiesAdapter
import com.example.sawit.databinding.FragmentActivitiesListBinding
import com.example.sawit.viewmodels.ActivityViewModel
import kotlinx.coroutines.launch

class PlannedActivitiesFragment : Fragment(R.layout.fragment_activities_list) {

    private var _binding: FragmentActivitiesListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ActivityViewModel by activityViewModels()
    private lateinit var adapter: ActivitiesAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentActivitiesListBinding.bind(view)

        binding.tvEmptyActivitiesTitle.text = "No Planned Activities Yet"
        binding.tvEmptyActivitiesMessage.text = "Tap the '+' button to schedule your first task"

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
                val intent = Intent(requireContext(), CreateEditActivity::class.java).apply {
                    putExtra(CreateEditActivity.EXTRA_ACTIVITY, activity)
                }
                startActivity(intent)
            },
            onDeleteClicked = { activity ->
                viewModel.deleteActivity(activity.id)
            }
        )

        //        adapter = ActivitiesAdapter(
//            onCheckboxClicked = { activity, isChecked ->
//                val status = if (isChecked) "completed" else "planned"
//                viewModel.updateActivityStatus(activity.id, status)
//            },
//            onEditClicked = { activity ->
//                val intent = Intent(requireContext(), CreateEditActivity::class.java).apply {
//                    putExtra(CreateEditActivity.EXTRA_ACTIVITY, activity)
//                }
//                startActivity(intent)
//            },
//            onDeleteClicked = { activity ->
//                viewModel.deleteActivity(activity.id)
//            }
//        )

        binding.rvActivities.apply {
            this.adapter = this@PlannedActivitiesFragment.adapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun observeActivities() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.activities.collect { allActivities ->
                    val planned = allActivities.filter { it.status == "planned" }
                    adapter.submitList(planned)

                    if (planned.isEmpty()) {
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