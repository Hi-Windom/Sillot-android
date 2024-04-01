package org.b3log.siyuan.sillot.ui.nav.config

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.b3log.siyuan.sillot.model.alist.AListConfig
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch

class AListConfigViewModel : ViewModel() {
    var config by mutableStateOf(AListConfig())

    fun init() {
        viewModelScope.launch {

        }
    }
}