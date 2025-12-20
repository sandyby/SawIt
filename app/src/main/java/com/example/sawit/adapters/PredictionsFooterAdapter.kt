package com.example.sawit.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sawit.R

class PredictionsFooterAdapter :
    RecyclerView.Adapter<PredictionsFooterAdapter.PredictionsFooterViewHolder>() {
    class PredictionsFooterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PredictionsFooterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_predictions_list_footer, parent, false)
        return PredictionsFooterViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: PredictionsFooterViewHolder,
        position: Int
    ) {
        //
    }

    override fun getItemCount(): Int {
        return 1
    }
}