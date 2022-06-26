package com.itant.music.listener


interface PlayListener {
    /**
     * 歌曲切换，只需更新adapter即可
     */
    fun onSongSwitch()

    /**
     * 暂停或者恢复播放
     */
    fun onPlayStatusChange()
}