package com.itant.music.play

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.TELEPHONY_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.text.TextUtils
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.ToastUtils
import com.itant.music.manager.PlayManager
import com.itant.music.manager.PlaySpeedManager
import com.miekir.common.context.MainContext
import com.miekir.common.log.L


class ActionHandler(private val mediaCenter: MediaCenter) {

    /**
     * 监听电话来的话就暂停
     */
    private val mPhoneListener: PhoneStateListener = object : PhoneStateListener() {
        var isPlaying = false

        override fun onCallStateChanged(state: Int, incomingNumber: String) {
            when (state) {
                TelephonyManager.CALL_STATE_RINGING -> {
                    isPlaying = PlayManager.isPlaying
                    if (isPlaying) {
                        // 暂停播放
                        execute(MusicService.KEY_ACTION_PAUSE)
                    }
                }
                TelephonyManager.CALL_STATE_OFFHOOK -> {}
                TelephonyManager.CALL_STATE_IDLE -> {
                    if (isPlaying) {
                        // 恢复播放
                        execute(MusicService.KEY_ACTION_PLAY_RESUME)
                    }
                }
                else -> {}
            }
        }
    }

    /**
     * 监听耳机拔出
     */
    private val noisyAudioStreamReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (TextUtils.equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY, intent.action)) {
                // 暂停播放
                execute(MusicService.KEY_ACTION_PAUSE)
            }
        }
    }

    init {
        // 电话来了暂停音乐
        val telephonyManager = MainContext.getContext().getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        telephonyManager.listen(mPhoneListener, PhoneStateListener.LISTEN_CALL_STATE)

        // 耳机拔了要暂停音乐
        val intentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        MainContext.getContext().registerReceiver(noisyAudioStreamReceiver, intentFilter)
    }

    /**
     * 执行点击通知栏后的动作
     */
    fun execute(action: String?) {
        if (PlayManager.currentSong == null &&
            !TextUtils.isEmpty(action) &&
            !TextUtils.equals(action, MusicService.KEY_ACTION_CLOSE)) {
            // 当前没有可以播放的歌曲，且点击了通知栏里的按钮(不是关闭按钮)
            ToastUtils.showShort("没有可播放的歌曲")
            return
        }

        when (action) {
            MusicService.KEY_ACTION_PREV -> {
                // 上一曲
                PlayManager.startPlayPrev()
            }

            MusicService.KEY_ACTION_PLAY_NEW -> {
                // 新的播放
                PlayManager.isPlaying = true
                //mediaCenter.mediaController.transportControls.play()
                mediaCenter.onMusicPlay()
            }

            MusicService.KEY_ACTION_PLAY_RESUME -> {
                // 恢复播放
                PlayManager.isPlaying = true
                mediaCenter.onMusicResume()
            }

            MusicService.KEY_ACTION_PAUSE -> {
                // 暂停
                PlayManager.isPlaying = false
                //mediaCenter.mediaController.transportControls.pause()
                mediaCenter.onMusicPause()
            }

            MusicService.KEY_ACTION_INIT_PAUSE -> {
                // 初始化暂停
                PlayManager.isPlaying = false
                mediaCenter.initAndPause()
            }

            MusicService.KEY_ACTION_NEXT -> {
                // 下一曲
                PlayManager.startPlayNext(false)
            }

            MusicService.KEY_ACTION_CHANGE_SPEED -> {
                // 改变播放速度
                //mediaCenter.mediaController.transportControls.setPlaybackSpeed(PlaySpeedManager.playSpeed)
                try {
                    mediaCenter.mediaPlayer.playbackParams = mediaCenter.mediaPlayer.playbackParams
                        .setSpeed(PlaySpeedManager.playSpeed)
                } catch (e: Exception) {
                    L.e(e.message)
                }
            }

            MusicService.KEY_ACTION_CLOSE -> {
                // 停止播放，退出应用
                val context = MainContext.getContext()
                context.stopService(Intent(context, MusicService::class.java))
                AppUtils.exitApp()
            }
        }
    }
}