package org.b3log.siyuan.ld246.api

import org.b3log.siyuan.ld246.ld246_Response
import org.b3log.siyuan.ld246.ld246_Response_NoData
import org.b3log.siyuan.ld246.ld246_Response_User
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query


// 定义API服务接口
interface ApiServiceNotification {
    @GET("api/v2/notifications/commented")
    fun apiV2NotificationsCommentedGet(
        @Query("p") page: Int?,
        @Header("Authorization") Authorization: String?,
        @Header("User-Agent") UA: String?,
    ): Call<ld246_Response>

    @GET("api/v2/notifications/comment2ed") // 该接口服务器端异常
    fun apiV2NotificationsComment2edGet(
        @Query("p") page: Int?,
        @Header("Authorization") Authorization: String?,
        @Header("User-Agent") UA: String?,
    ): Call<ld246_Response>

    @GET("api/v2/notifications/reply")
    fun apiV2NotificationsReplyGet(
        @Query("p") page: Int?,
        @Header("Authorization") Authorization: String?,
        @Header("User-Agent") UA: String?,
    ): Call<ld246_Response>

    @GET("api/v2/notifications/at")
    fun apiV2NotificationsAtGet(
        @Query("p") page: Int?,
        @Header("Authorization") Authorization: String?,
        @Header("User-Agent") UA: String?,
    ): Call<ld246_Response>

    @GET("api/v2/notifications/following")
    fun apiV2NotificationsFollowingGet(
        @Query("p") page: Int?,
        @Header("Authorization") Authorization: String?,
        @Header("User-Agent") UA: String?,
    ): Call<ld246_Response>

    @GET("api/v2/notifications/point")
    fun apiV2NotificationsPointGet(
        @Query("p") page: Int?,
        @Header("Authorization") Authorization: String?,
        @Header("User-Agent") UA: String?,
    ): Call<ld246_Response>

    @GET("api/v2/notifications/make-read/{type}")
    fun apiV2NotificationsMakeRead(
        @Path("type") type: String,
        @Header("Authorization") Authorization: String?,
        @Header("User-Agent") UA: String?,
    ): Call<ld246_Response_NoData>

    @GET("api/v2/user")
    fun apiV2UserGet(
        @Header("Authorization") Authorization: String?,
        @Header("User-Agent") UA: String?,
    ): Call<ld246_Response_User>
}
