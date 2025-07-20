package com.mealmatch.data.network


import com.mealmatch.data.network.service.AuthApiService
import com.mealmatch.data.network.service.GroupApiService
import com.mealmatch.data.network.service.ProfilePrefApiService
import com.mealmatch.data.network.service.SessionApiService

import com.mealmatch.data.network.service.FriendApiService

import com.mealmatch.data.network.service.PostApiService

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "http://10.0.2.2:3000/"

    // Create an OkHttpClient to add an interceptor
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val newRequest = originalRequest.newBuilder()
                    .header("Content-Type", "application/json")
                    .build()
                chain.proceed(newRequest)
            }
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val groupApiService: GroupApiService by lazy {
        retrofit.create(GroupApiService::class.java)
    }

    val authApiService: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }

    val profilePrefApiService: ProfilePrefApiService by lazy {
        retrofit.create(ProfilePrefApiService::class.java)
    }

    val sessionApiService: SessionApiService by lazy {
        retrofit.create(SessionApiService::class.java)
    }

    val friendApiService: FriendApiService by lazy {
        retrofit.create(FriendApiService::class.java)
    }

    val postApiService: PostApiService by lazy {
        retrofit.create(PostApiService::class.java)
    }

}