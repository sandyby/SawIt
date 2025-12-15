package com.example.sawit.adapters

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

    inner class AddFieldViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(isListEmpty: Boolean) {
            val params = itemView.layoutParams
            if (isListEmpty){
                params.width = ViewGroup.LayoutParams.MATCH_PARENT
            } else {
                val widthInPx = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    180f,
                    itemView.resources.displayMetrics
                ).toInt()
                params.width = widthInPx
            }
            itemView.layoutParams = params
            itemView.setOnClickListener { onAddClick() }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val count = currentList.size.coerceAtMost(2)
//        if (currentList.isEmpty()) return VIEW_TYPE_ADD
//        return if (position < currentList.size) VIEW_TYPE_FIELD else VIEW_TYPE_ADD
        return if (position < count) VIEW_TYPE_FIELD else VIEW_TYPE_ADD
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

//    override fun onCreateViewHolder(
//        parent: ViewGroup,
//        viewType: Int
//    ): RecyclerView.ViewHolder {
//        val view =
//            LayoutInflater.from(parent.context)
//                .inflate(R.layout.fields_card_item_dashboard, parent, false)
//        return ViewHolder(view)
//    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        if (holder is ViewHolder) {
            holder.bind(getItem(position))
        } else if (holder is AddFieldViewHolder) {
            holder.bind(currentList.isEmpty())
        }
    }

    override fun getItemCount(): Int {
        val count = currentList.size
        val fieldCardsToShow = count.coerceAtMost(2)
        val addCard = 1
//        if (count == 0) return 1
//        return if (count > 2) 2 else 2
        return fieldCardsToShow + addCard
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