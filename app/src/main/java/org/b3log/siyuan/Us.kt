package org.b3log.siyuan

import android.Manifest
import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.blankj.utilcode.util.ActivityUtils.startActivity
import com.kongzue.dialogx.dialogs.PopNotification
import com.kongzue.dialogx.dialogs.PopTip
import org.b3log.siyuan.andapi.Toast
import org.b3log.siyuan.videoPlayer.SimplePlayer
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import kotlin.math.pow
import kotlin.math.sqrt


object Us {
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

    fun getMimeType(context: Context, uri: Uri): String? {
        return context.contentResolver.getType(uri)
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