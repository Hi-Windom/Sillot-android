/*
 * SiYuan - 源于思考，饮水思源
 * Copyright (c) 2020-present, b3log.org
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.b3log.siyuan

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import cn.jpush.android.api.JPushInterface
import com.blankj.utilcode.util.Utils
import org.b3log.siyuan.common.ForegroundPushManager

/**
 * SiYuan Application.
 *
 * @author [Liang Ding](http://88250.b3log.org)
 * @version 1.0.0.0, Feb 23, 2022
 * @since 1.0.0
 */
class App : Application() {
    override fun onCreate() {
        var refCount = 0
        super.onCreate()
        Utils.init(this)
        JPushInterface.setDebugMode(true)
        JPushInterface.init(this)
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityPaused(activity: Activity) {
            }

            override fun onActivityStarted(activity: Activity) {
                refCount++
            }

            override fun onActivityDestroyed(activity: Activity) {
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            }

            override fun onActivityStopped(activity: Activity) {
                refCount--
                if (refCount == 0) {
                    ForegroundPushManager.showNotification(this@App)
                }
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            }

            override fun onActivityResumed(activity: Activity) {
                ForegroundPushManager.stopNotification(this@App)
            }

        })


    }
}