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

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.JavascriptInterface;

import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;

import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.StringUtils;
import com.zackratos.ultimatebarx.ultimatebarx.java.UltimateBarX;

import org.b3log.siyuan.andapi.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Date;

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

    // Sillot extend start
    @JavascriptInterface
    public void requestPermission(final String id) {
        Context mContext = activity.getApplicationContext();
        Toast.INSTANCE.Show(mContext,"注意：后台稳定伺服会消耗额外电量");
        Intent battery = new Intent("sc.windom.sillot.intent.permission.Battery");
        battery.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(battery);
    }

    @JavascriptInterface
    public void setMMKV(final String key, final String value) {
        activity.mmkv.encode(key, value);
    }

    @JavascriptInterface
    public String getMMKV(final String key) {
        // 出于安全考虑禁止实现
//        return activity.mmkv.decodeString(key);
        return "";
    }

    @JavascriptInterface
    public void showBiometricPrompt(final String captcha) {
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
                        android.widget.Toast.makeText(activity, "抱歉出错了 ＞︿＜", android.widget.Toast.LENGTH_LONG).show();
                        return;
                    }
                    Mobile.setBiometricPass(accessAuthCode);

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
                }

                @Override
                public void onAuthenticationFailed() {
                    // 认证失败的处理逻辑
//                    Mobile.setBiometricPass(false);
                }

                @Override
                public void onAuthenticationError(CharSequence errString) {
                    // 认证错误的处理逻辑
//                    Mobile.setBiometricPass(false);
                }

            });

        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        });

    }

    @JavascriptInterface
    public void savePictureByURL(final String imageUrl) throws IOException {
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
            android.widget.Toast.makeText(activity, "图片已保存到 /DCIM/Sillot", android.widget.Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("saveLongScreenshot", "Failed to save Sillot_savePictureByURL");
        }
    }

    private void notifyGallery(File imageFile) {
//        向系统相册发送媒体文件扫描广播来通知系统相册更新媒体库
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(imageFile);
        mediaScanIntent.setData(contentUri);
        activity.sendBroadcast(mediaScanIntent);
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




    // Sillot extend end

    @JavascriptInterface
    public String getBlockURL() {
        return activity.getIntent().getStringExtra("blockURL");
    }

    @JavascriptInterface
    public String readClipboard() {
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
        final ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
        final ClipData clip = ClipData.newUri(activity.getContentResolver(), "Copied img from SiYuan", Uri.parse("http://127.0.0.1:58131/" + uri));
        clipboard.setPrimaryClip(clip);
    }

    @JavascriptInterface
    public void writeClipboard(final String content) {
        final ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
        final ClipData clip = ClipData.newPlainText("Copied text from SiYuan", content);
        clipboard.setPrimaryClip(clip);
    }

    @JavascriptInterface
    public void returnDesktop() {
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

    @JavascriptInterface
    public void exitSillotAndroid() {
        activity.finishAffinity();
        activity.finishAndRemoveTask();
        System.exit(0);
    }

    @JavascriptInterface
    public void openExternal(String url) {
        if (StringUtils.isEmpty(url)) {
            return;
        }

        if (url.startsWith("#")) {
            return;
        }

        if (url.startsWith("assets/")) {
            // Support opening assets through other apps on the Android https://github.com/siyuan-note/siyuan/issues/10657
            final String workspacePath = Mobile.getCurrentWorkspacePath();
            final File asset = new File(workspacePath, "data/" + url);
            Uri uri = FileProvider.getUriForFile(activity.getApplicationContext(), "org.b3log.siyuan", asset);
            final String type = Utils.getMimeType(url);
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

        if (url.startsWith("/")) {
            url = "http://127.0.0.1:58131" + url;
        }

        final Uri uri = Uri.parse(url);
        final Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
        activity.startActivity(browserIntent); // https://developer.android.google.cn/training/app-links/verify-android-applinks?hl=zh-cn
        // 从 Android 12 开始，经过验证的链接现在会自动在相应的应用中打开，以获得更简化、更快速的用户体验。谷歌还更改了未经Android应用链接验证或用户手动批准的链接的默认处理方式。谷歌表示，Android 12将始终在默认浏览器中打开此类未经验证的链接，而不是向您显示应用程序选择对话框。
    }

    @JavascriptInterface
    public void changeStatusBarColor(final String color, final int appearanceMode) {
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
