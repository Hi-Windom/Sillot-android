package sc.windom.sofill.Us

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.os.Build
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.annotation.VisibleForTesting
import androidx.core.view.WindowInsetsCompat

object U_Layout {
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
     * 获取导航栏高度
     */
    val View.navigationBarHeight: Int
        @SuppressLint("ObsoleteSdkInt") @RequiresApi(Build.VERSION_CODES.S)
        get() {
            // 获取当前视图的 WindowInsets
            val insets = WindowInsetsCompat.toWindowInsetsCompat(this.rootWindowInsets ?: return 0)

            // 获取系统窗口的可见区域
            val visibleInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            // 返回底部系统窗口的不可见区域高度，这通常是导航栏的高度
            return visibleInsets.bottom
        }

}