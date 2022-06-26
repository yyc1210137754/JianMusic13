package com.itant.music.main.artist

import com.itant.music.utils.PinyinUtils
import com.miekir.common.widget.loading.LoadingType
import com.miekir.mvp.presenter.BasePresenter
import com.miekir.task.extension.launchTask
import java.util.*
import kotlin.collections.ArrayList


class ArtistPresenter : BasePresenter<ArtistFragment>() {

    /**
     * 歌手排序
     */
    fun sortArtist() {
        launchTask(
            {
                val rawArtistList = ArtistManager.artistSongMap.keys().toList()
                // 按拼音排序
                Collections.sort(rawArtistList, kotlin.Comparator { name1, name2 ->
                    PinyinUtils.ccs2Pinyin(name1)
                        .compareTo(PinyinUtils.ccs2Pinyin(name2), ignoreCase = true)
                })
                val artistList = ArrayList<ArtistBean>()
                for (name in rawArtistList) {
                    artistList.add(
                        ArtistBean(
                            firstName = PinyinUtils.getPinyinFirstLetter(name).toUpperCase(),
                            lastName = name
                        )
                    )
                }
                artistList
            }, onResult = { _, result, _, _ ->
                view?.onArtistSorted(result)
            }, loadingType = LoadingType.INVISIBLE
        )
    }
}