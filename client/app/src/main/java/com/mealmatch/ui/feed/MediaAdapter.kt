package com.mealmatch.ui.feed

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mealmatch.R
import com.mealmatch.data.model.Media
import com.bumptech.glide.Glide
import androidx.recyclerview.widget.GridLayoutManager

class MediaAdapter(
    private val mediaList: MutableList<Media>,
    private val onRemoveClick: (Int) -> Unit,
    private val isEditable: Boolean = true,
    private val itemLayoutRes: Int = R.layout.item_media
) : RecyclerView.Adapter<MediaAdapter.MediaViewHolder>() {

    class MediaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.mediaImageView)
        val removeButton: ImageButton = view.findViewById(R.id.removeMediaButton)
        val playButton: ImageView = view.findViewById(R.id.playButton)
        val overlayView: View? = view.findViewById(R.id.mediaOverlay)
        val overlayText: TextView? = view.findViewById(R.id.overlayText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(itemLayoutRes, parent, false)
        return MediaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val media = mediaList[position]
        val shouldShowOverlay = position == 2 && mediaList.size > 3

        val recyclerView = holder.itemView.parent as? RecyclerView
        recyclerView?.let {
            val layoutManager = it.layoutManager as? GridLayoutManager
            val spanCount = layoutManager?.spanCount ?: 3
            val spacing = it.context.resources.getDimensionPixelSize(R.dimen.media_item_spacing)
            val totalSpacing = spacing * (spanCount - 1)
            val parentWidth = it.width
            if (parentWidth > 0) {
                val itemWidth = (parentWidth - totalSpacing) / spanCount
                val params = holder.itemView.layoutParams
                params.width = itemWidth
                holder.itemView.layoutParams = params
            }
        }

        if (shouldShowOverlay) {
            holder.overlayView?.visibility = View.VISIBLE
            holder.overlayText?.visibility = View.VISIBLE
            holder.overlayText?.text = "+${mediaList.size - 3}"

            holder.overlayView?.setOnClickListener {
                showAllImagesDialog(holder.itemView.context, mediaList)
            }
            holder.overlayText?.setOnClickListener {
                showAllImagesDialog(holder.itemView.context, mediaList)
            }

        } else {
            holder.overlayView?.visibility = View.GONE
            holder.overlayText?.visibility = View.GONE
            holder.overlayView?.setOnClickListener(null)
            holder.overlayText?.setOnClickListener(null)
        }

        Glide.with(holder.imageView.context)
            .load(media.url)
            .into(holder.imageView)

        holder.imageView.setOnClickListener {
            if (shouldShowOverlay) {
                showAllImagesDialog(holder.itemView.context, mediaList)
            } else {
                showImageDialog(holder.itemView.context, media)
            }
        }

        holder.removeButton.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                onRemoveClick(pos)
            }
        }

        if (isEditable) {
            holder.removeButton.visibility = View.VISIBLE
        } else {
            holder.removeButton.visibility = View.GONE
        }

        if (media.type == "video") {
            holder.playButton.visibility = View.VISIBLE
            holder.playButton.setOnClickListener {
                val dialog = VideoPlayerDialog(holder.itemView.context, media.url)
                dialog.show()
            }
        } else {
            holder.playButton.visibility = View.GONE
        }
    }

    private fun showAllImagesDialog(context: android.content.Context, mediaList: List<Media>) {
        val dialog = ImageGalleryDialog(context, mediaList)
        dialog.show()
    }

    private fun showImageDialog(context: android.content.Context, media: Media) {
        if (media.type == "video") {
            val dialog = VideoPlayerDialog(context, media.url)
            dialog.show()
        } else {
            val dialog = ImageViewerDialog(context, media.url)
            dialog.show()
        }
    }

    override fun onViewRecycled(holder: MediaViewHolder) {
        super.onViewRecycled(holder)

        holder.overlayView?.setOnClickListener(null)
        holder.overlayText?.setOnClickListener(null)
        holder.imageView.setOnClickListener(null)
        holder.removeButton.setOnClickListener(null)
        holder.playButton.setOnClickListener(null)
    }

    override fun getItemCount(): Int = mediaList.size

}