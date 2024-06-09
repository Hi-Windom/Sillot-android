package sc.windom.sofill.compose.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CascadeMaterialTheme(
    content: @Composable () -> Unit
) {
    val LightColorScheme = lightColorScheme(
        primary = Color(0xFFB5D2C3),
        background = Color(0xFFB5D2C3),
        surface = Color(0xFFE5F0EB),
        onSurface = Color(0xFF356859),
        onSurfaceVariant = Color(0xFF356859),
    )
    val DarkColorScheme = darkColorScheme(
        primary = Color(0xFF1F1B1D),
        background = Color(0xF0131415),
        surface = Color(0xFF323F4B),
        onSurface = Color(0xFF4F9D69),
        onSurfaceVariant = Color(0xFF4F9D69),
    )
    val colorScheme =
        if (isSystemInDarkTheme()) {
            DarkColorScheme
        } else {
            LightColorScheme
        }
    val typography = Typography(
        titleLarge = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
        ),
        labelLarge = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
        ),
    )
    val shapes = Shapes(
        extraSmall = RoundedCornerShape(12.dp)
    )
    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = shapes
    ) {
        content()
    }
}