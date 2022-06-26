package com.itant.music.bean

import com.itant.music.main.song.SongBean


data class PlayBean(
    // 当前正在播放的歌曲
    var songBean: SongBean? = null,
    // 播放速度：0.5、 0.75、 1.0、 1.25、 1.5、 2.0
    var speed: Float = 1.0f,
    // 歌曲总数
    var songNum: Int = 0,
    // 播放模式：顺序播放一次（0），随机播放（1），单曲循环（2），全部循环（3）
    var playMode: Int = 0,
    // 当前歌曲播放进度
    var currentTime: Long = 0
)
