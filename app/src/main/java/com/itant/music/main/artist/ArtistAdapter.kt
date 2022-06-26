package com.itant.music.main.artist

import android.content.Intent
import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.itant.music.R
import com.itant.music.main.artist.song.ArtistSongActivity
import com.miekir.common.tools.ActivityTools


class ArtistAdapter(private val artistList: MutableList<ArtistBean>):
    BaseQuickAdapter<ArtistBean, BaseViewHolder>(R.layout.item_artist, data = artistList) {

    override fun convert(holder: BaseViewHolder, item: ArtistBean) {
        holder.setText(R.id.tv_first_name, item.firstName)
        holder.setText(R.id.tv_last_name, item.lastName)

        holder.getView<View>(R.id.mcv_song).setOnClickListener {
            // 点击歌手
            val intent = Intent(context, ArtistSongActivity::class.java)
            intent.putExtra(ArtistSongActivity.KEY_ARTIST_NAME, item.lastName)
            ActivityTools.openActivity(context, intent)
        }
    }
}