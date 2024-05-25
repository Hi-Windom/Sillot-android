package org.b3log.siyuan.ld246

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.util.Base64
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.webkit.CookieManager
import android.webkit.CookieSyncManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.twotone.Article
import androidx.compose.material.icons.automirrored.twotone.Reply
import androidx.compose.material.icons.twotone.AccountCircle
import androidx.compose.material.icons.twotone.Album
import androidx.compose.material.icons.twotone.Attribution
import androidx.compose.material.icons.twotone.CenterFocusWeak
import androidx.compose.material.icons.twotone.Cookie
import androidx.compose.material.icons.twotone.Navigation
import androidx.compose.material.icons.twotone.OpenInBrowser
import androidx.compose.material.icons.twotone.Person
import androidx.compose.material.icons.twotone.Quickreply
import androidx.compose.material.icons.twotone.SafetyCheck
import androidx.compose.material.icons.twotone.Swipe
import androidx.compose.material.icons.twotone.TextFields
import androidx.compose.material.icons.twotone.Token
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.text.HtmlCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Scale
import coil.size.Size
import com.kongzue.dialogx.dialogs.FullScreenDialog
import com.kongzue.dialogx.dialogs.InputDialog
import com.kongzue.dialogx.dialogs.PopNotification
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.interfaces.DialogLifecycleCallback
import com.kongzue.dialogx.interfaces.OnBindView
import com.kongzue.dialogx.util.views.ActivityScreenShotImageView
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch
import org.b3log.siyuan.CascadeMaterialTheme
import org.b3log.siyuan.R
import org.b3log.siyuan.S
import org.b3log.siyuan.Us
import org.b3log.siyuan.appUtils.HWs
import org.b3log.siyuan.compose.MyTagHandler
import org.b3log.siyuan.compose.components.CommonTopAppBar
import org.b3log.siyuan.ld246.api.ApiServiceNotification
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class HomeActivity : ComponentActivity() {
    val TAG = "ld246-HomeActivity"
    var mmkv: MMKV = MMKV.defaultMMKV()
    var token = Us.getDecryptedToken(mmkv, S.KEY_TOKEN_ld246, S.KEY_AES_TOKEN_ld246)
    val ua = "Sillot-anroid/0.35"
    private var exitTime: Long = 0
    private var fullScreenDialog: FullScreenDialog? = null
    private var openUrlExternal: Boolean = false
    private val titles = listOf("回帖", "评论", "回复", "提及", "关注", "积分")
    private val titles_type = listOf("commented", "comment2ed", "reply", "at", "following", "point")
    private val titles_icons = listOf(
        Icons.AutoMirrored.TwoTone.Article,
        Icons.TwoTone.Quickreply,
        Icons.AutoMirrored.TwoTone.Reply,
        Icons.TwoTone.Attribution,
        Icons.TwoTone.CenterFocusWeak,
        Icons.TwoTone.Album
    )
    private var LockNoteType_EN: String = titles_type[0]
    val mapEmpty = mutableMapOf<String, List<ld246_Response_Data_Notification>?>().apply {
        titles.associateWithTo(this) { emptyList() }
    }
    var map: MutableMap<String, List<ld246_Response_Data_Notification>?> = mapEmpty
    private var job: Job? = null
    var viewmodel: NotificationsViewModel? = null
    var retrofit: Retrofit? = null
    var apiService: ApiServiceNotification? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent
        val uri = intent.data
        Log.i(TAG, "onCreate() invoked")
        val scheme = uri?.scheme
        val host = uri?.host
        Log.d(TAG, "scheme: $scheme, host:$host")
        // 设置沉浸式通知栏
        window.setDecorFitsSystemWindows(false)
        window.decorView.setOnApplyWindowInsetsListener { _, insets ->
            insets
        }
        setContent {
            CascadeMaterialTheme {
                UI(intent)
            }
        }
        viewmodel = NotificationsViewModel()
        // 创建Retrofit实例
        retrofit = Retrofit.Builder()
            .baseUrl("https://${S.HOST_ld246}/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        // 创建API服务实例
        apiService = retrofit!!.create(ApiServiceNotification::class.java)
        ActivityScreenShotImageView.hideContentView =
            true; // https://github.com/kongzue/DialogX/wiki/%E5%85%A8%E5%B1%8F%E5%AF%B9%E8%AF%9D%E6%A1%86-FullScreenDialog
        // 获取OnBackPressedDispatcher
        val onBackPressedDispatcher = onBackPressedDispatcher
        // 设置OnBackPressedCallback
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 在这里处理后退逻辑
                if (fullScreenDialog?.isShow() == true) {
                    // 如果全屏对话框正在显示，优先处理对话框内的返回逻辑
                    val webView =
                        fullScreenDialog?.getCustomView()?.findViewById<WebView>(R.id.webView)
                    if (webView?.canGoBack() == true) {
                        webView.goBack()
                    } else {
                        fullScreenDialog?.dismiss()
                    }
                } else {
                    if (System.currentTimeMillis() - exitTime > 2000) {
                        PopTip.show("再按一次结束当前活动")
                        exitTime = System.currentTimeMillis()
                    } else {
                        HWs.getInstance().vibratorWaveform(
                            applicationContext,
                            longArrayOf(0, 30, 25, 40, 25, 10),
                            intArrayOf(2, 4, 3, 2, 2, 2),
                            -1
                        )
                        try {
                            Thread.sleep(200)
                        } catch (e: InterruptedException) {
                            Log.w(TAG, e.toString())
                        }
                        Log.w(TAG, "再见")
                        finish()
//                        exitProcess(0)
                    }
                }
                HWs.getInstance().vibratorWaveform(
                    applicationContext,
                    longArrayOf(0, 30, 25, 40, 25),
                    intArrayOf(9, 2, 1, 7, 2),
                    -1
                )
            }
        })

        if (uri != null) {
            if (S.isUriMatched(uri, S.case_ld246_1) || S.isUriMatched(
                    uri,
                    S.case_ld246_2
                ) || S.isUriMatched(uri, S.case_github_1) || uri.scheme?.startsWith("http") == true
            ) {
                showFullScreenDialog(uri.toString())
            }
        }
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class, DelicateCoroutinesApi::class)
    @Composable
    private fun UI(intent: Intent?) {
        val uri = intent?.data
        val Lcc = LocalContext.current
        val isTabChanged = rememberSaveable { mutableStateOf(titles[0]) }
        val PullToRefreshState = rememberPullToRefreshState()
        val isMenuVisible = rememberSaveable { mutableStateOf(false) }
        val isShowBottomText = rememberSaveable { mutableStateOf(false) }
        val isUserPage = rememberSaveable { mutableStateOf(false) }
        var userPageData by remember { mutableStateOf<User>(User()) }

        LaunchedEffect(isUserPage.value) {
            if (isUserPage.value) {
                try {
                    val caller = apiService?.apiV2UserGet(token, ua)
                    caller?.enqueue(object : Callback<ld246_Response> {
                        override fun onResponse(
                            p0: Call<ld246_Response>,
                            p1: Response<ld246_Response>
                        ) {
                            if (p1.isSuccessful) {
                                p1.body()?.data?.user?.let { userPageData = it };
                            } else {
                                PopNotification.show(p1.code(), p1.toString()).noAutoDismiss()
                            }
                        }

                        override fun onFailure(p0: Call<ld246_Response>, p1: Throwable) {
                            //
                        }
                    })
                } catch (e: Exception) {
                    PopNotification.show(TAG, e.toString()).noAutoDismiss()
                }
            }
        }

        DisposableEffect(viewmodel) {
            onDispose {
                // 在这里释放资源
                job?.cancel()
            }
        }
        // 将LiveData转换为Compose可以理解的State对象。这样，每当LiveData的值发生变化时，Compose就会自动重组使用该State的UI部分。
        val observeNotifications = viewmodel!!.notificationsMap.observeAsState(listOf())
        if (PullToRefreshState.isRefreshing) {
            LaunchedEffect(true) {
                viewmodel!!.fetchNotifications(PullToRefreshState, apiService!!, isTabChanged)
                delay(200) // 避免接口请求频繁
            }
        }

        LaunchedEffect(Unit) {
            snapshotFlow { isTabChanged.value } // 创建一个Flow，它在每次isTabChanged.value变化时发出（启动时也会执行一次）
                .conflate() // 当新值到来时，如果上一个值还没被处理，就忽略它
                .collectLatest { // collectLatest会取消当前正在进行的操作，并开始新的操作
                    Log.d("LaunchedEffect-snapshotFlow", isTabChanged.value)
                    viewmodel!!.fetchNotifications(PullToRefreshState, apiService!!, isTabChanged)
                    delay(200) // 避免接口请求频繁
                }
        }


        Scaffold(
            topBar = {
                CommonTopAppBar(
                    "汐洛链滴社区客户端",
                    uri,
                    isMenuVisible,
                    additionalMenuItem = {
                        AddDropdownMenu(onDismiss = {
                            isMenuVisible.value = false
                        }, isShowBottomText, isTabChanged)
                    }) {
                    // 将Context对象安全地转换为Activity
                    if (Lcc is Activity) {
                        Lcc.finish() // 结束活动
                    }
                }
            }, modifier = Modifier
                .background(Color.Gray)
                .nestedScroll(PullToRefreshState.nestedScrollConnection)
        ) {
            Box(
                Modifier
                    .padding(it)
                    .fillMaxSize()
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (isUserPage.value) {
                        UserPage(userPageData)
                    } else {
                        NotificationsScreen(observeNotifications)
                    }

                    SecondaryTextTabs(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(top = 31.dp),
                        isTabChanged,
                        isUserPage,
                        isShowBottomText
                    )
                }
                if (PullToRefreshState.isRefreshing) {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                } else {
                    LinearProgressIndicator(
                        progress = { PullToRefreshState.progress },
                        Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun AddDropdownMenu(
        onDismiss: () -> Unit,
        isShowBottomText: MutableState<Boolean>,
        isTabChanged: MutableState<String>
    ) {
        DropdownMenuItem(
            text = { Text("手动刷新") },
            leadingIcon = { Icon(Icons.TwoTone.Swipe, contentDescription = null) },
            onClick = {
                onDismiss()
                viewmodel!!.fetchNotifications(null, apiService!!, isTabChanged)
            }
        )
        DropdownMenuItem(
            text = {
                if (isShowBottomText.value) {
                    Text("图标底部导航")
                } else {
                    Text("文字底部导航")
                }
            },
            leadingIcon = if (isShowBottomText.value) {
                { Icon(Icons.TwoTone.Navigation, contentDescription = null) }
            } else {
                { Icon(Icons.TwoTone.TextFields, contentDescription = null) }
            },
            onClick = {
                onDismiss()
                isShowBottomText.value = !isShowBottomText.value
            }
        )
        DropdownMenuItem(
            text = {
                if (openUrlExternal) {
                    Text("应用内打开链接")
                } else {
                    Text("浏览器打开链接")
                }
            },
            leadingIcon = {
                if (openUrlExternal) {
                    Icon(Icons.TwoTone.SafetyCheck, contentDescription = null)
                } else {
                    Icon(Icons.TwoTone.OpenInBrowser, contentDescription = null)
                }
            },
            onClick = {
                onDismiss()
                openUrlExternal = !openUrlExternal
            }
        )
        DropdownMenuItem(
            text = { Text("清除 Cookie") },
            leadingIcon = { Icon(Icons.TwoTone.Cookie, contentDescription = null) },
            onClick = {
                onDismiss()
                showFullScreenDialog("action?=Logout")
            }
        )
        DropdownMenuItem(
            text = { Text("链滴 API TOKEN") },
            leadingIcon = { Icon(Icons.TwoTone.Token, contentDescription = null) },
            onClick = {
                onDismiss()
                val deToken = Us.getDecryptedToken(mmkv, S.KEY_TOKEN_ld246, S.KEY_AES_TOKEN_ld246)
                InputDialog(
                    "🛸 API TOKEN",
                    "可在社区 设置 - 账号 中找到 API Token，固定以 'token ' 开头\n\n温馨提示：应用存储 Token 时进行了一定的处理，且不会传输到网络，但用户仍需注意防止 Token 泄露！建议使用前先阅读源代码",
                    "确定",
                    "取消",
                    deToken?.let { deToken } ?: run { "token " }
                )
                    .setCancelable(false)
                    .setOkButton { baseDialog, v, inputStr ->
                        token = inputStr
                        // 生成AES密钥
                        val aesKey = Us.generateAesKey()
                        // 注意：这里需要将SecretKey转换为可以存储的格式，例如转换为字节数组然后进行Base64编码
                        val encodedKey = Base64.encodeToString(aesKey.encoded, Base64.DEFAULT)
                        // 加密Token
                        val encryptedToken = Us.encryptAes(inputStr, aesKey)
                        // 将加密后的Token存储到MMKV中
                        mmkv.encode(S.KEY_AES_TOKEN_ld246, encodedKey)
                        mmkv.encode(S.KEY_TOKEN_ld246, encryptedToken)
                        PopNotification.show(
                            "TOKEN已更新（${
                                Us.displayTokenLimiter(
                                    inputStr,
                                    "token ".length + 4,
                                    4
                                )
                            }）",
                            "请手动刷新界面，通常为下拉刷新"
                        ).noAutoDismiss()
                        false
                    }
                    .show()
            },
        )
    }

    @Composable
    fun SecondaryTextTabs(
        modifier: Modifier,
        isTabChanged: MutableState<String>,
        isUserPage: MutableState<Boolean>, isShowBottomText: MutableState<Boolean>,
    ) {
        // REF https://www.composables.com/material3/tabrow
        var state by remember { mutableStateOf(0) }
        val selectedContentColor = S.C.btn_bgColor_pink.current    // 选中时文字颜色
        val unselectedContentColor = Color.Gray // 未选中时文字颜色

        Column(modifier = modifier) {
            TabRow(
                selectedTabIndex = state,
                containerColor = MaterialTheme.colorScheme.surface, // 设置TabRow的背景颜色
                indicator = { tabPositions ->
                    SecondaryIndicator(
                        modifier = Modifier
                            .tabIndicatorOffset(tabPositions[state])
                            .height(0.dp), // 设置选中指示器的高度 （切换动画有卡顿感，干脆隐藏了）
                        color = selectedContentColor // 设置选中指示器的颜色
                    )
                }
            ) {
                titles.forEachIndexed { index, title ->
                    Tab(
                        selected = state == index,
                        onClick = {
                            state = index
                            LockNoteType_EN = titles_type[index];
                            isTabChanged.value = title
                            isUserPage.value = false
                        },
                        selectedContentColor = selectedContentColor,
                        unselectedContentColor = unselectedContentColor,
                        text = {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.height(46.dp),
                            ) {
                                if (!isShowBottomText.value) {
                                    Icon(
                                        imageVector = titles_icons[index],
                                        modifier = Modifier
                                            .fillMaxSize(),
                                        contentDescription = title
                                    )
                                } else {
                                    Text(
                                        text = title,
                                        maxLines = 2,
                                        modifier = Modifier.fillMaxSize(),
                                        overflow = TextOverflow.Ellipsis,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    )
                }
                Tab(
                    selected = state == titles.size,
                    onClick = {
                        state = titles.size
                        isUserPage.value = true
                    },
                    selectedContentColor = selectedContentColor,
                    unselectedContentColor = unselectedContentColor,
                    text = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.height(46.dp),
                        ) {
                            if (!isShowBottomText.value) {
                                Icon(
                                    imageVector = Icons.TwoTone.Person,
                                    modifier = Modifier
                                        .fillMaxSize(),
                                    contentDescription = "user"
                                )
                            } else {
                                Text(
                                    text = "用户",
                                    maxLines = 2,
                                    modifier = Modifier.fillMaxSize(),
                                    overflow = TextOverflow.Ellipsis,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                )
            }
        }
    }


    @Composable
    fun UserPage(user: User) {
        val Lcc = LocalContext.current
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            // 高斯模糊背景
            AsyncImage(
                model = user.userCardBImgURL,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp)
                    .blur(radius = 20.dp) // 这里添加高斯模糊效果
            )
            // 这里可以放置其他内容，它们将显示在背景图片之上

            Column(
                modifier = Modifier
                    .padding(6.dp),
            ) {
                // 用户头像和基本信息
                Row(
                    modifier = Modifier
                        .padding(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clickable {
                                Lcc.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://${S.HOST_ld246}/member/${user.userName}")
                                    )
                                )
                            }, contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(user.userAvatarURL)
                                .size(Size(300, 300))
                                .scale(Scale.FILL)
                                .build(),
                            contentDescription = "User Avatar",
                            modifier = Modifier
                                .size(150.dp)
                                .clip(CircleShape), // 使用圆形裁剪
                        )
                    }
                    Spacer(
                        modifier = Modifier
                            .width(16.dp)
                            .fillMaxWidth()
                    )
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column {
                            Text(
                                text = "${user.userName} (${user.userNickname})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            )
                            Text(
                                text = user.userIntro,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                Spacer(
                    modifier = Modifier
                        .height(16.dp)
                        .fillMaxWidth()
                )
                UserProfileScreen(user)
            }
        }
    }

    @Composable
    fun UserProfileScreen(user: User) {
        // 两列布局
        Row {
            // 左侧列
            Column(modifier = Modifier.weight(1f)) {
                ProfileInfoItem(
                    "编号",
                    user.userNo
                ) { _openURL("https://${S.HOST_ld246}/member/${user.userName}") }
                ProfileInfoItem(
                    "帖子",
                    user.userArticleCount
                ) { _openURL("https://${S.HOST_ld246}/member/${user.userName}/articles") }
                ProfileInfoItem(
                    "回帖",
                    user.userCommentCount
                ) { _openURL("https://${S.HOST_ld246}/member/${user.userName}/comments") }
                ProfileInfoItem(
                    "评论",
                    user.userComment2Count
                ) { _openURL("https://${S.HOST_ld246}/member/${user.userName}/comment2s") }
            }
            // 右侧列
            Column(modifier = Modifier.weight(1f)) {
                ProfileInfoItem(
                    "积分",
                    user.userPoint
                ) { _openURL("https://${S.HOST_ld246}/member/${user.userName}/points") }
                ProfileInfoItem(
                    "综合贡献点",
                    user.userGeneralRank
                ) { _openURL("https://${S.HOST_ld246}/top/general") }
                ProfileInfoItem(
                    "最近连签",
                    user.userCurrentCheckinStreak
                ) { _openURL("https://${S.HOST_ld246}/activity/checkin") }
                ProfileInfoItem(
                    "最长连签",
                    user.userLongestCheckinStreak
                ) { _openURL("https://${S.HOST_ld246}/activity/checkin") }
            }
        }
    }


    @Composable
    fun ProfileInfoItem(title: String, value: Any, onClick: () -> Unit) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .height(38.dp)
                .clickable(onClick = onClick)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = value.toString(),
                fontStyle = FontStyle.Italic,
                fontSize = 18.sp
            )
        }
    }


    @Composable
    fun NotificationsScreen(n: State<List<ld246_Response_Data_Notification>?>) {
        // 观察LiveData并更新状态
        val v = n.value
        if (v.isNullOrEmpty()) {
            // 显示空数据状态的占位符
            LazyColumn {
                items(13) { index ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0x31F1F2F3),
                            contentColor = Color.Black
                        )
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(text = "", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(bottom = 70.dp) // 避免被底栏遮住
            ) {
                item {
                    v?.forEach { notification ->
                        NotificationCard(notification)
                    }
                }
            }
        }
    }

    @Composable
    fun NotificationCard(notification: ld246_Response_Data_Notification) {
        val uriHandler = LocalUriHandler.current
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp),
            colors = if (notification.hasRead) {
                // 如果已读
                CardDefaults.cardColors(
                    containerColor = S.C.Card_bgColor_green1.current,
                    contentColor = Color.White
                )
            } else {
                // 如果未读
                CardDefaults.cardColors(
                    containerColor = S.C.Card_bgColor_gold1.current,
                    contentColor = Color.White
                )
            }
        ) {
            Column(modifier = Modifier
                .padding(10.dp)
                .clickable {
                    // SelectableHtmlText 需要响应内容的点击事件，因此打开文章得扩大到整个卡片。
                    _openURL(
                        "https://${S.HOST_ld246}/article/${notification.dataId}",
                        uriHandler
                    )
                }) {
                if (notification.authorName != null) { // 积分通知
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                _openURL(
                                    "https://${S.HOST_ld246}/member/${notification.authorName}",
                                    uriHandler
                                )
                            }
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(notification.authorAvatarURL)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Author Avatar",
                            modifier = Modifier.size(26.dp)
                        )
                        Text(
                            text = notification.authorName,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(start = 8.dp) // 添加一些 padding 以增加间隔
                        )
                    }
                    notification.title?.let {
                        Text(
                            text = it,
                            fontSize = 15.sp,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                }
                SelectableUrlHandleHtmlText(notification.content)
            }
        }
    }


    @SuppressLint("StaticFieldLeak")
    inner class NotificationsViewModel : ViewModel() {
        val TAG = "NotificationsViewModel"
        var __init__ = false;
        private val _notificationsMap = MutableLiveData<List<ld246_Response_Data_Notification>?>()
        val notificationsMap: MutableLiveData<List<ld246_Response_Data_Notification>?> =
            _notificationsMap

        @OptIn(ExperimentalMaterial3Api::class)
        private fun updateNotificationsMap(
            isTabChanged: MutableState<String>, state: PullToRefreshState?
        ) {
            _notificationsMap.postValue(map[isTabChanged.value])
            if (state != null) {
                if (state.isRefreshing) {
                    state.endRefresh()
                }
            }
        }

        private fun handleErrorResponse(response: Response<ld246_Response>) {
            val message = "更新失败: ${response.message()}"
            when (response.code()) {
                401 -> PopNotification.show(
                    message,
                    "TOKEN为空或者错误，请在右上角设置 TOKEN 后下拉刷新"
                ).noAutoDismiss()

                403 -> PopNotification.show(message, "权限不足").noAutoDismiss()
                else -> PopNotification.show("onResponse失败", response.toString()).noAutoDismiss()
            }
        }

        @OptIn(ExperimentalMaterial3Api::class)
        fun fetchNotifications(
            state: PullToRefreshState?,
            apiService: ApiServiceNotification,
            isTabChanged: MutableState<String>
        ) {
            job = viewModelScope.launch {
                try {
                    if (state == null || state.isRefreshing || !__init__ || map[isTabChanged.value].isNullOrEmpty()) {
                        // 执行网络请求
                        val caller: Call<ld246_Response> = when (isTabChanged.value) {
                            "回帖" -> apiService.apiV2NotificationsCommentedGet(1, token, ua)
                            "评论" -> apiService.apiV2NotificationsComment2edGet(1, token, ua)
                            "回复" -> apiService.apiV2NotificationsReplyGet(1, token, ua)
                            "提及" -> apiService.apiV2NotificationsAtGet(1, token, ua)
                            "关注" -> apiService.apiV2NotificationsFollowingGet(1, token, ua)
                            "积分" -> apiService.apiV2NotificationsPointGet(1, token, ua)
                            else -> null
                        } ?: return@launch
                        // enqueue 方法通常用于将一个网络请求加入到请求队列中，准备异步执行
                        caller.enqueue(object : Callback<ld246_Response> {
                            override fun onResponse(
                                call: Call<ld246_Response>,
                                response: Response<ld246_Response>
                            ) {
                                if (response.isSuccessful) {
                                    Log.d(TAG, "onResponse: ${response.body().toString()}")
                                    response.body()?.data?.let {
                                        map[isTabChanged.value] = when (isTabChanged.value) {
                                            "回帖" -> it.commentedNotifications
                                            "评论" -> it.comment2edNotifications
                                            "回复" -> it.replyNotifications
                                            "提及" -> it.atNotifications
                                            "关注" -> it.followingNotifications
                                            "积分" -> it.pointNotifications
                                            else -> listOf()
                                        }
                                    }
                                    PopTip.show("<(￣︶￣)↗[${response.code()}]")
                                    val _mr: Call<ld246_Response> =
                                        apiService.apiV2NotificationsMakeRead(
                                            LockNoteType_EN,
                                            token,
                                            ua
                                        )
                                    _mr.enqueue(object : Callback<ld246_Response> {
                                        override fun onResponse(
                                            p0: Call<ld246_Response>,
                                            p1: Response<ld246_Response>
                                        ) {
                                            Log.i(TAG, "Make Read: ${p1}")
                                        }

                                        override fun onFailure(
                                            p0: Call<ld246_Response>,
                                            p1: Throwable
                                        ) {
                                            Log.w(TAG, "Make Read: ${p1}")
                                        }
                                    })
                                } else {
                                    handleErrorResponse(response)
                                }
                                updateNotificationsMap(isTabChanged, state)
                            }

                            override fun onFailure(call: Call<ld246_Response>, t: Throwable) {
                                // 处理异常
                                PopNotification.show(call.toString(), t.toString()).noAutoDismiss()
                                updateNotificationsMap(isTabChanged, state)
                            }
                        })
                    } else {
                        updateNotificationsMap(isTabChanged, state)
                    }
                } catch (e: Exception) {
                    // 处理错误
                    PopNotification.show("任务失败", e.toString()).noAutoDismiss()
                    updateNotificationsMap(isTabChanged, state)
                } finally {
                    // 此处执行则不会等待 onResponse
                }
            }
            __init__ = true;
        }
    }


    @Preview(showBackground = true)
    @Composable
    fun DefaultPreview() {
        UI(null)
    }

    private fun handleUrlLoading(view: WebView, url: String): Boolean {
        val _url = Us.replaceScheme_deepDecode(url, "googlechrome://", "slld246://")
        val real_url = Us.replaceEncodeScheme(url, "googlechrome://", "slld246://")
        Log.d(TAG, _url)

        if (_url.startsWith("mqq://") || _url.startsWith("wtloginmqq://") || _url.startsWith("sinaweibo://")) {
            return try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(real_url))
                ActivityCompat.startActivityForResult(view.context as Activity, intent, 1, null)
                true
            } catch (e: Exception) {
                PopNotification.show(TAG, e.toString()).noAutoDismiss()
                false
            }
        } else {
            return false
        }
    }

    private fun webViewClient(): WebViewClient {
        return object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                return handleUrlLoading(view, request.url.toString())
            }
        }
    }

    private fun clearWebViewCookies(webView: WebView?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().apply {
                removeAllCookies { success ->
                    if (success) {
                        webView?.clearCache(true)
                        fullScreenDialog?.dismiss()
                        PopTip.show("<(￣︶￣)↗[success]")
                    } else {
                        fullScreenDialog?.dismiss()
                        PopTip.show(" ￣へ￣ [failed]")
                    }
                }
            }
        } else {
            CookieSyncManager.createInstance(this)
            CookieManager.getInstance().apply {
                removeAllCookie()
                webView?.clearCache(true)
                fullScreenDialog?.dismiss()
            }
            CookieSyncManager.getInstance().sync()
        }
    }

    fun showFullScreenDialog(url: String) {
        Log.w("showFullScreenDialog", url)
        if (fullScreenDialog == null) {
            fullScreenDialog = FullScreenDialog.build().apply {
                setDialogLifecycleCallback(object : DialogLifecycleCallback<FullScreenDialog?>() {
                    override fun onShow(dialog: FullScreenDialog?) {
                        when (url) {
                            "action?=Logout" -> {
                                dialog?.hide() // 隐藏处理过程
                                val webView =
                                    dialog?.getCustomView()?.findViewById<WebView>(R.id.webView)
                                clearWebViewCookies(webView)
                                return
                            }

                            else -> {}
                        }
                        dialog?.setCustomView(object :
                            OnBindView<FullScreenDialog?>(R.layout.layout_full_screen) {
                            override fun onBind(dialog: FullScreenDialog?, v: View) {
                                val webView = v.findViewById<WebView>(R.id.webView)
                                webView.webViewClient = webViewClient()
                                webView.loadUrl(url)

                                val btnRefresh = v.findViewById<TextView>(R.id.btnRefresh)
                                btnRefresh.setOnClickListener {
                                    webView.reload()
                                }

                                val btnClose = v.findViewById<TextView>(R.id.btnClose)
                                btnClose.setOnClickListener {
                                    dialog?.dismiss()
                                }
                            }
                        })
                    }

                    override fun onDismiss(dialog: FullScreenDialog?) {
                        // 对话框关闭时的操作
                        fullScreenDialog = null
                    }
                })
            }.show()
        } else {
            fullScreenDialog?.let { dialog ->
                val webView = dialog.getCustomView()?.findViewById<WebView>(R.id.webView)
                Log.w(fullScreenDialog.toString(), webView.toString())
                webView?.loadUrl(url)
            }
        }
    }

    fun _openURL(url: String, uriHandler: UriHandler? = null) {
        if (openUrlExternal) {
            if (uriHandler != null) {
                uriHandler.openUri(url)
            } else {
                Us.openUrl(url)
            }
        } else {
            showFullScreenDialog(url)
        }
    }

    @Composable
    fun SelectableUrlHandleHtmlText(html: String, modifier: Modifier = Modifier) {
        // 完全体，因为需要共享一些数据不好抽离，基础版在 HTML.kt
        AndroidView(
            modifier = modifier.fillMaxWidth(),
            factory = { context ->
                TextView(context).apply {
                    // 允许长按复制文本，需放在前面
                    setTextIsSelectable(true)
                    // 设置MovementMethod以使链接可点击，需放在后面
                    // 尝试过自定义处理逻辑，结果替换个链接都费劲
                    movementMethod = LinkMovementMethod.getInstance()
                    // 设置全局字体大小
                    textSize = 17f
                }
            },
            update = { textView ->
                // 设置自定义的MovementMethod
                textView.movementMethod = CustomLinkMovementMethod()
                val _Html = Us.parseAndDecodeUrl(
                    html,
                    """['"]https://ld246.com/forward\?goto=([^'"]*)['"]""".toRegex()
                )
//            Log.i("HTML", _Html)
                textView.text = HtmlCompat.fromHtml(
                    _Html,
                    HtmlCompat.FROM_HTML_MODE_COMPACT,
                    null,
                    MyTagHandler()
                )
            }
        )
    }

    inner class CustomLinkMovementMethod : LinkMovementMethod() {
        override fun onTouchEvent(
            widget: TextView,
            buffer: Spannable,
            event: MotionEvent
        ): Boolean {
            val action = event.action

            if (action == MotionEvent.ACTION_UP) {
                val x = event.x.toInt()
                val y = event.y.toInt()

                val layout = widget.layout
                val line = layout.getLineForVertical(y)
                val off = layout.getOffsetForHorizontal(line, x.toFloat())

                val links = buffer.getSpans(off, off, ClickableSpan::class.java)

                if (links.isNotEmpty()) {
                    // 这里执行您的自定义逻辑，例如：
                    // 获取链接的URL
                    val urlSpan = links[0] as URLSpan
                    val url = urlSpan.url
                    _openURL(url)
                    return true
                }
            }

            return super.onTouchEvent(widget, buffer, event)
        }
    }
}
