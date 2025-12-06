package com.example.sawit.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sawit.R
import com.example.sawit.adapters.FieldsDashboardAdapter
import com.example.sawit.databinding.FragmentHomeBinding
import com.example.sawit.ui.NotificationIconWithBadge
import com.example.sawit.utils.HorizontalSpaceItemDecoration
import com.example.sawit.viewmodels.FieldViewModel
import com.example.sawit.viewmodels.NotificationViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val fieldViewModel: FieldViewModel by viewModels()
    private lateinit var fullName: String
    private val notificationViewModel: NotificationViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        val composeView = binding.cvNotification
        composeView.setContent {
            val notificationCount by notificationViewModel.notificationCount.observeAsState(0)

//            var notificationCount by remember { mutableIntStateOf(0) }

            NotificationIconWithBadge(
                count = notificationCount,
                onClick = {
                    notificationViewModel.increment()
                    Log.d("HomeFragment", "Notification icon clicked — count = $notificationCount")
                }
            )
        }

        val sharedPrefs = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        fullName = sharedPrefs.getString("fullName", "User") ?: "User"

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = FieldsDashboardAdapter { field ->
            Toast.makeText(context, "clicked: ${field.fieldName}", Toast.LENGTH_SHORT).show()
        }

        binding.tvFullName.text = fullName

        binding.rvFieldsOverview.apply {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
        val spacingInPx = resources.getDimensionPixelSize(R.dimen.horizontal_item_spacing)
        binding.rvFieldsOverview.addItemDecoration(HorizontalSpaceItemDecoration(spacingInPx))

        viewLifecycleOwner.lifecycleScope.launch {
            fieldViewModel.fieldsData.collectLatest { fields ->
                adapter.submitList(fields)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
