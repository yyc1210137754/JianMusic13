package com.itant.music.net

import com.miekir.common.exception.Code
import com.miekir.common.exception.ExceptionResult
import com.miekir.common.tools.ToastTools
import com.miekir.task.net.AbstractResponse

/**
 * 歌词统一返回封装
 * @author Miekir
 */
class LyricResponse<T> : AbstractResponse() {
    /**
     * 返回状态代码
     */
    var code = 0

    /**
     * 数量
     */
    var count: String? = null

    /**
     * 返回的实体数据
     */
    var result: T? = null

    /**
     * 调用一下，防止有些不需要使用到结果的接口不断提交失败，及时发现隐藏的重大错误如登录过期等
     */
    override fun valid(): Boolean {
        if (code != Code.SUCCESS) {
            if (code == -99) {
                ToastTools.showShort("登录已过期，请重新登录")
                //重新登录 ActivityTools.jump("com.itant.LoginActivity")
            }
            throw ExceptionResult(code, "")
        }
        return true
    }
}