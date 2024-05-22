package org.b3log.siyuan.ld246

data class 回帖消息Response(
    val msg: String,
    val random: String,
    val code: Int,
    val data: 回帖消息Response_Data,
    // 其他可能存在的字段可以省略
)

data class 回帖消息Response_Data(
    val commentedNotifications: List<回帖消息Response_Notification>,
    val pagination: Pagination,
    val unreadNotificationCount: UnreadNotificationCount,
    // 其他可能存在的字段可以省略
)

data class 回帖消息Response_Notification(
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
