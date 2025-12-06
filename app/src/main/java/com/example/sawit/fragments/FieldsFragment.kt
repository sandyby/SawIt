package com.example.sawit.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sawit.R
import com.example.sawit.activities.CreateFieldActivity
import com.example.sawit.adapters.FieldsFieldsAdapter
import com.example.sawit.databinding.FragmentFieldsBinding
import com.example.sawit.utils.VerticalSpaceItemDecoration
import com.example.sawit.viewmodels.FieldViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FieldsFragment : Fragment() {
    private var _binding: FragmentFieldsBinding? = null
    private val binding get() = _binding!!
    private val fieldViewModel: FieldViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFieldsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = FieldsFieldsAdapter(
            onClick = { field ->
                val action = FieldsDetailFragment.newInstance(field.fieldId)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fl_scroll_view_content, action)
                    .addToBackStack(null)
                    .commit()
            },
            onDeleteClick = { field ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Delete Field")
                    .setMessage("Are you sure you want to delete '${field.fieldName}'?")
                    .setPositiveButton("Delete") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setCancelable(true)
                    .show()
            }
        )

        binding.efabFields.setOnClickListener { _ ->
            val intent = Intent(requireContext(), CreateFieldActivity::class.java)
            startActivity(intent)
        }

        binding.rvFieldsFields.apply {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }

        val spacingInPx = resources.getDimensionPixelSize(R.dimen.vertical_item_spacing)
        binding.rvFieldsFields.addItemDecoration(VerticalSpaceItemDecoration(spacingInPx))

        viewLifecycleOwner.lifecycleScope.launch {
            fieldViewModel.fieldsData.collectLatest { fields ->
                adapter.submitList(fields)
            }
        }


    }
}