package com.itant.music.main

import android.database.Cursor
import android.provider.MediaStore
import android.text.TextUtils
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.itant.music.constant.CurrentListType
import com.itant.music.main.artist.ArtistManager
import com.itant.music.main.setting.SettingManager
import com.itant.music.main.song.SongBean
import com.itant.music.main.song.SongManager
import com.itant.music.main.star.StarManager
import com.itant.music.manager.PlayManager
import com.itant.music.manager.StoreManager
import com.itant.music.utils.PinyinUtils
import com.miekir.common.context.MainContext
import com.miekir.common.log.L
import com.miekir.common.widget.loading.LoadingType
import com.miekir.mvp.presenter.BasePresenter
import com.miekir.task.extension.launchTask
import java.util.*
import kotlin.collections.ArrayList



class MainMusicPresenter: BasePresenter<MainMusicActivity>() {
    private var isFirstSearch = true
    /**
     * 处理歌曲
     */
    fun sortSongs(cursor: Cursor) {
        launchTask({
            // 清空旧数据
            StarManager.starSongList.clear()
            ArtistManager.artistSongMap.clear()
            PlayManager.currentSongList.clear()
            StarManager.starSongList.clear()

            // 收藏列表
            val starSongString = StoreManager.keyValue.decodeString(StarManager.KEY_STAR_LIST)
            if (!TextUtils.isEmpty(starSongString)) {
                try {
                    val starSongList = Gson().fromJson<ArrayList<SongBean>>(
                        starSongString,
                        object : TypeToken<ArrayList<SongBean>>(){}.type
                    )
                    StarManager.starSongList.addAll(starSongList)
                } catch (e: Exception) {
                    L.e(e.message)
                }
            }

            val allSongList: MutableList<SongBean> = ArrayList()
            val songIdList = ArrayList<Long>()
            while (cursor.moveToNext()) {
                val song = SongBean()
                // 时长
                song.duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
                if (song.duration < SettingManager.durationFilter) {
                    // 时长不符合的不要加入
                    continue
                }
                song.id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID))
                songIdList.add(song.id)
                // 歌名
                song.title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                // 专辑
                song.album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))
                // 歌手
                song.artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                if (TextUtils.isEmpty(song.artist)) {
                    song.artist = "未知"
                }
                // 格式
                song.format = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE))
                if (song.format.contains("/")) {
                    song.format = song.format.split("/")[1]
                }
                song.fileSize = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE))
                allSongList.add(song)

                // 歌手和对应的歌曲
                artistCollectSong(song)
            }

            // 按拼音排序
            Collections.sort(allSongList, kotlin.Comparator { song1, song2 ->
                PinyinUtils.ccs2Pinyin(song1.title)
                    .compareTo(PinyinUtils.ccs2Pinyin(song2.title), ignoreCase = true)
            })
            // 所有歌曲
            SongManager.allSongList.run {
                clear()
                addAll(allSongList)
            }

            // 恢复正在播放
            if (songIdList.contains(PlayManager.currentSongId)) {
                restoreCurrentSong()
            }

            // 保存最新收藏列表，对比被删除了的并从我的收藏中删除
            for (song in StarManager.starSongList) {
                if (!songIdList.contains(song.id)) {
                    StarManager.starSongList.remove(song)
                }
            }
            StoreManager.keyValue.encode(StarManager.KEY_STAR_LIST, Gson().toJson(StarManager.starSongList))

            // 根据播放模式恢复当前列表
            restoreCurrentList()
        }, onResult = {_, _, _, _ ->
            view?.onSortFinished()
        }, loadingType = LoadingType.INVISIBLE, loadingMessage = "main")
    }

    /**
     * 根据播放模式恢复当前列表
     */
    private fun restoreCurrentList() {
        when (StoreManager.keyValue.decodeInt(PlayManager.KEY_CURRENT_LIST_TYPE, CurrentListType.MAIN)) {
            CurrentListType.MAIN -> {
                // 上次播放的是主列表
                PlayManager.currentSongList.addAll(SongManager.allSongList)
            }

            CurrentListType.ARTIST -> {
                // 上次播放的是歌手列表下某个歌手的歌曲
                val key = StoreManager.keyValue.decodeString(ArtistManager.KEY_CURRENT_ARTIST, "")
                val currentList = ArtistManager.artistSongMap[key]
                if (currentList != null) {
                    // 按拼音排序
                    Collections.sort(currentList, kotlin.Comparator { song1, song2 ->
                        PinyinUtils.ccs2Pinyin(song1.title)
                            .compareTo(PinyinUtils.ccs2Pinyin(song2.title), ignoreCase = true)
                    })
                    PlayManager.currentSongList.addAll(currentList)
                }
            }

            CurrentListType.STAR -> {
                // 上次播放的是收藏列表中的
                PlayManager.currentSongList.addAll(StarManager.starSongList)
            }
        }
    }

    /**
     * 歌手拥有多首歌曲
     */
    private fun artistCollectSong(song: SongBean) {
        var artistSongList  = ArtistManager.artistSongMap[song.artist!!]
        if (artistSongList == null) {
            artistSongList = ArrayList<SongBean>()
        }
        artistSongList.add(song)
        ArtistManager.artistSongMap[song.artist!!] = artistSongList
    }

    /**
     * 第一次启动，之前有正在播放的歌曲，恢复到通知栏
     */
    private fun restoreCurrentSong() {
        if (!isFirstSearch) {
            return
        }
        isFirstSearch = false
        if (PlayManager.currentSong != null) {
            PlayManager.initPause()
        }
    }

    /**
     * 歌曲搜索条件
     */
    fun getMusicLoader() : Loader<Cursor> {
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.SIZE
        )
        // MediaStore.Audio.Media.BITRATE
        val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"
        return CursorLoader(
            MainContext.getContext(),
            uri,
            projection,
            selection,
            null,
            null
        )
    }
}