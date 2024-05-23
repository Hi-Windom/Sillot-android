package org.b3log.siyuan.compose.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.MenuItem
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.twotone.BugReport
import androidx.compose.material.icons.twotone.Check
import androidx.compose.material.icons.twotone.Close
import androidx.compose.material.icons.twotone.ContentCopy
import androidx.compose.material.icons.twotone.Delete
import androidx.compose.material.icons.twotone.Email
import androidx.compose.material.icons.twotone.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import me.saket.cascade.CascadeDropdownMenu
import me.saket.cascade.rememberCascadeState
import org.b3log.siyuan.R
import org.b3log.siyuan.S
import org.b3log.siyuan.Us
import org.b3log.siyuan.Utils
import org.b3log.siyuan.andapi.Toast

data class MenuItem31(val title: String, val action: () -> Unit)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonTopAppBar(
    title: String, // 应用栏标题
    uri: Uri?,
    additionalMenuItem: @Composable (() -> Unit)? = null,
    onBackPressed: () -> Unit, // 返回按钮的点击事件
) {
    val TAG = "CommonTopAppBar"
    var isMenuVisible by rememberSaveable { mutableStateOf(false) }
    val Lcc = LocalContext.current
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
            IconButton(onClick = { isMenuVisible = true }) {
                Icon(Icons.Rounded.MoreVert, contentDescription = "More options")
            }
            TopRightMenu(
                expanded = isMenuVisible,
                onDismiss = { isMenuVisible = false },
                TAG = TAG,
                uri = uri,
                additionalMenuItem = additionalMenuItem // 将额外的菜单项传递给 TopRightMenu
            )
        }
    )
}

@Composable
fun TopRightMenu(
    expanded: Boolean,
    TAG: String,
    uri: Uri?,
    onDismiss: () -> Unit,
    additionalMenuItem: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val state = rememberCascadeState()
    val Lcc = LocalContext.current
    CascadeDropdownMenu(
        state = state,
        modifier = modifier,
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        if(uri != null){
            DropdownMenuItem(
                text = { Text("复制") },
                leadingIcon = { Icon(Icons.TwoTone.ContentCopy, contentDescription = null) },
                onClick = {
                    // 获取系统的剪贴板管理器
                    val clipboardManager = Lcc.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
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
                            Us.deleteFileByUri(Lcc, uri).let {
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
            text = { Text("帮助") },
            leadingIcon = { Icon(
                painterResource(R.drawable.icon), contentDescription = null, modifier = modifier.size(
                    S.C.small_iconSize.current)) },
            children = {
                DropdownMenuItem(
                    text = { Text("报告此页") },
                    leadingIcon = { Icon(Icons.TwoTone.Email, contentDescription = null) },
                    onClick = {  Us.sendEmail(Lcc.packageManager, S.emailAdress, "汐洛安卓反馈 - 报告此页", "TAG: ${TAG}\n${Utils.getDeviceInfoString()}")  },
                )
                DropdownMenuItem(
                    text = { Text("反馈此页") },
                    leadingIcon = { Icon(Icons.TwoTone.BugReport, contentDescription = null) },
                    onClick = { Us.openUrl("${S.gitRepoUrl}/issues/new")},
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
