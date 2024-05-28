package org.b3log.siyuan

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
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
import android.text.format.Formatter
import android.util.Log
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
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.ObservableOnSubscribe
import io.reactivex.rxjava3.core.ObservableSource
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Function
import splitties.systemservices.connectivityManager
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.Collections
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class FloatingWindowService : Service() {
    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var floatingBallView: View? = null
    private var wifiStatusTextView: TextView? = null
    private var closeButton: Button? = null
    private var hideButton: Button? = null
    private var isBallVisible = false
    private var wifiDisposable: Disposable? = null
    private var lanIpTextView: TextView? = null
    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private val wifiStateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            updateWifiInfo()
        }
    }
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
        Log.e("FloatingWindowService", "onBind called")
        updateWifiInfo()
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Log.e("FloatingWindowService", "onCreate called")
        instance = this
        initializeWindow()
        initializeUI()
        createNotificationChannel()
        // 初始化NotificationManager
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 创建NotificationBuilder
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
        notificationBuilder = NotificationCompat.Builder(this, S.SY_NOTIFICATION_CHANNEL_ID)
            .setContentTitle("显示 Wifi 悬浮窗")
            .setContentText(lanIpTextView?.text)
            .setSmallIcon(R.drawable.icon)
            .setContentIntent(pendingIntent)
        // 启动前台服务
        startForeground(1, notificationBuilder.build())
        registerNetworkCallback()
    }

    private fun createNotificationChannel() {
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
        Log.e("FloatingWindowService", "onStartCommand called")
        super.onStartCommand(intent, flags, startId)
        updateWifiInfo()
        return START_STICKY
    }

    override fun onDestroy() {
        Log.e("FloatingWindowService", "onDestroy called")
        super.onDestroy()
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.unregisterNetworkCallback(networkCallback)
        //        unregisterWifiReceiverIfNeeded();
    }

    private fun stopService() {
        Log.e("FloatingWindowService", "stopService called")
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
    private fun initializeWindow() {
        Log.e("FloatingWindowService", "initializeWindow called")
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        floatingView = inflater.inflate(R.layout.floating_window_layout, null)
        // 创建悬浮球视图
        floatingBallView = inflater.inflate(R.layout.floating_ball_layout, null)

        layoutParams.gravity = Gravity.TOP or Gravity.START
        layoutParams.x = 0
        layoutParams.y = 0
        windowManager!!.addView(floatingView, layoutParams)
    }

    private fun initializeUI() {
        Log.e("FloatingWindowService", "initializeUI called")
        wifiStatusTextView = floatingView!!.findViewById(R.id.wifi_status_textview)
        closeButton = floatingView!!.findViewById(R.id.close_button)
        closeButton?.setOnClickListener { v: View? -> stopService() }
        hideButton = floatingView!!.findViewById(R.id.hide_button)
        hideButton?.setOnClickListener { v: View? ->
            // 当点击 Hide 按钮时，切换到悬浮球
            toggleFloatingWindowAndBall()
        }

        // 设置悬浮球的点击事件，以切换回悬浮窗
        floatingBallView?.findViewById<TextView>(R.id.wifi_floating_ball)?.setOnClickListener {
            toggleFloatingWindowAndBall()
        }
        // 设置触摸事件监听器
        floatingView!!.setOnTouchListener(FloatingViewTouchListener())
        // Initialize the LAN IP TextView
        lanIpTextView = floatingView!!.findViewById(R.id.lan_ip_textview)


        // Get and set the LAN IP address
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetwork

        if (networkInfo != null) {
            // Retrieve the IP address using non-deprecated methods
            val executorService: ExecutorService = Executors.newSingleThreadExecutor()
            executorService.execute {
                try {
                    val networkInterfaces = NetworkInterface.getNetworkInterfaces()
                    for (networkInterface in Collections.list(networkInterfaces)) {
                        if (!networkInterface.name.equals("wlan0", ignoreCase = true)) continue

                        val inetAddresses = networkInterface.inetAddresses
                        for (inetAddress in Collections.list(inetAddresses)) {
                            if (inetAddress is Inet4Address && !inetAddress.isLoopbackAddress) {
                                val ipAddress = inetAddress.hostAddress
                                Handler(Looper.getMainLooper()).post {
                                    lanIpTextView?.text = "LAN IP: $ipAddress"
                                }
                                break
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("FloatingWindowService", "Error getting LAN IP address", e)
                }
            }
        } else {
            lanIpTextView?.text = "LAN IP: Not Connected"
        }


    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            updateWifiInfo()
        }

        override fun onLost(network: Network) {
            updateWifiInfo()
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            updateWifiInfo()
        }
    }
    private fun registerNetworkCallback() {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }
    private fun updateIpAddress() {
        val executorService: ExecutorService = Executors.newSingleThreadExecutor()
        executorService.execute {
            try {
                var ipAddress = "Not Connected"
                val networkInterfaces = NetworkInterface.getNetworkInterfaces()
                for (networkInterface in Collections.list(networkInterfaces)) {
                    if (!networkInterface.name.equals("wlan0", ignoreCase = true)) continue

                    val inetAddresses = networkInterface.inetAddresses
                    for (inetAddress in Collections.list(inetAddresses)) {
                        if (inetAddress is Inet4Address && !inetAddress.isLoopbackAddress) {
                            ipAddress = inetAddress.hostAddress
                            break
                        }
                    }
                }
                Handler(Looper.getMainLooper()).post {
                    lanIpTextView?.text = "LAN IP: $ipAddress"
                }
                // IP地址更新后，更新通知内容
                val newNotification = notificationBuilder
                    .setContentText("LAN IP: $ipAddress") // 更新内容文本
                    .build()

                // 使用notify方法更新通知
                notificationManager.notify(1, newNotification)
            } catch (e: Exception) {
                Log.e("FloatingWindowService", "Error getting LAN IP address", e)
            }
        }
    }


    // 切换悬浮窗和悬浮球的函数
    private fun toggleFloatingWindowAndBall() {
        layoutParams.gravity = Gravity.TOP or Gravity.START
        layoutParams.x = 0
        layoutParams.y = 0
        if (isBallVisible) {
            // 如果悬浮球当前可见，则切换回悬浮窗
            windowManager!!.removeView(floatingBallView)
            windowManager!!.addView(floatingView, layoutParams)
        } else {
            // 如果悬浮窗当前可见，则切换到悬浮球
            windowManager!!.removeView(floatingView)
            windowManager!!.addView(floatingBallView, layoutParams)
        }
        isBallVisible = !isBallVisible
    }

    // 注册WiFi接收器
    private fun registerWifiReceiver() {
        Log.e("FloatingWindowService", "registerWifiReceiver called")
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        registerReceiver(wifiStateReceiver, intentFilter)
    }

    // 注册WiFi接收器（如果需要）
    private fun registerWifiReceiverIfNeeded() {
        Log.e("FloatingWindowService", "registerWifiReceiverIfNeeded called")
        val isWifiReceiverRegistered = isReceiverRegistered(WifiStateReceiver::class.java)
        if (!isWifiReceiverRegistered) {
            val intentFilter = IntentFilter()
            intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
            registerReceiver(WifiStateReceiver(), intentFilter)
        }
    }

    // 检查接收器是否已经注册
    private fun isReceiverRegistered(receiverClass: Class<*>): Boolean {
        Log.e("FloatingWindowService", "isReceiverRegistered called")
        val intent = Intent(applicationContext, receiverClass)
        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        return pendingIntent != null
    }

    // 取消注册WiFi接收器（如果需要）
    private fun unregisterWifiReceiverIfNeeded() {
        Log.e("FloatingWindowService", "unregisterWifiReceiverIfNeeded called")
        if (isReceiverRegistered(WifiStateReceiver::class.java)) {
            unregisterReceiver(wifiStateReceiver)
        }
    }

    // 更新WiFi信息 rxJava 版
    private fun updateWifiInfo() {
        Log.e("FloatingWindowService", "updateWifiInfo called")
        updateIpAddress()
        wifiDisposable =
            Observable.create(ObservableOnSubscribe { emitter: ObservableEmitter<String> ->
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
            })
                .repeatWhen(Function<Observable<Any>, ObservableSource<*>> { objectObservable: Observable<Any> ->
                    objectObservable.delay(
                        1000,
                        TimeUnit.MILLISECONDS
                    )
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { permission: String ->
                    if (permission == "PermissionGranted") {
                        registerWifiReceiverIfNeeded()
                        performWifiScan()
                    }
                }
    }

    // 执行WiFi扫描操作
    private fun performWifiScan() {
        Log.e("FloatingWindowService", "performWifiScan called")
        val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        if (wifiManager != null && wifiManager.isWifiEnabled) {
            // 获取当前连接的WiFi信息
            val wifiInfo = wifiManager.connectionInfo
            val ssid = wifiInfo.getSSID()
            val bssid = wifiInfo.bssid
            val signalStrength = wifiInfo.rssi // 获取连接的WiFi信号强度
            // 显示当前连接的WiFi信息以及信号强度
            val wifiDetails = StringBuilder()
            wifiDetails.append("Connected to: ").append(ssid).append("\nBSSID: ").append(bssid)
                .append("\nSignal Strength: ").append(getSignalStrengthLevel(signalStrength))
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
        } else {
            wifiStatusTextView?.text = "WiFi is disabled."
        }
    }

    private fun getSignalStrengthLevel(signalStrength: Int): String {
        val signalStrengthLevel: String
        signalStrengthLevel = if (signalStrength >= -50) {
            "极好"
        } else if (signalStrength >= -60) {
            "很好"
        } else if (signalStrength >= -70) {
            "好"
        } else if (signalStrength >= -80) {
            "一般"
        } else if (signalStrength >= -90) {
            "较差"
        } else {
            "非常差"
        }
        return signalStrengthLevel
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

                            val screenWidth = windowManager!!.defaultDisplay.width
                            val screenHeight = windowManager!!.defaultDisplay.height
                            val maxX = screenWidth - v.width
                            val maxY = screenHeight - v.height

                            newX = max(0, min(maxX, newX))
                            newY = max(0, min(maxY, newY))

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