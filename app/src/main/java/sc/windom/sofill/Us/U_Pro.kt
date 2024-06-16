package sc.windom.sofill.Us

import android.app.Activity
import android.content.Intent
import android.view.DragEvent
import android.view.View

object U_Pro {

    @JvmStatic
    fun View.onDragSend2Producer(event: DragEvent, targetActivityClass: Class<out Activity>) {
        when (event.action) {
            DragEvent.ACTION_DROP -> {
                val clipData = event.clipData
                if (clipData != null && clipData.itemCount > 0) {
                    val item = clipData.getItemAt(0)
                    val intent = Intent(this.context, targetActivityClass)
                    when {
                        item.uri != null -> {
                            intent.data = item.uri
                            intent.action = event.action.toString()
                        }
                        item.text != null -> {
                            val text = item.text.toString()
                            intent.putExtra("text_data", text)
                            intent.action = Intent.ACTION_SEND
                            intent.type = "text/plain"
                        }
                    }
                    this.context.startActivity(intent)
                }
            }
        }
    }
}