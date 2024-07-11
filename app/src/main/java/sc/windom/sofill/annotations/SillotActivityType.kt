/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/7/11 下午10:31
 * updated: 2024/7/11 下午10:31
 */

package sc.windom.sofill.annotations

enum class SillotActivityType {
    /**
     * 未知活动类型
     */
    Unknown,

    /**
     * 主要活动，可以被 AppActivity 启动
     */
    Main,

    /**
     * 桌面启动入口
     */
    Launcher,

    /**
     * 总是可见
     */
    Visible,

    /**
     * 默认可见，满足条件不可见
     */
    UseVisible,

    /**
     * 默认不可见，满足条件可见
     */
    UseInVisible,

    /**
     * 总是不可见
     */
    InVisible,
}
