package org.b3log.siyuan.permission

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.kongzue.dialogx.dialogs.PopTip
import org.b3log.siyuan.andapi.Toast


class Battery : AppCompatActivity() {
//    在应用清单声明对应 activity
    private lateinit var requestIgnoreBatteryOptimizationsLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mContext: Context = applicationContext
        requestIgnoreBatteryOptimizationsLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK || isIgnoringBatteryOptimizations()) {
                // 用户同意了加入电池优化的白名单
                Toast.Show(mContext, "已加入电池优化的白名单")
            } else {
                // 用户拒绝了加入电池优化的白名单，或者没有作出选择
                Toast.Show(mContext, "未加入电池优化的白名单")
            }
            // 用户已经做出选择，现在可以结束 Activity
            finish()
        }

        if (!isIgnoringBatteryOptimizations()) {
            // 未加入电池优化的白名单，则弹出系统弹窗供用户选择
            requestIgnoreBatteryOptimizations()
        } else {
            // 已加入电池优化的白名单，直接结束 Activity
            finish()
        }
    }


    private fun isIgnoringBatteryOptimizations(): Boolean {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(packageName)
    }

    @SuppressLint("BatteryLife")
    private fun requestIgnoreBatteryOptimizations() {
        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS) // REF https://developer.android.com/reference/android/provider/Settings#ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            intent.data = Uri.parse("package:$packageName")
            requestIgnoreBatteryOptimizationsLauncher.launch(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}
