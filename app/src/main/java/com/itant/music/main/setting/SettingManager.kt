package com.itant.music.main.setting

import com.itant.music.manager.DurationManager
import com.itant.music.manager.StoreManager

object SettingManager {
    /**
     * 时长过滤
     */
    var durationFilter: Long = DurationManager.NO_LIMIT
        get() {
            return StoreManager.keyValue.decodeLong(DurationManager.KEY_DURATION_FILTER, DurationManager.NO_LIMIT)
        }
        set(value) {
            StoreManager.keyValue.encode(DurationManager.KEY_DURATION_FILTER, value)
            field = value
        }
}