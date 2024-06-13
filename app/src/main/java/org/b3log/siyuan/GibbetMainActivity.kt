package org.b3log.siyuan

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 汐洛绞架，暂时与思源汐洛版共存，逐步迁移过来
 */
class GibbetMainActivity : AppCompatActivity() {
}

@Composable
fun BootScreen() {
    // 整个屏幕的背景色
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
    ) {
        // 垂直居中的内容布局
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 启动Logo
            Image(
                painter = painterResource(id = R.drawable.icon),
                contentDescription = "Boot logo",
                modifier = Modifier.size(296.dp)
            )
            // 进度条
            LinearProgressIndicator(
                modifier = Modifier
                    .width(300.dp)
                    .padding(top = 16.dp),
                color = Color(0xFFD23F31)
            )
            // 启动细节文本
            Text(
                text = "Boot details", // 这里需要替换为实际的启动详情文本
                fontSize = 8.sp,
                modifier = Modifier.padding(top = 2.dp, start = 16.dp, end = 16.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BootScreenPreview() {
    BootScreen()
}