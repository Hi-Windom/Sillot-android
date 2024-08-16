/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/8/12 04:43
 * updated: 2024/8/12 04:43
 */

package sc.windom.sillot.workers

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.work.Worker
import androidx.work.WorkerParameters

class ActivityRunInBgWorker(private val context: Context, params: WorkerParameters) : Worker(context, params) {
    private val TAG = "workers/ActivityRunInBgWorker.kt"
    @SuppressLint("ServiceCast")
    override fun doWork(): Result {
        val activityClassName = inputData.getString("activity")
        val matrixModel = inputData.getString("matrixModel")
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningTasks = activityManager.getRunningTasks(Integer.MAX_VALUE)
        for (task in runningTasks) {
            task.topActivity?.let {
//                Log.w(TAG, "${it.className} $activityClassName")
                if (
                    activityClassName == it.className // Activity处于栈顶
                    ) {
                    // 使用Handler将Toast显示操作发送到主线程
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context.applicationContext, "${matrixModel}进入后台运行", Toast.LENGTH_SHORT).show()
                    }
                    return Result.success()
                }
            }
        }
        return Result.retry()
    }
}
