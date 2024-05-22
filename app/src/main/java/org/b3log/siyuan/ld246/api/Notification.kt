package org.b3log.siyuan.ld246.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import org.b3log.siyuan.ld246.回帖消息Response
import retrofit2.http.Header


// 定义API服务接口
interface ApiServiceNotification {
    @GET("api/v2/notifications/commented")
    fun apiV2NotificationsCommentedGet(
        @Query("p") page: String?,
        @Header("Authorization") Authorization: String?,
        @Header("User-Agent") UA: String?,
    ): Call<回帖消息Response>
}
