package com.mealmatch.data.network

import com.mealmatch.data.network.service.GroupApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "http://10.0.2.2:3000/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val groupApiService: GroupApiService by lazy {
        retrofit.create(GroupApiService::class.java)
    }
}