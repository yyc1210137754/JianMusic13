package com.itant.music.main

import android.Manifest
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.view.View
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ToastUtils
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.itant.music.base.MusicActivity
import com.itant.music.base.MusicFragment
import com.itant.music.databinding.ActivityMainBinding
import com.itant.music.main.artist.ArtistFragment
import com.itant.music.main.detail.MusicDetailActivity
import com.itant.music.main.search.SearchActivity
import com.itant.music.main.setting.SettingActivity
import com.itant.music.main.song.SongFragment
import com.itant.music.main.song.SongManager
import com.itant.music.main.star.StarFragment
import com.itant.music.manager.PlayManager
import com.itant.music.play.MusicService
import com.itant.music.utils.NotificationUtils
import com.itant.music.widget.expandClickSpace
import com.itant.music.widget.expandVerticalClick
import com.miekir.common.extension.lazy
import com.miekir.common.extension.openActivity
import com.miekir.common.extension.requestPermissions
import com.miekir.common.extension.setSingleClick
import com.miekir.common.tools.ToastTools
import java.util.*


/**
 * 主界面
 */
class MainMusicActivity : MusicActivity<ActivityMainBinding>(), LoaderManager.LoaderCallbacks<Cursor> {
    private lateinit var mMainAdapter: MainMusicAdapter
    private lateinit var mFragmentArray: Array<MusicFragment<*>>
    private val mPresenter: MainMusicPresenter by lazy()

    override fun onBindingInflate(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onInit() {
        requestPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) { granted, temp ->
            if (granted) {
                init()
            } else {
                if (temp) {
                    ToastTools.showShort("请授予权限后重试")
                } else {
                    // 多次拒绝，跳转设置界面
                    NotificationUtils.jumpSettingForPermission(this)
                }
            }
        }
    }

    /**
     * 初始化
     */
    private fun init() {
        // 需要有通知权限才能在通知栏控制音乐
        NotificationUtils.judgeNotificationPermission(this)
        startService(Intent(this, MusicService::class.java))

        // 去除尽头拖拽时的阴影效果
        (binding.pager.getChildAt(0) as? RecyclerView)?.overScrollMode = RecyclerView.OVER_SCROLL_NEVER

        expandClickSpace(binding.ivSetting)
        binding.ivSetting.setSingleClick {
            // 跳转设置界面
            openActivity<SettingActivity>()
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

        binding.tvTitle.expandVerticalClick(8.0f)
        binding.tvTitle.setSingleClick {
            if (binding.srlSong.isRefreshing) {
                ToastUtils.showShort("请等待搜索完成")
                return@setSingleClick
            }
            // 跳转搜索
            openActivity<SearchActivity>()
        }

        mFragmentArray = arrayOf(SongFragment(), ArtistFragment(), StarFragment())
        mMainAdapter = MainMusicAdapter(this, mFragmentArray)
        binding.pager.adapter = mMainAdapter
        binding.pager.offscreenPageLimit = mFragmentArray.size

        TabLayoutMediator(binding.tabLayout, binding.pager) { tab, position ->
            tab.text = mFragmentArray[position].getTitle()
        }.attach()
        binding.tabLayout.addOnTabSelectedListener(object :TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                mFragmentArray[tab.position].onTabReselected()
            }
        })
    }

    /**
     * 获取SwipeRefreshLayout
     */
    fun getSwipeLayout() : View {
        return binding.srlSong
    }

    /**
     * fragment启动完成
     */
    fun onFragmentReady() {
        // 监听下拉刷新
        binding.srlSong.setOnRefreshListener {
            LoaderManager.getInstance(this).restartLoader(SongFragment.MUSIC_ID, null, this)
        }
        // 开始扫描歌曲
        binding.srlSong.isRefreshing = true
        LoaderManager.getInstance(this).initLoader(SongFragment.MUSIC_ID, null, this)
    }

    /**
     * 歌曲搜索条件
     */
    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        return mPresenter.getMusicLoader()
    }

    /**
     * 音乐加载完成
     */
    override fun onLoadFinished(loader: Loader<Cursor>, cursor: Cursor) {
        mPresenter.sortSongs(cursor)
    }

    /**
     * 音乐在后台加载完成并排好序了
     */
    fun onSortFinished() {
        // 歌曲总数量
        binding.tvMusicSum.text = "${SongManager.allSongList.size}"
        // 告诉各个Fragment
        for (musicFragment in mFragmentArray) {
            musicFragment.onSongListUpdate()
        }

        binding.srlSong.isRefreshing = false
        // 免得resume的时候搜索到0首歌曲
        LoaderManager.getInstance(this).destroyLoader(SongFragment.MUSIC_ID)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {}

    var lastBackMillis = 0L
    override fun onBackPressed() {
        if (System.currentTimeMillis() - lastBackMillis > 2500) {
            ToastUtils.showShort("再按一次退出")
            lastBackMillis = System.currentTimeMillis()
        } else {
            super.onBackPressed()
        }
    }
}