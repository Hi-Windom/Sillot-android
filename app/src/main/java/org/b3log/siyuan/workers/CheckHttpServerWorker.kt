package org.b3log.siyuan.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.blankj.utilcode.util.ServiceUtils.stopService
import mobile.Mobile

class CheckHttpServerWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        if (!Mobile.isHttpServing()) {
            // 如果没有运行，重启
            stopService("BootService")
        }
        // 返回Result.success()表示任务成功完成
        return Result.success()
    }
}
