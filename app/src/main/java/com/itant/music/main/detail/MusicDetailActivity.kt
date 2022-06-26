package com.itant.music.main.detail

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.TextUtils
import android.widget.PopupMenu
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ToastUtils
import com.itant.music.R
import com.itant.music.base.MusicActivity
import com.itant.music.bean.LyricBean
import com.itant.music.databinding.ActivityMusicDetailBinding
import com.itant.music.listener.PlayListener
import com.itant.music.main.MainMusicActivity
import com.itant.music.main.star.StarManager
import com.itant.music.manager.PlayManager
import com.itant.music.manager.PlayModeManager
import com.itant.music.manager.PlaySpeedManager
import com.itant.music.manager.StoreManager
import com.miekir.common.extension.lazy
import com.miekir.common.extension.openActivity
import com.miekir.common.extension.setSingleClick
import okhttp3.ResponseBody
import java.util.concurrent.TimeUnit



class MusicDetailActivity : MusicActivity<ActivityMusicDetailBinding>(), PlayListener {
    companion object {
        const val WHAT_UPDATE_PROGRESS = 0
        const val KEY_LYRIC = "key_lyric"
    }
    // 当前歌曲是否被收藏了
    private var isCurrentStar = false
    // 更新进度
    private val progressHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            binding.pbSong.progress = getCurrentProgress()
            binding.tvPosition.text = getDurationString(PlayManager.mediaPlayer!!.currentPosition.toLong())
            sendEmptyMessageDelayed(WHAT_UPDATE_PROGRESS, 1000L)
        }
    }

    private val mPresenter by lazy<MusicDetailActivity, MusicDetailPresenter>()

    override fun onBindingInflate(): ActivityMusicDetailBinding {
        return ActivityMusicDetailBinding.inflate(layoutInflater)
    }

    override fun onInit() {
        PlayManager.playListener.add(this)

        // 搜索歌词
        binding.tvLyric.setSingleClick {
            val currentSong = PlayManager.currentSong
            if (currentSong == null || TextUtils.isEmpty(currentSong.title)) {
                return@setSingleClick
            }
            mPresenter.getLyricList(currentSong.id.toString(), currentSong.title!!)
        }

        // 收藏/取消收藏
        binding.ivStar.setSingleClick {
            isCurrentStar = !isCurrentStar
            binding.ivStar.setImageResource(if (isCurrentStar) R.drawable.ic_star else R.drawable.ic_unstar)
            StarManager.updateStarList(isCurrentStar)
            PlayManager.onSongSwitch()
        }

        // 上一曲
        binding.ivPreSong.setSingleClick {
            PlayManager.startPlayPrev()
        }

        // 下一曲
        binding.ivNextSong.setSingleClick {
            PlayManager.startPlayNext(false)
        }

        // 播放/暂停播放
        binding.ivPausePlay.setSingleClick {
            PlayManager.togglePlay()
        }

        // 切换播放模式
        binding.tvPlayMode.setSingleClick {
            PlayModeManager.switchPlayMode()
            val playMode = PlayModeManager.getPlayModeString()
            binding.tvPlayMode.text = playMode
            ToastUtils.showShort(playMode)
        }

        // 切换播放速度
        binding.tvPlaySpeed.setSingleClick {
            val popupMenu = PopupMenu(this, binding.tvPlaySpeed)
            val menu = popupMenu.menu
            for ((index, speed) in PlaySpeedManager.SPEED_ARRAY.withIndex()) {
                menu.add(0, 0, index, PlaySpeedManager.getPlaySpeed(speed))
            }
            popupMenu.setOnMenuItemClickListener {
                PlaySpeedManager.playSpeed = PlaySpeedManager.SPEED_ARRAY[it.order]
                binding.tvPlaySpeed.text = PlaySpeedManager.getPlaySpeed()
                PlayManager.changePlaySpeed()
                true
            }
            popupMenu.show()
        }

        binding.tvPlayMode.text = PlayModeManager.getPlayModeString()
        binding.tvPlaySpeed.text = PlaySpeedManager.getPlaySpeed()
        updateStatus()

        // 每隔一秒刷新进度
        progressHandler.sendEmptyMessageDelayed(WHAT_UPDATE_PROGRESS, 1000L)
    }

    override fun onDestroy() {
        progressHandler.removeMessages(WHAT_UPDATE_PROGRESS)
        PlayManager.playListener.remove(this)
        // 退出的时候，如果主界面不存在，则打开主界面
        if (!ActivityUtils.isActivityExistsInStack(MainMusicActivity::class.java)) {
            openActivity<MainMusicActivity>()
        }
        super.onDestroy()
    }

    /**
     * 切换歌曲，更新当前歌曲信息
     */
    override fun onSongSwitch() {
        updateStatus()
    }

    /**
     * 暂停或者恢复播放
     */
    override fun onPlayStatusChange() {
        updateStatus()
    }

    /**
     * 初始化或者切歌了，需要更新状态
     */
    private fun updateStatus() {
        isCurrentStar = StarManager.isCurrentSongStar()
        // 歌曲标题
        binding.tvTitle.text = PlayManager.currentSong!!.title
        binding.ivStar.setImageResource(if (isCurrentStar) R.drawable.ic_star else R.drawable.ic_unstar)
        binding.ivPausePlay.setImageResource(if (PlayManager.isPlaying) R.drawable.ic_pause else R.drawable.ic_play)
        binding.pbSong.max = 100
        binding.tvDuration.text = getDurationString(PlayManager.currentSong!!.duration)

        // 看看本地有没有缓存的
        val lyric = StoreManager.lyricKeyValue.decodeString(PlayManager.currentSong!!.id.toString(), "")
        binding.tvLyric.text = if (TextUtils.isEmpty(lyric)) "暂无歌词（点击这里尝试搜索）" else lyric
    }

    /**
     * 获取格式化后的总时长
     */
    private fun getDurationString(duration: Long): String {
        val seconds = duration / 1000
        val m = TimeUnit.SECONDS.toMinutes(seconds)
        val s = seconds - m * 60
        return String.format("%02d:%02d", m, s)
    }

    /**
     * 获取当前播放位置
     */
    private fun getCurrentProgress(): Int {
        if (PlayManager.mediaPlayer == null) {
            return 0
        }
        return ((PlayManager.mediaPlayer!!.currentPosition.toFloat() / PlayManager.currentSong!!.duration) * 100).toInt()
    }

    /**
     * 获取歌词列表成功
     */
    fun getLyricListSuccess(songId: String, lyricList: ArrayList<LyricBean>?) {
        if (lyricList != null && lyricList.isNotEmpty()) {
            if (TextUtils.isEmpty(lyricList[0].lrc)) {
                return
            }
            mPresenter.getLyric(songId, lyricList[0].lrc!!)
        }
    }

    /**
     * 获取歌词成功
     */
    fun getLyricSuccess(songId: String, lyricResponse: ResponseBody?) {
        if (lyricResponse == null) {
            return
        }
        // 最小匹配[开头的，]结尾的，替换为空
        val lyric = lyricResponse.string()
            .replace("""\[[^\[]*?\]""".toRegex(), "")
            //.replace(" ", "\n")
        binding.tvLyric.text = lyric
        // 把歌词缓存下来
        StoreManager.lyricKeyValue.encode(songId, lyric)
    }
}