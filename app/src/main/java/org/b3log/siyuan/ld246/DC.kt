package org.b3log.siyuan.ld246

data class ld246_Response(
    val msg: String,
    val random: String,
    val code: Int,
    val data: ld246_Response_Data,
    // 其他可能存在的字段可以省略
)
data class ld246_Response_Data(
    val notifications: List<ld246_Response_Data_Notification>, // 理想的字段，其实不存在
    val commentedNotifications: List<ld246_Response_Data_Notification>, // 回帖
    val comment2edNotifications: List<ld246_Response_Data_Notification>, // 评论
    val replyNotifications: List<ld246_Response_Data_Notification>, // 回复
    val atNotifications: List<ld246_Response_Data_Notification>, // 提及
    val followingNotifications: List<ld246_Response_Data_Notification>, // 关注
    val pagination: Pagination,
    val unreadNotificationCount: UnreadNotificationCount,
    // 其他可能存在的字段可以省略
)
data class ld246_Response_Data_Notification(
    val dataId: String,
    val authorName: String,
    val authorAvatarURL: String,
    val dataType: Int,
    val hasRead: Boolean,
    val title: String,
    val content: String,
    // 其他可能存在的字段可以省略
)

data class Pagination(
    val paginationPageCount: Int,
    val paginationPageNums: List<Int>
    // 其他可能存在的字段可以省略
)

data class UnreadNotificationCount(
    val unreadReviewNotificationCnt: Int,
    val unreadNotificationCnt: Int,
    // 其他可能存在的字段可以省略
)
