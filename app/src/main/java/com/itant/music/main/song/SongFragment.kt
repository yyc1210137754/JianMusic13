package com.itant.music.main.song

import android.text.TextUtils
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.ToastUtils
import com.itant.music.R
import com.itant.music.base.MusicFragment
import com.itant.music.constant.CurrentListType
import com.itant.music.databinding.FragmentSongBinding
import com.itant.music.listener.PlayListener
import com.itant.music.listener.SongScrollListener
import com.itant.music.main.MainMusicActivity
import com.itant.music.manager.PlayManager
import com.itant.music.utils.PinyinUtils
import com.itant.music.widget.index.IIndexListener

/**
 * 展示所有歌曲的界面
 */
class SongFragment : MusicFragment<FragmentSongBinding>(), IIndexListener, PlayListener {
    private val songList = arrayListOf<SongBean>()
    private lateinit var mAdapter: SongAdapter
    private var scrollListener: SongScrollListener? = null

    companion object {
        const val MUSIC_ID = 0
    }

    override fun getTitle(): String {
        return "曲子"
    }

    override fun onBindingInflate(): FragmentSongBinding {
        return FragmentSongBinding.inflate(layoutInflater)
    }

    override fun onLazyInit() {
        mAdapter = SongAdapter(songList, CurrentListType.MAIN)
        binding.rvSong.run {
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }
        scrollListener = SongScrollListener(this)
        binding.rvSong.addOnScrollListener(scrollListener!!)
        // 设置空View之前必须先绑定RecyclerView
        mAdapter.setEmptyView(R.layout.view_empty)
        // 监听索引点击
        binding.ivSong.indexListener = this

        (activity as MainMusicActivity).onFragmentReady()
        PlayManager.playListener.add(this)
        super.onLazyInit()
    }

    override fun onDestroy() {
        super.onDestroy()
        PlayManager.playListener.remove(this)
    }

    /**
     * 歌曲扫描完毕
     */
    override fun onSongListUpdate() {
        songList.clear()
        songList.addAll(SongManager.allSongList)
        mAdapter.notifyDataSetChanged()
    }

    override fun onTabReselected() {
        // 滚到正在播放
        (binding.rvSong.layoutManager as LinearLayoutManager).run {
            val lastPosition = findLastVisibleItemPosition()
            val firstPosition = findFirstVisibleItemPosition()
            val halfCount = (lastPosition - firstPosition) / 2
            val playingPosition = mAdapter.getPlayingPosition()
            var targetPosition = 0
            targetPosition = if (lastPosition > playingPosition) {
                if (playingPosition - firstPosition > halfCount) {
                    playingPosition + halfCount
                } else {
                    // 正在播放的在看不见的上方
                    playingPosition - halfCount
                }
            } else {
                // 正在播放的在看不见的下方
                playingPosition + halfCount
            }
            if (targetPosition < 0) {
                targetPosition = 0
            }
            binding.rvSong.scrollToPosition(targetPosition)
        }
    }

    override fun onIndexClick(indexText: String) {
        scrollListener?.hideIndex()
        // 选中某个英文字母
        if (TextUtils.equals(indexText, "#")) {
            // 滚到顶部
            binding.rvSong.scrollToPosition(0)
        } else {
            for ((index, song) in songList.withIndex()) {
                if (indexText.equals(PinyinUtils.getPinyinFirstLetter(song.title), true)) {
                    binding.rvSong.scrollToPosition(index)
                    return
                }
            }
            ToastUtils.showShort("没有${indexText}开头的歌曲")
        }
    }

    override fun onSongSwitch() {
        mAdapter.notifyDataSetChanged()
    }

    override fun onPlayStatusChange() {

    }
}