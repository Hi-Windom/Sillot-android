/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/8/6 05:49
 * updated: 2024/8/6 05:49
 */

package sc.windom.sillot

import androidx.fragment.app.FragmentActivity

abstract class MatrixModel: FragmentActivity() {
    /**
     * 基于此类的活动需要重写，例如汐洛绞架
     * 抽象方法。这样，任何继承自MatrixModel的类都必须实现这个方法，否则它们将无法被实例化。
     */
    abstract fun getMatrixModel(): String

    /**
     * 基于此类的活动可选是否重写
     * TODO
     */
    open fun undefined(): String = ""
}