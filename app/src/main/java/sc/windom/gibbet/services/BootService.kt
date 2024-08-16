/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/8/16 18:54
 * updated: 2024/8/16 18:54
 */

package sc.windom.gibbet.services

import android.Manifest
import android.app.Activity
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.provider.Settings
import android.webkit.WebView
import androidx.core.app.ActivityCompat
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.blankj.utilcode.util.ServiceUtils
import com.tencent.bugly.crashreport.BuglyLog
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.cio.CIOApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import mobile.Mobile
import org.apache.commons.io.FileUtils
import org.b3log.siyuan.Utils
import sc.windom.gibbet.workers.CheckHttpServerWorker
import sc.windom.gibbet.workers.SyncDataWorker
import sc.windom.sofill.S
import sc.windom.sofill.Us.U_Thread.runOnUiThread
import sc.windom.sofill.Us.getWebViewVer
import sc.windom.sofill.android.webview.WebPoolsPro
import sc.windom.sofill.dataClass.ISiyuanFilelockWalk
import sc.windom.sofill.dataClass.ISiyuanFilelockWalkRes
import sc.windom.sofill.dataClass.ISiyuanFilelockWalkResFiles
import sc.windom.sofill.dataClass.ISiyuanFilelockWalkResFilesItem
import java.io.File
import java.net.InetAddress
import java.net.ServerSocket
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class BootService : Service() {
    private val TAG = "services/BootService.kt"

    @JvmField
    val checkHttpServerWorkerName = "CheckHttpServerWork"

    @JvmField
    val workManager = WorkManager.getInstance(this)
    var server: CIOApplicationEngine? = null
    var serverPort = S.AndroidServerPort
    val localIPs = Utils.getIPAddressList()
    var webView: WebView? = null
    var webViewVer: String? = null
    var userAgent: String? = null
    var kernelStarted = false
    var stopKernelOnDestroy = true
    private lateinit var mHandlerThread: HandlerThread
    private lateinit var mHandler: Handler
    private var webViewKey: String? = null
    private lateinit var dataDir: String
    private lateinit var appDir: String
    override fun onCreate() {
        super.onCreate()
        dataDir = filesDir.absolutePath
        appDir = "$dataDir/app"
        mHandlerThread = HandlerThread("MyHandlerThread")
        mHandlerThread.start()
        mHandler = Handler(mHandlerThread.looper)
        works()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        BuglyLog.i(TAG, "onStartCommand() -> intent: $intent")
        works()
        return START_REDELIVER_INTENT // 如果 Service 被杀死，系统会尝试重新创建 Service，并且会重新传递最后一个 Intent 给 Service 的 onStartCommand() 方法。
    }

    override fun onDestroy() {
        super.onDestroy()
        BuglyLog.i(TAG, "onDestroy() invoked")
        webView?.let { webViewKey?.let { it1 -> WebPoolsPro.instance?.recycle(it, it1) } }
        server?.stop()
        if (stopKernelOnDestroy) Mobile.stopKernel() else server?.stop()
    }

    private val binder = LocalBinder()

    override fun onBind(intent: Intent): IBinder {
        webViewKey = intent.getStringExtra(S.INTENT.EXTRA_WEB_VIEW_KEY)
        return binder
    }

    inner class LocalBinder : Binder() {
        fun getService(): BootService = this@BootService
    }

    private fun works() {
        BuglyLog.d(TAG, "-> 初始化 UI 元素")
        init_webView()

        BuglyLog.d(TAG, "-> 拉起内核")
        startKernel()

//        BuglyLog.d(TAG, "-> 周期同步数据")
//        scheduleSyncDataWork()

        BuglyLog.d(TAG, "-> 内核心跳检测")
        CheckHttpServerWork()
    }

    private fun init_webView() {
        // 不使用 activity 的上下文会导致 https://github.com/Hi-Windom/Sillot/issues/814 这里改为获取在 MainActivity 初始化好的 webView
        webView =
            webViewKey?.let { WebPoolsPro.instance?.acquireWebView(it) }
        webView?.setBackgroundColor(Color.parseColor(S.ColorStringHex.bgColor_light))
        val ws = webView?.settings
        userAgent = ws?.userAgentString
        webViewVer = this.getWebViewVer()
    }

    private val bootHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            val cmd = msg.getData().getString("cmd")
            if ("startKernel" == cmd) {
                bootKernel()
            } else {
                BuglyLog.w(TAG, cmd.toString())
            }
        }
    }

    fun isHttpServerRunning(): Boolean {
        return server != null
    }

    fun startHttpServer() {
        if (isHttpServerRunning()) {
            server?.stop()
            BuglyLog.w(TAG, "startHttpServer() stop exist server")
        }
        try {
            // 解决乱码问题 https://github.com/koush/AndroidAsync/issues/656#issuecomment-523325452
            val charsetClass = Charsets::class.java
            val usAscii = charsetClass.getDeclaredField("US_ASCII")
            usAscii.isAccessible = true
            usAscii[Charsets::class.java] = Charsets.UTF_8
        } catch (e: Exception) {
            Utils.LogError(TAG, "init charset failed", e)
        }
        /**
         * localIPs 多于一个则绑定所有网卡以便通过局域网IP访问；
         * localIPs 只有一个则绑定回环地址 可能是 127.0.0.1 也可能是 [::1]
         */
        val inetAddress: String =
            if (localIPs.split(',').size > 1) "0.0.0.0" else InetAddress.getLoopbackAddress().hostAddress
        server = embeddedServer(CIO, port = getAvailablePort(), host = inetAddress) {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                })
            }
            routing {
                post("/api/walkDir") {
                    withContext(Dispatchers.IO) { // 使用 IO 协程上下文处理文件系统操作
                        val start = System.currentTimeMillis()
                        try {
//                        BuglyLog.w(TAG, "${call.request.contentLength()} ${call.request.contentType()}")
                            val requestJSON = call.receive<ISiyuanFilelockWalk>()
                            val dir = requestJSON.dir
                            val directory = File(dir)
                            val filesList =
                                ISiyuanFilelockWalkResFiles(files = mutableListOf<ISiyuanFilelockWalkResFilesItem>())
                            directory.walkTopDown().filter { it.isDirectory || it.isFile }
                                .forEach { file ->
                                    filesList.files.add(
                                        ISiyuanFilelockWalkResFilesItem(
                                            path = file.absolutePath,
                                            name = file.name,
                                            size = file.length(),
                                            updated = file.lastModified(),
                                            isDir = file.isDirectory
                                        )
                                    )
                                }

                            call.respond(
                                ISiyuanFilelockWalkRes(
                                    code = 0,
                                    msg = "",
                                    data = filesList
                                )
                            )
                            Utils.LogInfo(
                                TAG,
                                "walk dir [$dir] in [${System.currentTimeMillis() - start}] ms"
                            )
                        } catch (e: Exception) {
                            Utils.LogError(TAG, "walk dir failed: ${e.message}", e)
                            call.respond(
                                ISiyuanFilelockWalkRes(
                                    code = 0,
                                    msg = e.stackTraceToString(),
                                    data = null
                                )
                            )
                        }
                    }
                }
            }
        }
        server?.let {
            it.start(wait = false) // 不等待阻塞
            val actualPort = it.environment.connectors.first().port
            val actualHost = it.environment.connectors.first().host
            Utils.LogInfo(TAG, "HTTP server is listening on ${actualHost}, port [$actualPort]")
        }
    }

    private fun getAvailablePort(): Int {
        var ret = serverPort
        try {
            ServerSocket(serverPort).use { socket ->
                ret = socket.localPort
            }
        } catch (e: Exception) {
            Utils.LogError(
                TAG,
                "$serverPort not available: ${e.message} \n will try to use a automatically port",
                e
            )
            try {
                ServerSocket(0).use { socket ->
                    ret = socket.localPort
                }
            } catch (e: Exception) {
                BuglyLog.e(TAG, "get available port failed ${e.message}")
                Utils.LogError(TAG, "get available port failed", e)
            }
        }
        return ret
    }

    private fun startKernel() {
        BuglyLog.w(TAG, "startKernel() invoked")
        synchronized(this) {
            if (kernelStarted) {
                return
            }
            kernelStarted = true
            val b = Bundle()
            b.putString("cmd", "startKernel")
            val msg = Message()
            msg.data = b
            bootHandler.sendMessage(msg)
        }
    }

    fun bootKernel() {
        Mobile.setHttpServerPort(serverPort.toLong())
        if (Mobile.isHttpServing()) {
            Utils.LogInfo(TAG, "kernel HTTP server is running")
            return
        }
        initAppAssets()
        startHttpServer()
        val appDir = filesDir.absolutePath + "/app"
        // As of API 24 (Nougat) and later 获取用户的设备首选语言
        val locales = resources.configuration.getLocales()
        val locale = locales[0]
        val workspaceBaseDir = getExternalFilesDir(null)?.absolutePath
        val timezone = TimeZone.getDefault().id
        mHandler.post {
            try {
                val lang = determineLanguage(locale)
                BuglyLog.d(
                    TAG,
                    "Mobile.startKernel() -> [${localIPs}] workspaceBaseDir -> $workspaceBaseDir"
                )
                Mobile.startKernel(
                    "android", appDir, workspaceBaseDir, timezone, localIPs, lang,
                    Build.VERSION.RELEASE +
                            "/SDK " + Build.VERSION.SDK_INT +
                            "/WebView " + webViewVer +
                            "/Manufacturer " + Build.MANUFACTURER +
                            "/Brand " + Build.BRAND +
                            "/UA " + userAgent
                )
                BuglyLog.d(TAG, "Mobile.startKernel() ok")
            } catch (e: Exception) {
                // 处理异常
                BuglyLog.e(TAG, "Error in background thread", e)
            }
        }
        val b = Bundle()
        b.putString("cmd", "bootIndex")
        val msg = Message()
        msg.data = b
        bootHandler.sendMessage(msg)
    }

    private fun determineLanguage(locale: Locale): String {
        val lang = locale.language + "_" + locale.country
        return when {
            lang.lowercase(Locale.getDefault()).contains("cn") -> "zh_CN"
            lang.lowercase(Locale.getDefault()).contains("es") -> "es_ES"
            lang.lowercase(Locale.getDefault()).contains("fr") -> "fr_FR"
            else -> "en_US"
        }
    }

    private fun needUnzipAssets(): Boolean {
        BuglyLog.i(TAG, "needUnzipAssets() invoked")
        val appDirFile = File(appDir)
        appDirFile.mkdirs()
        var ret = true
        if (Utils.isDebugPackageAndMode(this)) {
            BuglyLog.i("boot", "always unzip assets in debug mode")
            return ret
        }
        val appVerFile = File(appDir, "VERSION")
        if (appVerFile.exists()) {
            try {
                val ver = FileUtils.readFileToString(appVerFile, StandardCharsets.UTF_8)
                ret = ver != Utils.version
            } catch (e: java.lang.Exception) {
                Utils.LogError("boot", "check version failed", e)
            }
        }
        return ret
    }

    private fun initAppAssets() {
        if (needUnzipAssets()) {
            val appVerFile = File(appDir, "VERSION")
            BuglyLog.i(TAG, "Clearing appearance... 20%")
            try {
                FileUtils.deleteDirectory(File(appDir))
            } catch (e: java.lang.Exception) {
                Utils.LogError(
                    "boot",
                    "delete dir [$appDir] failed, exit application", e
                )
                stopSelf()
                return
            }
            BuglyLog.i(TAG, "Initializing appearance... 60%")
            Utils.unzipAsset(assets, "app.zip", "$appDir/app")
            try {
                FileUtils.writeStringToFile(appVerFile, Utils.version, StandardCharsets.UTF_8)
            } catch (e: java.lang.Exception) {
                Utils.LogError("boot", "write version failed", e)
            }
            BuglyLog.i(TAG, "Booting kernel... 80%")
        }
    }

    /**
     * 请在 activity 中调用
     */
    fun showWifi(activity: Activity) {
        if (!kernelStarted) {
            return
        }
        BuglyLog.d(TAG, "showWifi() invoked")
        val locationPermissionObservable_POST_NOTIFICATIONS =
            Observable.create { emitter: ObservableEmitter<Boolean> ->
                if (ActivityCompat.checkSelfPermission(
                        activity,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    emitter.onNext(true)
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        ActivityCompat.requestPermissions(
                            activity,
                            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                            S.REQUEST_CODE.REQUEST_POST_NOTIFICATIONS
                        )
                    }
                }
            }
        val locationPermissionObservable =
            Observable.create { emitter: ObservableEmitter<Boolean> ->
                if (ActivityCompat.checkSelfPermission(
                        activity,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    emitter.onNext(true)
                } else {
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        S.REQUEST_CODE.REQUEST_LOCATION
                    )
                }
            }
        val locationPermissionObservable_FOREGROUND_SERVICE_LOCATION =
            Observable.create { emitter: ObservableEmitter<Boolean> ->
                if (ActivityCompat.checkSelfPermission(
                        activity,
                        Manifest.permission.FOREGROUND_SERVICE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    emitter.onNext(true)
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        ActivityCompat.requestPermissions(
                            activity,
                            arrayOf(Manifest.permission.FOREGROUND_SERVICE_LOCATION),
                            S.REQUEST_CODE.REQUEST_FOREGROUND_SERVICE_LOCATION
                        )
                    }
                }
            }
        val overlayPermissionObservable = Observable.create { emitter: ObservableEmitter<Boolean> ->
            if (Settings.canDrawOverlays(activity)) {
                emitter.onNext(true)
            } else {
                // 请求悬浮窗权限
                runOnUiThread {
                    val intent = Intent("sc.windom.sillot.intent.permission.Overlay")
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    ActivityCompat.startActivityForResult(
                        activity,
                        intent,
                        S.REQUEST_CODE.REQUEST_OVERLAY,
                        null
                    )
                }
            }
        }
        val disposable = Observable.combineLatest(
            locationPermissionObservable_POST_NOTIFICATIONS,
            locationPermissionObservable,
            locationPermissionObservable_FOREGROUND_SERVICE_LOCATION,
            overlayPermissionObservable
        ) { POST_NOTIFICATIONS_Granted: Boolean, locationGranted: Boolean, FOREGROUND_SERVICE_LOCATION_Granted: Boolean, overlayGranted: Boolean ->
            POST_NOTIFICATIONS_Granted && locationGranted && FOREGROUND_SERVICE_LOCATION_Granted && overlayGranted
        }
            .filter { granted: Boolean? -> granted!! } // 过滤掉未授予权限的情况
            .flatMap { granted: Boolean? ->
                // 启动悬浮窗服务
                val intent = Intent(activity, FloatingWindowService::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                ServiceUtils.startService(intent)
                Observable.just(true)
            }
            .subscribeOn(AndroidSchedulers.mainThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { granted: Boolean? ->
                    BuglyLog.d(
                        TAG,
                        "showWifi() -> Permissions granted and service started."
                    )
                },
                { throwable: Throwable ->
                    BuglyLog.e(
                        TAG,
                        "showWifi() -> Error occurred: " + throwable.message
                    )
                }
            )
    }

    /**
     * 目前看似乎没有必要，同步感知可以及时同步
     */
    private fun scheduleSyncDataWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // 确保在网络连接时运行
            .setRequiresBatteryNotLow(true) // 低电量时不运行
            .build()
        // 可以定义的最短重复间隔是 15 分钟
        val periodicWorkRequest =
            PeriodicWorkRequest.Builder(SyncDataWorker::class.java, 15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .setInitialDelay(10, TimeUnit.MINUTES) // 在加入队列后至少经过 10 分钟后再运行
                .build()

        workManager.enqueueUniquePeriodicWork(
            "scheduleSyncDataWork",
            ExistingPeriodicWorkPolicy.KEEP, // 如果已经存在，则保持
            periodicWorkRequest
        )
    }

    /**
     * 这种方法并不是官方推荐的，因为它可能会导致任务之间的延迟，并且在高频率下可能会对系统资源造成压力。
     */
    private fun CheckHttpServerWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // 创建一个OneTimeWorkRequest
        val oneTimeWorkRequest = OneTimeWorkRequest.Builder(CheckHttpServerWorker::class.java)
            .setConstraints(constraints)
            .build()

        // 将任务加入到WorkManager中，并设置一个UniqueWork名称
        workManager.enqueueUniqueWork(
            checkHttpServerWorkerName,
            ExistingWorkPolicy.REPLACE, // 每次都替换之前的任务
            oneTimeWorkRequest
        )

        // 任务完成后，延迟一段时间再次启动同一个任务
        workManager.getWorkInfoByIdLiveData(oneTimeWorkRequest.id)
            .observeForever { workInfo ->
                if (workInfo != null) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        CheckHttpServerWork()
                    }, 60000)
                }
            }
    }
}

/**
 * 使用协程等待内核 HTTP 服务启动。
 */
@JvmStatic
fun waitForKernelHttpServingWithCoroutines() = runBlocking {
    while (!Mobile.isHttpServing()) {
        delay(20)
    }
}