package org.b3log.siyuan.compose

import android.annotation.SuppressLint
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@SuppressLint("UnrememberedMutableInteractionSource")
@Composable
fun SelectableText(text: String, modifier: Modifier = Modifier, style: TextStyle = TextStyle()) {
    // 支持长按选择操作
    SelectionContainer {
        Text(
            text = text,
            modifier = modifier,
            style = style
                .copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    fontStyle = FontStyle.Normal
                )
        )
    }
}