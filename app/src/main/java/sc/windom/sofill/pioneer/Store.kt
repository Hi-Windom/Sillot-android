/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/7/8 下午8:13
 * updated: 2024/7/8 下午8:13
 */

package sc.windom.sofill.pioneer

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.core.content.edit
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.serializer
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
 * 创建一个支持序列化 @Serializable 的状态，并使用 MMKV 库进行状态的保存和恢复。当值发生变化时自动保存，无需用户额外 LaunchedEffect 和 DisposableEffect
 * 由于rememberSaveable不支持复杂的数据类型。此函数使用remember而不是rememberSaveable来创建状态。
 * TODO: 支持 @Parcelize ( fun rememberParcelizeMMKV）
 *
 * @param mmkv MMKV 实例，用于状态的保存和恢复。
 * @param key 在 MMKV 中用于标识状态的键。
 * @param defaultValue 状态的默认值，当从 MMKV 中恢复状态时，如果找不到对应的键，则返回此默认值。
 * @param onLaunchEffect 在 LaunchedEffect 中运行的可选回调函数。第一个参数与传参key一致，第二个参数与返回值一致。
 * @return 返回一个 MutableState<T> 对象，其中 T 是状态的实际类型。
 */
@Composable
inline fun <reified T : Any> rememberSerializableMMKV(
    mmkv: MMKV,
    key: String,
    defaultValue: T,
    noinline onLaunchEffect: ((key: String, it: MutableState<T>) -> Unit)? = null
): MutableState<T> {
    val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true } // 创建Json实例，可以根据需要配置
    val serializer = serializer<T>() // 获取T类型的序列化器

    // 从MMKV中恢复状态，如果不存在，则使用默认值
    val restoredValue = mmkv.getString(key, null)?.let { json.decodeFromString(serializer, it) } ?: defaultValue

    // 创建可持久化的状态
    val state = remember { mutableStateOf(restoredValue) }

    // 当状态发生变化时，保存到MMKV
    LaunchedEffect(state.value) {
        mmkv.encode(key, json.encodeToString(serializer, state.value))
        onLaunchEffect?.invoke(key, state)
    }

    // 在配置更改时，从MMKV恢复状态
    DisposableEffect(key) {
        onDispose {
            state.value = mmkv.getString(key, null)?.let { json.decodeFromString(serializer, it) } ?: defaultValue
        }
    }

    return state
}


/**
 * 创建一个支持序列化 @Serializable 的 Flow 状态，并使用 MMKV 库进行状态的保存和恢复。当值发生变化时自动保存。配合 ViewModel 使用
 * @param mmkv MMKV 实例，用于状态的保存和恢复。
 * @param key 在 MMKV 中用于标识状态的键。
 * @param defaultValue 状态的默认值，当从 MMKV 中恢复状态时，如果找不到对应的键，则返回此默认值。
 * @param onInit 初始化后需要做什么，提供一个可空的 savedValue ，从MMKV中恢复。应当只进行数据操作，不要直接操作UI
 * @return 返回一个 MutableStateFlow<T?> 对象，其中 T 是状态的实际类型。
 * @suppress T 不能为可变类型，例如 MutableMap 。在 Kotlin 中，MutableMap 是一个可变的映射，而 Map 是一个不可变的映射。如果源必须是 MutableMap ，可以转换为 Map 传递
 */
inline fun <reified T : Any> savableStateFlowMMKV(
    mmkv: MMKV,
    key: String,
    defaultValue: T?,
    noinline onInit: ((savedValue: T?) -> Unit)? = null
): MutableStateFlow<T?> {
    val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
    val serializer = serializer<T>()

    // 从MMKV中恢复状态，如果不存在，则使用默认值
    val _saved = mmkv.getString(key, null)?.let { json.decodeFromString(serializer, it) }
    val _stateFlow = _saved?.let { MutableStateFlow(it) } ?: MutableStateFlow(defaultValue)
    // 使用类型转换来消除 CapturedType
    val stateFlow: MutableStateFlow<T?> = _stateFlow as MutableStateFlow<T?>

    CoroutineScope(Dispatchers.IO).launch { // Use GlobalScope.launch if you want the coroutine to be lifecycle-aware
        onInit?.invoke(_saved)
        // 当状态发生变化时，保存到MMKV
        stateFlow.collect { value ->
            // Serialize and save only the value, not the StateFlow itself
            value?.let {
                Log.d("savableStateFlowMMKV", "value.hashCode ${value.hashCode()}")
                mmkv.encode(key, json.encodeToString(serializer(), value))
            }
        }
    }

    return stateFlow
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


