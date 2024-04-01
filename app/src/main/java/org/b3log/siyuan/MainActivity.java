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

import static org.b3log.siyuan.json.TestMoshiKt.testmoshi;

import org.b3log.siyuan.permission.Ps;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.LocaleList;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.DragEvent;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.view.WindowManager;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.StringUtils;
import com.bumptech.glide.Glide;
import com.kongzue.dialogx.dialogs.PopTip;
import com.tencent.bugly.crashreport.CrashReport;
import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpPost;
import com.koushikdutta.async.http.body.JSONObjectBody;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.util.Charsets;
import com.zackratos.ultimatebarx.ultimatebarx.java.UltimateBarX;

import org.apache.commons.io.FileUtils;
import org.b3log.siyuan.appUtils.HWs;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.b3log.siyuan.realm.TestRealm;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import mobile.Mobile;
import com.tencent.mmkv.MMKV;

/**
 * 主程序.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.0.1, Mar 3, 2024
 * @since 1.0.0
 */
public class MainActivity extends AppCompatActivity implements com.blankj.utilcode.util.Utils.OnAppStatusChangedListener {
    private final String TAG = "MainActivity";
    private AsyncHttpServer server;
    private int serverPort = Ss.DefaultHTTPPort;
    public WebView webView;
    private ImageView bootLogo;
    private ProgressBar bootProgressBar;
    private TextView bootDetailsText;
    private String webViewVer;
    private String userAgent;
    private ValueCallback<Uri[]> uploadMessage;
    private static final int REQUEST_SELECT_FILE = Ss.REQUEST_SELECT_FILE;
    private static final int REQUEST_CAMERA = Ss.REQUEST_CAMERA;
    private long exitTime;
    public MMKV mmkv;
    private String MainActivityLifeState = "";
    private boolean needColdRestart = false;
    private boolean isWebviewReady = false;
    private int works = 0;
    private final HashSet<String> permissionList = new HashSet<>();

    private boolean isFirstRun() {
        final String dataDir = getFilesDir().getAbsolutePath();
        final String appDir = dataDir + "/app";
        final File appDirFile = new File(appDir);
        return !appDirFile.exists();
    }

//    dispatchKeyEvent 是一个更高级的方法，它可以处理所有类型的按键事件，包括按键按下、抬起和长按。
//    dispatchKeyEvent 方法在事件传递给 onKeyDown、onKeyUp 或其他控件之前被调用。
        // REF https://ld246.com/article/1711543259805
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
//            try {
//                throw new Exception(String.valueOf(event.getKeyCode()));
//            } catch (Exception e) {
//                Log.e(TAG, "捕获到异常：" + e.getMessage());
//                App.getInstance().reportException(e);
//            }
            if (event.getKeyCode() == KeyEvent.KEYCODE_ESCAPE) { // getKeyCode 的数字只能拿来和 KeyEvent 里面的对比，不然没有意义
                // 处理ESC键按下事件，并不能阻止输入法对ESC的响应
                Log.e("ESC键被按下",String.valueOf(event.getKeyCode()));
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onNewIntent(final Intent intent) {
        Log.w(TAG, "onNewIntent() involved");
        super.onNewIntent(intent);
        if (null != webView) {
            final String blockURL = intent.getStringExtra("blockURL");
            if (!StringUtils.isEmpty(blockURL)) {
                webView.evaluateJavascript("javascript:window.openFileByURL('" + blockURL + "')", null);
            }
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) { // 只执行一次。在这里设置布局和初始化数据。在大多数情况下，不需要在 onRestart 中做太多事情，因为 onStart 已经处理了活动可见时的初始化。
        Log.w(TAG, "onCreate() involved");
        MainActivityLifeState = "onCreate";
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MMKV.initialize(this);
        mmkv = MMKV.defaultMMKV();



        // 这段代码并不会直接导致高刷率的生效，它只是在获取支持的显示模式中寻找高刷率最大的模式，并将其设置为首选模式。
        Display display = null;
        display = this.getDisplay(); // 等效于 getApplicationContext().getDisplay() 因为Activity已经实现了Context接口，所以用 this 替换
        if (display != null) {
            Display.Mode[] modes = display.getSupportedModes();
            Display.Mode preferredMode = modes[0];
            for (Display.Mode mode : modes) {
                Log.d("MainActivity Display", "supported mode: " + mode.toString());
                if (mode.getRefreshRate() > preferredMode.getRefreshRate() && mode.getPhysicalWidth() >= preferredMode.getPhysicalWidth()) {
                    preferredMode = mode;
                }
            }
            Log.d("MainActivity Display", "preferredMode mode: " + preferredMode.toString());
            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.preferredDisplayModeId = preferredMode.getModeId();
            getWindow().setAttributes(params);
        }

        // 获取OnBackPressedDispatcher
        OnBackPressedDispatcher onBackPressedDispatcher = getOnBackPressedDispatcher();

        // 设置OnBackPressedCallback
        onBackPressedDispatcher.addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 在这里处理后退逻辑
                if (Utils.isPad(getApplicationContext())) {
                    if ((System.currentTimeMillis() - exitTime) > 2000) {
                        PopTip.show("再按一次退出程序");
                        exitTime = System.currentTimeMillis();
                    } else {
                        HWs.getInstance().vibratorWaveform(getApplicationContext(), new long[]{0, 30, 25, 40, 25, 10}, new int[]{2, 4, 3, 2, 2, 2}, -1);
                        exit();
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        System.exit(0);
                    }
                } else {
                    webView.evaluateJavascript("javascript:window.goBack ? window.goBack() : window.history.back()", null);
                }
                HWs.getInstance().vibratorWaveform(getApplicationContext(), new long[]{0, 30, 25, 40, 25}, new int[]{9, 2, 1, 7, 2}, -1);
            }
        });


        // 启动 HTTP Server
        Log.w(TAG, "onStart() -> startHttpServer() involved");
        startHttpServer();

        // 初始化 UI 元素
        Log.w(TAG, "onStart() -> initUIElements() involved");
        initUIElements();

        // 拉起内核
        Log.w(TAG, "onStart() -> startKernel() involved");
        startKernel();

        // 初始化外观资源
        Log.w(TAG, "onStart() -> initAppearance() involved");
        initAppearance();

        AppUtils.registerAppStatusChangedListener(this);

        // 使用 Chromium 调试 WebView
        // WebView.setWebContentsDebuggingEnabled(true);

        // 注册工具栏显示/隐藏跟随软键盘状态
        // Fix https://github.com/siyuan-note/siyuan/issues/9765
        Utils.registerSoftKeyboardToolbar(this, webView);

        // 沉浸式状态栏设置
        UltimateBarX.statusBarOnly(this).transparent().light(false).color(Color.parseColor("#1e1e1e")).apply();
        ((ViewGroup) webView.getParent()).setPadding(0, UltimateBarX.getStatusBarHeight(), 0, 0);

        // Fix https://github.com/siyuan-note/siyuan/issues/9726
        // KeyboardUtils.fixAndroidBug5497(this);
        AndroidBug5497Workaround.assistActivity(this);



        HashSet<String> permissionsToCheck = new HashSet<>(Ps.PG_Core); // 核心权限组，每次启动都要检查
        if (Build.VERSION.SDK_INT >= 33) {
            permissionsToCheck.addAll(Ps.useAPI33);
        }
        for (String permission : permissionsToCheck) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(permission);
            } else {
                Log.d(TAG, "onCreate() -> "+permission+" granted [Ps.PG_Core]");
            }
        }


        if (isFirstRun()) {
            permissionsToCheck.addAll(Ps.PG_unCore); // 非核心权限组，仅安装后首次启动集中申请
            for (String permission : permissionsToCheck) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionList.add(permission);
                } else {
                    Log.d(TAG, "onCreate() -> "+permission+" granted [Ps.PG_unCore]");
                }
            }
        }

        works += permissionList.size();
        doPermissionApply();

        // 单独的授权界面，暂时淘汰
//        Intent InitActivity = new Intent(this, org.b3log.siyuan.permission.InitActivity.class);
//        InitActivity.putExtra("contentViewId", R.layout.init_activity);
//        InitActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(InitActivity);
    }


    private void initUIElements() {
        bootLogo = findViewById(R.id.bootLogo);
        bootProgressBar = findViewById(R.id.progressBar);
        bootDetailsText = findViewById(R.id.bootDetails);
        webView = findViewById(R.id.webView);
        webView.setBackgroundColor(Color.parseColor("#1e1e1e"));
        webView.setWebViewClient(new WebViewClient() { // setWebViewClient 和 setWebChromeClient 并不同，别看走眼了
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                // 在加载页面出现错误时进行处理
                if (error != null) {
                    Log.e("WebViewClient", "onReceivedError: " + error.getDescription());
                }
            }
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                // 页面开始加载时调用
                Log.d("WebViewClient", "onPageStarted: " + url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                // 页面加载完成时调用
                Log.d("WebViewClient", "onPageFinished: " + url);
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(final WebView mWebView, final ValueCallback<Uri[]> filePathCallback, final FileChooserParams fileChooserParams) {
                if (uploadMessage != null) {
                    uploadMessage.onReceiveValue(null);
                }

                uploadMessage = filePathCallback;

                if (fileChooserParams.isCaptureEnabled()) {
                    if (Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
                        // 不支持 Android 10 以下
                        PopTip.show("Capture is not supported on your device (Android 10+ required)");
                        uploadMessage = null;
                        return false;
                    }

                    final String[] permissions = {android.Manifest.permission.CAMERA};
                    if (!hasPermissions(permissions)) {
                        ActivityCompat.requestPermissions(MainActivity.this, permissions, REQUEST_CAMERA);
                        return true;
                    }

                    openCamera();
                    return true;
                }

                final Intent intent = fileChooserParams.createIntent();
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                try {
                    startActivityForResult(intent, REQUEST_SELECT_FILE);
                } catch (final Exception e) {
                    uploadMessage = null;
                    PopTip.show("Cannot open file chooser");
                    return false;
                }
                return true;
            }

            @Override
            public void onPermissionRequest(final PermissionRequest request) { // 当网页请求其他权限时，会回调此方法以询问用户是否允许
                request.grant(request.getResources());
            }

            @Override
            public void onProgressChanged(WebView webView, int progress) { // 当 WebView 加载页面时，会多次回调此方法以报告加载进度
                // 增加Javascript异常监控
                CrashReport.setJavascriptMonitor(webView, true);
                super.onProgressChanged(webView, progress);
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                Date date = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
                String formattedDate = sdf.format(date);

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("onJsAlert from WebView")
                        .setMessage("\n--------------------------------------------\n" + message + "\n--------------------------------------------\n\n* " + view.getTitle() + "\n* " + formattedDate)
                        .setPositiveButton("OK", (dialog, which) -> result.confirm())
                        .setCancelable(false)
                        .show();
                return true;
            }

        });

        webView.setOnDragListener((v, event) -> {
            // 禁用拖拽 https://github.com/siyuan-note/siyuan/issues/6436
            return DragEvent.ACTION_DRAG_ENDED != event.getAction();
        });

        final WebSettings ws = webView.getSettings();
        checkWebViewVer(ws);
        userAgent = ws.getUserAgentString();
    }

    @SuppressLint("SetJavaScriptEnabled")
    void showBootIndex() {
        Log.w(TAG, "showBootIndex() involved");
        webView.setVisibility(View.VISIBLE);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(final WebView view, final WebResourceRequest request) {
                final Uri uri = request.getUrl();
                final String url = uri.toString();
                Log.w(TAG, "showBootIndex() -> [WebViewClient] shouldOverrideUrlLoading <- "+url);
                if (url.contains("127.0.0.1")) {
                    var AppCheckInState = mmkv.getString("AppCheckInState", "");
                    if (AppCheckInState.equals("lockScreen")) {
                        try {
                            String encodedUrl = URLEncoder.encode(url, "UTF-8");
                            String gotourl = "http://127.0.0.1:58131/check-auth?to=" + encodedUrl;
                            Log.w(TAG,"showBootIndex() -> [WebViewClient] shouldOverrideUrlLoading -> " + gotourl);
                            view.loadUrl(gotourl);
                        } catch (UnsupportedEncodingException e) {
                            // 编码失败的处理
                            e.printStackTrace();
                        }
                    } else {
                        view.loadUrl(url);
                    }
                    return true;
                }

                if (url.contains("siyuan://api/system/exit")) {
                    exit();
                    return true;
                }
                if (url.contains("siyuan://androidRestartSiYuan")) {
//                    sleep(2000);
                    coldRestart();
                    return false;
                }

                if (uri.getScheme().toLowerCase().startsWith("http")) {
                    final Intent i = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(i); // https://developer.android.google.cn/training/app-links/verify-android-applinks?hl=zh-cn
                    // 从 Android 12 开始，经过验证的链接现在会自动在相应的应用中打开，以获得更简化、更快速的用户体验。谷歌还更改了未经Android应用链接验证或用户手动批准的链接的默认处理方式。谷歌表示，Android 12将始终在默认浏览器中打开此类未经验证的链接，而不是向您显示应用程序选择对话框。
                    return true;
                }
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                // 页面加载完成时调用
                Log.d("WebViewClient", "onPageFinished: " + url);
                bootLogo.postDelayed(() -> {
                    bootLogo.setVisibility(View.GONE);
                    bootProgressBar.setVisibility(View.GONE);
                    bootDetailsText.setVisibility(View.GONE);
                    bootLogo.setVisibility(View.GONE);
                }, 186);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                // 在加载页面出现错误时进行处理
                if (error != null) {
                    Log.e("WebViewClient", "onReceivedError: " + error.getDescription());
                }
            }
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                // 页面开始加载时调用
                Log.d("WebViewClient", "onPageStarted: " + url);
            }

        });

        final JSAndroid JSAndroid = new JSAndroid(this);
        webView.addJavascriptInterface(JSAndroid, "JSAndroid");
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        final WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setCacheMode(WebSettings.LOAD_NO_CACHE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ws.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        ws.setTextZoom(100);
        ws.setUseWideViewPort(true);
        ws.setLoadWithOverviewMode(true);
        ws.setUserAgentString("SiYuan-Sillot/" + Utils.version + " https://b3log.org/siyuan Android " + ws.getUserAgentString());

        waitFotKernelHttpServing();
        WebView.setWebContentsDebuggingEnabled(true);
        webView.loadUrl("http://127.0.0.1:58131/appearance/boot/index.html?v=" + Utils.version);

        new Thread(this::keepLive).start();
        isWebviewReady = true;
    }

    private Handler bootHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(final Message msg) {
            final String cmd = msg.getData().getString("cmd");
            if ("startKernel".equals(cmd)) {
                bootKernel();
            } else {
                showBootIndex();
            }
        }
    };

    private void startHttpServer() {
        if (null != server) {
            server.stop();
            Log.w(TAG, "startHttpServer() stop exist server");
        }

        try {
            // 解决乱码问题 https://github.com/koush/AndroidAsync/issues/656#issuecomment-523325452
            final Class<Charsets> charsetClass = Charsets.class;
            Field usAscii = charsetClass.getDeclaredField("US_ASCII");
            usAscii.setAccessible(true);
            usAscii.set(Charsets.class, Charsets.UTF_8);
        } catch (final Exception e) {
            Utils.LogError("http", "init charset failed", e);
        }

        server = new AsyncHttpServer();
        server.post("/api/walkDir", (request, response) -> {
            try {
                final long start = System.currentTimeMillis();
                final JSONObject requestJSON = (JSONObject) request.getBody().get();
                final String dir = requestJSON.optString("dir");
                final JSONObject data = new JSONObject();
                final JSONArray files = new JSONArray();
                FileUtils.listFilesAndDirs(new File(dir), TrueFileFilter.INSTANCE, DirectoryFileFilter.DIRECTORY).forEach(file -> {
                    final String path = file.getAbsolutePath();
                    final JSONObject info = new JSONObject();
                    try {
                        info.put("path", path);
                        info.put("name", file.getName());
                        info.put("size", file.length());
                        info.put("updated", file.lastModified());
                        info.put("isDir", file.isDirectory());
                    } catch (final Exception e) {
                        Utils.LogError("http", "walk dir failed", e);
                    }
                    files.put(info);
                });
                data.put("files", files);
                final JSONObject responseJSON = new JSONObject().put("code", 0).put("msg", "").put("data", data);
                response.send(responseJSON);
                Utils.LogInfo("http", "walk dir [" + dir + "] in [" + (System.currentTimeMillis() - start) + "] ms");
            } catch (final Exception e) {
                Utils.LogError("http", "walk dir failed", e);
                try {
                    response.send(new JSONObject().put("code", -1).put("msg", e.getMessage()));
                } catch (final Exception e2) {
                    Utils.LogError("http", "walk dir failed", e2);
                }
            }
        });

        serverPort = getAvailablePort();
        final AsyncServer s = AsyncServer.getDefault();
        // 生产环境绑定 ipv6 回环地址 [::1] 以防止被远程访问
        s.listen(InetAddress.getLoopbackAddress(), serverPort, server.getListenCallback());
        // 开发环境绑定所有网卡以便调试
//        s.listen(null, serverPort, server.getListenCallback());
        Log.w(TAG, "startHttpServer() -> HTTP server is listening on port [" + serverPort + "]");
        Utils.LogInfo("http", "HTTP server is listening on port [" + serverPort + "]");
    }

    private int getAvailablePort() {
        int ret = 6906;
        try {
            ServerSocket s = new ServerSocket(0);
            ret = s.getLocalPort();
            s.close();
        } catch (final Exception e) {
            Utils.LogError("http", "get available port failed", e);
        }
        return ret;
    }

    private void startKernel() {
        final Bundle b = new Bundle();
        b.putString("cmd", "startKernel");
        final Message msg = new Message();
        msg.setData(b);
        bootHandler.sendMessage(msg);
    }

    private void bootKernel() {
        Mobile.setHttpServerPort(serverPort);
        if (Mobile.isHttpServing()) {
            Utils.LogInfo("boot", "kernel HTTP server is running");
            showBootIndex();
            return;
        }
        startHttpServer();
        final String appDir = getFilesDir().getAbsolutePath() + "/app";
//        final Locale locale = getResources().getConfiguration().locale;
        // As of API 24 (Nougat) and later
        LocaleList locales = getResources().getConfiguration().getLocales();
        // Now you can access the first locale in the list as follows:
        Locale locale = locales.get(0);
        final String workspaceBaseDir = getExternalFilesDir(null).getAbsolutePath();
        final String timezone = TimeZone.getDefault().getID();
        new Thread(() -> {
            final String localIPs = Utils.getIPAddressList();
            String lang = locale.getLanguage() + "_" + locale.getCountry();
            if (lang.toLowerCase().contains("cn")) {
                lang = "zh_CN";
            } else {
                lang = "en_US";
            }

            Mobile.startKernel("android", appDir, workspaceBaseDir, timezone, localIPs, lang,
                    Build.VERSION.RELEASE +
                            "/SDK " + Build.VERSION.SDK_INT +
                            "/WebView " + webViewVer +
                            "/Manufacturer " + android.os.Build.MANUFACTURER +
                            "/Brand " + android.os.Build.BRAND +
                            "/UA " + userAgent);
        }).start();

        final Bundle b = new Bundle();
        b.putString("cmd", "bootIndex");
        final Message msg = new Message();
        msg.setData(b);
        bootHandler.sendMessage(msg);
    }
    void bootKernel(String i) {
        Mobile.setHttpServerPort(serverPort);
        if (Mobile.isHttpServing()) {
            Log.w(TAG, "bootKernel(i) Mobile.isHttpServing , not need showBootIndex()");
            webView.evaluateJavascript("javascript:window.reconnectWebSocket()", null);
//            showBootIndex();
            return;
        }
        final String appDir = getFilesDir().getAbsolutePath() + "/app";
//        final Locale locale = getResources().getConfiguration().locale;
        // As of API 24 (Nougat) and later
        LocaleList locales = getResources().getConfiguration().getLocales();
        // Now you can access the first locale in the list as follows:
        Locale locale = locales.get(0);
        final String workspaceBaseDir = getExternalFilesDir(null).getAbsolutePath();
        final String timezone = TimeZone.getDefault().getID();
        new Thread(() -> {
            final String localIPs = Utils.getIPAddressList();
            String lang = locale.getLanguage() + "_" + locale.getCountry();
            if (lang.toLowerCase().contains("cn")) {
                lang = "zh_CN";
            } else {
                lang = "en_US";
            }

            Mobile.startKernelFast("android", appDir, workspaceBaseDir, localIPs);
//            Mobile.startKernel("android", appDir, workspaceBaseDir, timezone, localIPs, lang,
//                    Build.VERSION.RELEASE +
//                            "/SDK " + Build.VERSION.SDK_INT +
//                            "/WebView " + webViewVer +
//                            "/Manufacturer " + android.os.Build.MANUFACTURER +
//                            "/Brand " + android.os.Build.BRAND +
//                            "/UA " + userAgent);
        }).start();
//        initUIElements();
//        if (null != webView) {
//            webView.evaluateJavascript("javascript:window.reconnectWebSocket()", null);
//        }
    }

    /**
     * 通知栏保活。
     */
    private void keepLive() {
        while (true) {
            try {
                final Intent intent = new Intent(MainActivity.this, KeepLiveService.class);
                ContextCompat.startForegroundService(this, intent);
                sleep(31 * 1000);
                stopService(intent);
            } catch (final Throwable t) {
            }
        }
    }

    /**
     * 等待内核 HTTP 服务伺服。
     */
    private void waitFotKernelHttpServing() {
        while (true) {
            sleep(10);
            if (Mobile.isHttpServing()) {
                break;
            }
        }
    }

    private void initAppearance() {
        if (needUnzipAssets()) {
            bootLogo.setVisibility(View.VISIBLE);
            // 不要进度条更平滑一些
            //bootProgressBar.setVisibility(View.VISIBLE);
            //bootDetailsText.setVisibility(View.VISIBLE);

            final String dataDir = getFilesDir().getAbsolutePath();
            final String appDir = dataDir + "/app";
            final File appVerFile = new File(appDir, "VERSION");

            setBootProgress("Clearing appearance...", 20);
            try {
                FileUtils.deleteDirectory(new File(appDir));
            } catch (final Exception e) {
                Utils.LogError("boot", "delete dir [" + appDir + "] failed, exit application", e);
                exit();
                return;
            }

            setBootProgress("Initializing appearance...", 60);
            Utils.unzipAsset(getAssets(), "app.zip", appDir + "/app");

            try {
                FileUtils.writeStringToFile(appVerFile, Utils.version, StandardCharsets.UTF_8);
            } catch (final Exception e) {
                Utils.LogError("boot", "write version failed", e);
            }

            setBootProgress("Booting kernel...", 80);
        }

    }

    private void setBootProgress(final String text, final int progressPercent) {
        runOnUiThread(() -> {
            bootDetailsText.setText(text);
            bootProgressBar.setProgress(progressPercent);
        });
    }

    private void sleep(final long time) {
        try {
            Thread.sleep(time);
        } catch (final Exception e) {
            Utils.LogError("runtime", "sleep failed", e);
        }
    }

    // 用于保存拍照图片的 uri
    private Uri mCameraUri;

    private boolean hasPermissions(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.w(TAG, "onRequestPermissionsResult() -> requestCode "+requestCode+"  grantResults[0] ");
        if (grantResults.length > 0) {
            Log.w(TAG, "onRequestPermissionsResult() -> requestCode "+requestCode+"  grantResults[0] "+grantResults[0]);
        }
        if (requestCode == REQUEST_CAMERA) {
            // 请求码应该在整个应用中是全局唯一的，但是处理权限请求结果应该是在申请权限的活动中进行。
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
                return;
            }

            PopTip.show("Permission denied");
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }



    private void doPermissionApply() {
        if (!permissionList.isEmpty()) {
            ActivityResultLauncher<String[]> requestPermissionLauncher = registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    permissions -> {
                        boolean allPermissionsGranted = true;
                        for (String permission : permissions.keySet()) {
                            if (permissions.get(permission) != null && Boolean.TRUE.equals(permissions.get(permission))) {
                                works--;
                            } else {
                                allPermissionsGranted = false;
                            }
                        }
                        if (works == 0 && allPermissionsGranted) {
                            // 所有权限都得到了允许，执行相应操作
                        } else {
                            // 处理未被允许的权限
                        }
                    });

            String[] permissionsToRequest = permissionList.toArray(new String[0]);
            if (shouldShowRequestPermissionRationale(permissionsToRequest)) {
                // 显示权限说明
                PopTip.show("必要权限缺失，请处理！");
                // 打开应用详情界面
                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            } else {
                requestPermissionLauncher.launch(permissionsToRequest);
            }
        } else {
            // 处理无权限的情况，例如提醒用户或者直接返回
        }
    }

    private boolean shouldShowRequestPermissionRationale(String[] permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                return true;
            }
        }
        return false;
    }


    private void openCamera() {
        final Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (captureIntent.resolveActivity(getPackageManager()) != null) {
            final Uri photoUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
            mCameraUri = photoUri;
            if (photoUri != null) {
                captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                captureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                startActivityForResult(captureIntent, REQUEST_CAMERA);
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Log.w(TAG, "onActivityResult() -> requestCode "+requestCode+"  resultCode "+resultCode);
        // 检查返回结果是否是权限请求的结果，不管 resultCode 是什么都要重启的
        if (requestCode == Ss.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS_AND_REBOOT && webView != null) {
//            needColdRestart = true;
            RestartSiyuanInWebview();
        }
        if (null == uploadMessage) {
            super.onActivityResult(requestCode, resultCode, intent);
            return;
        }

        if (requestCode == REQUEST_CAMERA) {
            if (RESULT_OK != resultCode) {
                uploadMessage.onReceiveValue(null);
                uploadMessage = null;
                return;
            }

            uploadMessage.onReceiveValue(new Uri[]{mCameraUri});
        } else if (requestCode == REQUEST_SELECT_FILE) {
            // 以下代码参考自 https://github.com/mgks/os-fileup/blob/master/app/src/main/java/mgks/os/fileup/MainActivity.java MIT license

            Uri[] results = null;
            ClipData clipData;
            String stringData;
            try {
                clipData = intent.getClipData();
                stringData = intent.getDataString();
            } catch (Exception e) {
                clipData = null;
                stringData = null;
            }

            if (clipData != null) {
                final int numSelectedFiles = clipData.getItemCount();
                results = new Uri[numSelectedFiles];
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    results[i] = clipData.getItemAt(i).getUri();
                }
            } else {
                try {
                    Bitmap cam_photo = (Bitmap) intent.getExtras().get("data");
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    cam_photo.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                    stringData = MediaStore.Images.Media.insertImage(this.getContentResolver(), cam_photo, null, null);
                } catch (Exception ignored) {
                }

                if (!StringUtils.isEmpty(stringData)) {
                    results = new Uri[]{Uri.parse(stringData)};
                }
            }

            uploadMessage.onReceiveValue(results);
        }

        uploadMessage = null;
        super.onActivityResult(requestCode, resultCode, intent);
    }

    private boolean needUnzipAssets() {
        final String dataDir = getFilesDir().getAbsolutePath();
        final String appDir = dataDir + "/app";
        final File appDirFile = new File(appDir);
        appDirFile.mkdirs();

        boolean ret = true;
        final File appVerFile = new File(appDir, "VERSION");
        if (appVerFile.exists()) {
            try {
                final String ver = FileUtils.readFileToString(appVerFile, StandardCharsets.UTF_8);
                ret = !ver.equals(Utils.version);
            } catch (final Exception e) {
                Utils.LogError("boot", "check version failed", e);
            }
        }
        return ret;
    }

    @Override
    protected void onDestroy() {
        Log.w(TAG, "onDestroy() involved");
        MainActivityLifeState = "onDestroy";
        super.onDestroy();
        KeyboardUtils.unregisterSoftInputChangedListener(getWindow());
        AppUtils.unregisterAppStatusChangedListener(this);
        if (null != webView) {
            webView.removeAllViews();
            webView.destroy();
        }
        if (null != server) {
            server.stop();
        }
    }

    @Override
    public void onForeground(Activity activity) {
        Log.w(TAG, "onForeground() involved");
        MainActivityLifeState = "onForeground";
        startSyncData();
        if (null != webView) {
            webView.evaluateJavascript("javascript:window.reconnectWebSocket()", null);
        }
    }

    @Override
    public void onBackground(Activity activity) {
        Log.w(TAG, "onBackground() involved");
        MainActivityLifeState = "onBackground";
        startSyncData();
    }

    @Override
    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        super.onMultiWindowModeChanged(isInMultiWindowMode);
        Log.w(TAG, "onMultiWindowModeChanged() involved");
    }

    @Override
    protected void onStart() { // 当活动变得对用户可见时，系统会调用这个方法。这是在活动即将进入前台并且用户可以看到它时进行最后准备的地方。在这里进行用户可见时的初始化，比如开始动画、注册广播接收器等。
        super.onStart();
        MainActivityLifeState = "onStart";

        Log.w(TAG, "onStart() -> canPopInBackground "+Utils.canPopInBackground(this));
        Log.w(TAG, "onStart() -> canShowOnTop "+Utils.canShowOnTop(this));
        Log.w(TAG, "onStart() -> isShowingOnLockScreen "+Utils.isShowingOnLockScreen(this));
        Log.w(TAG, "onStart() -> canManageAllFiles "+Utils.canManageAllFiles(this));
        Log.w(TAG, "onStart() -> canAccessDeviceState "+Utils.canAccessDeviceState(this));
        Log.w(TAG, "onStart() -> canRequestPackageInstalls "+Utils.canRequestPackageInstalls(this));


    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.w(TAG, "onStop() involved");
        MainActivityLifeState = "onStop";
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.w(TAG, "onResume() involved");
        MainActivityLifeState = "onResume";
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.w(TAG, "onPause() involved");
        MainActivityLifeState = "onPause";
    }

    @Override
    protected void onRestart() { // 当活动重新启动时调用（一般是onStop后）。在这里恢复活动之前的状态，比如重新获取数据、恢复界面状态等。
        super.onRestart();
        Log.w(TAG, "onRestart() involved");
        MainActivityLifeState = "onRestart";
    }

    public void exit() {
        finishAffinity();
        finishAndRemoveTask();
    }

    public void coldRestart() {
        finishAffinity(); //  这个方法用于结束当前活动所在的亲和性任务中的所有活动。亲和性任务是指具有相同 taskAffinity 属性的一组活动。当调用 finishAffinity() 时，系统会结束当前活动所在任务中所有与当前活动具有相同 taskAffinity 的活动，但不会结束其他任务中的活动。
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
//        finishAndRemoveTask(); //  这个方法用于结束当前活动，并从任务栈中移除整个任务。这意味着，当前活动所在的任务中所有的活动都会被结束，并且任务本身也会被移除。如果这个任务是最顶层的任务，那么用户将返回到主屏幕。
       android.os.Process.killProcess(android.os.Process.myPid()); // 暂时无法解决杀死其他任务栈的冲突，不加这句重启活动会崩溃
    }

    public void RestartSiyuanInWebview() {
        if (webView != null) {
            webView.evaluateJavascript("javascript:window.Sillot.androidRestartSiYuan();", null);
            // 接下来由 webview 发起 "siyuan://androidRestartSiYuan"
        }
    }


    private void checkWebViewVer(final WebSettings ws) {
        // Android check WebView version 75+ https://github.com/siyuan-note/siyuan/issues/7840
        final String ua = ws.getUserAgentString();
        if (ua.contains("Chrome/")) {
            final int minVer = 95;
            try {
                final String chromeVersion = ua.split("Chrome/")[1].split(" ")[0];
                if (chromeVersion.contains(".")) {
                    final String[] chromeVersionParts = chromeVersion.split("\\.");
                    webViewVer = chromeVersionParts[0];
                    if (Integer.parseInt(webViewVer) < minVer) {
                        PopTip.show("WebView version " + webViewVer + " is too low, please upgrade to " + minVer + "+");
                    }
                }
            } catch (final Exception e) {
                Utils.LogError("boot", "check webview version failed", e);
                PopTip.show("Check WebView version failed: " + e.getMessage());
            }
        }
    }

    private static boolean syncing;

    public static void startSyncData() {
        new Thread(MainActivity::syncData).start();
    }

    public static void syncData() {
        try {
            if (syncing) {
                Log.i("sync", "data is syncing...");
                return;
            }
            syncing = true;

            final AsyncHttpPost req = new com.koushikdutta.async.http.AsyncHttpPost("http://127.0.0.1:58131/api/sync/performSync");
            req.setBody(new JSONObjectBody(new JSONObject().put("mobileSwitch", true)));
            AsyncHttpClient.getDefaultInstance().executeJSONObject(req,
                    new com.koushikdutta.async.http.AsyncHttpClient.JSONObjectCallback() {
                        @Override
                        public void onCompleted(Exception e, com.koushikdutta.async.http.AsyncHttpResponse source, JSONObject result) {
                            if (null != e) {
                                Utils.LogError("sync", "data sync failed", e);
                            }
                        }
                    });
        } catch (final Throwable e) {
            Utils.LogError("sync", "data sync failed", e);
        } finally {
            syncing = false;
        }
    }
}
