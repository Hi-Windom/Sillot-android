/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/7/6 下午8:57
 * updated: 2024/7/6 下午8:57
 */

package sc.windom.sofill.compose

import android.webkit.WebView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tencent.mmkv.MMKV
import sc.windom.sofill.Ss.S_Webview
import sc.windom.sofill.pioneer.rememberSaveableMMKV
import kotlin.math.roundToInt


@Composable
fun SettingScreen(thisWebView: MutableState<WebView?>) {
    val selectedOption_搜索引擎 = rememberSaveableMMKV(
        mmkv = MMKV.defaultMMKV(),
        key = "WebViewContainer@selectedOption_搜索引擎",
        defaultValue = S_Webview.searchEngines.keys.first()
    )
    val switchState_使用系统自带下载器下载文件 = rememberSaveableMMKV(
        mmkv = MMKV.defaultMMKV(),
        key = "WebViewContainer@switchState_使用系统自带下载器下载文件",
        defaultValue = false
    )
    var sliderState_webViewTextZoom = rememberSaveableMMKV(
        mmkv = MMKV.defaultMMKV(),
        key = "WebViewContainer@sliderState_webViewTextZoom",
        defaultValue = 100
    )
    val stepValue_webViewTextZoom = 5 // 步长
    val minValue_webViewTextZoom = 50
    val maxValue_webViewTextZoom = 150
    val stepSize_webViewTextZoom =
        (maxValue_webViewTextZoom - minValue_webViewTextZoom) / stepValue_webViewTextZoom // 分为多少块

    Column(modifier = Modifier.padding(start = 6.dp, end = 6.dp, top = 3.dp, bottom = 3.dp)) {
        Text(text = "设置", style = MaterialTheme.typography.headlineMedium)

        // 单选按钮组
        SettingRadioButton(
            title = "搜索引擎",
            options = S_Webview.searchEngines,
            saveOption = selectedOption_搜索引擎.value,
            onValueChanged = { k ->
                selectedOption_搜索引擎.value = k
            }
        )

        // Switch组件
        SettingSwitch("使用系统自带下载器下载文件", switchState_使用系统自带下载器下载文件) // TODO
        // 根据switchState的变化来执行操作
        if (switchState_使用系统自带下载器下载文件.value) {
            Text("使用系统自带下载器，用户可在通知栏查看下载进度。不同系统表现不一致。")
        } else {
            Text("使用汐洛下载器，功能更加强大，不同系统表现一致，稳定性较差。如果出现问题请切换回系统自带下载器。")
        }

        // 滑块组件
        Text(text = "WebView缩放比例", fontSize = 16.sp)
        Slider(
            value = sliderState_webViewTextZoom.value.toFloat(),
            onValueChange = {
                // 将滑块的值四舍五入到最接近的步长
                val roundedValue =
                    (it / stepValue_webViewTextZoom).roundToInt() * stepValue_webViewTextZoom
                // 仅当值在范围内时才更新状态
                if (roundedValue in minValue_webViewTextZoom..maxValue_webViewTextZoom) {
                    sliderState_webViewTextZoom.value = roundedValue
                }
            },
            valueRange = minValue_webViewTextZoom.toFloat()..maxValue_webViewTextZoom.toFloat(),
            onValueChangeFinished = {
                thisWebView.value?.settings?.let {
                    it.textZoom = sliderState_webViewTextZoom.value
                }
            },
            steps = stepSize_webViewTextZoom,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary
            )
        )
        Text(text = "当前缩放比例：${sliderState_webViewTextZoom.value}%")
    }
}

@Composable
fun SettingRadioButton(
    title: String,
    options: Map<String, String>,
    saveOption: String,
    onValueChanged: (key: String) -> Unit
) {
    var selectedOption = rememberSaveable { mutableStateOf(saveOption) }

    Column {
        Text(title)
        options.forEach { (k, v) ->
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedOption.value == k,
                    onClick = {
                        selectedOption.value = k
                        onValueChanged(k)
                    }
                )
                Text(text = k)
            }
        }
    }
}

@Composable
fun SettingSwitch(
    title: String,
    state: MutableState<Boolean>,
    onValueChanged: ((Boolean) -> Unit)? = null
) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Text(title)
        HorizontalDivider(
            modifier = Modifier
                .weight(1f), thickness = 0.dp, color = Color.Transparent
        )
        Switch(
            checked = state.value,
            onCheckedChange = { state.value = it; onValueChanged?.invoke(it) }
        )
    }
}
