package org.b3log.siyuan.ld246

data class ld246_Response_NoData(
    val msg: String,
    val random: String,
    val code: Int,
)
data class ld246_Response(
    val msg: String,
    val random: String,
    val code: Int,
    val data: ld246_Response_Data,
)
data class ld246_Response_Data(
    val notifications: List<ld246_Response_Data_Notification>, // 理想的字段，其实不存在
    val commentedNotifications: List<ld246_Response_Data_Notification>, // 回帖
    val comment2edNotifications: List<ld246_Response_Data_Notification>, // 评论
    val replyNotifications: List<ld246_Response_Data_Notification>, // 回复
    val atNotifications: List<ld246_Response_Data_Notification>, // 提及
    val followingNotifications: List<ld246_Response_Data_Notification>, // 关注
    val pointNotifications: List<ld246_Response_Data_Notification>, // 积分
    val pagination: Pagination,
    val unreadNotificationCount: UnreadNotificationCount,
)

data class ld246_Response_Data_Notification(
    val dataId: String,
    val authorName: String?,
    val authorAvatarURL: String?,
    val dataType: Int,
    val hasRead: Boolean,
    val title: String?,
    val content: String,
)

data class Pagination(
    val paginationPageCount: Int,
    val paginationPageNums: List<Int>
)

data class UnreadNotificationCount(
    val unreadReviewNotificationCnt: Int,
    val unreadNotificationCnt: Int,
)
data class ld246_Response_User(
    val msg: String,
    val random: String,
    val code: Int,
    val data: ld246_Response_Data_User,
)
data class ld246_Response_Data_User(
    val user: User, // 用户信息
)

data class User(
    val userNickname: String = "", //
    val userAppRole: Int = -1,
    val userCardBImgURL: String = "", //
    val userCurrentCheckinStreak: Int = -1, //
    val userAvatarURL: String = "", //
    val userIntro: String = "",
    val userHomeBImgDColor: String = "",
    val userTags: String = "",
    val userURL: String = "",
    val userTagCount: Int = -1,
    val userComment2Count: Int = -1,
    val userLongestCheckinStreak: Int = -1,
    val userNo: Int = -1,
    val userCardBImgDColor: String = "",
    val userPoint: Int = -1,
    val userCommentCount: Int = -1,
    val userGeneralRank: Int = -1, //
    val oId: String = "",
    val userName: String = "", //
    val userHomeBImgURL: String = "",
    val userArticleCount: Int = -1, //
    val userRole: String = ""
)