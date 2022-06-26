package com.itant.music.manager

import com.tencent.mmkv.MMKV


object StoreManager {
    /**
     * 本地数据存储
     */
    val keyValue: MMKV by lazy {  MMKV.defaultMMKV() }

    /**
     * 专门存歌词的
     */
    val lyricKeyValue: MMKV by lazy {  MMKV.mmkvWithID("lyric") }
}