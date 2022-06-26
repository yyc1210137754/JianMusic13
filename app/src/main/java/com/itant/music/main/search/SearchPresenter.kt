package com.itant.music.main.search

import android.text.TextUtils
import com.itant.music.main.song.SongBean
import com.itant.music.main.song.SongManager
import com.miekir.mvp.presenter.BasePresenter
import com.miekir.task.extension.launchTask

class SearchPresenter: BasePresenter<SearchActivity>() {
    /**
     * 根据关键词搜索
     */
    fun searchLocalSong(keyWords: String?) {
        if (TextUtils.isEmpty(keyWords)) {
            view?.onSearchResult(ArrayList<SongBean>())
            return
        }
        launchTask(
            {
                val songList = ArrayList<SongBean>()
                var title:String? = ""
                var artist:String? = ""
                for (song in SongManager.allSongList) {
                    title = song.title
                    artist = song.artist
                    if ((title != null && title.contains(keyWords!!)) || (artist != null && artist.contains(keyWords!!))) {
                        songList.add(song)
                    }
                }
                songList
            }, onResult = { success, result, code, message ->
                view?.onSearchResult(result)
            }
        )
    }
}