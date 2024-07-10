/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/7/10 下午11:00
 * updated: 2024/7/10 下午11:00
 */

package sc.windom.sillot

import androidx.activity.ComponentActivity
import sc.windom.sofill.Us.thisSourceFilePath
import sc.windom.sofill.pioneer.SillotActivity
import sc.windom.sofill.pioneer.SillotActivityType

@SillotActivity(SillotActivityType.Launcher)
@SillotActivity(SillotActivityType.UseInVisible)
class AppActivity : ComponentActivity() {
    private val TAG = "AppActivity.kt"
    private val srcPath = thisSourceFilePath(TAG)
}