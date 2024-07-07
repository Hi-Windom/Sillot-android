/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/7/8 上午5:39
 * updated: 2024/7/8 上午5:39
 */

package org.b3log.siyuan.sillot.ui.nav

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import sc.windom.namespace.SillotMatrix.R

sealed class BottomNavRoute(
    @StringRes val strId: Int,
    val id: String,
    val icon: @Composable (() -> Unit),
) {
    companion object {
        val routes = listOf(
            AListConfig,
            AList,
            Web,
//            AListProvider,
            Settings,
        )
    }

    data object AListConfig : BottomNavRoute(R.string.alist_config, "alist_config", {
        Icon(Icons.Default.Code, null)
    })

    data object AList : BottomNavRoute(R.string.app_name, "alist", {
        Icon(Icons.Default.AcUnit, null)
    })

    data object AListProvider : BottomNavRoute(R.string.alist_provider, "alist_provider", {
        Icon(Icons.Default.PhoneAndroid, null)
    })

    data object Settings : BottomNavRoute(R.string.settings, "settings", {
        Icon(Icons.Default.Settings, null)
    })

    data object Web : BottomNavRoute(R.string.web, "web", {
        Icon(Icons.Default.Preview, null)
    })
}