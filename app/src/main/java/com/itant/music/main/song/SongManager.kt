package com.itant.music.main.song

import java.util.concurrent.CopyOnWriteArrayList


object SongManager {
    /**
     * 本机所有歌曲
     */
    val allSongList = CopyOnWriteArrayList<SongBean>()
}