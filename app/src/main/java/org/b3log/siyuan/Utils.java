/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2020-2024.
 *
 * lastModified: 2024/7/8 上午5:43
 * updated: 2024/7/8 上午5:43
 */
package org.b3log.siyuan;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.text.TextUtils;

import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.tencent.bugly.crashreport.BuglyLog;

import org.apache.commons.io.FileUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import mobile.Mobile;
import sc.windom.namespace.SillotMatrix.BuildConfig;

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

    public static boolean isFirstLaunch(Activity activity) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        boolean isFirstLaunch = sharedPreferences.getBoolean("is_first_launch", true);
        if (isFirstLaunch) {
            sharedPreferences.edit().putBoolean("is_first_launch", false).apply();
        }
        return isFirstLaunch;
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
                if (dir != null && !dir.isDirectory() && !dir.mkdirs())
                    throw new FileNotFoundException("Failed to ensure directory: " + dir.getAbsolutePath());
                if (ze.isDirectory())
                    continue;
                try (FileOutputStream fout = new FileOutputStream(file)) {
                    while ((count = zis.read(buffer)) != -1)
                        fout.write(buffer, 0, count);
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
                BuglyLog.e(tag, msg, e);
            } else {
                BuglyLog.e(tag, msg);
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
                    writer.write(e + "\n");
                }
                writer.flush();
                writer.close();
            } catch (final Exception ex) {
                BuglyLog.e("logging", "Write mobile log failed", ex);
            }
        }
    }

    public static void LogInfo(final String tag, final String msg) {
        synchronized (Utils.class) {
            BuglyLog.i(tag, msg);
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
                BuglyLog.e("logging", "Write mobile log failed", ex);
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
            LogError("isDebugPackageAndMode", e.getLocalizedMessage(), e);
        }

        // Check if the package name contains ".debug"
        boolean isDebugPackage = context.getPackageName() != null && context.getPackageName().contains(".debug");
        boolean isDebugMode = appInfo != null && (appInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        return isDebugPackage && isDebugMode;
    }
}
