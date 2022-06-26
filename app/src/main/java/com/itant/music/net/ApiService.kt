package com.itant.music.net

import com.itant.music.bean.LyricBean
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Url


interface ApiService {
    /**
     * 获取歌词列表
     */
    //@GET("lyric/{songNamePath}")用@Path("songNamePath") songName: String会被转义
    @GET
    suspend fun getLyricList(@Url url: String): LyricResponse<ArrayList<LyricBean>>

    /**
     * 获取歌词，因为返回的格式不是JSON，所以不能直接转成实体
     */
    @GET(".")
    suspend fun getLyric(): ResponseBody
}