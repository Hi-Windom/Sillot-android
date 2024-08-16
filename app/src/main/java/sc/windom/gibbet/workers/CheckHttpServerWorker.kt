/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/8/12 05:07
 * updated: 2024/8/12 05:07
 */

package sc.windom.gibbet.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.tencent.bugly.crashreport.BuglyLog
import mobile.Mobile

class CheckHttpServerWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    private val TAG = "workers/CheckHttpServerWorker.kt"
    override fun doWork(): Result {
        Mobile.isHttpServing().let {
            BuglyLog.i(TAG, "check: Mobile.isHttpServing(): $it")
            if (it) return Result.success()
        }
        return Result.failure()
    }
}
