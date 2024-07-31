/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/8/1 06:25
 * updated: 2024/8/1 06:25
 */

package sc.windom.sofill.android.webview

import android.app.Activity
import android.content.Context
import android.content.MutableContextWrapper
import android.webkit.WebView
import java.util.Queue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicReference


/**
 * @author zgx
 */
class WebPools private constructor() {
    private val mWebViews: Queue<WebView> = LinkedBlockingQueue()

    private val lock = Any()

    /**
     * 页面销毁
     * （1）去除WebView的上下文，避免内存泄漏
     * （2）加入缓存
     */
    fun recycle(webView: WebView) {
        recycleInternal(webView)
    }

    /**
     * 页面加入浏览器
     * （1）缓存没有，则新建webView
     */
    fun acquireWebView(context: Context): WebView {
        return acquireWebViewInternal(context)
    }


    private fun acquireWebViewInternal(context: Context): WebView {
        val mWebView = mWebViews.poll()

        if (mWebView == null) {
            synchronized(lock) {
                return WebView(MutableContextWrapper(context))
            }
        } else {
            val mMutableContextWrapper = mWebView.context as MutableContextWrapper
            mMutableContextWrapper.baseContext = context
            return mWebView
        }
    }


    private fun recycleInternal(webView: WebView) {
        try {
            if (webView.context is MutableContextWrapper) {
                val mContext = webView.context as MutableContextWrapper
                mContext.baseContext = mContext.applicationContext
                mWebViews.offer(webView)
            }
            if (webView.context is Activity) {
                throw RuntimeException("leaked")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private var mWebPools: WebPools? = null

        /**
         * 引用类型的原子类
         */
        private val mAtomicReference = AtomicReference<WebPools?>()

        val instance: WebPools?
            get() {
                while (true) {
                    if (mWebPools != null) return mWebPools
                    if (mAtomicReference.compareAndSet(
                            null,
                            WebPools()
                        )
                    ) return mAtomicReference.get().also {
                        mWebPools = it
                    }
                }
            }
    }
}

class WebPoolsPro private constructor() {
    private val mWebViews: ConcurrentHashMap<String, WebView> = ConcurrentHashMap()

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
     */
    fun createWebView(context: Context, key: String): WebView {
        return WebView(MutableContextWrapper(context)).apply {
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