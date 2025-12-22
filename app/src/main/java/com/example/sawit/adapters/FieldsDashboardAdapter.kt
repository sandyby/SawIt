package com.example.sawit.adapters

import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.getString
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sawit.R
import com.example.sawit.models.Field
import com.example.sawit.utils.ImageCacheManager
import com.example.sawit.utils.formatFieldArea
import com.example.sawit.utils.formatOilPalmAge
import java.io.File

class FieldsDashboardAdapter(
    private val onClick: (Field) -> Unit,
    private val onAddClick: () -> Unit
) :
    ListAdapter<Field, RecyclerView.ViewHolder>(FieldDiffCallback()) {
    companion object {
        private const val VIEW_TYPE_FIELD = 1
        private const val VIEW_TYPE_ADD = 2
        private val ADD_FIELD_ID = Field.ADD_PLACEHOLDER.fieldId
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivFieldPhoto: ImageView = view.findViewById<ImageView>(R.id.iv_fields_photo)
        val tvFieldName: TextView = view.findViewById<TextView>(R.id.tv_fields_name)
        val tvFieldDesc: TextView = view.findViewById<TextView>(R.id.tv_fields_description)
        val tvFieldLocation: TextView = view.findViewById<TextView>(R.id.tv_fields_location)
        val tvFieldAreaBadge: TextView = view.findViewById<TextView>(R.id.tv_fields_area_badge)
        val tvFieldAgeBadge: TextView = view.findViewById<TextView>(R.id.tv_fields_age_badge)

        fun bind(field: Field) {
            tvFieldName.text = field.fieldName
            tvFieldLocation.text = field.fieldLocation.address
            tvFieldAreaBadge.text = field.fieldArea.formatFieldArea()
            tvFieldAgeBadge.text = field.avgOilPalmAgeInMonths.formatOilPalmAge()
            tvFieldDesc.text = field.fieldDesc
            if (field.fieldPhotoPath != null && File(field.fieldPhotoPath!!).exists()) {
                Glide.with(itemView.context)
                    .load(File(field.fieldPhotoPath!!))
                    .override(800, 400)
                    .centerCrop()
                    .placeholder(R.drawable.placeholder_200x100)
                    .into(ivFieldPhoto)
            } else if (!field.fieldPhotoBase64.isNullOrEmpty()) {
                val newLocalPath = ImageCacheManager.base64ToLocalCache(itemView.context, field.fieldPhotoBase64)

                if (newLocalPath != null) {
                    field.fieldPhotoPath = newLocalPath

                    Glide.with(itemView.context)
                        .load(File(newLocalPath))
                        .override(800, 400)
                        .centerCrop()
                        .placeholder(R.drawable.placeholder_200x100)
                        .into(ivFieldPhoto)
                } else {
                    Glide.with(itemView.context).load(R.drawable.placeholder_200x100).into(ivFieldPhoto)
                }

            } else {
                Glide.with(itemView.context).load(R.drawable.placeholder_200x100).into(ivFieldPhoto)
            }
            itemView.setOnClickListener { onClick(field) }
        }
    }

    inner class AddFieldViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind() {
            val params = itemView.layoutParams
            val widthInPx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                180f,
                itemView.resources.displayMetrics
            ).toInt()

            params.width = widthInPx

            itemView.layoutParams = params
            itemView.setOnClickListener { onAddClick() }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return if (item.fieldId == ADD_FIELD_ID) VIEW_TYPE_ADD else VIEW_TYPE_FIELD
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_FIELD) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fields_card_item_dashboard, parent, false)
            ViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fields_card_add_item_dashboard, parent, false)
            AddFieldViewHolder(view)
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        val item = getItem(position)
        if (holder is ViewHolder) {
            holder.bind(item)
        } else if (holder is AddFieldViewHolder) {
            holder.bind()
        }
    }

    class FieldDiffCallback : DiffUtil.ItemCallback<Field>() {
        override fun areItemsTheSame(oldItem: Field, newItem: Field): Boolean {
            if (oldItem.fieldId == ADD_FIELD_ID || newItem.fieldId == ADD_FIELD_ID) {
                return oldItem.fieldId == newItem.fieldId
            }
            return oldItem.fieldId == newItem.fieldId
        }

        override fun areContentsTheSame(oldItem: Field, newItem: Field): Boolean {
            if (oldItem.fieldId == ADD_FIELD_ID && newItem.fieldId == ADD_FIELD_ID) {
                return true
            }
            return oldItem == newItem
        }
    }
}