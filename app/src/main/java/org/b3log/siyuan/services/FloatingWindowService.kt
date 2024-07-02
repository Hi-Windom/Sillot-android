package org.b3log.siyuan.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.kongzue.dialogx.dialogs.PopNotification
import com.tencent.bugly.crashreport.BuglyLog
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.b3log.siyuan.R
import org.b3log.siyuan.WifiStateReceiver
import sc.windom.sofill.S
import sc.windom.sofill.U
import sc.windom.sofill.Us.U_Permission.hasPermission_FOREGROUND_SERVICE_LOCATION
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.Collections
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * 必须 hasPermission_FOREGROUND_SERVICE_LOCATION
 */
class FloatingWindowService : Service() {
    val TAG = "services/FloatingWindowService.kt"
    private val ACTION_TOGGLE_WINDOW = "ACTION_TOGGLE_WINDOW"
    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var floatingBallView: View? = null
    private var wifiStatusTextView: TextView? = null
    private var closeButton: Button? = null
    private var hideButton: Button? = null
    private var kill_kernel_button: Button? = null
    private var isBallVisible = false
    private var wifiDisposable: Disposable? = null
    private var lanIpTextView: TextView? = null
    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var wifiStateReceiver: WifiStateReceiver
    private val layoutParams = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS // 允许窗口移出屏幕边界，实际上被自定义的触摸事件监听器阻止了，不删也不影响，万一哪天用上了
                or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
        PixelFormat.TRANSLUCENT
    )

    override fun onBind(intent: Intent): IBinder? {
        BuglyLog.i(TAG, "onBind called")
        return null
    }

    override fun onCreate() {
        super.onCreate()
        BuglyLog.i(TAG, "onCreate called")
        works()
    }

    private fun createNotificationChannel() {
        BuglyLog.i(TAG, "createNotificationChannel called")
        val serviceChannel = NotificationChannel(
            S.FloatingWindowService_NOTIFICATION_CHANNEL_ID,
            "显示悬浮窗",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(
            NotificationManager::class.java
        )
        manager.createNotificationChannel(serviceChannel)
    }

    // 启动服务
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        BuglyLog.i(TAG, "onStartCommand() -> intent: $intent")
        if (intent.action.isNullOrEmpty()) {
            works()
        }
        if (intent.action == ACTION_TOGGLE_WINDOW) {
            floatingView?.let { it.visibility = View.VISIBLE }
        }
        super.onStartCommand(intent, flags, startId)
        return START_REDELIVER_INTENT // 如果 Service 被杀死，系统会尝试重新创建 Service，并且会重新传递最后一个 Intent 给 Service 的 onStartCommand() 方法。
    }

    override fun onDestroy() {
        BuglyLog.i(TAG, "onDestroy called")
        super.onDestroy()
        unregisterNetworkCallback()
//        val connectivityManager =
//            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        connectivityManager.unregisterNetworkCallback(networkCallback)
        //        unregisterWifiReceiverIfNeeded();
    }

    @SuppressLint("LaunchActivityFromNotification")
    private fun works() {
        BuglyLog.i(TAG, "works called")
        instance = this
        createNotificationChannel()
        // 初始化NotificationManager
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 创建NotificationBuilder
        val notificationIntent = Intent(this, FloatingWindowService::class.java)
        notificationIntent.action = ACTION_TOGGLE_WINDOW
        val pendingIntent =
            PendingIntent.getService(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
        notificationBuilder = NotificationCompat.Builder(this, S.FloatingWindowService_NOTIFICATION_CHANNEL_ID)
            .setContentTitle("初始化通知，看到这个说明有问题")
            .setContentText(lanIpTextView?.text)
            .setSmallIcon(R.drawable.icon)
            .setOngoing(true) //不能清除通知
            .setPriority(NotificationManager.IMPORTANCE_HIGH) // 通知类别，适用“勿扰模式”
            .setCategory(NotificationCompat.CATEGORY_MESSAGE) // 通知类别，"勿扰模式"时系统会决定要不要显示你的通知
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // 屏幕可见性，适用“锁屏状态”
            .setSilent(true) // 静默通知 https://github.com/Hi-Windom/Sillot-android/issues/80
            .setContentIntent(pendingIntent)
        // 启动前台服务
        startService(notificationIntent)
        if (hasPermission_FOREGROUND_SERVICE_LOCATION(applicationContext)) {
            // 必须首先始终调用 startService(Intent) 来告诉系统应该让服务持续运行，然后使用此方法告诉它要更努力地保持运行。
            startForeground(1, notificationBuilder.build())
        }
        initializeWindow()
        initializeUI()
        registerNetworkCallback()
    }

    private fun stopService() {
        BuglyLog.i(TAG, "stopService called")
        instance = null
        unregisterWifiReceiverIfNeeded()
        // ... 清理逻辑 ...
        // 在销毁 Service 时移除悬浮窗口和广播接收器
        if (floatingView != null) {
            windowManager!!.removeView(floatingView)
            floatingView = null // 避免悬浮窗口对象持有 Service 的引用
        }
        stopSelf() // 停止服务
    }

    // 初始化窗口
    @SuppressLint("InflateParams")
    private fun initializeWindow() {
        BuglyLog.i(TAG, "initializeWindow called")
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        // 创建悬浮球视图
        floatingBallView = inflater.inflate(R.layout.floating_ball_layout, null)

        layoutParams.gravity = Gravity.TOP or Gravity.START
        layoutParams.x = 0
        layoutParams.y = 0
        if (floatingView == null) {
            floatingView = inflater.inflate(R.layout.floating_window_layout, null)
            windowManager?.addView(floatingView, layoutParams)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initializeUI() {
        BuglyLog.i(TAG, "initializeUI called")
        wifiStatusTextView = floatingView?.findViewById(R.id.wifi_status_textview)
        closeButton = floatingView?.findViewById(R.id.close_button)
        closeButton?.setOnClickListener { floatingView?.let { it.visibility = View.GONE } }
        hideButton = floatingView?.findViewById(R.id.hide_button)
        hideButton?.setOnClickListener {
            // 当点击 Hide 按钮时，切换到悬浮球
            toggleFloatingWindowAndBall()
        }

        // 设置悬浮球的点击事件，以切换回悬浮窗
        floatingBallView?.findViewById<TextView>(R.id.wifi_floating_ball)?.setOnClickListener {
            toggleFloatingWindowAndBall()
        }
        // 设置触摸事件监听器
        floatingView?.setOnTouchListener(FloatingViewTouchListener())
        // Initialize the LAN IP TextView
        lanIpTextView = floatingView?.findViewById(R.id.lan_ip_textview)
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            updateWifiInfo()
        }

        override fun onLost(network: Network) {
            updateWifiInfo()
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            updateWifiInfo()
        }
    }

    private var isNetworkCallbackRegistered = false

    private fun registerNetworkCallback() {
        if (isNetworkCallbackRegistered) {
            // NetworkCallback 已经注册，无需再次注册
            return
        }

        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        isNetworkCallbackRegistered = true
    }

    // 在适当的生命周期函数中注销NetworkCallback，例如在Activity的onDestroy中
    private fun unregisterNetworkCallback() {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.unregisterNetworkCallback(networkCallback)
        isNetworkCallbackRegistered = false
    }

    @SuppressLint("SetTextI18n")
    private fun updateIpAddress(notificationText: String? = "Wifi : 没有诶") {
        BuglyLog.i(TAG, "updateIpAddress called -> notificationText: $notificationText")
        val executorService: ExecutorService = Executors.newSingleThreadExecutor()
        executorService.execute {
            try {
                var ipAddress = "0.0.0.0"
                val networkInterfaces = NetworkInterface.getNetworkInterfaces()
                for (networkInterface in Collections.list(networkInterfaces)) {
                    if (!networkInterface.name.equals("wlan0", ignoreCase = true)) continue

                    val inetAddresses = networkInterface.inetAddresses
                    for (inetAddress in Collections.list(inetAddresses)) {
                        if (inetAddress is Inet4Address && !inetAddress.isLoopbackAddress) {
                            ipAddress = inetAddress.hostAddress as String
                            break
                        }
                    }
                }
                Handler(Looper.getMainLooper()).post {
                    lanIpTextView?.text = "IP: $ipAddress"
                    // IP地址更新后，更新通知内容
                    val newNotification = notificationBuilder
                        .setContentTitle("IP: $ipAddress")
                        .setContentText(notificationText) // 更新内容文本
                        .build()

                    // 使用notify方法更新通知
                    notificationManager.notify(1, newNotification)
                }
            } catch (e: Exception) {
                PopNotification.show(TAG, "Error getting LAN IP address : $e")
            }
        }
    }

    // 切换悬浮窗和悬浮球的函数
    private fun toggleFloatingWindowAndBall() {
        BuglyLog.i(TAG, "toggleFloatingWindowAndBall called")
        layoutParams.gravity = Gravity.TOP or Gravity.START
        layoutParams.x = 0
        layoutParams.y = 0
        if (isBallVisible) {
            // 如果悬浮球当前可见，则切换回悬浮窗
            floatingBallView?.let { windowManager?.removeView(it) }
            windowManager?.addView(floatingView, layoutParams)
        } else {
            // 如果悬浮窗当前可见，则切换到悬浮球
            floatingView?.let { windowManager?.removeView(it) }
            windowManager?.addView(floatingBallView, layoutParams)
        }
        isBallVisible = !isBallVisible
    }

    // 注册WiFi接收器
    private fun registerWifiReceiver() {
        BuglyLog.i(TAG, "registerWifiReceiver called")
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        registerReceiver(wifiStateReceiver, intentFilter)
    }

    // 注册WiFi接收器（如果需要）
    private fun registerWifiReceiverIfNeeded() {
        BuglyLog.i(TAG, "registerWifiReceiverIfNeeded called")
        val isWifiReceiverRegistered = isReceiverRegistered(WifiStateReceiver::class.java)
        if (!isWifiReceiverRegistered) {
            val intentFilter = IntentFilter()
            intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
            wifiStateReceiver = WifiStateReceiver()
            registerReceiver(wifiStateReceiver, intentFilter)
        }
    }

    // 检查接收器是否已经注册
    private fun isReceiverRegistered(receiverClass: Class<*>): Boolean {
        BuglyLog.i(TAG, "isReceiverRegistered called")
        val intent = Intent(this, receiverClass) // 确保使用与注册时相同的Context
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        return pendingIntent != null
    }

    // 取消注册WiFi接收器（如果需要）
    private fun unregisterWifiReceiverIfNeeded() {
        BuglyLog.i(TAG, "unregisterWifiReceiverIfNeeded called")
        if (::wifiStateReceiver.isInitialized && isReceiverRegistered(WifiStateReceiver::class.java)) {
            unregisterReceiver(wifiStateReceiver)
        }
    }

    // 更新WiFi信息 rxJava 版
    private fun updateWifiInfo_old() {
        BuglyLog.i(TAG, "updateWifiInfo called")
        wifiDisposable =
            Observable.create { emitter: ObservableEmitter<String> ->
                if (ActivityCompat.checkSelfPermission(
                        applicationContext, Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    emitter.onNext("PermissionGranted")
                } else {
                    Handler(Looper.getMainLooper()).postDelayed({
                        if (ActivityCompat.checkSelfPermission(
                                applicationContext, Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            emitter.onNext("PermissionGranted")
                        } else {
                            emitter.onNext("PermissionDenied")
                        }
                    }, 1000)
                }
            }
                .repeatWhen { objectObservable: Observable<Any> ->
                    objectObservable.delay(
                        500,
                        TimeUnit.MILLISECONDS
                    )
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { permission: String ->
                    if (permission == "PermissionGranted") {
                        registerWifiReceiverIfNeeded()
                        performWifiScan()
                    }
                }
    }

    private fun updateWifiInfo() {
        BuglyLog.i(TAG, "updateWifiInfo called")

        // 创建 Observable，检查定位权限
        val permissionCheckObservable = Observable.create { emitter ->
            if (ActivityCompat.checkSelfPermission(
                    applicationContext, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                emitter.onNext("PermissionGranted")
            } else {
                emitter.onNext("PermissionDenied")
            }
        }

        // 订阅 Observable，处理权限结果
        wifiDisposable = permissionCheckObservable
            .subscribeOn(Schedulers.io()) // 在 IO 线程检查权限
            .observeOn(AndroidSchedulers.mainThread()) // 在主线程处理结果
            .doOnTerminate {
                // 在 Observable 完成时执行清理操作
                // 例如，取消注册的接收器或取消网络请求
                BuglyLog.d(TAG, "Permission check Observable terminated")
            }
            .subscribe { permission ->
                when (permission) {
                    "PermissionGranted" -> {
                        BuglyLog.d(TAG, "Location permission granted, registering receiver and performing wifi scan")
                        registerWifiReceiverIfNeeded()
                        performWifiScan()
                    }
                    "PermissionDenied" -> {
                        // 处理权限被拒绝的情况，例如显示一个对话框或 toast
                        BuglyLog.i(TAG, "Location permission denied")
                    }
                }
            }
    }




    // 执行WiFi扫描操作
    @SuppressLint("SetTextI18n")
    private fun performWifiScan() {
        BuglyLog.i(TAG, "performWifiScan called")
        val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        if (wifiManager.isWifiEnabled) {
            // 获取当前连接的WiFi信息
            val wifiInfo = wifiManager.connectionInfo
            val ssid = wifiInfo.getSSID()
            val bssid = wifiInfo.bssid
            val signalStrength = wifiInfo.rssi // 获取连接的WiFi信号强度
            val readableSS = U.getSignalStrengthLevel(signalStrength)
            // 显示当前连接的WiFi信息以及信号强度
            val wifiDetails = StringBuilder()
            wifiDetails.append("Connected to: ").append(ssid).append("\nBSSID: ").append(bssid)
                .append("\nSignal Strength: ").append(readableSS)
                .append("\n")

            // 扫描附近的WiFi网络
//            @SuppressLint("MissingPermission") val scanResults = wifiManager.scanResults
//            if (scanResults != null && !scanResults.isEmpty()) {
//                wifiDetails.append("\nAvailable WiFi:\n")
//                for (result in scanResults) {
//                    val resultSSID = result.SSID
//                    if (resultSSID != null && resultSSID.isNotEmpty()) { // 检查SSID是否为空
//                        val resultSignalStrength = result.level // 信号强度
//                        val signalStrengthLevel = getSignalStrengthLevel(resultSignalStrength)
//                        wifiDetails.append("SSID: ").append(resultSSID)
//                            .append("\t（信号").append(signalStrengthLevel).append("）\n")
//                    }
//                }
//            } else {
//                wifiDetails.append("No WiFi networks found.")
//            }
            wifiStatusTextView?.text = wifiDetails.toString()
            updateIpAddress("${ssid} 信号${readableSS}")
        } else {
            wifiStatusTextView?.text = "WiFi is disabled."
            updateIpAddress("💔 WiFi is disabled.")
        }
    }



    companion object {
        @SuppressLint("StaticFieldLeak")
        var instance: FloatingWindowService? = null
            private set
    }

    inner class FloatingViewTouchListener() : View.OnTouchListener {

        private var initialX = 0
        private var initialY = 0
        private var initialTouchX = 0f
        private var initialTouchY = 0f

        // 最小滑动距离，用来判断是点击还是拖拽
        private val touchSlop =
            floatingView?.let { ViewConfiguration.get(it.context).scaledTouchSlop }

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            val layoutParams = v.layoutParams as WindowManager.LayoutParams

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = layoutParams.x
                    initialY = layoutParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    return true
                }

                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.rawX - initialTouchX
                    val deltaY = event.rawY - initialTouchY

                    if (windowManager != null && touchSlop != null) {

                        // 如果移动距离大于最小滑动距离，则认为是拖拽事件
                        if (abs(deltaX) > touchSlop || abs(deltaY) > touchSlop) {
                            var newX = initialX + deltaX.toInt()
                            var newY = initialY + deltaY.toInt()

                            val screenWidth = windowManager?.defaultDisplay?.width
                            val screenHeight = windowManager?.defaultDisplay?.height
                            val maxX = screenWidth?.minus(v.width)
                            val maxY = screenHeight?.minus(v.height)

                            newX = max(0, maxX?.let { min(it, newX) } ?: 0)
                            newY = max(0, maxY?.let { min(it, newY) } ?: 0)

                            layoutParams.x = newX
                            layoutParams.y = newY
                            windowManager!!.updateViewLayout(v, layoutParams)
                        }
                    }
                    return true
                }

                MotionEvent.ACTION_UP -> {
                    return true
                }

                MotionEvent.ACTION_CANCEL -> {
                    return true
                }
            }
            return false
        }
    }

}