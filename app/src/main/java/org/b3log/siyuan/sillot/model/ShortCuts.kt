/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/7/8 上午5:39
 * updated: 2024/7/8 上午5:39
 */

package org.b3log.siyuan.sillot.model

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import sc.windom.namespace.SillotMatrix.R
import org.b3log.siyuan.sillot.ui.SwitchServerActivity


object ShortCuts {
    private inline fun <reified T> buildIntent(context: Context): Intent {
        val intent = Intent(context, T::class.java)
        intent.action = Intent.ACTION_VIEW
        return intent
    }


    private fun buildAlistSwitchShortCutInfo(context: Context): ShortcutInfoCompat {
        val msSwitchIntent = buildIntent<SwitchServerActivity>(context)
        return ShortcutInfoCompat.Builder(context, "alist_switch")
            .setShortLabel(context.getString(R.string.app_switch))
            .setLongLabel(context.getString(R.string.app_switch))
            .setIcon(IconCompat.createWithResource(context, R.drawable.icon))
            .setIntent(msSwitchIntent)
            .build()
    }


    fun buildShortCuts(context: Context) {
        ShortcutManagerCompat.setDynamicShortcuts(
            context, listOf(
                buildAlistSwitchShortCutInfo(context),
            )
        )
    }


}