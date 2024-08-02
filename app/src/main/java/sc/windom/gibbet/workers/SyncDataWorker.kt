/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/8/2 18:07
 * updated: 2024/8/2 18:07
 */

package sc.windom.gibbet.workers

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.koushikdutta.async.http.AsyncHttpClient
import com.koushikdutta.async.http.AsyncHttpPost
import com.koushikdutta.async.http.AsyncHttpResponse
import com.koushikdutta.async.http.body.JSONObjectBody
import org.b3log.siyuan.Utils
import org.json.JSONObject
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean

class SyncDataWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    private val TAG = "workers/SyncDataWorker.kt"
    override fun doWork(): Result {
        val latch = CountDownLatch(1)
        var workResult: Result? = null
        // 实现数据同步的逻辑
        syncData { result ->
            workResult = result
            latch.countDown()
        }
        latch.await() // 等待直到latch的计数变为0
        return workResult ?: Result.failure()
    }
    // 虑在多线程环境中的同步问题。可以使用AtomicBoolean来确保同步状态的更新是原子的
    private val syncing = AtomicBoolean(false)

    private fun syncData(callback: (Result) -> Unit) {
        try {
            if (syncing.get()) {
                Log.i(TAG, "data is syncing...")
                callback(Result.failure())
            }
            syncing.set(true)

            val req = AsyncHttpPost("http://127.0.0.1:58131/api/sync/performSync")
            req.setBody(JSONObjectBody(JSONObject().put("mobileSwitch", true)))
            AsyncHttpClient.getDefaultInstance().executeJSONObject(req, object :
                AsyncHttpClient.JSONObjectCallback() {
                override fun onCompleted(e: Exception?, source: AsyncHttpResponse?, result: JSONObject?) {
                    if (e == null) {
                        callback(Result.success())
                    } else {
                        Utils.LogError(TAG, "data sync failed", e)
                        callback(Result.failure())
                    }
                }
            })
        } catch (e: Throwable) {
            Utils.LogError(TAG, "data sync failed", e)
            callback(Result.failure())
        } finally {
            syncing.set(false)
        }
    }
}
