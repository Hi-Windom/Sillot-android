package org.b3log.siyuan.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.tencent.bugly.crashreport.BuglyLog
import mobile.Mobile
import org.b3log.siyuan.App

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
