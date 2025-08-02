package com.mealmatch.ui.feed

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.ImageButton
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mealmatch.R
import com.mealmatch.data.model.Media

class ImageGalleryDialog(
    context: Context,
    private val mediaList: List<Media>
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_image_gallery)

        window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )

        setupRecyclerView()
        setupCloseButton()
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.galleryRecyclerView)
        val adapter = ImageGalleryAdapter(mediaList.toMutableList()) { media ->
            if (media.type == "video") {
                val videoDialog = VideoPlayerDialog(context, media.url)
                videoDialog.show()
            } else {
                val imageDialog = ImageViewerDialog(context, media.url)
                imageDialog.show()
            }
        }

        recyclerView.layoutManager = GridLayoutManager(context, 3)
        recyclerView.adapter = adapter
    }

    private fun setupCloseButton() {
        findViewById<ImageButton>(R.id.closeGalleryButton).setOnClickListener {
            dismiss()
        }
    }
}