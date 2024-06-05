package org.b3log.siyuan.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import mobile.Mobile
import org.b3log.siyuan.services.BootService

class CheckHttpServerWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        if (!Mobile.isHttpServing()) {
            // 如果没有运行，重启kernel
            (applicationContext as BootService).bootKernel(true)
        }
        // 返回Result.success()表示任务成功完成
        return Result.success()
    }
}
