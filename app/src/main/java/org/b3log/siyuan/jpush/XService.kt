package org.b3log.siyuan.jpush

import android.app.Service
import android.content.Intent
import android.os.IBinder

class XService : Service() {
    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}