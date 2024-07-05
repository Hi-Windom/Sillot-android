package sc.windom.sofill.Us

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.util.Log
import android.webkit.ConsoleMessage
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
import sc.windom.sofill.Ss.S_Webview
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object U_Webview {
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
            Log.d(TAG, "onScaleChanged ->")
            super.onScaleChanged(view, oldScale, newScale)
        }

        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            Log.e(TAG, "onReceivedError -> $error")
            super.onReceivedError(view, request, error)
        }

        override fun onReceivedSslError(
            view: WebView?,
            handler: SslErrorHandler?,
            error: SslError?
        ) {
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
                Log.w(TAG, errorResponse.toString())
            }
            super.onReceivedHttpError(view, request, errorResponse)
        }
    }
}