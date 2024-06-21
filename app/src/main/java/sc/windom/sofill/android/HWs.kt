package sc.windom.sofill.android

import android.annotation.SuppressLint
import android.app.Activity
import android.app.KeyguardManager
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.media.AudioAttributes
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.telephony.TelephonyManager
import android.util.Log
import java.lang.reflect.Field

class HWs {
    /*
     * 获取MEID
     * 注：调用前需要获取READ_PHONE_STATE权限
     * */
    @SuppressLint("MissingPermission", "HardwareIds")
    fun getMEID(context: Context): String {
        var meid = ""
        val mTelephonyMgr = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        meid = mTelephonyMgr.meid
        Log.i(TAG, "Android版本大于o-26-优化后的获取---meid:$meid")

        Log.i(TAG, "优化后的获取---meid:$meid")

        return meid
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
    fun getIMEI(context: Context, index: Int): String {
        var imei = ""
        val mTelephonyMgr = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        imei = mTelephonyMgr.getImei(index)
        Log.i(TAG, "Android版本大于o-26-优化后的获取---imei-:$imei")

        Log.i(TAG, "优化后的获取---imei：$imei")
        return imei
    }

    /**
     * 获取双卡手机的imei
     */
    @Throws(Exception::class)
    private fun getDoubleImei(
        telephony: TelephonyManager,
        predictedMethodName: String,
        slotID: Int
    ): String? {
        var inumeric: String? = null

        val telephonyClass = Class.forName(telephony.javaClass.name)
        val parameter = arrayOfNulls<Class<*>?>(1)
        parameter[0] = Int::class.javaPrimitiveType
        val getSimID = telephonyClass.getMethod(predictedMethodName, *parameter)
        val obParameter = arrayOfNulls<Any>(1)
        obParameter[0] = slotID
        val ob_phone = getSimID.invoke(telephony, *obParameter)
        if (ob_phone != null) {
            inumeric = ob_phone.toString()
        }
        return inumeric
    }

    val phoneBrand: String
        /**
         * 获取品牌
         */
        get() {
//        TelephonyManager manager= (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//        String mtype = android.os.Build.MODEL;
            val brand = Build.BRAND //手机品牌
            return brand
        }

    val phoneMODEL: String
        /**
         * 获取型号
         */
        get() {
            val model = Build.MODEL //手机型号
            return model
        }

    /**
     * 获取手机分辨率
     *
     * @param context
     * @return
     */
    fun getResolution(context: Context): String {
        // 方法1 Android获得屏幕的宽和高
        val windowManager = (context as Activity).windowManager
        val display = windowManager.defaultDisplay
        val screenWidth = display.width
        val screenHeight = display.height
        Log.w(TAG, "分辨率：$screenWidth*$screenHeight")
        return "$screenWidth*$screenHeight"
    }


    val oS: String
        /**
         * 获取操作系统
         *
         * @return
         */
        get() {
            Log.w(TAG, "操作系统:" + "Android" + Build.VERSION.RELEASE)
            return "Android" + Build.VERSION.RELEASE
        }

    /**
     * 获取wifi当前ip地址
     *
     * @param context
     * @return
     */
    fun getLocalIpAddress(context: Context): String {
        try {
            val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiManager.connectionInfo
            val i = wifiInfo.ipAddress
            return int2ip(i)
        } catch (ex: Exception) {
            return """ 请保证是WIFI,或者请重新打开网络!
${ex.message}"""
        }
    }

    /**
     * 将ip的整数形式转换成ip形式
     *
     * @param ipInt
     * @return
     */
    fun int2ip(ipInt: Int): String {
        val sb = StringBuilder()
        sb.append(ipInt and 0xFF).append(".")
        sb.append((ipInt shr 8) and 0xFF).append(".")
        sb.append((ipInt shr 16) and 0xFF).append(".")
        sb.append((ipInt shr 24) and 0xFF)
        return sb.toString()
    }

    val btAddressByReflection: String?
        /**
         * 获取蓝牙MAC地址
         *
         * @return
         */
        get() {
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            try {
                val _field = BluetoothAdapter::class.java.getDeclaredField("mService")
                _field.isAccessible = true
                val bluetoothManagerService = _field[bluetoothAdapter] ?: return null
                val method = bluetoothManagerService.javaClass.getMethod("getAddress")
                val obj = method.invoke(bluetoothManagerService)
                if (obj != null) {
                    return obj.toString()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

    @get:Synchronized
    val serialNumber: String
        /**
         * 获取android序列号SN
         * 8675 8604 3504 498
         *
         * @return id或者空串
         */
        get() {
            var serialNumber: String? = null
            try {
                val clazz = Class.forName("android.os.SystemProperties")
                val method_get = clazz.getMethod("get", String::class.java, String::class.java)
                serialNumber = method_get.invoke(clazz, "ro.serialno", "") as String
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
            }

            return serialNumber ?: ""
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
    fun getPhone(context: Context): String {
        var tel = ""
        try {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            tel = tm.line1Number
            return tel
        } catch (e: Exception) {
            tel = ""
        }
        return tel
    }

    /**
     * 震动
     *
     * @param context
     * @param vibrationPattern 第二参数表示从哪里开始循环，比如这里的0表示这个数组在第一次循环完之后会从下标0开始循环到最后，这里的如果是-1表示不循环。
     */
    fun vibrator(context: Context, vibrationPattern: LongArray?) {
        //获取系统的Vibrator服务
        val vibrator = context.getSystemService(Service.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(vibrationPattern, -1)
    }

    /**
     * 创建一次性振动 Android 8.0+
     * 一次性振动将以指定的振幅在指定的时间段内持续振动，然后停止。
     * milliseconds：振动的毫秒数。这必须是一个正数。
     * amplitude：振动的强度。它必须是1到255之间的值，或 DEFAULT_AMPLITUDE（-1）。
     *
     */
    fun vibratorOneShot(context: Context, milliseconds: Long, amplitude: Int) {
        val mVibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val mEffect = VibrationEffect.createOneShot(milliseconds, amplitude)

        //
//        VibrationEffect.DEFAULT_AMPLITUDE = -1 默认效果
//        VibrationEffect.EFFECT_CLICK = 0 点击效果
//        VibrationEffect.EFFECT_DOUBLE_CLICK = 1 双击效果
//        VibrationEffect.EFFECT_HEAVY_CLICK = 5 震动效果更强
//        VibrationEffect.EFFECT_TICK = 2 滴水效果
//
        var audioAttributes: AudioAttributes? = null
        audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_ALARM) //key
            .build()
        mVibrator.vibrate(mEffect, audioAttributes)
    }

    /**
     * 创建波形振动 Android 8.0+
     */
    fun vibratorWaveform(
        context: Context,
        timings: LongArray?,
        amplitudes: IntArray?,
        repeat: Int
    ) {
        val mVibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val mEffect = VibrationEffect.createWaveform(timings, amplitudes, repeat)

        //
//        createWaveform(long[] timings, int[] amplitudes, int repeat)
//        timings:震动替换倒计时，从停止开始，依次为停->震->停->震……，比如 {10000，20000，30000，40000} 表示 停10秒 震20秒 停30秒 震40秒
//        amplitudes：震动幅度，数值0 - 255 ，0表示不震动，255震感最强，数组长度与timings一致
//        repeat：引索，-1表示震动一次，其他表示重复震动
//        repeat这个值有点特别，repeat = -1时会依次震动一次，repeat = 0 时不停重复震动，其他值时，会先从0-引索间跳过停止时间仅保留震动时长，并且在引索后不停重复，比如：timings = {10000，20000，30000，40000}，repeat = 2表示先震动20秒，再重复30秒->40秒
        var audioAttributes: AudioAttributes? = null
        audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_ALARM) //key
            .build()
        mVibrator.vibrate(mEffect, audioAttributes)
    }

    companion object {
        private const val TAG = "HWs"
        private val `object` = Any()
        private var HWs: HWs? = null

        @JvmStatic
        val instance: HWs?
            get() {
                if (HWs == null) {
                    synchronized(`object`) {
                        if (HWs == null) {
                            HWs = HWs()
                        }
                    }
                }
                return HWs
            }

        /**
         * 获取联网方式
         * 需要权限 Manifest.permission.READ_PHONE_STATE
         */
        fun getNetworkType(context: Context): String {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            val network = connectivityManager.activeNetwork
            if (network != null) {
                val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
                if (networkCapabilities != null) {
                    if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        return "WiFi"
                    } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        val telephonyManager =
                            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                        @SuppressLint("MissingPermission") val networkType =
                            telephonyManager.dataNetworkType
                        return when (networkType) {
                            TelephonyManager.NETWORK_TYPE_NR -> "5G"
                            TelephonyManager.NETWORK_TYPE_LTE -> "4G"
                            TelephonyManager.NETWORK_TYPE_HSPAP, TelephonyManager.NETWORK_TYPE_HSPA, TelephonyManager.NETWORK_TYPE_HSDPA, TelephonyManager.NETWORK_TYPE_HSUPA -> "3G"
                            TelephonyManager.NETWORK_TYPE_EDGE, TelephonyManager.NETWORK_TYPE_GPRS -> "2G"
                            else -> "Unknown"
                        }
                    }
                }
            }
            return "Unknown"
        }

        /**
         * 唤醒屏幕
         *
         * @param context
         */
        @SuppressLint("InvalidWakeLockTag")
        fun wakeUpAndUnlock(context: Context) {
            //屏锁管理器
            val km = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            val kl = km.newKeyguardLock("unLock")
            //解锁
            kl.disableKeyguard()
            //获取电源管理器对象
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            //获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
            val wl = pm.newWakeLock(
                PowerManager.ACQUIRE_CAUSES_WAKEUP or
                        PowerManager.SCREEN_DIM_WAKE_LOCK, "bright"
            )
            //点亮屏幕
            wl.acquire(10 * 60 * 1000L /*10 minutes*/)
            //释放
            wl.release()
        }
    }
}
