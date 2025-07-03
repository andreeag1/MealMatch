package com.mealmatch.ui.map

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mealmatch.R
import com.mealmatch.data.model.Restaurant

class RestaurantAdapter
    : ListAdapter<Restaurant, RestaurantAdapter.ViewHolder>(DIFF_CALLBACK) {

    private var fullList = listOf<Restaurant>()

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Restaurant>() {
            override fun areItemsTheSame(a: Restaurant, b: Restaurant) = a.name == b.name
            override fun areContentsTheSame(a: Restaurant, b: Restaurant) = a == b
        }
    }

    fun updateData(list: List<Restaurant>) {
        fullList = list
        super.submitList(list)
    }

    fun filterByName(query: String) {
        val filtered = fullList.filter {
            it.name.contains(query, ignoreCase = true)
        }
        super.submitList(filtered)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_restaurant, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val nameTv: TextView = v.findViewById(R.id.tvName)
        private val typeTv: TextView = v.findViewById(R.id.tvType)
        private val ratingTv: TextView = v.findViewById(R.id.tvRating)
        private val distTv: TextView = v.findViewById(R.id.tvDistance)

        fun bind(r: Restaurant) {
            nameTv.text = r.name
            typeTv.text = r.type
            ratingTv.text = r.rating.toString()
            distTv.text = "${r.distance} km"
        }
    }
}
