package com.itant.music.widget.index

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.ToastUtils
import com.itant.music.R




class IndexView : FrameLayout, View.OnClickListener {
    companion object {
        val INDEX_TEXT = arrayOf("#", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L",
            "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z")
    }

    var indexListener : IIndexListener? = null

    constructor (context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        LayoutInflater.from(context).inflate(R.layout.view_index, this)
        val wrapperView = findViewById<LinearLayout>(R.id.ll_index)
        for (text in INDEX_TEXT) {
            val textView = TextView(context)
            textView.setTextColor(ContextCompat.getColor(context, R.color.white))
            textView.setTypeface(textView.typeface, Typeface.BOLD)
            textView.text = text
            textView.layoutParams = LinearLayout.LayoutParams(
                MATCH_PARENT,
                //resources.getDimension(R.dimen.size_index_bg).toInt(),
                0,
                1.0f
            )
            textView.gravity = Gravity.CENTER
            //expandClickSpace(textView)
            // 设置背景颜色，解决很难触发点击的问题
            //textView.setBackgroundColor(ContextCompat.getColor(context, R.color.red_droid))
            textView.setOnClickListener(this)
            wrapperView.addView(textView)
        }
    }

    override fun onClick(view: View?) {
        if (view is TextView) {
            ToastUtils.showShort("跳转到${view.text}")
            indexListener?.onIndexClick(view.text.toString())
        }
    }
}