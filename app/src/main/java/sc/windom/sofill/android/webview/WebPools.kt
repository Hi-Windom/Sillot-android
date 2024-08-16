/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/8/6 14:23
 * updated: 2024/8/6 14:23
 */

package sc.windom.sofill.android.webview

import android.content.Context
import android.content.MutableContextWrapper
import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebView
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

class WebPoolsPro private constructor() {
    private val TAG = "WebPools.kt"
    private val mWebViews: ConcurrentHashMap<String, WebView> = ConcurrentHashMap()

    init {
        printWebViewPackageName()
    }

    private fun getWebViewPackageName(): String? {
        return try {
            WebView::class.java.getMethod("getWebViewPackageName").invoke(null) as String
        } catch (e: Exception) {
            // Method not found or other exceptions
            null
        }
    }

    private fun printWebViewPackageName() {
        val webViewPackageName = getWebViewPackageName()
        webViewPackageName?.let {
            Log.w(TAG, "WebView package name: $it")
        } ?: Log.w(TAG, "WebView package name could not be determined.")
    }


    /**
     * 页面销毁
     * （1）去除WebView的上下文，避免内存泄漏
     * （2）加入缓存
     */
    fun recycle(webView: WebView, key: String) {
        val context = webView.context
        if (context is MutableContextWrapper) {
            context.baseContext = context.applicationContext
            mWebViews[key] = webView
        } else {
            throw IllegalArgumentException("Context is not MutableContextWrapper")
        }
    }

    /**
     * 页面加入浏览器
     * （1）缓存没有，则新建webView
     */
    fun acquireWebView(key: String): WebView? {
        return mWebViews[key]
    }

    /**
     * 创建新的WebView并与其key关联
     * @param context WebView 始终应使用活动上下文（Activity Context）进行实例化。
     * 如果使用应用上下文（Application Context）或者其他非目标活动上下文进行实例化，WebView 将无法提供一些功能，例如 JavaScript 对话框和自动填充。
     */
    fun createWebView(context: Context, key: String): WebView {
        return WebView(MutableContextWrapper(context)).apply {
            /**
             * 告知 WebView 网络状态。这用于设置 JavaScript 属性 window.navigator.isOnline ，**应当监听网络变化动态设置**
             * 通过这个方法可以设置当前网络是否可用，借此触发HTML5和JS的online/offline模式，但是并不会对WebView的网络访问产生实质性影响。
             * 这意味着通过某种方式将网络的连接状态传递给 WebView ，以便在 JavaScript 环境中能够获取到当前的网络状态。
             * 例如，当网络从连接状态变为断开时，WebView 能够根据这个通知相应地设置 window.navigator.isOnline 的值为 false ，并触发离线事件。
             * 反之，当网络重新连接，也能进行相应的设置和触发在线事件。这对于依赖网络状态进行功能调整的网页应用来说非常重要，
             * 可以让网页能够根据实时的网络情况做出合适的响应，比如暂停加载大文件、显示离线提示等。
             */
            this.setNetworkAvailable(true)
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
            mWebViews[key] = this
        }
    }

    companion object {
        private var mWebPools: WebPoolsPro? = null

        /**
         * 引用类型的原子类
         */
        private val mAtomicReference = AtomicReference<WebPoolsPro?>()

        @JvmStatic
        val instance: WebPoolsPro?
            get() {
                while (true) {
                    mWebPools?.let { return it }
                    if (mAtomicReference.compareAndSet(null, WebPoolsPro())) {
                        return mAtomicReference.get().apply { mWebPools = this }
                    }
                }
            }

        const val key_SG = "Sillot-Gibbet"
        const val key_SB = "Sillot-Browser"

        /**
         * 共享 webview 节省内存，使用时注意串扰
         */
        const val key_NoActivity_Shared = "NoActivity ld246"
    }
}