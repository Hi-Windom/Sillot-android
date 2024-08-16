/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/8/12 22:43
 * updated: 2024/8/12 22:43
 */

package sc.windom.sofill.base

import android.util.Log

/**
 * 调试吉尼太美
 */
open class Debuggable(protected val TAG: String) {
    enum class DebugLevel(val level: Int) {
        /**
         * # 禁用调试, 捕获的报错仍然会输出
         */
        DISABLED(1),
        /**
         * # 基本调试
         */
        MINIMAL(2),
        /**
         * # 详细调试
         */
        VERBOSE(3);

        companion object {
            fun fromLevel(level: Int) = DebugLevel.entries.firstOrNull { it.level == level }
        }
    }

    /**
     * # 调试级别
     *
     * - 禁用调试, 捕获的报错仍然会输出 [DebugLevel.DISABLED]
     * - 基本调试 [DebugLevel.MINIMAL]
     * - 详细调试 [DebugLevel.VERBOSE]
     */
    @JvmField
    var debugLevel: DebugLevel = DebugLevel.MINIMAL

    /**
     * # 输出日志的 Tag 标签
     */
    var debugTag: String = TAG
        set(value) {
            field = "$TAG-$value"
        }

    /**
     * # 自定义 logger , TODO
     */
//    var logger: Any? = null

    /**
     * debugLevel == DebugLevel.VERBOSE
     */
    protected fun bug(message: String) {
        if (debugLevel == DebugLevel.VERBOSE) Log.d(debugTag, message)
    }

    /**
     * debugLevel >= DebugLevel.MINIMAL
     */
    protected fun log(message: String) {
        if (debugLevel >= DebugLevel.MINIMAL) Log.i(debugTag, message)
    }

    /**
     * debugLevel >= DebugLevel.MINIMAL
     */
    protected fun warn(message: String) {
        if (debugLevel >= DebugLevel.MINIMAL) Log.w(debugTag, message)
    }

    /**
     * 任意 debugLevel
     */
    protected fun error(message: String) {
        Log.e(debugTag, message)
    }
}