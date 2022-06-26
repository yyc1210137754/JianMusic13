package com.itant.music.play

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import com.itant.music.manager.PlayManager


class MusicService : Service() {
    companion object {
        // 整型 ID 不得为 0
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "J_Music_Id"
        const val SESSION_TAG = "J_Music_Session"
        const val KEY_ACTION_CLOSE = "-1"
        const val KEY_ACTION_PREV = "0"
        const val KEY_ACTION_PLAY_NEW = "1"
        const val KEY_ACTION_PLAY_RESUME = "2"
        const val KEY_ACTION_PAUSE = "3"
        const val KEY_ACTION_NEXT = "4"
        const val KEY_ACTION_CHANGE_SPEED = "5"
        const val KEY_ACTION_INIT_PAUSE = "6"
    }

    private lateinit var mediaCenter: MediaCenter
    private lateinit var musicNotice: MusicNotice

    override fun onCreate() {
        super.onCreate()
        musicNotice = MusicNotice()
        mediaCenter = MediaCenter(this)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            ActionHandler(mediaCenter).execute(intent.action)
        }
        startForegroundNotification()
        return START_STICKY
    }

    /**
     * 开始展示播放通知
     */
    fun startForegroundNotification() {
        val song = PlayManager.currentSong
        if (song != null) {
            val metaData = MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.title)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.artist)
                //.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, -1L)// 隐藏进度条
            mediaCenter.mediaSession.setMetadata(metaData.build())
        }

        startForeground(
            NOTIFICATION_ID,
            musicNotice.getMusicNotification(applicationContext, mediaCenter.mediaSession)
        )
    }
}