package com.itant.music

import android.app.Application
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.ToastUtils
import com.itant.music.net.ApiManager
import com.miekir.common.log.L
import com.miekir.task.TaskManager
import com.tencent.bugly.Bugly
import com.tencent.mmkv.MMKV



class MusicApp: Application() {
    override fun onCreate() {
        super.onCreate()
        TaskManager.getInstance().baseUrl(ApiManager.BASE_URL)
        Bugly.init(this, "3c2e7830bc", true)
        // 设置Toast风格
        ToastUtils.getDefaultMaker()
            .setBgColor(ContextCompat.getColor(this, R.color.green_ka))
            .setTextColor(ContextCompat.getColor(this, R.color.white))
        // 初始化存储
        val rootDir = MMKV.initialize(this)
        L.i("mmkv root: $rootDir")
    }
}