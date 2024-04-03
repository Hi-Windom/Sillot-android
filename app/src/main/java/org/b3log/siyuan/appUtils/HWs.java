package org.b3log.siyuan.appUtils;

import static android.content.Context.TELEPHONY_SERVICE;

import static org.b3log.siyuan.BuildConfig.DEBUG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.KeyguardManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import androidx.core.app.ActivityCompat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class HWs {
    private static final String TAG = "HWs";
    private static final Object object = new Object();
    private static HWs HWs;

    public static HWs getInstance() {
        if (HWs == null) {
            synchronized (object) {
                if (HWs == null) {
                    HWs = new HWs();
                }
            }
        }
        return HWs;
    }

    /*
     * 获取MEID
     * 注：调用前需要获取READ_PHONE_STATE权限
     * */
    @SuppressLint({"MissingPermission", "HardwareIds"})
    public String getMEID(Context context) {
        String meid = "";
        TelephonyManager mTelephonyMgr = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
        if (null != mTelephonyMgr) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                meid = mTelephonyMgr.getMeid();
                Log.i(TAG, "Android版本大于o-26-优化后的获取---meid:" + meid);
            } else {
                meid = mTelephonyMgr.getDeviceId();
            }
        }

        Log.i(TAG, "优化后的获取---meid:" + meid);

        return meid;
    }

    /**
     * 获取IMEI
     * 注：调用前需要获取READ_PHONE_STATE权限
     *
     * @param context Context
     * @param index   取第几个imei(0,1)
     * @return
     */
    @SuppressLint("MissingPermission")
    public String getIMEI(Context context, int index) {
        String imei = "";
        TelephonyManager mTelephonyMgr = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
        if (null != mTelephonyMgr) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                imei = mTelephonyMgr.getImei(index);
                Log.i(TAG, "Android版本大于o-26-优化后的获取---imei-:" + imei);
            } else {
                try {
                    imei = getDoubleImei(mTelephonyMgr, "getDeviceIdGemini", index);
                } catch (Exception e) {
                    try {
                        imei = getDoubleImei(mTelephonyMgr, "getDeviceId", index);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    Log.e(TAG, "get device id fail: " + e.toString());
                }
            }
        }

        Log.i(TAG, "优化后的获取---imei：" + imei);
        return imei;
    }

    /**
     * 获取双卡手机的imei
     */
    private String getDoubleImei(TelephonyManager telephony, String predictedMethodName, int slotID) throws Exception {
        String inumeric = null;

        Class<?> telephonyClass = Class.forName(telephony.getClass().getName());
        Class<?>[] parameter = new Class[1];
        parameter[0] = int.class;
        Method getSimID = telephonyClass.getMethod(predictedMethodName, parameter);
        Object[] obParameter = new Object[1];
        obParameter[0] = slotID;
        Object ob_phone = getSimID.invoke(telephony, obParameter);
        if (ob_phone != null) {
            inumeric = ob_phone.toString();
        }
        return inumeric;
    }

    /**
     * 获取品牌
     */
    public String getPhoneBrand() {
//        TelephonyManager manager= (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//        String mtype = android.os.Build.MODEL;
        String brand = android.os.Build.BRAND;//手机品牌
        return brand;
    }

    /**
     * 获取型号
     */
    public String getPhoneMODEL() {
        String model = android.os.Build.MODEL;//手机型号
        return model;
    }

    /**
     * 获取手机分辨率
     *
     * @param context
     * @return
     */
    public String getResolution(Context context) {
        // 方法1 Android获得屏幕的宽和高
        WindowManager windowManager = ((Activity) context).getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        int screenWidth = display.getWidth();
        int screenHeight = display.getHeight();
        Log.w(TAG, "分辨率：" + screenWidth + "*" + screenHeight);
        return screenWidth + "*" + screenHeight;
    }


    /**
     * 获取联网方式
     * 需要权限 Manifest.permission.READ_PHONE_STATE
     */
    public static String getNetworkType(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            Network network = connectivityManager.getActiveNetwork();
            if (network != null) {
                NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
                if (networkCapabilities != null) {
                    if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        return "WiFi";
                    } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                        if (telephonyManager != null) {
                            @SuppressLint("MissingPermission") int networkType = telephonyManager.getDataNetworkType();
                            switch (networkType) {
                                case TelephonyManager.NETWORK_TYPE_NR:
                                    return "5G";
                                case TelephonyManager.NETWORK_TYPE_LTE:
                                    return "4G";
                                case TelephonyManager.NETWORK_TYPE_HSPAP:
                                case TelephonyManager.NETWORK_TYPE_HSPA:
                                case TelephonyManager.NETWORK_TYPE_HSDPA:
                                case TelephonyManager.NETWORK_TYPE_HSUPA:
                                    return "3G";
                                case TelephonyManager.NETWORK_TYPE_EDGE:
                                case TelephonyManager.NETWORK_TYPE_GPRS:
                                    return "2G";
                                default:
                                    return "Unknown";
                            }
                        }
                    }
                }
            }
        }
        return "Unknown";
    }

    /**
     * 获取操作系统
     *
     * @return
     */
    public String getOS() {
        Log.w(TAG, "操作系统:" + "Android" + android.os.Build.VERSION.RELEASE);
        return "Android" + android.os.Build.VERSION.RELEASE;
    }

    /**
     * 获取wifi当前ip地址
     *
     * @param context
     * @return
     */
    public String getLocalIpAddress(Context context) {
        try {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int i = wifiInfo.getIpAddress();
            return int2ip(i);
        } catch (Exception ex) {
            return " 请保证是WIFI,或者请重新打开网络!\n" + ex.getMessage();
        }
    }

    /**
     * 将ip的整数形式转换成ip形式
     *
     * @param ipInt
     * @return
     */
    public String int2ip(int ipInt) {
        StringBuilder sb = new StringBuilder();
        sb.append(ipInt & 0xFF).append(".");
        sb.append((ipInt >> 8) & 0xFF).append(".");
        sb.append((ipInt >> 16) & 0xFF).append(".");
        sb.append((ipInt >> 24) & 0xFF);
        return sb.toString();
    }

    /**
     * 获取蓝牙MAC地址
     *
     * @return
     */
    public String getBtAddressByReflection() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Field field = null;
        try {
            field = BluetoothAdapter.class.getDeclaredField("mService");
            field.setAccessible(true);
            Object bluetoothManagerService = field.get(bluetoothAdapter);
            if (bluetoothManagerService == null) {
                return null;
            }
            Method method = bluetoothManagerService.getClass().getMethod("getAddress");
            if (method != null) {
                Object obj = method.invoke(bluetoothManagerService);
                if (obj != null) {
                    return obj.toString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取android序列号SN
     * 8675 8604 3504 498
     *
     * @return id或者空串
     */
    public synchronized String getSerialNumber() {
        String serialNumber = null;
        try {
            Class clazz = Class.forName("android.os.SystemProperties");
            if (clazz != null) {
                Method method_get = clazz.getMethod("get", String.class, String.class);
                if (method_get != null) {
                    serialNumber = (String) (method_get.invoke(clazz, "ro.serialno", ""));
                }
            }
        } catch (Exception e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        }

        return serialNumber != null ? serialNumber : "";
    }

    /*
     * 获取手机号(基于sim卡是否有写入，未写入则返回空)
     * 注：调用前需要获取READ_PHONE_STATE权限！！！
     * @return 手机号
     *
     *  //截取+86
            if (phone.startsWith("+86")) {
                phone = phone.substring(3, phone.length());
            }
     *
     *
     * */
    @SuppressLint("MissingPermission")
    public String getPhone(Context context) {
        String tel = "";
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
            tel = tm.getLine1Number();
            return tel;
        } catch (Exception e) {
            tel = "";
        }
        return tel;

    }

    /**
     * 唤醒屏幕
     *
     * @param context
     */
    @SuppressLint("InvalidWakeLockTag")
    public static void wakeUpAndUnlock(Context context) {
        //屏锁管理器
        KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock kl = km.newKeyguardLock("unLock");
        //解锁
        kl.disableKeyguard();
        //获取电源管理器对象
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        //获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP |
                PowerManager.SCREEN_DIM_WAKE_LOCK, "bright");
        //点亮屏幕
        wl.acquire(10*60*1000L /*10 minutes*/);
        //释放
        wl.release();
    }

    /**
     * 震动
     *
     * @param context
     * @param vibrationPattern 第二参数表示从哪里开始循环，比如这里的0表示这个数组在第一次循环完之后会从下标0开始循环到最后，这里的如果是-1表示不循环。
     */
    public void vibrator(Context context, long[] vibrationPattern) {
        //获取系统的Vibrator服务
        Vibrator vibrator = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
        vibrator.vibrate(vibrationPattern, -1);
    }
    /**
     * 创建一次性振动 Android 8.0+
     * 一次性振动将以指定的振幅在指定的时间段内持续振动，然后停止。
     * milliseconds：振动的毫秒数。这必须是一个正数。
     * amplitude：振动的强度。它必须是1到255之间的值，或 DEFAULT_AMPLITUDE（-1）。
     *
     */
    public void vibratorOneShot(Context context, long milliseconds, int amplitude) {
        Vibrator mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        VibrationEffect mEffect = VibrationEffect.createOneShot(milliseconds, amplitude);
//
//        VibrationEffect.DEFAULT_AMPLITUDE = -1 默认效果
//        VibrationEffect.EFFECT_CLICK = 0 点击效果
//        VibrationEffect.EFFECT_DOUBLE_CLICK = 1 双击效果
//        VibrationEffect.EFFECT_HEAVY_CLICK = 5 震动效果更强
//        VibrationEffect.EFFECT_TICK = 2 滴水效果
//

        AudioAttributes audioAttributes = null;
        audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM) //key
                .build();
        mVibrator.vibrate(mEffect, audioAttributes);
    }
    /**
     * 创建波形振动 Android 8.0+
     */
    public void vibratorWaveform(Context context, long[] timings, int[] amplitudes, int repeat) {
        Vibrator mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        VibrationEffect mEffect = VibrationEffect.createWaveform(timings, amplitudes, repeat);
//
//        createWaveform(long[] timings, int[] amplitudes, int repeat)
//        timings:震动替换倒计时，从停止开始，依次为停->震->停->震……，比如 {10000，20000，30000，40000} 表示 停10秒 震20秒 停30秒 震40秒
//        amplitudes：震动幅度，数值0 - 255 ，0表示不震动，255震感最强，数组长度与timings一致
//        repeat：引索，-1表示震动一次，其他表示重复震动
//        repeat这个值有点特别，repeat = -1时会依次震动一次，repeat = 0 时不停重复震动，其他值时，会先从0-引索间跳过停止时间仅保留震动时长，并且在引索后不停重复，比如：timings = {10000，20000，30000，40000}，repeat = 2表示先震动20秒，再重复30秒->40秒

        AudioAttributes audioAttributes = null;
        audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM) //key
                .build();
        mVibrator.vibrate(mEffect, audioAttributes);
    }
}
