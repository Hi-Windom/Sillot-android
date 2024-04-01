package org.b3log.siyuan.sillot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.b3log.siyuan.sillot.service.AListService

class BootStartReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            context.startService(Intent(context, AListService::class.java))
        }
    }
}