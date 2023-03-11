package org.b3log.siyuan.producer

import android.app.Activity
import android.os.Bundle

class MainPro: Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val intent = intent

        val uri = intent.data

        if (uri != null) {
            val scheme = uri.scheme
            val host = uri.host
            val port: String = uri.port.toString()
            val path = uri.path
            val query = uri.query
        }
        //...

        finish()
    }
}