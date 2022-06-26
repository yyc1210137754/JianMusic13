package com.itant.music.main.song

import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.google.android.material.card.MaterialCardView
import com.itant.music.R
import com.itant.music.constant.CurrentListType
import com.itant.music.main.artist.ArtistManager
import com.itant.music.main.star.StarManager
import com.itant.music.manager.PlayManager
import com.itant.music.manager.StoreManager
import com.itant.music.utils.getMusicSize
import com.itant.music.utils.getMusicTotalTime


class SongAdapter(private val songList: MutableList<SongBean>, private val currentListType: Int) :
    BaseQuickAdapter<SongBean, BaseViewHolder>(R.layout.item_song, data = songList) {

    private var mPlayingSong: SongBean? = null
    private val mColorPlaying: Int by lazy {
        R.drawable.shape_playing
    }
    private val mColorNormal: Int by lazy {
        R.color.transparent
    }

    /**
     * 获取当前正在播放的位置
     */
    fun getPlayingPosition(): Int {
        for ((index, song) in songList.withIndex()) {
            if (song.id == PlayManager.currentSongId) {
                return index
            }
        }
        return 0
    }

    override fun convert(holder: BaseViewHolder, songBean: SongBean) {
        holder.setText(R.id.tv_name, songBean.title)
        holder.setText(R.id.tv_artist, songBean.artist)
        holder.setText(R.id.tv_size, getMusicSize(songBean.fileSize))
        holder.setText(R.id.tv_total_time, getMusicTotalTime(songBean.duration))
        holder.setVisible(R.id.iv_star, songBean.isStarred)

        when {
            songBean.format.contains("flac", true) -> {
                holder.setTextColor(R.id.tv_format, ContextCompat.getColor(context, R.color.red_droid))
                holder.setBackgroundResource(R.id.tv_format, R.drawable.shape_music_format_h)
            }
            songBean.format.contains("mp3") -> {
                holder.setTextColor(R.id.tv_format, ContextCompat.getColor(context, R.color.black_text))
                holder.setBackgroundResource(R.id.tv_format, R.drawable.shape_music_format_m)
            }
            else -> {
                holder.setTextColor(R.id.tv_format, ContextCompat.getColor(context, R.color.green_ka))
                holder.setBackgroundResource(R.id.tv_format, R.drawable.shape_music_format_u)
            }
        }
        holder.setText(R.id.tv_format, songBean.format)


        val songItemView = holder.getView<MaterialCardView>(R.id.mcv_song)
        songItemView.setOnClickListener {
            onSongClick(songBean)
        }

        // 正在播放的歌曲
        val fl_playing = holder.getView<FrameLayout>(R.id.fl_playing)
        if (songBean.id == PlayManager.currentSongId) {
            fl_playing.setBackgroundResource(mColorPlaying)
            mPlayingSong = songBean
        } else {
            fl_playing.setBackgroundResource(mColorNormal)
        }
    }

    /**
     * 点击某一首歌曲，开始播放
     */
    private fun onSongClick(songBean: SongBean) {
        // 点击歌曲，开始播放
        PlayManager.currentSongId = songBean.id
        if (PlayManager.currentListType != currentListType) {
            // 点击了不同的列表，需要更新当前列表
            PlayManager.currentSongList.clear()
            when (currentListType) {
                CurrentListType.MAIN -> {
                    // 切换到主列表
                    PlayManager.currentSongList.addAll(SongManager.allSongList)
                }
                CurrentListType.ARTIST -> {
                    val artistSongList = ArtistManager.artistSongMap[songBean.artist]
                    if (artistSongList != null) {
                        // 切换到某个歌手下的列表
                        PlayManager.currentSongList.addAll(artistSongList)
                    }
                }
                CurrentListType.STAR -> {
                    // 切换到我的喜欢
                    PlayManager.currentSongList.addAll(StarManager.starSongList)
                }
            }
        }
        PlayManager.currentListType = currentListType
        if (currentListType == CurrentListType.ARTIST) {
            // 如果点击的是歌手的歌曲列表里的歌曲，保存这个歌手
            StoreManager.keyValue.encode(ArtistManager.KEY_CURRENT_ARTIST, songBean.artist)
        }
        notifyDataSetChanged()
        // 开始播放，在通知栏显示
        PlayManager.startPlayNew()
        PlayManager.onSongSwitch()
    }
}