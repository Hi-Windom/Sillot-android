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

import static androidx.core.app.ActivityCompat.startActivityForResult;
import static com.blankj.utilcode.util.ActivityUtils.startActivity;
import static com.blankj.utilcode.util.ViewUtils.runOnUiThread;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.LinearLayout;

import androidx.biometric.BiometricManager;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;

import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.ServiceUtils;
import com.blankj.utilcode.util.StringUtils;
import com.kongzue.dialogx.dialogs.MessageDialog;
import com.kongzue.dialogx.dialogs.PopTip;
import com.kongzue.dialogx.dialogs.TipDialog;
import com.kongzue.dialogx.dialogs.WaitDialog;
import com.kongzue.dialogx.util.TextInfo;
import com.zackratos.ultimatebarx.ultimatebarx.java.UltimateBarX;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Date;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;

import io.reactivex.rxjava3.disposables.Disposable;
import mobile.Mobile;

/**
 * JavaScript 接口.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.3.0, Feb 3, 2024
 * @since 1.0.0
 */
public final class JSAndroid {
    private MainActivity activity;

    public JSAndroid(final MainActivity activity) {
        this.activity = activity;
    }

    //// Sillot extend start
    @JavascriptInterface
    public boolean requestExternalStoragePermission() {
        Utils.requestExternalStoragePermission(activity);
        return true;
    }
    @JavascriptInterface
    public boolean requestPermissionActivity(final String id, final String Msg, final String callback) {
        Log.w("JSAndroid", "requestPermissionActivity() invoked");
        if (Msg != null && !Msg.isEmpty()) {
            PopTip.show(Msg);
        }
        if (id.equals("Battery")) {
            if (callback.equals("coldRestart")) {
                if (Utils.isIgnoringBatteryOptimizations(activity)) {
                    // 应用在电池优化的豁免列表中
                    MessageDialog messageDialog = new MessageDialog("伺服已开启，重启后生效"
                            , "稍后手动重启可能导致未知的错误，非必要不选择"
                            , "立即重启", "稍后手动重启", null)
                            .setCancelable(false) //是否允许点击外部区域或返回键关闭
                            .setMaskColor(Color.parseColor("#3D000000"))
                            ;
                    messageDialog.show().setCancelButton((baseDialog, v) -> false).setOkButton((baseDialog, v) -> {
                        activity.RestartSiyuanInWebview();
                        return false;
                    }).setCancelTextInfo(new TextInfo().setFontColor(Color.RED));
                } else {
                    // 应用不在电池优化的豁免列表中
                    MessageDialog messageDialog = new MessageDialog("伺服已开启，重启后生效"
                            , "申请权限忽略电源优化（需系统支持）能获得稳定的伺服体验，代价是增加耗电，请按需选择。如果已经忽略，可选择立即重启。\n\n稍后手动重启可能导致未知的错误，非必要不选择"
                            , "申请权限并重启", "稍后手动重启", "立即重启")
                            .setButtonOrientation(LinearLayout.VERTICAL)  // 选项竖向排列
                            .setCancelable(false) //是否允许点击外部区域或返回键关闭
                            .setMaskColor(Color.parseColor("#3D000000"))
                            ;
                    messageDialog.show().setOkButton((baseDialog, v) -> {
                        activity.runOnUiThread(() -> {
                            Intent battery = new Intent("sc.windom.sillot.intent.permission.Battery");
                            battery.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivityForResult(activity, battery, Ss.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS_AND_REBOOT, null);
                        });
                        return false;
                    }).setCancelButton((baseDialog, v) -> false).setOtherButton((baseDialog, v) -> {
                        activity.RestartSiyuanInWebview();
                        return false;
                    }).setCancelTextInfo(new TextInfo().setFontColor(Color.RED));
                }
            } else {
                Intent battery = new Intent("sc.windom.sillot.intent.permission.Battery");
                battery.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(activity, battery, Ss.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, null);
            }
        }
        Log.w("JSAndroid", "requestPermissionActivity()  return true");
        return true; // 返回真表示已经发起申请，不代表结果
    }
    @JavascriptInterface
    public boolean requestPermission(final String id, final String Msg) {
        Log.d("JSAndroid", "requestPermission() invoked");
        if (Utils.isValidPermission(id)) {
            Log.d("JSAndroid", "requestPermission("+id+")  return false");
            return false;
        }
        if (Msg != null && !Msg.isEmpty()) {
            PopTip.show(Msg);
        }
        ActivityCompat.requestPermissions(activity, new String[]{ id }, 1001);
        Log.d("JSAndroid", "requestPermission()  return true");
        return true; // 返回真表示已经发起申请，不代表结果
    }
    @JavascriptInterface
    public void openURLuseDefaultApp(final String url) {
        Uri uri = Uri.parse(url);
        if (uri.getScheme().toLowerCase().startsWith("http")) {
            final Intent i = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(i); // https://developer.android.google.cn/training/app-links/verify-android-applinks?hl=zh-cn
            // 从 Android 12 开始，经过验证的链接现在会自动在相应的应用中打开，以获得更简化、更快速的用户体验。谷歌还更改了未经Android应用链接验证或用户手动批准的链接的默认处理方式。谷歌表示，Android 12将始终在默认浏览器中打开此类未经验证的链接，而不是向您显示应用程序选择对话框。
        }
    }

    @JavascriptInterface
    public void showWifi() {
        Log.d("JSAndroid", "showWifi() invoked");

        if (activity == null) {
            Log.e("JSAndroid", "showWifi() -> Activity is null");
            return;
        }

        Observable<Boolean> locationPermissionObservable = Observable.create(emitter -> {
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                emitter.onNext(true);
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Ss.REQUEST_LOCATION);
            }
        });

        Observable<Boolean> overlayPermissionObservable = Observable.create(emitter -> {
            if (Settings.canDrawOverlays(activity)) {
                emitter.onNext(true);
            } else {
                // 请求悬浮窗权限
                activity.runOnUiThread(() -> {
                    Intent intent = new Intent("sc.windom.sillot.intent.permission.Overlay");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
//                    intent.setData(Uri.parse("package:" + activity.getPackageName()));
                    startActivityForResult(activity, intent, Ss.REQUEST_OVERLAY, null);
                });
            }
        });


        Disposable disposable = Observable.combineLatest(locationPermissionObservable, overlayPermissionObservable, (locationGranted, overlayGranted) -> locationGranted && overlayGranted)
                .filter(granted -> granted) // 过滤掉未授予权限的情况
                .flatMap(granted -> {
                    // 启动悬浮窗服务
                    Intent intent = new Intent(activity, FloatingWindowService.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                    ServiceUtils.startService(intent);
                    return Observable.just(true);
                })
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        granted -> Log.d("JSAndroid", "showWifi() -> Permissions granted and service started."),
                        throwable -> Log.e("JSAndroid", "showWifi() -> Error occurred: " + throwable.getMessage())
                );
    }




    @JavascriptInterface
    public void setMMKV(final String key, final String value) {
        Log.d("JSAndroid", "setMMKV() invoked");
        activity.mmkv.encode(key, value);
    }

    @JavascriptInterface
    public String getMMKV(final String key) {
        Log.d("JSAndroid", "getMMKV() invoked");
        // 出于安全考虑禁止实现
        return "";
    }

    @JavascriptInterface
    public void showBiometricPrompt(final String captcha) {
        Log.d("JSAndroid", "showBiometricPrompt() invoked");
        Handler mainHandler = new Handler(Looper.getMainLooper());
        // 在主线程中执行
        mainHandler.post(() -> {
        // 在 MainActivity 中调用 showBiometricPrompt 方法
        try {
            BiometricHelper.showBiometricPrompt(activity, "指纹解锁", "", "取消", new BiometricHelper.BiometricCallback() {
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
                    String jsCode =
                            """
                                    fetch('/api/system/loginAuth', {
                                                method: 'POST',
                                                body: JSON.stringify({
                                                    authCode: '"""+accessAuthCode+"',"+"""
                                                        captcha: '"""+captcha+"',"+"""
                                                    }),
                                                }).then((response) => {
                                                    return response.json()
                                                }).then((response) => {
                                                    if (0 === response.code) {
                                                        const url = new URL(window.location)
                                                        window.location.href = url.searchParams.get("to") || "/"
                                                        return
                                                    }
                                                                                
                                                    if (response.code === 1) {
                                                        captchaElement.previousElementSibling.src = `/api/system/getCaptcha?v=${new Date().getTime()}`
                                                        captchaElement.parentElement.style.display = 'block'
                                                    } else {
                                                        captchaElement.parentElement.style.display = 'none'
                                                        captchaElement.previousElementSibling.src = ''
                                                    }
                                                                                
                                                    document.querySelector('#message').classList.add('b3-snackbar--show')
                                                    document.querySelector('#message').firstElementChild.textContent = response.msg
                                                    inputElement.value = ''
                                                    captchaElement.value = ''
                                                    inputElement.focus()
                                                    setTimeout(() => {
                                                        document.querySelector('#message').classList.remove('b3-snackbar--show')
                                                        document.querySelector('#message').firstElementChild.textContent = ''
                                                    }, 6000)
                                                })""";
                    Log.d("evaluateJavascript", jsCode);
                    activity.webView.evaluateJavascript(jsCode, null);
                    activity.mmkv.putString("AppCheckInState","unlockScreen");
                }

                @Override
                public void onAuthenticationFailed() {
                    // 认证失败的处理逻辑
                    TipDialog.show("指纹不匹配!", WaitDialog.TYPE.ERROR);
                }

                @Override
                public void onAuthenticationError(CharSequence errString) {
                    // 认证错误的处理逻辑（一般是用户点击了取消）

                    BiometricManager biometricManager = BiometricManager.from(activity);
                    switch (biometricManager.canAuthenticate()) {
                        case BiometricManager.BIOMETRIC_SUCCESS:
                            Log.d("BiometricManager","应用可以进行生物识别技术进行身份验证。");
                            return;
                        case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                            Log.e("BiometricManager","该设备上没有搭载可用的生物特征功能。");
                            PopTip.show("该设备上没有搭载可用的生物特征功能");
                            return;
                        case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                            Log.e("BiometricManager","生物识别功能当前不可用。");
                            PopTip.show("生物识别功能当前不可用");
                            return;
                        case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                            Log.e("BiometricManager","用户没有录入生物识别数据。");
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
        Log.d("JSAndroid", "savePictureByURL() invoked");
        // 下载图片
        Bitmap IMG = BitmapFactory.decodeStream(new URL("http://127.0.0.1:58131"+imageUrl).openConnection().getInputStream());
        // 获取内部存储的 DCIM/Sillot 目录
        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Sillot");
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                Log.e("Sillot_savePictureByURL", "Failed to create directory: " + directory.getAbsolutePath());
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
            Log.i("saveLongScreenshot", "Sillot_savePictureByURL saved to " + file.getAbsolutePath());
            notifyGallery(file);
            PopTip.show("图片已保存到 /DCIM/Sillot");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("saveLongScreenshot", "Failed to save Sillot_savePictureByURL");
        }
    }

    private void notifyGallery(File imageFile) {
//        向系统相册发送媒体文件扫描广播来通知系统相册更新媒体库

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaScannerConnection.scanFile(activity,
                    new String[]{imageFile.toString()}, null,
                    (path, uri) -> {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
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
        Log.d("JSAndroid", "exitSillotAndroid() invoked");
        // 使用runOnUiThread确保在主线程中获取WebView的URL
        activity.runOnUiThread(() -> {
            String webViewUrl = activity.webView.getUrl();
            if (webViewUrl != null && webViewUrl.contains("/check-auth?")) {
                activity.mmkv.putString("AppCheckInState","lockScreen");
                Log.d("JSAndroid", "exitSillotAndroid() AppCheckInState->lockScreen");
            } else {
                activity.mmkv.putString("AppCheckInState","unlockScreen");
                Log.d("JSAndroid", "exitSillotAndroid() AppCheckInState->unlockScreen");
            }
            activity.finishAffinity();
            activity.finishAndRemoveTask();
            System.exit(0);
        });
    }



//    @JavascriptInterface
//    public void restartSillotAndroid() {
//        Log.d("JSAndroid", "restartSillotAndroid() invoked");
//        activity.RestartSiyuanInWebview();
//    }
    @JavascriptInterface
    public void androidReboot() {
        Log.d("JSAndroid", "restartSillotAndroid() invoked");
        runOnUiThread(() -> {
            activity.bootKernel("");
//            activity.showBootIndex();
        });
    }


    //// Sillot extend end

    @JavascriptInterface
    public String getBlockURL() {
        var url = activity.getIntent().getStringExtra("blockURL");
        Log.d("JSAndroid", "getBlockURL() invoked. original url="+url);
        if (url == null) { url = ""; }
        return url;
    }

    @JavascriptInterface
    public String readClipboard() {
        Log.d("JSAndroid", "readClipboard() invoked");
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
        Log.d("JSAndroid", "writeImageClipboard() invoked");
        final ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
        final ClipData clip = ClipData.newUri(activity.getContentResolver(), "Copied img from SiYuan", Uri.parse("http://127.0.0.1:58131/" + uri));
        clipboard.setPrimaryClip(clip);
    }

    @JavascriptInterface
    public void writeClipboard(final String content) {
        Log.d("JSAndroid", "writeClipboard() invoked");
        final ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
        final ClipData clip = ClipData.newPlainText("Copied text from SiYuan", content);
        clipboard.setPrimaryClip(clip);
    }

    @JavascriptInterface
    public void returnDesktop() {
        Log.d("JSAndroid", "returnDesktop() invoked");
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

    @JavascriptInterface
    public void openExternal(String url) {
        Log.d("JSAndroid", "openExternal() invoked");
        if (StringUtils.isEmpty(url)) {
            return;
        }
        Log.d("JSAndroid.openExternal", url);

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
                    Log.e("File Not Found", "File does not exist: " + asset.getAbsolutePath());
                    url = "http://127.0.0.1:58131/" + url;
                } else {
                    Log.d("if (url.startsWith(\"assets/\"))", asset.getAbsolutePath());
                    final Uri uri = FileProvider.getUriForFile(activity.getApplicationContext(), "sc.windom.sillot", asset);
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
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
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
                    Log.e("File Not Found", "File does not exist: " + asset.getAbsolutePath());
                } else {
                    Log.d("if (url.endsWith(\".zip\") && url.startsWith(\"/export/\"))", asset.getAbsolutePath());
                    Uri uri = FileProvider.getUriForFile(activity.getApplicationContext(), "sc.windom.sillot", asset);
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
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        if (url.startsWith("/")) {
            url = "http://127.0.0.1:58131" + url;
        }
        Log.d("openExternal final url ", url);

        final Uri uri = Uri.parse(url);
        final Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
        activity.startActivity(browserIntent); // https://developer.android.google.cn/training/app-links/verify-android-applinks?hl=zh-cn
        // 从 Android 12 开始，经过验证的链接现在会自动在相应的应用中打开，以获得更简化、更快速的用户体验。谷歌还更改了未经Android应用链接验证或用户手动批准的链接的默认处理方式。谷歌表示，Android 12将始终在默认浏览器中打开此类未经验证的链接，而不是向您显示应用程序选择对话框。
    }

    @JavascriptInterface
    public void changeStatusBarColor(final String color, final int appearanceMode) {
        Log.d("JSAndroid", "changeStatusBarColor() invoked");
        activity.runOnUiThread(() -> {
            UltimateBarX.statusBarOnly(activity).transparent().light(appearanceMode == 0).color(parseColor(color)).apply();

            BarUtils.setNavBarLightMode(activity, appearanceMode == 0);
            BarUtils.setNavBarColor(activity, parseColor(color));
        });
    }

    private int parseColor(String str) {
        try {
            str = str.trim();
            if (str.toLowerCase().contains("rgb")) {
                String splitStr = str.substring(str.indexOf('(') + 1, str.indexOf(')'));
                String[] splitString = splitStr.split(",");

                final int[] colorValues = new int[splitString.length];
                for (int i = 0; i < splitString.length; i++) {
                    colorValues[i] = Integer.parseInt(splitString[i].trim());
                }
                return Color.rgb(colorValues[0], colorValues[1], colorValues[2]);
            }
            if (7 > str.length()) {
                // https://stackoverflow.com/questions/10230331/how-to-convert-3-digit-html-hex-colors-to-6-digit-flex-hex-colors
                str = str.replaceAll("#([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])", "#$1$1$2$2$3$3");
            }
            if (9 == str.length() && '#' == str.charAt(0)) {
                // The status bar color on Android is incorrect https://github.com/siyuan-note/siyuan/issues/10278
                // 将 #RRGGBBAA 转换为 #AARRGGBB
                str = "#" + str.substring(7, 9) + str.substring(1, 7);
            }
            return Color.parseColor(str);
        } catch (final Exception e) {
            Utils.LogError("js", "parse color failed", e);
            return Color.parseColor("#212224");
        }
    }


}
