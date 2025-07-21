package com.mealmatch.ui.feed

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.mealmatch.R
import com.mealmatch.data.model.Media
import com.bumptech.glide.Glide

class MediaAdapter(
    private val mediaList: MutableList<Media>,
    private val onRemoveClick: (Int) -> Unit
) : RecyclerView.Adapter<MediaAdapter.MediaViewHolder>() {

    class MediaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.mediaImageView)
        val removeButton: ImageButton = view.findViewById(R.id.removeMediaButton)
        val playButton: ImageView = view.findViewById(R.id.playButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_media, parent, false)
        return MediaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val media = mediaList[position]

        Glide.with(holder.imageView.context)
            .load(media.url)
            .into(holder.imageView)

        holder.removeButton.setOnClickListener {
            onRemoveClick(position)
        }

        if (media.type == "video") {
            holder.playButton.visibility = View.VISIBLE
        } else {
            holder.playButton.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = mediaList.size

}