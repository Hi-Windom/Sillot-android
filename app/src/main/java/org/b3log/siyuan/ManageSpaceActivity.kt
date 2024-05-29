package org.b3log.siyuan

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.DelicateCoroutinesApi
import sc.windom.sofill.compose.components.CommonTopAppBar
import sc.windom.sofill.compose.theme.CascadeMaterialTheme

// TODO: 仿哔哩哔哩界面
// TODO: 实际清理逻辑
class ManageSpaceActivity : ComponentActivity() {
//     将"清除数据"项变为"管理空间"，自定义数据清除    https://github.com/Hi-Windom/Sillot-android/issues/49
    val TAG = "ManageSpaceActivity.kt"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 设置沉浸式通知栏
        window.setDecorFitsSystemWindows(false)
        window.decorView.setOnApplyWindowInsetsListener { _, insets ->
            insets
        }
        setContent {
            CascadeMaterialTheme {
                UI(intent, TAG)
            }
        }

//设置竖屏锁定
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, DelicateCoroutinesApi::class)
@Composable
private fun UI(intent: Intent?, TAG: String) {
    val uri = intent?.data
    val Lcc = LocalContext.current
    var isMenuVisible = rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CommonTopAppBar("汐洛存储清理助手", TAG, uri, isMenuVisible) {
                // 将Context对象安全地转换为Activity
                if (Lcc is Activity) {
                    Lcc.finish() // 结束活动
                }
            }
        }, modifier = Modifier.background(Color.Gray)
    ) {

    }
}
