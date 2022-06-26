package com.itant.music.main.setting

import android.app.AlertDialog
import android.content.DialogInterface
import com.blankj.utilcode.util.ClipboardUtils
import com.blankj.utilcode.util.ToastUtils
import com.itant.music.base.MusicActivity
import com.itant.music.databinding.ActivitySettingBinding
import com.itant.music.manager.StoreManager
import com.itant.music.widget.expandClick
import com.miekir.common.extension.getText
import com.miekir.common.extension.setSingleClick
import com.miekir.common.tools.ActivityTools


class SettingActivity : MusicActivity<ActivitySettingBinding>() {
    override fun onBindingInflate(): ActivitySettingBinding {
        return ActivitySettingBinding.inflate(layoutInflater)
    }

    override fun onInit() {
        ActivityTools.swipeActivity(this)
        //expandClickSpace(binding.ivBack)
        binding.ivBack.expandClick()
        binding.ivBack.setOnClickListener {
            onBackPressed()
        }
        binding.tvFeedback.setOnClickListener {
            ClipboardUtils.copyText("964161472@qq.com")
            ToastUtils.showShort("邮箱复制成功")
        }

        // 设置时长过滤
        binding.etDurationFilter.setText("${SettingManager.durationFilter/1000L}")
        binding.btnSave.setSingleClick {
            val duration = binding.etDurationFilter.getText("时长不能为空") ?: return@setSingleClick
            SettingManager.durationFilter = duration.toLong() * 1000L
            ToastUtils.showShort("保存成功，请刷新列表")
            finish()
        }

        // 清除歌词缓存
        binding.btnClearLyric.setSingleClick {
            AlertDialog.Builder(this@SettingActivity)
                .setMessage("确定清除本地缓存的歌词(${getLyricSize(StoreManager.lyricKeyValue.actualSize())})？")
                .setPositiveButton("确定", DialogInterface.OnClickListener { dialog, which ->
                    dialog.cancel()
                    StoreManager.lyricKeyValue.clearAll()
                    ToastUtils.showShort("清除成功")
                })
                .setNegativeButton("取消", DialogInterface.OnClickListener { dialog, which ->
                    dialog.cancel()
                }).show()
        }
    }

    private fun getLyricSize(size: Long):String {
        if (size < 1024) {
            return "1KB"
        }

        if (size < 1024 * 1024) {
            return "${size/1024}KB"
        }

        return "${size/1024/1024}MB"
    }
}