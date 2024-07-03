package sc.windom.sofill.Us

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLink
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
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import org.b3log.siyuan.sillot.util.FileUtil
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import java.text.DecimalFormat
import java.util.Locale

object U_FileUtils {

    // 将URI数据保存到缓存目录，并返回缓存文件
    fun saveUriToCache(context: Context, uri: Uri, cacheDir: File): File? {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val tempFile = File.createTempFile("cached-", ".tmp", cacheDir)
            tempFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            return tempFile
        }
        return null
    }

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
    @JvmStatic
    fun Context.workspaceParentDir(): String {
        val externalFilesDir = this.getExternalFilesDir(null)
        val filesDir = externalFilesDir ?: this.filesDir
        return filesDir.absolutePath
    }

    @SuppressLint("Range")
    fun getFileName(context: Context, uri: Uri): String? {
        var result: String? = null
        val scheme = uri.scheme

        if (scheme == "content") {
            // 对于内容URI，尝试使用ContentResolver查询文件名
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }
            // 如果是临时权限，尝试获取读取权限
            if (result == null) {
                // 检查是否有临时权限
                val flags = context.contentResolver.persistedUriPermissions
                val hasReadPermission = flags.any { it.uri == uri && it.isReadPermission }

                if (!hasReadPermission) {
                    // 尝试获取持久化权限
                    try {
                        val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        context.contentResolver.takePersistableUriPermission(uri, takeFlags)
                    } catch (e: SecurityException) {
                        // 处理异常，可能是没有权限或者其他原因
                        Log.e("getFileName", "Failed to take persistable permission", e)
                    }
                }
                result = getFileNameFromUri(context, uri)
            }
        } else if (scheme == "magnet") {
            // 对于磁力链接，直接返回整个链接作为文件名
            result = uri.toString()
        } else {
            // 对于其他类型的URI，尝试使用URI的最后一部分作为名称
            result = uri.lastPathSegment ?: uri.toString()
        }

        return result
    }

    private fun getFileNameFromUri(context: Context, uri: Uri): String? {
        val resolver = context.contentResolver
        val openFileDescriptor = resolver.openFileDescriptor(uri, "r", null)
        val fileDescriptor = openFileDescriptor?.fileDescriptor
        if (fileDescriptor != null) {
            val inputStream = FileInputStream(fileDescriptor).buffered()
            val path = File(context.cacheDir, "temp_file").apply {
                outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }.absolutePath
            return getFileName(context, path.toUri())
        }
        openFileDescriptor?.close()
        return null
    }
    fun getFileFromUri(context: Context, uri: Uri): File? {
        // 使用ContentResolver打开Uri对应的输入流
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val tempFile = createTempFile(context, uri)

        // 将输入流写入临时文件
        tempFile.outputStream().use { outputStream ->
            inputStream.copyTo(outputStream)
        }

        // 关闭输入流
        inputStream.close()

        return tempFile
    }

    private fun createTempFile(context: Context, uri: Uri): File {
        // 创建临时文件的目录
        val tempDirectory = File(context.cacheDir, "temp_files")
        tempDirectory.mkdirs()

        // 生成临时文件名
        val fileName = getFileName(context, uri) ?: "${System.currentTimeMillis()}.tmp"

        // 创建临时文件
        return File(tempDirectory, fileName).apply {
            createNewFile()
        }
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
        val fileSize =
            getFileSizeFromUri(contentResolver, uri) ?: return false // 获取文件大小，如果获取失败则返回 false
        val fileSizeInMB = fileSize / (1024 * 1024) // 将文件大小转换为 MB
        return fileSizeInMB > limitMB
    }

    /**
     * 根据 Uri 获取文件大小
     *
     * @param contentResolver ContentResolver
     * @param uri 目标文件夹的 uri
     * @see getFileSize
     */
    fun getFileSizeFromUri(contentResolver: ContentResolver, uri: Uri): Long? {
        var fileSize: Long? = null
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (sizeIndex >= 0 && cursor.moveToFirst()) {
                fileSize = cursor.getLong(sizeIndex)
            }
        }
        return fileSize
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
    fun copyFileToFolderByDocumentTree(
        context: Context,
        treeUri: Uri,
        targetFileName: String = "Untitled.sc",
        sourceFilePath: String,
        mimeType: String,
        move: Boolean = false
    ) {

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
        if (move) {
            sourceFile.delete()
        }
    }


    /**
     * 将源文件复制到应用私有的目录中。当文件存在时不会创建新的重命名文件。进行哈希检查来跳过覆写不会减少耗时因此不弄检查。
     *
     * @param targetFolderPath 目标文件夹的路径，相对于应用的私有目录。使用绝对路径，例： /storage/emulated/0/Android/data/sc.windom.sillot/files/sillot
     * @param targetFileName 目标文件的名称。
     * @param sourceFilePath 源文件的路径。
     */
    fun copyFileToMyAppFolder(
        targetFolderPath: String,
        targetFileName: String,
        sourceFilePath: String
    ) {
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
     * 获取文件或文件夹的大小
     *
     * @param uri 目标文件或文件夹的 uri
     */
    fun getFileOrFolderSize(file: File?): String {
        if (file != null) {
            return if (file.exists()) {
                val fileSizeInBytes: Double = if (file.isDirectory) {
                    getDirectorySize(file).toDouble()
                } else {
                    file.length().toDouble()
                }
                val units = arrayOf("B", "KB", "MB", "GB", "TB")
                var fileSize: Double = fileSizeInBytes
                var unitIndex = 0

                // 转换文件大小到合适的单位
                while (fileSize >= 1024 && unitIndex < units.size - 1) {
                    fileSize /= 1024
                    unitIndex++
                }

                // 格式化文件大小，保留两位小数
                val df = DecimalFormat("#.##")
                "${df.format(fileSize)}${units[unitIndex]}"
            } else {
                "File does not exist"
            }
        }
        return "unknown"
    }

    /**
     * 递归计算文件夹的大小
     *
     * @param directory 目标文件夹
     * @return 文件夹大小（以字节为单位）
     */
    private fun getDirectorySize(directory: File): Long {
        var length: Long = 0
        directory.listFiles()?.forEach {
            if (it.isFile) {
                length += it.length()
            } else if (it.isDirectory) {
                length += getDirectorySize(it)
            }
        }
        return length
    }

    /**
     * 扩展 File 类，添加一个获取文件大小的属性
     */
    val File.sizeInBytes: Long
        get() = if (isDirectory) getDirectorySize(this) else length()

    /**
     * 递归计算文件和目录
     */
    fun File.getSizeRecursively(): Long {
        if (!this.exists()) return 0
        return if (this.isFile) this.length() else this.listFiles()?.sumOf { it.getSizeRecursively() } ?: 0
    }

    /**
     * 简单版，不支持 content:// 协议查询
     *
     * @param uri 目标文件的 uri
     * @see .getFileSize(context: Context, uri: Uri) 增强版函数，支持 content:// 协议查询
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
     * @see getFileSizeFromUri
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
            "未知大小"
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


    /**
     * 对于非文件的链接，返回自定义的MIME类型
     */
    fun getMimeType(context: Context, uri: Uri): String? {
        var mimeType: String? = context.contentResolver.getType(uri)

        if (mimeType == null) {
            val scheme = uri.scheme
            when (scheme) {
                "magnet" -> {
                    // 磁力链接通常用于BitTorrent，但没有官方的MIME类型
                    mimeType = "application/x-magnet"
                }
                // 添加更多的case来处理其他非标准URI的scheme
            }
        }

        return mimeType
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
            // 自定义的MIME类型::开始
            fileType == "磁力链接" -> Icons.Default.AddLink
            // 自定义的MIME类型::结束
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

    /**
     * 为了避免重复请求下载的情况（例如先请求 https://.../eg 然后请求 https://.../eg.zip），采用 mimeType + fileName 联合判断
     */
    fun isCommonSupportDownloadMIMEType(mimeType: String, fileName: String): Boolean {
        val fn = fileName.lowercase(Locale.CHINESE)
        when {
            fn.endsWith(".apk.1") -> {
                return true
            }
        }
        when (mimeType) {
            "video/mp4" -> return fn.endsWith(".mp4")
            "audio/mpeg" -> return fn.endsWith(".mp3")
            "audio/x-wav" -> return fn.endsWith(".wav")
            "application/vnd.android.package-archive" -> return fn.endsWith(".apk")
            "application/pdf" -> return fn.endsWith(".pdf")
            "application/zip" -> return fn.endsWith(".zip")
            "application/epub+zip" -> return fn.endsWith(".epub")
            "application/msword" -> return fn.endsWith(".doc") || fn.endsWith(".dot")
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> return fn.endsWith(".docx")
            "application/vnd.ms-excel" -> return fn.endsWith(".xls") || fn.endsWith(".xlt") || fn.endsWith(".xlm") || fn.endsWith(".xlsm")
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" -> return fn.endsWith(".xlsx")
            "application/vnd.ms-powerpoint" -> return fn.endsWith(".ppt") || fn.endsWith(".pps") || fn.endsWith(".pot")
            "application/vnd.openxmlformats-officedocument.presentationml.presentation" -> return fn.endsWith(".pptx")
        }
        return false
    }

    fun getFileMIMEType(mimeType: String, fileName: String = ""): String {
        when {
            fileName.endsWith(".apk.1") -> {
                return "程序"
            }
        }
        return when {
            // 自定义的MIME类型::开始
            mimeType == "application/x-magnet" -> "磁力链接"
            // 自定义的MIME类型::结束

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

}