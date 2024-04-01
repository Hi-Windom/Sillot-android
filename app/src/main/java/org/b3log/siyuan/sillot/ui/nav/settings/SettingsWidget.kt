package org.b3log.siyuan.sillot.ui.nav.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.b3log.siyuan.sillot.ui.widgets.AppDialog

@Composable
internal fun DropdownPreference(
    modifier: Modifier = Modifier,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    icon: @Composable () -> Unit,
    title: @Composable () -> Unit,
    subTitle: @Composable () -> Unit,
    actions: @Composable ColumnScope. () -> Unit = {}
) {
    BasePreferenceWidget(modifier = modifier, icon = icon, onClick = {
        onExpandedChange(true)
    }, title = title, subTitle = subTitle) {
        DropdownMenu(
            modifier = Modifier.align(Alignment.Top),
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }) {
            actions()
        }
    }
}

@Composable
internal fun DividerPreference(title: @Composable () -> Unit) {
    Column(Modifier.padding(top = 4.dp)) {
        HorizontalDivider(thickness = 0.5.dp)
        Row(
            Modifier
                .padding(vertical = 8.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            CompositionLocalProvider(
                LocalTextStyle provides MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                ),
            ) {
                title()
            }
        }
    }

}

@Composable
internal fun SwitchPreference(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    subTitle: @Composable () -> Unit,
    icon: @Composable () -> Unit = {},

    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    BasePreferenceWidget(
        modifier = modifier,
        onClick = { onCheckedChange(!checked) },
        title = title,
        subTitle = subTitle,
        icon = icon,
        content = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                Modifier.align(Alignment.CenterVertically)
            )
        }
    )
}

@Composable
internal fun BasePreferenceWidget(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    title: @Composable () -> Unit,
    subTitle: @Composable () -> Unit = {},
    icon: @Composable () -> Unit = {},
    content: @Composable RowScope.() -> Unit = {},
) {
    Row(modifier = modifier
        .minimumInteractiveComponentSize()
        .defaultMinSize(minHeight = 64.dp)
        .clip(MaterialTheme.shapes.extraSmall)
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = rememberRipple()
        ) {
            onClick()
        }
        .padding(8.dp)
    ) {
        Column(
            Modifier.align(Alignment.CenterVertically)
        ) {
            icon()
        }

        Column(
            Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
                .padding(horizontal = 8.dp)
        ) {
            CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.titleMedium) {
                title()
            }

            CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyMedium) {
                subTitle()
            }
        }

        Row(Modifier.align(Alignment.CenterVertically)) {
            content()
        }
    }
}

@Composable
internal fun SliderPreference(
    title: @Composable () -> Unit,
    subTitle: @Composable () -> Unit,
    icon: @Composable () -> Unit = {},
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    label: @Composable (title: @Composable () -> Unit) -> Unit,
) {
    val view = LocalView.current
    LaunchedEffect(value) {
        view.announceForAccessibility(value.toString())
    }

}

@Composable
internal fun PreferenceDialog(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    subTitle: @Composable () -> Unit,
    icon: @Composable () -> Unit,

    dialogContent: @Composable ColumnScope.() -> Unit,
    endContent: @Composable RowScope.() -> Unit = {},
) {
    var showDialog by remember { mutableStateOf(false) }
    if (showDialog) {
        AppDialog(
            title = title,
            content = {
                Column {
                    dialogContent()
                }
            },
            onDismissRequest = { showDialog = false }
        )
    }
    BasePreferenceWidget(modifier, onClick = {
        showDialog = true
    }, title = title, icon = icon, subTitle = subTitle) {
        endContent()
    }
}