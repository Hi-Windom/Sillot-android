package org.b3log.siyuan.sillot

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import org.b3log.siyuan.App
import org.b3log.siyuan.sillot.util.ClipboardUtils
import org.b3log.siyuan.sillot.util.FileUtil
import java.text.DateFormat
import java.util.Date

/* loaded from: classes2.dex */
class CrashHandler(val context: Context) : Thread.UncaughtExceptionHandler {
    companion object {
        const val TAG = "CrashHandler"
    }

    private var defaultHandler: Thread.UncaughtExceptionHandler? = null

    init {
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    // java.lang.Thread.UncaughtExceptionHandler
    override fun uncaughtException(t: Thread, e: Throwable) {
        handleException(e)
        val uncaughtExceptionHandler = defaultHandler
        uncaughtExceptionHandler?.uncaughtException(t, e)
    }

    @Suppress("DEPRECATION")
    private fun handleException(e: Throwable) {
        if (App.isMainThread)
            Thread.sleep(2000)
        val packageName = this.context.packageName
        try {
            val packageInfo = this.context.packageManager.getPackageInfo(packageName, 1)
            val str = """
                ${DateFormat.getInstance().format(Date())}
                Version：${packageInfo.versionCode} (${packageInfo.versionName})
                Brand：${Build.BRAND}，Model：${Build.MODEL}，Android：${Build.VERSION.RELEASE}
                StackTrace：
                ${Log.getStackTraceString(e)}
                """.trimIndent()

            ClipboardUtils.copyText("AlistAndroid", str)
            FileUtil.writeFile(context.getExternalFilesDir("log").toString() + "/crash.log", str)
        } catch (e2: PackageManager.NameNotFoundException) {
            Log.e(TAG, "handleException: ", e2)
        }
    }
}