 /*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2020-2024.
 *
 * lastModified: 2024/8/18 07:56
 * updated: 2024/8/18 07:56
 */
package org.b3log.siyuan;

 import static sc.windom.gibbet.GibbetMainActivityHelperKt.androidFeedback;
 import static sc.windom.gibbet.GibbetMainActivityHelperKt.coldRestart;
 import static sc.windom.gibbet.GibbetMainActivityHelperKt.handleKeyEvent;
 import static sc.windom.gibbet.GibbetMainActivityHelperKt.handleOnBack;
 import static sc.windom.gibbet.GibbetMainActivityHelperKt.onDragInsertIntoWebView;
 import static sc.windom.gibbet.GibbetMainActivityHelperKt.openCamera;
 import static sc.windom.gibbet.GibbetMainActivityHelperKt.setSillotGibbetCheckInState;
 import static sc.windom.sofill.Ss.S_REQUEST_CODEKt.REQUEST_CAMERA;
 import static sc.windom.sofill.Ss.S_REQUEST_CODEKt.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS_AND_REBOOT;
 import static sc.windom.sofill.Ss.S_REQUEST_CODEKt.REQUEST_OVERLAY;
 import static sc.windom.sofill.Ss.S_REQUEST_CODEKt.REQUEST_SELECT_FILE;
 import static sc.windom.sofill.Us.U_FileUtils.getMimeTypeForHTTP;
 import static sc.windom.sofill.Us.U_LayoutKt.applyStatusBarConfigurationV2;
 import static sc.windom.sofill.Us.U_WebviewKt.checkWebViewVer;
 import static sc.windom.sofill.Us.U_WebviewKt.showJSAlert;
 import static sc.windom.sofill.pioneer.StoreKt.mmkvGibbet;

 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.ClipData;
 import android.content.ComponentName;
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

 import mobile.Mobile;
 import sc.windom.sillot.MatrixModel;
 import sc.windom.sofill.Ss.S_Webview;
 import sc.windom.sofill.Us.U_DEBUG;
 import sc.windom.sofill.Us.U_Permission;
 import sc.windom.sofill.Us.U_Phone;
 import sc.windom.sofill.Us.U_WebviewKt;
 import sc.windom.sofill.android.webview.WebViewLayoutManager;
 import sc.windom.sofill.Ss.S_Events;
 import sc.windom.sofill.Ss.S_Intent;
 import sc.windom.sofill.android.webview.WebPoolsPro;
 import android.webkit.ConsoleMessage;
 import android.webkit.JsResult;
 import android.webkit.PermissionRequest;
 import android.webkit.ValueCallback;
 import android.webkit.WebChromeClient;
 import android.webkit.WebResourceError;
 import android.webkit.WebResourceRequest;
 import android.webkit.WebResourceResponse;
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
 import androidx.core.app.ActivityCompat;
 import androidx.core.content.ContextCompat;

 import com.blankj.utilcode.util.AppUtils;
 import com.blankj.utilcode.util.StringUtils;
 import com.kongzue.dialogx.dialogs.PopNotification;
 import com.kongzue.dialogx.dialogs.PopTip;
 import com.tencent.bugly.crashreport.BuglyLog;
 import com.tencent.bugly.crashreport.CrashReport;

 import sc.windom.gibbet.services.BootService;

 import org.greenrobot.eventbus.EventBus;
 import org.greenrobot.eventbus.Subscribe;
 import org.greenrobot.eventbus.ThreadMode;

 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URLEncoder;
 import java.nio.charset.StandardCharsets;
 import java.util.Map;
 import java.util.Objects;
 import java.util.UUID;

 import sc.windom.sofill.S;
 import sc.windom.namespace.SillotMatrix.R;
 import sc.windom.sofill.annotations.SillotActivity;
 import sc.windom.sofill.annotations.SillotActivityType;

 /**
 * 主程序.
 *
 * @author <a href="https://88250.b3log.org">Liang Ding</a>
 * @version 1.1.0.3, Apr 24, 2024
 * @since 1.0.0
 */
 @SillotActivity(TYPE = SillotActivityType.Main)
 @SillotActivity(TYPE = SillotActivityType.Launcher)
 @SillotActivity(TYPE = SillotActivityType.UseVisible)
public class MainActivity extends MatrixModel implements com.blankj.utilcode.util.Utils.OnAppStatusChangedListener {
    private final String TAG = "MainActivity-SiYuan";
     private final String Matrix_model = "汐洛绞架";
     @NonNull
     @Override
     public String getMatrixModel() {
         return Matrix_model;
     }

     /**
      * 检查是否已经存在 MainActivity 实例
      */
     public static boolean isInstanceCreated = false;
     private Activity thisActivity;
    public WebView webView;
    private FrameLayout webViewContainer;
    private ImageView bootLogo;
    private ProgressBar bootProgressBar;
    private TextView bootDetailsText;
    private ValueCallback<Uri[]> uploadMessage;
    private long exitTime;
    public ActivityResultLauncher<String[]> requestPermissionLauncher;
    public int requestPermissionAll_works = 0;

     /**
      * dispatchKeyEvent 是一个更高级的方法，它可以处理所有类型的按键事件，包括按键按下、抬起和长按。
      * dispatchKeyEvent 方法在事件传递给 onKeyDown、onKeyUp 或其他控件之前被调用。
      * TODO: <a href="https://ld246.com/article/1711543259805">部分安卓平板上无法正常使用 ESC 键</a>
      * @param event The key event.
      *
      */
    @SuppressLint("RestrictedApi")
    @Override
    public boolean dispatchKeyEvent(@NonNull KeyEvent event) {
        boolean handled = handleKeyEvent(thisActivity, event);
        if (!handled) {
            return super.dispatchKeyEvent(event);
        } else {
            return true;
        }
    }

    @Override
    public void onNewIntent(final Intent intent) {
        BuglyLog.w(TAG, "onNewIntent() invoked");
        super.onNewIntent(intent);
        init(intent);
    }

     @Override
     protected void onSaveInstanceState(Bundle outState) {
         BuglyLog.d(TAG, "outState: " + outState);
         if (outState.isEmpty()) return; // avoid crash
         super.onSaveInstanceState(outState);
         // 可添加额外需要保存可序列化的数据
     }

     private void init(final Intent intent) {
        if (null == intent) {
            return;
        }
        BuglyLog.w(TAG, "init() invoked");
        final String blockURL = intent.getStringExtra("blockURL");
        if (!StringUtils.isEmpty(blockURL) && null != webView) {
            BuglyLog.w(TAG, blockURL);
            webView.evaluateJavascript(
                    "javascript:window.openFileByURL('" + blockURL + "')",
                    value -> Toast.makeText(getApplicationContext(), blockURL, Toast.LENGTH_SHORT).show()
            );
        }
     }

    public BootService bootService;
    private String instanceId; // 用于区分不同实例的ID

    final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            BuglyLog.i(TAG, "onServiceConnected invoked");
            BootService.LocalBinder binder = (BootService.LocalBinder) service;
            bootService = binder.getService();
            // 服务绑定后，执行依赖于bootService的代码
            performActionWithService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            BuglyLog.i(TAG, "onServiceDisconnected invoked");
            releaseBootService();
        }
    };

    private void releaseBootService() {
        BuglyLog.i(TAG, "releaseBootService invoked");
        // 销毁WebView并从池中移除
        if (webView != null) {
            webView.setOnDragListener(null);
            ViewGroup parent = (ViewGroup) webView.getParent();
            parent.removeView(webView); // 从原来的容器中移除WebView
            Objects.requireNonNull(WebPoolsPro.getInstance()).recycle(webView, "Sillot-Gibbet");
        }
        bootService.setKernelStarted(false);
        bootService.stopSelf();
        bootService = null;
        if (serviceConnection != null) {
            unbindService(serviceConnection);
        }
    }

    void bindBootService() {
        if (bootService == null){
            webView = Objects.requireNonNull(WebPoolsPro.getInstance()).createWebView(this, WebPoolsPro.key_SG);
            instanceId = UUID.randomUUID().toString();
            Intent intent = new Intent(getApplicationContext(), BootService.class);
            intent.putExtra("INSTANCE_ID", instanceId); // 绑定服务时传递instanceId
            S.getINTENT();
            intent.putExtra(S_Intent.EXTRA_WEB_VIEW_KEY, WebPoolsPro.key_SG);
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
        if (bootService != null) {
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
        isInstanceCreated = true;
        setContentView(R.layout.activity_main);
        applyStatusBarConfigurationV2(this, false); // 可以伸到状态栏和导航栏的位置（沉浸式）
        bindBootService();
        // 注册 EventBus
        EventBus.getDefault().register(this);
        // 获取OnBackPressedDispatcher
        OnBackPressedDispatcher onBackPressedDispatcher = getOnBackPressedDispatcher();

        // 设置OnBackPressedCallback
        onBackPressedDispatcher.addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 在这里处理后退逻辑
                exitTime = handleOnBack(getApplicationContext(), webView, exitTime);
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
        init(getIntent());
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

            // 注册工具栏显示/隐藏跟随软键盘状态
            // Fix https://github.com/siyuan-note/siyuan/issues/9765
            // Fix https://github.com/siyuan-note/siyuan/issues/9726
            // https://github.com/Hi-Windom/Sillot-android/issues/84
            View monitor = this.findViewById(android.R.id.content);
            WebViewLayoutManager webViewLayoutManager = WebViewLayoutManager.assistActivity(this, webView, monitor);
            webViewLayoutManager.setDebugTag(TAG);
            webViewLayoutManager.edgeToEdge = true;
            if (!U_Phone.isPad(this)) {
                webViewLayoutManager.delayResetLayoutWhenImeShow = 186;
                // https://github.com/siyuan-note/siyuan/issues/11098?utm_source=ld246.com 这里也锁定键盘不自动收起，而且JS中 show 之前的 hide 也不能删
                webViewLayoutManager.JSonImeShow = "window.Sillot.android.LockKeyboardToolbar=true;hideKeyboardToolbar();showKeyboardToolbar();";
                webViewLayoutManager.JSonImeHide = "window.Sillot.android.LockKeyboardToolbar=false;hideKeyboardToolbar();";
                // LockKeyboardToolbar 锁定方便悬浮键盘不自动收起
                webViewLayoutManager.JSonImeShow0Height = "window.Sillot.android.LockKeyboardToolbar=true;hideKeyboardToolbar();showKeyboardToolbar();";
                webViewLayoutManager.JSonImeHide0Height = "window.Sillot.android.LockKeyboardToolbar=false;hideKeyboardToolbar();";
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

                    mCameraUri = openCamera(thisActivity);
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
                showJSAlert(MainActivity.this, view, url, message, result);
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

     @SuppressLint("DefaultLocale")
     private String createProgressDataJson(int progress) {
         String details = "Initializing components..."; // 假设的详情信息
         return String.format("{\"data\": {\"progress\": %d, \"details\": \"%s\"}}", progress, details);
     }

    @SuppressLint("SetJavaScriptEnabled")
    void showBootIndex() {
        BuglyLog.w(TAG, "showBootIndex() invoked");
        webViewContainer.setVisibility(View.VISIBLE);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                Uri url = request.getUrl();
                BuglyLog.d(TAG, "shouldInterceptRequest -> " + url);
                // 检查请求的URL是否是我们想要拦截的本地HTML文件
                if (url.toString().startsWith("http://127.0.0.1:58131/appearance/") || url.toString().startsWith("http://127.0.0.1:58131/stage/")) {
                    try {
                        String f =
                                url.toString().replace("http://127.0.0.1:58131/", view.getContext().getApplicationContext().getFilesDir().getAbsolutePath() +
                                        "/app/app/").split("\\?")[0];
                        BuglyLog.d(TAG, "shouldInterceptRequest -> " + f);
                        File file = new File(f);
                        if (file.exists()) {
                            InputStream inputStream = new FileInputStream(file);
                            String mimeType = getMimeTypeForHTTP(f);
                            WebResourceResponse response = new WebResourceResponse(mimeType, "UTF-8", inputStream);
                            return response;
                        } else {
                            BuglyLog.e(TAG, "File not found: " + f);
                        }
                    } catch (Exception e) {
                        BuglyLog.e(TAG, "shouldInterceptRequest -> " + e);
                    }
                }
                if (url.toString().equals("http://127.0.0.1:58131/api/system/bootProgress")) {
                    if (Mobile.isHttpServing()) {
                        try {
                            BuglyLog.w(TAG, "shouldInterceptRequest -> isHttpServing return bootProgress 100");
                            String progressDataJson = createProgressDataJson(100);
                            InputStream inputStream = new ByteArrayInputStream(progressDataJson.getBytes(StandardCharsets.UTF_8));
                            WebResourceResponse response = new WebResourceResponse("application/json", "UTF-8", inputStream);
                            return response;
                        } catch (Exception e) {
                            BuglyLog.e(TAG, "shouldInterceptRequest -> " + e);
                        }
                    }
                }
                // 对于其他请求，继续使用默认的处理方式
                return super.shouldInterceptRequest(view, request);
            }
            /**
             * setWebViewClient 和 setWebChromeClient 并不同，别看走眼了.
             * 对于 POST 请求不会调用此方法
             * @param view The WebView that is initiating the callback.
             * @param request Object containing the details of the request.
             * @return
             */
            @Override
            public boolean shouldOverrideUrlLoading(final WebView view, final WebResourceRequest request) {
                final Uri uri = request.getUrl();
                final String url = uri.toString();
                BuglyLog.w(TAG, "[WebViewClient] shouldOverrideUrlLoading <- "+url);
                if (url.contains("127.0.0.1:58131")) {
                    var AppCheckInState = mmkvGibbet.getString("AppCheckInState", "");
                    if (AppCheckInState.equals("lockScreen")) {
                        try {
                            String encodedUrl = URLEncoder.encode(url, "UTF-8");
                            String gotourl = "http://127.0.0.1:58131/check-auth?to=" + encodedUrl;
                            BuglyLog.w(TAG,"[WebViewClient] shouldOverrideUrlLoading -> " + gotourl);
                            view.loadUrl(gotourl);
                            return true;
                        } catch (Exception e) {
                            // 编码失败的处理
                            BuglyLog.w(TAG, e.toString());
                        }
                    }
                    // 返回 false 则让 WebView 像往常一样继续加载 URL
                    // https://github.com/Hi-Windom/Sillot/issues/990
                    // 修复无网络时小米系统无法加载页面的问题，但是不解决被禁用应用访问网络权限无法加载的问题，这是小米系统的问题，无法解决
                    // 关联 shouldInterceptRequest ，谨慎修改
                    return false;
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
                    androidFeedback(thisActivity);
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

            /**
             * 如果网页存在重定向，onPageFinished 的时候 progress 不一定为 100。
             *  {@link android.webkit.WebChromeClient} 的 onProgressChanged 方法监听也是一样的
             */
            @Override
            public void onPageFinished(WebView view, String url) {
                int progress = view.getProgress();
                BuglyLog.d(TAG, "[WebViewClient] onPageFinished: " + url + " Progress == " + progress);
                if (progress == 100) {
                    view.evaluateJavascript("javascript:document.body.classList.add(\"body--mobile\")", null);
                    bootLogo.postDelayed(() -> {
                        bootProgressBar.setVisibility(View.GONE);
                        bootDetailsText.setVisibility(View.GONE);
                        bootLogo.setVisibility(View.GONE);
                    }, 20);
                }
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                // 在加载页面出现错误时进行处理
                if (error != null) {
                    BuglyLog.e("WebViewClient", "onReceivedError: " + error.getDescription());
                }
                // super.onReceivedError(view, request, error);
            }
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                // 页面开始加载时调用
                BuglyLog.d("WebViewClient", "onPageStarted: " + url);
            }

        });

        final JSAndroid JSAndroid = new JSAndroid(this);
        webView.addJavascriptInterface(JSAndroid, "JSAndroid");
        final WebSettings ws = webView.getSettings();
        U_WebviewKt.applyDefault(ws, 100,
                "SiYuan-Sillot/" + Utils.version + " https://b3log.org/siyuan Android " + ws.getUserAgentString());

        // 使用loadDataWithBaseURL方法加载HTML内容 并不能解决小米手机禁用APP的网络无法加载的问题，因为 HTML 脚本里依旧有 http 请求。
        webView.loadUrl("127.0.0.1:58131/appearance/boot/index.html?v=" + Utils.version);
//        waitForKernelHttpServingWithCoroutines();
        checkWebViewVer(thisActivity, S_Webview.getMinVersion());
        // 增加Javascript异常监控
        CrashReport.setJavascriptMonitor(webView, true);
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
                mCameraUri = openCamera(thisActivity);
                return;
            }

            PopTip.show("Permission denied");
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
        if (event.getRequestCode() == REQUEST_OVERLAY) {
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
            if (event.getRequestCode() == REQUEST_IGNORE_BATTERY_OPTIMIZATIONS_AND_REBOOT) {
                // 不管结果是什么都重启
                S.getEVENTS();
                if (event.getCallback().equals(S_Events.INSTANCE.getCALL_MainActivity_siyuan_1())) {
                    coldRestart(thisActivity, thisActivity.getClass(), webView);
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
        BuglyLog.w(TAG, "onDestroy() invoked");
        isInstanceCreated = false;
        // 注销 EventBus
        EventBus.getDefault().unregister(this);
        AppUtils.unregisterAppStatusChangedListener(this);
        releaseBootService();
        super.onDestroy();
    }

    @Override
    public void onForeground(Activity activity) {
        BuglyLog.w(TAG, "onForeground() invoked");
        if (null != webView) {
            webView.evaluateJavascript("javascript:window.reconnectWebSocket()", null);
        }
    }

    @Override
    public void onBackground(Activity activity) {
        BuglyLog.w(TAG, "onBackground() invoked");
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
            setSillotGibbetCheckInState(webView);
            finishAfterTransition();
//            System.exit(0);
        });
    }
}
