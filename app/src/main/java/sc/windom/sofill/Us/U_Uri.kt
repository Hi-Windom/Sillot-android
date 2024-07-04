package sc.windom.sofill.Us

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.app.ActivityCompat
import com.blankj.utilcode.util.ActivityUtils.startActivity
import com.kongzue.dialogx.dialogs.PopNotification

object U_Uri {
    /**
     * 请求意图，来自特殊URL协议
     * @param _url 原始URL
     * @param real_url 真实URL，为空等于 _url
     * @return 返回请求是否成功，不含结果。结果在 Activity 查收
     */
    fun Activity.askIntentForSUS(_url: String, real_url: String = _url): Boolean {
        val TAG = "askIntentForSUS"
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


    fun openUrl(url: String, noBrowser: Boolean = false) {
        val i = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        if (noBrowser) {
            i.addFlags(Intent.FLAG_ACTIVITY_REQUIRE_NON_BROWSER)
        }
        try {
            startActivity(i)
        } catch (e: Exception) {
            PopNotification.show(e.message, e.stackTrace.toString()).noAutoDismiss()
        }
    }
}