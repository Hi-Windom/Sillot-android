package sc.windom.sofill.compose

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.PermissionRequest
import android.webkit.URLUtil
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cookie
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.filled.Webhook
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.app.ActivityCompat
import com.kongzue.dialogx.dialogs.PopTip
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import sc.windom.sofill.U
import sc.windom.sofill.Us.U_DEBUG
import sc.windom.sofill.android.webview.applySystemThemeToWebView
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class MenuOptionState {
    object Disabled : MenuOptionState()
    object Default : MenuOptionState()
    object Active : MenuOptionState()
}

data class MenuOption(
    val title: String,
    val icon: ImageVector,
    val iconInActive: ImageVector = icon,
    val titleInActive: String = title,
    val state: MenuOptionState = MenuOptionState.Default,
    val canToggle: Boolean = false, // 表示选项是否可以在 Default 和 Active 之间切换
    val closeMenuAfterClick: Boolean = true, // 点击后是否收起菜单
    val onClick: () -> Unit,
)

@Composable
fun Menu(options: List<MenuOption>, columnCount: Int, openBottomSheet: MutableState<Boolean>) {
    // 一行 n 列
    options.chunked(columnCount).forEach { rowOptions ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly // 均匀分布菜单项
        ) {
            rowOptions.forEach { option ->
                val tint = when (option.state) {
                    MenuOptionState.Disabled -> Color.Gray
                    MenuOptionState.Default -> Color.Black
                    MenuOptionState.Active -> Color.Blue
                }
                val isActive = if (option.canToggle) {
                    rememberSaveable { mutableStateOf(option.state == MenuOptionState.Active) }
                } else {
                    // 对于不可切换的选项，直接使用其初始状态
                    remember { mutableStateOf(option.state == MenuOptionState.Active) }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = tint != Color.Gray) {
                            if (option.canToggle) {
                                // 只有可切换的选项会在点击时切换状态
                                isActive.value = !isActive.value
                            }
                            if (option.closeMenuAfterClick) {
                                openBottomSheet.value = false
                            }
                            option.onClick()
                        }
                        .weight(1f), // 均分宽度
                    horizontalAlignment = Alignment.CenterHorizontally // 居中图标和文本
                ) {
                    Icon(
                        if (option.canToggle && isActive.value) option.iconInActive else option.icon,
                        contentDescription = null,
                        tint = if (option.canToggle && isActive.value) Color.Blue else tint,
                        modifier = Modifier.size(34.dp)
                    )
                    Text(
                        text = if (option.canToggle && isActive.value) option.titleInActive else option.title,
                        color = if (option.canToggle && isActive.value) Color.Blue else tint,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

/**
 * 参考了 https://www.composables.com/material3/modalbottomsheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialBottomMenu(
    openBottomSheet: MutableState<Boolean>,
    sheetContent: @Composable () -> Unit
) {
    var skipPartiallyExpanded by rememberSaveable { mutableStateOf(false) }
    val bottomSheetState =
        rememberModalBottomSheetState(skipPartiallyExpanded = skipPartiallyExpanded)

    // Sheet content
    if (openBottomSheet.value) {
        ModalBottomSheet(
            onDismissRequest = { openBottomSheet.value = false },
            sheetState = bottomSheetState
        ) {
            Column {
                sheetContent()
                HorizontalDivider(modifier = Modifier
                    .height(6.dp), thickness = 0.dp, color = Color.Transparent)
            }
        }
    }
}



@SuppressLint("SetJavaScriptEnabled")
@Composable
fun FullScreenWebView(activity: Activity, originUrl: String, onDismiss: () -> Unit) {
    val TAG = "FullScreenWebView"
    var thisWebView: WebView? = null
    var canGoBack by rememberSaveable { mutableStateOf(false) }
    var canGoForward by rememberSaveable { mutableStateOf(false) }
    val openBrowserSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { /* Handle the result if needed */ }
    val menuOptions = listOf(
        MenuOption("关于", Icons.Filled.Info, state = MenuOptionState.Disabled){ /* 点击事件 */ },
        MenuOption("设置", Icons.Filled.Settings, state = MenuOptionState.Disabled){ /* 点击事件 */ },
        MenuOption("设为默认", Icons.Filled.Webhook) {
            val intent = Intent(android.provider.Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
            openBrowserSettingsLauncher.launch(intent)
        },
        MenuOption("清除 Cookie", Icons.Filled.Cookie) {
            CookieManager.getInstance().apply {
                removeAllCookies { success ->
                    if (success) {
                        thisWebView?.clearCache(true)
                        PopTip.show("<(￣︶￣)↗[success]")
                        thisWebView?.reload()
                    } else {
                        PopTip.show(" ￣へ￣ [failed]")
                    }
                }
            }
        },
        MenuOption("桌面版网页", Icons.Filled.DesktopWindows, canToggle = true,
            iconInActive = Icons.Filled.PhoneAndroid,
            titleInActive = "移动端网页") { /* 点击事件 */ },
        MenuOption("前往", Icons.Filled.TravelExplore, state = MenuOptionState.Disabled) { /* 点击事件 */ },
        MenuOption("翻译", Icons.Filled.Translate, state = MenuOptionState.Disabled) { /* 点击事件 */ },
        MenuOption("分享", Icons.Filled.Share, state = MenuOptionState.Disabled) { /* 点击事件 */ },
        MenuOption("深色模式", Icons.Filled.DarkMode, state = MenuOptionState.Disabled) { /* 点击事件 */ },
        MenuOption("刷新", Icons.Filled.Refresh) { thisWebView?.reload() },
        MenuOption("退出", Icons.Filled.Close) { onDismiss.invoke() },
    )
    var expanded = rememberSaveable { mutableStateOf(false) }


    DisposableEffect(Unit) {
        // 在 Composition 销毁时
        onDispose { }
    }

    // 菜单面板
    MaterialBottomMenu(expanded) { Menu(menuOptions, 5, expanded) }

    Scaffold(
        modifier = Modifier.imePadding(), // 布局适配软键盘，一般来说不需要嵌套声明
        content = { padding ->
            Box(modifier = Modifier
                .padding(padding)
                .fillMaxSize()) {
                // 使用AndroidView嵌入WebView
                AndroidView(factory = {
                    WebView(it).apply {
                        this.webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean {
                                if (view == null || request == null) {
                                    return super.shouldOverrideUrlLoading(view, request)
                                }
                                return handleUrlLoading(activity, request)
                            }
                            override fun onPageFinished(view: WebView, url: String) {
                                Log.d(TAG, "onPageFinished -> $url")
                                super.onPageFinished(view, url)
                                canGoBack = view.canGoBack()
                                canGoForward = view.canGoForward()
                                applySystemThemeToWebView(activity, view)
                                injectLocalJS(view)
                            }

                            override fun onPageStarted(
                                view: WebView,
                                url: String?,
                                favicon: Bitmap?
                            ) {
                                Log.d(TAG, "onPageStarted -> $url")
                                super.onPageStarted(view, url, favicon)
                            }

                            override fun onLoadResource(view: WebView?, url: String?) {
                                Log.d(TAG, "onLoadResource -> $url")
                                super.onLoadResource(view, url)
                            }

                            override fun onScaleChanged(
                                view: WebView?,
                                oldScale: Float,
                                newScale: Float
                            ) {
                                Log.d(TAG, "onScaleChanged ->")
                                super.onScaleChanged(view, oldScale, newScale)
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: WebResourceError?
                            ) {
                                Log.d(TAG, "onReceivedError -> $error")
                                super.onReceivedError(view, request, error)
                            }
                        }
                        this.webChromeClient = object : WebChromeClient() {
                            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                                consoleMessage?.let { it1 ->
                                    Log.d(
                                        "$TAG [WebChromeClient] ", "onConsoleMessage -> " +
                                                U_DEBUG.prettyConsoleMessage(
                                                    it1
                                                )
                                    )
                                }
                                return true // 屏蔽默认日志输出避免刷屏
                            }

                            override fun onJsAlert(
                                view: WebView,
                                url: String?,
                                message: String?,
                                result: JsResult?
                            ): Boolean {
                                val date = Date()
                                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
                                val formattedDate = sdf.format(date)

                                AlertDialog.Builder(activity)
                                    .setTitle("[WebChromeClient] onJsAlert from WebView")
                                    .setMessage(
                                        """
        
        --------------------------------------------
        $message
        --------------------------------------------
        
        * ${view.title}
        * $formattedDate
        """.trimIndent()
                                    )
                                    .setPositiveButton("OK") { dialog: DialogInterface?, which: Int -> result!!.confirm() }
                                    .setCancelable(false)
                                    .show()
                                return true // 已处理
                            }

                            override fun onJsConfirm(
                                view: WebView?,
                                url: String?,
                                message: String?,
                                result: JsResult?
                            ): Boolean {
                                return super.onJsConfirm(view, url, message, result)
                            }

                            override fun onJsPrompt(
                                view: WebView?,
                                url: String?,
                                message: String?,
                                defaultValue: String?,
                                result: JsPromptResult?
                            ): Boolean {
                                return super.onJsPrompt(view, url, message, defaultValue, result)
                            }

                            override fun onPermissionRequest(request: PermissionRequest?) {
                                super.onPermissionRequest(request)
                            }

                            override fun onPermissionRequestCanceled(request: PermissionRequest?) {
                                super.onPermissionRequestCanceled(request)
                            }

                            override fun onShowFileChooser(
                                webView: WebView?,
                                filePathCallback: ValueCallback<Array<Uri>>?,
                                fileChooserParams: FileChooserParams?
                            ): Boolean {
                                return super.onShowFileChooser(
                                    webView,
                                    filePathCallback,
                                    fileChooserParams
                                )
                            }
                        }
                        thisWebView = this
                        CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                        val ws = this.settings
                        ws.javaScriptEnabled = true
                        ws.domStorageEnabled = true
                        ws.cacheMode = WebSettings.LOAD_NO_CACHE
                        ws.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        ws.textZoom = 100
                        ws.useWideViewPort = true
                        ws.loadWithOverviewMode = true
                        ws.userAgentString =
                            "Mozilla/5.0 (Linux; Android 10; K) " +
                                    "AppleWebKit/537.36 (KHTML, like Gecko) " +
                                    "Chrome/120.0.0.0 " +
                                    "Mobile Safari/537.36 " +
                                    "EdgA/120.0.0.0 " // edge 浏览器安卓UA
                    }
                }, update = {
                    Log.d(TAG, "update -> $originUrl")
                    when (originUrl) {
                        "action?=Logout" -> {
                            // 隐藏处理过程
                            CookieManager.getInstance().apply {
                                removeAllCookies { success ->
                                    if (success) {
                                        thisWebView?.clearCache(true)
                                        PopTip.show("<(￣︶￣)↗[success]")
                                    } else {
                                        PopTip.show(" ￣へ￣ [failed]")
                                    }
                                }
                            }
                            return@AndroidView
                        }

                        else -> {
                            it.loadUrl(originUrl)
                        }
                    }

                })
            }

        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 58.dp,
                contentPadding = PaddingValues(2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp)
                    .height(36.dp)
                    .zIndex(999f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { thisWebView?.goBack() }, enabled = canGoBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                    IconButton(onClick = { expanded.value = !expanded.value }) {
                        if(expanded.value) Icon(Icons.Filled.MoreVert, contentDescription = "More") else Icon(Icons.Filled.MoreHoriz, contentDescription = "More")
                    }
                    IconButton(onClick = { thisWebView?.goForward() }, enabled = canGoForward) {
                        Icon(Icons.Filled.ArrowForward, contentDescription = "Forward")
                    }
                }
            }
        }
    )
}

@OptIn(DelicateCoroutinesApi::class)
private fun handleUrlLoading(activity: Activity, request: WebResourceRequest): Boolean {
    val TAG = "handleUrlLoading"
    val url = request.url.toString()
    val _url = U.replaceScheme_deepDecode(url, "googlechrome://", "slld246://")
    val real_url = U.replaceEncodeScheme(url, "googlechrome://", "slld246://")
    Log.d(TAG, "[handleUrlLoading] isForMainFrame:${request.isForMainFrame} $_url -> $real_url")
    // 在IO线程尝试下载，不阻塞
    GlobalScope.launch(Dispatchers.IO) {
        // 发送HEAD请求以获取Content-Type
        var connection: HttpURLConnection? = null
        try {
            connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "HEAD"
            connection.connect()

            val contentType = connection.contentType
            if (contentType != null && contentType.startsWith("application/")) {
                // 如果Content-Type指示它是一个应用程序文件（例如zip, pdf等），则下载文件
                val downloadRequest = DownloadManager.Request(Uri.parse(url))
                downloadRequest.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
                downloadRequest.setTitle(URLUtil.guessFileName(url, null, null))
                downloadRequest.setDescription("Downloading file...")
                downloadRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                downloadRequest.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, null, null))

                val downloadManager = activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                downloadManager.enqueue(downloadRequest)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error while checking Content-Type: ", e)
        } finally {
            connection?.disconnect()
        }
    }
    return if (_url.startsWith("mqq://") || _url.startsWith("wtloginmqq://") || _url.startsWith(
            "sinaweibo://"
        )
    ) {
        Log.d(TAG, "try to startActivityForResult by $_url")
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(real_url))
            ActivityCompat.startActivityForResult(activity, intent, 1, null)
            true
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
            false
        }
    } else {
        false
    }
}

private fun injectLocalJS(view: WebView) {
    // 读取本地 JavaScript 文件并注入到当前加载的页面中
    val js = "var script = document.createElement('script'); " +
            "script.type = 'text/javascript'; " +
            "script.src = 'https://unpkg.com/vconsole@latest/dist/vconsole.min.js'; " +
            "document.head.appendChild(script); " +
            "script.onload = function() { " +
            "  var vConsole = new window.VConsole();vConsole.showSwitch(); " +
            "};"
    view.evaluateJavascript(js, null)
}