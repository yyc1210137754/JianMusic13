package com.itant.music.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.itant.music.base.MusicFragment

class MainMusicAdapter(activity: FragmentActivity, private val fragmentList: Array<MusicFragment<*>>) :
    FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = fragmentList.size

    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }
}