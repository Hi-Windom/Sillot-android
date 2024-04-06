package org.b3log.siyuan.producer

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.b3log.siyuan.andapi.Toast
import org.b3log.siyuan.videoPlayer.SimplePlayer
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.DecimalFormat

// TODO: 多选文件打开的处理
// TODO: 文件覆盖提醒
// TODO: 缓存清理
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
            val ft = getMimeType(this,uri)?.let { getFileType(it) }
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
    return when (fileType) {
        "视频" -> Icons.Default.Movie
        "音频" -> Icons.Default.MusicNote
        "文本" -> Icons.Default.Description
        "图像" -> Icons.Default.Image
        "程序" -> Icons.Default.Android
        else -> Icons.AutoMirrored.Filled.InsertDriveFile // 默认图标，您可以根据需要修改
    }
}
fun getFileType(mimeType: String): String {
    return when {
        mimeType.startsWith("video/") -> "视频"
        mimeType.startsWith("audio/") -> "音频"
        mimeType.startsWith("text/") -> "文本"
        mimeType.startsWith("image/") -> "图像"
        mimeType == "application/vnd.android.package-archive" -> "程序"
        else -> "其他"
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
    val file = File(uri.path)
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

// 根据 Uri 获取文件对象
fun getFileFromUri(context: Context, uri: Uri): File? {
    if (!isStorageSpaceAvailable(context.contentResolver, uri)) {
        // 存储空间不足，处理逻辑
        Toast.Show(context, "存储空间不足，请先清理")
        return null
    }
    val inputStream = context.contentResolver.openInputStream(uri)
    val file = File.createTempFile("temp_file", null)
    val outputStream = FileOutputStream(file)
    inputStream?.use { input ->
        outputStream.use { output ->
            val buffer = ByteArray(8 * 1024) // 缓冲区大小为 8 KB
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                output.write(buffer, 0, bytesRead)
            }
            output.flush()
        }
    }
    return file
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

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, DelicateCoroutinesApi::class)
@Composable
fun MyUI(intent: Intent?, fileType: String?, context: Context) {
    val TAG = "MainPro-MyUI"
    val uri = intent?.data
    val coroutineScope = rememberCoroutineScope()
    val fileName = uri?.let { getFileName(context, it) }
    val fileSize = uri?.let { getFileSize(it) }
//    val mimeType = intent?.data?.let { getMimeType(context, it) }
//    val fileType = mimeType?.let { getFileType(it) }
    val Thumbnail_Height = 250
    val Button_Width = 300
    val btn_lspace = 0.1.em
    val btn_PaddingTopH = 3.dp
    val btn_PaddingTopV = 6.dp
    val btn_TextFontsizeH = 20.sp
    val btn_TextFontsizeV = 18.sp
    val btn_Color1 = Color.White
    val btn_bgColor1 = Color(0xFF2196F3)
    val btn_bgColor2 = Color(0xFF1976D2)
    val btn_bgColor3 = Color(0xFF2391B5)
    val btn_bgColor4 = Color(0xFF237A58)
    val btnText1 = "分享"
    val btnText2 = "复制到剪贴板"
    val btnText3 = "保存到指定文件夹"
    val btnText4 = "存入工作空间级资源目录"
    var progressValue by remember { mutableStateOf(0) }
    var isButton3OnClickRunning by remember { mutableStateOf(false) }
    var isButton4OnClickRunning by remember { mutableStateOf(false) }
    var destinationDir by remember {
        mutableStateOf(File(context.getExternalFilesDir(null), "sillot/data/assets"))
    }
    var sourceFile by remember {
        mutableStateOf(uri?.let { getFileFromUri(context, it) })
    }
    var selectedFolder by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                // Get the selected folder path from the URI
                selectedFolder = uri
            }
        }
    }
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    LaunchedEffect(key1 = isButton3OnClickRunning) {
        if (isButton3OnClickRunning && selectedFolder!=null) {
            // 启动一个协程来执行任务
            coroutineScope.launch {
                withContext(Dispatchers.IO) {
                    var inputStream: BufferedInputStream? = null
                    var outputStream: BufferedOutputStream? = null

                    try {
                        inputStream = BufferedInputStream(FileInputStream(sourceFile))
                        val destinationFile =
                            (fileName ?: sourceFile?.name)?.let { File(destinationDir, it) }
                        outputStream = BufferedOutputStream(FileOutputStream(destinationFile))

                        val buffer = ByteArray(1024 * 64) // 64KB的缓冲区
                        val totalBytes = sourceFile?.length()
                        var bytesCopied = 0L

                        var bytesRead: Int
                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                            bytesCopied += bytesRead

                            if (totalBytes != null) {
                                progressValue = (bytesCopied.toFloat() / totalBytes.toFloat() * 100).toInt()
                            }
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } finally {
                        outputStream?.flush()
                        outputStream?.close()
                        inputStream?.close()
                    }
                }
                withContext(Dispatchers.Main) {
                    Toast.Show(context, "已复制到指定文件夹")
                }
                // 执行任务完成后，关闭遮罩
                isButton3OnClickRunning = false
                selectedFolder = null
            }
        }
    }
    LaunchedEffect(key1 = isButton4OnClickRunning) {
        if (isButton4OnClickRunning) {
            // 启动一个协程来执行任务
            coroutineScope.launch {
                withContext(Dispatchers.IO) {
                    var inputStream: BufferedInputStream? = null
                    var outputStream: BufferedOutputStream? = null

                    try {
                        inputStream = BufferedInputStream(FileInputStream(sourceFile))
                        val destinationFile =
                            (fileName ?: sourceFile?.name)?.let { File(destinationDir, it) }
                        outputStream = BufferedOutputStream(FileOutputStream(destinationFile))

                        val buffer = ByteArray(1024 * 64) // 64KB的缓冲区
                        val totalBytes = sourceFile?.length()
                        var bytesCopied = 0L

                        var bytesRead: Int
                        while (inputStream!!.read(buffer).also { bytesRead = it } != -1) {
                            outputStream!!.write(buffer, 0, bytesRead)
                            bytesCopied += bytesRead

                            if (totalBytes != null) {
                                progressValue = (bytesCopied.toFloat() / totalBytes.toFloat() * 100).toInt()
                            }
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } finally {
                        outputStream?.flush()
                        outputStream?.close()
                        inputStream?.close()
                    }
                }
                            withContext(Dispatchers.Main) {
                                Toast.Show(context, "已存入")
                            }
                // 执行任务完成后，关闭遮罩
                isButton4OnClickRunning = false
                selectedFolder = null
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
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            // 遮罩内容
            Box(modifier = Modifier.fillMaxSize()) {
                // 这里可以添加一些内容，比如进度条
                LinearProgressIndicator(
                    progress = { progressValue / 100f },
                    modifier = Modifier.padding(2.dp).height(13.dp).fillMaxWidth().align(Alignment.Center),
                )
                Text("${progressValue / 100f} / 100")
            }
        }
    }
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
        if (isLandscape) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                    // 横屏
                    LazyColumn(modifier = Modifier.weight(1f).padding(horizontal = 8.dp, vertical = 4.dp).fillMaxHeight(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                        item {
                            // 显示图像缩略图或文件类型图标
                            Box(modifier = Modifier
                                .size(Thumbnail_Height.dp)
                                .fillMaxSize()) {
                                if (fileType == "图像") {
                                    val bitmap = uri?.let { it1 ->
                                        context.contentResolver?.loadThumbnail(it1,
                                            Size(Thumbnail_Height, Thumbnail_Height), null)
                                    }
                                    bitmap?.let {
                                        Image(bitmap = it.asImageBitmap(), contentDescription = "Thumbnail", modifier = Modifier
                                            .size(Thumbnail_Height.dp)
//                                            .fillParentMaxSize(0.9f)
                                        )
                                    }
                                } else {
                                    val icon = fileType?.let { it1 -> getIconForFileType(it1) }
                                    icon?.let { it1 ->
                                        Icon(
                                            imageVector = it1,
                                            contentDescription = "File Type Icon",
                                            modifier = Modifier
                                                .size(Thumbnail_Height.dp)
//                                                .fillParentMaxSize(0.9f)
                                        )
                                    }
//                    Image( // 对应的是 R.drawable.id 方案
//                        painter = painterResource(id = icon),
//                        contentDescription = null,
//                        modifier = Modifier.size(100.dp)
//                    )
                                }
                            }

                            fileName?.let { it1 ->
                                Text(
                                    text = it1, // 使用获取到的文件名
                                    fontSize = 20.sp, // 横屏字体小一点
                                    modifier = Modifier.padding(8.dp).fillParentMaxWidth(0.8f)
                                )
                            }
                            Text(
                                text = "$fileSize ($fileType)",
                                fontSize = 13.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )

                        }
                    }
                    LazyColumn(modifier = Modifier.weight(1f).padding(start = 0.dp, top = 8.dp, end = 8.dp, bottom = 4.dp
                    ).fillMaxHeight(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                        item {

                            Button(modifier= Modifier
                                .width(Button_Width.dp)
                                .padding(top = btn_PaddingTopH),
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
                                    fontSize = btn_TextFontsizeH
                                )
                            }

                            Button(modifier= Modifier
                                .width(Button_Width.dp)
                                .padding(top = btn_PaddingTopH),
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
                                    fontSize = btn_TextFontsizeH
                                )
                            }
                            Button(modifier= Modifier
                                .width(Button_Width.dp)
                                .padding(top = btn_PaddingTopH),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = btn_bgColor3,
                                    contentColor = btn_Color1
                                ), enabled = true, onClick = {
                                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                                launcher.launch(intent)
                                // 非阻塞
                            }) {
                                Text(
                                    text = btnText3,
                                    letterSpacing = btn_lspace,
                                    fontSize = btn_TextFontsizeH
                                )
                            }

                            Button(modifier= Modifier
                                .width(Button_Width.dp)
                                .padding(top = btn_PaddingTopH),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = btn_bgColor4,
                                    contentColor = btn_Color1
                                ), enabled = true, onClick = {
                                if (uri!=null) {
                                    coroutineScope.launch {
                                        destinationDir = File(context.getExternalFilesDir(null), "sillot/data/assets")
                                        sourceFile = getFileFromUri(context, uri)
                                        if (sourceFile == null || isFileSizeOverLimit(context.contentResolver, uri, 5858)) {
                                            withContext(Dispatchers.Main) {
                                                Toast.Show(context, "暂不支持超大文件或文件不存在")
                                            }
                                        } else {
                                            Log.e(TAG,destinationDir.toString())
                                            isButton4OnClickRunning = true // 值变化时会触发重组
                                        }
                                    }
                                }
                            }) {
                                Text(
                                    text = btnText4,
                                    letterSpacing = btn_lspace,
                                    fontSize = btn_TextFontsizeH
                                )
                            }

                        }
                    }

                selectedFolder?.let { folder ->
                    val folderPath = "/storage/emulated/0/" + (folder.lastPathSegment?.split(":")
                        ?.last() ?: "")
                    if (folderPath!="") {
                        Log.e(TAG, "Selected Folder: $folderPath")
                        Text("Selected Folder: $folderPath")
                        destinationDir = File(folderPath)
                        sourceFile = uri?.let { it1 -> getFileFromUri(context, it1) }
                        isButton3OnClickRunning = true // 值变化时会触发重组
                    }
                }
            }
        } else {
            // 竖屏
            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                item {
                    // 显示图像缩略图或文件类型图标
                    Box(modifier = Modifier
                        .size(Thumbnail_Height.dp)
                        .fillMaxSize()) {
                        if (fileType == "图像") {
                            val bitmap = uri?.let { it1 ->
                                context.contentResolver?.loadThumbnail(it1,
                                    Size(Thumbnail_Height, Thumbnail_Height), null)
                            }
                            bitmap?.let {
                                Image(bitmap = it.asImageBitmap(), contentDescription = "Thumbnail", modifier = Modifier
                                    .size(Thumbnail_Height.dp)
                                    .fillMaxSize())
                            }
                        } else {
                            val icon = fileType?.let { it1 -> getIconForFileType(it1) }
                            icon?.let { it1 ->
                                Icon(
                                    imageVector = it1,
                                    contentDescription = "File Type Icon",
                                    modifier = Modifier
                                        .size(Thumbnail_Height.dp)
                                        .fillMaxSize()
                                )
                            }
//                    Image( // 对应的是 R.drawable.id 方案
//                        painter = painterResource(id = icon),
//                        contentDescription = null,
//                        modifier = Modifier.size(100.dp)
//                    )
                        }
                    }

                    fileName?.let { it1 ->
                        Text(
                            text = it1, // 使用获取到的文件名
                            fontSize = 24.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    Text(
                        text = "$fileSize ($fileType)",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Button(modifier= Modifier
                        .width(Button_Width.dp)
                        .padding(top = btn_PaddingTopV),
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
                            fontSize = btn_TextFontsizeV
                        )
                    }

                    Button(modifier= Modifier
                        .width(Button_Width.dp)
                        .padding(top = btn_PaddingTopV),
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
                            fontSize = btn_TextFontsizeV
                        )
                    }
                    Button(modifier= Modifier
                        .width(Button_Width.dp)
                        .padding(top = btn_PaddingTopV),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = btn_bgColor3,
                            contentColor = btn_Color1
                        ), enabled = true, onClick = {
                        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                        launcher.launch(intent)
                        // 非阻塞
                    }) {
                        Text(
                            text = btnText3,
                            letterSpacing = btn_lspace,
                            fontSize = btn_TextFontsizeV
                        )
                    }

                    Button(modifier= Modifier
                        .width(Button_Width.dp)
                        .padding(top = btn_PaddingTopV),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = btn_bgColor4,
                            contentColor = btn_Color1
                        ), enabled = true, onClick = {
                        if (uri!=null) {
                            coroutineScope.launch {
                                destinationDir = File(context.getExternalFilesDir(null), "sillot/data/assets")
                                sourceFile = getFileFromUri(context, uri)
                                if (sourceFile == null || isFileSizeOverLimit(context.contentResolver, uri, 5858)) {
                                    withContext(Dispatchers.Main) {
                                        Toast.Show(context, "暂不支持超大文件或文件不存在")
                                    }
                                } else {
                                    Log.e(TAG,destinationDir.toString())
                                    isButton4OnClickRunning = true // 值变化时会触发重组
                                }
                            }
                        }
                    }) {
                        Text(
                            text = btnText4,
                            letterSpacing = btn_lspace,
                            fontSize = btn_TextFontsizeV
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))


                    selectedFolder?.let { folder ->
                        val folderPath = "/storage/emulated/0/" + (folder.lastPathSegment?.split(":")
                            ?.last() ?: "")
                        if (folderPath!="") {
                            Log.e(TAG, "Selected Folder: $folderPath")
                            Text("Selected Folder: $folderPath")
                            destinationDir = File(folderPath)
                            sourceFile = uri?.let { it1 -> getFileFromUri(context, it1) }
                            isButton3OnClickRunning = true // 值变化时会触发重组
                        }
                    }
                }
            }

        }

    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyUI(null, null, LocalContext.current)
}