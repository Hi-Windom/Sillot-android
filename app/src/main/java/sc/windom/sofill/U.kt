package sc.windom.sofill

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.KeyguardManager
import android.content.ActivityNotFoundException
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.database.Cursor
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.webkit.WebSettings
import androidx.compose.material.icons.Icons
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
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import com.blankj.utilcode.util.ActivityUtils.startActivity
import com.kongzue.dialogx.dialogs.PopNotification
import com.kongzue.dialogx.dialogs.PopTip
import com.tencent.mmkv.MMKV
import org.b3log.siyuan.MainActivity
import org.b3log.siyuan.Utils
import org.b3log.siyuan.andapi.Toast
import org.b3log.siyuan.sillot.util.FileUtil
import org.b3log.siyuan.videoPlayer.SimplePlayer
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.URLDecoder
import java.net.URLEncoder
import java.security.MessageDigest
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import kotlin.math.pow
import kotlin.math.sqrt


object U {
    val dateFormat_full1 = SimpleDateFormat("yyyyMMdd-HHmmss")

    /**
     * 获取应用文件目录，它会自动处理多用户的情况。注意，应用文件目录包括了其外部存储的文件目录
     * @return 例如 /data/user/$userId/$packageName/files
     */
    fun Context.usertDir(): String {
        return this.filesDir.absolutePath
    }

    /**
     * 本质上是获取外部存储的文件目录的绝对路径，它会自动处理多用户的情况。如果外部存储不可用，则返回内部存储的文件目录
     * @return 例如 /storage/emulated/$userId/Android/data/$packageName/files
     */
    fun Context.workspaceParentDir(): String {
        val externalFilesDir = this.getExternalFilesDir(null)
        val filesDir = externalFilesDir ?: this.filesDir
        return filesDir.absolutePath
    }
    /**
     * @param blockURL: 格式为 siyuan://blocks/xxx
     */
    fun startMainActivityWithBlock(blockURL: String, applicationContext: Context) {
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.putExtra("blockURL", blockURL)
        startActivity(intent)
    }
    fun getSignalStrengthLevel(signalStrength: Int): String {
        val signalStrengthLevel: String
        signalStrengthLevel = if (signalStrength >= -50) {
            "极好"
        } else if (signalStrength >= -60) {
            "很好"
        } else if (signalStrength >= -70) {
            "正常"
        } else if (signalStrength >= -80) {
            "一般"
        } else if (signalStrength >= -90) {
            "较弱"
        } else {
            "极弱"
        }
        return signalStrengthLevel
    }
    fun checkWebViewVer(ws: WebSettings): String {
        val ua = ws.userAgentString
        var webViewVer = ""
        if (ua.contains("Chrome/")) {
            val minVer = 95
            try {
                val chromeVersion = ua.split("Chrome/".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()[1].split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()[0]
                if (chromeVersion.contains(".")) {
                    val chromeVersionParts =
                        chromeVersion.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()
                    webViewVer = chromeVersionParts[0]
                    if (webViewVer.toInt() < minVer) {
                        PopTip.show("WebView version $webViewVer is too low, please upgrade to $minVer+")
                    }
                }
            } catch (e: java.lang.Exception) {
                Utils.LogError("boot", "check webview version failed", e)
                PopTip.show("Check WebView version failed: " + e.message)
            }
        }
        return webViewVer
    }
    fun replaceScheme_deepDecode(url: String, old: String, new: String): String {
        // 解码URL
        var decodedUrl = URLDecoder.decode(url, "UTF-8")
        // 替换scheme
        decodedUrl = decodedUrl.replace(old, new)
        var previousUrl: String
        do {
            previousUrl = decodedUrl
            // 再次解码URL
            decodedUrl = URLDecoder.decode(decodedUrl, "UTF-8")
        } while (decodedUrl != previousUrl)

        return decodedUrl
    }
    fun replaceEncodeScheme(url: String, old: String, new: String): String {
        return url.replace(URLEncoder.encode(old), URLEncoder.encode(new))
    }
    fun parseAndDecodeUrl(url: String, regex: Regex): String {
        val decodedUrls = regex.findAll(url).map { matchResult ->
            val encodedUrl = matchResult.groupValues[1]
            URLDecoder.decode(encodedUrl, "UTF-8")
        }.joinToString(separator = " ", prefix = "\"", postfix = "\"")

        // 使用解码后的 URL 替换原始 URL 中的匹配部分
        return regex.replace(url, decodedUrls)
    }
    fun displayTokenEndLimiter(inputStr: String, endLength: Int): String {
        val length = inputStr.length
        return if (length >= endLength) {
            "*".repeat(length - endLength) + inputStr.substring(length - endLength)
        } else {
            inputStr
        }
    }
    fun displayTokenLimiter(inputStr: String, startLength: Int, endLength: Int): String {
        val length = inputStr.length
        return if (length <= startLength) {
            inputStr
        } else {
            val starsCount = length - startLength - endLength
            if (starsCount > 0) {
                inputStr.substring(0, startLength) + "*".repeat(starsCount) + inputStr.substring(length - endLength)
            } else {
                inputStr.substring(0, startLength + starsCount) + inputStr.substring(length - endLength)
            }
        }
    }

    /**
     * 生成AES密钥
     *
     */
    fun generateAesKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(128) // 选择密钥大小，这里为128位
        return keyGenerator.generateKey()
    }


    /**
     * AES加密
     *
     */
    @SuppressLint("GetInstance")
    fun encryptAes(data: String, key: SecretKey): String {
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encryptedBytes = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
    }


    /**
     * AES解密
     *
     */
    @SuppressLint("GetInstance")
    fun decryptAes(encryptedData: String, key: SecretKey): String {
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.DECRYPT_MODE, key)
        val decryptedBytes = cipher.doFinal(Base64.decode(encryptedData, Base64.DEFAULT))
        return String(decryptedBytes, Charsets.UTF_8)
    }

    /**
     * 读取MMKV中存储的加密Token并进行解密
     *
     */
    fun getDecryptedToken(mmkv: MMKV, MMKV_KEY: String, AES_KEY: String): String? {
        // 从MMKV中读取存储的AES密钥
        val encodedKey = mmkv.decodeString(AES_KEY, null) ?: return null
        val keyBytes = Base64.decode(encodedKey, Base64.DEFAULT)
        val aesKey = SecretKeySpec(keyBytes, "AES")

        // 从MMKV中读取存储的加密Token
        val encryptedToken = mmkv.decodeString(MMKV_KEY, null) ?: return null

        // 解密Token
        return decryptAes(encryptedToken, aesKey)
    }

    fun isMIUI(applicationContext : Context): Boolean {
        val packageManager = applicationContext.packageManager
        val miuiPackageName = "com.miui.gallery"
        return try {
            packageManager.getPackageInfo(miuiPackageName, PackageManager.GET_META_DATA)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun isLargeScreenMachine(context: Context): Boolean {
        // 获取屏幕的方向
        val screenLayout = context.resources.configuration.screenLayout
        // 获取屏幕尺寸的掩码
        val sizeMask = Configuration.SCREENLAYOUT_SIZE_MASK
        // 获取屏幕尺寸的值
        val screenSize = screenLayout and sizeMask

        // 如果屏幕尺寸是超大屏或者巨屏，则可能是平板电脑
        return screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE ||
                screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE
    }
    fun isPad(context: Context): Boolean { // Converted from Utils.java
        val metrics = context.resources.displayMetrics
        val widthInches = metrics.widthPixels / metrics.xdpi
        val heightInches = metrics.heightPixels / metrics.ydpi
        val diagonalInches = sqrt(widthInches.toDouble().pow(2.0) + heightInches.toDouble()
            .pow(2.0)
        )
        return diagonalInches >= 7
    }

    fun isValidPermission(id: String?): Boolean { // Converted from Utils.java
        if (id.isNullOrEmpty()) {
            return false
        }
        try {
            // 使用反射获取 Manifest.permission 类中的所有静态字段
            val fields = Manifest.permission::class.java.getFields()
            for (field in fields) {
                // 检查是否存在与id匹配的静态字段
                if (field.type == String::class.java && field[null] == id) {
                    return false
                }
            }
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
            return false
        }
        return true
    }


    fun requestExternalStoragePermission(activity: Activity) {
        if (!canManageAllFiles(activity)) {
            val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            ActivityCompat.startActivityForResult(
                activity,
                intent,
                S.REQUEST_CODE_MANAGE_STORAGE,
                null
            )
        }
    }

    fun canManageAllFiles(context: Context): Boolean { // 管理所有文件
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED &&
                context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED
        // On older versions, we assume that the READ_EXTERNAL_STORAGE and WRITE_EXTERNAL_STORAGE
        // permissions are sufficient to manage all files.
    }

    fun canAccessDeviceState(context: Context): Boolean { // 访问设备状态信息
        return context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) ==
                PackageManager.PERMISSION_GRANTED
    }

    fun isIgnoringBatteryOptimizations(context: Context): Boolean { // 忽略电池优化
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: false
    }

    fun isShowingOnLockScreen(context: Context): Boolean { // 锁屏显示
        val keyguardManager = context.getSystemService(
            KeyguardManager::class.java
        )
        return keyguardManager?.isDeviceLocked ?: false
    }


    fun canShowOnTop(context: Context?): Boolean { // 悬浮窗
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else true
        // Assuming it's allowed on older versions
    }

    fun canPopInBackground(context: Context?): Boolean { // 后台弹出界面
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Settings.canDrawOverlays(context)
        } else true
        // Assuming it's allowed on older versions
    }

    fun canRequestPackageInstalls(context: Context): Boolean { // 安装未知应用
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else true
        // Assuming it's allowed on older versions
    }

    fun getDirectoriesInPath(path: String): List<String> {
        val directories = mutableListOf<String>()
        val file = File(path)
        if (file.exists() && file.isDirectory) {
            val files = file.listFiles()
            if (files != null) {
                for (currentFile in files) {
                    if (currentFile.isDirectory) {
                        directories.add(currentFile.name)
                    }
                }
            }
        }
        return directories
    }

    fun filesHaveSameHash(file1: File, file2: File): Boolean {
        val digest1 = getFileHash(file1)
        val digest2 = getFileHash(file2)
        return digest1.contentEquals(digest2)
    }

    fun getFileHash(file: File): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { inputStream ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest()
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

    /**
     * 根据 Uri 获取文件大小
     *
     * @param contentResolver ContentResolver
     * @param uri 目标文件夹的 uri
     */
    fun getFileSizeFromUri(contentResolver: ContentResolver, uri: Uri): Long? {
        return contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            cursor.moveToFirst()
            cursor.getLong(sizeIndex)
        }
    }

    /**
     * 从 uri 获取文件路径
     *
     * @param context Context
     * @param uri 目标文件夹的 uri
     */
    fun getPathFromUri(context: Context, uri: Uri): String? {
        var filePath: String? = null
        val contentResolver = context.contentResolver

        Log.i("getPathFromUri() -> ", uri.toString());
        // 根据不同的URI方案执行不同的处理
        when {
            "content" == uri.scheme -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Android 10及以上版本使用openFileDescriptor来获取文件路径
                    contentResolver.openFileDescriptor(uri, "r", null).use { parcelFileDescriptor ->
                        if (parcelFileDescriptor != null) {
                            val tempFile = File(context.cacheDir, "temp_file")
                            FileInputStream(parcelFileDescriptor.fileDescriptor).use { inputStream ->
                                FileOutputStream(tempFile).use { outputStream ->
                                    inputStream.copyTo(outputStream)
                                }
                            }

                            filePath = tempFile.absolutePath
                        }
                    }
                } else {
                    val cursor = contentResolver.query(uri, null, null, null, null)
                    cursor?.use {
                        if (it.moveToFirst()) {
                            // MediaStore.Images.Media.DATA 不存在则会 crash ，比如微信文件，但是安卓10以下不管了
                            val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                            filePath = it.getString(columnIndex)
                        }
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
                    filePath = FileUtil.getDataColumn(context, contentUri, null, null)
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
                    filePath = FileUtil.getDataColumn(context, contentUri, selection, selectionArgs)
                }
            }
        }
        filePath?.let { Log.i("getPathFromUri() -> ", it) };
        return filePath
    }

    /**
     * 检查URI是否是外部存储器文档
     *
     * @param uri 目标文件夹的 uri
     */
    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * 检查URI是否是下载文档
     *
     * @param uri 目标文件夹的 uri
     */
    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * 检查URI是否是媒体文档
     *
     * @param uri 目标文件夹的 uri
     */
    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
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
    fun copyFileToFolderByDocumentTree(context: Context, treeUri: Uri, targetFileName: String = "Untitled.sc", sourceFilePath: String, mimeType: String, move: Boolean = false) {

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


    /**
     * 将源文件复制到应用私有的目录中。当文件存在时不会创建新的重命名文件。进行哈希检查来跳过覆写不会减少耗时因此不弄检查。
     *
     * @param targetFolderPath 目标文件夹的路径，相对于应用的私有目录。使用绝对路径，例： /storage/emulated/0/Android/data/sc.windom.sillot/files/sillot
     * @param targetFileName 目标文件的名称。
     * @param sourceFilePath 源文件的路径。
     */
    fun copyFileToMyAppFolder(targetFolderPath: String, targetFileName: String, sourceFilePath: String) {
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
     * 简单版，如需支持 content:// 协议查询，请使用增强版 getFileSize(context: Context, uri: Uri)
     *
     * @param uri 目标文件的 uri
     *
     */
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

    /**
     * 增强版，支持 content:// 协议查询
     *
     * @param context 若要读取content://协议的文件大小，需要使用ContentResolver和Cursor。这是因为content:// URI通常指向设备上的内容提供器，这些内容提供器可能存储在数据库中，也可能存储在文件系统中，或者有其他的存储机制。
     * @param uri 目标文件的 uri
     */
    fun getFileSize(context: Context, uri: Uri): String {
        val contentResolver: ContentResolver = context.contentResolver
        var fileSizeInBytes: Long = 0

        // 尝试使用文件路径获取文件大小
        val file = uri.path?.let { File(it) }
        if (file != null && file.exists()) {
            fileSizeInBytes = file.length()
        } else {
            // 尝试使用ContentResolver查询
            val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val sizeIndex = it.getColumnIndexOrThrow(OpenableColumns.SIZE)
                    fileSizeInBytes = it.getLong(sizeIndex)
                }
            }
        }

        // 如果文件大小不为0，转换并格式化文件大小
        return if (fileSizeInBytes > 0) {
            formatFileSize(fileSizeInBytes)
        } else {
            "unknown"
        }
    }

    fun formatFileSize(fileSizeInBytes: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var fileSize = fileSizeInBytes.toDouble()
        var unitIndex = 0

        // 转换文件大小到合适的单位
        while (fileSize >= 1024 && unitIndex < units.size - 1) {
            fileSize /= 1024
            unitIndex++
        }

        // 格式化文件大小，保留两位小数
        val df = DecimalFormat("#.##")
        return "${df.format(fileSize)}${units[unitIndex]}"
    }

    fun getMimeType(context: Context, uri: Uri): String? {
        return context.contentResolver.getType(uri)
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
            else -> Icons.Default.QuestionMark // 默认图标
        }
    }


    fun getFileMIMEType(mimeType: String, fileName: String=""): String {
        when {
            fileName.endsWith(".apk.1") -> { return "程序" }
        }
        return when {
            mimeType.startsWith("video/") -> {
                when (mimeType) {
                    "video/mp4" -> "MP4 视频"
                    "video/mpeg" -> "MPEG 视频"
                    "video/quicktime" -> "QuickTime 视频"
                    "video/x-msvideo" -> "AVI 视频"
                    "video/x-flv" -> "FLV 视频"
                    "video/x-matroska" -> "Matroska 视频"
                    "video/webm" -> "WebM 视频"
                    else -> "其他视频"
                }
            }
            mimeType.startsWith("audio/") -> {
                when (mimeType) {
                    "audio/mpeg" -> "MP3 音频"
                    "audio/x-wav" -> "WAV 音频"
                    "audio/ogg" -> "OGG 音频"
                    "audio/aac" -> "AAC 音频"
                    "audio/flac" -> "FLAC 音频"
                    "audio/amr" -> "AMR 音频"
                    "audio/midi" -> "MIDI 音频"
                    "audio/x-ms-wma" -> "WMA 音频"
                    "audio/x-aiff" -> "AIFF 音频"
                    "audio/x-ms-wmv" -> "WMV 音频"
                    "audio/mp4" -> "M4A 音频"
                    else -> "其他音频"
                }
            }
            mimeType.startsWith("text/") -> {
                when (mimeType) {
                    "text/plain" -> "文本"
                    "text/html" -> "HTML"
                    "text/css" -> "CSS"
                    "text/javascript" -> "JavaScript"
                    else -> "其他文本"
                }
            }
            mimeType.startsWith("image/") -> {
                when (mimeType) {
                    "image/jpeg" -> "JPEG 图像"
                    "image/png" -> "PNG 图像"
                    "image/gif" -> "GIF 图像"
                    "image/bmp" -> "BMP 图像"
                    "image/webp" -> "WebP 图像"
                    "image/tiff" -> "TIFF 图像"
                    "image/tiff-fx" -> "TIFF-FX 图像"
                    else -> "其他图像"
                }
            }
            mimeType.startsWith("application/") -> {
                when (mimeType) {
                    "application/vnd.android.package-archive" -> "程序"
                    "application/pdf" -> "PDF"
                    "application/zip" -> "压缩文件"
                    "application/epub+zip" -> "EPUB"
                    "application/msword" -> "Word文档"
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> "Word文档"
                    "application/vnd.ms-excel" -> "Excel表格"
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" -> "Excel表格"
                    "application/vnd.ms-powerpoint" -> "PowerPoint演示文稿"
                    "application/vnd.openxmlformats-officedocument.presentationml.presentation" -> "PowerPoint演示文稿"
                    else -> mimeType
                }
            }
            // 其他类型
            else -> mimeType
        }
    }

    fun openUrl(url: String, noBrowser: Boolean=false) {
        val i = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        if (noBrowser == true) {
            i.addFlags(Intent.FLAG_ACTIVITY_REQUIRE_NON_BROWSER)
        }
        try {
            startActivity(i)
        } catch (e: Exception) {
            PopNotification.show(e.message, e.stackTrace.toString()).noAutoDismiss()
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

    fun handleVideo(context: Context, uri: Uri) {
        val videoPath = if (uri.scheme == "file") {
            // 本地文件
            uri.path ?: ""
        } else {
            // URL
            uri.toString()
        }

        val intent = Intent(context, SimplePlayer::class.java)
        intent.putExtra("videoPath", videoPath)
        startActivity(intent)
    }

    fun installApk(activity: Activity, apkFile: File) {
        val installIntent: Intent
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            // Android N及以上版本需要使用FileProvider安装APK
            val apkUri = FileProvider.getUriForFile(
                activity,
                "${activity.packageName}.fileprovider",
                apkFile
            )
            installIntent = Intent(Intent.ACTION_INSTALL_PACKAGE)
            installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            installIntent.setData(apkUri)
        } else {
            // Android N以下版本直接使用文件路径
            val apkUri = Uri.fromFile(apkFile)
            installIntent = Intent(Intent.ACTION_VIEW)
            installIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            installIntent.setDataAndType(apkUri, "application/vnd.android.package-archive")
        }

        activity.startActivity(installIntent)
    }

    /**
    * 安装 apk 文件，需要申请权限。不申请权限安装请使用 installApk2
    */
    fun installApk(activity: Activity, apkUri: Uri) {
        try {
            val installIntent: Intent

            // 检查是否已有安装未知来源应用的权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val packageManager = activity.packageManager
                val hasInstallPermission = packageManager.canRequestPackageInstalls()
                if (!hasInstallPermission) {
                    Toast.Show(activity, "请先授予汐洛安装未知应用权限")
                    // 启动授权 activity
                    val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                    activity.startActivityForResult(intent, S.REQUEST_CODE_INSTALL_PERMISSION)
                    return
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // Android N及以上版本需要额外权限
                installIntent = Intent(Intent.ACTION_INSTALL_PACKAGE) // 忽略已弃用，神金搞那么复杂
                installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//                installIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION) // 安装应用不需要写权限，如果是另一个应用的私有文件会导致无法安装
            } else {
                // Android N以下版本
                installIntent = Intent(Intent.ACTION_VIEW)
                installIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            installIntent.setDataAndType(apkUri, "application/vnd.android.package-archive")
            activity.startActivity(installIntent)
        } catch (e: Exception) {
            Log.e("Us.installApk", e.toString())
            PopNotification.show("任务失败", e.toString()).noAutoDismiss()
        }
    }

    /**
     * 安装 apk 文件，无需申请权限，但是需要有对应处理软件（一般系统都自带）。申请权限安装请使用 installApk
     */
    fun installApk2(activity: Activity, apkUri: Uri) {
        try {
            val installIntent = Intent(Intent.ACTION_VIEW)
            installIntent.setDataAndType(apkUri, "application/vnd.android.package-archive")
            installIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

            // 如果是第三方软件提供的安装包，确保继承了读取权限
            installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            // 创建一个选择器对话框，让用户选择使用哪个应用来打开APK文件
            val chooserIntent = Intent.createChooser(installIntent, "选择安装应用的方式")

            // 启动系统提供的对话框，让用户选择处理意图的应用
            activity.startActivity(chooserIntent)
        } catch (e: ActivityNotFoundException) {
            // 如果没有找到可以处理的应用，提示用户
            PopNotification.show("任务失败", "没有找到可以安装APK的应用，请尝试使用文件管理器或其他第三方应用打开APK文件。")
        } catch (e: Exception) {
            Log.e("Us.installApk", e.toString())
            PopNotification.show("任务失败", e.toString()).noAutoDismiss()
        }
    }



    fun sendEmail(packageManager: PackageManager, recipient: String, subject: String?, body: String?) {
        val emailIntent = Intent(Intent.ACTION_SENDTO)
        emailIntent.setData(Uri.parse("mailto:")) // only email apps should handle this

        // 设置收件人
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
        // 设置邮件主题
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
        // 设置邮件正文
        emailIntent.putExtra(Intent.EXTRA_TEXT, body)
        if (emailIntent.resolveActivity(packageManager) != null) {
            startActivity(emailIntent)
        } else {
            PopTip.show("No email client found")
        }
    }

    fun deleteFileByUri(context: Context, uri: Uri): Boolean {
        // 获取ContentResolver实例
        val contentResolver = context.contentResolver

        // 尝试从内容提供者中删除文件
        try {
            // 删除文件，这个调用会同时从文件系统和内容提供者的数据库中删除文件
            val deletedRows = contentResolver.delete(uri, null, null)

            // 如果删除的行数大于0，则表示文件删除成功
            if (deletedRows > 0) {
                return true
            }
        } catch (e: Exception) {
            // 处理可能出现的异常，例如权限问题或文件不存在
            Log.e("FileDelete", "Error deleting file", e)
        }

        // 删除失败
        return false
    }

    fun notifyGallery(context: Context, imageUri: Uri) {
//        向系统相册发送媒体文件扫描广播来通知系统相册更新媒体库
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaScannerConnection.scanFile(
                context, arrayOf(imageUri.toString()), null
            ) { path: String, uri: Uri ->
                Log.i("ExternalStorage", "Scanned $path:")
                Log.i("ExternalStorage", "-> uri=$uri")
            }
        } else {
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            mediaScanIntent.setData(imageUri)
            context.sendBroadcast(mediaScanIntent)
        }
    }
    fun notifyGallery(context: Context, imageFile: File) {
//        向系统相册发送媒体文件扫描广播来通知系统相册更新媒体库
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaScannerConnection.scanFile(
                context, arrayOf(imageFile.toString()), null
            ) { path: String, uri: Uri ->
                Log.i("ExternalStorage", "Scanned $path:")
                Log.i("ExternalStorage", "-> uri=$uri")
            }
        } else {
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            val contentUri = Uri.fromFile(imageFile)
            mediaScanIntent.setData(contentUri)
            context.sendBroadcast(mediaScanIntent)
        }
    }

    fun notifyGallery(activity: Activity, imageFile: File) {
        notifyGallery(activity as Context, imageFile)
    }

}