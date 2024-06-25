package sc.windom.sofill.Us

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.provider.Settings
import android.text.TextUtils
import android.webkit.ConsoleMessage
import com.tencent.bugly.crashreport.BuglyLog
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException


object U_DEBUG {
    /**
     * 将类的全名转换为使用斜杠的路径形式，并附加给定的文件名。
     */
    fun Any.srcPath(fileName: String): String {
        val className = this::class.java.name
        val packageName = className.substring(0, className.lastIndexOf('.'))
        val slashedPackageName = packageName.replace('.', '/')
        return "$slashedPackageName/$fileName"
    }
    @JvmStatic
    fun prettyConsoleMessage(consoleMessage: ConsoleMessage): String {
        val messageLevel = when (consoleMessage.messageLevel()) {
            ConsoleMessage.MessageLevel.TIP -> "T"
            ConsoleMessage.MessageLevel.LOG -> "I"
            ConsoleMessage.MessageLevel.WARNING -> "W"
            ConsoleMessage.MessageLevel.ERROR -> "E"
            ConsoleMessage.MessageLevel.DEBUG -> "D"
            else -> "Unknown"
        }

        // 根据消息级别决定是否包含行号和来源
        val lineNumberAndSource = if (consoleMessage.messageLevel() == ConsoleMessage.MessageLevel.ERROR) {
            " line ${consoleMessage.lineNumber()} in ${consoleMessage.sourceId()}"
        } else {
            ""
        }

        // 格式化输出
        return "$messageLevel ${consoleMessage.message()} $lineNumberAndSource"
    }
    /**
     * 检查当前应用程序是否为内测版或公测版，并且是否处于调试模式。
     *
     *
     * 该方法首先检查应用程序的标签（名称）是否包含“内测版”或“公测版”，然后检查应用程序是否设置了可调试标志。
     * 如果应用程序的标签包含“内测版”或“公测版”并且应用程序是可调试的，则返回true。
     *
     * @param context 应用程序上下文，用于获取包管理器和应用程序信息。
     * @return 如果应用程序是内测版或公测版并且处于调试模式，则返回true，否则返回false。
     */
    @JvmStatic
    fun isDebugPackageAndMode(context: Context): Boolean {
        val packageManager = context.packageManager
        var appInfo: ApplicationInfo? = null
        try {
            appInfo = packageManager.getApplicationInfo(context.packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            BuglyLog.e("logging", "check isDebugPackageAndMode failed", e)
        }

        // 获取应用名称
        val appName =
            if (appInfo != null) packageManager.getApplicationLabel(appInfo).toString() else ""
        val isDebugPackage = appName.contains("内测版") || appName.contains("公测版")
        val isDebugMode =
            appInfo != null && (appInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        return isDebugPackage && isDebugMode
    }
    /**
     * 获取进程号对应的进程名
     *
     * @param pid 进程号
     * @return 进程名
     */
    fun getProcessName(pid: Int): String? {
        var reader: BufferedReader? = null
        try {
            reader = BufferedReader(FileReader("/proc/$pid/cmdline"))
            var processName = reader.readLine()
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim { it <= ' ' }
            }
            return processName
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
        } finally {
            try {
                reader?.close()
            } catch (exception: IOException) {
                exception.printStackTrace()
            }
        }
        return null
    }

    @SuppressLint("HardwareIds")
    fun getAndroidId(context: Context): String {
        // 获取 Android ID
        val androidId: String =
            Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID)
        return androidId
    }
}