/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/7/11 下午10:33
 * updated: 2024/7/11 下午10:33
 */

package sc.windom.sofill.annotations

/**
 *  TODO
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@Repeatable
annotation class SillotActivity(
    val TYPE: SillotActivityType = SillotActivityType.Unknown
)
