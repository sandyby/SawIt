package com.example.sawit.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sawit.R
import com.example.sawit.models.Field
import com.example.sawit.utils.formatFieldArea
import com.example.sawit.utils.formatFieldPalmOilType
import com.example.sawit.utils.formatOilPalmAge
import java.io.File

class FieldsFieldsAdapter(
    private val onClick: (Field) -> Unit,
    private val onDeleteClick: (Field) -> Unit
) : ListAdapter<Field, FieldsFieldsAdapter.ViewHolder>(FieldDiffCallback()) {
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder
        (view) {
        val ivFieldPhoto: ImageView = view.findViewById(R.id.iv_fields_photo)
        val tvFieldName: TextView = view.findViewById(R.id.tv_fields_name)
        val tvFieldLocation: TextView = view.findViewById(R.id.tv_fields_location)
        val tvFieldAreaBadge: TextView = view.findViewById(R.id.tv_fields_area_badge)
        val tvFieldAgeBadge: TextView = view.findViewById(R.id.tv_fields_age_badge)
        val tvFieldPalmOilTypeBadge: TextView = view.findViewById(R.id.tv_fields_palm_oil_type)
        val tvFieldDesc: TextView = view.findViewById(R.id.tv_fields_description)
        val ibDeleteBtn: ImageButton = view.findViewById(R.id.ib_delete_field_btn)

        fun bind(field: Field) {
            tvFieldName.text = field.fieldName
            tvFieldLocation.text = field.fieldLocation.address
            tvFieldAreaBadge.text = field.fieldArea.formatFieldArea()
            tvFieldAgeBadge.text = field.avgOilPalmAgeInMonths.formatOilPalmAge()
            tvFieldPalmOilTypeBadge.text = field.oilPalmType.formatFieldPalmOilType()
            tvFieldDesc.text = field.fieldDesc
            if (field.fieldPhotoPath != null) {
                val imageFile = File(field.fieldPhotoPath)
                Log.d("FieldsFieldsAdapter", "imageFile: ${imageFile.absolutePath}")
                Glide.with(itemView.context).load(imageFile).override(800, 400).centerCrop()
                    .placeholder(R.drawable.placeholder_200x100)
                    .error(R.drawable.placeholder_200x100).into(ivFieldPhoto)
            } else {
                Glide.with(itemView.context).load(R.drawable.placeholder_200x100).into(ivFieldPhoto)
            }
            itemView.setOnClickListener { onClick(field) }
            ibDeleteBtn.setOnClickListener { onDeleteClick(field) }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fields_card_item_fields, parent, false)
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