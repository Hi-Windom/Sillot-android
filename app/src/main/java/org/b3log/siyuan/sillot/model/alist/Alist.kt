/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/7/8 上午5:50
 * updated: 2024/7/8 上午5:50
 */

package org.b3log.siyuan.sillot.model.alist

import android.annotation.SuppressLint
import android.util.Log
import sc.windom.sillot.app
import org.b3log.siyuan.sillot.service.AListService
import org.b3log.siyuan.sillot.util.FileUtils.readAllText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import java.io.File
import java.io.IOException
import kotlin.coroutines.coroutineContext

object AList {
    private val execPath by lazy {
        context.applicationInfo.nativeLibraryDir + File.separator + "libalist.so"
    }

    val context = app

    val dataPath: String
        get() = context.getExternalFilesDir("data")?.absolutePath!!

    val configPath: String
        get() = "$dataPath${File.separator}config.json"


    fun setAdminPassword(pwd: String) {
        val log = execWithParams(
            redirect = true,
            params = arrayOf("admin", "set", pwd, "--data", dataPath)
        ).inputStream.readAllText()
//        appDb.serverLogDao.insert(ServerLog(level = LogLevel.INFO, message = log.removeAnsiCodes()))
    }


    fun shutdown() {
        runCatching {
            mProcess?.destroy()
        }.onFailure {
//            context.longToast(R.string.server_shutdown_failed, it.toString())
        }
    }

    private var mProcess: Process? = null

    private suspend fun errorLogWatcher(onNewLine: (String) -> Unit) {
        mProcess?.apply {
            errorStream.bufferedReader().use {
                while (coroutineContext.isActive) {
                    val line = it.readLine() ?: break
                    Log.d(AListService.TAG, "Process errorStream: $line")
                    onNewLine(line)
                }
            }
        }
    }

    private suspend fun logWatcher(onNewLine: (String) -> Unit) {
        mProcess?.apply {
            inputStream.bufferedReader().use {
                while (coroutineContext.isActive) {
                    val line = it.readLine() ?: break
                    Log.d(AListService.TAG, "Process inputStream: $line")
                    onNewLine(line)
                }
            }
        }
    }

    private val mScope = CoroutineScope(Dispatchers.IO + Job())
//    private fun initOutput() {
//        val dao = appDb.serverLogDao
//        mScope.launch {
//            runCatching {
//                logWatcher { msg ->
//                    msg.removeAnsiCodes().evalLog()?.let {
//                        dao.insert(
//                            ServerLog(
//                                level = it.level,
//                                message = it.message
//                            )
//                        )
//                        return@logWatcher
//                    }
//
//                    dao.insert(
//                        ServerLog(
//                            level = if (msg.startsWith("fail")) LogLevel.ERROR else LogLevel.INFO,
//                            message = msg
//                        )
//                    )
//
//                }
//            }.onFailure {
//                it.printStackTrace()
//            }
//        }
//        mScope.launch {
//            runCatching {
//                errorLogWatcher { msg ->
//                    val log = msg.removeAnsiCodes().evalLog() ?: return@errorLogWatcher
//                    dao.insert(
//                        ServerLog(
//                            level = log.level,
//                            message = log.message,
////                            description = log.time + "\n" + log.code
//                        )
//                    )
//                }
//            }.onFailure {
//                it.printStackTrace()
//            }
//        }
//    }


    @SuppressLint("SdCardPath")
    fun startup(
        dataFolder: String = context.getExternalFilesDir("data")?.absolutePath
            ?: "/data/data/${context.packageName}/files/data"
    ): Int {
        mProcess =
            execWithParams(params = arrayOf("server", "--data", dataFolder))
//        initOutput()

        return mProcess!!.waitFor()
    }


    private fun execWithParams(
        redirect: Boolean = false,
        vararg params: String
    ): Process {
        val cmdline = arrayOfNulls<String>(params.size + 1)
        cmdline[0] = execPath
        System.arraycopy(params, 0, cmdline, 1, params.size)
        return ProcessBuilder(*cmdline).redirectErrorStream(redirect).start()
            ?: throw IOException("Process is null!")
    }
}