/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/7/20 11:13
 * updated: 2024/7/20 11:13
 */

package sc.windom.sofill.api.siyuan

import androidx.compose.runtime.MutableState
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import sc.windom.sofill.dataClass.INbList
import sc.windom.sofill.dataClass.INotebook
import sc.windom.sofill.dataClass.IPayload
import sc.windom.sofill.dataClass.IResponse

object siyuan_Works {

    /**
     * 获取笔记本列表
     */
    fun getNotebooks(
        api: SiyuanNoteAPI,
        token: String,
        callback: (notebooks: List<INotebook>?, info: String, code: Int?) -> Unit
    ) {
        val body = mapOf("flashcard" to false)
        val notebooksCall = api.getNotebooks(token, body)
        notebooksCall.enqueue(object : Callback<IResponse<INbList>> {
            override fun onResponse(
                call: Call<IResponse<INbList>>,
                response: Response<IResponse<INbList>>
            ) {
                if (response.isSuccessful && response.body()?.code == 0) {
                    callback(
                        response.body()?.data?.notebooks,
                        "${response.body()}",
                        response.code()
                    )
                } else {
                    callback(
                        null,
                        "Failed to get notebooks: ${response.message()} \n ${response.body()}",
                        response.code()
                    )
                }
            }

            override fun onFailure(call: Call<IResponse<INbList>>, t: Throwable) {
                callback(null, "getNotebooks Error: ${t.message}", null)
            }
        })
    }


    /**
     * 创建新的 Markdown 笔记
     */
    fun createNote(
        api: SiyuanNoteAPI,
        payload: IPayload,
        token: MutableState<String?>,
        callback: (success: Boolean, info: String, response: Response<IResponse<String>>?) -> Unit
    ) {
        val createNoteCall = api.createNote(payload, token.value)
        createNoteCall.enqueue(object : Callback<IResponse<String>> {
            override fun onResponse(
                call: Call<IResponse<String>>,
                response: Response<IResponse<String>>
            ) {
                if (response.isSuccessful && response.body()?.code == 0) {
                    callback(true, "Note created successfully. ${response.body()}", response)
                } else {
                    callback(
                        false,
                        "Failed to create note: ${response.message()} \n ${response.body()}",
                        response
                    )
                }
            }

            override fun onFailure(call: Call<IResponse<String>>, t: Throwable) {
                callback(false, "createNote Error: ${t.message}", null)
            }
        })
    }
}