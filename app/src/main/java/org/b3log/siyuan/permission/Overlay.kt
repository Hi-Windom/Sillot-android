package org.b3log.siyuan.permission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import org.b3log.siyuan.andapi.Toast

class Overlay : AppCompatActivity() {
    //    在应用清单声明对应 activity
    private lateinit var requestOverlayPermissionLauncher: ActivityResultLauncher<Intent>
    private lateinit var mContext: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = applicationContext

        requestOverlayPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (Settings.canDrawOverlays(mContext)) {
                // 用户同意了显示悬浮窗权限
                Toast.Show(mContext, "已获取显示悬浮窗权限")
                finish()
            } else {
                // 用户拒绝了显示悬浮窗权限
                Toast.Show(mContext, "未获取显示悬浮窗权限")
                finish()
            }
        }

        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.SYSTEM_ALERT_WINDOW
            )
        ) {
            // 用户拒绝过这个权限了，应该提示用户，为什么需要这个权限。
            Toast.Show(mContext, "请允许显示悬浮窗权限以实现某些功能")
            finish()
        } else {
            if (!Settings.canDrawOverlays(mContext)) {
                // 未获取显示悬浮窗权限，则弹出系统弹窗供用户选择
                requestOverlayPermission()
                // 这里不能结束
            } else {
                // 已获取显示悬浮窗权限，跳过
                finish()
            }
        }
    }

    private fun requestOverlayPermission() {
        try {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            intent.data = Uri.parse("package:$packageName")
            requestOverlayPermissionLauncher.launch(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
