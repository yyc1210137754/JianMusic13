package com.itant.music.main.artist

import com.itant.music.main.song.SongBean
import java.util.concurrent.ConcurrentHashMap


object ArtistManager {
    /**
     * 当前列表的歌手，只有点击了某个歌手下的所有歌曲中的一首时才生效
     */
    const val KEY_CURRENT_ARTIST = "key_current_artist"

    /**
     * 歌手与对应的歌曲
     */
    val artistSongMap = ConcurrentHashMap<String, ArrayList<SongBean>>()

}