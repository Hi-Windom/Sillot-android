package org.b3log.siyuan.compose

import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext


/**
 * 用于 Composable 的屏幕锁定
 *
 * @param orientation Int 类型，一般是 ActivityInfo.SCREEN_ORIENTATION_*
 */
@Composable
fun LockScreenOrientation(orientation: Int) {
    val activity = (LocalContext.current as? ComponentActivity)
    DisposableEffect(activity) {
        activity?.requestedOrientation = orientation
        onDispose {
            // Reset the orientation to the system settings when the DisposableEffect is disposed
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }
}

fun ComponentActivity.lockScreenOrientation(orientation: Int) {
    requestedOrientation = orientation
}