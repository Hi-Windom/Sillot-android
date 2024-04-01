package org.b3log.siyuan.sillot.ui.nav.alist

import android.content.Intent
import android.content.IntentFilter
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.b3log.siyuan.sillot.model.alist.AList
import org.b3log.siyuan.sillot.service.AListService
import org.b3log.siyuan.sillot.service.AListService.Companion.ACTION_STATUS_CHANGED
import org.b3log.siyuan.sillot.ui.LocalMainViewModel
import org.b3log.siyuan.sillot.ui.MyTools
import org.b3log.siyuan.sillot.ui.SwitchServerActivity
import org.b3log.siyuan.sillot.ui.widgets.LocalBroadcastReceiver
import org.b3log.siyuan.R
import org.b3log.siyuan.BuildConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AListScreen() {
    val context = LocalContext.current
    val mainVM = LocalMainViewModel.current
    val view = LocalView.current
    var alistRunning by remember { mutableStateOf(AListService.isRunning) }

    LocalBroadcastReceiver(intentFilter = IntentFilter(ACTION_STATUS_CHANGED)) {
        if (it?.action == ACTION_STATUS_CHANGED)
            alistRunning = AListService.isRunning
    }

    fun switch() {
        context.startService(Intent(context, AListService::class.java).apply {
            action = if (alistRunning) AListService.ACTION_SHUTDOWN else ""
        })
        alistRunning = !alistRunning
    }

    var showPwdDialog by remember { mutableStateOf(false) }
    if (showPwdDialog) {
        var pwd by remember { mutableStateOf("") }
        AlertDialog(onDismissRequest = { showPwdDialog = false },
            title = { Text(stringResource(R.string.admin_password)) },
            text = {
                Column(Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        value = pwd,
                        label = { Text(stringResource(id = R.string.password)) },
                        onValueChange = { pwd = it },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.titleMedium
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = pwd.isNotBlank(),
                    onClick = {
                        showPwdDialog = false
                        AList.setAdminPassword(pwd)
//                        context.longToast(
//                            R.string.admin_password_set_to,
//                            pwd
//                        )
                    }) {
                    Text(stringResource(id = R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPwdDialog = false }) {
                    Text(stringResource(id = R.string.cancel))
                }
            })
    }
    var showMoreOptions by remember { mutableStateOf(false) }

    var showAboutDialog by remember { mutableStateOf(false) }
    if (showAboutDialog) {
        AboutDialog {
            showAboutDialog = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row {
                        Text(stringResource(R.string.app_name))
                        Text(" - " + BuildConfig.VERSION_NAME)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        MyTools.addShortcut(
                            context,
                            context.getString(R.string.alist_server),
                            "alist_switch",
                            R.drawable.icon,
                            Intent(context, SwitchServerActivity::class.java)
                        )
                    }) {
                        Icon(
                            Icons.Default.AddBusiness,
                            stringResource(R.string.add_desktop_shortcut)
                        )
                    }

                    IconButton(onClick = {
                        showPwdDialog = true
                    }) {
                        Icon(
                            Icons.Default.Password,
                            stringResource(R.string.admin_password)
                        )
                    }

                    IconButton(onClick = {
                        showMoreOptions = true
                    }) {
                        DropdownMenu(
                            expanded = showMoreOptions,
                            onDismissRequest = { showMoreOptions = false }) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.check_update)) },
                                onClick = {
                                    showMoreOptions = false
                                }
                            )

                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.about)) },
                                onClick = {
                                    showMoreOptions = false
                                    showAboutDialog = true
                                }
                            )
                        }
                        Icon(Icons.Default.MoreVert, stringResource(R.string.more_options))
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 8.dp)
        ) {

            SwitchFloatingButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                switch = alistRunning
            ) {
                switch()
            }
        }
    }
}

@Composable
fun SwitchFloatingButton(modifier: Modifier, switch: Boolean, onSwitchChange: (Boolean) -> Unit) {
    val targetIcon =
        if (switch) Icons.Filled.Stop else Icons.AutoMirrored.Filled.Send
    val rotationAngle by animateFloatAsState(targetValue = if (switch) 360f else 0f, label = "")

    val color =
        animateColorAsState(
            targetValue = if (switch) MaterialTheme.colorScheme.inversePrimary else MaterialTheme.colorScheme.primaryContainer,
            label = "",
            animationSpec = tween(500, 0, LinearEasing)
        )

    FloatingActionButton(
        modifier = modifier,
        elevation = FloatingActionButtonDefaults.elevation(8.dp),
        shape = CircleShape,
        containerColor = color.value,
        onClick = { onSwitchChange(!switch) }) {

        Crossfade(targetState = targetIcon, label = "") {
            Icon(
                imageVector = it,
                contentDescription = stringResource(id = if (switch) R.string.shutdown else R.string.start),
                modifier = Modifier
                    .rotate(rotationAngle)
                    .graphicsLayer {
                        rotationZ = rotationAngle
                    }
                    .size(if (switch) 42.dp else 32.dp)
            )
        }

    }
}