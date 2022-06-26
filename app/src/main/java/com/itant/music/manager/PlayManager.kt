package com.itant.music.manager

import android.content.Intent
import android.media.MediaPlayer
import com.blankj.utilcode.util.ToastUtils
import com.itant.music.constant.CurrentListType
import com.itant.music.listener.PlayListener
import com.itant.music.main.song.SongBean
import com.itant.music.main.song.SongManager
import com.itant.music.play.MusicService
import com.miekir.common.context.MainContext
import java.util.concurrent.CopyOnWriteArrayList


object PlayManager {
    /**
     * 当前播放的歌曲的ID
     */
    private const val KEY_CURRENT_SONG_ID = "key_current_song_id"

    /**
     * 当前列表应该使用主列表，某个歌手的列表，还是我的收藏列表，参考：[com.itant.music.bean.CurrentListType]
     */
    const val KEY_CURRENT_LIST_TYPE = "key_current_list_type"

    /**
     * 当前选中的歌曲ID
     */
    var currentSongId: Long = -1L
    get() {
        return StoreManager.keyValue.decodeLong(KEY_CURRENT_SONG_ID, -1L)
    }
    set(value) {
        // 保存当前正在播放的歌曲ID，下次进入恢复
        StoreManager.keyValue.encode(KEY_CURRENT_SONG_ID, value)
        field = value
    }

    /**
     * 当前列表类型
     */
    var currentListType: Int = CurrentListType.MAIN
    get() {
        return StoreManager.keyValue.decodeInt(KEY_CURRENT_LIST_TYPE,
            CurrentListType.MAIN)
    }
    set(value) {
        StoreManager.keyValue.encode(KEY_CURRENT_LIST_TYPE, value)
        field = value
    }

    /**
     * 当前选中的歌曲
     */
    val currentSong: SongBean?
        get() {
        if (currentSongId == -1L || SongManager.allSongList.isEmpty()) {
            return null
        } else {
            for (song in SongManager.allSongList) {
                if (song.id == currentSongId) {
                    return song
                }
            }
            return null
        }
    }

    /**
     * 是否正在播放歌曲
     */
    var isPlaying = false
    set(value) {
        field = value
        for (listener in playListener) {
            listener.onPlayStatusChange()
        }
    }

    /**
     * 当前歌曲列表，用于确定上一曲、下一曲、循环播放
     */
    var currentSongList = CopyOnWriteArrayList<SongBean>()

    /**
     * 音乐切换、内容变动（我的收藏）监听者
     */
    var playListener: ArrayList<PlayListener> = ArrayList<PlayListener>()

    /**
     * 切歌了，或者收藏变动（仅针对我的收藏）
     */
    fun onSongSwitch() {
        for (listener in playListener) {
            listener.onSongSwitch()
        }
    }

    /**
     * 播放上一曲
     */
    fun startPlayPrev() {
        val tempSong = currentSong
        if (currentSongList.isEmpty() || tempSong == null) {
            ToastUtils.showShort("没有上一曲了")
            return
        }

        when (PlayModeManager.playMode) {
            PlayModeManager.ALL_ONCE, PlayModeManager.ALL_CIRCLE -> {
                // 播放一次和循环播放
                var targetIndex = 0
                for ((index, song) in currentSongList.withIndex()) {
                    if (song.id == tempSong.id) {
                        targetIndex = index-1
                        break
                    }
                }
                if (targetIndex < 0) {
                    targetIndex = currentSongList.size -1
                }
                currentSongId = currentSongList[targetIndex].id
                startPlayNew()
            }
            PlayModeManager.ONE_ONCE, PlayModeManager.ONE_CIRCLE -> {
                // 单曲一次和单曲循环
                startPlayNew()
            }
            PlayModeManager.RANDOM -> {
                // 随机播放
                currentSongId = currentSongList.random().id
                startPlayNew()
            }
        }
        onSongSwitch()
    }

    /**
     * 播放下一曲
     * @param isAuto 是否自动播放下一曲
     */
    fun startPlayNext(isAuto: Boolean) {
        val tempSong = currentSong
        if (currentSongList.isEmpty() || tempSong == null) {
            ToastUtils.showShort("没有下一曲了")
            return
        }

        if (isAuto && PlayModeManager.playMode == PlayModeManager.ONE_ONCE) {
            // 单曲一次没有下一曲
            pausePlay()
            return
        }

        when (PlayModeManager.playMode) {
            PlayModeManager.ONE_ONCE, PlayModeManager.ALL_ONCE, PlayModeManager.ALL_CIRCLE -> {
                // 单曲一次手动点击下一首，全部一次和循环播放
                var targetIndex = 0
                for ((index, song) in currentSongList.withIndex()) {
                    if (song.id == tempSong.id) {
                        targetIndex = index+1
                        break
                    }
                }
                if (targetIndex == currentSongList.size) {
                    // 自动全部一次到底了的话，不用继续了
                    if (isAuto && PlayModeManager.playMode == PlayModeManager.ALL_ONCE) {
                        pausePlay()
                        return
                    }
                    targetIndex = 0
                }
                currentSongId = currentSongList[targetIndex].id
                startPlayNew()
            }
            PlayModeManager.ONE_CIRCLE -> {
                // 单曲循环
                startPlayNew()
            }
            PlayModeManager.RANDOM -> {
                // 随机播放
                currentSongId = currentSongList.random().id
                startPlayNew()
            }
        }
        onSongSwitch()
    }

    /**
     * 初始化上次选中的歌曲
     */
    fun initPause() {
        play(MusicService.KEY_ACTION_INIT_PAUSE)
    }

    /**
     * 从头开始播放歌曲
     */
    fun startPlayNew() {
        play(MusicService.KEY_ACTION_PLAY_NEW)
    }

    /**
     * 暂停播放歌曲
     */
    fun pausePlay() {
        play(MusicService.KEY_ACTION_PAUSE)
    }

    /**
     * 恢复播放歌曲
     */
    fun resumePlay() {
        play(MusicService.KEY_ACTION_PLAY_RESUME)
    }

    /**
     * 改变播放速度
     */
    fun changePlaySpeed() {
        play(MusicService.KEY_ACTION_CHANGE_SPEED)
    }

    /**
     * 暂停或者恢复
     */
    fun togglePlay() {
        if (currentSong == null) {
            ToastUtils.showShort("没有正在播放的歌曲")
            return
        }
        if (isPlaying) {
            ToastUtils.showShort("暂停播放")
            pausePlay()
        } else {
            ToastUtils.showShort("恢复播放")
            resumePlay()
        }
    }

    private fun play(action : String) {
        val context = MainContext.getContext()
        val intent = Intent(context, MusicService::class.java)
        intent.action = action
        context.startService(intent)
    }

    /**
     * 多媒体播放器，用来获取当前播放进度
     */
    var mediaPlayer: MediaPlayer? = null
}