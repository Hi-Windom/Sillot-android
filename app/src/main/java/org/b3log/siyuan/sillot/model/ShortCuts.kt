package org.b3log.siyuan.sillot.model

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import org.b3log.siyuan.R
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