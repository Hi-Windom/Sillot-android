package sc.windom.sofill.Us

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import android.text.TextUtils
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException


object U_DEBUG {
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