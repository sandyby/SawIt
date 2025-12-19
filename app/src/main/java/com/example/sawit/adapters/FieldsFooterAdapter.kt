package com.example.sawit.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sawit.R

class FieldsFooterAdapter :
    RecyclerView.Adapter<FieldsFooterAdapter.FieldsFooterViewHolder>() {
    class FieldsFooterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FieldsFooterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_fields_list_footer, parent, false)
        return FieldsFooterViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: FieldsFooterViewHolder,
        position: Int
    ) {
        //
    }

    override fun getItemCount(): Int {
        return 1
    }
}