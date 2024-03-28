package org.b3log.siyuan

import android.app.Activity
import android.app.Application
import android.os.Bundle
import cn.jpush.android.api.JPushInterface
import com.baidu.android.pushservice.PushConstants
import com.baidu.android.pushservice.PushManager
import com.blankj.utilcode.util.Utils
import com.kwai.koom.base.DefaultInitTask.init
import com.kwai.koom.base.MonitorLog
import com.kwai.koom.base.MonitorManager
import com.kwai.koom.javaoom.monitor.OOMHprofUploader
import com.kwai.koom.javaoom.monitor.OOMMonitor
import com.kwai.koom.javaoom.monitor.OOMMonitorConfig
import com.kwai.koom.javaoom.monitor.OOMReportUploader
import org.b3log.siyuan.common.ForegroundPushManager
import java.io.File


class App : Application() {
    override fun onCreate() {
        var refCount = 0
        super.onCreate()
        Utils.init(this)
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


        init(this)
        val config = OOMMonitorConfig.Builder()
            .setThreadThreshold(50) //50 only for test! Please use default value!
            .setFdThreshold(300) // 300 only for test! Please use default value!
            .setHeapThreshold(0.9f) // 0.9f for test! Please use default value!
            .setVssSizeThreshold(1_000_000) // 1_000_000 for test! Please use default value!
            .setMaxOverThresholdCount(1) // 1 for test! Please use default value!
            .setAnalysisMaxTimesPerVersion(3) // Consider use default value！
            .setAnalysisPeriodPerVersion(15 * 24 * 60 * 60 * 1000) // Consider use default value！
            .setLoopInterval(5_000) // 5_000 for test! Please use default value!
            .setEnableHprofDumpAnalysis(true)
            .setHprofUploader(object: OOMHprofUploader {
                override fun upload(file: File, type: OOMHprofUploader.HprofType) {
                    MonitorLog.e("OOMMonitor", "todo, upload hprof ${file.name} if necessary")
                }
            })
            .setReportUploader(object: OOMReportUploader {
                override fun upload(file: File, content: String) {
                    MonitorLog.i("OOMMonitor", content)
                    MonitorLog.e("OOMMonitor", "todo, upload report ${file.name} if necessary")
                }
            })
            .build()

        MonitorManager.addMonitorConfig(config)
        OOMMonitor.startLoop() // 启动 OOMMonitor，开始周期性的检测泄漏
    }
}