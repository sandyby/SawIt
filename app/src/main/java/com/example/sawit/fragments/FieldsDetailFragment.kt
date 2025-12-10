package com.example.sawit.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.sawit.R
import com.example.sawit.models.Field
import com.example.sawit.utils.FieldsTabView
import com.example.sawit.viewmodels.FieldViewModel
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FieldsDetailFragment : Fragment(R.layout.fragment_fields_detail) {
    private val fieldViewModel: FieldViewModel by viewModels()
    private var currentField: Field? = null
    private var fieldId: String? = null
    private lateinit var layoutDetails: View
    private lateinit var layoutStatistics: View

    companion object {
        private const val ARG_FIELD_ID = "fieldId"

        fun newInstance(fieldId: String?) = FieldsDetailFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_FIELD_ID, fieldId)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_fields_detail, container, false)

        layoutDetails = view.findViewById(R.id.layout_details)
        layoutStatistics = view.findViewById(R.id.layout_statistics)
        val ftvFields = view.findViewById<FieldsTabView>(R.id.ftv_fields)
        val ivBack = view.findViewById<ImageView>(R.id.iv_back)
        val tvFieldName = view.findViewById<TextView>(R.id.tv_field_name)

        showDetails()

        ftvFields.onTabSelectedListener = { index ->
            when (index) {
                0 -> showDetails()
                1 -> showStatistics()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            fieldViewModel.fieldsData.collectLatest { fields ->
                currentField = fields.find { it.fieldId == fieldId }
                currentField?.let { field ->
                    tvFieldName.text = field.fieldName
                }
            }
        }

        ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        return view
    }

    private fun showDetails() {
        layoutDetails.visibility = View.VISIBLE
        layoutStatistics.visibility = View.GONE
    }

    private fun showStatistics() {
        layoutDetails.visibility = View.GONE
        layoutStatistics.visibility = View.VISIBLE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val btnAddData = view.findViewById<MaterialButton>(R.id.mb_add_data_btn)
        val btnEditData = view.findViewById<MaterialButton>(R.id.mb_edit_data_btn)

        btnAddData.setOnClickListener {
            val addFieldDataFragment = AddFieldDataFragment()
            Log.d("FieldsDetailFragment", "tes")
            parentFragmentManager.beginTransaction()
                .replace(R.id.fl_scroll_view_content, addFieldDataFragment)
                .addToBackStack(null)
                .commit()
        }

        btnEditData.setOnClickListener {
            val editFieldDataFragment = EditFieldDataFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.cl_fragments_fields_detail, EditFieldDataFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}