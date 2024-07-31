/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/7/31 23:29
 * updated: 2024/7/31 23:29
 */

package sc.windom.sofill.Us

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Message
import android.util.Log
import android.view.View
import android.webkit.ConsoleMessage
import android.webkit.GeolocationPermissions
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.PermissionRequest
import android.webkit.SslErrorHandler
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebChromeClient.FileChooserParams
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.MutableState
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kongzue.dialogx.dialogs.PopNotification
import com.kongzue.dialogx.interfaces.OnDialogButtonClickListener
import sc.windom.sofill.Ss.S_Uri
import sc.windom.sofill.Ss.S_Webview
import sc.windom.sofill.Ss.S_packageName
import sc.windom.sofill.U.compareVersions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object U_Webview {
}

@JvmStatic
fun Context.getWebViewVer(): String {
    var webViewVer = ""
    val packageManager = this.packageManager
    val webViewPackageInfo = packageManager.getPackageInfo(S_packageName.GoogleWebview, 0)
    webViewVer = webViewPackageInfo.versionName
    Log.w("U_Webview", webViewVer)
    return webViewVer
}
@JvmStatic
fun Activity.checkWebViewVer(minVer: String) {
    val webViewVer = getWebViewVer()
    val result = compareVersions(webViewVer, minVer)
    Log.w("U_Webview", "checkWebViewVer result : $result")
    if (result < 0) {
        PopNotification.show("系统 WebView 版本 $webViewVer 太低, 请升级至 $minVer+").noAutoDismiss().onPopNotificationClickListener =
            object : OnDialogButtonClickListener<PopNotification?> {
                override fun onClick(
                    p0: PopNotification?,
                    p1: View?
                ): Boolean {
                    U_Uri.openURLUseDefaultApp(S_Uri.URL_Sillot_docs_uprade_webview)
                    return true
                }
            }
    }
}

fun WebView.injectVConsole(resultCallback: ValueCallback<String?>? = null) {
    val js = """
        let script = document.createElement('script');
        script.type = 'text/javascript';
        script.src = 'https://unpkg.com/vconsole@latest/dist/vconsole.min.js';
        document.head.appendChild(script);
        script.onload = function() {
            var vConsole = new window.VConsole();
            vConsole.showSwitch();
        };
"""
    this.evaluateJavascript(js, resultCallback)
}

fun WebView.injectEruda(resultCallback: ValueCallback<String?>? = null) {
    val js = """
                    (function () {
                        if (window.eruda) return;
                        var define;
                        if (window.define) {
                            define = window.define;
                            window.define = null;
                        }
                        var script = document.createElement('script'); 
                        script.src = '//cdn.jsdelivr.net/npm/eruda'; 
                        document.body.appendChild(script); 
                        script.onload = function () { 
                            eruda.init();
                            if (define) {
                                window.define = define;
                            }
                        }
                    })();
                """
    this.evaluateJavascript(js, resultCallback)
}

/**
 * 修复QQ授权登录界面“一键登录”按钮不显示的问题。**这是临时性修复，问题根源需要去解决，多半是 webview 布局出现了问题（肉眼观察不出来）**
 * @see [sc.windom.sofill.android.webview.WebViewLayoutManager]
 */
fun WebView.fixQQAppLaunchButton(resultCallback: ValueCallback<String?>? = null) {
    val js = """
        let e = document.querySelector("#onekey");
        if (e) { e.style.position = "relative"; }
"""
    this.evaluateJavascript(js, resultCallback)
}

@SuppressLint("SetJavaScriptEnabled")
fun WebSettings.applyDefault(webViewTextZoom: Int = 100, ua: String = S_Webview.UA_edge_android) {
    apply {
        javaScriptEnabled = true
        allowUniversalAccessFromFileURLs = true
        allowFileAccessFromFileURLs = true
        domStorageEnabled = true
        allowFileAccess = true
        allowContentAccess = true
        cacheMode = WebSettings.LOAD_NO_CACHE
        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW // 允许Http和Https混合
        textZoom = webViewTextZoom
        useWideViewPort = true
        loadWithOverviewMode =
            false // 设置 WebView 是否以概览模式加载页面，即按宽度缩小内容以适应屏幕。设为 true 实测发现 github 等页面会有个不美观的抽搐过程
        userAgentString = ua
    }
}

fun thisWebChromeClient(activity: Activity): WebChromeClient {
    val TAG = "thisWebChromeClient"
    return object : WebChromeClient() {
        override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
            consoleMessage?.let { it1 ->
                Log.d(
                    TAG, "onConsoleMessage -> " +
                            U_DEBUG.prettyConsoleMessage(
                                it1
                            )
                )
            }
            return true // 屏蔽默认日志输出避免刷屏
        }

        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            // TODO 进度条 https://github.com/Hi-Windom/Sillot-android/issues/148
            super.onProgressChanged(view, newProgress)
        }

        override fun onCreateWindow(
            view: WebView?,
            isDialog: Boolean,
            isUserGesture: Boolean,
            resultMsg: Message?
        ): Boolean {
            Log.d(TAG, "onCreateWindow -> $isDialog $isUserGesture $resultMsg")
            return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg)
        }

        override fun onCloseWindow(window: WebView?) {
            Log.d(TAG, "onCloseWindow -> $window")
            super.onCloseWindow(window)
        }

        /**
         * 此方法仅针对源自诸如 https 等安全来源的请求调用。在非安全来源上，地理定位请求将自动被拒绝。
         */
        override fun onGeolocationPermissionsShowPrompt(
            origin: String?,
            callback: GeolocationPermissions.Callback?
        ) {
            Log.d(TAG, "onGeolocationPermissionsShowPrompt -> $origin $callback")
            super.onGeolocationPermissionsShowPrompt(origin, callback)
        }

        override fun onRequestFocus(view: WebView?) {
            Log.d(TAG, "onRequestFocus -> $view")
            super.onRequestFocus(view)
        }

        override fun onJsAlert(
            view: WebView,
            url: String?,
            message: String?,
            result: JsResult?
        ): Boolean {
            activity.showJSAlert(view, url, message, result)
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
            Log.d(TAG, "onPermissionRequest -> ${request?.resources}")
            super.onPermissionRequest(request)
            request?.grant(request.resources)
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
}

fun thisWebViewClient(
    activity: Activity,
    currentUrl: MutableState<String>,
    canGoBack: MutableState<Boolean>,
    canGoForward: MutableState<Boolean>,
    handleUrlLoading: ((activity: Activity, request: WebResourceRequest) -> Boolean)? = null,
    handlePageFinished: ((activity: Activity, view: WebView, url: String) -> Unit)? = null
): WebViewClient {
    val TAG = "thisWebViewClient"
    return object : WebViewClient() {
        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            if (view == null || request == null || handleUrlLoading == null) {
                return super.shouldOverrideUrlLoading(view, request)
            }
            val headers = request.requestHeaders
            if (headers.isNullOrEmpty()) {
                Log.w("WebViewClient", "Request headers isNullOrEmpty")
            } else {
                Log.d("WebViewClient", "Request headers: $headers")
            }
            return handleUrlLoading.invoke(activity, request)
        }

        override fun onPageFinished(view: WebView, url: String) {
            Log.d(TAG, "onPageFinished -> $url")
            super.onPageFinished(view, url)
            canGoBack.value = view.canGoBack()
            canGoForward.value = view.canGoForward()
            handlePageFinished?.invoke(activity, view, url)
        }

        override fun onPageStarted(
            view: WebView,
            url: String,
            favicon: Bitmap?
        ) {
            Log.d(TAG, "onPageStarted -> $url")
            super.onPageStarted(view, url, favicon)
            currentUrl.value = url
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
            Log.d(TAG, "onScaleChanged invoked")
            super.onScaleChanged(view, oldScale, newScale)
        }

        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            Log.e(TAG, "onReceivedError -> code=${error?.errorCode} description=${error?.description}")
            super.onReceivedError(view, request, error)
        }

        @SuppressLint("WebViewClientOnReceivedSslError")
        override fun onReceivedSslError(
            view: WebView?,
            handler: SslErrorHandler?,
            error: SslError?
        ) {
            Log.w(TAG, "onReceivedSslError invoked")
            handler?.proceed() // 请谨慎使用，仅当您信任该网站时才这样做
            error?.let {
                val msg = when (error.getPrimaryError()) {
                    SslError.SSL_DATE_INVALID -> "证书日期无效"
                    SslError.SSL_EXPIRED -> "证书已过期。"
                    SslError.SSL_IDMISMATCH -> "主机名不匹配。"
                    SslError.SSL_INVALID -> "发生一般错误"
                    SslError.SSL_NOTYETVALID -> "证书尚未生效。"
                    SslError.SSL_UNTRUSTED -> "证书颁发机构不受信任。" // 可能是自定义证书
                    else -> "SSL证书错误,错误码：" + error.getPrimaryError()
                }
                Log.w(TAG, "onReceivedSslError -> $msg")

                if (error.getPrimaryError() != SslError.SSL_UNTRUSTED) {
                    super.onReceivedSslError(view, handler, error)
                }
            }
        }

        override fun onReceivedHttpError(
            view: WebView?,
            request: WebResourceRequest?,
            errorResponse: WebResourceResponse?
        ) {
            errorResponse?.let {
                Log.e(TAG, "onReceivedHttpError -> ${it.statusCode} ${it.mimeType}  ?by ${request?.url} ${request?.method} \n" +
                        " responseHeaders=${it.responseHeaders} \n requestHeaders=${request?.requestHeaders}")
            }
            super.onReceivedHttpError(view, request, errorResponse)
        }
    }
}

@JvmStatic
fun Activity.showJSAlert(
    view: WebView,
    url: String?,
    message: String?,
    result: JsResult?
) {
    val date = Date()
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
    val formattedDate = sdf.format(date)

    MaterialAlertDialogBuilder(this)
        .setTitle("[WebChromeClient] onJsAlert from WebView")
        .setMessage(
            """
        
        -------------------------------
        $message
        -------------------------------
        
        * ${view.title}
        * $formattedDate
        * $url
        """.trimIndent()
        )
        .setPositiveButton("OK") { dialog: DialogInterface?, which: Int -> result!!.confirm() }
        .setCancelable(false)
        .show()
}