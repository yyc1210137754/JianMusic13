package com.itant.music.listener

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.recyclerview.widget.RecyclerView
import com.itant.music.main.song.SongFragment
import com.itant.music.widget.FixedScrollListener

/**
 * RecyclerView滚动监听，修复Activity的SwipeRefreshLayout包裹Fragment的RecyclerView，
 * RecyclerView还没滚动到顶部下滑却触发SwipeRefreshLayout刷新（往左下滑动可复现）的问题
 */
class SongScrollListener(private val fragment: SongFragment) : FixedScrollListener(fragment) {
    companion object {
        const val WHAT_SHOW = 1
        const val WHAT_HIDE = 2
    }
    private val mHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                WHAT_SHOW -> mIndexView.visibility = VISIBLE
                WHAT_HIDE -> mIndexView.visibility = GONE
            }
        }
    }
    private val mIndexView = fragment.binding.ivSong

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
        when (newState) {
            RecyclerView.SCROLL_STATE_IDLE -> run {
                // 没有滚动
                hideIndex()
            }
            RecyclerView.SCROLL_STATE_DRAGGING -> run {
                // 正在滚动
                showIndex()
            }
            RecyclerView.SCROLL_STATE_SETTLING -> {
                // 滑动后自然沉降的状态
            }
        }
    }

    private fun showIndex() {
        mHandler.removeMessages(WHAT_HIDE)
        if (mHandler.hasMessages(WHAT_SHOW) || mIndexView.visibility == VISIBLE) {
            return
        }
        mHandler.sendEmptyMessageDelayed(WHAT_SHOW, 500)
    }

    fun hideIndex() {
        mIndexView.visibility = VISIBLE
        mHandler.removeMessages(WHAT_SHOW)
        mHandler.removeMessages(WHAT_HIDE)
        mHandler.sendEmptyMessageDelayed(WHAT_HIDE, 3500)
    }
}