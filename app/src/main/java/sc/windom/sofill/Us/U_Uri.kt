package sc.windom.sofill.Us

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.app.ActivityCompat
import com.kongzue.dialogx.dialogs.PopNotification

object U_Uri {
    /**
     * 请求意图，来自特殊URL协议
     * @param _url 原始URL
     * @param real_url 真实URL，为空等于 _url
     * @return 返回请求是否成功，不含结果。结果在 Activity 查收
     */
    fun Activity.askIntentForSUS(_url: String, real_url: String = _url): Boolean {
        val TAG = "handleUrlLoading"
        Log.d(TAG, "try to startActivityForResult by $_url")
        PopNotification.show("处理授权回调注意事项", "必须先将汐洛设置为默认浏览器（获得授权后即可恢复默认浏览器）。").noAutoDismiss()
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(real_url))
            ActivityCompat.startActivityForResult(this, intent, 1, null)
            true
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
            false
        }
    }
}