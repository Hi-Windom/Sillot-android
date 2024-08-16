/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/8/13 01:45
 * updated: 2024/8/13 01:45
 */

package sc.windom.sofill.Us

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.util.DisplayMetrics
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.core.graphics.Insets
import androidx.core.view.WindowInsetsCompat

/**
 * 获取根视图的高度
 */
fun View.getRootViewHeight(): Int {
    return this.rootView.height
}

/**
 * 获取根视图的宽度
 */
fun View.getRootViewWidth(): Int {
    return this.rootView.width
}

/**
 * 计算可用高度
 */
fun View.computeUsableHeight(): Int {
    val rect = visibleRect
    return rect.height()
}

/**
 * 计算可用宽度
 */
fun View.computeUsableWidth(): Int {
    val rect = visibleRect
    return rect.width()
}

/**
 * 获取视图的可见区域
 */
val View.visibleRect: Rect
    get() {
        val rect = Rect()
        getWindowVisibleDisplayFrame(rect)
        return rect
    }

/**
 * 获取显示度量
 */
val Activity.displayMetrics: DisplayMetrics
    get() {
        val displayMetrics = DisplayMetrics()
        // 获取WindowManager服务
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
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
 * 获取导航栏高度，在竖屏时调用
 */
@JvmStatic
@Deprecated("使用 View.navigationBarHeight 替代")
val View.navigationBarHeightV: Int
    @SuppressLint("ObsoleteSdkInt") @RequiresApi(Build.VERSION_CODES.S)
    get() {
        val insets = WindowInsetsCompat.toWindowInsetsCompat(this.rootWindowInsets ?: return 0)
        val visibleInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        return visibleInsets.bottom
    }

/**
 * 获取导航栏高度，在横屏时调用
 */
@JvmStatic
@Deprecated("使用 View.navigationBarWidth 替代")
val View.navigationBarHeightH: Int
    @SuppressLint("ObsoleteSdkInt") @RequiresApi(Build.VERSION_CODES.S)
    get() {
        val insets = WindowInsetsCompat.toWindowInsetsCompat(this.rootWindowInsets ?: return 0)
        val visibleInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        return maxOf(visibleInsets.left, visibleInsets.right)
    }
/**
 * 获取导航栏高度，任意屏幕时调用
 */
@JvmStatic
fun View.navigationBarHeightOrWidth(rotation: Int?): Int {
    val visibleInsets = getSystemBarInsets()
    return when (rotation) {
        Surface.ROTATION_0 -> visibleInsets.bottom // 正常位置
        Surface.ROTATION_90 -> visibleInsets.right // 顺时针旋转90度
        Surface.ROTATION_180 -> visibleInsets.top // 旋转180度
        Surface.ROTATION_270 -> visibleInsets.left // 逆时针旋转90度
        else -> 0 // 未知旋转角度
    }
}

/**
 * 获取导航栏高度，在竖屏时调用
 */
@JvmStatic
fun View.navigationBarHeight(rotation: Int?): Int {
    val visibleInsets = getSystemBarInsets()
    return when (rotation) {
        Surface.ROTATION_0 -> visibleInsets.bottom // 正常位置
        Surface.ROTATION_90 -> 0 // 顺时针旋转90度
        Surface.ROTATION_180 -> visibleInsets.top // 旋转180度
        Surface.ROTATION_270 -> 0 // 逆时针旋转90度
        else -> 0 // 未知旋转角度
    }
}

/**
 * 获取导航栏高度，在横屏时调用
 */
@JvmStatic
fun View.navigationBarWidth(rotation: Int?): Int {
    val visibleInsets = getSystemBarInsets()
    return when (rotation) {
        Surface.ROTATION_0 -> 0 // 正常位置
        Surface.ROTATION_90 -> visibleInsets.right // 顺时针旋转90度
        Surface.ROTATION_180 -> 0 // 旋转180度
        Surface.ROTATION_270 -> visibleInsets.left // 逆时针旋转90度
        else -> 0 // 未知旋转角度
    }
}

/**
 * 获取系统栏（状态栏和导航栏）
 */
@JvmStatic
fun View.getSystemBarInsets(): Insets {
    val insets = rootWindowInsets?.let { WindowInsetsCompat.toWindowInsetsCompat(it) }
    return insets?.getInsets(WindowInsetsCompat.Type.systemBars()) ?: Insets.NONE
}

/**
 * 调整布局边距参数以避免被系统栏遮挡。此函数不会让布局立即改变，需要手动调用 [View.requestLayout]
 */
@JvmStatic
fun View.adjustLayoutMarginForSystemBars(fitTop: Boolean = false, fitBottom: Boolean = false) {
    val systemBarInsets = getSystemBarInsets()
    layoutParams?.let {
//        Log.d("adjustLayoutMarginForSystemBars", "fitTop: $fitTop , fitBottom: $fitBottom")
        if (it is ViewGroup.MarginLayoutParams) {
            it.topMargin = if (fitTop) systemBarInsets.top else 0
            it.bottomMargin = if (fitBottom) systemBarInsets.bottom else 0
            it.leftMargin = systemBarInsets.left
            it.rightMargin = systemBarInsets.right
        }
    }
}

/**
 * 获取状态栏高度, 兼容小窗模式
 */
@JvmStatic
val View.statusBarHeight: Int
    get() {
        return getSystemBarInsets().top
    }

/**
 * 通过 visibleRect 获取状态栏高度, 效果尚未测试, 应该比 [getSystemBarInsets] 更稳定
 */
@JvmStatic
val View.statusBarHeightFromRect: Int
    get() {
        return visibleRect.top
    }

/**
 * 获取状态栏高度
 */
@JvmStatic
@Deprecated("使用 View.statusBarHeight 替代")
val Activity.statusBarHeight: Int?
    get() {
        if (!hasWindowFocus()) return null
        // 获取当前视图的 WindowInsets
        val rootWindowInsets = window.decorView.rootWindowInsets ?: return null
        val insets = WindowInsetsCompat.toWindowInsetsCompat(rootWindowInsets)

        // 获取系统窗口的可见区域
        val visibleInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())

        // 返回顶部系统窗口的不可见区域高度，这通常是状态栏的高度
        return visibleInsets.top
    }

@Deprecated("安卓12+请使用applyStatusBarConfigurationV2")
@JvmStatic
fun Activity.applyStatusBarConfiguration(fitWindow: Boolean) {
    // 获取WindowInsetsController
    val insetsController = window.insetsController ?: return

    // 设置是否占用系统窗口空间
    val systemUiVisibility = if (fitWindow) {
        // 占用系统窗口空间
        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    } else {
        // 不占用系统窗口空间
        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    }
    window.decorView.systemUiVisibility = systemUiVisibility

    // 应用配置
    insetsController.systemBarsBehavior =
        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
}

@SuppressLint("ObsoleteSdkInt")
@JvmStatic
@RequiresApi(Build.VERSION_CODES.S)
fun Activity.applyStatusBarConfigurationV2(fitWindow: Boolean) {
    // 获取WindowInsetsController
    val insetsController = window.insetsController ?: return

    // 设置是否占用系统窗口空间
    window.setDecorFitsSystemWindows(fitWindow)

    // 控制系统栏的可见性
    insetsController.apply {
        // 设置系统栏的行为
        systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}

/**
 * 递归函数，用于设置所有子视图的宽高为MATCH_PARENT
 */
fun View.setLayoutParamsToMatchParent() {
    (this as? ViewGroup)?.let {
        for (i in 0 until it.childCount) {
            it.getChildAt(i).apply {
                layoutParams.height = -1
                layoutParams.width = -1
                setLayoutParamsToMatchParent() // 递归调用
            }
        }
    }
}