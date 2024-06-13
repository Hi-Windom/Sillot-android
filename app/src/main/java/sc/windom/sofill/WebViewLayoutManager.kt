package sc.windom.sofill

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentCallbacks
import android.content.Context
import android.content.res.Configuration
import android.graphics.Rect
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.webkit.WebView
import android.widget.FrameLayout
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.tencent.mmkv.MMKV
import sc.windom.sofill.U.applySystemThemeToWebView
import sc.windom.sofill.U.isInSpecialMode
import sc.windom.sofill.Us.U_Phone.isLandscape


/**
 * Android small window mode soft keyboard black occlusion [siyuan-note/siyuan-android#7](https://github.com/siyuan-note/siyuan-android/pull/7)
 *
 * 优化注册工具栏显示/隐藏跟随软键盘状态 [Hi-Windom/Sillot-android#84](https://github.com/Hi-Windom/Sillot-android/issues/84)
 *
 * 基于 AndroidBug5497Workaround 改进，将原软键盘监听集成，统一调整布局。MIUI小窗底部有小白条，已进行抬高处理
 * TODO: 平板端测试有一定延迟，后续优化
 * TODO: 测试发现保持键盘呼出态从竖屏切换为横屏再切换回来布局异常，需要解决
 * TODO: 屏幕方向变化时处理；包括使用悬浮键盘的情况
 * @author Yingyi, Soltus
 */
class WebViewLayoutManager private constructor(
    private val activity: Activity,
    private val webView: WebView
) {
    private val TAG = "AndroidBug5497Workaround"
    private var mmkv: MMKV = MMKV.defaultMMKV()
    var autoWebViewDarkMode: Boolean = false
    private var currentOrientation: Int = -1
    private var isKeyboardShow = false // 键盘是否显示
    private var HeightKeyboard = 0 // 键盘高度
    private val view: View // Activity的内容视图

    init {
        autoWebViewDarkMode = mmkv.getBoolean("autoWebViewDarkMode", false)
        applySystemThemeToWebView(activity, webView, autoWebViewDarkMode)
        val frameLayout = activity.findViewById<FrameLayout>(android.R.id.content)
        this.view = frameLayout.getChildAt(0)
//  不兼容小窗模式      KeyboardUtils.fixAndroidBug5497(activity)
        KeyboardUtils.fixSoftInputLeaks(activity)
        KeyboardUtils.registerSoftInputChangedListener(
            activity
        ) { height: Int ->
            this.isKeyboardShow = height > 0
            this.HeightKeyboard = height
            logInfo()
            restLayout("KeyboardUtils")
            // showKeyboardToolbar 不知道在哪已经实现了随键盘呼出（有延时，大概率是在前端），这里依旧调用是因为响应更快
            val javascriptCommand =
                if (height > 0) "showKeyboardToolbar()" else "hideKeyboardToolbar()"
            webView.evaluateJavascript("javascript:$javascriptCommand", null)
        }
        // 监听布局变化
        frameLayout.viewTreeObserver.addOnGlobalLayoutListener {
            restLayout("监听布局变化")
        }
        // 监听配置变化
        activity.registerComponentCallbacks(object : ComponentCallbacks {
            override fun onConfigurationChanged(newConfig: Configuration) {
                applySystemThemeToWebView(activity, webView, autoWebViewDarkMode)
                Log.w(
                    TAG,
                    "新配置是否横屏: ${newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE}, " +
                            "旧配置是否横屏: ${currentOrientation  == Configuration.ORIENTATION_LANDSCAPE}"
                )
                logInfo()
                if (newConfig.orientation != currentOrientation) {
                    currentOrientation = newConfig.orientation
                    restLayout("监听配置编号-屏幕方向已改变")
                }
                else {
                    restLayout("监听配置变化")
                }
            }

            override fun onLowMemory() {
                TODO("Not yet implemented")
            }
        })
    }

    /**
     * 重置布局
     * @param traker 跟踪调用者
     */
    private fun restLayout(traker: String) {
        // 添加判断避免重置布局后又触发布局变化监听里的调用
        // activity.isInSpecialMode() 特殊模式无法判断 isKeyboardShow
        // 悬浮键盘无法判断
        val newHight =
            this.getRootViewHeight() - BarUtils.getNavBarHeight() / 2 + this.navigationBarHeight / 2
        if (view.height != newHight && view.height + this.HeightKeyboard != newHight) {
            logInfo()
            Log.w(
                TAG,
                "restLayout@$traker, view.height: ${view.height}, newHight: $newHight, isKeyboardShow: ${this.isKeyboardShow}, HeightKeyboard: ${this.HeightKeyboard} , isInSpecialMode(): ${activity.isInSpecialMode()}"
            )
            if (this.isKeyboardShow) {
                view.layoutParams.height = newHight - if (activity.isInSpecialMode()) 0 else this.HeightKeyboard
                webView.layoutParams.height = -1 // 这里不能改，必须MATCH_PARENT
                view.requestLayout()
                webView.requestLayout()
            } else {
                view.layoutParams.height = newHight
                webView.layoutParams.height = -1 // 这里不能改，必须MATCH_PARENT
                view.requestLayout()
                webView.requestLayout()
            }
            if (this.HeightKeyboard == 0 && activity.isLandscape()) {
                // 横屏可能使用悬浮键盘
                webView.evaluateJavascript("javascript:hideKeyboardToolbar();showKeyboardToolbar();window.Sillot.android.LockKeyboardToolbar=true;", null)
            } else {
                webView.evaluateJavascript("javascript:window.Sillot.android.LockKeyboardToolbar=false;", null)
            }
        }
    }


    /**
     * 打印日志信息
     *
     * view.layoutParams.height：
     * 这个属性指的是视图的布局参数中的高度设置。
     * 它决定了视图在布局过程中应该占用多大的高度空间。
     * LayoutParams类是视图的一个属性，用于定义如何将视图放置在其父视图容器中。
     * height可以设置为具体的像素值（如LayoutParams.MATCH_PARENT，LayoutParams.WRAP_CONTENT或具体的数值），
     * 或者是一个基于父容器的比例（如LayoutParams.FILL_PARENT，这已在新版本中不推荐使用）。
     *
     * view.height：
     * 这个属性指的是视图在布局完成后实际的高度。
     * 它是在布局过程结束后，系统根据view.layoutParams.height和其他因素（如视图的内容、父容器的限制等）计算出的实际高度值。
     * 这个值是只读的，开发者在代码中不能直接设置view.height，它是由布局系统确定的。
     *
     * view.bottom：
     * view.bottom是视图底边的坐标，相对于其父视图的坐标空间。它是视图顶部坐标加上视图高度的结果，即view.bottom = view.top + view.height。
     * view.bottom通常用于确定视图在屏幕上的位置，特别是在计算视图之间的间隔或确定触摸事件的坐标时。
     *
     * height = bottom - top
     */
    private fun logInfo() {
        val rect = this.visibleRect
        val rootViewHeight = this.getRootViewHeight()
        val rootViewWidth = this.getRootViewWidth()
        val display = this.displayMetrics
        val navigationBarHeight = this.navigationBarHeight
        Log.d(
            TAG,
            "rect.top: ${rect.top} | view.top: ${view.top} | webView.top: ${webView.top}"
        )
        Log.d(
            TAG,
            "rect.height: ${rect.height()} | view.height: ${view.height} | webView.height: ${webView.height}"
        )
        Log.d(
            TAG,
            "rect.bottom: ${rect.bottom} | view.bottom: ${view.bottom} | webView.bottom: ${webView.bottom}"
        )
        Log.d(
            TAG,
            "view.layoutParams.height: ${view.layoutParams.height}, webView.layoutParams.height: ${webView.layoutParams.height}"
        )
        Log.d(
            TAG,
            "navigationBarHeight: $navigationBarHeight, NavBarHeight: ${BarUtils.getNavBarHeight()}, StatusBarHeight: ${BarUtils.getStatusBarHeight()}" +
                    ", rootViewHeight: $rootViewHeight, display.heightPixels: ${display.heightPixels}"
                    + "\n------------------------------------------------\n"
        )
    }

    /**
     * 计算可用高度
     */
    private fun computeUsableHeight(): Int {
        val rect = visibleRect
        return rect.height()
    }

    /**
     * 计算可用宽度
     */
    private fun computeUsableWidth(): Int {
        val rect = visibleRect
        return rect.width()
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
    private fun getRootViewWidth(): Int {
        return view.rootView.width
    }

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


    companion object {
        @JvmStatic
        fun assistActivity(activity: Activity, webView: WebView) {
            WebViewLayoutManager(activity, webView)
        }
    }
}
