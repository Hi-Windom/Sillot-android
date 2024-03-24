package org.b3log.siyuan;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;

public class FloatingWindowService extends Service {
    private static FloatingWindowService instance;
    private WindowManager windowManager;
    private View floatingView;
    private TextView wifiStatusTextView;
    private Button closeButton;
    private Disposable wifiDisposable;
    private BroadcastReceiver wifiStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateWifiInfo();
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        Log.e("FloatingWindowService", "onBind called");
        updateWifiInfo();
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("FloatingWindowService", "onCreate called");
        instance = this;
        initializeWindow();
        initializeUI();

        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        // 创建PendingIntent时，添加FLAG_IMMUTABLE标志
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, Ss.SY_NOTIFICATION_CHANNEL_ID)
                .setContentTitle("FloatingWindowService")
                .setContentText("显示 Wifi 悬浮窗")
                .setSmallIcon(R.drawable.icon)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);


    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    Ss.FloatingWindowService_NOTIFICATION_CHANNEL_ID,
                    "显示悬浮窗",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    public static FloatingWindowService getInstance() {
        return instance;
    }

    // 启动服务
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("FloatingWindowService", "onStartCommand called");
        super.onStartCommand(intent, flags, startId);
        updateWifiInfo();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.e("FloatingWindowService", "onDestroy called");
        super.onDestroy();
//        unregisterWifiReceiverIfNeeded();
    }

    private void stopService() {
        Log.e("FloatingWindowService", "stopService called");
        instance = null;
        unregisterWifiReceiverIfNeeded();
        // ... 清理逻辑 ...
        // 在销毁 Service 时移除悬浮窗口和广播接收器
        if (floatingView != null) {
            windowManager.removeView(floatingView);
            floatingView = null; // 避免悬浮窗口对象持有 Service 的引用
        }
        stopSelf(); // 停止服务
    }


    // 初始化窗口
    private void initializeWindow() {
        Log.e("FloatingWindowService", "initializeWindow called");
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        floatingView = inflater.inflate(R.layout.floating_window_layout, null);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS // 允许窗口移出屏幕边界，实际上被自定义的触摸事件监听器阻止了，不删也不影响，万一哪天用上了
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                android.graphics.PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 0;
        windowManager.addView(floatingView, params);
    }

    private void initializeUI() {
        Log.e("FloatingWindowService", "initializeUI called");
        wifiStatusTextView = floatingView.findViewById(R.id.wifi_status_textview);
        closeButton = floatingView.findViewById(R.id.close_button);

        closeButton.setOnClickListener(v -> {
            stopService();
        });

        // 设置触摸事件监听器
        FloatingViewTouchListener touchListener = new FloatingViewTouchListener(windowManager, floatingView);
        floatingView.setOnTouchListener(touchListener);
    }


    // 注册WiFi接收器
    private void registerWifiReceiver() {
        Log.e("FloatingWindowService", "registerWifiReceiver called");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(wifiStateReceiver, intentFilter);
    }

    // 注册WiFi接收器（如果需要）
    private void registerWifiReceiverIfNeeded() {
        Log.e("FloatingWindowService", "registerWifiReceiverIfNeeded called");
        boolean isWifiReceiverRegistered = isReceiverRegistered(WifiStateReceiver.class);
        if (!isWifiReceiverRegistered) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            registerReceiver(new WifiStateReceiver(), intentFilter);
        }
    }

    // 检查接收器是否已经注册
    private boolean isReceiverRegistered(Class<?> receiverClass) {
        Log.e("FloatingWindowService", "isReceiverRegistered called");
        Intent intent = new Intent(getApplicationContext(), receiverClass);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
        return pendingIntent != null;
    }

    // 取消注册WiFi接收器（如果需要）
    private void unregisterWifiReceiverIfNeeded() {
        Log.e("FloatingWindowService", "unregisterWifiReceiverIfNeeded called");
        if (isReceiverRegistered(WifiStateReceiver.class)) {
            unregisterReceiver(wifiStateReceiver);
        }
    }


    // 更新WiFi信息 rxJava 版
    private void updateWifiInfo() {
        Log.e("FloatingWindowService", "updateWifiInfo called");
        wifiDisposable = Observable.create((ObservableOnSubscribe<String>) emitter -> {
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        emitter.onNext("PermissionGranted");
                    } else {
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                emitter.onNext("PermissionGranted");
                            } else {
                                emitter.onNext("PermissionDenied");
                            }
                        }, 1000);
                    }
                })
                .repeatWhen(objectObservable -> objectObservable.delay(1000, TimeUnit.MILLISECONDS))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(permission -> {
                    if (permission.equals("PermissionGranted")) {
                        registerWifiReceiverIfNeeded();
                        performWifiScan();
                    }
                });
    }

    // 执行WiFi扫描操作
    private void performWifiScan() {
        Log.e("FloatingWindowService", "performWifiScan called");
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null && wifiManager.isWifiEnabled()) {
            // 获取当前连接的WiFi信息
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String ssid = wifiInfo.getSSID();
            String bssid = wifiInfo.getBSSID();
            int signalStrength = wifiInfo.getRssi(); // 获取连接的WiFi信号强度
            // 显示当前连接的WiFi信息以及信号强度
            StringBuilder wifiDetails = new StringBuilder();
            wifiDetails.append("Connected to: ").append(ssid).append("\nBSSID: ").append(bssid)
                    .append("\nSignal Strength: ").append(getSignalStrengthLevel(signalStrength)).append("\n");

            // 扫描附近的WiFi网络
            @SuppressLint("MissingPermission") List<ScanResult> scanResults = wifiManager.getScanResults();
            if (scanResults != null && !scanResults.isEmpty()) {
                wifiDetails.append("\nAvailable WiFi:\n");
                for (ScanResult result : scanResults) {
                    String resultSSID = result.SSID;
                    if (resultSSID != null && !resultSSID.isEmpty()) { // 检查SSID是否为空
                        int resultSignalStrength = result.level; // 信号强度
                        String signalStrengthLevel = getSignalStrengthLevel(resultSignalStrength);

                        wifiDetails.append("SSID: ").append(resultSSID)
                                .append("\t（信号").append(signalStrengthLevel).append("）\n");
                    }
                }
            } else {
                wifiDetails.append("No WiFi networks found.");
            }

            wifiStatusTextView.setText(wifiDetails.toString());
        } else {
            wifiStatusTextView.setText("WiFi is disabled.");
        }
    }

    private String getSignalStrengthLevel(int signalStrength) {
        String signalStrengthLevel;
        if (signalStrength >= -50) {
            signalStrengthLevel = "极好";
        } else if (signalStrength >= -60) {
            signalStrengthLevel = "很好";
        } else if (signalStrength >= -70) {
            signalStrengthLevel = "好";
        } else if (signalStrength >= -80) {
            signalStrengthLevel = "一般";
        } else if (signalStrength >= -90) {
            signalStrengthLevel = "较差";
        } else {
            signalStrengthLevel = "非常差";
        }
        return signalStrengthLevel;
    }
}