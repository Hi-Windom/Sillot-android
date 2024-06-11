package sc.windom.sofill.android.webview

import android.content.Context
import android.webkit.WebView
import android.webkit.WebViewClient

@Deprecated("请使用 WebPools 替代")
class WebViewPool private constructor() {
    companion object {
        private var instance: WebViewPool? = null

        @JvmStatic
        fun getInstance(): WebViewPool {
            if (instance == null) {
                instance = WebViewPool()
            }
            return instance!!
        }
    }

    private val webViews = mutableListOf<WebViewWrapper>()

    fun getWebView(context: Context): WebView {
        // 从池中获取WebView，如果池中没有，则创建一个新的
        return if (webViews.isEmpty() || webViews[0].isDestroyed) {
            WebViewWrapper(context).apply { webViews.add(this) }
        } else {
            webViews.removeAt(0)
        }.webView
    }

    fun releaseWebView(webView: WebView) {
        // 查找对应的WebViewWrapper
        val wrapper = webViews.find { it.webView === webView }
        // 只有当WebView没有被销毁时，才将其放回池中
        if (wrapper != null && !wrapper.isDestroyed) {
            webViews.add(wrapper)
        }
    }

    // 自定义WebViewWrapper类，用于跟踪WebView的状态
    class WebViewWrapper(context: Context) {
        val webView: WebView = WebView(context)
        var isDestroyed: Boolean = false
            private set // 将setter设为private，因为只应在内部设置

        fun destroy() {
            webView.destroy()
            isDestroyed = true
        }

//        fun getWebView(): WebView {
//            return webView
//        }

        fun isWebViewDestroyed(): Boolean {
            return isDestroyed
        }
    }
}
