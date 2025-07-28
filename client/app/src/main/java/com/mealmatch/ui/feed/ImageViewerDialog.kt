package com.mealmatch.ui.feed

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.mealmatch.R

class ImageViewerDialog(
    context: Context,
    private val imageUrl: String
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_image_viewer)

        window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )

        setupImageView()
        setupCloseButton()
    }

    private fun setupImageView() {
        val imageView = findViewById<ImageView>(R.id.fullScreenImageView)
        Glide.with(context)
            .load(imageUrl)
            .into(imageView)
    }

    private fun setupCloseButton() {
        findViewById<ImageButton>(R.id.closeImageViewerButton).setOnClickListener {
            dismiss()
        }
    }
}