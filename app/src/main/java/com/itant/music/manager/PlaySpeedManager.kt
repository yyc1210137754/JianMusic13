package com.itant.music.manager


object PlaySpeedManager {
    /**
     * 播放速度
     */
    private const val KEY_PLAY_SPEED = "key_play_speed"

    private const val SPEED_0_5     = 0.5f
    private const val SPEED_0_75    = 0.75f
    private const val SPEED_1_0     = 1.0f
    private const val SPEED_1_25    = 1.25f
    private const val SPEED_1_75    = 1.75f
    private const val SPEED_2_0     = 2.0f

    val SPEED_ARRAY   = arrayListOf<Float>(SPEED_0_5, SPEED_0_75, SPEED_1_0, SPEED_1_25, SPEED_1_75, SPEED_2_0)

    /**
     * 播放速度
     */
    var playSpeed: Float = SPEED_1_0
        get() {
            return StoreManager.keyValue.decodeFloat(KEY_PLAY_SPEED, SPEED_1_0)
        }
        set(value) {
            StoreManager.keyValue.encode(KEY_PLAY_SPEED, value)
            field = value
        }

    /**
     * 获取播放速度对应的字符串
     */
    fun getPlaySpeed(speed: Float = playSpeed): String {
        return when(speed) {
            SPEED_0_5   -> "0.5X"
            SPEED_0_75  -> "0.75X"
            SPEED_1_0   -> "1.0X"
            SPEED_1_25  -> "1.25X"
            SPEED_1_75  -> "1.75X"
            SPEED_2_0   -> "2.0X"
            else        -> "1.0X"
        }
    }
}