package org.b3log.siyuan

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.widget.FrameLayout
import com.blankj.utilcode.util.BarUtils

/**
 * Android small window mode soft keyboard black occlusion [siyuan-note/siyuan-android#7](https://github.com/siyuan-note/siyuan-android/pull/7)
 *
 * @author [al...@tutanota.com](https://issuetracker.google.com/issues/36911528#comment100)
 * @author [Yingyi](https://github.com/Zuoqiu-Yingyi)
 * @version 1.0.0.0, Nov 24, 2023
 * @since 2.11.0
 */
class AndroidBug5497Workaround private constructor(private val activity: Activity) {
    private val TAG = "AndroidBug5497Workaround"
    private var windowMode = 0 // 当前窗口模式，0表示全屏，100表示多窗口
    private var resize = false // 标记是否需要调整大小
    private var usableHeight = 0 // 可用高度
    private var rootViewHeight = 0 // 根视图的高度
    private val view: View // Activity的内容视图
    private val frameLayoutParams: FrameLayout.LayoutParams // 视图的布局参数

    init {
        val frameLayout = activity.findViewById<FrameLayout>(android.R.id.content)
        this.view = frameLayout.getChildAt(0)
        // 添加全局布局监听器，当布局发生变化时调用possiblyResizeChildOfContent方法
        frameLayout.viewTreeObserver.addOnGlobalLayoutListener { this.possiblyResizeChildOfContent() }
        this.frameLayoutParams = view.layoutParams as FrameLayout.LayoutParams
    }

    /**
     * 检查并调整Activity内容视图的大小
     */
    private fun possiblyResizeChildOfContent() {
        val usableHeight = this.computeUsableHeight() // 计算可用高度
        val rootViewHeight = this.getRootViewHeight() // 获取根视图高度
//        logInfo() // 打印日志信息

        // 如果可用高度或根视图高度发生变化，则需要调整大小
        if (usableHeight != this.usableHeight || rootViewHeight != this.rootViewHeight) {
            this.resize = false // 重置调整大小的标记

            // 如果处于多窗口模式
            if (activity.isInMultiWindowMode) {
                this.resize = true // 设置需要调整大小
                this.windowMode = 100 // 设置窗口模式为多窗口
                frameLayoutParams.height = -1 // 设置视图高度为MATCH_PARENT
            } else {
                // 如果处于全屏模式
                this.windowMode = 0 // 设置窗口模式为全屏
                frameLayoutParams.height = -1 // 设置视图高度为MATCH_PARENT
            }

            view.requestLayout() // 请求重新布局
            this.usableHeight = usableHeight // 更新可用高度
            this.rootViewHeight = rootViewHeight // 更新根视图高度
        } else if (this.resize) {
            // 如果已经标记为需要调整大小
            if (this.windowMode == 100) {
                // 如果当前不是MATCH_PARENT，则设置为MATCH_PARENT并请求重新布局
                if (frameLayoutParams.height != -1) {
                    frameLayoutParams.height = -1
                    view.requestLayout()
                } else {
                    this.resize = false // 如果已经是MATCH_PARENT，则重置调整大小的标记
                }
            }
        }
    }

    /**
     * 计算可用高度
     */
    private fun computeUsableHeight(): Int {
        val rect = visibleRect
        return rect.height()
    }

    /**
     * 获取视图的可见区域
     */
    private val visibleRect: Rect
        get() {
            val rect = Rect()
            view.getWindowVisibleDisplayFrame(rect)
            return rect
        }

    /**
     * 获取根视图的高度
     */
    private fun getRootViewHeight(): Int {
        return view.rootView.height
    }

    /**
     * 获取根视图的宽度
     */
    private val rootViewWidth: Int
        get() = view.rootView.width

    /**
     * 获取显示度量
     */
    private val displayMetrics: DisplayMetrics
        get() {
            val displayMetrics = DisplayMetrics()
            // 获取WindowManager服务
            val windowManager = activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            // 获取当前窗口度量
            val windowMetrics = windowManager.currentWindowMetrics
            // 获取窗口的边界
            val bounds = windowMetrics.bounds
            // 设置显示度量到窗口的大小
            displayMetrics.widthPixels = bounds.width()
            displayMetrics.heightPixels = bounds.height()
            return displayMetrics
        }

    /**
     * 获取导航栏高度
     */
    @get:SuppressLint("DiscouragedApi", "InternalInsetResource")
    private val navigationBarHeight: Int
        get() {
            val context = view.context
            val hasMenuKey = ViewConfiguration.get(context).hasPermanentMenuKey()
            if (!hasMenuKey) {
                val resourceId =
                    context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
                return if (resourceId > 0) context.resources.getDimensionPixelSize(resourceId) else 0
            } else {
                return 0
            }
        }

    /**
     * 打印日志信息
     */
    private fun logInfo() {
        val rect = this.visibleRect
        Log.d(
            TAG,
            "rect.top: " + rect.top + ", rect.bottom: " + rect.bottom + ", rect.height(): " + rect.height() + ", rect.width(): " + rect.width()
        )

        Log.d(
            TAG,
            "view.top: " + view.top + ", view.bottom: " + view.bottom + ", view.height(): " + view.height + ", view.width(): " + view.width
        )

        val rootViewHeight = this.getRootViewHeight()
        val rootViewWidth = this.rootViewWidth
        Log.d(TAG, "rootViewHeight: $rootViewHeight, rootViewWidth:$rootViewWidth")

        val display = this.displayMetrics
        Log.d(
            TAG,
            "display.heightPixels: " + display.heightPixels + ", display.widthPixels: " + display.widthPixels
        )

        Log.d(TAG, "frameLayoutParams.height: " + frameLayoutParams.height)

        val navigationBarHeight = this.navigationBarHeight
        Log.d(TAG, "navigationBarHeight: $navigationBarHeight")

        Log.d(TAG, "StatusBarHeight: " + BarUtils.getStatusBarHeight())
        Log.d(TAG, "NavBarHeight: " + BarUtils.getNavBarHeight())
    }

    companion object {
        @JvmStatic
        fun assistActivity(activity: Activity) {
            AndroidBug5497Workaround(activity)
        }
    }
}
