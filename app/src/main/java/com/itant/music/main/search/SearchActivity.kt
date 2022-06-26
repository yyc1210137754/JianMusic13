package com.itant.music.main.search

import android.view.WindowManager
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.KeyboardUtils
import com.itant.music.R
import com.itant.music.base.MusicActivity
import com.itant.music.constant.CurrentListType
import com.itant.music.databinding.ActivitySearchBinding
import com.itant.music.main.song.SongAdapter
import com.itant.music.main.song.SongBean
import com.itant.music.widget.expandClick
import com.miekir.common.extension.lazy
import com.miekir.common.tools.ActivityTools

class SearchActivity : MusicActivity<ActivitySearchBinding>() {
    private val songList = arrayListOf<SongBean>()
    private lateinit var mAdapter: SongAdapter
    private val searchPresenter by lazy<SearchActivity, SearchPresenter>()

    override fun onBindingInflate(): ActivitySearchBinding {
        return ActivitySearchBinding.inflate(layoutInflater)
    }

    override fun onInit() {
        ActivityTools.swipeActivity(this)
        //expandClickSpace(binding.ivBack)
        binding.ivBack.expandClick()
        binding.ivBack.setOnClickListener {
            onBackPressed()
        }

        mAdapter = SongAdapter(songList, CurrentListType.MAIN)
        binding.rvSearchSong.run {
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }
        // 设置空View之前必须先绑定RecyclerView
        mAdapter.setEmptyView(R.layout.view_empty)

        binding.etKeyWords.addTextChangedListener {
            val keyWords = it.toString()
            searchPresenter.searchLocalSong(keyWords)
        }
        // 解决软键盘弹出时闪现背景色的问题
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        binding.etKeyWords.requestFocus()
        binding.etKeyWords.postDelayed({
            KeyboardUtils.showSoftInput(this)
        }, 500)
    }

    fun onSearchResult(result: ArrayList<SongBean>?) {
        songList.clear()
        if (result != null) {
            songList.addAll(result)
        }
        mAdapter.notifyDataSetChanged()
    }
}