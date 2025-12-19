package com.example.sawit.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sawit.R

class ActivitiesFooterAdapter :
    RecyclerView.Adapter<ActivitiesFooterAdapter.ActivitiesFooterViewHolder>() {
    class ActivitiesFooterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivitiesFooterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activities_list_footer, parent, false)
        return ActivitiesFooterViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ActivitiesFooterViewHolder,
        position: Int
    ) {
        //
    }

    override fun getItemCount(): Int {
        return 1
    }
}