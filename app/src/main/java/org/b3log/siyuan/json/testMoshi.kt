package org.b3log.siyuan.json

import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

fun testmoshi() {
    val TAG = "testmoshi"
    val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val person = Person("Alice", 30, "alice@example.com")

    val jsonAdapter = moshi.adapter(Person::class.java)
    val json = jsonAdapter.toJson(person)

    Log.w(TAG, json)
}
