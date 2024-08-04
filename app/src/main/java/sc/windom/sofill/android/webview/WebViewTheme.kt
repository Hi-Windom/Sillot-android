/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/8/4 23:20
 * updated: 2024/8/4 23:20
 */

package sc.windom.sofill.android.webview

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.PixelCopy
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsetsController
import android.webkit.WebView
import androidx.core.view.isVisible
import sc.windom.sofill.U.isLightColor
import sc.windom.sofill.Us.statusBarHeight


/**
 * 推荐在四处调用，将良好的沉浸式状态栏与导航栏体验带到 webView：
 * 在 [WebViewLayoutManager] 的 onConfigurationChangedCallback、 onLayoutChangedCallback，
 * 在 [android.webkit.WebChromeClient] 的 onPageFinished 中调用.
 * 在 [android.app.Activity] 的 onForeground 中调用
 *
 * `webView.rootView.setBackgroundColor(statusBarBelowColor)` 与 [WebViewLayoutManager] 进行联动（键盘弹出时延时重置布局并不总是理想，有时候还是会漏出布局底色露馅，
 * 一般来说 statusBarBelowColor 与网页背景色是一致的，因此不容易被发现）。
 * @suppress 不可能一直监听状态栏下方区域颜色是否变化，这样的开销没必要。因此如果切换主题不触发页面重载，即不经过onPageFinished，可以通过其他方式手动触发一下，问题不大。
 * 或者可以通过 JSAndroid 调用触发
 * @param isRoot webview 是否是根布局。无论是否是根布局我们都假设 webview 顶部与状态栏无缝衔接。当沉浸式异常时可以尝试设置为 true
 * @see [WebViewLayoutManager]
 */
@JvmOverloads // 自动生成带有默认参数的重载函数供 java 使用
fun applySystemThemeToWebView(
    activity: Activity,
    webView: WebView,
    isRoot: Boolean = false,
) {
    Handler(Looper.getMainLooper()).postDelayed({
        if (webView.isVisible) {
            activity.getDominantColorFromBelowStatusBar(webView, isRoot) { color ->
                color?.let {
                    if (isRoot) webView.setBackgroundColor(it) else webView.rootView.setBackgroundColor(it)
                    activity.setNavigationBarIconColorAccordingToColor(it)
                    activity.setStatusBarIconColorAccordingToColor(it)
                }
            }
        }
    }, 0) // 延时只有调试的时候需要设置延时
}

@SuppressLint("BlockedPrivateApi")
private fun Activity.setStatusBarIconColorAccordingToColor(color: Int) {
    val lightIcons = color.isLightColor()
    window.statusBarColor = color
    window.insetsController?.setSystemBarsAppearance(
        if (lightIcons) WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS else 0,
        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
    )
}

@SuppressLint("BlockedPrivateApi")
private fun Activity.setNavigationBarIconColorAccordingToColor(color: Int) {
    val lightIcons = color.isLightColor()
    window.navigationBarColor = color
    window.insetsController?.setSystemBarsAppearance(
        if (lightIcons) WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS else 0,
        WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
    )
}

private fun Activity.getDominantColorFromBelowStatusBar(view: View, isRoot: Boolean, callback: (Int?) -> Unit) {
    val _view = if (isRoot) view else view.rootView
    val statusBarHeight = _view?.statusBarHeight ?: return

    val decorView = _view as? ViewGroup ?: return
    val statusBarBelowView = decorView.findViewById<View>(android.R.id.content) ?: return
    // 创建位图以存储状态栏下方的视图内容
    val bitmap = Bitmap.createBitmap(statusBarBelowView.width, statusBarBelowView.height - statusBarHeight, Bitmap.Config.ARGB_8888)

    // 设置源矩形区域为状态栏下方的区域
    val sourceRect = Rect(0, statusBarHeight, statusBarBelowView.width, statusBarBelowView.height)

    try {
        PixelCopy.request(
            window,
            sourceRect,
            bitmap,
            { copyResult ->
                if (copyResult == PixelCopy.SUCCESS) {
                    val dominantColor = bitmap.getPixel(0, statusBarHeight) // 获取状态栏下方的颜色
                    bitmap.recycle() // 回收位图
                    return@request callback(dominantColor)
                } else {
                    // PixelCopy失败
                    return@request callback(null)
                }
            },
            Handler(Looper.getMainLooper())
        )
    } catch (e: Exception) {
        Log.e("applySystemThemeToWebView", "getDominantColorFromBelowStatusBar failed: ${e.message}")
    }
}

@Deprecated("该方法已过时，并且特殊情况下有问题")
private fun getDominantColorFromBelowStatusBar(view: View, isRoot: Boolean): Int? {
    val _view = if (isRoot) view else view.rootView
    // 获取状态栏高度，如果view或其rootView为null，则返回null
    val statusBarHeight = _view?.statusBarHeight ?: return null

    // 获取状态栏下方的视图，如果获取失败，则返回null
    val decorView = _view as? ViewGroup ?: return null
    val statusBarBelowView = decorView.findViewById<View>(android.R.id.content) ?: return null

    // 获取状态栏下方视图的位图
    statusBarBelowView.isDrawingCacheEnabled = true
    statusBarBelowView.buildDrawingCache(true)
    val bitmap = statusBarBelowView.drawingCache ?: return null

    // 获取状态栏下方的颜色
    val dominantColor = bitmap.getPixel(0, statusBarHeight)

    // 清理缓存
    statusBarBelowView.isDrawingCacheEnabled = false
    statusBarBelowView.destroyDrawingCache()

    return dominantColor
}
