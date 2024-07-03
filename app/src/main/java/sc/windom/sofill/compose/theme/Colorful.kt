package sc.windom.sofill.compose.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver

@Composable
fun disabledColor(): Color {
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val disabledAlpha = 0.38f // 这是 Material Design 推荐的禁用状态透明度
    return onSurfaceColor.copy(alpha = disabledAlpha).compositeOver(MaterialTheme.colorScheme.surface)
}

@Composable
fun defaultColor(): Color {
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val disabledAlpha = 1f
    return onSurfaceColor.copy(alpha = disabledAlpha).compositeOver(MaterialTheme.colorScheme.surface)
}

@Composable
fun activeColor(): Color {
    val onSurfaceColor = MaterialTheme.colorScheme.onSurfaceVariant
    val disabledAlpha = 1f
    return onSurfaceColor.copy(alpha = disabledAlpha).compositeOver(MaterialTheme.colorScheme.surface)
}