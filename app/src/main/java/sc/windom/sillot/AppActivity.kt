/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/8/16 20:32
 * updated: 2024/8/16 20:32
 */

package sc.windom.sillot

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.tencent.bugly.crashreport.BuglyLog
import sc.windom.sofill.Us.thisSourceFilePath
import sc.windom.sofill.annotations.SillotActivity
import sc.windom.sofill.annotations.SillotActivityType

@SillotActivity(SillotActivityType.Launcher)
@SillotActivity(SillotActivityType.UseInVisible)
class AppActivity : ComponentActivity() {
    private val TAG = "AppActivity.kt"
    private val srcPath = thisSourceFilePath(TAG)

    override fun onSaveInstanceState(outState: Bundle) {
        BuglyLog.d(TAG, "outState: $outState")
        if (outState.isEmpty) return // avoid crash
        super.onSaveInstanceState(outState)
        // 可添加额外需要保存可序列化的数据
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        BuglyLog.d(TAG, "onNewIntent() invoked. @ $intent")
        init(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BuglyLog.d(TAG, "onCreate() invoked. @ $intent")
        init(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        BuglyLog.d(TAG, "onDestroy() invoked")
    }

    private fun init(in2intent: Intent?) {
        BuglyLog.d(TAG, "in2intent: $in2intent")
        appIns.startTargetActivity()
        finishAndRemoveTask()
    }
}