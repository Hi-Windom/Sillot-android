/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/7/8 上午5:39
 * updated: 2024/7/8 上午5:39
 */

package org.b3log.siyuan.sillot.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import sc.windom.namespace.SillotMatrix.R
import org.b3log.siyuan.sillot.service.AListService

class SwitchServerActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (AListService.isRunning) {
//            toast(R.string.alist_shut_downing)
            startService(Intent(this, AListService::class.java).apply {
                action = AListService.ACTION_SHUTDOWN
            })
        } else {
//            toast(R.string.alist_starting)
            startService(Intent(this, AListService::class.java))
        }

        finish()
    }
}