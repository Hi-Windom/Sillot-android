/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/8/17 13:25
 * updated: 2024/8/17 13:25
 */

package sc.windom.sofill.permission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import org.b3log.siyuan.OnSiYuanMainRequestEvent
import org.greenrobot.eventbus.EventBus
import sc.windom.sofill.Ss.REQUEST_OVERLAY
import sc.windom.sofill.Us.Toast


class Overlay : AppCompatActivity() {
    private lateinit var requestOverlayPermissionLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appContext: Context = applicationContext
        Toast.Show(appContext, "找到汐洛并允许显示悬浮窗")
        requestOverlayPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (Settings.canDrawOverlays(appContext)) {
                Toast.Show(appContext, "已获取显示悬浮窗权限")
                // 发送事件，将权限请求的结果发送出去
                EventBus.getDefault().post(
                    OnSiYuanMainRequestEvent(
                        REQUEST_OVERLAY,
                        RESULT_OK,
                        "showwifi"
                    )
                )
            } else {
                Toast.Show(appContext, "未获取显示悬浮窗权限")
                EventBus.getDefault().post(
                    OnSiYuanMainRequestEvent(
                        REQUEST_OVERLAY,
                        RESULT_CANCELED,
                        ""
                    )
                )
            }
            finish()
        }

        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.SYSTEM_ALERT_WINDOW
            )
        ) {
            Toast.Show(appContext, "请允许显示悬浮窗权限以实现某些功能")
            finish()
        } else {
            if (!Settings.canDrawOverlays(appContext)) {
                requestOverlayPermission()
            } else {
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
