/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/8/21 11:42
 * updated: 2024/8/21 11:42
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
import android.util.Log
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
import org.b3log.siyuan.MainActivity
import sc.windom.gibbet.MG.ForegroundPushManager
import sc.windom.sillot.workers.ActivityRunInBgWorker
import sc.windom.sillot.workers.ActivityRunInMultiWindowModeWorker
import sc.windom.sofill.S
import sc.windom.sofill.U
import sc.windom.sofill.Us.U_Phone.setPreferredDisplayMode
import sc.windom.sofill.Us.addFlagsForMatrixModel
import sc.windom.sofill.android.lifecycle.AppMonitor
import sc.windom.sofill.annotations.SillotActivity
import sc.windom.sofill.annotations.SillotActivityType
import java.lang.StringBuilder
import java.util.LinkedHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
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

/**
 * 在Kotlin中，`by lazy` 是一种委托属性，它确保了属性的值只在首次访问时计算一次，并在后续访问时返回相同的值。而直接访问 companion object 中的属性则是直接访问那个属性。
 * 具体来说，`App.instance` 和 `appIns` 的区别如下：
 * 1. **延迟初始化 (`by lazy`)**:
 *    - `val app by lazy { App.application }` 这行代码意味着 `app` 属性会在首次访问时被初始化。如果 `App.application` 的初始化过程很昂贵（比如需要加载资源或者进行网络请求），使用 `by lazy` 可以延迟这个初始化过程直到真正需要它的时候。此外，`by lazy` 还确保了线程安全，即在多线程环境中，`app` 属性的初始化只会发生一次。
 * 2. **直接访问**:
 *    - `val appIns by lazy { App.instance }` 这行代码也是延迟初始化，但是它延迟的是对 `App.instance` 的访问，而不是 `App.instance` 自身的初始化。`App.instance` 是在 `App` 类的 `onCreate` 方法中初始化的（通常是在应用启动时），因此 `App.instance` 总是会在应用的生命周期早期就被初始化，而 `appIns` 则是首次访问这个属性时才会进行赋值。
 * 以下是具体的区别：
 * - **初始化时机**:
 *   - `App.instance` 在 `App` 类的 `onCreate` 方法中被初始化，通常是在应用启动时。
 *   - `appIns` 在首次访问该属性时被初始化。
 * - **线程安全**:
 *   - `App.instance` 的初始化通常在主线程中完成，由系统保证线程安全。
 *   - `appIns` 的初始化由 `by lazy` 代理确保线程安全。
 * - **性能考量**:
 *   - 如果 `App.instance` 的初始化非常轻量级，那么使用 `appIns` 可能不会带来明显的性能优势，因为 `App.instance` 已经在应用启动时初始化了。
 *   - 如果 `App.instance` 的初始化非常耗时，那么使用 `appIns` 可以确保只有当真正需要时才进行初始化。
 * - **代码意图**:
 *   - 直接使用 `App.instance` 通常表明你希望直接访问应用实例。
 *   - 使用 `appIns` 可能表明你希望延迟对应用实例的访问，直到真正需要的时候。
 * 在大多数情况下，如果你只是想要访问 `App` 的单例实例，直接使用 `App.instance` 就足够了。使用 `by lazy` 只有在你确实需要延迟访问时才有意义。
 * 当然，`app` 更简洁，这也是一个优势。
 *
 */
val app by lazy { App.application }
val appIns by lazy { App.instance }

/**
 * 汐洛 APP
 */
class App : Application() {
    val TAG = "App"

    companion object {
        @JvmStatic
        lateinit var instance: App
            private set

        lateinit var application: Application
            private set

        val isMainThread: Boolean
            get() = Looper.getMainLooper().thread.id == Thread.currentThread().id

        const val activityRunNote1 = "进入后台/多窗口模式运行提醒"

    }

    @JvmField
    val currentIntentRef = AtomicReference<Intent>()

    fun startTargetActivity() {
        if (currentIntentRef.get() == null) {
            Intent(this, MainActivity::class.java).apply {
                addFlagsForMatrixModel()
            }.also {
                Log.d("App", "startTargetActivity @ new $it")
                startActivity(it)
            }
        } else {
            currentIntentRef.get().let {
                Log.d("App", "startTargetActivity @ $it")
                startActivity(it)
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate() {
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
                Log.w(
                    TAG,
                    "onActivityPaused() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
            }

            override fun onActivityStarted(activity: Activity) {
                Log.w(
                    TAG,
                    "onActivityStarted() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
                refCount++

                val annotations =
                    activity.javaClass.getAnnotationsByType(SillotActivity::class.java)

                // 遍历注解并处理每个注解
                annotations.forEach { annotation ->
                    Log.d(
                        TAG,
                        "onActivityStarted() invoked -> the activity's annotation.TYPE ${annotation.TYPE}"
                    )
                    if (annotation.TYPE == SillotActivityType.Main) {
                        currentIntentRef.set(activity.intent)
                    }
                }
            }

            override fun onActivityDestroyed(activity: Activity) {
                Log.w(
                    TAG,
                    "onActivityDestroyed() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
                Log.w(
                    TAG,
                    "onActivitySaveInstanceState() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
            }

            @SuppressLint("RestrictedApi")
            override fun onActivityStopped(activity: Activity) {
                Log.w(
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
                    Log.w("App", "Matrix_model: $matrixModel")
                    val constraints = Constraints.Builder()
                        .build()
                    val data = Data.Builder()
                        .putString("activity", activity.javaClass.name)
                        .putString("matrixModel", matrixModel)
                        .build()
                    // 创建一个OneTimeWorkRequest
                    var oneTimeWorkRequest: OneTimeWorkRequest? = null

                    // 将任务加入到WorkManager中，并设置一个UniqueWork名称
                    if (activity.isInMultiWindowMode) {
                        oneTimeWorkRequest = OneTimeWorkRequest.Builder(ActivityRunInMultiWindowModeWorker::class.java)
                            .setConstraints(constraints)
                            .setInputData(data)
                            .setInitialDelay(2, TimeUnit.SECONDS) // 在频繁切换活动时至少要2秒延时才能来得及取消
                            .build()
                    } else {
                        oneTimeWorkRequest = OneTimeWorkRequest.Builder(ActivityRunInBgWorker::class.java)
                            .setConstraints(constraints)
                            .setInputData(data)
                            .setInitialDelay(2, TimeUnit.SECONDS) // 在频繁切换活动时至少要2秒延时才能来得及取消
                            .build()
                    }
                    workManager.enqueueUniqueWork(
                        "${activity.javaClass.name}${activityRunNote1}",
                        ExistingWorkPolicy.REPLACE, // 每次都替换之前的任务
                        oneTimeWorkRequest
                    )
                }
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                Log.w(
                    TAG,
                    "onActivityCreated() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
            }

            override fun onActivityResumed(activity: Activity) {
                Log.w(
                    TAG,
                    "onActivityResumed() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
                ForegroundPushManager.stopNotification(this@App)
            }

            override fun onActivityPreDestroyed(activity: Activity) {
                Log.w(
                    TAG,
                    "onActivityPreDestroyed() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
                super.onActivityPreDestroyed(activity)
            }

            override fun onActivityPreCreated(activity: Activity, savedInstanceState: Bundle?) {
                Log.w(
                    TAG,
                    "onActivityPreCreated() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
                activity.setPreferredDisplayMode() // 全局高刷新率
                super.onActivityPreCreated(activity, savedInstanceState)
            }

            override fun onActivityPreStarted(activity: Activity) {
                Log.w(
                    TAG,
                    "onActivityPreStarted() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
                workManager.cancelUniqueWork("${activity.javaClass.name}${activityRunNote1}")
                super.onActivityPreStarted(activity)
            }

            override fun onActivityPreStopped(activity: Activity) {
                Log.w(
                    TAG,
                    "onActivityPreStopped() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
                super.onActivityPreStopped(activity)
            }

            override fun onActivityPrePaused(activity: Activity) {
                Log.w(
                    TAG,
                    "onActivityPrePaused() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
                super.onActivityPrePaused(activity)
            }

            override fun onActivityPreResumed(activity: Activity) {
                Log.w(
                    TAG,
                    "onActivityPreResumed() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
                super.onActivityPreResumed(activity)
            }

            override fun onActivityPostCreated(activity: Activity, savedInstanceState: Bundle?) {
                Log.w(
                    TAG,
                    "onActivityPostCreated() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
                super.onActivityPostCreated(activity, savedInstanceState)
            }

            override fun onActivityPostDestroyed(activity: Activity) {
                Log.w(
                    TAG,
                    "onActivityPostDestroyed() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
                super.onActivityPostDestroyed(activity)
            }

            override fun onActivityPostPaused(activity: Activity) {
                Log.w(
                    TAG,
                    "onActivityPostPaused() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
                super.onActivityPostPaused(activity)
            }

            override fun onActivityPostSaveInstanceState(activity: Activity, outState: Bundle) {
                Log.w(
                    TAG,
                    "onActivityPostSaveInstanceState() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
                super.onActivityPostSaveInstanceState(activity, outState)
            }

            override fun onActivityPostResumed(activity: Activity) {
                Log.w(
                    TAG,
                    "onActivityPostResumed() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
                super.onActivityPostResumed(activity)
            }

            override fun onActivityPostStarted(activity: Activity) {
                Log.w(
                    TAG,
                    "onActivityPostStarted() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
                super.onActivityPostStarted(activity)
            }

            override fun onActivityPostStopped(activity: Activity) {
                Log.w(
                    TAG,
                    "onActivityPostStopped() invoked -> Activity : ${activity.javaClass.simpleName}"
                )
                super.onActivityPostStopped(activity)
            }

            override fun onActivityPreSaveInstanceState(activity: Activity, outState: Bundle) {
                Log.w(
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
        Log.w(
            TAG,
            "onLowMemory() invoked"
        )
    }

    override fun attachBaseContext(base: Context?) { // 在onCreate方法之前。这个方法的目的是将应用程序的上下文与它的基类上下文关联起来。
        super.attachBaseContext(base)
        Log.w(TAG, "new app base on $base")
        application = this
        instance = this
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
        val funTAG = "$TAG AppMonitor"
        //初始化
        AppMonitor.initialize(this, true)
        //注册监听 App 状态变化（前台，后台）
        AppMonitor.registerAppStatusCallback(object : AppMonitor.OnAppStatusCallback {
            override fun onAppForeground(activity: Activity) {
                //App 切换到前台
                Log.d(funTAG, "onAppForeground(Activity = $activity)")
            }

            override fun onAppBackground(activity: Activity) {
                //App 切换到后台
                Log.d(funTAG, "onAppBackground(Activity = $activity)")
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
                Log.d(
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
                Log.d(
                    funTAG,
                    "onActiveStatusChanged(Activity = $activity, isActiveState = $isActiveState, activeActivityCount = $activeActivityCount)"
                )
            }

        })

        //注册监听屏幕状态变化（开屏、关屏、解锁）
        AppMonitor.registerScreenStatusCallback(object : AppMonitor.OnScreenStatusCallback {
            override fun onScreenStatusChanged(isScreenOn: Boolean) {
                //屏幕状态发生变化（开屏或关屏）
                Log.d(funTAG, "onScreenStatusChanged(isScreenOn = $isScreenOn)")
            }

            override fun onUserPresent() {
                //解锁：当设备唤醒后，用户在（解锁键盘消失）时回调
                Log.d(funTAG, "onUserPresent()")
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