package org.b3log.siyuan.services

import android.Manifest
import android.app.Activity
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.provider.Settings
import android.util.Log
import android.webkit.ValueCallback
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
import com.blankj.utilcode.util.ThreadUtils.runOnUiThread
import com.koushikdutta.async.AsyncServer
import com.koushikdutta.async.http.server.AsyncHttpServer
import com.koushikdutta.async.http.server.AsyncHttpServerRequest
import com.koushikdutta.async.http.server.AsyncHttpServerResponse
import com.koushikdutta.async.util.Charsets
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import mobile.Mobile
import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.DirectoryFileFilter
import org.apache.commons.io.filefilter.TrueFileFilter
import org.b3log.siyuan.Utils
import org.b3log.siyuan.workers.CheckHttpServerWorker
import org.b3log.siyuan.workers.SyncDataWorker
import org.json.JSONArray
import org.json.JSONObject
import sc.windom.sofill.S
import sc.windom.sofill.U
import sc.windom.sofill.android.webview.WebViewPool
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
    private lateinit var mHandlerThread: HandlerThread
    private lateinit var mHandler: Handler
    private val uploadMessage: ValueCallback<Array<Uri>>? = null
    override fun onCreate() {
        super.onCreate()
        mHandlerThread = HandlerThread("MyHandlerThread")
        mHandlerThread.start()
        mHandler = Handler(mHandlerThread.looper)
        works()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i(TAG, "onStartCommand() -> intent: $intent")
        works()
        return START_REDELIVER_INTENT // 如果 Service 被杀死，系统会尝试重新创建 Service，并且会重新传递最后一个 Intent 给 Service 的 onStartCommand() 方法。
    }

    override fun onDestroy() {
        super.onDestroy()
        server?.stop()
    }

    private val binder = LocalBinder()

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    inner class LocalBinder : Binder() {
        fun getService(): BootService = this@BootService
    }

    private fun works() {
        // 初始化 UI 元素
        Log.w(TAG, "onStart() -> initUIElements() invoked")
        init_webView()

        // 拉起内核
        Log.w(TAG, "onStart() -> startKernel() invoked")
        startKernel()

        // 周期同步数据
        scheduleSyncDataWork()

        // 内核心跳检测
        scheduleCheckHttpServerWork()
    }

    private fun init_webView() {
        webView = WebViewPool.getInstance().getWebView(this) // 由于不使用 activity 的上下文导致 https://github.com/Hi-Windom/Sillot/issues/814 暂时没有解决方法
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
                Log.w(TAG, cmd.toString())
            }
        }
    }

    fun isHttpServerRunning(): Boolean {
        return server != null
    }

    fun startHttpServer(isServable: Boolean) {
        if (isHttpServerRunning()) {
            server?.stop()
            Log.w(TAG, "startHttpServer() stop exist server")
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
            Log.w(
                TAG,
                "startHttpServer() -> HTTP server is listening on all interfaces, port [$serverPort]"
            )
        } else {
            // 绑定 ipv6 回环地址 [::1] 以防止被远程访问
            s.listen(InetAddress.getLoopbackAddress(), serverPort, server!!.listenCallback)
            Log.w(
                TAG,
                "startHttpServer() -> HTTP server is listening on loopback address, port [$serverPort]"
            )
        }
        Log.w(TAG, "startHttpServer() -> HTTP server is listening on port [$serverPort]")
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
        Log.w(TAG, "startKernel() invoked")
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
                Log.d(TAG, "Mobile.startKernel() -> workspaceBaseDir -> $workspaceBaseDir")
                Mobile.startKernel(
                    "android", appDir, workspaceBaseDir, timezone, localIPs, lang,
                    Build.VERSION.RELEASE +
                            "/SDK " + Build.VERSION.SDK_INT +
                            "/WebView " + webViewVer +
                            "/Manufacturer " + Build.MANUFACTURER +
                            "/Brand " + Build.BRAND +
                            "/UA " + userAgent
                )
                Log.d(TAG, "Mobile.startKernel() ok")
            } catch (e: Exception) {
                // 处理异常
                Log.e(TAG, "Error in background thread", e)
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
        Log.i(TAG, "needUnzipAssets() invoked")
        val dataDir = filesDir.absolutePath
        val appDir = "$dataDir/app"
        val appDirFile = File(appDir)
        appDirFile.mkdirs()
        var ret = true
        if (Utils.isDebugPackageAndMode(this)) {
            Log.i("boot", "always unzip assets in debug mode")
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
            val dataDir = filesDir.absolutePath
            val appDir = "$dataDir/app"
            val appVerFile = File(appDir, "VERSION")
            Log.i(TAG, "Clearing appearance... 20%")
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
            Log.i(TAG, "Initializing appearance... 60%")
            Utils.unzipAsset(assets, "app.zip", "$appDir/app")
            try {
                FileUtils.writeStringToFile(appVerFile, Utils.version, StandardCharsets.UTF_8)
            } catch (e: java.lang.Exception) {
                Utils.LogError("boot", "write version failed", e)
            }
            Log.i(TAG, "Booting kernel... 80%")
        }
    }

    /**
     * 请在 activity 中调用
     */
    fun showWifi(activity: Activity) {
        if (!kernelStarted) {
            return
        }
        Log.d(TAG, "showWifi() invoked")
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
                        S.REQUEST_LOCATION
                    )
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
                        S.REQUEST_OVERLAY,
                        null
                    )
                }
            }
        }
        val disposable = Observable.combineLatest(
            locationPermissionObservable,
            overlayPermissionObservable
        ) { locationGranted: Boolean, overlayGranted: Boolean ->
            locationGranted && overlayGranted
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
                    Log.d(
                        TAG,
                        "showWifi() -> Permissions granted and service started."
                    )
                },
                { throwable: Throwable ->
                    Log.e(
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