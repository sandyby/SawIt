package com.example.sawit.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.example.sawit.R
import com.example.sawit.activities.CreateEditFieldActivity
import com.example.sawit.adapters.FieldsFieldsAdapter
import com.example.sawit.adapters.FieldsPagerAdapter
import com.example.sawit.databinding.FragmentFieldsDetailBinding
import com.example.sawit.models.Field
import com.example.sawit.viewmodels.FieldViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FieldsDetailFragment : Fragment(R.layout.fragment_fields_detail) {
    private var _binding: FragmentFieldsDetailBinding? = null
    private val binding get() = _binding!!
        private val fieldViewModel: FieldViewModel by activityViewModels()
    private var currentField: Field? = null
    private lateinit var fieldsAdapter: FieldsFieldsAdapter
    private var fieldId: String? = null
    private var fieldName: String? = null

    companion object {
        private const val ARG_FIELD_ID = "fieldId"
        private const val ARG_FIELD_NAME = "fieldName"

        fun newInstance(fieldId: String?, fieldName: String?) = FieldsDetailFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_FIELD_ID, fieldId)
                putString(ARG_FIELD_NAME, fieldName)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fieldId = arguments?.getString(ARG_FIELD_ID)
        fieldName = arguments?.getString(ARG_FIELD_NAME)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFieldsDetailBinding.bind(view)

        fieldId = arguments?.getString("fieldId") ?: return
        fieldName = arguments?.getString("fieldName") ?: return

        setupNavigation()
        setupDeleteBtn()
        setupViewPager(fieldId!!)
        observeFieldHeader(fieldId!!)
        observeLoadingState()
    }

    private fun setupDeleteBtn() {
        binding.btnDeleteField.setOnClickListener {
            val fieldToDelete = currentField ?: return@setOnClickListener
            val dialog = MaterialAlertDialogBuilder(
                requireContext(),
                R.style.DeleteDialogTheme
            )
                .setTitle("Delete Field")
                .setMessage("Are you sure you want to delete '${fieldToDelete.fieldName}'?")
                .setPositiveButton("Delete") { dialog, _ ->
                    fieldViewModel.deleteField(fieldToDelete, requireContext())
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
    }

    private fun setupViewPager(id: String) {
        val fieldsPagerAdapter = FieldsPagerAdapter(this, id, fieldName!!)
        binding.vpFieldDetail.adapter = fieldsPagerAdapter

        TabLayoutMediator(binding.tlFieldsDetails, binding.vpFieldDetail) { tab, position ->
            tab.text = if (position == 0) "Details" else "Logs"
        }.attach()
    }

    private fun setupNavigation() {
        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnEditField.setOnClickListener {
            val fieldToEdit = currentField ?: return@setOnClickListener
            val intent = Intent(requireContext(), CreateEditFieldActivity::class.java).apply {
                putExtra(CreateEditFieldActivity.EXTRA_FIELD, fieldToEdit)
            }
            startActivity(intent)
        }
    }

    private fun observeLoadingState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                fieldViewModel.isLoading.collect { isLoading ->
                    binding.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun observeFieldHeader(id: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                fieldViewModel.fieldsData.collectLatest { fields ->
                    val field = fields.find { it.fieldId == id }
                    if (field != null){
                        currentField = field
                        binding.tvDetailName.text = field.fieldName

                        field.fieldPhotoPath?.let { path ->
                            Glide.with(this@FieldsDetailFragment)
                                .load(path)
                                .placeholder(R.drawable.placeholder_200x100)
                                .into(binding.ivDetailPhoto)
                        }
                    } else {
                        parentFragmentManager.popBackStack()
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