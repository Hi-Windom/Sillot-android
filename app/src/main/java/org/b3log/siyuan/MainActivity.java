 /*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2020-2024.
 *
 * lastModified: 2024/7/8 下午11:48
 * updated: 2024/7/8 下午11:48
 */
package org.b3log.siyuan;

 import static org.b3log.siyuan.MainActivityHelperKt.onDragInsertIntoWebView;
 import static sc.windom.sofill.android.webview.WebViewThemeKt.applySystemThemeToWebView;

 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.ClipData;
 import android.content.ComponentName;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.pm.PackageManager;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.provider.MediaStore;
 import android.view.DragEvent;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.ViewGroup;

 import sc.windom.sillot.App;
 import sc.windom.sofill.Us.U_DEBUG;
 import sc.windom.sofill.Us.U_Layout;
 import sc.windom.sofill.Us.U_Permission;
 import sc.windom.sofill.Us.U_Phone;
 import sc.windom.sofill.android.webview.WebViewLayoutManager;
 import sc.windom.sofill.Ss.S_Events;
 import sc.windom.sofill.Ss.S_Intent;
 import sc.windom.sofill.Ss.S_REQUEST_CODE;
 import sc.windom.sofill.android.webview.WebPoolsPro;
 import android.webkit.ConsoleMessage;
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
 import com.blankj.utilcode.util.StringUtils;
 import com.kongzue.dialogx.dialogs.BottomMenu;
 import com.kongzue.dialogx.dialogs.PopNotification;
 import com.kongzue.dialogx.dialogs.PopTip;
 import com.tencent.bugly.crashreport.BuglyLog;
 import com.tencent.bugly.crashreport.CrashReport;
 import com.tencent.mmkv.MMKV;

 import sc.windom.sofill.android.HWs;

 import sc.windom.gibbet.services.BootService;
 import sc.windom.gibbet.workers.SyncDataWorker;
 import org.greenrobot.eventbus.EventBus;
 import org.greenrobot.eventbus.Subscribe;
 import org.greenrobot.eventbus.ThreadMode;

 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Objects;
 import java.util.UUID;
 import java.util.concurrent.atomic.AtomicReference;

 import mobile.Mobile;
 import sc.windom.sofill.U;
 import sc.windom.sofill.S;
 import sc.windom.namespace.SillotMatrix.R;

 /**
 * 主程序.
 *
 * @author <a href="https://88250.b3log.org">Liang Ding</a>
 * @version 1.1.0.3, Apr 24, 2024
 * @since 1.0.0
 */
public class MainActivity extends AppCompatActivity implements com.blankj.utilcode.util.Utils.OnAppStatusChangedListener {
    private final String TAG = "MainActivity-SiYuan";
    private Activity thisActivity;
    public WebView webView;
    private FrameLayout webViewContainer;
    private ImageView bootLogo;
    private ProgressBar bootProgressBar;
    private TextView bootDetailsText;
    private ValueCallback<Uri[]> uploadMessage;
    private static final int REQUEST_SELECT_FILE;
    private static final int REQUEST_CAMERA;
    static {
         S.getREQUEST_CODE();
         REQUEST_SELECT_FILE = S_REQUEST_CODE.REQUEST_SELECT_FILE;
         REQUEST_CAMERA = S_REQUEST_CODE.REQUEST_CAMERA;
    }
    private long exitTime;
    public MMKV mmkv;
    public ActivityResultLauncher<String[]> requestPermissionLauncher;
    public int requestPermissionAll_works = 0;

     /**
      * dispatchKeyEvent 是一个更高级的方法，它可以处理所有类型的按键事件，包括按键按下、抬起和长按。
      * dispatchKeyEvent 方法在事件传递给 onKeyDown、onKeyUp 或其他控件之前被调用。
      * TODO: <a href="https://ld246.com/article/1711543259805">部分安卓平板上无法正常使用 ESC 键</a>
      * @param event The key event.
      *
      * @return
      */
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_ESCAPE) { // getKeyCode 的数字只能拿来和 KeyEvent 里面的对比，不然没有意义
                // 处理ESC键按下事件，并不能阻止输入法对ESC的响应，只有输入法退出了才轮到这里。
                // 除非设置 WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM，那键盘需要自己处理了
                BuglyLog.e("ESC键被按下",String.valueOf(event.getKeyCode()));
                // return true; // 返回true表示事件已被处理，不再传递
            }
        }
        // 事件未被处理，继续传递事件
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onNewIntent(final Intent intent) {
        BuglyLog.w(TAG, "onNewIntent() invoked");
        super.onNewIntent(intent);
        if (null != webView) {
            final String blockURL = intent.getStringExtra("blockURL");
            if (!StringUtils.isEmpty(blockURL)) {
                webView.evaluateJavascript("javascript:window.openFileByURL('" + blockURL + "')", null);
            }
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
                .setOnMenuItemClickListener((dialog, text, index) -> {
                    if (text.equals("电子邮件")) {
                        U.getFuckOtherApp().sendEmail(this.getPackageManager(), "694357845@qq.com", "汐洛安卓反馈", U_DEBUG.getDeviceInfoString());
                    } else if (text.equals("QQ")) {
                        U.getFuckOtherApp().launchQQAndCopyToClipboard(this, "694357845", "开发者 QQ 号已复制");
                    } else if (text.equals("抖音")) {
                        U.getFuckOtherApp().launchTikTopAndCopyToClipboard(this, "AsyncTTk", "开发者抖音号已复制");
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
            BuglyLog.i(TAG, "onServiceConnected invoked");
            BootService.LocalBinder binder = (BootService.LocalBinder) service;
            bootService = binder.getService();
            serviceBound = true;
            App.bootService = bootService;
            // 服务绑定后，执行依赖于bootService的代码
            performActionWithService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            BuglyLog.i(TAG, "onServiceDisconnected invoked");
            serviceBound = false;
            bootService.setKernelStarted(false);
            bootService.stopSelf();
            bootService = null;
            App.bootService = null;
            releaseBootService();
        }
    };

    void releaseBootService() {
        BuglyLog.i(TAG, "releaseBootService invoked");
        // 销毁WebView并从池中移除
        if (webView != null) {
            webView.setOnDragListener(null);
            ViewGroup parent = (ViewGroup) webView.getParent();
            parent.removeView(webView); // 从原来的容器中移除WebView
            Objects.requireNonNull(WebPoolsPro.getInstance()).recycle(webView, "Sillot-Gibbet");
        }
    }

    void bindBootService() {
        if (bootService == null){
            webView = Objects.requireNonNull(WebPoolsPro.getInstance()).createWebView(this, "Sillot-Gibbet");
            instanceId = UUID.randomUUID().toString();
            Intent intent = new Intent(getApplicationContext(), BootService.class);
            intent.putExtra("INSTANCE_ID", instanceId); // 绑定服务时传递instanceId
            S.getINTENT();
            intent.putExtra(S_Intent.EXTRA_WEB_VIEW_KEY, "Sillot-Gibbet");
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE); // TODO: 双开共存时内核固定端口冲突
        } else {
            performActionWithService();
        }
    }

     /**
      * 在这里执行依赖于bootService的代码
      */
    private void performActionWithService() {
        BuglyLog.w(TAG, "performActionWithService invoked");
        if (serviceBound && bootService != null) {
            bootService.showWifi(this);
            // 初始化 UI 元素
            BuglyLog.w(TAG, "performActionWithService() -> initUIElements() invoked");
            initUIElements();

            AppUtils.registerAppStatusChangedListener(this);
        } else {
            // 服务尚未绑定或实例为空，处理错误或等待绑定
            PopNotification.show("服务尚未绑定或实例为空").noAutoDismiss();
        }
    }


     private String saveFileToCache(Uri uri, String mimeType) {
         try {
             InputStream inputStream = getContentResolver().openInputStream(uri);
             File cacheDir = getCacheDir(); // 应用私有缓存目录
             File file = File.createTempFile("temp", mimeType.substring(mimeType.indexOf('/') + 1), cacheDir);
             OutputStream outputStream = new FileOutputStream(file);

             byte[] buffer = new byte[1024];
             int length;
             if (inputStream != null) {
                 while ((length = inputStream.read(buffer)) > 0) {
                     outputStream.write(buffer, 0, length);
                 }
             }

             outputStream.close();
             if (inputStream != null) {
                 inputStream.close();
             }

             return file.getAbsolutePath();
         } catch (Exception e) {
             PopNotification.show(TAG, e.toString());
             return null;
         }
     }

    @Override
    protected void onCreate(final Bundle savedInstanceState) { // 只执行一次。在这里设置布局和初始化数据。在大多数情况下，不需要在 onRestart 中做太多事情，因为 onStart 已经处理了活动可见时的初始化。
        BuglyLog.w(TAG, "onCreate() invoked");
        super.onCreate(savedInstanceState);
        thisActivity = this;
        setContentView(R.layout.activity_main);
        U_Layout.applyStatusBarConfigurationV2(this, false); // 可以伸到状态栏和导航栏的位置（沉浸式）
        bindBootService();
        mmkv = MMKV.defaultMMKV();
        // 注册 EventBus
        EventBus.getDefault().register(this);
        // 获取OnBackPressedDispatcher
        OnBackPressedDispatcher onBackPressedDispatcher = getOnBackPressedDispatcher();

        // 设置OnBackPressedCallback
        onBackPressedDispatcher.addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 在这里处理后退逻辑
                if (U_Phone.isPad(getApplicationContext())) {
                    if ((System.currentTimeMillis() - exitTime) > 2000) {
                        PopTip.show( "再按一次退出汐洛绞架");
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

        // 只能写在 activity onCreate() 这里
        requestPermissionLauncher = this.registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                (Map<String, Boolean> permissions) -> {
                    boolean allPermissionsGranted = true;
                    for (String permission : permissions.keySet()) {
                        if (permissions.get(permission) != null && Boolean.TRUE.equals(permissions.get(permission))) {
                            requestPermissionAll_works--;
                        } else {
                            allPermissionsGranted = false;
                        }
                    }
                    if (requestPermissionAll_works == 0 && allPermissionsGranted) {
                        // 所有权限都得到了允许，执行相应操作
                    } else {
                        // 处理未被允许的权限
                    }
                });

    }


    private void initUIElements() {
        bootLogo = findViewById(R.id.bootLogo);
        bootProgressBar = findViewById(R.id.progressBar);
        bootDetailsText = findViewById(R.id.bootDetails);
        if (webView != null) {
            // 设置WebView的布局参数
            webView.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT));
            webViewContainer = findViewById(R.id.webViewContainer);
            ViewGroup parent = (ViewGroup) webView.getParent();
            if (parent == null) {
                BuglyLog.d(TAG, "将WebView添加到容器中");
                webViewContainer.addView(webView); // 将WebView添加到容器中
            } else {
                if (parent != webViewContainer) {
                    BuglyLog.d(TAG, "WebView已在其他容器中，先移除再添加");
                    parent.removeView(webView); // 从原来的容器中移除WebView
                    webViewContainer.addView(webView); // 将WebView添加到当前容器中
                } else {
                    BuglyLog.d(TAG, "WebView已在当前容器中，无需再次添加");
                }
            }

            // 避免和状态栏之间存在留白
            ((ViewGroup) webView.getParent()).setPadding(0, U_Layout.getStatusBarHeight(webView), 0, 0);

            // 注册工具栏显示/隐藏跟随软键盘状态
            // Fix https://github.com/siyuan-note/siyuan/issues/9765
            // Fix https://github.com/siyuan-note/siyuan/issues/9726
            // https://github.com/Hi-Windom/Sillot-android/issues/84
            WebViewLayoutManager webViewLayoutManager = WebViewLayoutManager.assistActivity(this, webView);
            webViewLayoutManager.setOnConfigurationChangedCallback((newConfig)->{
                BuglyLog.w(TAG, "新配置屏幕方向: " + newConfig.orientation);
                applySystemThemeToWebView(this, webView);
                return null;
            });
            webViewLayoutManager.setOnLayoutChangedCallback((frameLayout)->{
                applySystemThemeToWebView(thisActivity, webView);
                return null;
            });
            if (!U.getPHONE().isPad(this)) {
                webViewLayoutManager.setDelayResetLayoutWhenImeShow(120);
                // showKeyboardToolbar 不知道在哪已经实现了随键盘呼出（有延时，大概率是在前端），这里依旧调用是因为响应更快
                webViewLayoutManager.setJSonImeShow("showKeyboardToolbar();");
                webViewLayoutManager.setJSonImeHide("hideKeyboardToolbar();");
                // 锁定方便悬浮键盘不自动收起
                webViewLayoutManager.setJSonImeShow0Height("window.Sillot.android.LockKeyboardToolbar=true;hideKeyboardToolbar();showKeyboardToolbar();");
                webViewLayoutManager.setJSonImeHide0Height("window.Sillot.android.LockKeyboardToolbar=false;hideKeyboardToolbar();");
            }

            // 使用 Chromium 调试 WebView
            if (U_DEBUG.isDebugPackageAndMode(this)) {
                BuglyLog.w(TAG, "已启用 Chromium 调试 WebView。Edge 浏览器请访问 edge://inspect ，更多信息请访问 https://learn.microsoft.com/zh-cn/microsoft-edge/devtools-guide-chromium/remote-debugging/webviews");
                WebView.setWebContentsDebuggingEnabled(true);
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

                /**
                 * 当网页请求其他权限时，会回调此方法以询问用户是否允许
                 * @param request the PermissionRequest from current web content.
                 */
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                request.grant(request.getResources());
            }

                /**
                 * 当 WebView 加载页面时，会多次回调此方法以报告加载进度
                 * @param webView The WebView that initiated the callback.
                 * @param progress Current page loading progress, represented by
                 *                    an integer between 0 and 100.
                 */
            @Override
            public void onProgressChanged(WebView webView, int progress) {
                // 增加Javascript异常监控
                CrashReport.setJavascriptMonitor(webView, true);
                super.onProgressChanged(webView, progress);
            }

                /**
                 * 自定义处理前端默认弹窗，后续可以考虑使用 DialogX 等库美化
                 * @param view The WebView that initiated the callback.
                 * @param url The url of the page requesting the dialog.
                 * @param message Message to be displayed in the window.
                 * @param result A JsResult to confirm that the user closed the window.
                 */
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                Date date = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
                String formattedDate = sdf.format(date);

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("[WebChromeClient] onJsAlert from WebView")
                        .setMessage("\n--------------------------------------------\n" + message + "\n--------------------------------------------\n\n* " + view.getTitle() + "\n* " + formattedDate)
                        .setPositiveButton("OK", (dialog, which) -> result.confirm())
                        .setCancelable(false)
                        .show();
                return true;
            }

                /**
                 * 这里可以通过捕获控制台输出，进行特定的操作
                 * @param consoleMessage Object containing details of the console message.
                 */
                @Override
                public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                    BuglyLog.d(TAG + " [WebChromeClient] ", "onConsoleMessage -> " + U_DEBUG.prettyConsoleMessage(consoleMessage));
                    return true; // 屏蔽默认日志输出避免刷屏
                    // return super.onConsoleMessage(consoleMessage);
                }

        });

            webView.setOnDragListener((view, event) -> {
                BuglyLog.d(TAG, "webView.setOnDragListener((view, event) -> view: " + view.toString() + ", event: " + event.toString());
                // 支持 OriginOS4 超级拖拽 #90
                onDragInsertIntoWebView(webView, event);
                // 禁用拖拽 https://github.com/siyuan-note/siyuan/issues/6436
                return DragEvent.ACTION_DRAG_ENDED != event.getAction();
            });
            showBootIndex();
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    void showBootIndex() {
        BuglyLog.w(TAG, "showBootIndex() invoked");
        webViewContainer.setVisibility(View.VISIBLE);
        webView.setWebViewClient(new WebViewClient() {
            // setWebViewClient 和 setWebChromeClient 并不同，别看走眼了
            // 对于 POST 请求不会调用此方法
            @Override
            public boolean shouldOverrideUrlLoading(final WebView view, final WebResourceRequest request) {
                final Uri uri = request.getUrl();
                final String url = uri.toString();
                BuglyLog.w(TAG, "[WebViewClient] shouldOverrideUrlLoading <- "+url);
                if (url.contains("127.0.0.1")) {
                    var AppCheckInState = mmkv.getString("AppCheckInState", "");
                    if (AppCheckInState.equals("lockScreen")) {
                        try {
                            String encodedUrl = URLEncoder.encode(url, "UTF-8");
                            String gotourl = "http://127.0.0.1:58131/check-auth?to=" + encodedUrl;
                            BuglyLog.w(TAG,"[WebViewClient] shouldOverrideUrlLoading -> " + gotourl);
                            view.loadUrl(gotourl);
                        } catch (UnsupportedEncodingException e) {
                            // 编码失败的处理
                            BuglyLog.w(TAG, e.toString());
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

                if (Objects.requireNonNull(uri.getScheme()).toLowerCase().startsWith("http")) {
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
                BuglyLog.d(TAG, "[WebViewClient] onPageFinished: " + url);
                view.evaluateJavascript("javascript:document.body.classList.add(\"body--mobile\")", null);
                bootLogo.postDelayed(() -> {
                    bootLogo.setVisibility(View.GONE);
                    bootProgressBar.setVisibility(View.GONE);
                    bootDetailsText.setVisibility(View.GONE);
                    bootLogo.setVisibility(View.GONE);
                }, 186);

                // autoWebViewDarkMode 决定是否自动在 webview 中应用暗黑模式。如果前端已经有暗黑模式配置，此项应为 false（默认值）
                applySystemThemeToWebView(thisActivity, webView);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                // 在加载页面出现错误时进行处理
                if (error != null) {
                    BuglyLog.e("WebViewClient", "onReceivedError: " + error.getDescription());
                }
            }
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                // 页面开始加载时调用
                BuglyLog.d("WebViewClient", "onPageStarted: " + url);
            }

        });

        final JSAndroid JSAndroid = new JSAndroid(this);
        webView.addJavascriptInterface(JSAndroid, "JSAndroid");
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        final WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setCacheMode(WebSettings.LOAD_NO_CACHE);
        ws.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, int[] grantResults) { // 其他 Activity 的结果不要傻傻的在这里处理好吧
        if (grantResults.length > 0) {
            BuglyLog.w(TAG, "onRequestPermissionsResult() -> requestCode "+requestCode+"  grantResults[0] "+grantResults[0]);
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
        BuglyLog.w(TAG, "onActivityResult() -> requestCode "+requestCode+"  resultCode "+resultCode);
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
                    Bitmap cam_photo = (Bitmap) Objects.requireNonNull(intent.getExtras()).get("data");
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    if (cam_photo != null) {
                        cam_photo.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                    }
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
        S.getREQUEST_CODE();
        if (event.getRequestCode() == S_REQUEST_CODE.REQUEST_OVERLAY) {
            if (event.getResultCode() == RESULT_OK) {
                // 权限已授予
                // 在此处执行相应的操作
                if (event.getCallback().equals("showwifi")) { // 已有权限的情况下不走这里
                    // 单一权限不够了
//                    Intent intent = new Intent(this, FloatingWindowService.class);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
//                    ServiceUtils.startService(intent);
                }
            } else {
                // 权限未授予
                // 在此处执行相应的操作
            }
        } else {
            S.getREQUEST_CODE();
            if (event.getRequestCode() == S_REQUEST_CODE.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS_AND_REBOOT) {
                // 不管结果是什么都重启
                S.getEVENTS();
                if (event.getCallback().equals(S_Events.INSTANCE.getCALL_MainActivity_siyuan_1())) {
                    coldRestart();
                }
            }
        }
    }

    // https://github.com/Hi-Windom/Sillot-android/issues/84 不能在此重写
//    @Override
//    public void onConfigurationChanged(@NonNull Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//    }


    @Override
    protected void onDestroy() {
        BuglyLog.w(TAG, "onDestroy() invoked"); // 大概率不会输出
        // 注销 EventBus
        EventBus.getDefault().unregister(this);
        AppUtils.unregisterAppStatusChangedListener(this);
        releaseBootService();
        super.onDestroy();
    }

    @Override
    public void onForeground(Activity activity) {
        BuglyLog.w(TAG, "onForeground() invoked");
        startSyncData();
        if (null != webView) {
            webView.evaluateJavascript("javascript:window.reconnectWebSocket()", null);
        }
    }

    @Override
    public void onBackground(Activity activity) {
        BuglyLog.w(TAG, "onBackground() invoked");
        startSyncData();
    }

    @Override
    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        super.onMultiWindowModeChanged(isInMultiWindowMode);
        BuglyLog.w(TAG, "onMultiWindowModeChanged() invoked");
    }

    @Override
    protected void onStart() { // 当活动变得对用户可见时，系统会调用这个方法。这是在活动即将进入前台并且用户可以看到它时进行最后准备的地方。在这里进行用户可见时的初始化，比如开始动画、注册广播接收器等。
        super.onStart();
        BuglyLog.w(TAG, "onStart() -> canPopInBackground "+ U_Permission.canPopInBackground(this));
        BuglyLog.w(TAG, "onStart() -> canShowOnTop "+U_Permission.canShowOnTop(this));
        BuglyLog.w(TAG, "onStart() -> isShowingOnLockScreen "+U_Permission.isShowingOnLockScreen(this));
        BuglyLog.w(TAG, "onStart() -> canManageAllFiles "+U_Permission.canManageAllFiles(this));
        BuglyLog.w(TAG, "onStart() -> canAccessDeviceState "+U_Permission.canAccessDeviceState(this));
        BuglyLog.w(TAG, "onStart() -> canRequestPackageInstalls "+U_Permission.canRequestPackageInstalls(this));
    }

    @Override
    protected void onStop() {
        super.onStop();
        BuglyLog.w(TAG, "onStop() invoked");
    }

    @Override
    protected void onResume() {
        super.onResume();
        BuglyLog.w(TAG, "onResume() invoked");
    }

    @Override
    protected void onPause() {
        super.onPause();
        BuglyLog.w(TAG, "onPause() invoked");
    }

    @Override
    protected void onRestart() { // 当活动重新启动时调用（一般是onStop后）。在这里恢复活动之前的状态，比如重新获取数据、恢复界面状态等。
        super.onRestart();
        BuglyLog.w(TAG, "onRestart() invoked");
    }

    public void exit() {
        runOnUiThread(() -> {
            setSillotGibbetCheckInState();
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
                BuglyLog.d(TAG, "exit() AppCheckInState->lockScreen");
            } else {
                mmkv.putString("AppCheckInState","unlockScreen");
                result.set(false);
                BuglyLog.d(TAG, "exit() AppCheckInState->unlockScreen");
            }
        });
        return result.get();
    }

    public void coldRestart() {
        BuglyLog.w(TAG, "coldRestart() invoked");
        setSillotGibbetCheckInState();
        // 从任务列表中移除，禁止放在 onDestroy
        finishAndRemoveTask(); //  这个方法用于结束当前活动，并从任务栈中移除整个任务。这意味着，当前活动所在的任务中所有的活动都会被结束，并且任务本身也会被移除。如果这个任务是最顶层的任务，那么用户将返回到主屏幕。
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_NEW_DOCUMENT
                        | Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        );
        startActivity(intent);
       android.os.Process.killProcess(android.os.Process.myPid()); // 暂时无法解决杀死其他任务栈的冲突，不加这句无法重启内核
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
