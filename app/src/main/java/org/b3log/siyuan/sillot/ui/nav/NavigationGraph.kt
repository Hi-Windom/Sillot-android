package org.b3log.siyuan.sillot.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.b3log.siyuan.sillot.ui.nav.alist.AListScreen
import org.b3log.siyuan.sillot.ui.nav.config.AListConfigScreen
import org.b3log.siyuan.sillot.ui.nav.provider.AListProviderScreen
import org.b3log.siyuan.sillot.ui.nav.settings.SettingsScreen
import org.b3log.siyuan.sillot.ui.nav.web.WebScreen

@Composable
fun NavigationGraph(navController: NavHostController, modifier: Modifier) {
    NavHost(
        navController,
        startDestination = BottomNavRoute.AList.id,
        modifier = modifier
    ) {
        composable(BottomNavRoute.AListConfig.id) {
            AListConfigScreen()
        }

        composable(BottomNavRoute.AList.id) {
            AListScreen()
        }

        composable(BottomNavRoute.Settings.id) {
            SettingsScreen()
        }

        composable(BottomNavRoute.Web.id) {
            WebScreen()
        }

        composable(BottomNavRoute.AListProvider.id) {
            AListProviderScreen()
        }

    }
}