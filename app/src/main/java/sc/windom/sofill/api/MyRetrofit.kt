package sc.windom.sofill.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object MyRetrofit {
    /**
     * 创建最简单的 Retrofit 实例
     */
    fun createRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}