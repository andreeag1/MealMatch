package com.mealmatch.ui.feed

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.mealmatch.R
import com.mealmatch.data.model.Media
import com.bumptech.glide.Glide

class ImageGalleryAdapter(
    private val mediaList: MutableList<Media>,
    private val onItemClick: (Media) -> Unit
) : RecyclerView.Adapter<ImageGalleryAdapter.GalleryViewHolder>() {

    class GalleryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.galleryImageView)
        val playButton: ImageView = view.findViewById(R.id.galleryPlayButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_gallery_image, parent, false)
        return GalleryViewHolder(view)
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        val media = mediaList[position]

        Glide.with(holder.imageView.context)
            .load(media.url)
            .into(holder.imageView)

        holder.itemView.setOnClickListener {
            onItemClick(media)
        }

        if (media.type == "video") {
            holder.playButton.visibility = View.VISIBLE
        } else {
            holder.playButton.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = mediaList.size
}