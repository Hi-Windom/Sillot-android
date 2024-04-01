package org.b3log.siyuan

import android.Manifest
import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.ActivityCompat
import kotlin.math.pow
import kotlin.math.sqrt

object Us {
    fun isMIUI(applicationContext : Context): Boolean {
        val packageManager = applicationContext.packageManager
        val miuiPackageName = "com.miui.gallery"
        return try {
            packageManager.getPackageInfo(miuiPackageName, PackageManager.GET_META_DATA)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun isLargeScreenMachine(context: Context): Boolean {
        // 获取屏幕的方向
        val screenLayout = context.resources.configuration.screenLayout
        // 获取屏幕尺寸的掩码
        val sizeMask = Configuration.SCREENLAYOUT_SIZE_MASK
        // 获取屏幕尺寸的值
        val screenSize = screenLayout and sizeMask

        // 如果屏幕尺寸是超大屏或者巨屏，则可能是平板电脑
        return screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE ||
                screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE
    }
    fun isPad(context: Context): Boolean { // Converted from Utils.java
        val metrics = context.resources.displayMetrics
        val widthInches = metrics.widthPixels / metrics.xdpi
        val heightInches = metrics.heightPixels / metrics.ydpi
        val diagonalInches = sqrt(widthInches.toDouble().pow(2.0) + heightInches.toDouble()
            .pow(2.0)
        )
        return diagonalInches >= 7
    }

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


    fun requestExternalStoragePermission(activity: Activity) {
        if (!canManageAllFiles(activity)) {
            val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            ActivityCompat.startActivityForResult(
                activity,
                intent,
                Ss.REQUEST_ExternalStorageManager,
                null
            )
        }
    }

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

    fun canAccessDeviceState(context: Context): Boolean { // 访问设备状态信息
        return context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) ==
                PackageManager.PERMISSION_GRANTED
    }

    fun isIgnoringBatteryOptimizations(context: Context): Boolean { // 忽略电池优化
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: false
    }

    fun isShowingOnLockScreen(context: Context): Boolean { // 锁屏显示
        val keyguardManager = context.getSystemService(
            KeyguardManager::class.java
        )
        return keyguardManager?.isDeviceLocked ?: false
    }


    fun canShowOnTop(context: Context?): Boolean { // 悬浮窗
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else true
        // Assuming it's allowed on older versions
    }

    fun canPopInBackground(context: Context?): Boolean { // 后台弹出界面
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Settings.canDrawOverlays(context)
        } else true
        // Assuming it's allowed on older versions
    }

    fun canRequestPackageInstalls(context: Context): Boolean { // 安装未知应用
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else true
        // Assuming it's allowed on older versions
    }

}