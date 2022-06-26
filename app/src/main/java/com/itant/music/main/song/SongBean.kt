package com.itant.music.main.song

data class SongBean(
    var id: Long = -1,
    // 歌名
    var title: String? = "未知",
    var album: String? = "未知",
    // 总时长，单位：秒
    var duration: Long = 0,
    // 大小，单位：字节
    var fileSize: Long = 0,
    // 歌词
    var lyric: String = "",
    // 歌手
    var artist: String? = "未知",
    // 是否已收藏
    var isStarred: Boolean = false,
    // 格式，ape，flac要显示
    var format: String = ""
)
