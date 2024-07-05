/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/7/6 上午7:12
 * updated: 2024/7/6 上午7:12
 */

package sc.windom.sofill.compose

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.os.Parcelable
import android.util.Log
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebView.FindListener
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Cookie
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.filled.Webhook
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import com.blankj.utilcode.util.ActivityUtils.startActivity
import com.kongzue.dialogx.dialogs.PopTip
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import sc.windom.sofill.Ss.S_Webview
import sc.windom.sofill.U
import sc.windom.sofill.Us.U_Uri.askIntentForSUS
import sc.windom.sofill.Us.applyDefault
import sc.windom.sofill.Us.fixQQAppLaunchButton
import sc.windom.sofill.Us.injectVConsole
import sc.windom.sofill.Us.thisWebChromeClient
import sc.windom.sofill.Us.thisWebViewClient
import sc.windom.sofill.android.webview.applySystemThemeToWebView
import sc.windom.sofill.compose.theme.activeColor
import sc.windom.sofill.compose.theme.defaultColor
import sc.windom.sofill.compose.theme.disabledColor
import sc.windom.sofill.pioneer.getSavedValue

private val thisWebView: MutableState<WebView?> = mutableStateOf(null)

@Parcelize
sealed class MenuOptionState : Parcelable {
    data object Disabled : MenuOptionState()
    data object Default : MenuOptionState()
    data object Active : MenuOptionState()
}

@Parcelize
data class MenuOption(
    val title: String,
    val icon: @RawValue ImageVector,
    val iconInActive: @RawValue ImageVector = icon,
    val titleInActive: String = title,
    val state: MenuOptionState = MenuOptionState.Default,
    val isActive: @RawValue MutableState<Boolean> = mutableStateOf(state == MenuOptionState.Active),
    val canToggle: Boolean = false, // 表示选项是否可以在 Default 和 Active 之间切换
    val closeMenuAfterClick: Boolean = state != MenuOptionState.Disabled, // 点击后是否关闭菜单（需要重新渲染）, 默认值为非禁用则为 true
    val onClick: () -> Unit,
) : Parcelable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingBottomSheet(showSettings: MutableState<Boolean>) {
    val scope = rememberCoroutineScope()
    val skipPartiallyExpanded by rememberSaveable { mutableStateOf(true) }
    val bottomSheetState =
        rememberModalBottomSheetState(skipPartiallyExpanded = skipPartiallyExpanded)

    if (showSettings.value) {
        ModalBottomSheet(
            modifier = Modifier.fillMaxSize(),
            shape = RectangleShape, // 使用RectangleShape来移除圆角
            onDismissRequest = {
                scope
                    .launch { bottomSheetState.hide() }
                    .invokeOnCompletion {
                        if (!bottomSheetState.isVisible) {
                            showSettings.value = false
                        }
                    }
            },
            sheetState = bottomSheetState
        ) {
            SettingScreen(thisWebView)
        }
    }
}

@Composable
fun Menu(
    options: List<MenuOption>,
    columnCount: Int,
    openBottomSheet: MutableState<Boolean>
) {
    // 一行 n 列
    options.chunked(columnCount).forEach { rowOptions ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly // 均匀分布菜单项
        ) {
            rowOptions.forEach { option ->
                val tint = when (option.state) {
                    MenuOptionState.Disabled -> disabledColor()
                    MenuOptionState.Default -> defaultColor()
                    MenuOptionState.Active -> activeColor()
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = tint != Color.Gray) {
                            if (option.canToggle) {
                                // 只有可切换的选项会在点击时切换状态
                                option.isActive.value = !option.isActive.value
                            }
                            if (option.closeMenuAfterClick) {
                                openBottomSheet.value = false
                            }
                            option.onClick()
                        }
                        .weight(1f), // 均分宽度
                    horizontalAlignment = Alignment.CenterHorizontally // 居中图标和文本
                ) {
                    Icon(
                        if (option.canToggle && option.isActive.value) option.iconInActive else option.icon,
                        contentDescription = null,
                        tint = if (option.canToggle && option.isActive.value) Color.Blue else tint,
                        modifier = Modifier.size(34.dp)
                    )
                    Text(
                        text = if (option.canToggle && option.isActive.value) option.titleInActive else option.title,
                        color = if (option.canToggle && option.isActive.value) Color.Blue else tint,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

/**
 * 参考了 https://www.composables.com/material3/modalbottomsheet
 * @param openBottomSheet 控制是否渲染
 * @param fullHeight 是否全高展示
 * @param noRounded 是否不使用圆角，默认与 `fullHeight` 一致
 * @param sheetContent 内容
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialBottomMenu(
    openBottomSheet: MutableState<Boolean>,
    fullHeight: Boolean = false,
    noRounded: Boolean = fullHeight,
    sheetContent: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    val skipPartiallyExpanded by rememberSaveable { mutableStateOf(fullHeight) }  // 全高展示则必须跳过
    val bottomSheetState =
        rememberModalBottomSheetState(skipPartiallyExpanded = skipPartiallyExpanded)
    // Sheet content
    if (openBottomSheet.value) {
        ModalBottomSheet(
            modifier = if (fullHeight) Modifier
                .zIndex(999f)
                .fillMaxSize() else Modifier.zIndex(999f),
            shape = if (noRounded) RectangleShape else RoundedCornerShape(
                topStart = 22.dp,
                topEnd = 22.dp
            ),
            onDismissRequest = {
                scope
                    .launch { bottomSheetState.hide() }
                    .invokeOnCompletion {
                        if (!bottomSheetState.isVisible) {
                            openBottomSheet.value = false
                        }
                    }
            },
            sheetState = bottomSheetState
        ) {
            Column {
                sheetContent()
                HorizontalDivider(
                    modifier = Modifier
                        .height(6.dp), thickness = 0.dp, color = Color.Transparent
                )
            }
        }
    }
}

@Composable
fun GoToOption(
    ourl: String,
    onSearchInWebpage: (String) -> Unit, // 处理网页内搜索事件
    onNavigate: (String) -> Unit, // 处理导航事件
) {
    var url by remember { mutableStateOf(ourl) } // 用于存储用户输入的 URL
    var surl by remember { mutableStateOf(ourl) } // 用于存储搜索的 URL
    val keyboardController = LocalSoftwareKeyboardController.current // 用于控制键盘
    val SE = MMKV.defaultMMKV().getSavedValue(
        "WebViewContainer@selectedOption_搜索引擎",
        S_Webview.searchEngines.keys.first()
    )
    val SEV = S_Webview.searchEngines[SE]

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("换行将被忽略，无需担心", modifier = Modifier.padding(6.dp))
        Text("当前搜索引擎为$SE，可在设置中更改", modifier = Modifier.padding(6.dp))
        HorizontalDivider(
            modifier = Modifier.height(10.dp), thickness = 0.dp, color = Color.Transparent
        )
        TextField(
            value = url,
            placeholder = { Text("搜索或键入 Web 地址") },
            onValueChange = { newValue: String ->
                url = newValue
                surl = "https://$SEV$url"
            },
            singleLine = false,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp, 6.dp)
                .minimumInteractiveComponentSize() // 布局意外时确保可以输入
                .wrapContentHeight() // 匹配内容高度
                .weight(1f) // 使用 weight 来占满剩余空间
        )
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp, 3.dp),
            onClick = {
                keyboardController?.hide()
                onNavigate(url)
            }
        ) {
            Text("前往")
        }
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp, 3.dp),
            onClick = {
                keyboardController?.hide()
                onNavigate(surl)
            }
        ) {
            Text("搜索")
        }
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp, 3.dp),
            onClick = {
                keyboardController?.hide()
                onSearchInWebpage(url)
            }
        ) {
            Text("网页内搜索")
        }
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp, 3.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
            ),
            onClick = {
                url = ""
            }
        ) {
            Text("清除")
        }
        HorizontalDivider(
            modifier = Modifier
                .height(6.dp), thickness = 0.dp, color = Color.Transparent
        )
    }
}

@Composable
fun FullScreenWebView(activity: Activity, originUrl: String, onDismiss: () -> Unit) {
    val TAG = "FullScreenWebView"
    val uriHandler = LocalUriHandler.current
    val gotoUrl: MutableState<String?> = rememberSaveable { mutableStateOf(originUrl) }
    val currentUrl = rememberSaveable { mutableStateOf("") }
    val canGoBack = rememberSaveable { mutableStateOf(false) }
    val canGoForward = rememberSaveable { mutableStateOf(false) }
    val expanded = rememberSaveable { mutableStateOf(false) } // 控制菜单面板
    val showSettings = rememberSaveable { mutableStateOf(false) } // 控制设置面板
    val showGoToOption = rememberSaveable { mutableStateOf(false) } // 控制 GOTO 前往
    val inSearchInWebpage = rememberSaveable { mutableStateOf(false) } // 是否处于页面搜索
    val openBrowserSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { /* Handle the result if needed */ }
    LaunchedEffect(originUrl) {
        gotoUrl.value = originUrl
    }
    val menuOptions = listOf(
        MenuOption("关于", Icons.Filled.Info, state = MenuOptionState.Disabled) { /* 点击事件 */ },
        MenuOption(
            "设置",
            Icons.Filled.Settings
        ) { showSettings.value = true },
        MenuOption("设为默认", Icons.Filled.Webhook) {
            val intent = Intent(android.provider.Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
            openBrowserSettingsLauncher.launch(intent)
        },
        MenuOption("清除 Cookie", Icons.Filled.Cookie) {
            CookieManager.getInstance().apply {
                removeAllCookies { success ->
                    if (success) {
                        thisWebView.value?.clearCache(true)
                        PopTip.show("<(￣︶￣)↗[success]")
                        thisWebView.value?.reload()
                    } else {
                        PopTip.show(" ￣へ￣ [failed]")
                    }
                }
            }
        },
        MenuOption(
            "桌面版网页", Icons.Filled.DesktopWindows, canToggle = true,
            iconInActive = Icons.Filled.PhoneAndroid,
            titleInActive = "移动端网页"
        ) {
            // 切换用户代理字符串
            thisWebView.value?.settings?.apply {
                if (userAgentString.contains("Mobile")) {
                    // 设置为桌面版用户代理
                    userAgentString = S_Webview.UA_win10
                    PopTip.show("已切换到桌面版网页")
                } else {
                    // 设置为移动端用户代理
                    userAgentString = S_Webview.UA_edge_android
                    PopTip.show("已切换到移动端网页")
                }
            }
            thisWebView.value?.reload()
        },
        MenuOption(
            "前往/搜索",
            Icons.Filled.TravelExplore,
        ) {
            showGoToOption.value = true
        },
        MenuOption(
            "翻译",
            Icons.Filled.Translate,
            state = MenuOptionState.Disabled
        ) { /* 点击事件 */ },
        MenuOption("分享", Icons.Filled.Share) {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, currentUrl.value)
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        },
        MenuOption("默认打开", Icons.Filled.OpenInBrowser) { uriHandler.openUri(currentUrl.value) },
        MenuOption(
            "历史记录",
            Icons.Filled.History,
            state = MenuOptionState.Disabled
        ) { /* 点击事件 */ },
        MenuOption("刷新", Icons.Filled.Refresh) { thisWebView.value?.reload() },
        MenuOption("退出", Icons.Filled.PowerSettingsNew) { onDismiss.invoke() },
    )


    DisposableEffect(Unit) {
        // 在 Composition 销毁时
        onDispose { }
    }

    // 菜单面板
    MaterialBottomMenu(expanded) {
        Menu(
            menuOptions,
            4,
            expanded
        )
    }

    // 设置面板
    SettingBottomSheet(showSettings)

    // GOTO面板
    MaterialBottomMenu(showGoToOption, true) {
        GoToOption(currentUrl.value, onNavigate = { it1 ->
            Log.w(TAG, "goto -> $it1")
            gotoUrl.value = it1
            showGoToOption.value = false
        }, onSearchInWebpage = { it1 ->
            Log.w(TAG, "findAllAsync -> $it1")
            thisWebView.value?.findAllAsync(it1)
            showGoToOption.value = false
            inSearchInWebpage.value = true
        })
    }

    Scaffold(
        modifier = Modifier.imePadding(), // 布局适配软键盘，一般来说不需要嵌套声明
        content = { padding ->
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                WebViewPage(activity, gotoUrl, currentUrl, canGoBack, canGoForward)
            }

        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 58.dp,
                contentPadding = PaddingValues(2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp)
                    .height(36.dp)
                    .zIndex(999f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { thisWebView.value?.goBack() },
                        enabled = canGoBack.value
                    ) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                    IconButton(
                        onClick = { thisWebView.value?.goForward() },
                        enabled = canGoForward.value
                    ) {
                        Icon(Icons.Filled.ArrowForward, contentDescription = "Forward")
                    }
                    IconButton(onClick = { expanded.value = !expanded.value }) {
                        if (expanded.value) Icon(
                            Icons.Filled.MoreVert,
                            contentDescription = "More"
                        ) else Icon(Icons.Filled.MoreHoriz, contentDescription = "More")
                    }
                    if (inSearchInWebpage.value) {
                        IconButton(
                            onClick = {
                                thisWebView.value?.clearMatches()
                                inSearchInWebpage.value = false
                            }
                        ) {
                            Icon(Icons.Filled.Clear, contentDescription = "退出页面搜索")
                        }
                        IconButton(
                            onClick = { thisWebView.value?.findNext(true) }
                        ) {
                            Icon(
                                Icons.Filled.ArrowBackIos,
                                contentDescription = "上一个（页面搜索结果）"
                            )
                        }
                        IconButton(
                            onClick = { thisWebView.value?.findNext(true) }
                        ) {
                            Icon(
                                Icons.Filled.ArrowForwardIos,
                                contentDescription = "上一个（页面搜索结果）"
                            )
                        }
                    } else {
                        IconButton(
                            onClick = { thisWebView.value?.evaluateJavascript("window.scrollTo(0, 0)") { } }
                        ) {
                            Icon(Icons.Filled.ArrowUpward, contentDescription = "页面顶部")
                        }
                        IconButton(
                            onClick = { thisWebView.value?.evaluateJavascript("window.scrollTo(0, document.body.scrollHeight)") { } }
                        ) {
                            Icon(Icons.Filled.ArrowDownward, contentDescription = "页面底部")
                        }
                    }
                }
            }
        }
    )
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun WebViewPage(
    activity: Activity,
    gotoUrl: MutableState<String?>,
    currentUrl: MutableState<String>,
    canGoBack: MutableState<Boolean>,
    canGoForward: MutableState<Boolean>
) {
    val TAG = "WebViewPage"
    val mmkv = MMKV.defaultMMKV()
    val sliderState_webViewTextZoom =
        mmkv.getSavedValue("WebViewContainer@sliderState_webViewTextZoom", 100)
    // 使用AndroidView嵌入WebView
    AndroidView(modifier = Modifier.fillMaxSize(), factory = {
        WebView(it).apply {
            thisWebView.value = this
            val cm = CookieManager.getInstance()
            cm.setAcceptCookie(true)
            cm.setAcceptThirdPartyCookies(this, true)
            val ws = this.settings
            ws.applyDefault(sliderState_webViewTextZoom)
            this.webViewClient = thisWebViewClient(
                activity,
                currentUrl,
                canGoBack,
                canGoForward,
                ::handleUrlLoading,
                ::handlePageFinished
            )
            this.webChromeClient = thisWebChromeClient(activity)
            this.setDownloadListener { url, userAgent, contentDisposition, mimeType, contentLength ->
                activity.startDownload(url, userAgent, contentDisposition, mimeType)
            }
            this.setFindListener(object : FindListener {
                override fun onFindResultReceived(
                    activeMatchOrdinal: Int,
                    numberOfMatches: Int,
                    isDoneCounting: Boolean
                ) {
                    if (isDoneCounting) {
                        val thisNo = activeMatchOrdinal + 1
                        Log.d(TAG, "numberOfMatches: $numberOfMatches, activeMatchOrdinal: $thisNo")
                        if (numberOfMatches > 0) {
                            PopTip.show("$thisNo / $numberOfMatches")
                        } else {
                            PopTip.show("未共找到匹配项")
                        }
                    }
                }
            })
        }
    }, update = {
        Log.w(TAG, "update -> ${gotoUrl.value}")
        gotoUrl.value?.let { it1 ->
            when {
                it1 == "action?=Logout" -> {
                    // 隐藏处理过程
                    CookieManager.getInstance().apply {
                        removeAllCookies { success ->
                            if (success) {
                                thisWebView.value?.clearCache(true)
                                PopTip.show("<(￣︶￣)↗[success]")
                            } else {
                                PopTip.show(" ￣へ￣ [failed]")
                            }
                        }
                    }
                    return@AndroidView
                }

                it1.startsWith("wtloginmqq:") -> {
                    activity.askIntentForSUS(it1)
                    return@AndroidView
                }

                else -> {
                    it.loadUrl(it1)
                }
            }
        }
        gotoUrl.value = null // 避免重组副作用
    })
}


private fun handlePageFinished(activity: Activity, view: WebView, url: String) {
    applySystemThemeToWebView(activity, view)
    if (url.startsWith("https://xui.ptlogin2.qq.com/")) {
        view.fixQQAppLaunchButton()
    }
    view.injectVConsole()
}

@OptIn(DelicateCoroutinesApi::class)
private fun handleUrlLoading(activity: Activity, request: WebResourceRequest): Boolean {
    val TAG = "handleUrlLoading"
    val url = request.url.toString()
    val _url = U.replaceScheme_deepDecode(url, "googlechrome://", "slld246://")
    val real_url = U.replaceEncodeScheme(url, "googlechrome://", "slld246://")
    Log.d(
        TAG,
        "[handleUrlLoading] isForMainFrame:${request.isForMainFrame} isRedirect:${request.isRedirect} method:${request.method} " +
                "\n$_url -> $real_url"
    )

    return if (_url.startsWith("mqq://") || _url.startsWith("wtloginmqq://") || _url.startsWith(
            "sinaweibo://"
        )
    ) {
        activity.askIntentForSUS(_url, real_url)
    } else {
        // WebView.setDownloadListener 是更好的方案，这里保留历史代码，后续版本清理
        // 在IO线程尝试下载，不阻塞
//        GlobalScope.launch(Dispatchers.IO) {
//            if (request.isRedirect) {
//                return@launch // GitHub Assets 请求重定向导致重复下载问题
//            }
//            // 发送HEAD请求以获取Content-Type
//            var connection: HttpURLConnection? = null
//            try {
//                connection = URL(url).openConnection() as HttpURLConnection
//                connection.requestMethod = "HEAD"
//                connection.connect()
//                val contentType = connection.contentType
//
//                // 获取服务器提供的文件名
//                val contentDisposition = connection.getHeaderField("Content-Disposition")
//                val fileName =
//                    if (contentDisposition != null && contentDisposition.contains("filename=")) {
//                        contentDisposition.substringAfter("filename=").replace("\"", "")
//                    } else {
//                        URLUtil.guessFileName(url, null, contentType)
//                    }
//                Log.d(TAG, "$contentDisposition, $contentType, $fileName")
//                if (contentType != null && isCommonSupportDownloadMIMEType(contentType, fileName)) {
//                    val mmkv = MMKV.defaultMMKV()
//                    val switchState_使用系统自带下载器下载文件 = mmkv.getSavedValue(
//                        "WebViewContainer@switchState_使用系统自带下载器下载文件",
//                        false
//                    )
//
//
//                    if (switchState_使用系统自带下载器下载文件) {
//                        // 创建下载请求
//                        val downloadRequest = DownloadManager.Request(Uri.parse(url))
//                        downloadRequest.apply {
//                            setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
//                            setTitle(fileName)
//                            setDescription("Downloading file...")
//                            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
//                            setDestinationInExternalPublicDir(
//                                Environment.DIRECTORY_DOWNLOADS,
//                                fileName
//                            )
//                            setAllowedOverMetered(true)
//                            setAllowedOverRoaming(true)
//                            setMimeType(contentType)
//                            addRequestHeader("User-Agent", S_Webview.UA_edge_android)
//                        }
//                        val downloadManager =
//                            activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
//                        // 设置唯一的请求标识符
//                        val requestId = url.hashCode().toLong()
//                        // 尝试查询是否已经存在相同的下载任务
//                        val cursor =
//                            downloadManager.query(DownloadManager.Query().setFilterById(requestId))
//                        if (cursor.moveToFirst()) {
//                            // 如果已经存在相同的下载任务，则不重新下载
//                            PopTip.show("文件已在下载队列中")
//                        } else {
//                            // 如果不存在相同的下载任务，则添加到下载队列
//                            downloadManager.enqueue(downloadRequest)
//                            PopTip.show("开始下载文件")
//                        }
//                    } else {
//                        val sendIntent: Intent = Intent().apply {
//                            action = Intent.ACTION_SEND
//                            putExtra(Intent.EXTRA_TEXT, url)
//                            type = "text/plain"
//                        }
//
//                        val shareIntent = Intent.createChooser(sendIntent, null)
//                        startActivity(shareIntent)
//                    }
//
//
//                }
//            } catch (e: Exception) {
//                Log.e(TAG, "Error while checking Content-Type: ", e)
//            } finally {
//                connection?.disconnect()
//            }
//        }

        false
    }
}

@OptIn(DelicateCoroutinesApi::class)
private fun Activity.startDownload(
    url: String,
    userAgent: String,
    contentDisposition: String,
    mimeType: String
) {
    val TAG = "startDownload"
    val activity = this
    val fileName = URLUtil.guessFileName(url, null, mimeType)
    // 显示一个对话框，询问用户是否想要下载文件
    AlertDialog.Builder(activity)
        .setTitle("可下载文件 $fileName")
        .setMessage("您希望如何处理？点击空白处放弃处理并关闭对话框。下载链接：$url")
        .setPositiveButton("直接下载") { _, _ ->
            // 在IO线程尝试下载，不阻塞
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    // 创建下载请求
                    val downloadRequest = DownloadManager.Request(Uri.parse(url))
                    downloadRequest.apply {
                        setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
                        setTitle(fileName)
                        setDescription("Downloading file...")
                        setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        setDestinationInExternalPublicDir(
                            Environment.DIRECTORY_DOWNLOADS,
                            fileName
                        )
                        setAllowedOverMetered(true)
                        setAllowedOverRoaming(true)
                        setMimeType(mimeType)
                        addRequestHeader("User-Agent", S_Webview.UA_edge_android)
                    }
                    val downloadManager =
                        activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    // 设置唯一的请求标识符
                    val requestId = url.hashCode().toLong()
                    // 尝试查询是否已经存在相同的下载任务
                    val cursor =
                        downloadManager.query(DownloadManager.Query().setFilterById(requestId))
                    if (cursor.moveToFirst()) {
                        // 如果已经存在相同的下载任务，则不重新下载
                        PopTip.show("文件已在下载队列中")
                    } else {
                        // 如果不存在相同的下载任务，则添加到下载队列
                        downloadManager.enqueue(downloadRequest)
                        PopTip.show("开始下载文件")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error: ", e)
                }
            }
        }
        .setNegativeButton("分享下载链接") { _, _ ->
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, url)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }
        .show()
}