package com.mealmatch.data.network.service

import com.mealmatch.data.model.Post
import com.mealmatch.data.model.ApiResponse
import retrofit2.Response
import retrofit2.http.*

interface PostApiService {

    @GET("api/posts")
    suspend fun getPosts(@Header("Authorization") token: String): Response<ApiResponse<List<Post>>>

    @POST("api/posts")
    suspend fun createPost(@Header("Authorization") token: String, @Body post: Post): Response<ApiResponse<Post>>

    @DELETE("api/posts/{id}")
    suspend fun deletePost(@Header("Authorization") token: String, @Path("id") postId: String): Response<ApiResponse<Unit>>

    @PUT("api/posts/{id}")
    suspend fun updatePost(@Header("Authorization") token: String, @Path("id") postId: String, @Body updatedPost: Post): Response<ApiResponse<Post>>

}
