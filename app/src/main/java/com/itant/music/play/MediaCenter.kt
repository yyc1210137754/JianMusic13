package com.itant.music.play

import android.content.ContentUris
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.RemoteException
import android.provider.MediaStore
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.blankj.utilcode.util.ToastUtils
import com.itant.music.manager.PlayManager
import com.itant.music.manager.PlaySpeedManager
import com.miekir.common.context.MainContext
import com.miekir.common.log.L
import java.io.IOException


class MediaCenter(private val musicService: MusicService): MediaSessionCompat.Callback(), MediaPlayer.OnPreparedListener,
    MediaPlayer.OnBufferingUpdateListener {

    // 媒体播放器
    var mediaPlayer: MediaPlayer = MediaPlayer()
    // 媒体控制器
    lateinit var mediaController: MediaControllerCompat
    var mediaSession:MediaSessionCompat

    init {
        val context = MainContext.getContext()
        //初始化MediaSessionCompat
        mediaSession = MediaSessionCompat(context, MusicService.SESSION_TAG)
        //设置播放控制回调
        mediaSession.setCallback(this)
        //设置可接受媒体控制
        mediaSession.isActive = true

        // 设置音频流类型
        mediaPlayer.setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )
        mediaPlayer.setOnPreparedListener(this)
        mediaPlayer.setOnBufferingUpdateListener(this)
        // 播放出错
        mediaPlayer.setOnErrorListener(MediaPlayer.OnErrorListener { mp, what, extra ->
            onPlayError()
            return@OnErrorListener false
        })
        // 播放完成
        mediaPlayer.setOnCompletionListener {
            PlayManager.startPlayNext(true)
        }

        // 初始化MediaController
        try {
            mediaController = MediaControllerCompat(context, mediaSession.sessionToken)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
        PlayManager.mediaPlayer = mediaPlayer
        // 设置音量
        // mediaPlayer.setVolume(streamVolume,streamVolume);
    }

    /**
     * 点击了播放，根据歌曲ID获取URI，播放新的歌曲
     */
    fun onMusicPlay() {
        val song = PlayManager.currentSong ?: return
        val uri: Uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, song.id)
        try {
            mediaPlayer.reset()
            //设置播放地址
            mediaPlayer.setDataSource(MainContext.getContext(), uri)
            //异步进行播放
            mediaPlayer.prepareAsync()
            showControlWhenLockScreen(PlaybackStateCompat.STATE_PLAYING)
            //如果是播放网络歌曲，则告诉MediaSession当前最新的音频状态为PlaybackStateCompat.STATE_CONNECTING
        } catch (e: IOException) {
            L.e(e.message)
            onPlayError()
        }
    }

    /**
     * 点击了暂停
     */
    fun onMusicPause() {
        mediaPlayer.pause()
        musicService.startForegroundNotification()
        showControlWhenLockScreen(PlaybackStateCompat.STATE_PAUSED)
    }

    /**
     * 点击继续播放
     */
    fun onMusicResume() {
        mediaPlayer.start()
        musicService.startForegroundNotification()
        showControlWhenLockScreen(PlaybackStateCompat.STATE_PLAYING)
        mediaPlayer.playbackParams = mediaPlayer.playbackParams.setSpeed(PlaySpeedManager.playSpeed)
    }

    /**
     * 点击三星锁屏的播放
     */
    override fun onPlay() {
        super.onPlay()
        PlayManager.resumePlay()
    }

    /**
     * 点击三星锁屏的暂停
     */
    override fun onPause() {
        super.onPause()
        // 点击锁屏的暂停
        PlayManager.pausePlay()
    }

    /**
     * 点击三星锁屏的下一曲
     */
    override fun onSkipToNext() {
        super.onSkipToNext()
        PlayManager.startPlayNext(false)
    }

    /**
     * 点击三星锁屏的上一曲
     */
    override fun onSkipToPrevious() {
        super.onSkipToPrevious()
        PlayManager.startPlayPrev()
    }

    /**
     * 解决三星锁屏不显示控制栏的问题，和mediaSession.setCallback的回调配合使用
     */
    private fun showControlWhenLockScreen(state : Int) {
        mediaSession.setPlaybackState(PlaybackStateCompat.Builder()
            .setState(state, 0, 1.0f)
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
            ).build()
        )
    }

    /**
     * 初始化，之前有正在播放的歌曲，恢复到通知栏，但是暂停播放
     */
    fun initAndPause() {
        // 点击了播放，根据歌曲ID获取URI
        val song = PlayManager.currentSong ?: return
        val uri: Uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, song.id)
        try {
            mediaPlayer.reset()
            //设置播放地址
            mediaPlayer.setDataSource(MainContext.getContext(), uri)
            //异步进行播放
            mediaPlayer.prepareAsync()
        } catch (e: IOException) {
            L.e(e.message)
            onPlayError()
        }
    }

    override fun onBufferingUpdate(mp: MediaPlayer, percent: Int) {
        // 缓冲进度
    }

    override fun onPrepared(mp: MediaPlayer?) {
        // 准备完成了才能设置播放速度，这个时候可以开始播放
        if (PlayManager.isPlaying) {
            mediaPlayer.playbackParams = mediaPlayer.playbackParams.setSpeed(PlaySpeedManager.playSpeed)
            mediaPlayer.start()
        }
        musicService.startForegroundNotification()
    }

    /**
     * 播放出错
     */
    private fun onPlayError() {
        showControlWhenLockScreen(PlaybackStateCompat.STATE_PAUSED)
        ToastUtils.showShort("播放失败")
        PlayManager.isPlaying = false
        musicService.startForegroundNotification()
    }
}