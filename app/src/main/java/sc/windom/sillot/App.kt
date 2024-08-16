/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/8/16 18:18
 * updated: 2024/8/16 18:18
 */

package sc.windom.sillot

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.os.Process
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import cn.jpush.android.api.JPushInterface
import com.blankj.utilcode.util.Utils
import com.kongzue.dialogx.DialogX
import com.kongzue.dialogx.style.MIUIStyle
import com.kongzue.dialogx.util.views.ActivityScreenShotImageView
import com.microsoft.clarity.Clarity
import com.microsoft.clarity.ClarityConfig
import com.microsoft.clarity.models.LogLevel
import com.tencent.bugly.crashreport.BuglyLog
import com.tencent.bugly.crashreport.CrashReport
import com.tencent.bugly.crashreport.CrashReport.UserStrategy
import com.tencent.mmkv.MMKV
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Ignore
import io.realm.kotlin.types.annotations.PrimaryKey
import kotlinx.coroutines.DelicateCoroutinesApi
import sc.windom.gibbet.MG.ForegroundPushManager
import sc.windom.sillot.workers.ActivityRunInBgWorker
import sc.windom.sofill.S
import sc.windom.sofill.U
import sc.windom.sofill.Us.U_Phone.setPreferredDisplayMode
import sc.windom.sofill.android.lifecycle.AppMonitor
import sc.windom.sofill.annotations.SillotActivity
import sc.windom.sofill.annotations.SillotActivityType
import java.lang.StringBuilder
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.LinkedHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlin.also
import kotlin.collections.set
import kotlin.jvm.javaClass
import kotlin.let
import kotlin.text.toByteArray
import kotlin.toString


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

        fun getByName(ip: String?): InetAddress? {
            return try {
                InetAddress.getByName(ip)
            } catch (unused: UnknownHostException) {
                null
            }
        }

        val isMainThread: Boolean
            get() = Looper.getMainLooper().thread.id == Thread.currentThread().id

        @JvmField
        val currentIntentRef = AtomicReference<Intent>()
    }

    fun startTargetActivity() {
        val intent = currentIntentRef.get()
        startActivity(intent)
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate() {
        BuglyLog.w(TAG, "new one")
        super.onCreate()
        var refCount = 0
        val workManager = WorkManager.getInstance(this)
        Utils.init(this)
        JPushInterface.setDebugMode(true)
        JPushInterface.init(this)
        MMKV.initialize(this)
        initAppMonitor()
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

                val annotations =
                    activity.javaClass.getAnnotationsByType(SillotActivity::class.java)

                // 遍历注解并处理每个注解
                annotations.forEach { annotation ->
                    BuglyLog.d(
                        TAG,
                        "onActivityStarted() invoked -> the activity's annotation.TYPE ${annotation.TYPE}"
                    )
                    if (annotation.TYPE == SillotActivityType.Main) {
                        currentIntentRef.set(activity.intent)
                    }
                }
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

            @SuppressLint("RestrictedApi")
            override fun onActivityStopped(activity: Activity) {
                BuglyLog.w(
                    TAG,
                    "onActivityStopped() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
                refCount--
                if (refCount == 0) {
                    ForegroundPushManager.showNotification(this@App)
                }
                // 检查活动是否是MatrixModel的实例
                if (activity is MatrixModel) {
                    val matrixModel = activity.getMatrixModel()
                    BuglyLog.w("App", "Matrix_model: $matrixModel")
                    val constraints = Constraints.Builder()
                        .build()
                    val data = Data.Builder()
                        .putString("activity", activity.javaClass.name)
                        .putString("matrixModel", matrixModel)
                        .build()
                    // 创建一个OneTimeWorkRequest
                    val oneTimeWorkRequest =
                        OneTimeWorkRequest.Builder(ActivityRunInBgWorker::class.java)
                            .setConstraints(constraints)
                            .setInputData(data)
                            .setInitialDelay(2, TimeUnit.SECONDS) // 在频繁切换活动时至少要2秒延时才能来得及取消
                            .build()

                    // 将任务加入到WorkManager中，并设置一个UniqueWork名称
                    workManager.enqueueUniqueWork(
                        "${activity.javaClass.name}进入后台运行提醒",
                        ExistingWorkPolicy.REPLACE, // 每次都替换之前的任务
                        oneTimeWorkRequest
                    )

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
                activity.setPreferredDisplayMode() // 全局高刷新率
                super.onActivityPreCreated(activity, savedInstanceState)
            }

            override fun onActivityPreStarted(activity: Activity) {
                BuglyLog.w(
                    TAG,
                    "onActivityPreStarted() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
                workManager.cancelUniqueWork("${activity.javaClass.name}进入后台运行提醒")
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

        DialogX.globalTheme = DialogX.THEME.AUTO
        DialogX.autoRunOnUIThread = true // 自动在主线程执行
        if (U.PHONE.isMIUI(applicationContext) || U.PHONE.isLargeScreenMachine(this)) {
            DialogX.globalStyle = MIUIStyle()
        } else {
            // 其他主题感觉都不好看，暂时默认，以后可能自己弄个
        }
        ActivityScreenShotImageView.hideContentView =
            true; // https://github.com/kongzue/DialogX/wiki/%E5%85%A8%E5%B1%8F%E5%AF%B9%E8%AF%9D%E6%A1%86-FullScreenDialog
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
        strategy.setCrashHandleCallback(object : CrashReport.CrashHandleCallback() {
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
        val configClarity =
            ClarityConfig(projectId = S.DEBUG.Clarity_projectId, logLevel = LogLevel.Warning)
        Clarity.initialize(this, configClarity) // 初始化 Clarity
    }

    fun reportException(throwable: Throwable?) {
        // Ensure throwable is not null before reporting
        throwable?.let {
            // 主动上传到 bugly
            CrashReport.postCatchedException(it)
        }
    }

    private fun initAppMonitor() {
        val funTAG = "$TAG initAppMonitor"
        //初始化
        AppMonitor.initialize(this, true)
        //注册监听 App 状态变化（前台，后台）
        AppMonitor.registerAppStatusCallback(object : AppMonitor.OnAppStatusCallback {
            override fun onAppForeground(activity: Activity) {
                //App 切换到前台
                BuglyLog.d(funTAG, "onAppForeground(Activity = $activity)")
            }

            override fun onAppBackground(activity: Activity) {
                //App 切换到后台
                BuglyLog.d(funTAG, "onAppBackground(Activity = $activity)")
            }

        })
        //注册监听 Activity 状态变化
        AppMonitor.registerActivityStatusCallback(object : AppMonitor.OnActivityStatusCallback {
            override fun onAliveStatusChanged(
                activity: Activity,
                isAliveState: Boolean,
                aliveActivityCount: Int
            ) {
                //Activity 的存活状态或数量发生变化
                BuglyLog.d(
                    funTAG,
                    "onAliveStatusChanged(Activity = $activity, isAliveState = $isAliveState, aliveActivityCount = $aliveActivityCount)"
                )
            }

            override fun onActiveStatusChanged(
                activity: Activity,
                isActiveState: Boolean,
                activeActivityCount: Int
            ) {
                //Activity 的活跃状态或数量发生变化
                BuglyLog.d(
                    funTAG,
                    "onActiveStatusChanged(Activity = $activity, isActiveState = $isActiveState, activeActivityCount = $activeActivityCount)"
                )
            }

        })

        //注册监听屏幕状态变化（开屏、关屏、解锁）
        AppMonitor.registerScreenStatusCallback(object : AppMonitor.OnScreenStatusCallback {
            override fun onScreenStatusChanged(isScreenOn: Boolean) {
                //屏幕状态发生变化（开屏或关屏）
                BuglyLog.d(funTAG, "onScreenStatusChanged(isScreenOn = $isScreenOn)")
            }

            override fun onUserPresent() {
                //解锁：当设备唤醒后，用户在（解锁键盘消失）时回调
                BuglyLog.d(funTAG, "onUserPresent()")
            }

        })
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