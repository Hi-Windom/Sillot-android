package sc.windom.sofill.compose.components

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.twotone.BugReport
import androidx.compose.material.icons.twotone.Check
import androidx.compose.material.icons.twotone.Close
import androidx.compose.material.icons.twotone.Code
import androidx.compose.material.icons.twotone.ContentCopy
import androidx.compose.material.icons.twotone.Delete
import androidx.compose.material.icons.twotone.Email
import androidx.compose.material.icons.twotone.FitScreen
import androidx.compose.material.icons.twotone.Screenshot
import androidx.compose.material.icons.twotone.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import me.saket.cascade.CascadeDropdownMenu
import me.saket.cascade.rememberCascadeState
import org.b3log.siyuan.R
import sc.windom.sofill.S
import sc.windom.sofill.U
import org.b3log.siyuan.Utils
import org.b3log.siyuan.andapi.Toast
import sc.windom.sofill.U.disableScreenshot
import sc.windom.sofill.U.enableScreenshot
import sc.windom.sofill.Us.U_Phone.toggleFullScreen


data class MenuItem31(val title: String, val action: () -> Unit)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonTopAppBar(
    title: String, // 应用栏标题
    sourceFile: String, // 源代码文件名（包括后缀名），以 App.kt 为根目录，如果不在根目录请包含路径，例如 ld246/Home.kt
    uri: Uri?,
    isMenuVisible: MutableState<Boolean>,
    additionalMenuItem: @Composable (() -> Unit)? = null,
    onBackPressed: () -> Unit, // 返回按钮的点击事件
) {
//    var isMenuVisible by rememberSaveable { mutableStateOf(false) }
    val Lcc = LocalContext.current
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier
                        .clickable(onClick = onBackPressed)
                        .padding(end = 8.dp)
                )
                Text(
                    text = title,
                    fontSize = 18.sp
                )
            }
        },
        modifier = Modifier.background(Color.Blue),
        actions = {
            IconButton(onClick = { isMenuVisible.value = true }) {
                Icon(Icons.Rounded.MoreVert, contentDescription = "More options")
            }
            TopRightMenu(
                expanded = isMenuVisible.value,
                onDismiss = { isMenuVisible.value = false },
                sourceFile = sourceFile,
                uri = uri,
                additionalMenuItem = additionalMenuItem // 将额外的菜单项传递给 TopRightMenu
            )
        }
    )
}

@Composable
fun TopRightMenu(
    expanded: Boolean,
    sourceFile: String,
    uri: Uri?,
    onDismiss: () -> Unit,
    additionalMenuItem: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val state = rememberCascadeState()
    val Lcc = LocalContext.current
    var isFullScreen by rememberSaveable { mutableStateOf(false) }
    var canCaptureScreenshot by rememberSaveable { mutableStateOf(true) }
    LaunchedEffect(isFullScreen) {
        val activity = Lcc as Activity
        activity.toggleFullScreen(isFullScreen)
    }
    LaunchedEffect(canCaptureScreenshot) {
        val activity = Lcc as Activity
        if (canCaptureScreenshot) {
            activity.enableScreenshot()
        } else {
            activity.disableScreenshot()
        }
    }
    // 只有直接在CascadeDropdownMenu中才能使用childrenHeader和children，抽离出去的additionalMenuItem不行
    CascadeDropdownMenu(
        state = state,
        modifier = modifier,
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        if (uri != null && !listOf("http", "https", "siyuan").contains(uri.scheme)) {
            DropdownMenuItem(
                text = { Text("复制") },
                leadingIcon = { Icon(Icons.TwoTone.ContentCopy, contentDescription = null) },
                onClick = {
                    onDismiss()
                    // 获取系统的剪贴板管理器
                    val clipboardManager =
                        Lcc.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    // 创建一个剪贴板数据对象，将文件的 Uri 放入其中
                    val clipData = ClipData.newUri(Lcc.contentResolver, "label", uri)
                    // 设置剪贴板数据对象的 MIME 类型
                    clipData.addItem(ClipData.Item(uri))
                    // 将数据放入剪贴板
                    clipboardManager.setPrimaryClip(clipData)
                    Toast.Show(Lcc, "复制成功")
                },
            )
            DropdownMenuItem(
                text = { Text("分享") },
                leadingIcon = { Icon(Icons.TwoTone.Share, contentDescription = null) },
                onClick = {
                    onDismiss()
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_STREAM, uri) // 将文件 Uri 添加到 Intent 的 EXTRA_STREAM 中
                        type = "*/*" // 设置 MIME 类型为通配符，表示所有类型的文件
                    }

                    val chooserIntent = Intent.createChooser(shareIntent, "分享文件到")
                    chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // 添加新任务标志

                    // 使用 ContextCompat 中的 startActivity 启动分享意图
                    Lcc.let { it1 -> ContextCompat.startActivity(it1, chooserIntent, null) }
                },
            )
            DropdownMenuItem(
                text = { Text("删除") },
                leadingIcon = { Icon(Icons.TwoTone.Delete, contentDescription = null) },
                childrenHeader = {
                    DropdownMenuHeader {
                        Text(
                            text = "Are you sure?",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                children = {
                    DropdownMenuItem(
                        text = { Text("Yep") },
                        leadingIcon = { Icon(Icons.TwoTone.Check, contentDescription = null) },
                        onClick = {
                            onDismiss()
                            U.deleteFileByUri(Lcc, uri).let {
                                onDismiss()
                                if (it) {
//                                    Us.notifyGallery(Lcc, uri)
                                    Toast.Show(Lcc, "暂不支持该操作")
                                } else {
                                    Toast.Show(Lcc, "删除失败")
                                }
                            }
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Go back") },
                        leadingIcon = { Icon(Icons.TwoTone.Close, contentDescription = null) },
                        onClick = {
                            state.navigateBack()
                        }
                    )
                },
            )
        }
        // 调用额外的菜单项
        if (additionalMenuItem != null) {
            additionalMenuItem()
        }
        DropdownMenuItem(
            text = { Text(if (isFullScreen) "正常模式" else "全屏模式") },
            leadingIcon = { Icon(Icons.TwoTone.FitScreen, contentDescription = null) },
            onClick = {
                onDismiss()
                isFullScreen = !isFullScreen
            },
        )
        DropdownMenuItem(
            text = { Text(if (canCaptureScreenshot) "禁用截屏" else "允许截屏") },
            leadingIcon = { Icon(Icons.TwoTone.Screenshot, contentDescription = null) },
            onClick = {
                onDismiss()
                canCaptureScreenshot = !canCaptureScreenshot
            },
        )
        DropdownMenuItem(
            text = { Text("帮助") },
            leadingIcon = {
                Icon(
                    painterResource(R.drawable.icon),
                    contentDescription = null,
                    modifier = modifier.size(
                        S.C.small_iconSize.current
                    )
                )
            },
            children = {
                DropdownMenuItem(
                    text = { Text("报告此页") },
                    leadingIcon = { Icon(Icons.TwoTone.Email, contentDescription = null) },
                    onClick = {
                        onDismiss();U.FuckOtherApp.sendEmail(
                        Lcc.packageManager,
                        S.emailAdress,
                        "汐洛安卓反馈 - 报告此页",
                        "sourceFile: ${sourceFile}\n${Utils.getDeviceInfoString()}"
                    )
                    },
                )
                DropdownMenuItem(
                    text = { Text("反馈此页") },
                    leadingIcon = { Icon(Icons.TwoTone.BugReport, contentDescription = null) },
                    onClick = { onDismiss();U.openUrl("${S.gitRepoUrl}/issues/new") },
                )
                DropdownMenuItem(
                    text = { Text("查看源码") },
                    leadingIcon = { Icon(Icons.TwoTone.Code, contentDescription = null) },
                    onClick = { onDismiss();U.openUrl("${S.gitRepoUrl}/blob/HEAD/app/src/main/java/org/b3log/siyuan/${sourceFile}") },
                )
            },
        )
    }
}

data class MenuItem58(
    val title: String,
    val icon: ImageVector?,
    val contentDescription: String?,
    val action: () -> Unit
)

// 不好弄这个抽象
@Composable
fun CommonTopRightMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    menuItems: List<MenuItem58>,
    modifier: Modifier = Modifier
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        menuItems.forEach { item ->
            DropdownMenuItem(
                text = { Text(item.title) },
                leadingIcon = { item.icon?.let { Icon(it, contentDescription = null) } },
                onClick = {
                    item.action()
                    onDismiss()
                },
            )
        }
    }
}
