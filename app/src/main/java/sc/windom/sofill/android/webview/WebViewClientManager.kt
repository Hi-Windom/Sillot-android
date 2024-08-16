/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/8/11 02:26
 * updated: 2024/8/11 02:26
 */

package sc.windom.sofill.android.webview

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Message
import android.util.Log
import android.view.View
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.DownloadListener
import android.webkit.GeolocationPermissions
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.PermissionRequest
import android.webkit.SslErrorHandler
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.MutableState
import androidx.webkit.WebViewAssetLoader
import sc.windom.sofill.Us.U_DEBUG
import sc.windom.sofill.Us.applyDefault
import sc.windom.sofill.Us.showJSAlert
import java.io.File

class WebViewClientManager private constructor(
    private val activity: Activity,
    private val webView: WebView,
    private val inCompose: Boolean
) {
    private val TAG = "WebViewClientManager"

    /**
     * 避免作用域污染
     */
    private var thisIns: WebViewClientManager

    val cm = CookieManager.getInstance()

    var currentUrl: MutableState<String>? = null
    var canGoBack: MutableState<Boolean>? = null
    var canGoForward: MutableState<Boolean>? = null
    var handleUrlLoading: ((activity: Activity, request: WebResourceRequest) -> Boolean)? = null
    var handlePageFinished: ((activity: Activity, view: WebView, url: String) -> Unit)? = null

    var findListener: WebView.FindListener? = null
        set(value) {
            field = value
            webView.setFindListener(value)
        }

    var onLongClickListener: View.OnLongClickListener? = null
        set(value) {
            field = value
            webView.setOnLongClickListener(value)
        }

    var downloadListener: DownloadListener? = null
        set(value) {
            field = value
            webView.setDownloadListener(value)
        }

    init {
        cm.setAcceptCookie(true)
        cm.setAcceptThirdPartyCookies(webView, true)
        webView.settings.applyDefault()
        webView.webChromeClient = thisWebChromeClient()
        webView.webViewClient = thisWebViewClient()

        // 初始化 thisIns 在 init 块最后
        thisIns = this
    }

    fun thisWebChromeClient(): WebChromeClient {
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

    fun thisWebViewClient(): WebViewClient {
        return object : WebViewClient() {
            /**
             * 总的来说，拦截WebView的请求的原因无外乎两种，一是想篡改请求，二是想篡改响应。
             * 当webview页面有资源请求的时候通知宿主应用，允许应用自己返回数据给webview。如果返回值是null，就正常加载返回的数据，
             * 否则就加载应用自己return的response给webview。
             *
             * 参考资料：
             * - 如果遇到坑，可以看看 https://juejin.cn/post/6923415236049534989 有没有提及
             * - WebViewAssetLoader内置了三个对象 https://juejin.cn/post/7168382788905730062
             */
            override fun shouldInterceptRequest(
                view: WebView,
                request: WebResourceRequest
            ): WebResourceResponse? {
                Log.d(TAG, "shouldInterceptRequest -> ${request.url}")
                //离线资源路径
                val publicDir = File("${activity.filesDir.absolutePath}/app/app")
                Log.d(TAG, "filesDir:${publicDir}")
                val webViewAssetLoader = request.url.host?.let {
                    WebViewAssetLoader.Builder()
                        .setHttpAllowed(true)
//                    .setDomain(it) // 设置域名,在该域名优先适用离线资源,离线资源不可以用时再使用在线资源, 如果是 IP 则无效
                        .addPathHandler("/", WebViewAssetLoader.InternalStoragePathHandler(activity, publicDir))//对所有资源优先检查离线资源
                        .build()
                }
                return webViewAssetLoader?.shouldInterceptRequest(request.url) ?: super.shouldInterceptRequest(view, request)
            }
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
                return handleUrlLoading?.invoke(activity, request) ?: super.shouldOverrideUrlLoading(view, request)
            }

            /**
             * 如果网页存在重定向，onPageFinished 的时候 progress 不一定为 100。
             * [android.webkit.WebChromeClient.onProgressChanged]方法监听也是一样的
             */
            override fun onPageFinished(view: WebView, url: String) {
                view.progress.let {
                    Log.d(TAG, "onPageFinished -> $url Progress == $it")
                    if (it == 100) {
                        canGoBack?.let { it1 -> it1.value = view.canGoBack() }
                        canGoForward?.let { it1 -> it1.value = view.canGoForward() }
                        handlePageFinished?.invoke(activity, view, url)
                    }
                }
                super.onPageFinished(view, url)
            }

            override fun onPageStarted(
                view: WebView,
                url: String,
                favicon: Bitmap?
            ) {
                Log.d(TAG, "onPageStarted -> $url")
                super.onPageStarted(view, url, favicon)
                currentUrl?.let { it.value = url }
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
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                Log.e(TAG, "onReceivedError -> code=${error.errorCode} description=${error.description}")
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


    companion object {
        /**
         * 绑定使用
         * @param activity 活动
         * @param webView WebView
         * @param inCompose 是否 Compose 布局。`inCompose` 实际上没有发挥作用，保留字段后续可能有用
         */
        @JvmStatic
        @JvmOverloads // 自动生成带有默认参数的重载函数供 java 使用
        fun assistActivity(
            activity: Activity,
            webView: WebView,
            inCompose: Boolean = false
        ): WebViewClientManager {
            return WebViewClientManager(activity, webView, inCompose)
        }
    }
}