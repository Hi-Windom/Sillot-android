/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/7/11 下午10:57
 * updated: 2024/7/11 下午10:57
 */

package sc.windom.sillot

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.tencent.bugly.crashreport.BuglyLog
import org.b3log.siyuan.MainActivity
import sc.windom.sofill.Us.thisSourceFilePath
import sc.windom.sofill.annotations.SillotActivity
import sc.windom.sofill.annotations.SillotActivityType

@SillotActivity(SillotActivityType.Launcher)
@SillotActivity(SillotActivityType.UseInVisible)
class AppActivity : ComponentActivity() {
    private val TAG = "AppActivity.kt"
    private val srcPath = thisSourceFilePath(TAG)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var intent = App.currentIntentRef.get()
        if (intent == null) {
            intent = Intent(this, MainActivity::class.java)
            intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        or Intent.FLAG_ACTIVITY_NEW_DOCUMENT
                        or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
            )
        }
        BuglyLog.d(TAG, "intent ${intent}")
        startActivity(intent)
        finishAndRemoveTask()
    }

    override fun onDestroy() {
        super.onDestroy()
        BuglyLog.d(TAG, "onDestroy() invoked")
    }
}