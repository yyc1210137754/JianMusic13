package com.itant.music.main.artist.song

import android.text.TextUtils
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.ToastUtils
import com.itant.music.R
import com.itant.music.base.MusicActivity
import com.itant.music.constant.CurrentListType
import com.itant.music.databinding.ActivityArtistSongBinding
import com.itant.music.main.detail.MusicDetailActivity
import com.itant.music.main.song.SongAdapter
import com.itant.music.main.song.SongBean
import com.itant.music.manager.PlayManager
import com.itant.music.widget.expandClick
import com.miekir.common.extension.lazy
import com.miekir.common.extension.openActivity
import com.miekir.common.extension.setSingleClick
import com.miekir.common.tools.ActivityTools


class ArtistSongActivity : MusicActivity<ActivityArtistSongBinding>() {
    private val songList = arrayListOf<SongBean>()
    private lateinit var mAdapter: SongAdapter
    private val artistSongPresenter by lazy<ArtistSongActivity, ArtistSongPresenter>()

    companion object {
        const val KEY_ARTIST_NAME = "key_artist_name"
    }

    override fun onBindingInflate(): ActivityArtistSongBinding {
        return ActivityArtistSongBinding.inflate(layoutInflater)
    }

    override fun onInit() {
        ActivityTools.swipeActivity(this)
        val artistName = intent.getStringExtra(KEY_ARTIST_NAME)
        binding.tvArtist.text = artistName

        //expandClickSpace(binding.ivBack)
        binding.ivBack.expandClick(16.0f)
        binding.ivBack.setOnClickListener {
            onBackPressed()
        }

        binding.fabPlaying.setSingleClick {
            if (PlayManager.currentSong == null) {
                ToastUtils.showShort("没有正在播放的歌曲")
                return@setSingleClick
            }
            // 跳转详情
            openActivity<MusicDetailActivity>()
        }
        binding.fabPlaying.setOnLongClickListener {
            // 长按暂停或者恢复播放
            PlayManager.togglePlay()
            true
        }

        mAdapter = SongAdapter(songList, CurrentListType.ARTIST)
        binding.rvArtistSong.run {
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }
        // 设置空View之前必须先绑定RecyclerView
        mAdapter.setEmptyView(R.layout.view_empty)

        if (!TextUtils.isEmpty(artistName)) {
            artistSongPresenter.sortArtistSong(artistName!!)
        }
    }

    fun onSongSorted(currentSongs : List<SongBean>) {
        songList.addAll(currentSongs)
        mAdapter.notifyDataSetChanged()
    }
}