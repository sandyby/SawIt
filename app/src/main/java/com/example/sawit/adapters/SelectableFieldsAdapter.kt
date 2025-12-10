package com.example.sawit.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter

class SelectableFieldsAdapter(
    context: Context,
    private val resource: Int,
    private val objects: List<String>,
    private var selectedFieldName: String?
): ArrayAdapter<String>(context, resource, objects) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        val fieldName = objects[position]
        view.isActivated = fieldName == selectedFieldName
        return view
    }

    fun setSelected(fieldName: String){
        selectedFieldName = fieldName
        notifyDataSetChanged()
    }
}