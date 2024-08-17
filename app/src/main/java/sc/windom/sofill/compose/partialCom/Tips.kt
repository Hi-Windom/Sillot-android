/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/8/18 07:27
 * updated: 2024/8/18 07:27
 */

package sc.windom.sofill.compose.partialCom

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import sc.windom.sillot.App.Companion.application
import sc.windom.sofill.S
import sc.windom.sofill.compose.NetworkViewModelFlow

@Composable
fun NetworkAware() {
    Box(
        modifier = Modifier
            .zIndex(999f), // 确保在最顶层
        contentAlignment = Alignment.TopCenter
    ) {
        NetworkAwareContent(NetworkViewModelFlow(application))
    }
}

@Composable
fun NetworkAwareContent(viewModel: NetworkViewModelFlow) {
    val isNetworkAvailable by viewModel.isNetworkAvailable.collectAsState()
    if (!isNetworkAvailable) {
        val openSettingsLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { }

        Row(
            modifier = Modifier
                .background(S.C.Card_bgColor_red1.current)
                .fillMaxWidth()
                .clickable {
                    val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    openSettingsLauncher.launch(intent)
                }
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.TwoTone.WifiOff,
                contentDescription = "网络连接已断开",
                tint = Color.Yellow
            )
            Text(
                text = "当前网络不可用，请检查网络设置",
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(5.dp)
            )
        }
    }
}