package org.b3log.siyuan.sillot.ui.nav.web

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
fun WebScreen(modifier: Modifier = Modifier) {
    Scaffold {

        Column(modifier = modifier.padding(it)) {
            val url = remember { "https://sillot.db.sc.cn" }
            WebView(url = url)

        }
    }
}