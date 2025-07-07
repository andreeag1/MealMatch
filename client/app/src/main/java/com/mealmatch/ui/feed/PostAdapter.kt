package com.mealmatch.ui.feed

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mealmatch.R
import com.mealmatch.data.model.Post

class PostAdapter(private val postList: List<Post>) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val username: TextView = view.findViewById(R.id.text_user)
        val caption: TextView = view.findViewById(R.id.text_caption)
        val ratingBar: RatingBar = view.findViewById(R.id.rating_bar)
        val image: ImageView = view.findViewById(R.id.image_photo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_feed, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]
        holder.username.text = post.user?.username ?: "Unknown User"
        holder.caption.text = post.caption
        holder.ratingBar.rating = post.rating
        holder.image.visibility = View.GONE // for now
    }

    override fun getItemCount(): Int = postList.size
}