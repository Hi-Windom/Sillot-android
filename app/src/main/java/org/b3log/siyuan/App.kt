package org.b3log.siyuan

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.os.Looper
import android.util.Log
import cn.jpush.android.api.JPushInterface
import com.baidu.android.pushservice.PushConstants
import com.baidu.android.pushservice.PushManager
import com.blankj.utilcode.util.Utils
import com.kongzue.dialogx.DialogX
import com.kongzue.dialogx.style.MIUIStyle
import com.kwai.koom.base.DefaultInitTask.init
import com.kwai.koom.base.MonitorLog
import com.kwai.koom.base.MonitorManager
import com.kwai.koom.javaoom.monitor.OOMHprofUploader
import com.kwai.koom.javaoom.monitor.OOMMonitor
import com.kwai.koom.javaoom.monitor.OOMMonitorConfig
import com.kwai.koom.javaoom.monitor.OOMReportUploader
import com.microsoft.clarity.Clarity
import com.microsoft.clarity.ClarityConfig
import com.microsoft.clarity.models.LogLevel
import com.tencent.bugly.crashreport.CrashReport
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Ignore
import io.realm.kotlin.types.annotations.PrimaryKey
import org.b3log.siyuan.common.ForegroundPushManager
import java.io.File
import java.net.InetAddress
import java.net.UnknownHostException


val app by lazy { App.application }

// 定义RealmConfiguration，通常在应用启动时初始化
val realmConfig = RealmConfiguration.Builder(setOf(ProviderCache::class))
    .build()

// 获取Realm实例
val realm = Realm.open(realmConfig)

class App : Application() {
    var TAG = "App"
    companion object {
        @Volatile
        private var instance: App? = null

        @JvmStatic
        fun getInstance(): App { // 兼容 java 代码，在 kotlin 也需用这个
            return instance ?: synchronized(this) {
                instance ?: App().also { instance = it }
            }
        }
        lateinit var application: Application

        fun getByName(ip: String?): InetAddress? {
            return try {
                InetAddress.getByName(ip)
            } catch (unused: UnknownHostException) {
                null
            }
        }

        val isMainThread: Boolean
            get() = Looper.getMainLooper().thread.id == Thread.currentThread().id
    }
    override fun onCreate() {
        var refCount = 0
        super.onCreate()
        instance = this
        Utils.init(this)
        val configClarity = ClarityConfig(projectId="gqgzluae5t",logLevel= LogLevel.Verbose)
        Clarity.initialize(this, configClarity) // 初始化 Clarity
        CrashReport.initCrashReport(applicationContext, Ss.initCrashReportID, true) // 初始化 bugly
        JPushInterface.setDebugMode(true)
        JPushInterface.init(this)
        // 初始化 baidu PUSH
        PushManager.startWork(applicationContext, PushConstants.LOGIN_TYPE_API_KEY, Ss.initBaiduPushID);
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityPaused(activity: Activity) {
                Log.w(TAG, "onActivityPaused() involved -> Activity : ${activity.javaClass.simpleName}")
            }

            override fun onActivityStarted(activity: Activity) {
                Log.w(TAG, "onActivityStarted() involved -> Activity : ${activity.javaClass.simpleName}")
                refCount++
            }

            override fun onActivityDestroyed(activity: Activity) {
                Log.w(TAG, "onActivityDestroyed() involved -> Activity : ${activity.javaClass.simpleName}")
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
                Log.w(TAG, "onActivitySaveInstanceState() involved -> Activity : ${activity.javaClass.simpleName}")
            }

            override fun onActivityStopped(activity: Activity) {
                Log.w(TAG, "onActivityStopped() involved -> Activity : ${activity.javaClass.simpleName}")
                refCount--
                if (refCount == 0) {
                    ForegroundPushManager.showNotification(this@App)
                }
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                Log.w(TAG, "onActivityCreated() involved -> Activity : ${activity.javaClass.simpleName}")
            }

            override fun onActivityResumed(activity: Activity) {
                Log.w(TAG, "onActivityResumed() involved -> Activity : ${activity.javaClass.simpleName}")
                ForegroundPushManager.stopNotification(this@App)
            }

            override fun onActivityPreDestroyed(activity: Activity) {
                Log.w(TAG, "onActivityPreDestroyed() involved -> Activity : ${activity.javaClass.simpleName}")
                super.onActivityPreDestroyed(activity)
            }

            override fun onActivityPreCreated(activity: Activity, savedInstanceState: Bundle?) {
                Log.w(TAG, "onActivityPreCreated() involved -> Activity : ${activity.javaClass.simpleName}")
                super.onActivityPreCreated(activity, savedInstanceState)
            }

            override fun onActivityPreStarted(activity: Activity) {
                Log.w(TAG, "onActivityPreStarted() involved -> Activity : ${activity.javaClass.simpleName}")
                super.onActivityPreStarted(activity)
            }

            override fun onActivityPreStopped(activity: Activity) {
                Log.w(TAG, "onActivityPreStopped() involved -> Activity : ${activity.javaClass.simpleName}")
                super.onActivityPreStopped(activity)
            }

            override fun onActivityPrePaused(activity: Activity) {
                Log.w(TAG, "onActivityPrePaused() involved -> Activity : ${activity.javaClass.simpleName}")
                super.onActivityPrePaused(activity)
            }

            override fun onActivityPreResumed(activity: Activity) {
                Log.w(TAG, "onActivityPreResumed() involved -> Activity : ${activity.javaClass.simpleName}")
                super.onActivityPreResumed(activity)
            }

            override fun onActivityPostCreated(activity: Activity, savedInstanceState: Bundle?) {
                Log.w(TAG, "onActivityPostCreated() involved -> Activity : ${activity.javaClass.simpleName}")
                super.onActivityPostCreated(activity, savedInstanceState)
            }

            override fun onActivityPostDestroyed(activity: Activity) {
                Log.w(TAG, "onActivityPostDestroyed() involved -> Activity : ${activity.javaClass.simpleName}")
                super.onActivityPostDestroyed(activity)
            }

            override fun onActivityPostPaused(activity: Activity) {
                Log.w(TAG, "onActivityPostPaused() involved -> Activity : ${activity.javaClass.simpleName}")
                super.onActivityPostPaused(activity)
            }

            override fun onActivityPostSaveInstanceState(activity: Activity, outState: Bundle) {
                Log.w(TAG, "onActivityPostSaveInstanceState() involved -> Activity : ${activity.javaClass.simpleName}")
                super.onActivityPostSaveInstanceState(activity, outState)
            }

            override fun onActivityPostResumed(activity: Activity) {
                Log.w(TAG, "onActivityPostResumed() involved -> Activity : ${activity.javaClass.simpleName}")
                super.onActivityPostResumed(activity)
            }

            override fun onActivityPostStarted(activity: Activity) {
                Log.w(TAG, "onActivityPostStarted() involved -> Activity : ${activity.javaClass.simpleName}")
                super.onActivityPostStarted(activity)
            }

            override fun onActivityPostStopped(activity: Activity) {
                Log.w(TAG, "onActivityPostStopped() involved -> Activity : ${activity.javaClass.simpleName}")
                super.onActivityPostStopped(activity)
            }

            override fun onActivityPreSaveInstanceState(activity: Activity, outState: Bundle) {
                Log.w(TAG, "onActivityPreSaveInstanceState() involved -> Activity : ${activity.javaClass.simpleName}")
                super.onActivityPreSaveInstanceState(activity, outState)
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

        if (Us.isMIUI(applicationContext) || Us.isLargeScreenMachine(this)) {
            DialogX.globalStyle = MIUIStyle()
        } else {
            // 其他主题感觉都不好看，暂时默认，以后可能自己弄个
        }
    }

     override fun attachBaseContext(base: Context?) {
         super.attachBaseContext(base)
         application = this
     }
     fun reportException(throwable: Throwable?) {
         // Ensure throwable is not null before reporting
         throwable?.let {
             // 主动上传到 bugly
             CrashReport.postCatchedException(it)
         }
     }

 }

open class ProviderCache : RealmObject {
    @PrimaryKey
    var id: Long = 0

    var path: String = ""
    @Ignore
    var status: Status = Status.PADDING
    var modifier: String = ""
    var isDirection: Boolean = false

    enum class Status {
        PADDING,
        DONE
    }
}
open class ServerLogRealm : RealmObject {
    @PrimaryKey
    var id: Long = 0
    @Ignore
    var level: LogLevel = LogLevel.INFO
    var message: String = ""
    var description: String? = null

    enum class LogLevel {
        DEBUG,
        INFO,
        WARN,
        ERROR
    }
}