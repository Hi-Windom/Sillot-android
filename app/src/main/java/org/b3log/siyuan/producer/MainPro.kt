@file:Suppress("CompositionLocalNaming", "CompositionLocalNaming")
package org.b3log.siyuan.producer

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.Log
import android.util.Size
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Css
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Html
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Javascript
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import com.kongzue.dialogx.dialogs.BottomMenu
import com.kongzue.dialogx.dialogs.PopNotification
import com.kongzue.dialogx.interfaces.OnBottomMenuButtonClickListener
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.b3log.siyuan.R
import org.b3log.siyuan.S
import org.b3log.siyuan.Us
import org.b3log.siyuan.andapi.Toast
import org.b3log.siyuan.sillot.util.FileUtil.getDataColumn
import org.b3log.siyuan.videoPlayer.SimplePlayer
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.DecimalFormat


// TODO: 多选文件打开的处理
// TODO: 文件覆盖提醒
// TODO: 缓存清理
// TODO: 如果是 workspaceParentDir 目录下的文件支持删除
class MainPro : ComponentActivity() {
    val TAG = "MainPro"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent
        val uri = intent.data
// 设置沉浸式通知栏
        window.setDecorFitsSystemWindows(false)
        window.decorView.setOnApplyWindowInsetsListener { _, insets ->
            insets
        }
        if (uri != null) {
            val ft = getMimeType(this,uri)?.let { Us.getFileMIMEType(it) }
            ft?.let { Log.e(TAG, it) }
            if (ft == "视频") {
                handleVideo(uri)
            } else {
                // 处理其他类型文件
                setContent {
                    MyUI(intent, ft, this)
                }
            }
        } else {
        }

//        finish()
    }

    private fun handleVideo(uri: Uri) {
        val videoPath = if (uri.scheme == "file") {
            // 本地文件
            uri.path ?: ""
        } else {
            // URL
            uri.toString()
        }

        val intent = Intent(this, SimplePlayer::class.java)
        intent.putExtra("videoPath", videoPath)
        startActivity(intent)
    }

    private val pickVideoLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val videoPath = getRealPathFromURI(it)
            val intent = Intent(this, SimplePlayer::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            intent.putExtra("videoPath", videoPath)
            startActivity(intent)
        }
    }

    private fun getRealPathFromURI(uri: Uri): String {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        cursor?.let {
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            val path = cursor.getString(columnIndex)
            cursor.close()
            return path
        }
        return ""
    }
}
//fun getIconForFileType(fileType: String): Int { // 有空再说
//    return when (fileType) {
//        "视频" -> R.drawable.ic_video
//        "音频" -> R.drawable.ic_audio
//        "文本" -> R.drawable.ic_text
//        "图像" -> R.drawable.ic_image
//        "程序" -> R.drawable.ic_program
//        else -> R.drawable.ic_file // 默认图标
//    }
//}
fun getIconForFileType(fileType: String): ImageVector {
    return when {
        fileType == "其他文本" -> Icons.Default.TextFields
        fileType == "HTML" -> Icons.Default.Html
        fileType == "CSS" -> Icons.Default.Css
        fileType == "JavaScript" -> Icons.Default.Javascript
        fileType == "PDF" -> Icons.Default.PictureAsPdf
        fileType == "Word文档" -> Icons.Default.Badge
        fileType == "Excel表格" -> Icons.Default.TableChart
        fileType == "PowerPoint演示文稿" -> Icons.Default.Tab
        fileType == "压缩文件" -> Icons.Default.FolderZip
        fileType == "EPUB" -> Icons.Default.Book
        fileType.endsWith("视频") -> Icons.Default.Movie
        fileType.endsWith("音频") -> Icons.Default.MusicNote
        fileType.endsWith("文本") -> Icons.Default.Description
        fileType.endsWith("图像") -> Icons.Default.Image
        fileType.endsWith("程序") -> Icons.Default.Android
        fileType.endsWith("音频") -> Icons.Default.MusicNote
        else -> Icons.AutoMirrored.Filled.InsertDriveFile // 默认图标
    }
}


fun getMimeType(context: Context, uri: Uri): String? {
    return context.contentResolver.getType(uri)
}
@SuppressLint("Range")
fun getFileName(context: Context, uri: Uri): String? {
    var result: String? = null
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
        }
    }
    return result
}

fun getFileSize(uri: Uri): String? {
    val file = uri.path?.let { File(it) }
    if (file != null) {
        return if (file.exists()) {
            val fileSizeInBytes = file.length().toDouble()
            val units = arrayOf("B", "KB", "MB", "GB", "TB")
            var fileSize = fileSizeInBytes
            var unitIndex = 0

            // 转换文件大小到合适的单位
            while (fileSize >= 1024 && unitIndex < units.size - 1) {
                fileSize /= 1024
                unitIndex++
            }

            // 格式化文件大小，保留两位小数
            val df = DecimalFormat("#.##")
            "${df.format(fileSize)} ${units[unitIndex]}"
        } else {
            null
        }
    }
    return "unknown"
}

fun isStorageSpaceAvailable(contentResolver: ContentResolver, uri: Uri): Boolean {
    contentResolver.openFileDescriptor(uri, "r").use { pfd ->
        val inputStream = contentResolver.openInputStream(uri)
        inputStream?.use { input ->
            val fileSize = pfd?.statSize ?: 0
            val buffer = ByteArray(8 * 1024) // 缓冲区大小为 8 KB
            var bytesRead: Int
            var totalBytesRead = 0L
            while (input.read(buffer).also { bytesRead = it } != -1) {
                totalBytesRead += bytesRead
                // 假设存储空间不足的临界值为文件大小的三倍
                if (totalBytesRead > fileSize * 3) {
                    return false
                }
            }
        }
    }
    return true
}

fun getPathFromUri(context: Context, uri: Uri): String? {
    var filePath: String? = null
    val contentResolver = context.contentResolver

    // 根据不同的URI方案执行不同的处理
    when {
        "content" == uri.scheme -> {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    filePath = it.getString(columnIndex)
                }
            }
        }
        "file" == uri.scheme -> {
            filePath = uri.path
        }
        // 如果是通过DocumentProvider获取的URI
        DocumentsContract.isDocumentUri(context, uri) -> {
            if (isExternalStorageDocument(uri)) {
                // 处理外部存储器文档
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":")
                val type = split[0]

                if ("primary".equals(type, ignoreCase = true)) {
                    filePath = "${context.getExternalFilesDir(null)}/${split[1]}"
                }
            } else if (isDownloadsDocument(uri)) {
                // 处理下载的文件
                val id = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), id.toLong()
                )
                filePath = getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                // 处理媒体文档
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":")
                val type = split[0]

                var contentUri: Uri? = null
                when (type) {
                    "image" -> contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    "video" -> contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    "audio" -> contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }

                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])
                filePath = getDataColumn(context, contentUri, selection, selectionArgs)
            }
        }
    }
    return filePath
}
// 检查URI是否是外部存储器文档
private fun isExternalStorageDocument(uri: Uri): Boolean {
    return "com.android.externalstorage.documents" == uri.authority
}

// 检查URI是否是下载文档
private fun isDownloadsDocument(uri: Uri): Boolean {
    return "com.android.providers.downloads.documents" == uri.authority
}

// 检查URI是否是媒体文档
private fun isMediaDocument(uri: Uri): Boolean {
    return "com.android.providers.media.documents" == uri.authority
}

/**
 * 将源文件复制到应用私有的目录中。当文件存在时不会创建新的重命名文件。进行哈希检查来跳过覆写不会减少耗时。
 *
 * @param targetFolderPath 目标文件夹的路径，相对于应用的私有目录。使用绝对路径，例： /storage/emulated/0/Android/data/sc.windom.sillot/files/sillot
 * @param targetFileName 目标文件的名称。
 * @param sourceFilePath 源文件的路径。
 */
private fun copyFileToMyAppFolder(targetFolderPath: String, targetFileName: String, sourceFilePath: String) {
    // 创建源文件的 File 对象
    val sourceFile = File(sourceFilePath)

    // 创建目标文件夹的 File 对象
    val targetFolder = File(targetFolderPath)

    // 如果目标文件夹不存在，则创建它
    if (!targetFolder.exists()) {
        targetFolder.mkdirs()
    }

    // 创建目标文件的 File 对象
    val targetFile = File(targetFolder, targetFileName)

    // 打开源文件的输入流
    var inputStream: FileInputStream? = null
    // 获取目标文件的输出流
    var outputStream: FileOutputStream? = null

    inputStream = FileInputStream(sourceFile)
    outputStream = FileOutputStream(targetFile)

    // 将数据从输入流复制到输出流
    inputStream.copyTo(outputStream)

    // 关闭输入流和输出流，确保释放资源
    inputStream.close()
    outputStream.close()

}
/**
 * 将源文件复制到指定的目录，通过文档树这个特殊的口径。当文件存在时会创建新的重命名文件
 *
 * @param context 一般是当前活动
 * @param treeUri 用户选择的文件件Uri，不是真实路径Uri。
 * @param targetFileName 目标文件名。
 * @param sourceFilePath 源文件路径。
 * @param mimeType 源文件类型。
 * @param move 是否删除源文件
 */
private fun copyFileToFolderByDocumentTree(context: Context, treeUri: Uri, targetFileName: String = "Untitled.sc", sourceFilePath: String, mimeType: String, move: Boolean = false) {

    // 创建源文件的 File 对象
    val sourceFile = File(sourceFilePath)

    // 创建目标文件夹的 DocumentFile 对象
    val treeDocumentFile = DocumentFile.fromTreeUri(context, treeUri)

    // 在目标文件夹中创建一个新文件
    val targetFile = treeDocumentFile?.createFile(mimeType, targetFileName) ?: return

    // 打开源文件的输入流
    val inputStream = FileInputStream(sourceFile)

    // 获取目标文件的输出流
    context.contentResolver.openOutputStream(targetFile.uri)?.use { outputStream ->
        // 将数据从输入流复制到输出流
        inputStream.copyTo(outputStream)
    }

    // 关闭输入流
    inputStream.close()
    if (move) { sourceFile.delete() }
}

fun isFileSizeOverLimit(file: File, limitMB: Int): Boolean {
    val fileSize = file.length() // 获取文件大小，单位为字节
    val fileSizeInMB = fileSize / (1024 * 1024) // 将文件大小转换为 MB
    return fileSizeInMB > limitMB
}
fun isFileSizeOverLimit(contentResolver: ContentResolver, uri: Uri, limitMB: Int): Boolean {
    val fileSize = getFileSizeFromUri(contentResolver, uri) ?: return false // 获取文件大小，如果获取失败则返回 false
    val fileSizeInMB = fileSize / (1024 * 1024) // 将文件大小转换为 MB
    return fileSizeInMB > limitMB
}


// 根据 Uri 获取文件大小
fun getFileSizeFromUri(contentResolver: ContentResolver, uri: Uri): Long? {
    return contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
        cursor.moveToFirst()
        cursor.getLong(sizeIndex)
    }
}
@Composable
fun loadAppThumbnail(): ImageBitmap? { // 这里获取到的是自己的应用图标
    val context = LocalContext.current
    val packageManager = context.packageManager
    val packageName = context.packageName

    var thumbnail: ImageBitmap? = null

    val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
    val drawable = applicationInfo.loadIcon(packageManager)
    thumbnail = convertDrawableToImageBitmap(drawable)

    return thumbnail
}
@Composable
fun convertDrawableToImageBitmap(drawable: Drawable): ImageBitmap? {
    return try {
        val bitmap = (drawable as BitmapDrawable).bitmap
        bitmap.asImageBitmap()
    } catch (e: Exception) {
        null
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, DelicateCoroutinesApi::class)
@Composable
fun MyUI(intent: Intent?, fileType: String?, context: Context) {
    val TAG = "MainPro-MyUI"
    val uri = intent?.data
    val inspectionMode = LocalInspectionMode.current // 获取当前是否处于预览模式// 获取窗口尺寸
    val coroutineScope = rememberCoroutineScope()
    val fileName = uri?.let { getFileName(context, it) }
    val fileSize = uri?.let { getFileSize(it) }
    val mimeType = intent?.data?.let { getMimeType(context, it) } ?: ""
    val _fileType = Us.getFileMIMEType(mimeType)
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE // 是否横屏（宽高比）


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier
                                .clickable(onClick = {
                                    // 将Context对象安全地转换为Activity
                                    if (context is Activity) {
                                        context.finish() // 结束活动
                                    }
                                })
                                .padding(end = 8.dp)
                        )
                        Text(
                            text = "汐洛文件中转站",
                            fontSize = 18.sp
                        )
                    }
                }, modifier = Modifier.background(Color.Blue)
            )
        }, modifier = Modifier.background(Color.Gray)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
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
                            context = context,
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
                            context = context,
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
                            context = context,
                            uri = uri,
                            fileType = fileType,
                            fileName = fileName,
                            fileSize = fileSize
                        )
                        BtnPart(
                            context = context,
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
@SuppressLint("UnrememberedMutableInteractionSource")
@Composable
fun SelectableText(text: String, modifier: Modifier = Modifier, style: TextStyle = TextStyle()) {
    // 支持长按选择操作
    SelectionContainer {
        Text(
            text = text,
            modifier = modifier,
            style = style
                .copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    fontStyle = FontStyle.Normal
                )
        )
    }
}
@Composable
fun InfoPart(context: Context, uri: Uri?, fileType: String?, fileName: String?, fileSize: String?) {
    val TAG = "MainPro-InfoPart"
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
                        context.contentResolver?.loadThumbnail(
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
                    val icon = getIconForFileType(fileType)
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
fun BtnPart(context: Context, uri: Uri?, mimeType: String, fileName: String?) {
    val TAG = "MainPro-BtnPart"
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
        showAudioButton = mimeType.startsWith("audio/")
        showVideoButton = mimeType.startsWith("video/")
        showApkButton = mimeType == "application/vnd.android.package-archive"
    }

    fun onCopyFileToFolderByDocumentTree() {
        // 启动一个协程来执行任务
        coroutineScope.launch {
            withContext(Dispatchers.IO) {

                try {
                    if (!isStorageSpaceAvailable(context.contentResolver, uri_from_file)) {
                        // 存储空间不足，处理逻辑
                        Toast.Show(context, "存储空间不足，请先清理")
                        return@withContext
                    }
                    val sourceFilePath = getPathFromUri(context, uri_from_file)
                    // 复制文件到所选文件夹
                    fileName?.let {
                        sourceFilePath?.let { it1 ->
                            copyFileToFolderByDocumentTree(
                                context, uri_to_dir, it,
                                it1, mimeType
                            )
                        }
                    }
                    withContext(Dispatchers.Main) {
                        Toast.Show(context, "已复制到指定文件夹")
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
                    if (!isStorageSpaceAvailable(context.contentResolver, uri_from_file)) {
                        // 存储空间不足，处理逻辑
                        PopNotification.show(R.drawable.icon,  "存储空间不足，请先清理")
                        return@withContext
                    }
                    val sourceFilePath = getPathFromUri(context, uri_from_file)
                    // 复制文件到所选文件夹
                    fileName?.let {
                        sourceFilePath?.let { it1 ->
                            try {
                                copyFileToMyAppFolder(
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
        if (Us.canManageAllFiles(context)) {
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
                context.contentResolver.takePersistableUriPermission(_uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

//                Us.requestExternalStoragePermission(context as Activity)
                // 使用DocumentFile处理URI
                val rootDocument = DocumentFile.fromTreeUri(context, _uri)
                // 例如，列出根目录下的文件和文件夹
                rootDocument?.listFiles()?.forEach { file ->
                    // 处理文件或文件夹
                    Log.d(TAG, "File name: ${file.name}, Is directory: ${file.isDirectory}, mimeType: ${file.type}, canRead: ${file.canRead()}, canWrite: ${file.canWrite()}, lastModified: ${file.lastModified()} ")
                }

                uri_from_file = uri
                uri_to_dir = _uri
                if (Us.canManageAllFiles(context)) {
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
            if (Us.canManageAllFiles(context)) {
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
                                text = "操作正在进行……\n请勿旋转屏幕或退出",
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

    //// @D 通用按键部分

    // 分享
    Button(modifier= Modifier
        .width(Button_Width.dp)
        .padding(top = if (isLandscape) btn_PaddingTopH else btn_PaddingTopV),
        colors = ButtonDefaults.buttonColors(
            containerColor = btn_bgColor1,
            contentColor = btn_Color1
        ), enabled = true, onClick = {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, uri) // 将文件 Uri 添加到 Intent 的 EXTRA_STREAM 中
                type = "*/*" // 设置 MIME 类型为通配符，表示所有类型的文件
            }

            val chooserIntent = Intent.createChooser(shareIntent, "分享文件到")
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // 添加新任务标志

            // 使用 ContextCompat 中的 startActivity 启动分享意图
            context.let { it1 -> ContextCompat.startActivity(it1, chooserIntent, null) }
        }) {
        Text(
            text = btnText1,
            letterSpacing = btn_lspace,
            fontSize = if (isLandscape) btn_TextFontsizeH else btn_TextFontsizeV
        )
    }

    // 复制到剪贴板
    Button(modifier= Modifier
        .width(Button_Width.dp)
        .padding(top = if (isLandscape) btn_PaddingTopH else btn_PaddingTopV),
        colors = ButtonDefaults.buttonColors(
            containerColor = btn_bgColor2,
            contentColor = btn_Color1
        ), enabled = true, onClick = {
            // 获取系统的剪贴板管理器
            val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            // 创建一个剪贴板数据对象，将文件的 Uri 放入其中
            val clipData = ClipData.newUri(context.contentResolver, "label", uri)
            // 设置剪贴板数据对象的 MIME 类型
            clipData.addItem(ClipData.Item(uri))
            // 将数据放入剪贴板
            clipboardManager.setPrimaryClip(clipData)
            Toast.Show(context, "复制成功")
        }) {
        Text(
            text = btnText2,
            letterSpacing = btn_lspace,
            fontSize = if (isLandscape) btn_TextFontsizeH else btn_TextFontsizeV
        )
    }

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
                    var selectMenuIndex = 0
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

                                workspaceAssetsDir = "/storage/emulated/0/Android/data/sc.windom.sillot/files/${selectMenuText}/data/assets"
                                isButton4OnClickRunning = true // 值变化时会触发重组
                                false
                            })
                        .setCancelButton("取消",
                            OnBottomMenuButtonClickListener { menu, view ->
                                false
                            })
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
        uri?.let { Us.installApk(context as Activity, it) }
    }
    if (inspectionMode || showAudioButton) {
        AudioButtons()
    } else if (showVideoButton) {
        VideoButtons()
    } else if (showApkButton) {
        ApkButtons(::ApkBTNonClick1)
    }

}

@Composable
fun AudioButtons() {
    val TAG = "MainPro-BtnPart-AudioButtons"
    val inspectionMode = LocalInspectionMode.current // 获取当前是否处于预览模式// 获取窗口尺寸
    val Button_Width = S.C.Button_Width.current
    val btn_lspace = S.C.btn_lspace.current
    val btn_PaddingTopH = S.C.btn_PaddingTopH.current
    val btn_PaddingTopV = S.C.btn_PaddingTopV.current
    val btn_TextFontsizeH = S.C.btn_TextFontsizeH.current
    val btn_TextFontsizeV = S.C.btn_TextFontsizeV.current
    val btn_Color1 = S.C.btn_Color1.current
    val btn_bgColor1 = S.C.btn_bgColor_pink.current
    val btnText1 = S.C.btnTextAudio1.current
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE // 是否横屏（宽高比）
    Button(modifier= Modifier
        .width(Button_Width.dp)
        .padding(top = if (isLandscape) btn_PaddingTopH else btn_PaddingTopV),
        colors = ButtonDefaults.buttonColors(
            containerColor = btn_bgColor1,
            contentColor = btn_Color1
        ), enabled = true, onClick = { /*TODO*/ }) {
        Text(
            text = btnText1,
            letterSpacing = btn_lspace,
            fontSize = if (isLandscape) btn_TextFontsizeH else btn_TextFontsizeV
        )
    }
}

@Composable
fun VideoButtons() {
    val TAG = "MainPro-BtnPart-AudioButtons"
    val inspectionMode = LocalInspectionMode.current // 获取当前是否处于预览模式// 获取窗口尺寸
    val Button_Width = S.C.Button_Width.current
    val btn_lspace = S.C.btn_lspace.current
    val btn_PaddingTopH = S.C.btn_PaddingTopH.current
    val btn_PaddingTopV = S.C.btn_PaddingTopV.current
    val btn_TextFontsizeH = S.C.btn_TextFontsizeH.current
    val btn_TextFontsizeV = S.C.btn_TextFontsizeV.current
    val btn_Color1 = S.C.btn_Color1.current
    val btn_bgColor1 = S.C.btn_bgColor_pink.current
    val btnText1 = S.C.btnTextVideo1.current
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE // 是否横屏（宽高比）
    Button(modifier= Modifier
        .width(Button_Width.dp)
        .padding(top = if (isLandscape) btn_PaddingTopH else btn_PaddingTopV),
        colors = ButtonDefaults.buttonColors(
            containerColor = btn_bgColor1,
            contentColor = btn_Color1
        ), enabled = true, onClick = { /*TODO*/ }) {
        Text(
            text = btnText1,
            letterSpacing = btn_lspace,
            fontSize = if (isLandscape) btn_TextFontsizeH else btn_TextFontsizeV
        )
    }
}

@Composable
fun ApkButtons( ApkBTNonClick1: () -> Unit) {
    val TAG = "MainPro-BtnPart-AudioButtons"
    val inspectionMode = LocalInspectionMode.current // 获取当前是否处于预览模式// 获取窗口尺寸
    val Button_Width = S.C.Button_Width.current
    val btn_lspace = S.C.btn_lspace.current
    val btn_PaddingTopH = S.C.btn_PaddingTopH.current
    val btn_PaddingTopV = S.C.btn_PaddingTopV.current
    val btn_TextFontsizeH = S.C.btn_TextFontsizeH.current
    val btn_TextFontsizeV = S.C.btn_TextFontsizeV.current
    val btn_Color1 = S.C.btn_Color1.current
    val btn_bgColor1 = S.C.btn_bgColor_pink.current
    val btnText1 = S.C.btnText5Apk1.current
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE // 是否横屏（宽高比）
    Button(modifier= Modifier
        .width(Button_Width.dp)
        .padding(top = if (isLandscape) btn_PaddingTopH else btn_PaddingTopV),
        colors = ButtonDefaults.buttonColors(
            containerColor = btn_bgColor1,
            contentColor = btn_Color1
        ), enabled = true, onClick = ApkBTNonClick1) {
        Text(
            text = btnText1,
            letterSpacing = btn_lspace,
            fontSize = if (isLandscape) btn_TextFontsizeH else btn_TextFontsizeV
        )
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
        MyUI(null, null, LocalContext.current)
    }
}