package org.b3log.siyuan.ld246

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.delay
import org.b3log.siyuan.CascadeMaterialTheme
import org.b3log.siyuan.Us
import org.b3log.siyuan.compose.components.CommonTopAppBar

class ManageSpaceActivity : ComponentActivity() {
    //     将"清除数据"项变为"管理空间"，自定义数据清除    https://github.com/Hi-Windom/Sillot-android/issues/49
    val TAG = "ManageSpaceActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 设置沉浸式通知栏
        window.setDecorFitsSystemWindows(false)
        window.decorView.setOnApplyWindowInsetsListener { _, insets ->
            insets
        }
        setContent {
            CascadeMaterialTheme {
                UI(intent)
            }
        }

//设置竖屏锁定
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
}




@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, DelicateCoroutinesApi::class)
@Composable
private fun UI(intent: Intent?) {
    val TAG = "MainPro-MyUI"
    val uri = intent?.data
    val Lcc = LocalContext.current
    val inspectionMode = LocalInspectionMode.current // 获取当前是否处于预览模式// 获取窗口尺寸
    val coroutineScope = rememberCoroutineScope()
    val fileName = uri?.let { Us.getFileName(Lcc, it) }
    val fileSize = uri?.let { Us.getFileSize(Lcc, it) }
    val mimeType = intent?.data?.let { Us.getMimeType(Lcc, it) } ?: ""
    val fileType = fileName?.let { Us.getFileMIMEType(mimeType, it) } ?: run { Us.getFileMIMEType(mimeType) }
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE // 是否横屏（宽高比）

    var isMenuVisible by rememberSaveable { mutableStateOf(false) }
    var itemCount by remember { mutableStateOf(15) }
    val state = rememberPullToRefreshState()
    if (state.isRefreshing) {
        LaunchedEffect(true) {
            // fetch something
            delay(1500)
            itemCount += 5
            state.endRefresh()
        }
    }

    Scaffold(
        topBar = {
            CommonTopAppBar("汐洛链滴社区客户端", uri) {
                // 将Context对象安全地转换为Activity
                if (Lcc is Activity) {
                    Lcc.finish() // 结束活动
                }
            }
        }, modifier = Modifier.background(Color.Gray).nestedScroll(state.nestedScrollConnection)
    ) {
        Box(Modifier.padding(it).fillMaxSize()) {
            LazyColumn(Modifier.fillMaxSize()) {
                if (!state.isRefreshing) {
                    items(itemCount) {
//                        ListItem({
//                            Text(text = "Item ${itemCount - it}")
//                        })
                        NotificationsList(收到的回帖消息列表)
                    }
                }
            }
            if (state.isRefreshing) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            } else {
                LinearProgressIndicator(progress = { state.progress }, Modifier.fillMaxWidth())
            }
        }
//        NotificationsList(收到的回帖消息列表)
    }
}


@Composable
fun NotificationsList(notifications: List<回帖消息>) {
    Column {
        notifications.forEach { notification ->
            NotificationCard(notification)
        }
    }
}

@Composable
fun NotificationCard(notification: 回帖消息) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = notification.title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(text = "By ${notification.authorName}", fontSize = 16.sp, fontStyle = FontStyle.Italic)
            Text(text = notification.content, fontSize = 14.sp)
            Text(text = "Read: ${notification.hasRead}", fontSize = 12.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    UI(null)
}
