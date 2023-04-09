package org.b3log.siyuan.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.ActivityCompat
import org.b3log.siyuan.andapi.Toast

class Battery: Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)) {
            // 用户拒绝过这个权限了，应该提示用户，为什么需要这个权限。
            val mContext: Context = applicationContext
            Toast.Show(mContext,"伺服体验不完整")
        } else {
        }
        if (!isIgnoringBatteryOptimizations()) {
            //未加入电池优化的白名单 则弹出系统弹窗供用户选择(这个弹窗也是一个页面)
            requestIgnoreBatteryOptimizations();
        }else{
            //已加入电池优化的白名单 则进入耗电优化页面进一步设置
            // 还不知道怎么判断是否关闭了耗电优化，后面放在软件设置中通过按钮手动跳转
//            val powerUsageIntent = Intent(ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
//            val resolveInfo = packageManager.resolveActivity(powerUsageIntent, 0)
//            //判断系统是否有这个页面
//            if (resolveInfo != null) {
//                startActivity(powerUsageIntent)
//            }
        }
        val mContext: Context = applicationContext
        Toast.Show(mContext,"请手动重启应用")
        finish() // 完成当前 activity
    }


    private fun isIgnoringBatteryOptimizations(): Boolean {
        var isIgnoring = false
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        isIgnoring = powerManager.isIgnoringBatteryOptimizations(packageName)
        return isIgnoring
    }

    private fun requestIgnoreBatteryOptimizations() {
        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
//            var mContext: Context = applicationContext
//            Toast.Show(mContext,"后台稳定伺服需要额外权限，请允许")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}