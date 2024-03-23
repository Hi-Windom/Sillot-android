package org.b3log.siyuan

import android.app.Activity
import android.app.Application
import android.os.Bundle
import cn.jpush.android.api.JPushInterface
import com.baidu.android.pushservice.PushConstants
import com.baidu.android.pushservice.PushManager
import com.blankj.utilcode.util.Utils
import org.b3log.siyuan.common.ForegroundPushManager


class App : Application() {
    override fun onCreate() {
        var refCount = 0
        super.onCreate()
        Utils.init(this)
        xcrash.XCrash.init(this)
        JPushInterface.setDebugMode(true)
        JPushInterface.init(this)
        // 初始化 baidu PUSH
        PushManager.startWork(applicationContext, PushConstants.LOGIN_TYPE_API_KEY, Ss.initBaiduPushID);
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityPaused(activity: Activity) {
            }

            override fun onActivityStarted(activity: Activity) {
                refCount++
            }

            override fun onActivityDestroyed(activity: Activity) {
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            }

            override fun onActivityStopped(activity: Activity) {
                refCount--
                if (refCount == 0) {
                    ForegroundPushManager.showNotification(this@App)
                }
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            }

            override fun onActivityResumed(activity: Activity) {
                ForegroundPushManager.stopNotification(this@App)
            }

        })


    }
}