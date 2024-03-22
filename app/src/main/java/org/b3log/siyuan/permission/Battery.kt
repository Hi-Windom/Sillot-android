package org.b3log.siyuan.permission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.os.SystemClock.sleep
import android.provider.Settings
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import org.b3log.siyuan.MainActivity
import org.b3log.siyuan.andapi.Toast


class Battery : AppCompatActivity() {
    private lateinit var requestIgnoreBatteryOptimizationsLauncher: ActivityResultLauncher<Intent>
    private val activity: MainActivity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mContext: Context = applicationContext

        requestIgnoreBatteryOptimizationsLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                // 用户同意了加入电池优化的白名单
                Toast.Show(mContext, "已加入电池优化的白名单")
                finish()
            } else {
                // 用户拒绝了加入电池优化的白名单
                Toast.Show(mContext, "未加入电池优化的白名单")
                finish() // 完成当前 activity
            }
        }

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)) {
            // 用户拒绝过这个权限了，应该提示用户，为什么需要这个权限。
            Toast.Show(mContext, "伺服体验不完整")
            finish() // 完成当前 activity
        } else {
            if (!isIgnoringBatteryOptimizations()) {
                // 未加入电池优化的白名单，则弹出系统弹窗供用户选择，后续判断则在上面哈哈哈
                requestIgnoreBatteryOptimizations()
                // 这里不能结束
            } else {
                // 已加入电池优化的白名单，跳过
                finish()
            }
        }

    }

    private fun isIgnoringBatteryOptimizations(): Boolean {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(packageName)
    }

    private fun requestIgnoreBatteryOptimizations() {
        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.data = Uri.parse("package:$packageName")
            requestIgnoreBatteryOptimizationsLauncher.launch(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}
