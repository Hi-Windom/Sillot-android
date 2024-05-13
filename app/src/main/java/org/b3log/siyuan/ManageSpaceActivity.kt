package org.b3log.siyuan

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import org.b3log.siyuan.producer.MyUI

// TODO: 仿哔哩哔哩界面
// TODO: 实际清理逻辑
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
                // 改写，抽取重用
                MyUI(intent)
            }
        }

//设置竖屏锁定
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
}