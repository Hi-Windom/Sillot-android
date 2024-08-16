/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/8/15 00:33
 * updated: 2024/8/15 00:33
 */

package sc.windom.sofill.android.webview

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.PixelCopy
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sc.windom.sofill.U.isLightColor
import sc.windom.sofill.Us.getSystemBarInsets
import sc.windom.sofill.Us.visibleRect
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private val TAG = "WebViewTheme"

/**
 * # 与 [WebViewLayoutManager] 配合良好的沉浸式状态栏与导航栏体验
 *
 * ### 1. 与 [androidx.activity.enableEdgeToEdge] 不兼容
 *
 * ### 2. 在别的地方调用会与与下面的代码不兼容
 *
 * ```kotlin
 *  window.decorView.setOnApplyWindowInsetsListener { _, insets ->
 *     insets
 *  }
 * ```
 *
 * ### 3. 不要在 `onDraw` 中调用, 会造成卡顿
 *
 * ```kotlin
 *         viewTreeObserver.addOnDrawListener(object : ViewTreeObserver.OnDrawListener {
 *             override fun onDraw() {
 *                 webView.post {
 *                     applyViewColorToSystemBar(activity, webView, tracker = "onDraw")
 *                 }
 *             }
 *         })
 * ```
 *
 * @param tracker 调用者标识，用于调试
 * @see [WebViewLayoutManager.edgeToEdge]
 */
fun applyViewColorToSystemBar(
    activity: Activity,
    view: View,
    tracker: String? = null,
) {
//    Log.d(TAG, "$tracker applyViewColorToSystemBar invoked -> ${activity.hasWindowFocus()} ${view.hasFocus()} ${view.hasWindowFocus()} ${view.isShown}")
    view.requestFocus()
    if (!view.isShown) {
        tracker?.let {
            Log.w(
                TAG,
                "[${it}] view.isShown == false, cancel"
            )
        }
        return // 不显示无法获取, 避免锁屏等场景下视图未渲染获取到错误的颜色,
    }
    val y = view.getSystemBarInsets().top
    val x = view.visibleRect.width().div(2) // 取中间的点, 避免边缘干扰
    activity.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    activity.window.addFlags(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON)
    /**
     * 通常，如果在锁屏出现时启动一个活动，该活动会转换到停止状态，但设置了此标志后，活动将保持恢复状态并显示在锁屏之上。
     * 这个值可以使用 android.R.attr.showWhenLocked 作为清单属性进行设置。
     * 比如说，在一个即时通讯应用中，可以设置此标志，以便在锁屏状态下有新消息时直接在锁屏上显示相关的通知活动，方便用户快速获取重要信息。
     * 又比如，对于一个紧急警报应用，通过这样的设置确保在紧急情况下即使锁屏也能及时展示警报活动。
     */
    activity.setShowWhenLocked(true)
    activity.setTurnScreenOn(true)
    view.post {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val bitmap = requestCopy(activity.window, Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888))
                tracker?.let {
                    Log.d(
                        TAG,
                        "[${it}] x: $x , y: $y , bitmap: (${bitmap?.width}, ${bitmap?.height})"
                    )
                }
                if (bitmap == null) return@launch
                activity.getColorFromBelowStatusBar(bitmap, x, y) { color ->
                    color?.let {
                        tracker?.let { it1 ->
                            Log.d(
                                TAG,
                                "[${it1}] get color -> $it (isLightColor: ${it.isLightColor()}) " +
                                        "origin statusBarColor: ${activity.window.statusBarColor} " +
                                        "origin navigationBarColor: ${activity.window.navigationBarColor} "
                            )
                        }
                        view.setBackgroundColor(it)
                        (view.parent as? ViewGroup)?.setBackgroundColor(it)
                        activity.window.statusBarColor = color
                        activity.window.navigationBarColor = color
                        WindowInsetsControllerCompat(activity.window, view).run {
                            isAppearanceLightStatusBars = it.isLightColor()
                            isAppearanceLightNavigationBars = it.isLightColor()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(
                    TAG,
                    "$tracker applyViewColorToSystemBar failed: \n ${e.stackTraceToString()}"
                )
            }
        }
    }
}

fun Activity.getColorFromBelowStatusBar(
    bitmap: Bitmap,
    x: Int = 0,
    y: Int,
    callback: (Int?) -> Unit
) {
    val firstColor = bitmap.getPixel(x, y)
    callback(firstColor)
//    saveBitmapGallery(bitmap) // debug
    bitmap.recycle() // 回收位图
}

suspend fun requestCopy(window: Window, bitmap: Bitmap): Bitmap? = withContext(Dispatchers.Main) {
    if (!suspendCoroutine<Boolean> { continuation ->
            // 省略了要从源复制的区域的参数，将复制整个表面。
            PixelCopy.request(
                // 表示要复制的源。
                window,
                // 复制的目的地。源将被缩放以匹配此位图的宽度、高度和格式。
                bitmap,
                // 像素复制请求完成时的回调。
                { copyResult ->
                    continuation.resume(copyResult == PixelCopy.SUCCESS)
                },
                // 复制完成时，回调将在这个处理程序上被调用。
                Handler(Looper.getMainLooper())
            )
        }) {
        return@withContext null
    }
    bitmap
}


/**
 * Debug 用, 有一定性能开销
 */
fun Context.saveBitmapGallery(bitmap: Bitmap): Boolean {
    //返回出一个URI
    val insert = contentResolver.insert(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        /*
        这里如果不写的话 默认是保存在 /sdCard/DCIM/Pictures
         */
        ContentValues()//这里可以啥也不设置 保存图片默认就好了
    ) ?: return false //为空的话 直接失败返回了

    //这个打开了输出流  直接保存图片就好了
    contentResolver.openOutputStream(insert).use {
        it ?: return false
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
    }
    return true
}
