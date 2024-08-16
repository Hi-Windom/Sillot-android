/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/8/16 18:18
 * updated: 2024/8/16 18:18
 */

package sc.windom.sofill.android.lifecycle.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import sc.windom.sofill.android.lifecycle.AppMonitor

/**
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 */
internal class ScreenActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_SCREEN_ON -> {//开屏
                AppMonitor.mOnScreenStatusCallbacks.forEach {
                    it.onScreenStatusChanged(true)
                }
            }

            Intent.ACTION_SCREEN_OFF -> {//关屏
                AppMonitor.mOnScreenStatusCallbacks.forEach {
                    it.onScreenStatusChanged(false)
                }
            }

            Intent.ACTION_USER_PRESENT -> {//解锁（解锁键盘消失）
                AppMonitor.mOnScreenStatusCallbacks.forEach {
                    it.onUserPresent()
                }
            }
        }
    }
}