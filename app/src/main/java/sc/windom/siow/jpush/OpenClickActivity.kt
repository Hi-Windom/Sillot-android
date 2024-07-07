/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/7/8 上午5:49
 * updated: 2024/7/8 上午5:49
 */

package sc.windom.siow.jpush

import android.app.Activity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.TextView
import cn.jpush.android.api.JPushInterface
import org.json.JSONException
import org.json.JSONObject

/**
 * Created by jiguang on 17/7/5.
 */
class OpenClickActivity : Activity() {
    private var mTextView: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mTextView = TextView(this)
        setContentView(mTextView)
        handleOpenClick()
    }

    /**
     * 处理点击事件，当前启动配置的Activity都是使用
     * Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK
     * 方式启动，只需要在onCreat中调用此方法进行处理
     */
    private fun handleOpenClick() {
        Log.d(TAG, "用户点击打开了通知")
        var data: String? = null
        //获取华为平台附带的jpush信息
        if (intent.data != null) {
            data = intent.data.toString()
        }

        //获取fcm、oppo、vivo、华硕、小米平台附带的jpush信息
        if (TextUtils.isEmpty(data) && intent.extras != null) {
            data = intent.extras!!.getString("JMessageExtra")
        }
        Log.w(TAG, "msg content is " + data.toString())
        if (TextUtils.isEmpty(data)) return
        try {
            val jsonObject = JSONObject(data)
            val msgId = jsonObject.optString(KEY_MSGID)
            val whichPushSDK = jsonObject.optInt(KEY_WHICH_PUSH_SDK).toByte()
            val title = jsonObject.optString(KEY_TITLE)
            val content = jsonObject.optString(KEY_CONTENT)
            val extras = jsonObject.optString(KEY_EXTRAS)
            val sb = StringBuilder()
            sb.append("msgId:")
            sb.append(msgId.toString())
            sb.append("\n")
            sb.append("title:")
            sb.append(title.toString())
            sb.append("\n")
            sb.append("content:")
            sb.append(content.toString())
            sb.append("\n")
            sb.append("extras:")
            sb.append(extras.toString())
            sb.append("\n")
            sb.append("platform:")
            sb.append(getPushSDKName(whichPushSDK))
            mTextView!!.text = sb.toString()

            //上报点击事件
            JPushInterface.reportNotificationOpened(this, msgId, whichPushSDK)
        } catch (e: JSONException) {
            Log.w(TAG, "parse notification error")
        }
    }

    private fun getPushSDKName(whichPushSDK: Byte): String {
        val name: String
        name = when (whichPushSDK.toInt()) {
            0 -> "jpush"
            1 -> "xiaomi"
            2 -> "huawei"
            3 -> "meizu"
            4 -> "oppo"
            5 -> "vivo"
            6 -> "asus"
            8 -> "fcm"
            else -> "jpush"
        }
        return name
    }

    companion object {
        private const val TAG = "OpenClickActivity"

        /**消息Id */
        private const val KEY_MSGID = "msg_id"

        /**该通知的下发通道 */
        private const val KEY_WHICH_PUSH_SDK = "rom_type"

        /**通知标题 */
        private const val KEY_TITLE = "n_title"

        /**通知内容 */
        private const val KEY_CONTENT = "n_content"

        /**通知附加字段 */
        private const val KEY_EXTRAS = "n_extras"
    }
}