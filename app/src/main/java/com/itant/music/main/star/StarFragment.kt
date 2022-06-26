package com.itant.music.main.star

import androidx.recyclerview.widget.LinearLayoutManager
import com.itant.music.R
import com.itant.music.base.MusicFragment
import com.itant.music.constant.CurrentListType
import com.itant.music.databinding.FragmentSongBinding
import com.itant.music.listener.PlayListener
import com.itant.music.main.song.SongAdapter
import com.itant.music.main.song.SongBean
import com.itant.music.manager.PlayManager

/**
 * 收藏界面
 */
class StarFragment : MusicFragment<FragmentSongBinding>(), PlayListener {
    private val songList = arrayListOf<SongBean>()
    private lateinit var mAdapter: SongAdapter

    override fun getTitle(): String {
        return "喜欢"
    }

    override fun onBindingInflate(): FragmentSongBinding {
        return FragmentSongBinding.inflate(layoutInflater)
    }

    override fun onLazyInit() {
        PlayManager.playListener.add(this)
        mAdapter = SongAdapter(songList, CurrentListType.STAR)
        binding.rvSong.run {
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }
        // 设置空View之前必须先绑定RecyclerView
        mAdapter.setEmptyView(R.layout.view_empty)
        updateStarList()
        super.onLazyInit()
    }

    override fun onDestroy() {
        super.onDestroy()
        PlayManager.playListener.remove(this)
    }

    override fun onSongListUpdate() {
        if (!hasInit) {
            return
        }
        updateStarList()
    }

    /**
     * 刷新列表，数量可能有变化
     */
    private fun updateStarList() {
        songList.clear()
        songList.addAll(StarManager.starSongList)
        mAdapter.notifyDataSetChanged()
    }

    override fun onSongSwitch() {
        // 这里的切歌对于收藏界面来说是特殊的，因为有可能在播放里面收藏/取消收藏了歌曲，造成列表内容变更
        updateStarList()
    }

    override fun onPlayStatusChange() {
        mAdapter.notifyDataSetChanged()
    }
}