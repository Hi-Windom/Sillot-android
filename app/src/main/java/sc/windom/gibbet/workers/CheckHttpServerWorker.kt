/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/7/8 下午12:03
 * updated: 2024/7/8 下午12:03
 */

package sc.windom.gibbet.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.tencent.bugly.crashreport.BuglyLog
import mobile.Mobile
import sc.windom.sillot.App

class CheckHttpServerWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    private val TAG = "workers/CheckHttpServerWorker.kt"
    override fun doWork(): Result {
        val s = App.KernelService
        if (s != null) {
            BuglyLog.i(TAG, "check: Mobile.isHttpServing(): ${Mobile.isHttpServing()} || s.isHttpServerRunning(): ${s.isHttpServerRunning()}")
        }
        // 返回Result.success()表示任务成功完成
        return Result.success()
    }
}
