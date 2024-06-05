@file:Suppress("CompositionLocalNaming", "CompositionLocalNaming")

package org.b3log.siyuan.producer

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.util.Size
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Token
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.documentfile.provider.DocumentFile
import com.kongzue.dialogx.dialogs.BottomMenu
import com.kongzue.dialogx.dialogs.InputDialog
import com.kongzue.dialogx.dialogs.PopNotification
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.interfaces.OnBottomMenuButtonClickListener
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.b3log.siyuan.App
import org.b3log.siyuan.R
import org.b3log.siyuan.andapi.Toast
import org.b3log.siyuan.ld246.HomeActivity
import org.b3log.siyuan.services.BootService
import org.commonmark.node.Node
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import org.jsoup.Jsoup
import org.jsoup.safety.Safelist
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import sc.windom.sofill.S
import sc.windom.sofill.U
import sc.windom.sofill.U.workspaceParentDir
import sc.windom.sofill.api.siyuan.SiyuanNoteAPI
import sc.windom.sofill.compose.ApkButtons
import sc.windom.sofill.compose.AudioButtons
import sc.windom.sofill.compose.LockScreenOrientation
import sc.windom.sofill.compose.MagnetButtons
import sc.windom.sofill.compose.SelectableHtmlText
import sc.windom.sofill.compose.SelectableText
import sc.windom.sofill.compose.VideoButtons
import sc.windom.sofill.compose.components.CommonTopAppBar
import sc.windom.sofill.compose.partialCom.DdMenuI
import sc.windom.sofill.compose.partialCom.NetworkAware
import sc.windom.sofill.compose.theme.CascadeMaterialTheme
import sc.windom.sofill.dataClass.INbList
import sc.windom.sofill.dataClass.INotebook
import sc.windom.sofill.dataClass.IPayload
import sc.windom.sofill.dataClass.IResponse
import java.io.IOException
import java.util.Date


// TODO: 多选文件打开的处理
// TODO: 文件覆盖提醒
// TODO: 缓存清理
// TODO: 如果是 workspaceParentDir 目录下的文件支持删除
// TODO: 文件被删除时处理异常
class MainPro : ComponentActivity() {
    val TAG = "producer/MainPro.kt"
    private var mmkv: MMKV = MMKV.defaultMMKV()
    private var token =
        U.getDecryptedToken(mmkv, S.KEY_TOKEN_Sillot_Gibbet, S.KEY_AES_TOKEN_Sillot_Gibbet)
    private lateinit var thisActivity: Activity
    private var in2_data: Uri? = null
    private var in2_action: String? = null
    private var in2_type: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        thisActivity = this
        Log.i(TAG, "onCreate() invoked. @ $intent")
        if (intent == null) {
            return
        }
        in2_action = intent.action
        in2_type = intent.type
        in2_data = intent.data
        val scheme = in2_data?.scheme
        val host = in2_data?.host
        Log.d(TAG, "scheme: $scheme, host: $host, action: $in2_action, type: $in2_type")

        if (S.isUriMatched(in2_data, S.case_ld246_1) || S.isUriMatched(
                in2_data,
                S.case_ld246_2
            ) || S.isUriMatched(in2_data, S.case_github_1)
        ) {
            // 转发处理
            val homeIntent = Intent(this, HomeActivity::class.java)
            homeIntent.data = in2_data // 将URI数据传递给HomeActivity
            startActivity(homeIntent)
            finish() // 不需要返回MainPro，在这里结束它
        } else if (in2_data != null && in2_data?.scheme.isNullOrEmpty() || listOf(
                "http",
                "https",
                "siyuan"
            ).contains(
                in2_data?.scheme
            )
        ) {
            // 转发处理
            // 这里需要转发的思路是：此处判断作为兜底十有八九用户已经将汐洛设置为默认浏览器，转发给系统会导致死循环，系统认为没有应用能够处理，汐洛会闪退
            val homeIntent = Intent(this, HomeActivity::class.java)
            homeIntent.data = in2_data // 将URI数据传递给HomeActivity
            startActivity(homeIntent)
            finish() // 不需要返回MainPro，在这里结束它
        } else {
            // ...
        }

        // 设置沉浸式通知栏
        window.setDecorFitsSystemWindows(false)
        window.decorView.setOnApplyWindowInsetsListener { _, insets ->
            insets
        }
        if (bootService == null) {
            // 绑定服务
            val intent = Intent(applicationContext, BootService::class.java)
            bindService(intent, serviceConnection, BIND_AUTO_CREATE)
        } else {
            performActionWithService()
        }
    }

    override fun onDestroy() {
        Log.w(TAG, "onDestroy() invoked")
        super.onDestroy()
        // 解绑服务
        if (serviceBound) {
            unbindService(serviceConnection)
            serviceBound = false
        }
    }

    private var bootService: BootService? = null
    private var serviceBound = false

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as BootService.LocalBinder
            bootService = binder.getService()
            serviceBound = true
            App.bootService = bootService!!
            // 服务绑定后，执行依赖于bootService的代码
            performActionWithService()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            bootService = null
            serviceBound = false
        }
    }

    fun performActionWithService() {
        Log.w(TAG, "performActionWithService() invoked")
        setContent {
            CascadeMaterialTheme {
                MyUI(TAG)
            }
        }
    }

    private fun isMarkdown(text: String): Boolean {
        // 检查文本中是否包含Markdown特有的语法特征
        val containsMarkdownSyntax =
            text.contains(Regex("^(#{1,6})\\s|\\*\\s|_\\s|[\\[]\\([^\\)]+\\)[\\]]"))
        // 检查文本的开始和末尾是否是HTML标签
        val startsWithHtmlTag = Regex("^<[a-zA-Z]").find(text) != null
        val endsWithHtmlTag = Regex("[a-zA-Z]>$").find(text) != null

        // 如果文本包含Markdown语法，并且开始和末尾不是HTML标签，则判定为Markdown
        return containsMarkdownSyntax && !startsWithHtmlTag && !endsWithHtmlTag
    }

    private fun checkContentFormat(text: String): String {
        // 首先检查文本是否包含Markdown特有的语法
        if (isMarkdown(text)) {
            return "Markdown"
        } else {
            // 创建一个只允许特定HTML标签的Safelist
            val safelist = Safelist.relaxed()
            // 使用Jsoup尝试解析文本，并保留允许的HTML标签
            val cleanText = Jsoup.clean(text, safelist)
            if (cleanText != text) {
                // 文本包含HTML标签，因此是HTML内容
                return "HTML"
            }
        }
        // 如果没有HTML标签，也不是Markdown，则返回其他
        return "Other"
    }

    private fun processHtml(html: String): String {
        // 对HTML进行校验和清理
        val safeHtml = sanitizeHtml(html)
        // 处理安全的HTML文本
        Log.e(TAG, "HTML: $safeHtml")
        return safeHtml
    }

    private fun processMarkdown(markdown: String): String {
        val validMarkdown = validateMarkdown2HTML(markdown)
        Log.e(TAG, "Markdown: $validMarkdown")
        return validMarkdown
    }

    private fun processPlainText(text: String): String {
        // 处理普通文本
        Log.e(TAG, text)
        return text
    }

    private fun sanitizeHtml(html: String): String {
        // 使用Jsoup的Safelist清理HTML，只允许安全的标签和属性
        val whitelist = Safelist.relaxed()
        return Jsoup.clean(html, whitelist)
    }

    private fun validateMarkdown2HTML(markdown: String): String {
        // 使用CommonMark解析器解析Markdown
        val parser = Parser.builder().build()
        val renderer = HtmlRenderer.builder().build()

        try {
            // 解析Markdown文本
            val document: Node = parser.parse(markdown)
            // 渲染Markdown为HTML
            val html: String = renderer.render(document)
            // 如果没有异常，返回渲染后的HTML
            return html
        } catch (e: Exception) {
            // 如果解析过程中发生异常，返回错误信息
            return "Error: ${e.message}"
        }
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class, DelicateCoroutinesApi::class)
    @Composable
    fun MyUI(TAG: String) {
        val inspectionMode = LocalInspectionMode.current // 获取当前是否处于预览模式// 获取窗口尺寸
        val coroutineScope = rememberCoroutineScope()
        var head_title = "汐洛中转站"
        val fileName = in2_data?.let { U.getFileName(thisActivity, it) }
        val fileSize = in2_data?.let { U.getFileSize(thisActivity, it) }
        val mimeType = intent?.data?.let { U.getMimeType(thisActivity, it) } ?: ""
        val fileType =
            fileName?.let { U.getFileMIMEType(mimeType, it) }
                ?: run { U.getFileMIMEType(mimeType) }
        val isLandscape =
            LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE // 是否横屏（宽高比）

        var isMenuVisible = rememberSaveable { mutableStateOf(false) }

        head_title = if (in2_action == Intent.ACTION_SEND) {
            "汐洛受赏中转站"
        } else if (!in2_data?.scheme.isNullOrEmpty() && in2_type.isNullOrEmpty()){
            "汐洛链接中转站"
        } else if (in2_data != null) {
            "汐洛文件中转站"
        } else {
            "汐洛中转站"
        }
        Scaffold(
            topBar = {
                CommonTopAppBar(head_title, TAG, in2_data, isMenuVisible,
                    additionalMenuItem = {
                        AddDropdownMenu(onDismiss = {
                            isMenuVisible.value = false
                        })
                    }) {
                    // 将Context对象安全地转换为Activity
                    thisActivity.finish() // 结束活动
                }
            }, modifier = Modifier.background(Color.Gray)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it),
            ) {
                NetworkAware()
                if (in2_action == Intent.ACTION_SEND) {

                    // 根据mimeType处理不同的数据
                    when {
                        in2_type?.startsWith("text/") == true -> {
                            // 获取extra中的文本数据
                            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT) ?: return@Box
                            val html: String
                            // 判断文本类型并进行合法性校验
                            when (checkContentFormat(sharedText)) {
                                "HTML" -> {
                                    // 处理HTML文本
                                    html = processHtml(sharedText)
                                }

                                "Markdown" -> {
                                    // 处理Markdown文本
                                    html = processMarkdown(sharedText)
                                }

                                else -> {
                                    // 处理普通文本
                                    html = processPlainText(sharedText)
                                }
                            }
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                item {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    SelectableHtmlText(
                                        html,
                                        Modifier
                                            .fillMaxWidth()
                                            .height(200.dp)
                                            .padding(10.dp)
                                    )
                                    SendBtnPart(sharedText)
                                }
                            }
                        }

                        in2_type?.startsWith("image/") == true -> {
                        }
                    }
                } else if (isLandscape) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(.4f)
                                .fillMaxHeight()
                                .padding(vertical = 16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            InfoPart(
                                uri = in2_data,
                                fileType = fileType,
                                fileName = fileName,
                                fileSize = fileSize
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(.6f)
                                .fillMaxHeight()
//                            .height(IntrinsicSize.Min)
                                .padding(vertical = 16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            BtnPart(
                                uri = in2_data,
                                mimeType = mimeType,
                                fileName = fileName
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            InfoPart(
                                uri = in2_data,
                                fileType = fileType,
                                fileName = fileName,
                                fileSize = fileSize
                            )
                            BtnPart(
                                uri = in2_data,
                                mimeType = mimeType,
                                fileName = fileName
                            )
                        }
                    }
                }
            }


        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun AddDropdownMenu(
        onDismiss: () -> Unit,
    ) {
        DdMenuI(
            text = { Text("汐洛绞架内核 API TOKEN") },
            icon = { Icon(Icons.TwoTone.Token, contentDescription = null) },
            cb = {
                onDismiss()
                val deToken = U.getDecryptedToken(
                    mmkv,
                    S.KEY_TOKEN_Sillot_Gibbet,
                    S.KEY_AES_TOKEN_Sillot_Gibbet
                )
                InputDialog(
                    "🛸 API TOKEN",
                    "可在汐洛绞架 设置 - 关于 中找到 API Token，固定以 'token ' 开头\n\n温馨提示：应用存储 Token 时进行了一定的处理，且不会传输到网络，但用户仍需注意防止 Token 泄露！建议使用前先阅读源代码",
                    "确定",
                    "取消",
                    deToken?.let { deToken } ?: run { "token " }
                )
                    .setCancelable(false)
                    .setOkButton { baseDialog, v, inputStr ->
                        token = inputStr
                        // 生成AES密钥
                        val aesKey = U.generateAesKey()
                        // 注意：这里需要将SecretKey转换为可以存储的格式，例如转换为字节数组然后进行Base64编码
                        val encodedKey = Base64.encodeToString(aesKey.encoded, Base64.DEFAULT)
                        // 加密Token
                        val encryptedToken = U.encryptAes(inputStr, aesKey)
                        // 将加密后的Token存储到MMKV中
                        mmkv.encode(S.KEY_AES_TOKEN_Sillot_Gibbet, encodedKey)
                        mmkv.encode(S.KEY_TOKEN_Sillot_Gibbet, encryptedToken)
                        PopNotification.show(
                            "TOKEN已更新（${
                                U.displayTokenLimiter(
                                    inputStr,
                                    "token ".length + 4,
                                    4
                                )
                            }）"
                        ).noAutoDismiss()
                        false
                    }
                    .show()
            },
        )
    }

    // 创建 Retrofit 实例
    fun createRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // 获取笔记本列表
    fun getNotebooks(
        api: SiyuanNoteAPI,
        token: String,
        callback: (notebooks: List<INotebook>?, info: String) -> Unit
    ) {
        val body = mapOf("flashcard" to false)
        val notebooksCall = api.getNotebooks(token, body)
        notebooksCall.enqueue(object : Callback<IResponse<INbList>> {
            override fun onResponse(
                call: Call<IResponse<INbList>>,
                response: Response<IResponse<INbList>>
            ) {
                if (response.isSuccessful && response.body()?.code == 0) {
                    callback(response.body()?.data?.notebooks, "${response.body()}")
                } else {
                    callback(
                        null,
                        "Failed to get notebooks: ${response.code()} ${response.message()} \n ${response.body()}"
                    )
                }
            }

            override fun onFailure(call: Call<IResponse<INbList>>, t: Throwable) {
                callback(null, "getNotebooks Error: ${t.message}")
            }
        })
    }

    // 创建新的 Markdown 笔记
    fun createNote(
        api: SiyuanNoteAPI,
        payload: IPayload,
        token: String,
        callback: (success: Boolean, info: String) -> Unit
    ) {
        val createNoteCall = api.createNote(payload, token)
        createNoteCall.enqueue(object : Callback<IResponse<String>> {
            override fun onResponse(
                call: Call<IResponse<String>>,
                response: Response<IResponse<String>>
            ) {
                if (response.isSuccessful && response.body()?.code == 0) {
                    response.body()?.data?.let {
                        U.startMainActivityWithBlock(
                            "siyuan://blocks/$it",
                            applicationContext
                        )
                    }
                    callback(true, "Note created successfully. ${response.body()}")
                } else {
                    callback(
                        false,
                        "Failed to create note: ${response.code()} ${response.message()} \n ${response.body()}"
                    )
                }
            }

            override fun onFailure(call: Call<IResponse<String>>, t: Throwable) {
                callback(false, "createNote Error: ${t.message}")
            }
        })
    }

    // 在协程中调用sendMD2siyuan
    fun runSendMD2siyuan(markdownContent: String) = runBlocking<Unit> { // 启动主协程
        launch { // 启动一个新协程并运行挂起函数
            sendMD2siyuan(markdownContent)
        }
    }

    fun sendMD2siyuan(markdownContent: String) {
        val retrofit = createRetrofit("http://0.0.0.0:58131/")
        val api = retrofit.create(SiyuanNoteAPI::class.java)
        val helpInfo =
            "请注意：（1）TOKEN是否正确；（2）当前工作空间是否存在有效笔记本；（3）笔记本是否被关闭了"
        token?.let { _token ->
            getNotebooks(api, _token) { notebooks, info ->
                if (notebooks.isNullOrEmpty()) {
                    // 处理笔记本列表为空的情况
                    thisActivity.runOnUiThread {
                        PopNotification.show(
                            TAG,
                            "No notebooks received. reason:\n$info\n$helpInfo"
                        ).noAutoDismiss()
                    }
                } else {
                    // 处理获取到的笔记本列表
                    Log.i(TAG, "Received ${notebooks.size} notebooks.")
                    val notebookIDs: Array<String> = notebooks.map { it.id }.toTypedArray()
                    val notebookInfos: Array<String> = notebooks.map {
                        "（${if (it.closed) "不可用" else "可使用"}）${it.name}"
                    }.toTypedArray()
                    var selectMenuIndex = 0
                    BottomMenu.show(notebookInfos)
                        .setMessage("仅支持当前工作空间")
                        .setTitle("选择要存入的笔记本")
                        .setSelection(selectMenuIndex) //指定已选择的位置
                        .setOnMenuItemClickListener { dialog, text, index ->
                            selectMenuIndex = index
                            dialog.refreshUI() // 在 compose 里需要强制刷新
                            true // 点击菜单后不会自动关闭
                        }
                        .setOkButton("确定",
                            OnBottomMenuButtonClickListener { menu, view ->
                                val notebookId = notebookIDs[selectMenuIndex]
                                Log.e(TAG, notebookId)
                                val payload = IPayload(
                                    markdownContent, notebookId, "/来自汐洛受赏 ${
                                        U.dateFormat_full1.format(
                                            Date()
                                        )
                                    }"
                                )

                                createNote(api, payload, token!!) { success, info ->
                                    if (success) {
                                        // 处理创建笔记成功的情况
                                        Log.i(TAG, "Note creation succeeded. $info")
                                    } else {
                                        // 处理创建笔记失败的情况
                                        thisActivity.runOnUiThread {
                                            PopNotification.show(
                                                TAG,
                                                "Note creation failed. reason:\n$info\n$helpInfo"
                                            ).noAutoDismiss()
                                        }
                                    }
                                }
                                false
                            })
                        .setCancelButton("取消",
                            OnBottomMenuButtonClickListener { menu, view ->
                                false
                            })
                }
            }
        } ?: {
            thisActivity.runOnUiThread {
                PopTip("TOKEN为空，请在右上角设置 TOKEN 后重试")
            }
        }
    }

    @Composable
    fun SendBtnPart(markdown: String?) {
        val isLandscape =
            LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE // 是否横屏（宽高比）
        // 保存到指定文件夹
        Button(modifier = Modifier
            .width(S.C.Button_Width.current.dp)
            .padding(top = if (isLandscape) S.C.btn_PaddingTopH.current else S.C.btn_PaddingTopV.current),
            colors = ButtonDefaults.buttonColors(
                containerColor = S.C.btn_bgColor3.current,
                contentColor = S.C.btn_Color1.current
            ), enabled = true, onClick = {
                if (markdown != null) {
                    val directories = U.getDirectoriesInPath(thisActivity.workspaceParentDir())
                    val filteredDirectories = directories.filter { it != "home" }
                    if (filteredDirectories.isNotEmpty()) {
                        runSendMD2siyuan(markdown)
                    } else {
                        PopNotification.show(
                            R.drawable.icon,
                            "未发现任何工作空间",
                            "请检查是否初始化了，或者路径存在异常 ${thisActivity.workspaceParentDir()}/"
                        ).noAutoDismiss()
                    }
                }
            }) {
            Text(
                text = S.C.btnText5.current,
                letterSpacing = S.C.btn_lspace.current,
                fontSize = if (isLandscape) S.C.btn_TextFontsizeH.current else S.C.btn_TextFontsizeV.current
            )
        }
    }

    @Composable
    fun InfoPart(uri: Uri?, fileType: String?, fileName: String?, fileSize: String?) {
        val TAG = "MainPro-InfoPart"
        val inspectionMode = LocalInspectionMode.current // 获取当前是否处于预览模式// 获取窗口尺寸
        val Thumbnail_Height = S.C.Thumbnail_Height.current
        val Thumbnail_Height_IMG = S.C.Thumbnail_Height_IMG.current

        val isLandscape =
            LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE // 是否横屏（宽高比）

        // 显示图像缩略图或文件类型图标
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
            ) {
                if (fileType != null) {
                    if (fileType.endsWith("图像")) {
                        val bitmap = uri?.let { it1 ->
                            thisActivity.contentResolver?.loadThumbnail(
                                it1,
                                Size(
                                    if (isLandscape) Thumbnail_Height else Thumbnail_Height_IMG,
                                    if (isLandscape) Thumbnail_Height else Thumbnail_Height_IMG
                                ), null
                            )
                        }
                        bitmap?.let {
                            Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = "Thumbnail",
                                modifier = Modifier
                                    .size(Thumbnail_Height_IMG.dp)
                                    .fillMaxSize()
                            )
                        }
                    } else {
                        val icon = U.getIconForFileType(fileType)
                        Icon(
                            imageVector = icon,
                            contentDescription = "File Type Icon",
                            modifier = Modifier
                                .size(Thumbnail_Height.dp)
                        )
                        //                    Image( // 对应的是 R.drawable.id 方案
                        //                        painter = painterResource(id = icon),
                        //                        contentDescription = null,
                        //                        modifier = Modifier.size(100.dp)
                        //                    )
                    }
                }

                fileName?.let { it1 ->
                    SelectableText(
                        text = it1, // 使用获取到的文件名
                        style = TextStyle(
                            fontSize = if (isLandscape) 14.sp else 16.sp
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "$fileSize ($fileType)",
                        fontSize = if (isLandscape) 12.sp else 14.sp,
                        color = Color.Gray,
                        modifier = if (isLandscape) Modifier.padding(top = 6.dp) else Modifier.padding(
                            bottom = 6.dp
                        )
                    )
                }
            }
        }

    }

    @SuppressLint("Range")
    @Composable
    fun BtnPart(uri: Uri?, mimeType: String, fileName: String?) {
        val TAG = "MainPro-BtnPart"
        val inspectionMode = LocalInspectionMode.current // 获取当前是否处于预览模式// 获取窗口尺寸
        val coroutineScope = rememberCoroutineScope()

        var showSaveButton by remember { mutableStateOf(false) }
        var showAudioButton by remember { mutableStateOf(false) }
        var showVideoButton by remember { mutableStateOf(false) }
        var showApkButton by remember { mutableStateOf(false) }
        var showMagnetButton by remember { mutableStateOf(false) }

        val isLandscape =
            LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE // 是否横屏（宽高比）

        var progressValue by remember { mutableStateOf(0) }
        var isButton3OnClickRunning by remember { mutableStateOf(false) }
        var isButton4OnClickRunning by remember { mutableStateOf(false) }
        var workspaceAssetsDir by remember { mutableStateOf("${thisActivity.workspaceParentDir()}/sillot/data/assets") }
        var uri_from_file by remember { mutableStateOf(Uri.parse("")) }
        var uri_to_dir by remember { mutableStateOf(Uri.parse("")) }
        var selectedFolder by remember { mutableStateOf<Uri?>(null) }


        LaunchedEffect(key1 = mimeType) {
            showSaveButton = uri != null
            if (fileName != null) {
                showAudioButton = mimeType.startsWith("audio/")
                showVideoButton = mimeType.startsWith("video/")
                showApkButton =
                    mimeType == "application/vnd.android.package-archive" || (mimeType == "application/octet-stream" && fileName.endsWith(
                        ".apk.1"
                    ))
            }
            if (uri != null) {
                when (uri.scheme) {
                    "magnet" -> showMagnetButton = true
                }
            }
        }

        fun onCopyFileToFolderByDocumentTree() {
            // 启动一个协程来执行任务
            coroutineScope.launch {
                withContext(Dispatchers.IO) {

                    try {
                        if (!U.isStorageSpaceAvailable(thisActivity.contentResolver, uri_from_file)) {
                            // 存储空间不足，处理逻辑
                            Toast.Show(thisActivity, "存储空间不足，请先清理")
                            return@withContext
                        }
                        val sourceFilePath = U.getPathFromUri(thisActivity, uri_from_file)
                        // 复制文件到所选文件夹
                        fileName?.let {
                            sourceFilePath?.let { it1 ->
                                U.copyFileToFolderByDocumentTree(
                                    thisActivity, uri_to_dir, it,
                                    it1, mimeType
                                )
                            }
                        }
                        withContext(Dispatchers.Main) {
                            Toast.Show(thisActivity, "已复制到指定文件夹")
                        }
                    } catch (e: IOException) {
                        Log.e(TAG, e.toString())
                        withContext(Dispatchers.Main) {
                            PopNotification.show("任务失败", e.toString()).noAutoDismiss()
                        }
                    }
                    // 执行任务完成后，关闭遮罩
                    isButton3OnClickRunning = false
                }
            }
        }

        fun onCopyFileToMyAppFolder() {
            // 启动一个协程来执行任务
            coroutineScope.launch {
                withContext(Dispatchers.IO) {
                    try {
                        if (!U.isStorageSpaceAvailable(thisActivity.contentResolver, uri_from_file)) {
                            // 存储空间不足，处理逻辑
                            PopNotification.show(R.drawable.icon, "存储空间不足，请先清理")
                            return@withContext
                        }
                        val sourceFilePath = U.getPathFromUri(thisActivity, uri_from_file)
                        // 复制文件到所选文件夹
                        fileName?.let {
                            sourceFilePath?.let { it1 ->
                                try {
                                    U.copyFileToMyAppFolder(
                                        workspaceAssetsDir, it, it1
                                    )
                                    PopNotification.show(
                                        R.drawable.icon,
                                        "已存入 ${workspaceAssetsDir}"
                                    ).autoDismiss(5000)
                                } catch (e: IOException) {
                                    Log.e(TAG, e.toString())
                                    PopNotification.show(R.drawable.icon, "任务失败", e.toString())
                                        .noAutoDismiss()
                                }

                            }
                        }
                    } catch (e: IOException) {
                        Log.e(TAG, e.toString())
                        withContext(Dispatchers.Main) {
                            PopNotification.show(R.drawable.icon, "任务失败", e.toString())
                                .noAutoDismiss()
                        }
                    } finally {
                        // 执行任务完成后，关闭遮罩
                        isButton4OnClickRunning = false
                    }
                }
            }
        }

        var manageAllFilesPermissionLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (U.canManageAllFiles(thisActivity)) {
                    if (isButton3OnClickRunning) {
                        onCopyFileToFolderByDocumentTree()
                    } else if (isButton4OnClickRunning) {
                        onCopyFileToMyAppFolder()
                    }

                }
            }
        val bt3TaskLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    result.data?.data?.let { _uri ->
                        if (uri == null) return@let
                        isButton3OnClickRunning = true  // 没有设置 LaunchedEffect 但是需要显示遮罩
                        // 通过 SAF 获取持久性权限
                        thisActivity.contentResolver.takePersistableUriPermission(
                            _uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        )

//                Us.requestExternalStoragePermission(thisActivity)
                        // 使用DocumentFile处理URI
                        val rootDocument = DocumentFile.fromTreeUri(thisActivity, _uri)
                        // 例如，列出根目录下的文件和文件夹
                        rootDocument?.listFiles()?.forEach { file ->
                            // 处理文件或文件夹
                            Log.d(
                                TAG,
                                "File name: ${file.name}, Is directory: ${file.isDirectory}, mimeType: ${file.type}, canRead: ${file.canRead()}, canWrite: ${file.canWrite()}, lastModified: ${file.lastModified()} "
                            )
                        }

                        uri_from_file = uri
                        uri_to_dir = _uri
                        if (U.canManageAllFiles(thisActivity)) {
                            onCopyFileToFolderByDocumentTree()
                        } else {
                            val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                            manageAllFilesPermissionLauncher.launch(intent)
                        }

                    }
                }
            }

        LaunchedEffect(key1 = isButton4OnClickRunning) {
            if (isButton4OnClickRunning) {
                uri_from_file = uri
                uri_to_dir = Uri.parse(workspaceAssetsDir)
                if (U.canManageAllFiles(thisActivity)) {
                    onCopyFileToMyAppFolder()
                } else {
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    manageAllFilesPermissionLauncher.launch(intent)
                }

            }
        }
        // 遮罩组件
        if (isButton4OnClickRunning || isButton3OnClickRunning) {
            // 锁定当前屏幕方向，避免重绘，相当于 android.app.Activity.setRequestedOrientation
            LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED)
            Dialog(
                onDismissRequest = {
                    isButton3OnClickRunning = false
                    isButton4OnClickRunning = false
                    selectedFolder = null
                },
                properties = DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false
                )
            ) {
                // 遮罩内容
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (false) {
                        LinearProgressIndicator(
                            progress = { progressValue / 100f },
                            modifier = Modifier
                                .padding(2.dp)
                                .height(13.dp)
                                .fillMaxWidth(),
                        )
                    } else {
                        // 不设置progress参数，显示不确定进度
                        LinearProgressIndicator(
                            modifier = Modifier
                                .padding(bottom = 58.dp)
                                .height(13.dp)
                                .fillMaxWidth(),
                        )
                        Text(
                            text = "操作正在进行……\n请勿退出",
                            color = Color.Yellow,
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .align(Alignment.BottomCenter),
                            letterSpacing = S.C.btn_lspace.current,
                            fontSize = if (isLandscape) S.C.btn_TextFontsizeH.current else S.C.btn_TextFontsizeV.current
                        )
                    }
                }
            }
        }
        if (!isButton4OnClickRunning && !isButton3OnClickRunning) {
            // 解除屏幕方向锁定
            LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
        }

        //// @D 通用按键部分

        if (showSaveButton) {
            // 保存到指定文件夹
            Button(modifier = Modifier
                .width(S.C.Button_Width.current.dp)
                .padding(top = if (isLandscape) S.C.btn_PaddingTopH.current else S.C.btn_PaddingTopV.current),
                colors = ButtonDefaults.buttonColors(
                    containerColor = S.C.btn_bgColor3.current,
                    contentColor = S.C.btn_Color1.current
                ), enabled = true, onClick = {
                    val btn3_intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                    bt3TaskLauncher.launch(btn3_intent)
                    // 非阻塞
                }) {
                Text(
                    text = S.C.btnText3.current,
                    letterSpacing = S.C.btn_lspace.current,
                    fontSize = if (isLandscape) S.C.btn_TextFontsizeH.current else S.C.btn_TextFontsizeV.current
                )
            }

            // 存入工作空间级资源目录
            Button(modifier = Modifier
                .width(S.C.Button_Width.current.dp)
                .padding(top = if (isLandscape) S.C.btn_PaddingTopH.current else S.C.btn_PaddingTopV.current),
                colors = ButtonDefaults.buttonColors(
                    containerColor = S.C.btn_bgColor4.current,
                    contentColor = S.C.btn_Color1.current
                ), enabled = true, onClick = {
                    if (uri != null) {
                        Log.e(TAG, thisActivity.workspaceParentDir())
                        val directories = U.getDirectoriesInPath(thisActivity.workspaceParentDir())
                        val filteredDirectories = directories.filter { it != "home" }
                        if (filteredDirectories.isNotEmpty()) {
                            var selectMenuIndex = 0
                            var selectMenuText = "sillot"
                            BottomMenu.show(filteredDirectories)
                                .setMessage("sillot 是默认工作空间")
                                .setTitle("选择要存入的工作空间")
                                .setSelection(selectMenuIndex) //指定已选择的位置
                                .setOnMenuItemClickListener { dialog, text, index ->
                                    selectMenuIndex = index
                                    selectMenuText = text as String
                                    dialog.refreshUI() // 在 compose 里需要强制刷新
                                    true // 点击菜单后不会自动关闭
                                }
                                .setOkButton("确定",
                                    OnBottomMenuButtonClickListener { menu, view ->
                                        Log.e(TAG, "${selectMenuText}")

                                        workspaceAssetsDir =
                                            "${thisActivity.workspaceParentDir()}/${selectMenuText}/data/assets"
                                        isButton4OnClickRunning = true // 值变化时会触发重组
                                        false
                                    })
                                .setCancelButton("取消",
                                    OnBottomMenuButtonClickListener { menu, view ->
                                        false
                                    })
                        } else {
                            PopNotification.show(
                                R.drawable.icon,
                                "未发现任何工作空间",
                                "请检查是否初始化了，或者路径存在异常 ${thisActivity.workspaceParentDir()}/"
                            ).noAutoDismiss()
                        }
                    }
                }) {
                Text(
                    text = S.C.btnText4.current,
                    letterSpacing = S.C.btn_lspace.current,
                    fontSize = if (isLandscape) S.C.btn_TextFontsizeH.current else S.C.btn_TextFontsizeV.current
                )
            }
        }


        //// 类型按键区

        fun ApkBTNonClick1() {
            in2_data?.let {
                U.installApk2(
                    thisActivity,
                    it
                )
            } ?: run {
                PopNotification.show("安装失败", "无法获取安装包 uri")
            }
        }

        fun ApkBTNonClick2() {
            uri?.let {
                U.installApk(
                    thisActivity,
                    it
                )
            } ?: run {
                PopNotification.show("安装失败", "无法获取安装包 uri")
            }
        }

        fun MagnetBTNonClick1() {
            uri?.let {
                U.openUrl(it.toString(), true)
            }
        }
        if (inspectionMode || showAudioButton) {
            AudioButtons()
        } else if (showVideoButton) {
            uri?.let { VideoButtons(it) }
        } else if (showApkButton) {
            ApkButtons(S.C.btnText5Apk1.current, ::ApkBTNonClick1)
            ApkButtons(S.C.btnText5Apk2.current, ::ApkBTNonClick2)
        } else if (showMagnetButton) {
            MagnetButtons(S.C.btnTextMagnet1.current, ::MagnetBTNonClick1)
        }

    }


    @Preview(showBackground = true)
    @Composable
    fun DefaultPreview() {
        // 在预览环境中覆盖值。provides 提供明确值，这将覆盖组件嵌套中CompositionLocalProvider的值；providesDefault 提供默认值，
        // CompositionLocalProvider的作用域和它们在代码中的顺序决定了哪个providesDefault生效。
        CompositionLocalProvider(
            S.C.Thumbnail_Height provides 250,
            S.C.Button_Width providesDefault 300,
        ) {
            MyUI("")
        }
    }

}