package com.twoonethree.saltdemo.api

import com.twoonethree.saltdemo.model.NewsResponseDto
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
}