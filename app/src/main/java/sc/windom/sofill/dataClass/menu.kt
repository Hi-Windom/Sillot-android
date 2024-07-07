/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/7/8 上午3:23
 * updated: 2024/7/8 上午3:23
 */

package sc.windom.sofill.dataClass

import android.os.Parcelable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue


@Parcelize
sealed class MenuOptionState : Parcelable {
    data object Disabled : MenuOptionState()
    data object Default : MenuOptionState()
    data object Active : MenuOptionState()
}

@Parcelize
data class MenuOption(
    val title: String,
    val icon: @RawValue ImageVector,
    val iconInActive: @RawValue ImageVector = icon,
    val titleInActive: String = title,
    val state: MenuOptionState = MenuOptionState.Default,
    val isActive: @RawValue MutableState<Boolean> = mutableStateOf(state == MenuOptionState.Active),
    val canToggle: Boolean = false, // 表示选项是否可以在 Default 和 Active 之间切换
    val closeMenuAfterClick: Boolean = state != MenuOptionState.Disabled, // 点击后是否关闭菜单（需要重新渲染）, 默认值为非禁用则为 true
    val onClick: () -> Unit,
) : Parcelable