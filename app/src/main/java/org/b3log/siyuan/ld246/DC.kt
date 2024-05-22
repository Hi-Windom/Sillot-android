package org.b3log.siyuan.ld246

data class 回帖消息(
    val dataId: String,
    val authorName: String,
    val hasRead: Boolean,
    val title: String,
    val content: String
)
data class 回复消息(
    val dataId: String,
    val authorName: String,
    val hasRead: Boolean,
    val title: String,
    val content: String
)
data class 提及我的消息(
    val dataId: String,
    val authorName: String,
    val hasRead: Boolean,
    val title: String,
    val content: String
)
data class 我关注的消息(
    val dataId: String,
    val authorName: String,
    val hasRead: Boolean,
    val title: String,
    val content: String
)