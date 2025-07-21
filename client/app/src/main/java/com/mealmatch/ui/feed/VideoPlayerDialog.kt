package com.mealmatch.ui.feed

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.ImageButton
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.mealmatch.R

class VideoPlayerDialog(
    context: Context,
    private val videoUrl: String
) : Dialog(context) {

    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.item_video_player)

        window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )

        setupPlayer()
        setupCloseButton()
    }

    private fun setupPlayer() {
        val playerView = findViewById<PlayerView>(R.id.playerView)

        player = ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(videoUrl)
            setMediaItem(mediaItem)
            prepare()
            play()
        }

        playerView.player = player
    }

    private fun setupCloseButton() {
        findViewById<ImageButton>(R.id.closeButton).setOnClickListener {
            dismiss()
        }
    }

    override fun dismiss() {
        player?.release()
        player = null
        super.dismiss()
    }
}