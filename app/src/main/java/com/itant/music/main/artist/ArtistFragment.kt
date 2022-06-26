package com.itant.music.main.artist

import androidx.recyclerview.widget.LinearLayoutManager
import com.itant.music.R
import com.itant.music.base.MusicFragment
import com.itant.music.databinding.FragmentArtistBinding
import com.itant.music.widget.FixedScrollListener
import com.miekir.common.extension.lazy

/**
 * 歌手界面
 */
class ArtistFragment : MusicFragment<FragmentArtistBinding>() {
    private val artistList = ArrayList<ArtistBean>()
    private lateinit var mAdapter: ArtistAdapter
    private val presenter by lazy<ArtistFragment, ArtistPresenter>()

    override fun getTitle(): String {
        return "歌手"
    }

    override fun onBindingInflate(): FragmentArtistBinding {
        return FragmentArtistBinding.inflate(layoutInflater)
    }

    override fun onLazyInit() {
        // 把map的key展示出来
        mAdapter = ArtistAdapter(artistList)
        binding.rvArtist.run {
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
            addOnScrollListener(FixedScrollListener(this@ArtistFragment))
        }
        // 设置空View之前必须先绑定RecyclerView
        mAdapter.setEmptyView(R.layout.view_empty)
        presenter.sortArtist()
        super.onLazyInit()
    }

    override fun onSongListUpdate() {
        if (!hasInit) {
            return
        }
        presenter.sortArtist()
    }

    fun onArtistSorted(result: List<ArtistBean>?) {
        if (result == null) {
            return
        }
        artistList.clear()
        artistList.addAll(result)
        mAdapter.notifyDataSetChanged()
    }
}