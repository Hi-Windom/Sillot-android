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
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.core.app.ActivityCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.kongzue.dialogx.dialogs.PopTip
import org.b3log.siyuan.andapi.Toast

class Overlay : AppCompatActivity() {
    private lateinit var requestOverlayPermissionLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appContext: Context = applicationContext

        requestOverlayPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (Settings.canDrawOverlays(appContext)) {
                Toast.Show(appContext, "已获取显示悬浮窗权限")
            } else {
                Toast.Show(appContext, "未获取显示悬浮窗权限")
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
