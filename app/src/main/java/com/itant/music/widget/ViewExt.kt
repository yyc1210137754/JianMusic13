package com.itant.music.widget

import android.graphics.*
import android.view.TouchDelegate
import android.view.View
import com.itant.music.R
import com.miekir.common.context.MainContext
import com.miekir.common.extension.dp2px

/**
 * 扩展控件左右点击范围
 */
fun expandClickSpace(view : View) {
    val parent = view.parent as? View ?: return
    parent.post {
        val delegateArea = Rect()
        view.getHitRect(delegateArea)
        val extra = MainContext.getContext().resources.getDimension(R.dimen.padding_default).toInt()
        delegateArea.left -= extra
        delegateArea.top -= extra
        delegateArea.right += extra
        delegateArea.bottom += extra
        parent.touchDelegate = TouchDelegate(delegateArea, view)
    }
}

/**
 * 扩展控件点击范围
 */
fun View.expandClick(size: Float = 16.0f) {
    val viewParent = parent as View
    val view = this
    viewParent.post {
        val delegateArea = Rect()
        view.getHitRect(delegateArea)
        val extra = size.dp2px.toInt()
        delegateArea.left -= extra
        delegateArea.top -= extra
        delegateArea.right += extra
        delegateArea.bottom += extra
        viewParent.touchDelegate = TouchDelegate(delegateArea, view)
    }
}

/**
 * 扩展控件上下点击范围
 */
fun View.expandVerticalClick(size: Float = 16.0f) {
    val viewParent = parent as View
    val view = this
    viewParent.post {
        val delegateArea = Rect()
        view.getHitRect(delegateArea)
        val extra = size.dp2px.toInt()
        delegateArea.top -= extra
        delegateArea.bottom += extra
        viewParent.touchDelegate = TouchDelegate(delegateArea, view)
    }
}

fun Bitmap.tint(color: Int): Bitmap =
    Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888).also { outBmp ->
        Canvas(outBmp).drawBitmap(
            this, 0f, 0f,
            Paint().apply {
                this.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
            }
        )
    }