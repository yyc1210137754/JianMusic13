package com.itant.music.manager


object PlayModeManager {
    /**
     * 播放模式
     */
    private const val KEY_PLAY_MODE = "key_play_mode"

    /**
     * 顺序一次
     */
    const val ALL_ONCE = 0

    /**
     * 单曲一次
     */
    const val ONE_ONCE = 1

    /**
     * 全部循环
     */
    const val ALL_CIRCLE = 2

    /**
     * 单曲循环
     */
    const val ONE_CIRCLE = 3

    /**
     * 随机播放
     */
    const val RANDOM = 4

    /**
     * 播放模式
     */
    var playMode: Int = ALL_ONCE
        get() {
            return StoreManager.keyValue.decodeInt(KEY_PLAY_MODE, ALL_ONCE)
        }
        set(value) {
            StoreManager.keyValue.encode(KEY_PLAY_MODE, value)
            field = value
        }

    /**
     * 切换播放模式
     */
    fun switchPlayMode() {
        var mode = playMode + 1
        if (mode > RANDOM) {
            mode = ALL_ONCE
        }
        playMode = mode
    }

    fun getPlayModeString(): String {
        return when (playMode) {
            ALL_ONCE    -> "顺序一次"
            ONE_ONCE    -> "单曲一次"
            ALL_CIRCLE  -> "全部循环"
            ONE_CIRCLE  -> "单曲循环"
            RANDOM      -> "随机播放"
            else        -> "未知模式"
        }
    }
}