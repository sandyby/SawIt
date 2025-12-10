package com.example.sawit.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sawit.R
import com.example.sawit.models.Field
import java.io.File

class FieldsDashboardAdapter(
    private val onClick: (Field) -> Unit
//    private val fieldsData: ArrayList<Field>
) :
    ListAdapter<Field, FieldsDashboardAdapter.ViewHolder>(FieldDiffCallback()) {
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivFieldPhoto: ImageView = view.findViewById<ImageView>(R.id.iv_fields_photo)
        val tvFieldName: TextView = view.findViewById<TextView>(R.id.tv_fields_name)
        val tvFieldDesc: TextView = view.findViewById<TextView>(R.id.tv_fields_desc)
        val tvFieldLocation: TextView = view.findViewById<TextView>(R.id.tv_fields_location)

        fun bind(field: Field) {
            tvFieldName.text = field.fieldName
            tvFieldLocation.text = field.fieldLocation.address
            tvFieldDesc.text = field.fieldDesc
            if (field.fieldPhotoPath != null) {
                val imageFile = File(field.fieldPhotoPath)
                Glide.with(itemView.context).load(imageFile)
                    .placeholder(R.drawable.placeholder_200x100)
                    .error(R.drawable.placeholder_200x100)
                    .into(ivFieldPhoto)
            } else {
                Glide.with(itemView.context)
                    .load(R.drawable.placeholder_200x100)
                    .into(ivFieldPhoto)
            }
            itemView.setOnClickListener { onClick(field) }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.fields_card_item_dashboard, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }

    class FieldDiffCallback : DiffUtil.ItemCallback<Field>() {
        override fun areItemsTheSame(oldItem: Field, newItem: Field): Boolean {
            return oldItem.fieldId == newItem.fieldId
        }

        override fun areContentsTheSame(oldItem: Field, newItem: Field): Boolean {
            return oldItem == newItem
        }
    }
}