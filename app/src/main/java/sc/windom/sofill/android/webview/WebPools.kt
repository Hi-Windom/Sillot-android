package sc.windom.sofill.android.webview

import android.app.Activity
import android.content.MutableContextWrapper
import android.webkit.WebView
import java.util.Queue
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
    fun acquireWebView(activity: Activity): WebView {
        return acquireWebViewInternal(activity)
    }


    private fun acquireWebViewInternal(activity: Activity): WebView {
        val mWebView = mWebViews.poll()

        if (mWebView == null) {
            synchronized(lock) {
                return WebView(MutableContextWrapper(activity))
            }
        } else {
            val mMutableContextWrapper = mWebView.context as MutableContextWrapper
            mMutableContextWrapper.baseContext = activity
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