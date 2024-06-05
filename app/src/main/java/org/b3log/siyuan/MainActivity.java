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

 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.ClipData;
 import android.content.ClipboardManager;
 import android.content.ComponentName;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.pm.PackageManager;
 import android.content.res.Configuration;
 import android.graphics.Bitmap;
 import android.graphics.Color;
 import android.net.Uri;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.view.Display;
 import android.view.DragEvent;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.webkit.CookieManager;
 import android.webkit.JsResult;
 import android.webkit.PermissionRequest;
 import android.webkit.ValueCallback;
 import android.webkit.WebChromeClient;
 import android.webkit.WebResourceError;
 import android.webkit.WebResourceRequest;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;

 import androidx.activity.OnBackPressedCallback;
 import androidx.activity.OnBackPressedDispatcher;
 import androidx.activity.result.ActivityResultLauncher;
 import androidx.activity.result.contract.ActivityResultContracts;
 import androidx.annotation.NonNull;
 import androidx.appcompat.app.AlertDialog;
 import androidx.appcompat.app.AppCompatActivity;
 import androidx.core.app.ActivityCompat;
 import androidx.core.content.ContextCompat;
 import androidx.work.Constraints;
 import androidx.work.NetworkType;
 import androidx.work.OneTimeWorkRequest;
 import androidx.work.WorkManager;

 import com.blankj.utilcode.util.AppUtils;
 import com.blankj.utilcode.util.KeyboardUtils;
 import com.blankj.utilcode.util.ServiceUtils;
 import com.blankj.utilcode.util.StringUtils;
 import com.kongzue.dialogx.dialogs.BottomMenu;
 import com.kongzue.dialogx.dialogs.PopNotification;
 import com.kongzue.dialogx.dialogs.PopTip;
 import com.kongzue.dialogx.interfaces.OnMenuItemClickListener;
 import com.tencent.bugly.crashreport.CrashReport;
 import com.tencent.mmkv.MMKV;
 import com.zackratos.ultimatebarx.ultimatebarx.java.UltimateBarX;

 import org.b3log.siyuan.appUtils.HWs;
 import org.b3log.siyuan.services.BootService;
 import org.b3log.siyuan.services.FloatingWindowService;
 import org.b3log.siyuan.workers.SyncDataWorker;
 import org.greenrobot.eventbus.EventBus;
 import org.greenrobot.eventbus.Subscribe;
 import org.greenrobot.eventbus.ThreadMode;

 import java.io.ByteArrayOutputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Locale;
 import java.util.UUID;
 import java.util.concurrent.atomic.AtomicReference;

 import mobile.Mobile;
 import sc.windom.sofill.S;
 import sc.windom.sofill.android.permission.Ps;
 import sc.windom.sofill.android.webview.WebViewPool;

 /**
 * 主程序.
 *
 * @author <a href="https://88250.b3log.org">Liang Ding</a>
 * @version 1.1.0.3, Apr 24, 2024
 * @since 1.0.0
 */
public class MainActivity extends AppCompatActivity implements com.blankj.utilcode.util.Utils.OnAppStatusChangedListener {
    private final String TAG = "MainActivity-SiYuan";
    public WebView webView;
    private FrameLayout webViewContainer;
    private ImageView bootLogo;
    private ProgressBar bootProgressBar;
    private TextView bootDetailsText;
    private ValueCallback<Uri[]> uploadMessage;
    private static final int REQUEST_SELECT_FILE = S.REQUEST_SELECT_FILE;
    private static final int REQUEST_CAMERA = S.REQUEST_CAMERA;
    private long exitTime;
    public MMKV mmkv;
    private int works = 0;
    private final HashSet<String> permissionList = new HashSet<>();

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
        Log.w(TAG, "onNewIntent() invoked");
        super.onNewIntent(intent);
        if (null != webView) {
            final String blockURL = intent.getStringExtra("blockURL");
            if (!StringUtils.isEmpty(blockURL)) {
                webView.evaluateJavascript("javascript:window.openFileByURL('" + blockURL + "')", null);
            }
        }
    }
    public void sendEmail(String recipient, String subject, String body) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:")); // only email apps should handle this

        // 设置收件人
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{recipient});
        // 设置邮件主题
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        // 设置邮件正文
        emailIntent.putExtra(Intent.EXTRA_TEXT, body);

        if (emailIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(emailIntent);
        } else {
            PopTip.show("No email client found");
        }
    }
    public void launchQQAndCopyToClipboard(String qqNumber) {
        // 将QQ号复制到剪贴板
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", qqNumber);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(this, "QQ 号已复制", Toast.LENGTH_SHORT).show();

        Intent intent = getPackageManager().getLaunchIntentForPackage("com.tencent.mobileqq");

        if (intent != null) {
            startActivity(intent);
        } else {
            PopTip.show("QQ 未安装");
        }
    }
    public void launchTikTopAndCopyToClipboard(String TTA) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", TTA);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "抖音号已复制", Toast.LENGTH_SHORT).show();

        Intent intent = getPackageManager().getLaunchIntentForPackage("com.ss.android.ugc.aweme");

        if (intent != null) {
            startActivity(intent);
        } else {
            PopTip.show("抖音未安装");
        }
    }

    private void androidFeedback() {
        String[] menuOptions = {
                "电子邮件",
                "QQ",
                "抖音"
        };
        BottomMenu.show(menuOptions)
                .setMessage("请选择反馈渠道")
                .setOnMenuItemClickListener((OnMenuItemClickListener<BottomMenu>) (dialog, text, index) -> {
                    if (text.equals("电子邮件")) {
                        sendEmail("694357845@qq.com", "汐洛安卓反馈", Utils.getDeviceInfoString());
                    } else if (text.equals("QQ")) {
                        launchQQAndCopyToClipboard("694357845");
                    } else if (text.equals("抖音")) {
                        launchTikTopAndCopyToClipboard("AsyncTTk");
                    }
                    return false;
                });
    }

    public BootService bootService;
    private boolean serviceBound = false;
    private String instanceId; // 用于区分不同实例的ID

    final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.i(TAG, "onServiceConnected invoked");
            BootService.LocalBinder binder = (BootService.LocalBinder) service;
            bootService = binder.getService();
            serviceBound = true;
            App.bootService = bootService;
            // 服务绑定后，执行依赖于bootService的代码
            performActionWithService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.i(TAG, "onServiceDisconnected invoked");
            serviceBound = false;
            bootService.setKernelStarted(false);
            bootService.stopSelf();
            bootService = null;
            App.bootService = null;
            releaseBootService();
        }
    };

    void releaseBootService() {
        Log.i(TAG, "releaseBootService invoked");
        // 销毁WebView并从池中移除
        if (webViewWrapper != null) {
            ViewGroup parent = (ViewGroup) webView.getParent();
            parent.removeView(webView); // 从原来的容器中移除WebView
            WebViewPool webViewPool = WebViewPool.getInstance();
            webViewPool.releaseWebView(webView);
            webViewWrapper.destroy();
        }
    }

    void bindBootService() {
        if (bootService == null){
            // 生成一个唯一的instanceId
            instanceId = UUID.randomUUID().toString();
            // 绑定服务时传递instanceId
            Intent intent = new Intent(getApplicationContext(), BootService.class);
            intent.putExtra("INSTANCE_ID", instanceId);
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE); // TODO: 双开共存时内核固定端口冲突
        } else {
            performActionWithService();
        }
    }

    // 在这里执行依赖于bootService的代码
    private void performActionWithService() {
        Log.w(TAG, "performActionWithService invoked");
        if (serviceBound && bootService != null) {
            bootService.showWifi(this);
            // 初始化 UI 元素
            Log.w(TAG, "performActionWithService() -> initUIElements() invoked");
            initUIElements();

            AppUtils.registerAppStatusChangedListener(this);

            // 使用 Chromium 调试 WebView
            if (Utils.isDebugPackageAndMode(this)) {
                WebView.setWebContentsDebuggingEnabled(true);
            }

            // 注册工具栏显示/隐藏跟随软键盘状态
            // Fix https://github.com/siyuan-note/siyuan/issues/9765
            Utils.registerSoftKeyboardToolbar(this, webView);

            // 沉浸式状态栏设置
            UltimateBarX.statusBarOnly(this).transparent().light(false).color(Color.parseColor(S.ColorStringHex.INSTANCE.getBgColor_light())).apply();
            if (webView != null) {
                ((ViewGroup) webView.getParent()).setPadding(0, UltimateBarX.getStatusBarHeight(), 0, 0);
            }

            // Fix https://github.com/siyuan-note/siyuan/issues/9726
            // KeyboardUtils.fixAndroidBug5497(this);
            AndroidBug5497Workaround.assistActivity(this);
        } else {
            // 服务尚未绑定或实例为空，处理错误或等待绑定
            PopNotification.show("服务尚未绑定或实例为空").noAutoDismiss();
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) { // 只执行一次。在这里设置布局和初始化数据。在大多数情况下，不需要在 onRestart 中做太多事情，因为 onStart 已经处理了活动可见时的初始化。
        Log.w(TAG, "onCreate() invoked");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindBootService();

        mmkv = MMKV.defaultMMKV();

        // 注册 EventBus
        EventBus.getDefault().register(this);
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
                        PopTip.show("再按一次退出汐洛绞架");
                        exitTime = System.currentTimeMillis();
                    } else {
                        HWs.getInstance().vibratorWaveform(getApplicationContext(), new long[]{0, 30, 25, 40, 25, 10}, new int[]{2, 4, 3, 2, 2, 2}, -1);
                        if (webView != null) {
                            webView.evaluateJavascript("javascript:window.location.href = 'siyuan://api/system/exit';", null);
                        }
                    }
                } else {
                    webView.evaluateJavascript("javascript:window.goBack ? window.goBack() : window.history.back()", null);
                }
                HWs.getInstance().vibratorWaveform(getApplicationContext(), new long[]{0, 30, 25, 40, 25}, new int[]{9, 2, 1, 7, 2}, -1);
            }
        });

        HashSet<String> permissionsToCheck = new HashSet<>(Ps.PG_Core); // 核心权限组，每次启动都要检查
        if (Build.VERSION.SDK_INT >= 33) {
            permissionsToCheck.addAll(Ps.useAPI33);
        }
        for (String permission : permissionsToCheck) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(permission);
                Log.w(TAG, "onCreate() -> "+permission+" task add [Ps.PG_Core]");
            } else {
                Log.d(TAG, "onCreate() -> "+permission+" granted [Ps.PG_Core]");
            }
        }


        if (Utils.isFirstLaunch(this)) {
            permissionsToCheck.addAll(Ps.PG_unCore); // 非核心权限组，仅安装后首次启动集中申请
            for (String permission : permissionsToCheck) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionList.add(permission);
                    Log.w(TAG, "onCreate() -> "+permission+" task add [Ps.PG_unCore]");
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
        webView = bootService.getWebView();
        if (webView != null) {
            // 设置WebView的布局参数
            webView.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT));
            webViewContainer = findViewById(R.id.webViewContainer);
            ViewGroup parent = (ViewGroup) webView.getParent();
            if (parent == null) {
                Log.d(TAG, "将WebView添加到容器中");
                webViewContainer.addView(webView); // 将WebView添加到容器中
            } else {
                if (parent != webViewContainer) {
                    Log.d(TAG, "WebView已在其他容器中，先移除再添加");
                    parent.removeView(webView); // 从原来的容器中移除WebView
                    webViewContainer.addView(webView); // 将WebView添加到当前容器中
                } else {
                    Log.d(TAG, "WebView已在当前容器中，无需再次添加");
                }
            }


            webView.setWebChromeClient(new WebChromeClient() {
            // setWebViewClient 和 setWebChromeClient 并不同，别看走眼了
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
        showBootIndex();
        }
//        final WebSettings ws = webView.getSettings();
//        checkWebViewVer(ws);
//        userAgent = ws.getUserAgentString();
    }

    @SuppressLint("SetJavaScriptEnabled")
    void showBootIndex() {
        Log.w(TAG, "showBootIndex() invoked");
        webViewContainer.setVisibility(View.VISIBLE);
        webView.setWebViewClient(new WebViewClient() {
            // setWebViewClient 和 setWebChromeClient 并不同，别看走眼了
            // 对于 POST 请求不会调用此方法
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
                if (url.contains("siyuan://coldRestart")) {
//                    coldRestart();
                    return true; // 这里返回 true 阻止网页导航
                }
                if (url.contains("siyuan://androidFeedback")) {
                    androidFeedback();
                    return true; // 这里返回 true 阻止网页导航
                }

                if (uri.getScheme().toLowerCase().startsWith("http")) {
                    final Intent i = new Intent(Intent.ACTION_VIEW, uri);
                    i.addCategory(Intent.CATEGORY_BROWSABLE);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_REQUIRE_DEFAULT);
                    startActivity(i); // https://developer.android.google.cn/training/app-links/verify-android-applinks?hl=zh-cn
                    // 从 Android 12 开始，经过验证的链接现在会自动在相应的应用中打开，以获得更简化、更快速的用户体验。谷歌还更改了未经Android应用链接验证或用户手动批准的链接的默认处理方式。谷歌表示，Android 12将始终在默认浏览器中打开此类未经验证的链接，而不是向您显示应用程序选择对话框。
                    return true;
                }
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                // 页面加载完成时调用
                Log.d("WebViewClient", "onPageFinished: " + url);
                view.evaluateJavascript("javascript:document.body.classList.add(\"body--mobile\")", null);
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
    }

    /**
     * 等待内核 HTTP 服务伺服。
     */
    private void waitFotKernelHttpServing() {
        while (true) {
            sleep(100);
            if (Mobile.isHttpServing()) {
                break;
            }
        }
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
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) { // 其他 Activity 的结果不要傻傻的在这里处理好吧
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
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) { // 应该在 onRequestPermissionsResult 处理的别跑来这里啊混蛋！
        Log.w(TAG, "onActivityResult() -> requestCode "+requestCode+"  resultCode "+resultCode);
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(OnSiYuanMainRequestEvent event) {
        // 检查权限请求的结果
        if (event.getRequestCode() == S.REQUEST_OVERLAY) {
            if (event.getResultCode() == RESULT_OK) {
                // 权限已授予
                // 在此处执行相应的操作
                if (event.getCallback().equals("showwifi")) { // 已有权限的情况下不走这里
                    Intent intent = new Intent(this, FloatingWindowService.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                    ServiceUtils.startService(intent);
                }
            } else {
                // 权限未授予
                // 在此处执行相应的操作
            }
        } else if (event.getRequestCode() == S.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS_AND_REBOOT) {
            // 不管结果是什么都重启
            if (event.getCallback().equals(S.EVENTS.INSTANCE.getCALL_MainActivity_siyuan_1())) {
                coldRestart();
            }
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.w(TAG,"onConfigurationChanged -> invoked");
        // 检测屏幕方向是否发生改变
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // 当前为横屏，在这里处理横屏时的布局变化
            Log.w(TAG,"当前为横屏");
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            // 当前为竖屏，在这里处理竖屏时的布局变化
            Log.w(TAG,"当前为横屏");
        }

        // 检测软键盘的显示状态
        if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) {
            // 软键盘隐藏了，在这里处理布局变化
            Log.w(TAG,"软键盘隐藏了");
        } else if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {
            // 软键盘显示了，在这里处理布局变化
            Log.w(TAG,"软键盘隐藏了");
        }
    }

    private WebViewPool.WebViewWrapper webViewWrapper;

    @Override
    protected void onDestroy() {
        Log.w(TAG, "onDestroy() invoked"); // 大概率不会输出
        // 注销 EventBus
        EventBus.getDefault().unregister(this);
        KeyboardUtils.unregisterSoftInputChangedListener(getWindow());
        AppUtils.unregisterAppStatusChangedListener(this);
        releaseBootService();
        super.onDestroy();
    }

    @Override
    public void onForeground(Activity activity) {
        Log.w(TAG, "onForeground() invoked");
        startSyncData();
        if (null != webView) {
            webView.evaluateJavascript("javascript:window.reconnectWebSocket()", null);
        }
    }

    @Override
    public void onBackground(Activity activity) {
        Log.w(TAG, "onBackground() invoked");
        startSyncData();
    }

    @Override
    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        super.onMultiWindowModeChanged(isInMultiWindowMode);
        Log.w(TAG, "onMultiWindowModeChanged() invoked");
    }

    @Override
    protected void onStart() { // 当活动变得对用户可见时，系统会调用这个方法。这是在活动即将进入前台并且用户可以看到它时进行最后准备的地方。在这里进行用户可见时的初始化，比如开始动画、注册广播接收器等。
        super.onStart();
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
        Log.w(TAG, "onStop() invoked");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.w(TAG, "onResume() invoked");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.w(TAG, "onPause() invoked");
    }

    @Override
    protected void onRestart() { // 当活动重新启动时调用（一般是onStop后）。在这里恢复活动之前的状态，比如重新获取数据、恢复界面状态等。
        super.onRestart();
        Log.w(TAG, "onRestart() invoked");
    }

    public void exit() {
        runOnUiThread(() -> {
            setSillotGibbetCheckInState();
            finish();
            finishAndRemoveTask();
//            System.exit(0);
        });
    }

    public boolean setSillotGibbetCheckInState() {
        AtomicReference<Boolean> result = new AtomicReference<>(false);
        runOnUiThread(() -> {
            // 使用runOnUiThread确保在主线程中获取WebView的URL
            String webViewUrl = webView.getUrl();
            if (webViewUrl != null && webViewUrl.contains("/check-auth?")) {
                mmkv.putString("AppCheckInState","lockScreen");
                result.set(true);
                Log.d(TAG, "exit() AppCheckInState->lockScreen");
            } else {
                mmkv.putString("AppCheckInState","unlockScreen");
                result.set(false);
                Log.d(TAG, "exit() AppCheckInState->unlockScreen");
            }
        });
        return result.get();
    }

    public void coldRestart() {
        Log.w(TAG, "coldRestart() invoked");
        setSillotGibbetCheckInState();
        // 从任务列表中移除，禁止放在 onDestroy
        finishAndRemoveTask(); //  这个方法用于结束当前活动，并从任务栈中移除整个任务。这意味着，当前活动所在的任务中所有的活动都会被结束，并且任务本身也会被移除。如果这个任务是最顶层的任务，那么用户将返回到主屏幕。
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
//       android.os.Process.killProcess(android.os.Process.myPid()); // 暂时无法解决杀死其他任务栈的冲突，不加这句重启活动会崩溃
    }

    public void startSyncData() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED) // 确保在网络连接时运行
                .setRequiresBatteryNotLow(false) // 低电量时也运行
                .build();
        OneTimeWorkRequest syncDataWork = new OneTimeWorkRequest.Builder(SyncDataWorker.class)
                .setConstraints(constraints)
//                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST) // 加急工作。如果配额允许，它将立即开始在后台运行。但是可能会在 Android 12 上抛出运行时异常，并且在启动受到限制时可能会抛出异常。
                .build();
        WorkManager.getInstance(this).enqueue(syncDataWork);
    }
}
