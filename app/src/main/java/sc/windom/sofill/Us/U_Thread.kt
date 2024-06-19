package sc.windom.sofill.Us

import android.os.Handler
import android.os.Looper



object U_Thread {
    val HANDLER: Handler = Handler(Looper.getMainLooper())
    @JvmStatic
    fun runOnUiThread(runnable: Runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run()
        } else {
            HANDLER.post(runnable)
        }
    }
}