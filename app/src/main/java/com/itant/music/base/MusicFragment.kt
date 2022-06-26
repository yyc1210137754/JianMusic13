package com.itant.music.base

import androidx.viewbinding.ViewBinding
import com.miekir.mvp.view.BindingFragment

abstract class MusicFragment<VB : ViewBinding> : BindingFragment<VB>() {
    protected var hasInit = false
    /**
     * 获取标题
     */
    abstract fun getTitle(): String

    /**
     * 从主界面同步过来的歌曲变更，需要更新列表
     */
    abstract fun onSongListUpdate()

    open fun onTabReselected() {}

    override fun onLazyInit() {
        hasInit = true
    }
}