package sc.windom.sofill.dataClass

data class IResponse<T>(val code: Int,
                        val data: T,
                        val msg: String)

data class INbList(val notebooks: List<INotebook>)

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

data class IPayload(val markdown: String, val notebook: String, val path: String)
