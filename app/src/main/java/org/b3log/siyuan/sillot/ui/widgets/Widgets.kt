package org.b3log.siyuan.sillot.ui.widgets

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.accompanist.systemuicontroller.rememberSystemUiController


@Composable
fun TransparentSystemBars() {
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = !isSystemInDarkTheme()
    SideEffect {
        systemUiController.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = useDarkIcons,
            isNavigationBarContrastEnforced = false,
        )
    }
}

@Composable
fun BasicBroadcaseReceiver(
    intentFilter: IntentFilter,
    onReceive: (Intent?) -> Unit,
    onRegister: (BroadcastReceiver, Context) -> Unit,
    onUnregister: (BroadcastReceiver, Context) -> Unit
) {
    val context = LocalContext.current
    val currentReceive by rememberUpdatedState(onReceive)

    DisposableEffect(context, intentFilter) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                currentReceive(intent)
            }
        }
        onRegister(receiver, context)

        onDispose {
            onUnregister(receiver, context)
        }
    }
}

@Suppress("DEPRECATION")
@Composable
fun LocalBroadcastReceiver(intentFilter: IntentFilter, onReceive: (Intent?) -> Unit) {
    BasicBroadcaseReceiver(
        intentFilter,
        onReceive,
        { obj, context ->
            LocalBroadcastManager.getInstance(context).registerReceiver(obj, intentFilter)
        },
        { obj, context -> LocalBroadcastManager.getInstance(context).unregisterReceiver(obj) }
    )
}

@Composable
fun SystemBroadcastReceiver(
    intentFilter: IntentFilter,
    onSystemEvent: (intent: Intent?) -> Unit
) {
    BasicBroadcaseReceiver(
        intentFilter = intentFilter, onReceive = onSystemEvent,
        onRegister = { obj, context ->
            ContextCompat.registerReceiver(
                context,
                obj,
                intentFilter,
                ContextCompat.RECEIVER_EXPORTED
            )
        },
        onUnregister = { obj, context -> context.unregisterReceiver(obj) }
    )
}