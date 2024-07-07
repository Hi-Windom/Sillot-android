/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/7/7 下午7:08
 * updated: 2024/7/7 下午7:08
 */

package sc.windom.potter.producer

import android.app.Activity
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.MutableState
import com.kongzue.dialogx.dialogs.BottomMenu
import com.kongzue.dialogx.dialogs.PopNotification
import com.kongzue.dialogx.interfaces.OnMenuButtonClickListener
import com.tencent.bugly.crashreport.BuglyLog
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mobile.Mobile
import org.b3log.siyuan.App
import sc.windom.sofill.U
import sc.windom.sofill.api.MyRetrofit.createRetrofit
import sc.windom.sofill.api.siyuan.SiyuanNoteAPI
import sc.windom.sofill.api.siyuan.siyuan
import sc.windom.sofill.dataClass.ICreateDocWithMdRequest
import sc.windom.sofill.dataClass.INotebook
import sc.windom.sofill.dataClass.IPayload
import java.util.Date

class GibbetPro {
    val TAG = "GibbetPro.kt"
    var thisActivity: Activity

    constructor(
        activity: Activity
    ) {
        thisActivity = activity
    }

    // 在协程中调用sendMD2siyuan
    fun runSendMD2siyuan(markdownContent: String, token: MutableState<String?>) =
        runBlocking<Unit> { // 启动主协程
            launch { // 启动一个新协程并运行挂起函数，避免阻塞UI
                sendMD2siyuan(markdownContent, token)
            }
        }

    fun sendMD2siyuan(markdownContent: String, token: MutableState<String?>) {
        val retrofit = createRetrofit("http://0.0.0.0:58131/")
        val api = retrofit.create(SiyuanNoteAPI::class.java)
        val helpInfo =
            "请注意：（1）TOKEN是否正确；（2）当前工作空间是否存在有效笔记本；（3）笔记本是否被关闭了"
        token.value?.let { _token ->
            siyuan.Works.getNotebooks(api, _token) { notebooks, info ->
                if (notebooks.isNullOrEmpty()) {
                    // 处理笔记本列表为空的情况
                    thisActivity.runOnUiThread {
                        PopNotification.show(
                            TAG, "No notebooks received. reason:\n$info\n$helpInfo"
                        ).noAutoDismiss()
                    }
                } else {
                    // 处理获取到的笔记本列表
                    BuglyLog.i(TAG, "Received ${notebooks.size} notebooks.")
                    val notebookIDs: Array<String> = notebooks.map { it.id }.toTypedArray()
                    val notebookInfos: Array<String> = notebooks.map {
                        "（${if (it.closed) "不可用" else "可使用"}）${it.name}"
                    }.toTypedArray()
                    var selectMenuIndex = 0
                    BottomMenu.show(notebookInfos).setMessage("仅支持当前工作空间")
                        .setTitle("选择要存入的笔记本").setSelection(selectMenuIndex) //指定已选择的位置
                        .setOnMenuItemClickListener { dialog, text, index ->
                            selectMenuIndex = index
                            dialog.refreshUI() // 在 compose 里需要强制刷新
                            true // 点击菜单后不会自动关闭
                        }.setOkButton("确定", OnMenuButtonClickListener { menu, view ->
                            val notebookId = notebookIDs[selectMenuIndex]
                            BuglyLog.e(TAG, notebookId)
                            val payload = IPayload(
                                markdownContent, notebookId, "/来自汐洛受赏 ${
                                    U.dateFormat_full1.format(
                                        Date()
                                    )
                                }"
                            )

                            siyuan.Works.createNote(api, payload, token) { success, info ->
                                if (success) {
                                    // 处理创建笔记成功的情况
                                    BuglyLog.i(TAG, "Note creation succeeded. $info")
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
                        }).setCancelButton("取消", OnMenuButtonClickListener { menu, view ->
                            false
                        })
                }
            }
        }
    }

    fun sendMD2siyuanWithoutToken(md: String) {
        val helpInfo =
            "请注意：（1）内核是否存活；（2）当前工作空间是否存在有效笔记本；（3）笔记本是否被关闭了"

        val response = Mobile.getNotebooks(false)
        if (response.error.isNotEmpty()) {
            BuglyLog.e(TAG, "Error: ${response.error}")
        } else {
            val notebooksJSON = response.notebooksJSON
            val notebooks =
                kotlinx.serialization.json.Json.decodeFromString<List<INotebook>>(notebooksJSON)
            if (notebooks.isEmpty()) {
                // 处理笔记本列表为空的情况
                thisActivity.runOnUiThread {
                    PopNotification.show(
                        TAG, "No notebooks received. \n$helpInfo"
                    ).noAutoDismiss()
                }
            } else {
                // 处理获取到的笔记本列表
                BuglyLog.i(TAG, "Received ${notebooks.size} notebooks.")
                val notebookIDs: Array<String> = notebooks.map { it.id }.toTypedArray()
                val notebookInfos: Array<String> = notebooks.map {
                    "（${if (it.closed) "不可用" else "可使用"}）${it.name}"
                }.toTypedArray()
                var selectMenuIndex = 0
                BottomMenu.show(notebookInfos).setMessage("仅支持当前工作空间")
                    .setTitle("选择要存入的笔记本").setSelection(selectMenuIndex) //指定已选择的位置
                    .setOnMenuItemClickListener { dialog, text, index ->
                        selectMenuIndex = index
                        dialog.refreshUI() // 在 compose 里需要强制刷新
                        true // 点击菜单后不会自动关闭
                    }.setOkButton("确定", OnMenuButtonClickListener { menu, view ->
                        menu.dismiss()

                        val notebookId = notebookIDs[selectMenuIndex]
                        BuglyLog.i(TAG, notebookId)

                        // 使用Handler来执行耗时操作
                        val handler = Handler(Looper.getMainLooper())
                        handler.post {
                            // 在子线程中执行耗时操作
                            val payload = ICreateDocWithMdRequest(
                                Notebook = notebookId,
                                Path = "/来自汐洛受赏 ${U.dateFormat_full1.format(Date())}",
                                Markdown = md
                            )
                            val paramsJSON = kotlinx.serialization.json.Json.encodeToString(ICreateDocWithMdRequest.serializer(), payload)
                            BuglyLog.i(TAG, paramsJSON)

                            val response2 = Mobile.createDocWithMd(paramsJSON)

                            // 切换回主线程更新UI
                            thisActivity.runOnUiThread {
                                if (response2.error.isEmpty()) {
                                    BuglyLog.i(TAG, "Created document with ID: ${response2.id}")
                                    U.startMainActivityWithBlock(
                                        "siyuan://blocks/${response2.id}",
                                        App.application
                                    )
                                } else {
                                    BuglyLog.e(TAG, "Error: ${response2.error}")
                                    PopNotification.show(
                                        TAG,
                                        "Error: ${response2.error}\n$helpInfo"
                                    ).noAutoDismiss()
                                }
                            }
                        }
                        false
                    }).setCancelButton("取消", OnMenuButtonClickListener { menu, view ->
                        false
                    })
            }
        }
    }
}
