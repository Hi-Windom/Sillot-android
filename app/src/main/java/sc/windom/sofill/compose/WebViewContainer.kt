package sc.windom.sofill.compose

import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.PermissionRequest
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cookie
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Webhook
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import com.kongzue.dialogx.dialogs.PopTip
import sc.windom.sofill.U
import sc.windom.sofill.Us.U_DEBUG
import sc.windom.sofill.android.webview.WebPoolsPro
import sc.windom.sofill.android.webview.applySystemThemeToWebView
import sc.windom.sofill.pioneer.getSavedValue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun FullScreenWebView(activity: Activity, originUrl: String, onDismiss: () -> Unit) {
    val TAG = "FullScreenWebView"
//    val Lcc = LocalContext.current
    var thisWebView: WebView? = WebPoolsPro.instance?.createWebView(activity, "FullScreenWebView")
    var canGoBack by rememberSaveable { mutableStateOf(false) }
    var canGoForward by rememberSaveable { mutableStateOf(false) }
    val openBrowserSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { /* Handle the result if needed */ }

    DisposableEffect(Unit) {
        // 在 Composition 销毁时始终回收 thisWebView
        thisWebView?.let { WebPoolsPro.instance?.recycle(it, "FullScreenWebView") }
        onDispose { }
    }

    Scaffold(
        content = { padding ->
            Box(modifier = Modifier.padding(padding)) {
                // 使用AndroidView嵌入WebView
                AndroidView(factory = {
                    val webViewToUse = thisWebView ?: WebView(activity)
                    webViewToUse.apply {
                        this.webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean {
                                if (view == null || request == null) {
                                    return super.shouldOverrideUrlLoading(view, request)
                                }
                                val url = request.url.toString()
                                Log.d(TAG, "shouldOverrideUrlLoading -> $url")
                                return handleUrlLoading(activity, url)
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
                                applySystemThemeToWebView(activity, view)
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
                                Log.d(TAG, "onReceivedError ->")
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp)
                    .height(35.dp)
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
                    IconButton(onClick = { thisWebView?.goForward() }, enabled = canGoForward) {
                        Icon(Icons.Filled.ArrowForward, contentDescription = "Forward")
                    }
                    IconButton(onClick = { thisWebView?.reload() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                    IconButton(
                        onClick = {
                            val intent = Intent(android.provider.Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
                            openBrowserSettingsLauncher.launch(intent)
                        }
                    ) {
                        Icon(Icons.Filled.Webhook, contentDescription = "跳转到默认浏览器设置界面")
                    }
                    IconButton(onClick = {
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
                    }) {
                        Icon(Icons.Filled.Cookie, contentDescription = "清除 Cookie")
                    }
                }
            }
        }
    )
}

private fun handleUrlLoading(activity: Activity, url: String): Boolean {
    val TAG = "handleUrlLoading"
    val _url = U.replaceScheme_deepDecode(url, "googlechrome://", "slld246://")
    val real_url = U.replaceEncodeScheme(url, "googlechrome://", "slld246://")
    Log.d(TAG, "$_url -> $real_url")

    return if (_url.startsWith("mqq://") || _url.startsWith("wtloginmqq://") || _url.startsWith(
            "sinaweibo://"
        )
    ) {
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