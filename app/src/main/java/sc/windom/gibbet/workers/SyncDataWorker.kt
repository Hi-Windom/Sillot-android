/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/8/15 04:20
 * updated: 2024/8/15 04:20
 */

package sc.windom.gibbet.workers

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.util.InternalAPI
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.b3log.siyuan.Utils
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean

// 定义请求数据的模型
@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class SyncRequest(val mobileSwitch: Boolean = true)

// 定义回调接口
interface SyncCallback {
    fun onSuccess()
    fun onFailure(e: Exception?)
}



class SyncDataWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    private val TAG = "workers/SyncDataWorker.kt"
    override fun doWork(): Result = runBlocking {
        val latch = CountDownLatch(1)
        var workResult: Result? = null
        // 实现数据同步的逻辑
        syncData { result ->
            workResult = result
            latch.countDown()
        }
        latch.await() // 等待直到latch的计数变为0
        return@runBlocking workResult ?: Result.failure()
    }

    // 虑在多线程环境中的同步问题。可以使用AtomicBoolean来确保同步状态的更新是原子的
    private val syncing = AtomicBoolean(false)

    private suspend fun syncData(callback: (Result) -> Unit) {
        try {
            if (syncing.get()) {
                Log.i(TAG, "data is syncing...")
                callback(Result.failure())
            }
            syncing.set(true)

            performSync(object : SyncCallback {
                override fun onSuccess() {
                    Utils.LogInfo(TAG, "syncData success")
                    callback(Result.success())
                }

                override fun onFailure(e: Exception?) {
                    Utils.LogError(TAG, "data sync failed", e)
                    callback(Result.failure())
                }
            })
        } catch (e: Throwable) {
            Utils.LogError(TAG, "data sync failed", e)
            callback(Result.failure())
        } finally {
            syncing.set(false)
        }
    }

    @OptIn(InternalAPI::class)
    private suspend fun performSync(callback: SyncCallback) {
        val client = HttpClient(CIO) // 使用 CIO 引擎
        val url = "http://127.0.0.1:58131/api/sync/performSync"
        val request = SyncRequest(mobileSwitch = true)

        try {
            val response: io.ktor.client.statement.HttpResponse = client.post(url) {
                contentType(ContentType.Application.Json)
                body = Json.encodeToString(SyncRequest.serializer(), request)
            }

            if (response.status == HttpStatusCode.OK) {
                callback.onSuccess()
            } else {
                callback.onFailure(e = null)
            }
        } catch (e: Exception) {
            Utils.LogError(TAG, "data sync failed", e)
            callback.onFailure(e)
        } finally {
            client.close()
        }
    }
}
