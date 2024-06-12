package sc.windom.sofill.Us

import android.app.Activity
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager

object U_Phone {
    fun Activity.toggleFullScreen(fullScreen: Boolean) {
        val windowInsetsController = window.insetsController
        if (fullScreen) {
            val params = window.attributes
            params.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            window.attributes = params
            windowInsetsController?.systemBarsBehavior =
                WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE // 当用户从上往下滑动时，系统将短暂出现一个半透明的
            windowInsetsController?.hide(WindowInsets.Type.statusBars())
            windowInsetsController?.hide(WindowInsets.Type.systemBars())
            windowInsetsController?.hide(WindowInsets.Type.navigationBars())
            windowInsetsController?.hide(WindowInsets.Type.captionBar())
            windowInsetsController?.hide(WindowInsets.Type.systemGestures())
        } else {
            val params = window.attributes
            params.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
            window.attributes = params
            windowInsetsController?.systemBarsBehavior =
                WindowInsetsController.BEHAVIOR_DEFAULT
            windowInsetsController?.show(WindowInsets.Type.statusBars())
            windowInsetsController?.show(WindowInsets.Type.systemBars())
            windowInsetsController?.show(WindowInsets.Type.navigationBars())
            windowInsetsController?.show(WindowInsets.Type.captionBar())
            windowInsetsController?.show(WindowInsets.Type.systemGestures())
        }
    }
}