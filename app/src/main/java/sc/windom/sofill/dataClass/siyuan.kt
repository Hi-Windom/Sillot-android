package sc.windom.sofill.dataClass

import kotlinx.serialization.Serializable

data class IResponse<T>(val code: Int,
                        val data: T,
                        val msg: String)

data class INbList(val notebooks: List<INotebook>)

@Serializable
data class INotebook(
    val closed: Boolean,
    val dueFlashcardCount: Int,
    val flashcardCount: Int,
    val icon: String,
    val id: String,
    val name: String,
    val newFlashcardCount: Int,
    val sort: Int,
    val sortMode: Int
)

@Serializable
data class IPayload(val markdown: String, val notebook: String, val path: String)

@Serializable
data class ICreateDocWithMdRequest(
    val Markdown: String, val Notebook: String, val Path: String,
    val ID: String? = null,
    val ParentID: String? = null,
    val WithMath: Boolean = false
    )
