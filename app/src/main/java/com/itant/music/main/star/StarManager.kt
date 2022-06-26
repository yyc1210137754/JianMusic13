package com.itant.music.main.star

import com.blankj.utilcode.util.ToastUtils
import com.google.gson.Gson
import com.itant.music.main.song.SongBean
import com.itant.music.manager.PlayManager
import com.itant.music.manager.StoreManager
import java.util.concurrent.CopyOnWriteArraySet

/**
 * 我的收藏
 * @date 2021-8-1 20:38
 * @author 詹子聪
 */
object StarManager {
    const val KEY_STAR_LIST = "key_star_list"
    /**
     * 收藏的列表，不用排序
     */
    var starSongList = CopyOnWriteArraySet<SongBean>()

    /**
     * 更新收藏
     */
    fun updateStarList(isCurrentStar: Boolean) {
        val currentSongId = PlayManager.currentSong!!.id
        if (isCurrentStar) {
            // 添加收藏
            starSongList.add(PlayManager.currentSong)
            ToastUtils.showShort("收藏成功")
        } else {
            // 移除收藏
            for (song in starSongList) {
                if (currentSongId == song.id) {
                    starSongList.remove(song)
                    break
                }
            }
            ToastUtils.showShort("取消收藏")
        }
        StoreManager.keyValue.encode(KEY_STAR_LIST, Gson().toJson(starSongList))
    }

    /**
     * 当前歌曲是否被收藏
     */
    fun isCurrentSongStar(): Boolean {
        val currentSong = PlayManager.currentSong ?: return false
        for (song in starSongList) {
            if (song.id == currentSong.id) {
                return true
            }
        }
        return false
    }
}