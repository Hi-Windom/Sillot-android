package org.b3log.siyuan.sillot.ui

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import org.b3log.siyuan.sillot.model.ShortCuts
import org.b3log.siyuan.sillot.ui.nav.BottomNavBar
import org.b3log.siyuan.sillot.ui.nav.NavigationGraph
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch

val LocalMainViewModel = staticCompositionLocalOf<MainViewModel> {
    error("No MainViewModel provided")
}

class MainActivity : BaseComposeActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    private val vm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            ShortCuts.buildShortCuts(this@MainActivity)
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    override fun Content() {
        val scope = rememberCoroutineScope()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // A13
            val notificationPermission =
                rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)

            if (!notificationPermission.status.isGranted)
                LaunchedEffect(key1 = notificationPermission) {
                    notificationPermission.launchPermissionRequest()
                }
        }


        CompositionLocalProvider(
            LocalMainViewModel provides vm
        ) {

            val navController = rememberNavController()
            Scaffold(
                bottomBar = {
                    BottomNavBar(navController)
                }
            ) {
                NavigationGraph(
                    navController = navController,
                    Modifier.padding(bottom = it.calculateBottomPadding())
                )
            }
        }
    }


}