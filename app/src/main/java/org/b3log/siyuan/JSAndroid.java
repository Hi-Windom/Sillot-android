/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2020-2024.
 *
 * lastModified: 2024/7/9 上午12:27
 * updated: 2024/7/9 上午12:27
 */
package org.b3log.siyuan;

import static androidx.core.app.ActivityCompat.startActivityForResult;
import static com.blankj.utilcode.util.ActivityUtils.startActivity;
import static com.blankj.utilcode.util.ViewUtils.runOnUiThread;

import static sc.windom.sofill.Ss.S_Webview.jsCode_gibbetBiometricAuth;
import static sc.windom.sofill.Us.U_Phone.toggleFullScreen;
import static sc.windom.sofill.android.BiometricKt.newBiometricPrompt;
import static sc.windom.sofill.android.webview.WebViewThemeKt.applySystemThemeToWebView;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ShareCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.blankj.utilcode.util.StringUtils;
import com.kongzue.dialogx.dialogs.MessageDialog;
import com.kongzue.dialogx.dialogs.PopTip;
import com.kongzue.dialogx.dialogs.TipDialog;
import com.kongzue.dialogx.dialogs.WaitDialog;
import com.kongzue.dialogx.interfaces.OnDialogButtonClickListener;
import com.kongzue.dialogx.util.TextInfo;
import com.tencent.bugly.crashreport.BuglyLog;
import com.tencent.bugly.crashreport.CrashReport;

import sc.windom.sillot.App;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.FutureTask;

import mobile.Mobile;
import sc.windom.sofill.S;
import sc.windom.sofill.U;
import sc.windom.sofill.Us.U_Permission;
import sc.windom.sofill.Us.U_Phone;
import sc.windom.sofill.Us.U_Uri;
import sc.windom.sofill.android.BiometricCallback;
import sc.windom.sofill.android.permission.Ps;
import sc.windom.namespace.SillotMatrix.BuildConfig;
import sc.windom.sofill.compose.WebViewActivity;

/**
 * JavaScript 接口.
 *
 * @author <a href="https://88250.b3log.org">Liang Ding</a>
 * @author <a href="https://github.com/Soltus">绛亽</a>
 * @version 1.1.3.1, May 3, 2024
 * @since 1.0.0
 */
public final class JSAndroid {
    private MainActivity activity;
    private final String TAG = "JSAndroid.java";
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    public JSAndroid(final MainActivity activity) {
        this.activity = activity;
    }

    //// Sillot extend start
    @JavascriptInterface
    public void showInputMethodPicker() {
        U_Phone.showInputMethodPicker();
    }
    @JavascriptInterface
    public String getUA() {
        FutureTask<String> futureTask = new FutureTask<>(() -> {
            // 在主线程中执行
            WebSettings settings = activity.webView.getSettings();
            return settings.getUserAgentString();
        });

        mainHandler.post(futureTask);

        try {
            // 等待任务完成并返回结果
            return futureTask.get();
        } catch (Exception e) {
            BuglyLog.e(TAG, e.toString());
            return null;
        }
    }
    @JavascriptInterface
    public boolean isMIUI() {
        U.getPHONE();
        return U_Phone.isMIUI(activity.getApplicationContext());
    }
    @JavascriptInterface
    public boolean isOriginOS() {
        U.getPHONE();
        return U_Phone.isOriginOS(activity.getApplicationContext());
    }
    @JavascriptInterface
    public void toggleFullScreenState() {
        toggleFullScreen(activity);
        OnDialogButtonClickListener<PopTip> cb = (popTip, view) -> {
            toggleFullScreen(activity);
            popTip.dismiss();
            return false;
        };
       PopTip.show("知道了，玩去吧~", "撤销").setOnButtonClickListener(cb);
    }
    @JavascriptInterface
    public void buglyPost1() {
        try {
            throw new Exception("JavascriptInterface buglyPost1");
        } catch (Exception e) {
            BuglyLog.e(TAG, "捕获到异常：" + e.getMessage());
            App.getInstance().reportException(e);
        }
       PopTip.show("知道了，玩去吧~");
    }
    @JavascriptInterface
    public void buglyPost2() {
        try {
            CrashReport.testJavaCrash();
        } catch (Exception e) {
           PopTip.show( "知道了，玩去吧~");
        }
    }
    @JavascriptInterface
    public boolean requestExternalStoragePermission() {
        U_Permission.requestExternalStoragePermission(activity);
        return true;
    }
    @JavascriptInterface
    public boolean requestPermissionActivity(final String id, final String Msg, final String callback) {
        BuglyLog.w(TAG, "requestPermissionActivity() invoked");
        if (Msg != null && !Msg.isEmpty()) {
           PopTip.show(Msg);
        }
        if (id.equals("Battery")) {
            if (callback.equals("androidReboot")) {
                if (U_Permission.isIgnoringBatteryOptimizations(activity)) {
                    // 应用在电池优化的豁免列表中
                    MessageDialog messageDialog = new MessageDialog("伺服已开启，重启后生效"
                            , "为了重启内核，汐洛其他活动会被杀死，请确认重启前其他汐洛活动不存在未保存内容。\n\n稍后手动重启可能导致未知的错误，非必要不选择"
                            , "立即重启", "稍后手动重启", null)
                            .setCancelable(false) //是否允许点击外部区域或返回键关闭
                            .setMaskColor(Color.parseColor("#3D000000"))
                            ;
                    messageDialog.show().setCancelButton((baseDialog, v) -> false).setOkButton((baseDialog, v) -> {
                        androidReboot();
                        return false;
                    }).setCancelTextInfo(new TextInfo().setFontColor(Color.RED));
                } else {
                    // 应用不在电池优化的豁免列表中
                    MessageDialog messageDialog = new MessageDialog("伺服已开启，重启后生效"
                            , """
                            申请权限忽略电源优化（需系统支持）能获得稳定的伺服体验，代价是增加耗电，请按需选择。如果已经忽略，可选择立即重启。
                            
                            为了重启内核，汐洛其他活动会被杀死，请确认重启前其他汐洛活动不存在未保存内容。

                            稍后手动重启可能导致未知的错误，非必要不选择"""
                            , "申请权限并重启", "稍后手动重启", "立即重启")
                            .setButtonOrientation(LinearLayout.VERTICAL)  // 选项竖向排列
                            .setCancelable(false) //是否允许点击外部区域或返回键关闭
                            .setMaskColor(Color.parseColor("#3D000000"))
                            ;
                    messageDialog.show().setOkButton((baseDialog, v) -> {
                        activity.runOnUiThread(() -> {
                            Intent battery = new Intent("sc.windom.sillot.intent.permission.Battery");
                            battery.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivityForResult(activity, battery, S.getREQUEST_CODE().REQUEST_IGNORE_BATTERY_OPTIMIZATIONS_AND_REBOOT, null);
                        });
                        return false;
                    }).setCancelButton((baseDialog, v) -> false).setOtherButton((baseDialog, v) -> {
                        androidReboot();
                        return false;
                    }).setCancelTextInfo(new TextInfo().setFontColor(Color.RED));
                }
            } else {
                Intent battery = new Intent("sc.windom.sillot.intent.permission.Battery");
                battery.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(activity, battery, S.getREQUEST_CODE().REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, null);
            }
        }
        BuglyLog.w(TAG, "requestPermissionActivity()  return true");
        return true; // 返回真表示已经发起申请，不代表结果
    }
    @JavascriptInterface
    public boolean requestPermission(final String id, final String Msg) {
        BuglyLog.d(TAG, "requestPermission() invoked");
        if (U_Permission.isValidPermission(id)) {
            BuglyLog.d(TAG, "requestPermission("+id+")  return false");
            return false;
        }
        if (Msg != null && !Msg.isEmpty()) {
           PopTip.show(Msg);
        }
        ActivityCompat.requestPermissions(activity, new String[]{ id }, 1001);
        BuglyLog.d(TAG, "requestPermission()  return true");
        return true; // 返回真表示已经发起申请，不代表结果
    }
    @JavascriptInterface
    public void requestPermissionAll() {
        BuglyLog.d(TAG, "requestPermissionAll() invoked");
        final HashSet<String> permissionList = new HashSet<>();
        HashSet<String> permissionsToCheck = new HashSet<>(Ps.PG_Core); // 核心权限组，每次启动都要检查
        if (Build.VERSION.SDK_INT >= 33) {
            permissionsToCheck.addAll(Ps.useAPI33);
        }
        for (String permission : permissionsToCheck) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(permission);
                BuglyLog.w(TAG, "onCreate() -> "+permission+" task add [Ps.PG_Core]");
            } else {
                BuglyLog.d(TAG, "onCreate() -> "+permission+" granted [Ps.PG_Core]");
            }
        }


//        if (Utils.isFirstLaunch(activity)) {
            permissionsToCheck.addAll(Ps.PG_unCore); // 非核心权限组，仅安装后首次启动集中申请
            for (String permission : permissionsToCheck) {
                if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionList.add(permission);
                    BuglyLog.w(TAG, "onCreate() -> "+permission+" task add [Ps.PG_unCore]");
                } else {
                    BuglyLog.d(TAG, "onCreate() -> "+permission+" granted [Ps.PG_unCore]");
                }
            }
//        }

        activity.requestPermissionAll_works += permissionList.size();
        doPermissionApply(permissionsToCheck);
    }

    private void doPermissionApply(HashSet<String> permissionList) {
        if (!permissionList.isEmpty()) {


            String[] permissionsToRequest = permissionList.toArray(new String[0]);
            boolean shouldShowRationale = false;
            for (String permission : permissionsToRequest) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                    shouldShowRationale = true;
                    break;
                }
            }

            if (shouldShowRationale) {
                // 显示权限说明
               PopTip.show("必要权限缺失，请处理！");
                // 打开应用详情界面
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setData(Uri.parse("package:" + activity.getPackageName()));
                startActivity(intent);
            } else {
                activity.requestPermissionLauncher.launch(permissionsToRequest);
            }
        } else {
            // 处理无权限的情况，例如提醒用户或者直接返回
        }
    }

    @JavascriptInterface
    public void setMMKV(final String key, final String value) {
        BuglyLog.d(TAG, "setMMKV() invoked");
        activity.mmkv.encode(key, value);
    }

    @JavascriptInterface
    public String getMMKV(final String key) {
        BuglyLog.d(TAG, "getMMKV() invoked");
        // 出于安全考虑禁止实现
        return "";
    }

    @JavascriptInterface
    public void showBiometricPrompt(final String captcha) {
        BuglyLog.d(TAG, "showBiometricPrompt() invoked");
        // 在主线程中执行
        mainHandler.post(() -> {
        // 在 MainActivity 中调用 showBiometricPrompt 方法
        try {
            newBiometricPrompt(activity, "指纹解锁", "", "取消", new BiometricCallback() {
                @Override
                public void onAuthenticationSuccess() {
                    // 认证成功的处理逻辑
                    String accessAuthCode = activity.mmkv.decodeString("accessAuthCode");
                    if (accessAuthCode == null) {
                        TipDialog.show("抱歉出错了 ＞︿＜", WaitDialog.TYPE.WARNING);
                        return;
                    }
                    TipDialog.show("Success!", WaitDialog.TYPE.SUCCESS, 200);
                    // 在这里调用 WebView 方法
                    String jsCode = jsCode_gibbetBiometricAuth(accessAuthCode, captcha);
                    BuglyLog.d("evaluateJavascript", jsCode);
                    activity.webView.evaluateJavascript(jsCode, null);
                    activity.mmkv.putString("AppCheckInState","unlockScreen");
                }

                @Override
                public void onAuthenticationFailed() {
                    // 认证失败的处理逻辑
                    TipDialog.show("指纹不匹配!", WaitDialog.TYPE.ERROR);
                }

                @Override
                public void onAuthenticationError(@NonNull CharSequence errString) {
                    // 认证错误的处理逻辑（一般是用户点击了取消）

                    BiometricManager biometricManager = BiometricManager.from(activity);
                    switch (biometricManager.canAuthenticate()) {
                        case BiometricManager.BIOMETRIC_SUCCESS:
                            BuglyLog.d("BiometricManager","应用可以进行生物识别技术进行身份验证。");
                            return;
                        case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                            BuglyLog.e("BiometricManager","该设备上没有搭载可用的生物特征功能。");
                           PopTip.show("该设备上没有搭载可用的生物特征功能");
                            return;
                        case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                            BuglyLog.e("BiometricManager","生物识别功能当前不可用。");
                           PopTip.show("生物识别功能当前不可用");
                            return;
                        case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                            BuglyLog.e("BiometricManager","用户没有录入生物识别数据。");
                           PopTip.show("用户没有录入生物识别数据");
                            return;
                    }

                   PopTip.show("用户取消了指纹解锁");
                }

            });

        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        });

    }

    @JavascriptInterface
    public void savePictureByURL(final String imageUrl) throws IOException {
        BuglyLog.d(TAG, "savePictureByURL() invoked");
        // 下载图片
        Bitmap IMG = BitmapFactory.decodeStream(new URL("http://127.0.0.1:58131"+imageUrl).openConnection().getInputStream());
        // 获取内部存储的 DCIM/Sillot 目录
        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Sillot");
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                BuglyLog.e("Sillot_savePictureByURL", "Failed to create directory: " + directory.getAbsolutePath());
                return;
            }
        }

        String formattedDate = new SimpleDateFormat("yyyy-MM-dd_HHmmss").format(new Date());

        // 在 DCIM/Sillot 目录中创建文件
        File file = new File(directory, "Sillot_savePictureByURL_" + formattedDate + ".png");
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            IMG.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
            BuglyLog.i("saveLongScreenshot", "Sillot_savePictureByURL saved to " + file.getAbsolutePath());
            notifyGallery(file);
           PopTip.show("图片已保存到 /DCIM/Sillot");
        } catch (IOException e) {
            e.printStackTrace();
            BuglyLog.e("saveLongScreenshot", "Failed to save Sillot_savePictureByURL");
        }
    }

    private void notifyGallery(File imageFile) {
//        向系统相册发送媒体文件扫描广播来通知系统相册更新媒体库

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaScannerConnection.scanFile(activity,
                    new String[]{imageFile.toString()}, null,
                    (path, uri) -> {
                        BuglyLog.i("ExternalStorage", "Scanned " + path + ":");
                        BuglyLog.i("ExternalStorage", "-> uri=" + uri);
                    });
        } else {
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(imageFile);
                mediaScanIntent.setData(contentUri);
                activity.sendBroadcast(mediaScanIntent);
        }
    }
    // 使用图片选择器
    private void openGalleryPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        activity.startActivity(intent);
    }
    // 使用图片选择器2
    private void openGalleryPicker2() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE); // 删掉似乎不影响
        activity.startActivity(intent);
    }


    @JavascriptInterface
    public void exitSillotAndroid() {
        BuglyLog.d(TAG, "exitSillotAndroid() invoked");
        activity.runOnUiThread(() -> {
            activity.exit();
        });
    }
    @JavascriptInterface
    public void androidReboot() {
        BuglyLog.d(TAG, "androidReboot() invoked");
        runOnUiThread(() -> {
            activity.coldRestart();
        });
    }

    //// Sillot extend end

    @JavascriptInterface
    public String getBlockURL() {
        var url = activity.getIntent().getStringExtra("blockURL");
        BuglyLog.d(TAG, "getBlockURL() invoked. original url="+url);
        if (url == null) { url = ""; }
        return url;
    }

    @JavascriptInterface
    public String readClipboard() {
        BuglyLog.d(TAG, "readClipboard() invoked");
        final ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
        final ClipData clipData = clipboard.getPrimaryClip();
        if (null == clipData) {
            return "";
        }

        final ClipData.Item item = clipData.getItemAt(0);
        if (null != item.getUri()) {
            final Uri uri = item.getUri();
            final String url = uri.toString();
            if (url.startsWith("http://127.0.0.1:58131/assets/")) {
                final int idx = url.indexOf("/assets/");
                final String asset = url.substring(idx);
                String name = asset.substring(asset.lastIndexOf("/") + 1);
                final int suffixIdx = name.lastIndexOf(".");
                if (0 < suffixIdx) {
                    name = name.substring(0, suffixIdx);
                }
                if (23 < StringUtils.length(name)) {
                    name = name.substring(0, name.length() - 23);
                }
                return "![" + name + "](" + asset + ")";
            }
        }
        return item.getText().toString();
    }

    @JavascriptInterface
    public void writeImageClipboard(final String uri) {
        BuglyLog.d(TAG, "writeImageClipboard() invoked");
        final ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
        final ClipData clip = ClipData.newUri(activity.getContentResolver(), "Copied img from SiYuan", Uri.parse("http://127.0.0.1:58131/" + uri));
        clipboard.setPrimaryClip(clip);
    }

    @JavascriptInterface
    public void writeClipboard(final String content) {
        BuglyLog.d(TAG, "writeClipboard() invoked");
        final ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
        final ClipData clip = ClipData.newPlainText("Copied text from SiYuan", content);
        clipboard.setPrimaryClip(clip);
    }

    @JavascriptInterface
    public void returnDesktop() {
        BuglyLog.d(TAG, "returnDesktop() invoked");
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

    @JavascriptInterface
    public void openExternal(String url) {
        BuglyLog.d(TAG, "openExternal() invoked");
        if (StringUtils.isEmpty(url)) {
            return;
        }
        BuglyLog.d("JSAndroid.openExternal", url);

        if (url.startsWith("#")) {
            return;
        }

        if (url.startsWith("assets/")) {
            // Support opening assets through other apps on the Android https://github.com/siyuan-note/siyuan/issues/10657
            final String workspacePath = Mobile.getCurrentWorkspacePath();
            final String assetAbsPath = Mobile.getAssetAbsPath(url);
            File asset;
            String decodedUrl = url;
            try {
                if (assetAbsPath.contains(workspacePath)) {
                    asset = new File(workspacePath, assetAbsPath.substring(workspacePath.length() + 1));
                } else {
                    decodedUrl = URLDecoder.decode(url, "UTF-8");
                    asset = new File(workspacePath, "data/" + decodedUrl);
                }
                // 添加判断文件是否存在
                if (!asset.exists()) {
                    BuglyLog.e("File Not Found", "File does not exist: " + asset.getAbsolutePath());
                    url = "http://127.0.0.1:58131/" + url;
                } else {
                    BuglyLog.d("if (url.startsWith(\"assets/\"))", asset.getAbsolutePath());
                    final Uri uri = FileProvider.getUriForFile(activity.getApplicationContext(), BuildConfig.PROVIDER_AUTHORITIES, asset);
                    final String type = Mobile.getMimeTypeByExt(asset.getAbsolutePath());
                    Intent intent = new ShareCompat.IntentBuilder(activity.getApplicationContext())
                            .setStream(uri)
                            .setType(type)
                            .getIntent()
                            .setAction(Intent.ACTION_VIEW)
                            .setDataAndType(uri, type)
                            .addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                    activity.startActivity(intent);
                    return;
                }
            } catch (Exception e) {
                BuglyLog.e(TAG, String.valueOf(e));
                Utils.LogError(TAG, "openExternal failed", e);
            }
        }

        if (url.endsWith(".zip") && url.startsWith("/export/")) {
            final String workspacePath = Mobile.getCurrentWorkspacePath();
            String decodedUrl = url;
            try {
                decodedUrl = URLDecoder.decode(url, "UTF-8");
                final File asset = new File(workspacePath, "temp" + decodedUrl);
                // 添加判断文件是否存在
                if (!asset.exists()) {
                    BuglyLog.e("File Not Found", "File does not exist: " + asset.getAbsolutePath());
                } else {
                    BuglyLog.d("open by intent", asset.getAbsolutePath());
                    Uri uri = FileProvider.getUriForFile(activity.getApplicationContext(), BuildConfig.PROVIDER_AUTHORITIES, asset);
                    final String type = Mobile.getMimeTypeByExt(asset.getAbsolutePath());
                    Intent intent = new ShareCompat.IntentBuilder(activity.getApplicationContext())
                            .setStream(uri)
                            .setType(type)
                            .getIntent()
                            .setAction(Intent.ACTION_VIEW)
                            .setDataAndType(uri, type)
                            .addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                    activity.startActivity(intent);
                    return;
                }
            } catch (Exception e) {
                BuglyLog.e(TAG, String.valueOf(e));
            }
        }

        if (url.startsWith("/")) {
            url = "http://127.0.0.1:58131" + url;
        }
        BuglyLog.d("openExternal final url ", url);

        final boolean openURLUseDefaultApp = activity.mmkv.getBoolean("Gibbet@openURLUseDefaultApp", false);
        if (openURLUseDefaultApp) {
            U_Uri.openURLUseDefaultApp(url);
        } else {
            U_Uri.openURLUseSB(activity, url);
        }
    }

    @JavascriptInterface
    public void openURLuseDefaultApp(final String url) {
        BuglyLog.d(TAG, "openURLuseDefaultApp() invoked");
        U_Uri.openURLUseDefaultApp(url);
    }


    @JavascriptInterface
    public void openURLUseSB(final String url) {
        BuglyLog.d(TAG, "openURLUseSB() invoked");
        U_Uri.openURLUseSB(activity, url);
    }

    @JavascriptInterface
    public void changeStatusBarColor(final String color, final int appearanceMode) {
        BuglyLog.d(TAG, "changeStatusBarColor() invoked");
        activity.runOnUiThread(() -> {
            applySystemThemeToWebView(activity, activity.webView);
        });
    }

}
