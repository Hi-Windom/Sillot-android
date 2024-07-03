package sc.windom.sofill

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Base64
import android.util.DisplayMetrics
import android.util.Log
import android.view.ViewConfiguration
import android.view.Window
import android.view.WindowManager
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.core.content.FileProvider
import com.blankj.utilcode.util.ActivityUtils.startActivity
import com.kongzue.dialogx.dialogs.PopNotification
import com.kongzue.dialogx.dialogs.PopTip
import com.tencent.mmkv.MMKV
import org.b3log.siyuan.MainActivity
import org.b3log.siyuan.Utils
import org.b3log.siyuan.videoPlayer.SimplePlayer
import sc.windom.sofill.Us.Toast
import sc.windom.sofill.Us.U_DEBUG
import sc.windom.sofill.Us.U_DOSC
import sc.windom.sofill.Us.U_DialogX
import sc.windom.sofill.Us.U_FileUtils
import sc.windom.sofill.Us.U_FuckOtherApp
import sc.windom.sofill.Us.U_Layout
import sc.windom.sofill.Us.U_Permission
import sc.windom.sofill.Us.U_Phone
import sc.windom.sofill.Us.U_Pro
import sc.windom.sofill.Us.U_Safe
import sc.windom.sofill.Us.U_Uri
import java.io.File
import java.net.URLDecoder
import java.net.URLEncoder
import java.text.SimpleDateFormat
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec


object U {
    private val TAG = "sc.windom.sofill.U"
    val dateFormat_full1 = SimpleDateFormat("yyyyMMdd-HHmmss")

    @JvmStatic
    val DialogX = U_DialogX

    @JvmStatic
    val FuckOtherApp = U_FuckOtherApp

    @JvmStatic
    val FileUtils = U_FileUtils

    @JvmStatic
    val PS = U_Permission

    @JvmStatic
    val SAFE = U_Safe

    @JvmStatic
    val DEBUG = U_DEBUG

    @JvmStatic
    val PHONE = U_Phone

    @JvmStatic
    val DOSC = U_DOSC

    @JvmStatic
    val LAYOUT = U_Layout

    @JvmStatic
    val PRO = U_Pro

    @JvmStatic
    val URI = U_Uri


    @SuppressLint("ObsoleteSdkInt")
    fun Activity.isInFreeformMode(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 从Android M开始，可以通过判断窗口属性来检测是否为Freeform模式
            window.attributes.layoutInDisplayCutoutMode == WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        } else {
            false
        }
    }

    fun Activity.isInSpecialMode(): Boolean {
        return this.isInMultiWindowMode || this.isInFreeformMode() || this.isInPictureInPictureMode
    }

    /**
     * 判断颜色是否为亮色
     */
    fun Int.isLightColor(): Boolean {
        val darkness = 1 - (0.299 * Color.red(this) + 0.587 * Color.green(this) + 0.114 * Color.blue(this)) / 255
        return darkness < 0.5
    }

    fun genSpannableColorfulString(text: CharSequence, color: Int): SpannableString {
        val spannableString = SpannableString(text)
        val foregroundColorSpan = ForegroundColorSpan(color)
        // 设置颜色范围为整个字符串，从起始位置0到字符串长度
        spannableString.setSpan(
            foregroundColorSpan,
            0,
            spannableString.length,
            Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        )
        return spannableString
    }

    fun genSpannableColorfulRangeString(
        text: CharSequence,
        color: Int,
        start: Int,
        end: Int
    ): SpannableString {
        val spannableString = SpannableString(text)
        val foregroundColorSpan = ForegroundColorSpan(color)
        // 设置颜色范围为整个字符串，从起始位置0到字符串长度
        spannableString.setSpan(foregroundColorSpan, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        return spannableString
    }

    /**
     * 计算可用高度
     */
    private fun computeUsableHeight(window: Window): Int {
        val rect = getVisibleRect(window)
        return rect.height()
    }

    /**
     * 获取视图的可见区域
     */
    private fun getVisibleRect(window: Window): Rect {
        val rect = Rect()
        window.decorView.getWindowVisibleDisplayFrame(rect)
        return rect
    }

    /**
     * 获取根视图的高度
     */
    private fun getRootViewHeight(window: Window): Int {
        return window.decorView.rootView.height
    }

    /**
     * 获取根视图的宽度
     */
    private fun getRootViewWidth(window: Window): Int {
        return window.decorView.rootView.width
    }

    /**
     * 获取显示度量
     */
    private fun getDisplayMetrics(activity: Activity): DisplayMetrics {
        val displayMetrics = DisplayMetrics()
        val windowManager = activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val windowMetrics = windowManager.currentWindowMetrics
        val bounds = windowMetrics.bounds
        displayMetrics.widthPixels = bounds.width()
        displayMetrics.heightPixels = bounds.height()
        return displayMetrics
    }

    /**
     * 获取导航栏高度
     */
    private fun getNavigationBarHeight(activity: Activity): Int {
        val hasMenuKey = ViewConfiguration.get(activity).hasPermanentMenuKey()
        return if (!hasMenuKey) {
            val resourceId =
                activity.resources.getIdentifier("navigation_bar_height", "dimen", "android")
            if (resourceId > 0) activity.resources.getDimensionPixelSize(resourceId) else 0
        } else {
            0
        }
    }

    /**
     * 截取webView快照(webView加载的整个内容的大小)，本方法不适用于动态加载的网页
     * @param webView
     * @return
     */
    fun captureWebView(webView: WebView): Bitmap {
        val bmp: Bitmap
        val width = webView.width
        val height = webView.height

        // 如果WebView尚未测量其内容，则调用measure和layout以确保尺寸是正确的
        webView.measure(width, height)
        webView.layout(0, 0, width, height)

        // 创建与WebView尺寸相同的Bitmap
        bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        // 在Bitmap上创建一个Canvas，并通过WebView的draw方法绘制内容
        val canvas = Canvas(bmp)
        webView.draw(canvas)
        return bmp
    }

    /**
     * 禁止截屏
     */
    fun Activity.disableScreenshot() {
        this.window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }

    /**
     * 允许截屏
     */
    fun Activity.enableScreenshot() {
        this.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }

    fun WebView.isAndroidDarkMode(): Boolean {
        val currentNightMode =
            this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }

    fun isSystemDarkMode(context: Context): Boolean {
        val currentNightMode =
            context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
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
                inputStr.substring(0, startLength) + "*".repeat(starsCount) + inputStr.substring(
                    length - endLength
                )
            } else {
                inputStr.substring(
                    0,
                    startLength + starsCount
                ) + inputStr.substring(length - endLength)
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

    fun openUrl(url: String, noBrowser: Boolean = false) {
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
                    activity.startActivityForResult(
                        intent,
                        S.REQUEST_CODE.REQUEST_CODE_INSTALL_PERMISSION
                    )
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
            PopNotification.show(
                "任务失败",
                "没有找到可以安装APK的应用，请尝试使用文件管理器或其他第三方应用打开APK文件。"
            )
        } catch (e: Exception) {
            Log.e("Us.installApk", e.toString())
            PopNotification.show("任务失败", e.toString()).noAutoDismiss()
        }
    }

    /**
     * 使用第三方应用打开视频文件。
     *
     * @param activity 上下文Activity
     * @param videoUri 视频文件的Uri
     */
    fun openVideoWithThirdPartyApp(activity: Activity, videoUri: Uri) {
        try {
            val videoIntent = Intent(Intent.ACTION_VIEW)
            videoIntent.setDataAndType(videoUri, "video/*")
            videoIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

            // 确保第三方应用有读取该URI的权限
            videoIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val chooserIntent = Intent.createChooser(videoIntent, "选择处理此视频的应用")

            // 如果有可以处理该意图的应用，则启动选择器对话框
            if (videoIntent.resolveActivity(activity.packageManager) != null) {
                activity.startActivity(chooserIntent)
            } else {
                // 如果没有找到可以处理的应用，提示用户
                PopNotification.show("任务失败", "没有找到可以播放此视频的应用")
            }
        } catch (e: ActivityNotFoundException) {
            // 如果没有找到可以处理的应用，提示用户
            PopNotification.show("任务失败", "没有找到可以播放此视频的应用")
        } catch (e: Exception) {
            // 其他异常处理
            PopNotification.show("任务失败", "打开视频时出错: ${e.message}")
        }
    }

    /**
     * 使用第三方应用打开视频文件。
     *
     * @param activity 上下文Activity
     * @param audioUri 音频文件的Uri
     */
    fun openAudioWithThirdPartyApp(activity: Activity, audioUri: Uri) {
        try {
            val videoIntent = Intent(Intent.ACTION_VIEW)
            videoIntent.setDataAndType(audioUri, "audio/*")
            videoIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

            // 确保第三方应用有读取该URI的权限
            videoIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val chooserIntent = Intent.createChooser(videoIntent, "选择处理此音频的应用")

            // 如果有可以处理该意图的应用，则启动选择器对话框
            if (videoIntent.resolveActivity(activity.packageManager) != null) {
                activity.startActivity(chooserIntent)
            } else {
                // 如果没有找到可以处理的应用，提示用户
                PopNotification.show("任务失败", "没有找到可以播放此视频的应用")
            }
        } catch (e: ActivityNotFoundException) {
            // 如果没有找到可以处理的应用，提示用户
            PopNotification.show("任务失败", "没有找到可以播放此视频的应用")
        } catch (e: Exception) {
            // 其他异常处理
            PopNotification.show("任务失败", "打开音频时出错: ${e.message}")
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