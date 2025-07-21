package com.mealmatch.ui.feed

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mealmatch.R
import com.mealmatch.data.model.Post
import android.widget.ImageButton
import androidx.recyclerview.widget.GridLayoutManager

class PostAdapter(
    private val postList: List<Post>,
    private val currentUsername: String,
    private val onDeleteClick: (Post) -> Unit
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val username: TextView = view.findViewById(R.id.text_user)
        val caption: TextView = view.findViewById(R.id.text_caption)
        val ratingBar: RatingBar = view.findViewById(R.id.rating_bar)
        val postMediaRecyclerView: RecyclerView = view.findViewById(R.id.postMediaRecyclerView)
        val deleteButton: ImageButton = view.findViewById(R.id.button_delete)
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

        if (post.media.isNotEmpty()) {
            holder.postMediaRecyclerView.visibility = View.VISIBLE
            val spanCount = if (post.media.size == 1) 1 else 3
            val mediaAdapter = MediaAdapter(
                post.media.toMutableList(),
                {},
                false,
                R.layout.item_media_in_feed
            )

            holder.postMediaRecyclerView.layoutManager = GridLayoutManager(holder.itemView.context, spanCount)
            holder.postMediaRecyclerView.adapter = mediaAdapter

        } else {
            holder.postMediaRecyclerView.visibility = View.GONE
        }

        if (post.user?.username == currentUsername) {
            holder.deleteButton.visibility = View.VISIBLE
            holder.deleteButton.setOnClickListener {
                onDeleteClick(post)
            }
        } else {
            holder.deleteButton.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = postList.size
}