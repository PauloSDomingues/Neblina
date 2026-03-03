package com.paulosd.neblina.audio

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.RawResourceDataSource
import androidx.media3.exoplayer.ExoPlayer


class AudioPlayerManager(
    private val context: Context
) {

    private var player: ExoPlayer? = null

    @OptIn(UnstableApi::class)
    fun start(soundResId: Int) {
        if (player == null) {
            player = ExoPlayer.Builder(context).build()
        }

        val mediaItem = MediaItem.fromUri(
            RawResourceDataSource.buildRawResourceUri(soundResId)
        )

        player?.apply {
            setMediaItem(mediaItem)
            repeatMode = Player.REPEAT_MODE_ONE
            prepare()
            play()
        }
    }

    fun stop() {
        player?.stop()
        player?.release()
        player = null
    }
}