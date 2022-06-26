package com.itant.music.widget

import androidx.recyclerview.widget.RecyclerView
import com.itant.music.base.MusicFragment
import com.itant.music.main.MainMusicActivity

/**
 * RecyclerView滚动监听，修复Activity的SwipeRefreshLayout包裹Fragment的RecyclerView，
 * RecyclerView还没滚动到顶部下滑却触发SwipeRefreshLayout刷新（往左下滑动可复现）的问题
 */
open class FixedScrollListener(private val fragment: MusicFragment<*>) : RecyclerView.OnScrollListener() {

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        val topPosition = if (recyclerView.childCount == 0) 0 else recyclerView.getChildAt(0).top
        (fragment.activity as MainMusicActivity).getSwipeLayout().isEnabled = topPosition >= 0
        if (dx > 0) {
            // 向右滚
        } else if (dx < 0) {
            // 向左滚
        } else {
            // 不是水平滚
        }

        if (dy > 0) {
            // 向下滚
        } else if (dy < 0) {
            // 向上滚
        } else {
            // 不是垂直滚
        }
    }
}