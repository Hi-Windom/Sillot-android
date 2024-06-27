package sc.windom.sofill.Us

import android.Manifest
import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.kongzue.dialogx.dialogs.PopTip
import sc.windom.sofill.S

object U_Permission {
    /**
     * @param id 对应的是具体的类，在 permission 文件夹，没有事先创建则会报错
     */
    @JvmStatic
    fun requestPermissionActivity(
        context: Context,
        id: String,
        Msg: String?
    ) {
        if (id == "Battery") {
            val battery = Intent("sc.windom.sillot.intent.permission.$id")
            battery.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            // 获取 Application Context 并启动 Activity
            context.applicationContext.startActivity(battery)
        }
        if (!Msg.isNullOrEmpty()) {
            PopTip.show(Msg)
        }
    }

    @JvmStatic
    fun hasBatteryOptimizationPermission(context: Context): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    @JvmStatic
    fun isValidPermission(id: String?): Boolean { // Converted from Utils.java
        if (id.isNullOrEmpty()) {
            return false
        }
        try {
            // 使用反射获取 Manifest.permission 类中的所有静态字段
            val fields = Manifest.permission::class.java.getFields()
            for (field in fields) {
                // 检查是否存在与id匹配的静态字段
                if (field.type == String::class.java && field[null] == id) {
                    return false
                }
            }
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
            return false
        }
        return true
    }

    @JvmStatic
    fun requestExternalStoragePermission(activity: Activity) {
        if (!canManageAllFiles(activity)) {
            val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            ActivityCompat.startActivityForResult(
                activity,
                intent,
                S.REQUEST_CODE.REQUEST_CODE_MANAGE_STORAGE,
                null
            )
        }
    }

    @JvmStatic
    fun canManageAllFiles(context: Context): Boolean { // 管理所有文件
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED &&
                context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED
        // On older versions, we assume that the READ_EXTERNAL_STORAGE and WRITE_EXTERNAL_STORAGE
        // permissions are sufficient to manage all files.
    }

    @JvmStatic
    fun canAccessDeviceState(context: Context): Boolean { // 访问设备状态信息
        return context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) ==
                PackageManager.PERMISSION_GRANTED
    }

    @JvmStatic
    fun isIgnoringBatteryOptimizations(context: Context): Boolean { // 忽略电池优化
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    @JvmStatic
    fun isShowingOnLockScreen(context: Context): Boolean { // 锁屏显示
        val keyguardManager = context.getSystemService(
            KeyguardManager::class.java
        )
        return keyguardManager?.isDeviceLocked ?: false
    }

    @JvmStatic
    fun hasPermission_FOREGROUND_SERVICE_LOCATION(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            "android.permission.FOREGROUND_SERVICE_LOCATION"
        ) == PackageManager.PERMISSION_GRANTED
    }

    @JvmStatic
    fun canShowOnTop(context: Context?): Boolean { // 悬浮窗
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else true
        // Assuming it's allowed on older versions
    }

    @JvmStatic
    fun canPopInBackground(context: Context?): Boolean { // 后台弹出界面
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Settings.canDrawOverlays(context)
        } else true
        // Assuming it's allowed on older versions
    }

    @JvmStatic
    fun canRequestPackageInstalls(context: Context): Boolean { // 安装未知应用
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else true
        // Assuming it's allowed on older versions
    }
}