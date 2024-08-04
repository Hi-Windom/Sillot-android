/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/8/4 18:55
 * updated: 2024/8/4 18:55
 */

package sc.windom.sofill.Us

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.util.DisplayMetrics
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
val View.navigationBarHeightH: Int
    @SuppressLint("ObsoleteSdkInt") @RequiresApi(Build.VERSION_CODES.S)
    get() {
        val insets = WindowInsetsCompat.toWindowInsetsCompat(this.rootWindowInsets ?: return 0)
        val visibleInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        return maxOf(visibleInsets.left, visibleInsets.right)
    }

/**
 * 获取系统栏（状态栏和导航栏）
 */
@JvmStatic
fun View.getSystemBarInsets(): Insets {
    val insets = WindowInsetsCompat.toWindowInsetsCompat(rootWindowInsets)
    return insets.getInsets(WindowInsetsCompat.Type.systemBars())
}

/**
 * 调整布局边距参数以避免被系统栏遮挡。此函数不会让布局立即改变，需要手动调用 [View.requestLayout]
 */
@JvmStatic
fun View.adjustLayoutMarginForSystemBars() {
    val systemBarInsets = getSystemBarInsets()
    layoutParams?.let { layoutParams ->
        if (layoutParams is ViewGroup.MarginLayoutParams) {
            layoutParams.topMargin = 0 // 可能解决偶发从其他界面返回时顶部布局有问题，尚未得到验证
            layoutParams.bottomMargin = systemBarInsets.bottom
            layoutParams.leftMargin = systemBarInsets.left
            layoutParams.rightMargin = systemBarInsets.right
        }
    }
}

/**
 * 获取状态栏高度
 */
@JvmStatic
val View.statusBarHeight: Int
    get() {
        // 获取当前视图的 WindowInsets
        val insets = WindowInsetsCompat.toWindowInsetsCompat(this.rootWindowInsets ?: return 0)

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

