/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/7/7 下午9:24
 * updated: 2024/7/7 下午9:24
 */

package sc.windom.sofill.dataClass

import kotlinx.serialization.Serializable

// 与服务器交互的 dataClass 应避免混淆
@Serializable
data class ld246_Response(
    val msg: String,
    val random: String,
    val code: Int,
    val data: ld246_Response_Data? = null,
)
@Serializable
data class ld246_Response_Data(
    val notifications: List<ld246_Response_Data_Notification>? = null, // 理想的字段，其实不存在
    val commentedNotifications: List<ld246_Response_Data_Notification>? = null, // 回帖
    val comment2edNotifications: List<ld246_Response_Data_Notification>? = null, // 评论
    val replyNotifications: List<ld246_Response_Data_Notification>? = null, // 回复
    val atNotifications: List<ld246_Response_Data_Notification>? = null, // 提及
    val followingNotifications: List<ld246_Response_Data_Notification>? = null, // 关注
    val pointNotifications: List<ld246_Response_Data_Notification>? = null, // 积分
    val user: ld246_User? = null, // 用户信息
    val pagination: Pagination? = null,
    val unreadNotificationCount: UnreadNotificationCount? = null,
)
@Serializable
data class ld246_Response_Data_Notification(
    val dataId: String? = null,
    val authorName: String? = null,
    val authorAvatarURL: String? = null,
    val dataType: Int? = null,
    val hasRead: Boolean? = null,
    val title: String? = null,
    val content: String? = null,
)
@Serializable
data class Pagination(
    val paginationPageCount: Int,
    val paginationPageNums: List<Int>
)
@Serializable
data class UnreadNotificationCount(
    val unreadReviewNotificationCnt: Int,
    val unreadNotificationCnt: Int,
)

@Serializable
data class ld246_User(
    val userNickname: String? = null, //
    val userAppRole: Int? = null,
    val userCardBImgURL: String? = null, //
    val userCurrentCheckinStreak: Int? = null, //
    val userAvatarURL: String? = null, //
    val userIntro: String? = null,
    val userHomeBImgDColor: String? = null,
    val userTags: String? = null,
    val userURL: String? = null,
    val userTagCount: Int? = null,
    val userComment2Count: Int? = null,
    val userLongestCheckinStreak: Int? = null,
    val userNo: Int? = null,
    val userCardBImgDColor: String? = null,
    val userPoint: Int? = null,
    val userCommentCount: Int? = null,
    val userGeneralRank: Int? = null, //
    val oId: String? = null,
    val userName: String? = null, //
    val userHomeBImgURL: String? = null,
    val userArticleCount: Int? = null, //
    val userRole: String? = null
)