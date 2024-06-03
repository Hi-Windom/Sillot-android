package org.b3log.siyuan

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager

class WifiStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (WifiManager.NETWORK_STATE_CHANGED_ACTION == action) {
            // 处理WiFi状态变化的逻辑
        }
    }
}
