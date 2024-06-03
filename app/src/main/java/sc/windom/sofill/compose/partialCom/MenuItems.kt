package sc.windom.sofill.compose.partialCom

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable

@Composable
fun DdMenuI(text: @Composable () -> Unit, cb: () -> Unit, icon: @Composable() (() -> Unit)? = null) {
    return DropdownMenuItem(
        text = text,
        leadingIcon = icon,
        onClick = cb
    )
}