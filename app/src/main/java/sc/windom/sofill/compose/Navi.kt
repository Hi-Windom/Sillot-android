/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/7/8 上午4:16
 * updated: 2024/7/8 上午4:16
 */

package sc.windom.sofill.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LastPage
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.FirstPage
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex

@Composable
fun MultiIndexNavigationBottomBar(
    currentIndex: MutableIntState,
    maxIndex: MutableIntState,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onFirst: () -> Unit,
    onLast: () -> Unit
) {
    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 58.dp,
        contentPadding = PaddingValues(2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp)
            .height(36.dp)
            .zIndex(999f)
    )
    {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onFirst, enabled = currentIndex.intValue != 1) {
                Icon(Icons.Filled.FirstPage, contentDescription = "First")
            }
            IconButton(onClick = onPrevious, enabled = currentIndex.intValue != 1) {
                Icon(Icons.AutoMirrored.Filled.NavigateBefore, contentDescription = "Previous")
            }
            Text(
                "${currentIndex.intValue} / ${maxIndex.intValue}",
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
            )
            IconButton(onClick = onNext, enabled = currentIndex.intValue != maxIndex.intValue) {
                Icon(Icons.AutoMirrored.Filled.NavigateNext, contentDescription = "Next")
            }
            IconButton(onClick = onLast, enabled = currentIndex.intValue != maxIndex.intValue) {
                Icon(Icons.AutoMirrored.Filled.LastPage, contentDescription = "Last")
            }
        }
    }
}