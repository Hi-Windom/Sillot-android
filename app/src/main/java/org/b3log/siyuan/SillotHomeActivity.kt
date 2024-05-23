package org.b3log.siyuan

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import coil.load
import coil.size.Scale
import com.kongzue.dialogx.dialogs.PopTip
import org.b3log.siyuan.appUtils.HWs
import org.b3log.siyuan.json.testmoshi
import org.b3log.siyuan.realm.TestRealm


class SillotHomeActivity : AppCompatActivity() {
    private val TAG = "SillotHomeActivity"
    private var exitTime: Long = 0
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            try {
                throw Exception(event.keyCode.toString())
            } catch (e: Exception) {
                Log.e(TAG,"捕获到异常：${e.message}")
                App.getInstance().reportException(e)
                return false
            }
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyLongPress(keyCode: Int, event: KeyEvent?): Boolean {
        return super.onKeyLongPress(keyCode, event)
    }

    override fun onKeyMultiple(keyCode: Int, repeatCount: Int, event: KeyEvent?): Boolean {
        return super.onKeyMultiple(keyCode, repeatCount, event)
    }

    override fun onKeyShortcut(keyCode: Int, event: KeyEvent?): Boolean {
        return super.onKeyShortcut(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        return super.onKeyUp(keyCode, event)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i("boot", "create SillotHomeActivity activity")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sillot_home)
        // 获取OnBackPressedDispatcher
        val onBackPressedDispatcher = onBackPressedDispatcher
        // 设置OnBackPressedCallback
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 在这里处理后退逻辑
                    if (System.currentTimeMillis() - exitTime > 2000) {
                        PopTip.show("再按一次结束当前活动")
                        exitTime = System.currentTimeMillis()
                    } else {
                        HWs.getInstance().vibratorWaveform(
                            applicationContext,
                            longArrayOf(0, 30, 25, 40, 25, 10),
                            intArrayOf(2, 4, 3, 2, 2, 2),
                            -1
                        )
                        try {
                            Thread.sleep(200)
                        } catch (e: InterruptedException) {
                            Log.w(TAG, e.toString())
                        }
                        Log.w(TAG, "再见")
                        finish()
//                        exitProcess(0)
                    }
                HWs.getInstance().vibratorWaveform(
                    applicationContext,
                    longArrayOf(0, 30, 25, 40, 25),
                    intArrayOf(9, 2, 1, 7, 2),
                    -1
                )
            }
        })


        testmoshi()

        val testRealm = TestRealm()
        testRealm.onCreate()

        val mimeTypeMap = MimeTypeMap.getSingleton()
        mimeTypeMap.hasMimeType("text/html");
        mimeTypeMap.hasExtension("txt");
        mimeTypeMap.getMimeTypeFromExtension("mp4");
        mimeTypeMap.getExtensionFromMimeType("video/mpeg");

        val imageView = findViewById<ImageView>(R.id.testGlide)
        if (imageView != null) {
            imageView.setVisibility(View.VISIBLE)
            imageView.load("https://tse2-mm.cn.bing.net/th/id/OIP-C._2mnBV5bTFR3rgEI5tcrKgHaNK?w=187&h=333&c=7&r=0&o=5&dpr=1.3&pid=1.7") {
                crossfade(true)
                scale(Scale.FILL)
            }
        }
        Log.e(TAG, HWs.getNetworkType(this))
        PopTip.show("测试完毕")
//        val intent = Intent(this, POST_NOTIFICATIONS::class.java)
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        startActivity(intent)
//
//        val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
//        startActivity(intent)
//        FileSelector.from(this)
//            .onlySelectFolder()  //只能选择文件夹
//            .requestCode(1) //设置返回码
//            .start();
    }
}

