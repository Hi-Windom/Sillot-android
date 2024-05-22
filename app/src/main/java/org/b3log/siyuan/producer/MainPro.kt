@file:Suppress("CompositionLocalNaming", "CompositionLocalNaming")
package org.b3log.siyuan.producer

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.util.Size
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.documentfile.provider.DocumentFile
import com.kongzue.dialogx.dialogs.BottomMenu
import com.kongzue.dialogx.dialogs.PopNotification
import com.kongzue.dialogx.interfaces.OnBottomMenuButtonClickListener
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.b3log.siyuan.CascadeMaterialTheme
import org.b3log.siyuan.R
import org.b3log.siyuan.S
import org.b3log.siyuan.Us
import org.b3log.siyuan.andapi.Toast
import org.b3log.siyuan.compose.ApkButtons
import org.b3log.siyuan.compose.AudioButtons
import org.b3log.siyuan.compose.LockScreenOrientation
import org.b3log.siyuan.compose.SelectableText
import org.b3log.siyuan.compose.VideoButtons
import org.b3log.siyuan.compose.components.CommonTopAppBar
import java.io.IOException


// TODO: 多选文件打开的处理
// TODO: 文件覆盖提醒
// TODO: 缓存清理
// TODO: 如果是 workspaceParentDir 目录下的文件支持删除
// TODO: 文件被删除时处理异常
class MainPro : ComponentActivity() {
    val TAG = "MainPro"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent
        val uri = intent.data
        Log.i(TAG, "onCreate() invoked")
// 设置沉浸式通知栏
        window.setDecorFitsSystemWindows(false)
        window.decorView.setOnApplyWindowInsetsListener { _, insets ->
            insets
        }
        if (uri != null) {
            setContent {
                CascadeMaterialTheme {
                    MyUI(intent)
                }
            }
//            if (ft == "视频") {
//                handleVideo(uri) // 不再直接播放
//            }
        } else {
        }

//        finish()
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, DelicateCoroutinesApi::class)
@Composable
fun MyUI(intent: Intent?) {
    val TAG = "MainPro-MyUI"
    val uri = intent?.data
    val Lcc = LocalContext.current
    val inspectionMode = LocalInspectionMode.current // 获取当前是否处于预览模式// 获取窗口尺寸
    val coroutineScope = rememberCoroutineScope()
    val fileName = uri?.let { Us.getFileName(Lcc, it) }
    val fileSize = uri?.let { Us.getFileSize(Lcc, it) }
    val mimeType = intent?.data?.let { Us.getMimeType(Lcc, it) } ?: ""
    val fileType = fileName?.let { Us.getFileMIMEType(mimeType, it) } ?: run { Us.getFileMIMEType(mimeType) }
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE // 是否横屏（宽高比）

    var isMenuVisible by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CommonTopAppBar("汐洛文件中转站", uri) {
                // 将Context对象安全地转换为Activity
                if (Lcc is Activity) {
                    Lcc.finish() // 结束活动
                }
            }
        }, modifier = Modifier.background(Color.Gray)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            contentAlignment = Alignment.Center
        ) {
            if (isLandscape) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(.4f)
                            .fillMaxHeight()
                            .padding(vertical = 16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        InfoPart(
                            uri = uri,
                            fileType = fileType,
                            fileName = fileName,
                            fileSize = fileSize
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(.6f)
                            .fillMaxHeight()
//                            .height(IntrinsicSize.Min)
                            .padding(vertical = 16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        BtnPart(
                            uri = uri,
                            mimeType = mimeType,
                            fileName = fileName
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        InfoPart(
                            uri = uri,
                            fileType = fileType,
                            fileName = fileName,
                            fileSize = fileSize
                        )
                        BtnPart(
                            uri = uri,
                            mimeType = mimeType,
                            fileName = fileName
                        )
                    }
                }
            }
        }



    }
}

@Composable
fun InfoPart(uri: Uri?, fileType: String?, fileName: String?, fileSize: String?) {
    val TAG = "MainPro-InfoPart"
    val Lcc= LocalContext.current
    val inspectionMode = LocalInspectionMode.current // 获取当前是否处于预览模式// 获取窗口尺寸
    val Thumbnail_Height = S.C.Thumbnail_Height.current
    val Thumbnail_Height_IMG = S.C.Thumbnail_Height_IMG.current

    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE // 是否横屏（宽高比）

    // 显示图像缩略图或文件类型图标
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center)
        ) {
            if (fileType != null) {
                if (fileType.endsWith("图像")) {
                    val bitmap = uri?.let { it1 ->
                        Lcc.contentResolver?.loadThumbnail(
                            it1,
                            Size(
                                if (isLandscape) Thumbnail_Height else Thumbnail_Height_IMG,
                                if (isLandscape) Thumbnail_Height else Thumbnail_Height_IMG
                            ), null
                        )
                    }
                    bitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = "Thumbnail",
                            modifier = Modifier
                                .size(Thumbnail_Height_IMG.dp)
                                .fillMaxSize()
                        )
                    }
                } else {
                    val icon = Us.getIconForFileType(fileType)
                    Icon(
                        imageVector = icon,
                        contentDescription = "File Type Icon",
                        modifier = Modifier
                            .size(Thumbnail_Height.dp)
                    )
                    //                    Image( // 对应的是 R.drawable.id 方案
                    //                        painter = painterResource(id = icon),
                    //                        contentDescription = null,
                    //                        modifier = Modifier.size(100.dp)
                    //                    )
                }
            }

            fileName?.let { it1 ->
                SelectableText(
                    text = it1, // 使用获取到的文件名
                    style = TextStyle(
                        fontSize = if (isLandscape) 14.sp else 16.sp
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "$fileSize ($fileType)",
                    fontSize = if (isLandscape) 12.sp else 14.sp,
                    color = Color.Gray,
                    modifier = if (isLandscape) Modifier.padding(top = 6.dp) else Modifier.padding(
                        bottom = 6.dp
                    )
                )
            }
        }
    }

}

@SuppressLint("Range")
@Composable
fun BtnPart(uri: Uri?, mimeType: String, fileName: String?) {
    val TAG = "MainPro-BtnPart"
    val Lcc = LocalContext.current
    val inspectionMode = LocalInspectionMode.current // 获取当前是否处于预览模式// 获取窗口尺寸
    val coroutineScope = rememberCoroutineScope()
    val Button_Width = S.C.Button_Width.current
    val btn_lspace = S.C.btn_lspace.current
    val btn_PaddingTopH = S.C.btn_PaddingTopH.current
    val btn_PaddingTopV = S.C.btn_PaddingTopV.current
    val btn_TextFontsizeH = S.C.btn_TextFontsizeH.current
    val btn_TextFontsizeV = S.C.btn_TextFontsizeV.current
    val btn_Color1 = S.C.btn_Color1.current
    val btn_bgColor1 = S.C.btn_bgColor1.current
    val btn_bgColor2 = S.C.btn_bgColor2.current
    val btn_bgColor3 = S.C.btn_bgColor3.current
    val btn_bgColor4 = S.C.btn_bgColor4.current
    val btnText1 = S.C.btnText1.current
    val btnText2 = S.C.btnText2.current
    val btnText3 = S.C.btnText3.current
    val btnText4 = S.C.btnText4.current

    var showAudioButton by remember { mutableStateOf(false) }
    var showVideoButton by remember { mutableStateOf(false) }
    var showApkButton by remember { mutableStateOf(false) }

    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE // 是否横屏（宽高比）

    var progressValue by remember { mutableStateOf(0) }
    var isButton3OnClickRunning by remember { mutableStateOf(false) }
    var isButton4OnClickRunning by remember { mutableStateOf(false) }
    var workspaceAssetsDir by remember { mutableStateOf("${S.workspaceParentDir}/sillot/data/assets") }
    var uri_from_file by remember { mutableStateOf(Uri.parse("")) }
    var uri_to_dir by remember { mutableStateOf(Uri.parse("")) }
    var selectedFolder by remember { mutableStateOf<Uri?>(null) }


    LaunchedEffect(key1 = mimeType) {
        if (fileName != null) {
            showAudioButton = mimeType.startsWith("audio/")
            showVideoButton = mimeType.startsWith("video/")
            showApkButton = mimeType == "application/vnd.android.package-archive" || (mimeType == "application/octet-stream" && fileName.endsWith(".apk.1"))
        }
    }

    fun onCopyFileToFolderByDocumentTree() {
        // 启动一个协程来执行任务
        coroutineScope.launch {
            withContext(Dispatchers.IO) {

                try {
                    if (!Us.isStorageSpaceAvailable(Lcc.contentResolver, uri_from_file)) {
                        // 存储空间不足，处理逻辑
                        Toast.Show(Lcc, "存储空间不足，请先清理")
                        return@withContext
                    }
                    val sourceFilePath = Us.getPathFromUri(Lcc, uri_from_file)
                    // 复制文件到所选文件夹
                    fileName?.let {
                        sourceFilePath?.let { it1 ->
                            Us.copyFileToFolderByDocumentTree(
                                Lcc, uri_to_dir, it,
                                it1, mimeType
                            )
                        }
                    }
                    withContext(Dispatchers.Main) {
                        Toast.Show(Lcc, "已复制到指定文件夹")
                    }
                } catch (e: IOException) {
                    Log.e(TAG, e.toString())
                    withContext(Dispatchers.Main) {
                        PopNotification.show("任务失败", e.toString()).noAutoDismiss()
                    }
                }
                // 执行任务完成后，关闭遮罩
                isButton3OnClickRunning = false
            }
        }
    }
    fun onCopyFileToMyAppFolder() {
        // 启动一个协程来执行任务
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    if (!Us.isStorageSpaceAvailable(Lcc.contentResolver, uri_from_file)) {
                        // 存储空间不足，处理逻辑
                        PopNotification.show(R.drawable.icon,  "存储空间不足，请先清理")
                        return@withContext
                    }
                    val sourceFilePath = Us.getPathFromUri(Lcc, uri_from_file)
                    // 复制文件到所选文件夹
                    fileName?.let {
                        sourceFilePath?.let { it1 ->
                            try {
                                Us.copyFileToMyAppFolder(
                                    workspaceAssetsDir, it, it1
                                )
                                PopNotification.show(R.drawable.icon, "已存入 ${workspaceAssetsDir}").autoDismiss(5000)
                            } catch (e: IOException) {
                                Log.e(TAG, e.toString())
                                PopNotification.show(R.drawable.icon, "任务失败", e.toString()).noAutoDismiss()
                            }

                        }
                    }
                } catch (e: IOException) {
                    Log.e(TAG, e.toString())
                    withContext(Dispatchers.Main) {
                        PopNotification.show(R.drawable.icon, "任务失败", e.toString()).noAutoDismiss()
                    }
                } finally {
                    // 执行任务完成后，关闭遮罩
                    isButton4OnClickRunning = false
                }
            }
        }
    }
    var manageAllFilesPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (Us.canManageAllFiles(Lcc)) {
            if (isButton3OnClickRunning) { onCopyFileToFolderByDocumentTree() }
            else if (isButton4OnClickRunning) { onCopyFileToMyAppFolder() }

        }
    }
    val bt3TaskLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { _uri ->
                if (uri == null) return@let
                isButton3OnClickRunning = true  // 没有设置 LaunchedEffect 但是需要显示遮罩
                // 通过 SAF 获取持久性权限
                Lcc.contentResolver.takePersistableUriPermission(_uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

//                Us.requestExternalStoragePermission(Lcc as Activity)
                // 使用DocumentFile处理URI
                val rootDocument = DocumentFile.fromTreeUri(Lcc, _uri)
                // 例如，列出根目录下的文件和文件夹
                rootDocument?.listFiles()?.forEach { file ->
                    // 处理文件或文件夹
                    Log.d(TAG, "File name: ${file.name}, Is directory: ${file.isDirectory}, mimeType: ${file.type}, canRead: ${file.canRead()}, canWrite: ${file.canWrite()}, lastModified: ${file.lastModified()} ")
                }

                uri_from_file = uri
                uri_to_dir = _uri
                if (Us.canManageAllFiles(Lcc)) {
                    onCopyFileToFolderByDocumentTree()
                }
                else {
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    manageAllFilesPermissionLauncher.launch(intent)
                }

            }
        }
    }

    LaunchedEffect(key1 = isButton4OnClickRunning) {
        if (isButton4OnClickRunning) {
            uri_from_file = uri
            uri_to_dir = Uri.parse(workspaceAssetsDir)
            if (Us.canManageAllFiles(Lcc)) {
                onCopyFileToMyAppFolder()
            }
            else {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                manageAllFilesPermissionLauncher.launch(intent)
            }

        }
    }
    // 遮罩组件
    if (isButton4OnClickRunning || isButton3OnClickRunning) {
        // 锁定当前屏幕方向，避免重绘，相当于 android.app.Activity.setRequestedOrientation
        LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED)
                Dialog(
                    onDismissRequest = {
                        isButton3OnClickRunning = false
                        isButton4OnClickRunning = false
                        selectedFolder = null
                    },
                    properties = DialogProperties(
                        dismissOnBackPress = false,
                        dismissOnClickOutside = false
                    )
                ) {
                    // 遮罩内容
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (false) {
                            LinearProgressIndicator(
                                progress = { progressValue / 100f },
                                modifier = Modifier
                                    .padding(2.dp)
                                    .height(13.dp)
                                    .fillMaxWidth(),
                            )
                        } else {
                            // 不设置progress参数，显示不确定进度
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .padding(bottom = 58.dp)
                                    .height(13.dp)
                                    .fillMaxWidth(),
                            )
                            Text(
                                text = "操作正在进行……\n请勿退出",
                                color = Color.Yellow,
                                modifier = Modifier
                                    .padding(bottom = 8.dp)
                                    .align(Alignment.BottomCenter),
                                letterSpacing = btn_lspace,
                                fontSize = if (isLandscape) btn_TextFontsizeH else btn_TextFontsizeV
                            )
                        }
                    }
                }
    }
    if (!isButton4OnClickRunning && !isButton3OnClickRunning) {
        // 解除屏幕方向锁定
        LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
    }

    //// @D 通用按键部分

    // 保存到指定文件夹
    Button(modifier= Modifier
        .width(Button_Width.dp)
        .padding(top = if (isLandscape) btn_PaddingTopH else btn_PaddingTopV),
        colors = ButtonDefaults.buttonColors(
            containerColor = btn_bgColor3,
            contentColor = btn_Color1
        ), enabled = true, onClick = {
            val btn3_intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            bt3TaskLauncher.launch(btn3_intent)
            // 非阻塞
        }) {
        Text(
            text = btnText3,
            letterSpacing = btn_lspace,
            fontSize = if (isLandscape) btn_TextFontsizeH else btn_TextFontsizeV
        )
    }

    // 存入工作空间级资源目录
    Button(modifier= Modifier
        .width(Button_Width.dp)
        .padding(top = if (isLandscape) btn_PaddingTopH else btn_PaddingTopV),
        colors = ButtonDefaults.buttonColors(
            containerColor = btn_bgColor4,
            contentColor = btn_Color1
        ), enabled = true, onClick = {
            if (uri!=null) {
//                val handler = Handler(Looper.getMainLooper())
//                handler.post {
                    val directories = Us.getDirectoriesInPath(S.workspaceParentDir)
                    val filteredDirectories = directories.filter { it != "home" }
                if (filteredDirectories.isNotEmpty())
                {var selectMenuIndex = 0
                    var selectMenuText = "sillot"
                    BottomMenu.show(filteredDirectories)
                        .setMessage("sillot 是默认工作空间")
                        .setTitle("选择要存入的工作空间")
                        .setSelection(selectMenuIndex) //指定已选择的位置
                        .setOnMenuItemClickListener { dialog, text, index ->
                            selectMenuIndex = index
                            selectMenuText = text as String
                            dialog.refreshUI() // 在 compose 里需要强制刷新
                            true // 点击菜单后不会自动关闭
                        }
                        .setOkButton("确定",
                            OnBottomMenuButtonClickListener { menu, view ->
                                Log.e(TAG, "${selectMenuText}")

                                workspaceAssetsDir = "${S.workspaceParentDir}/${selectMenuText}/data/assets"
                                isButton4OnClickRunning = true // 值变化时会触发重组
                                false
                            })
                        .setCancelButton("取消",
                            OnBottomMenuButtonClickListener { menu, view ->
                                false
                            })
                } else {
                    PopNotification.show(R.drawable.icon, "未发现任何工作空间", "请检查是否初始化了，或者路径存在异常 ${S.workspaceParentDir}/").noAutoDismiss()
                }
//                }
            }
        }) {
        Text(
            text = btnText4,
            letterSpacing = btn_lspace,
            fontSize = if (isLandscape) btn_TextFontsizeH else btn_TextFontsizeV
        )
    }


    //// 类型按键区

    fun ApkBTNonClick1() {
        uri?.let { Us.installApk(Lcc as Activity, it) }
    }
    if (inspectionMode || showAudioButton) {
        AudioButtons()
    } else if (showVideoButton) {
        uri?.let { VideoButtons(it) }
    } else if (showApkButton) {
        ApkButtons(::ApkBTNonClick1)
    }

}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    // 在预览环境中覆盖值。provides 提供明确值，这将覆盖组件嵌套中CompositionLocalProvider的值；providesDefault 提供默认值，
    // CompositionLocalProvider的作用域和它们在代码中的顺序决定了哪个providesDefault生效。
    CompositionLocalProvider(
        S.C.Thumbnail_Height provides 250,
        S.C.Button_Width providesDefault 300,
    ) {
        MyUI(null)
    }
}