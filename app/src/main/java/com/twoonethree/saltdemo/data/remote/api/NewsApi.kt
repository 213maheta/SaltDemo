package com.twoonethree.saltdemo.data.remote.api

import com.twoonethree.saltdemo.data.remote.dto.NewsResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApi {
    @GET("top-headlines")
    suspend fun getTopHeadlines(
        @Query("country") country: String = "us",
        @Query("category") category: String? = null,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int = 20
    ): NewsResponseDto

    @GET("everything")
    suspend fun searchArticles(
        @Query("q") query: String,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
        @Query("sortBy") sortBy: String = "publishedAt"
    ): NewsResponseDto
}
