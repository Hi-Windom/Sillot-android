/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/7/15 上午10:54
 * updated: 2024/7/15 上午10:54
 */

package org.b3log.ld246

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Scale
import coil.size.Size
import sc.windom.sofill.Ss.S_Uri
import sc.windom.sofill.dataClass.ld246_User

@Composable
fun UserPage(user: ld246_User, _openURL: (url: String)-> Unit) {
    val Lcc = LocalContext.current
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        // 高斯模糊背景
        AsyncImage(
            model = user.userCardBImgURL,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp)
                .blur(radius = 20.dp) // 这里添加高斯模糊效果
        )
        // 这里可以放置其他内容，它们将显示在背景图片之上

        Column(
            modifier = Modifier
                .padding(6.dp),
        ) {
            // 用户头像和基本信息
            Row(
                modifier = Modifier
                    .padding(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clickable {
                            if (user.userName?.isNotBlank() == true) {
                                Lcc.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://${S_Uri.HOST_ld246}/member/${user.userName}")
                                    )
                                )
                            }
                        }, contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(user.userAvatarURL)
                            .size(Size(300, 300))
                            .scale(Scale.FILL)
                            .build(),
                        contentDescription = "User Avatar",
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape), // 使用圆形裁剪
                    )
                }
                Spacer(
                    modifier = Modifier
                        .width(16.dp)
                        .fillMaxWidth()
                )
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column {
                        user.userName?.let {
                            Text(
                                text = "$it (${user.userNickname})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            )
                        }
                        user.userIntro?.let {
                            Text(
                                text = it,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            Spacer(
                modifier = Modifier
                    .height(16.dp)
                    .fillMaxWidth()
            )
            UserProfileScreen(user, _openURL)
        }
    }
}

@Composable
private fun UserProfileScreen(user: ld246_User, _openURL: (url: String)-> Unit) {
    // 两列布局
    Row {
        // 左侧列
        Column(modifier = Modifier.weight(1f)) {
            user.userNo?.let {
                ProfileInfoItem(
                    "编号",
                    it
                ) { _openURL("https://${S_Uri.HOST_ld246}/member/${user.userName}") }
            }
            user.userArticleCount?.let {
                ProfileInfoItem(
                    "帖子",
                    it
                ) { _openURL("https://${S_Uri.HOST_ld246}/member/${user.userName}/articles") }
            }
            user.userCommentCount?.let {
                ProfileInfoItem(
                    "回帖",
                    it
                ) { _openURL("https://${S_Uri.HOST_ld246}/member/${user.userName}/comments") }
            }
            user.userComment2Count?.let {
                ProfileInfoItem(
                    "评论",
                    it
                ) { _openURL("https://${S_Uri.HOST_ld246}/member/${user.userName}/comment2s") }
            }
        }
        // 右侧列
        Column(modifier = Modifier.weight(1f)) {
            user.userPoint?.let {
                ProfileInfoItem(
                    "积分",
                    it
                ) { _openURL("https://${S_Uri.HOST_ld246}/member/${user.userName}/points") }
            }
            user.userGeneralRank?.let {
                ProfileInfoItem(
                    "综合贡献点",
                    it
                ) { _openURL("https://${S_Uri.HOST_ld246}/top/general") }
            }
            user.userCurrentCheckinStreak?.let {
                ProfileInfoItem(
                    "最近连签",
                    it
                ) { _openURL("https://${S_Uri.HOST_ld246}/activity/checkin") }
            }
            user.userLongestCheckinStreak?.let {
                ProfileInfoItem(
                    "最长连签",
                    it
                ) { _openURL("https://${S_Uri.HOST_ld246}/activity/checkin") }
            }
        }
    }
}


@Composable
private fun ProfileInfoItem(title: String, value: Any, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(38.dp)
            .clickable(onClick = onClick)
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value.toString(),
            fontStyle = FontStyle.Italic,
            fontSize = 18.sp
        )
    }
}