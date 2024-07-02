package sc.windom.sofill.pioneer

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.core.content.edit
import com.tencent.mmkv.MMKV
import sc.windom.sofill.S

/**
 * 从汐洛全局SharedPreferences中读取布尔值。
 *
 * @param key 键，用于在SharedPreferences中标识特定的值。
 * @param defaultValue 如果在SharedPreferences中找不到对应的键，返回的默认值。
 * @return 返回与键关联的布尔值，如果不存在则返回默认值。
 */
@Deprecated("SharedPreferences 线程不安全", ReplaceWith("MMKV.getSavedValue"))
fun Context.getSavedBoolean(key: String, defaultValue: Boolean): Boolean {
    val sharedPreferences = getSharedPreferences(S.AppQueryIDs.汐洛, Context.MODE_PRIVATE)
    return sharedPreferences.getBoolean(key, defaultValue)
}

/**
 * 将布尔值保存到汐洛全局SharedPreferences中。
 *
 * @param key 键，用于在SharedPreferences中标识特定的值。
 * @param value 要保存的布尔值。
 */
@Deprecated("SharedPreferences 线程不安全", ReplaceWith("MMKV.saveValue"))
fun Context.saveBoolean(key: String, value: Boolean) {
    val sharedPreferences = getSharedPreferences(S.AppQueryIDs.汐洛, Context.MODE_PRIVATE)
    sharedPreferences.edit { putBoolean(key, value) }
}

/**
 * 创建一个可持久化的状态，并使用 MMKV 库进行状态的保存和恢复。当值发生变化时自动保存，无需用户额外 LaunchedEffect 和 DisposableEffect
 *
 * @param mmkv MMKV 实例，用于状态的保存和恢复。
 * @param key 在 MMKV 中用于标识状态的键。
 * @param defaultValue 状态的默认值，当从 MMKV 中恢复状态时，如果找不到对应的键，则返回此默认值。
 * @param onLaunchEffect 在 LaunchedEffect 中运行的可选回调函数。第一个参数与传参key一致，第二个参数与返回值一致。
 * @return 返回一个 MutableState<T> 对象，其中 T 是状态的实际类型。
 */
@Composable
inline fun <reified T : Any> rememberSaveableMMKV(
    mmkv: MMKV,
    key: String,
    defaultValue: T,
    noinline onLaunchEffect: ((key: String, it: MutableState<T>) -> Unit?)? = null
): MutableState<T> {
    val state = rememberSaveable { mutableStateOf(mmkv.getSavedValue(key, defaultValue)) }
    LaunchedEffect(state.value) {
        mmkv.saveValue(key, state.value)
        onLaunchEffect?.invoke(key, state)
    }
    DisposableEffect(key) {
        state.value = mmkv.getSavedValue(key, defaultValue)
        onDispose { }
    }

    return state
}

/**
 * 从MMKV中读取一个泛型值。
 *
 * @param key 键，用于在MMKV中标识特定的值。
 * @param defaultValue 如果在MMKV中找不到对应的键，返回的默认值。
 * @return 返回与键关联的泛型值，如果不存在则返回默认值。
 */
inline fun <reified T> MMKV.getSavedValue(key: String, defaultValue: T): T {
    return when (T::class) {
        String::class -> this.getString(key, defaultValue as String) as T
        Int::class -> this.getInt(key, defaultValue as Int) as T
        Float::class -> this.getFloat(key, defaultValue as Float) as T
        Long::class -> this.getLong(key, defaultValue as Long) as T
        Boolean::class -> this.getBoolean(key, defaultValue as Boolean) as T
        // 可以根据需要添加更多类型
        else -> throw IllegalArgumentException("Unsupported data type")
    }
}

/**
 * 将一个泛型值保存到MMKV中。
 *
 * @param key 键，用于在MMKV中标识特定的值。
 * @param value 要保存的泛型值。
 */
inline fun <reified T> MMKV.saveValue(key: String, value: T) {
    when (T::class) {
        String::class -> this.encode(key, value as String)
        Int::class -> this.encode(key, value as Int)
        Float::class -> this.encode(key, value as Float)
        Long::class -> this.encode(key, value as Long)
        Boolean::class -> this.encode(key, value as Boolean)
        // 可以根据需要添加更多类型
        else -> throw IllegalArgumentException("Unsupported data type")
    }
}


