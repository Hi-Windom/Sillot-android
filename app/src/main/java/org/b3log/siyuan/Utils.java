/*
 * SiYuan - 源于思考，饮水思源
 * Copyright (c) 2020-present, b3log.org
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.b3log.siyuan;

import static android.content.Context.POWER_SERVICE;

import static androidx.core.app.ActivityCompat.startActivityForResult;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.app.KeyguardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.Manifest;
import android.webkit.WebView;

import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.kongzue.dialogx.dialogs.PopTip;

import org.apache.commons.io.FileUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import mobile.Mobile;
import sc.windom.sofill.S;

/**
 * 工具类.
 *
 * @author <a href="https://88250.b3log.org">Liang Ding</a>
 * @author <a href="https://github.com/wwxiaoqi">Jane Haring</a>
 * @version 1.1.0.7, Mar 20, 2024
 * @since 1.0.0
 */
public final class Utils {

    /**
     * App version.
     */
    public static final String version = BuildConfig.VERSION_NAME;

    public static String getDeviceInfoString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n- via -\n");
        sb.append("Device: ").append(Build.BRAND).append("-").append(Build.MODEL).append(" (").append(Build.MANUFACTURER).append(")\n");
        sb.append("Android: ").append(Build.VERSION.RELEASE).append(" (SDK_").append(Build.VERSION.SDK_INT).append(")\n");
        sb.append("Apk: ").append(BuildConfig.VERSION_NAME).append(" (").append(BuildConfig.VERSION_CODE).append(")\n");
        return sb.toString();
    }

    public static boolean isFirstLaunch(Activity activity) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        boolean isFirstLaunch = sharedPreferences.getBoolean("is_first_launch", true);
        if (isFirstLaunch) {
            sharedPreferences.edit().putBoolean("is_first_launch", false).apply();
        }
        return isFirstLaunch;
    }

    public static boolean isPad(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        float widthInches = metrics.widthPixels / metrics.xdpi;
        float heightInches = metrics.heightPixels / metrics.ydpi;
        double diagonalInches = Math.sqrt(
                Math.pow(widthInches, 2) + Math.pow(heightInches, 2));
        return diagonalInches >= 7;
    }

    public static boolean isValidPermission(String id) {
        if (id == null || id.isEmpty()) {
            return false;
        }
        try {
            // 使用反射获取 Manifest.permission 类中的所有静态字段
            Field[] fields = Manifest.permission.class.getFields();
            for (Field field : fields) {
                // 检查是否存在与id匹配的静态字段
                if (field.getType() == String.class && field.get(null).equals(id)) {
                    return false;
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    public static void requestExternalStoragePermission(Activity activity) {
        if (!canManageAllFiles(activity)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            startActivityForResult(activity, intent, S.REQUEST_CODE_MANAGE_STORAGE, null);
        }
    }

    public static boolean canManageAllFiles(Context context) { // 管理所有文件
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        }
        // On older versions, we assume that the READ_EXTERNAL_STORAGE and WRITE_EXTERNAL_STORAGE
        // permissions are sufficient to manage all files.
        return context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED &&
                context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED;
    }

    public static boolean canAccessDeviceState(Context context) { // 访问设备状态信息
        return context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) ==
                PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isIgnoringBatteryOptimizations(Context context) { // 忽略电池优化
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            return powerManager.isIgnoringBatteryOptimizations(context.getPackageName());
        }
        return false;
    }
    public static boolean isShowingOnLockScreen(Context context) { // 锁屏显示
        KeyguardManager keyguardManager = context.getSystemService(KeyguardManager.class);
        if (keyguardManager != null) {
            return keyguardManager.isDeviceLocked();
        }
        return false;
    }


    public static boolean canShowOnTop(Context context) { // 悬浮窗
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(context);
        }
        return true; // Assuming it's allowed on older versions
    }

    public static boolean canPopInBackground(Context context) { // 后台弹出界面
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return Settings.canDrawOverlays(context);
        }
        return true; // Assuming it's allowed on older versions
    }
    public static boolean canRequestPackageInstalls(Context context) { // 安装未知应用
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return context.getPackageManager().canRequestPackageInstalls();
        }
        return true; // Assuming it's allowed on older versions
    }


    public static void requestPermissionActivity(Context context, final String id, final String Msg) { // id 对应的是具体的类，在 permission 文件夹，没有事先创建则会报错


        if (id.equals("Battery")) {
            Intent battery = new Intent("sc.windom.sillot.intent.permission." + id);
            battery.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            // 获取 Application Context 并启动 Activity
            context.getApplicationContext().startActivity(battery);
        }
        if (Msg != null && !Msg.isEmpty()) {
            PopTip.show(Msg);
        }
    }
    public static boolean hasBatteryOptimizationPermission(Context context) {
            PowerManager pm = (PowerManager) context.getSystemService(POWER_SERVICE);
            if (pm != null) {
                return pm.isIgnoringBatteryOptimizations(context.getPackageName());
            }
        return false;
    }



    public static void registerSoftKeyboardToolbar(final Activity activity, final WebView webView) {
        KeyboardUtils.registerSoftInputChangedListener(activity, height -> {
            if (!activity.isInMultiWindowMode()) {
                String javascriptCommand = KeyboardUtils.isSoftInputVisible(activity) ? "showKeyboardToolbar()" : "hideKeyboardToolbar()";
                webView.evaluateJavascript("javascript:" + javascriptCommand, null);
            }
        });
    }


    public static void unzipAsset(final AssetManager assetManager, final String zipName, final String targetDirectory) {
        ZipInputStream zis = null;
        try {
            final InputStream zipFile = assetManager.open(zipName);
            zis = new ZipInputStream(new BufferedInputStream(zipFile));
            ZipEntry ze;
            int count;
            byte[] buffer = new byte[1024 * 512];
            while ((ze = zis.getNextEntry()) != null) {
                final File file = new File(targetDirectory, ze.getName());
                try {
                    ensureZipPathSafety(file, targetDirectory);
                } catch (final Exception se) {
                    throw se;
                }

                final File dir = ze.isDirectory() ? file : file.getParentFile();
                if (!dir.isDirectory() && !dir.mkdirs())
                    throw new FileNotFoundException("Failed to ensure directory: " + dir.getAbsolutePath());
                if (ze.isDirectory())
                    continue;
                FileOutputStream fout = new FileOutputStream(file);
                try {
                    while ((count = zis.read(buffer)) != -1)
                        fout.write(buffer, 0, count);
                } finally {
                    fout.close();
                }
            /* if time should be restored as well
            long time = ze.getTime();
            if (time > 0)
                file.setLastModified(time);
            */
            }
        } catch (final Exception e) {
            Utils.LogError("boot", "unzip asset [from=" + zipName + ", to=" + targetDirectory + "] failed", e);
        } finally {
            if (null != zis) {
                try {
                    zis.close();
                } catch (final Exception e) {
                }
            }
        }
    }

    private static void ensureZipPathSafety(final File outputFile, final String destDirectory) throws Exception {
        final String destDirCanonicalPath = (new File(destDirectory)).getCanonicalPath();
        final String outputFileCanonicalPath = outputFile.getCanonicalPath();
        if (!outputFileCanonicalPath.startsWith(destDirCanonicalPath)) {
            throw new Exception(String.format("Found Zip Path Traversal Vulnerability with %s", outputFileCanonicalPath));
        }
    }

    public static String getIPAddressList() {
        final List<String> list = new ArrayList<>();
        try {
            for (final Enumeration<NetworkInterface> enNetI = NetworkInterface.getNetworkInterfaces(); enNetI.hasMoreElements(); ) {
                final NetworkInterface netI = enNetI.nextElement();
                for (final Enumeration<InetAddress> enumIpAddr = netI.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    final InetAddress inetAddress = enumIpAddr.nextElement();
                    if (inetAddress instanceof Inet4Address && !inetAddress.isLoopbackAddress()) {
                        list.add(inetAddress.getHostAddress());
                    }
                }
            }
        } catch (final Exception e) {
            LogError("network", "get IP list failed, returns 127.0.0.1", e);
        }
        list.add("127.0.0.1");
        return TextUtils.join(",", list);
    }

    public static void LogError(final String tag, final String msg, final Throwable e) {
        synchronized (Utils.class) {
            if (null != e) {
                Log.e(tag, msg, e);
            } else {
                Log.e(tag, msg);
            }
            try {
                final String workspacePath = Mobile.getCurrentWorkspacePath();
                if (StringUtils.isEmpty(workspacePath)) {
                    return;
                }

                final String mobileLogPath = workspacePath + "/temp/mobile.log";
                final File logFile = new File(mobileLogPath);
                if (logFile.exists() && 1024 * 1024 * 8 < logFile.length()) {
                    FileUtils.deleteQuietly(logFile);
                }

                final FileWriter writer = new FileWriter(logFile, true);
                final String time = TimeUtils.millis2String(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss");
                writer.write("E " + time + " " + tag + " " + msg + "\n");
                if (null != e) {
                    writer.write(Log.getStackTraceString(e) + "\n");
                }
                writer.flush();
                writer.close();
            } catch (final Exception ex) {
                Log.e("logging", "Write mobile log failed", ex);
            }
        }
    }

    public static void LogInfo(final String tag, final String msg) {
        synchronized (Utils.class) {
            Log.i(tag, msg);
            try {
                final String workspacePath = Mobile.getCurrentWorkspacePath();
                if (StringUtils.isEmpty(workspacePath)) {
                    return;
                }

                final String mobileLogPath = workspacePath + "/temp/mobile.log";
                final File logFile = new File(mobileLogPath);
                if (logFile.exists() && 1024 * 1024 * 8 < logFile.length()) {
                    FileUtils.deleteQuietly(logFile);
                }

                final FileWriter writer = new FileWriter(logFile, true);
                final String time = TimeUtils.millis2String(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss");
                writer.write("I " + time + " " + tag + " " + msg + "\n");
                writer.flush();
                writer.close();
            } catch (final Exception ex) {
                Log.e("logging", "Write mobile log failed", ex);
            }
        }
    }

    /**
     * Checks if the current package name contains ".debug" and if debug mode is enabled.
     *
     * @param context The Android context used to retrieve the package information.
     * @return true if the package name contains ".debug" and debug mode is enabled, false otherwise.
     */
    public static boolean isDebugPackageAndMode(Context context) {
        PackageManager packageManager = context.getPackageManager();
        ApplicationInfo appInfo = null;
        try {
            appInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        // Check if the package name contains ".debug"
        boolean isDebugPackage = context.getPackageName() != null && context.getPackageName().contains(".debug");
        boolean isDebugMode = appInfo != null && (appInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        return isDebugPackage && isDebugMode;
    }
}
