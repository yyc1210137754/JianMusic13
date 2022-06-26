package com.itant.music.main.artist.song

import com.itant.music.main.artist.ArtistManager
import com.itant.music.manager.PlayManager
import com.itant.music.utils.PinyinUtils
import com.miekir.common.widget.loading.LoadingType
import com.miekir.mvp.presenter.BasePresenter
import com.miekir.task.extension.launchTask
import java.util.*


class ArtistSongPresenter : BasePresenter<ArtistSongActivity>() {

    fun sortArtistSong(artistName: String) {
        launchTask(
            {
                val currentList = ArtistManager.artistSongMap[artistName]
                PlayManager.currentSongList.clear()
                if (currentList != null) {
                    // 按拼音排序
                    Collections.sort(currentList, kotlin.Comparator { song1, song2 ->
                        PinyinUtils.ccs2Pinyin(song1.title)
                            .compareTo(PinyinUtils.ccs2Pinyin(song2.title), ignoreCase = true)
                    })
                    PlayManager.currentSongList.addAll(currentList)
                }

                PlayManager.currentSongList
            }, onResult = { success, result, code, message ->
                if (result != null) {
                    view?.onSongSorted(result)
                }
            }, loadingType = LoadingType.INVISIBLE
        )
    }
}