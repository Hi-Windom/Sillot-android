package org.b3log.siyuan.permission

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS

class Battery: Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isIgnoringBatteryOptimizations()) {
            //未加入电池优化的白名单 则弹出系统弹窗供用户选择(这个弹窗也是一个页面)
            requestIgnoreBatteryOptimizations();
        }else{
            //已加入电池优化的白名单 则进入耗电优化页面
            // 还不知道怎么判断是否关闭了耗电优化，后面放在软件设置中通过按钮手动跳转
//            val powerUsageIntent = Intent(ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
//            val resolveInfo = packageManager.resolveActivity(powerUsageIntent, 0)
//            //判断系统是否有这个页面
//            if (resolveInfo != null) {
//                startActivity(powerUsageIntent)
//            }
        }
        finish()
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}