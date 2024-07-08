/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/7/8 下午12:03
 * updated: 2024/7/8 下午12:03
 */

package org.b3log.siyuan.services

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
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.blankj.utilcode.util.ServiceUtils
import com.koushikdutta.async.AsyncServer
import com.koushikdutta.async.http.server.AsyncHttpServer
import com.koushikdutta.async.http.server.AsyncHttpServerRequest
import com.koushikdutta.async.http.server.AsyncHttpServerResponse
import com.koushikdutta.async.util.Charsets
import com.tencent.bugly.crashreport.BuglyLog
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import mobile.Mobile
import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.DirectoryFileFilter
import org.apache.commons.io.filefilter.TrueFileFilter
import org.b3log.siyuan.Utils
import sc.windom.gibbet.workers.CheckHttpServerWorker
import sc.windom.gibbet.workers.SyncDataWorker
import org.json.JSONArray
import org.json.JSONObject
import sc.windom.sillot.App
import sc.windom.sofill.S
import sc.windom.sofill.U
import sc.windom.sofill.Us.U_Thread.runOnUiThread
import sc.windom.sofill.android.webview.WebPoolsPro
import java.io.File
import java.net.InetAddress
import java.net.ServerSocket
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

class BootService : Service() {
    private val TAG = "services/BootService.kt"
    var server: AsyncHttpServer? = null
    var serverPort = S.DefaultHTTPPort
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
        if(stopKernelOnDestroy) Mobile.stopKernel() else server?.stop()
    }

    private val binder = LocalBinder()

    override fun onBind(intent: Intent): IBinder {
        App.KernelService = this
        webViewKey = intent.getStringExtra(S.INTENT.EXTRA_WEB_VIEW_KEY)
        return binder
    }

    inner class LocalBinder : Binder() {
        fun getService(): BootService = this@BootService
    }

    private fun works() {
        // 初始化 UI 元素
        BuglyLog.d(TAG, "-> 初始化 UI 元素")
        init_webView()

        // 拉起内核
        BuglyLog.d(TAG, "-> 拉起内核")
        startKernel()

        // 周期同步数据
        BuglyLog.d(TAG, "-> 周期同步数据")
        scheduleSyncDataWork()

        // 内核心跳检测
        BuglyLog.d(TAG, "-> 内核心跳检测")
        scheduleCheckHttpServerWork()
    }

    private fun init_webView() {
        // 不使用 activity 的上下文会导致 https://github.com/Hi-Windom/Sillot/issues/814 这里改为获取在 MainActivity 初始化好的 webView
        webView =
            webViewKey?.let { WebPoolsPro.instance?.acquireWebView(it) }
        webView?.setBackgroundColor(Color.parseColor(S.ColorStringHex.bgColor_light))
        val ws = webView?.getSettings()
        userAgent = ws?.userAgentString
        webViewVer = ws?.let { U.checkWebViewVer(it) }
    }

    private val bootHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            val cmd = msg.getData().getString("cmd")
            if ("startKernel" == cmd) {
                bootKernel(true)
            } else {
                BuglyLog.w(TAG, cmd.toString())
            }
        }
    }

    fun isHttpServerRunning(): Boolean {
        return server != null
    }

    fun startHttpServer(isServable: Boolean) {
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
            Utils.LogError("http", "init charset failed", e)
        }
        server = AsyncHttpServer()
        server?.post("/api/walkDir") { request: AsyncHttpServerRequest, response: AsyncHttpServerResponse ->
            try {
                val start = System.currentTimeMillis()
                val requestJSON = request.body.get() as JSONObject
                val dir = requestJSON.optString("dir")
                val data = JSONObject()
                val files = JSONArray()
                FileUtils.listFilesAndDirs(
                    File(dir),
                    TrueFileFilter.INSTANCE,
                    DirectoryFileFilter.DIRECTORY
                ).forEach(
                    Consumer { file: File ->
                        val path = file.absolutePath
                        val info = JSONObject()
                        try {
                            info.put("path", path)
                            info.put("name", file.getName())
                            info.put("size", file.length())
                            info.put("updated", file.lastModified())
                            info.put("isDir", file.isDirectory())
                        } catch (e: Exception) {
                            Utils.LogError("http", "walk dir failed", e)
                        }
                        files.put(info)
                    })
                data.put("files", files)
                val responseJSON = JSONObject().put("code", 0).put("msg", "").put("data", data)
                response.send(responseJSON)
                Utils.LogInfo(
                    "http",
                    "walk dir [" + dir + "] in [" + (System.currentTimeMillis() - start) + "] ms"
                )
            } catch (e: Exception) {
                Utils.LogError("http", "walk dir failed", e)
                try {
                    response.send(JSONObject().put("code", -1).put("msg", e.message))
                } catch (e2: Exception) {
                    Utils.LogError("http", "walk dir failed", e2)
                }
            }
        }
        serverPort = getAvailablePort()
        val s = AsyncServer.getDefault()
        if (isServable) {
            // 绑定所有网卡以便通过局域网IP访问
            s.listen(null, serverPort, server!!.listenCallback)
            BuglyLog.w(
                TAG,
                "startHttpServer() -> HTTP server is listening on all interfaces, port [$serverPort]"
            )
        } else {
            // 绑定 ipv6 回环地址 [::1] 以防止被远程访问
            s.listen(InetAddress.getLoopbackAddress(), serverPort, server!!.listenCallback)
            BuglyLog.w(
                TAG,
                "startHttpServer() -> HTTP server is listening on loopback address, port [$serverPort]"
            )
        }
        BuglyLog.w(TAG, "startHttpServer() -> HTTP server is listening on port [$serverPort]")
        Utils.LogInfo("http", "HTTP server is listening on port [$serverPort]")
    }

    private fun getAvailablePort(): Int {
        var ret = S.DefaultHTTPPort
        try {
            ServerSocket(0).use { socket ->
                ret = socket.localPort
            }
        } catch (e: Exception) {
            Utils.LogError("http", "get available port failed", e)
        }
        return ret
    }

    private fun startKernel() {
        BuglyLog.w(TAG, "startKernel() invoked")
        if (kernelStarted) {
            return
        }
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

    fun bootKernel(isServable: Boolean) {
        Mobile.setHttpServerPort(serverPort.toLong())
        if (Mobile.isHttpServing()) {
            Utils.LogInfo(TAG, "kernel HTTP server is running")
            return
        }
        initAppAssets()
        startHttpServer(isServable)
        val appDir = filesDir.absolutePath + "/app"
        // As of API 24 (Nougat) and later 获取用户的设备首选语言
        val locales = resources.configuration.getLocales()
        val locale = locales[0]
        val workspaceBaseDir = getExternalFilesDir(null)?.absolutePath
        val timezone = TimeZone.getDefault().id
        mHandler.post {
            try {
                val localIPs = Utils.getIPAddressList()
                val lang = determineLanguage(locale)
                BuglyLog.d(TAG, "Mobile.startKernel() -> workspaceBaseDir -> $workspaceBaseDir")
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
        ) { POST_NOTIFICATIONS_Granted: Boolean, locationGranted: Boolean,FOREGROUND_SERVICE_LOCATION_Granted: Boolean, overlayGranted: Boolean ->
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

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "SyncDataWork",
            ExistingPeriodicWorkPolicy.KEEP, // 如果已经存在，则保持
            periodicWorkRequest
        )
    }

    /**
     * 这种方法并不是官方推荐的，因为它可能会导致任务之间的延迟，并且在高频率下可能会对系统资源造成压力。
     * 实际上如果不显示前台WIFI悬浮窗的话这个很快就会失效。
     */
    private fun scheduleCheckHttpServerWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // 创建一个OneTimeWorkRequest
        val oneTimeWorkRequest = OneTimeWorkRequest.Builder(CheckHttpServerWorker::class.java)
            .setConstraints(constraints)
            .build()

        // 将任务加入到WorkManager中，并设置一个UniqueWork名称
        WorkManager.getInstance(this).enqueueUniqueWork(
            "CheckHttpServerWork",
            ExistingWorkPolicy.REPLACE, // 每次都替换之前的任务
            oneTimeWorkRequest
        )

        // 任务完成后，延迟10秒再次启动同一个任务
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(oneTimeWorkRequest.id)
            .observeForever { workInfo ->
                if (workInfo != null && workInfo.state == WorkInfo.State.SUCCEEDED) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        scheduleCheckHttpServerWork()
                    }, 10000)
                }
            }
    }
}