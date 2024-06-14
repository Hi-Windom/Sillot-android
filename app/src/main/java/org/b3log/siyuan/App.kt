package org.b3log.siyuan

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.os.Process
import cn.jpush.android.api.JPushInterface
import com.blankj.utilcode.util.Utils
import com.kongzue.dialogx.DialogX
import com.kongzue.dialogx.style.MIUIStyle
import com.microsoft.clarity.Clarity
import com.microsoft.clarity.ClarityConfig
import com.microsoft.clarity.models.LogLevel
import com.tencent.bugly.crashreport.BuglyLog
import com.tencent.bugly.crashreport.CrashReport
import com.tencent.bugly.crashreport.CrashReport.CrashHandleCallback
import com.tencent.bugly.crashreport.CrashReport.UserStrategy
import com.tencent.mmkv.MMKV
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Ignore
import io.realm.kotlin.types.annotations.PrimaryKey
import org.b3log.siyuan.common.ForegroundPushManager
import org.b3log.siyuan.services.BootService
import sc.windom.sofill.S
import sc.windom.sofill.U
import java.net.InetAddress
import java.net.UnknownHostException


// 定义RealmConfiguration，通常在应用启动时初始化
val realmConfig = RealmConfiguration.Builder(setOf(ProviderCache::class))
    .build()

// 获取Realm实例
val realm = Realm.open(realmConfig)


val app by lazy { App.application }

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

//            1. `instance ?:`：这是一个Elvis操作符，用于判断`instance`是否为`null`。如果`instance`不为`null`，则直接返回当前的`instance`。如果`instance`为`null`，则执行右边的代码块。
//            2. `synchronized(this) { ... }`：这是一个同步代码块，它确保在多线程环境中，只有一个线程能够进入这个代码块执行。`this`指的是`App`类的伴生对象，也就是`Companion`对象。这个同步块是必要的，因为在多线程情况下，可能会有多个线程同时尝试创建`App`的实例，同步块确保了这种情况下的线程安全。
//            3. `instance ?: App().also { instance = it }`：这是同步代码块中的代码。首先，它再次检查`instance`是否为`null`（这是必要的，因为可能在等待进入同步块的时候，另一个线程已经创建了实例）。如果`instance`仍然为`null`，则创建一个新的`App`实例，并使用`also`函数将其赋值给`instance`。`also`函数返回它接收的参数，因此这里返回的是新创建的`App`实例。
        }

        lateinit var application: Application
            private set // 确保application只能在App类内部被设置
        @SuppressLint("StaticFieldLeak")
        lateinit var bootService: BootService // 在 activity 中 get / set

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
        BuglyLog.w(TAG, "new one")
        super.onCreate()
        var refCount = 0
        Utils.init(this)
        JPushInterface.setDebugMode(true)
        JPushInterface.init(this)
        MMKV.initialize(this)
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityPaused(activity: Activity) {
                BuglyLog.w(
                    TAG,
                    "onActivityPaused() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
            }

            override fun onActivityStarted(activity: Activity) {
                BuglyLog.w(
                    TAG,
                    "onActivityStarted() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
                refCount++
            }

            override fun onActivityDestroyed(activity: Activity) {
                BuglyLog.w(
                    TAG,
                    "onActivityDestroyed() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
                BuglyLog.w(
                    TAG,
                    "onActivitySaveInstanceState() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
            }

            override fun onActivityStopped(activity: Activity) {
                BuglyLog.w(
                    TAG,
                    "onActivityStopped() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
                refCount--
                if (refCount == 0) {
                    ForegroundPushManager.showNotification(this@App)
                }
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                BuglyLog.w(
                    TAG,
                    "onActivityCreated() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
            }

            override fun onActivityResumed(activity: Activity) {
                BuglyLog.w(
                    TAG,
                    "onActivityResumed() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
                ForegroundPushManager.stopNotification(this@App)
            }

            override fun onActivityPreDestroyed(activity: Activity) {
                BuglyLog.w(
                    TAG,
                    "onActivityPreDestroyed() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
                super.onActivityPreDestroyed(activity)
            }

            override fun onActivityPreCreated(activity: Activity, savedInstanceState: Bundle?) {
                BuglyLog.w(
                    TAG,
                    "onActivityPreCreated() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
                super.onActivityPreCreated(activity, savedInstanceState)
            }

            override fun onActivityPreStarted(activity: Activity) {
                BuglyLog.w(
                    TAG,
                    "onActivityPreStarted() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
                super.onActivityPreStarted(activity)
            }

            override fun onActivityPreStopped(activity: Activity) {
                BuglyLog.w(
                    TAG,
                    "onActivityPreStopped() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
                super.onActivityPreStopped(activity)
            }

            override fun onActivityPrePaused(activity: Activity) {
                BuglyLog.w(
                    TAG,
                    "onActivityPrePaused() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
                super.onActivityPrePaused(activity)
            }

            override fun onActivityPreResumed(activity: Activity) {
                BuglyLog.w(
                    TAG,
                    "onActivityPreResumed() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
                super.onActivityPreResumed(activity)
            }

            override fun onActivityPostCreated(activity: Activity, savedInstanceState: Bundle?) {
                BuglyLog.w(
                    TAG,
                    "onActivityPostCreated() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
                super.onActivityPostCreated(activity, savedInstanceState)
            }

            override fun onActivityPostDestroyed(activity: Activity) {
                BuglyLog.w(
                    TAG,
                    "onActivityPostDestroyed() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
                super.onActivityPostDestroyed(activity)
            }

            override fun onActivityPostPaused(activity: Activity) {
                BuglyLog.w(
                    TAG,
                    "onActivityPostPaused() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
                super.onActivityPostPaused(activity)
            }

            override fun onActivityPostSaveInstanceState(activity: Activity, outState: Bundle) {
                BuglyLog.w(
                    TAG,
                    "onActivityPostSaveInstanceState() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
                super.onActivityPostSaveInstanceState(activity, outState)
            }

            override fun onActivityPostResumed(activity: Activity) {
                BuglyLog.w(
                    TAG,
                    "onActivityPostResumed() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
                super.onActivityPostResumed(activity)
            }

            override fun onActivityPostStarted(activity: Activity) {
                BuglyLog.w(
                    TAG,
                    "onActivityPostStarted() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
                super.onActivityPostStarted(activity)
            }

            override fun onActivityPostStopped(activity: Activity) {
                BuglyLog.w(
                    TAG,
                    "onActivityPostStopped() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
                super.onActivityPostStopped(activity)
            }

            override fun onActivityPreSaveInstanceState(activity: Activity, outState: Bundle) {
                BuglyLog.w(
                    TAG,
                    "onActivityPreSaveInstanceState() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
                super.onActivityPreSaveInstanceState(activity, outState)
            }

        })

        if (U.PHONE.isMIUI(applicationContext) || U.PHONE.isLargeScreenMachine(this)) {
            DialogX.globalStyle = MIUIStyle()
        } else {
            // 其他主题感觉都不好看，暂时默认，以后可能自己弄个
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        BuglyLog.w(
            TAG,
            "onLowMemory() invoked"
        )
    }

    override fun attachBaseContext(base: Context?) { // 在onCreate方法之前。这个方法的目的是将应用程序的上下文与它的基类上下文关联起来。
        super.attachBaseContext(base)
        application = this
        val strategy = UserStrategy(this)
        val sb = StringBuilder()
        sb.append(Build.BRAND).append("-").append(Build.MODEL).append(" (")
            .append(Build.MANUFACTURER).append(")")
        strategy.setDeviceModel(sb.toString())
        // 设置anr时是否获取系统trace文件，默认为false 。抓取堆栈的系统接口 Thread.getStackTrace 可能造成crash，建议只对少量用户开启
        strategy.isEnableCatchAnrTrace = true

        // 获取当前包名
        val packageName: String = this.packageName
        // 获取当前进程名
        val processName = U.DEBUG.getProcessName(Process.myPid())
        // 设置是否为上报进程
        strategy.setUploadProcess(processName == null || processName == packageName)
        strategy.setCrashHandleCallback(object : CrashHandleCallback() {
            override fun onCrashHandleStart(
                crashType: Int, errorType: String,
                errorMessage: String, errorStack: String
            ): Map<String, String> {
                val map = LinkedHashMap<String, String>()
                map["Key"] = "Value"
                return map
            }

            override fun onCrashHandleStart2GetExtraDatas(
                crashType: Int, errorType: String,
                errorMessage: String, errorStack: String
            ): ByteArray {
                return try {
                    "Extra data.".toByteArray(charset("UTF-8"))
                } catch (e: Exception) {
                    e.toString().toByteArray(charset("UTF-8"))
                }
            }
        })
        CrashReport.initCrashReport(this, S.initCrashReportID, true, strategy) // 初始化 bugly
        BuglyLog.setCache(3 * 1024) // 大于阈值会持久化至文件
        val configClarity = ClarityConfig(projectId = S.DEBUG.Clarity_projectId, logLevel = LogLevel.Warning)
        Clarity.initialize(this, configClarity) // 初始化 Clarity
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