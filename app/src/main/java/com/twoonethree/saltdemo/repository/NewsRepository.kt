package com.twoonethree.saltdemo.repository

import com.twoonethree.saltdemo.api.NewsApi
import com.twoonethree.saltdemo.model.Article
import com.twoonethree.saltdemo.model.toDomain
import com.twoonethree.saltdemo.model.toEntity
import com.twoonethree.saltdemo.network.ApiCaller
import com.twoonethree.saltdemo.network.NetworkResult
import com.twoonethree.saltdemo.room.ArticleDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NewsRepository(
    private val newsApi: NewsApi,
    private val articleDao: ArticleDao
) {

    // Returns the count of articles fetched in this page — 0 means no more data
    suspend fun fetchTopHeadlines(
        category: String,
        page: Int
    ): NetworkResult<Int> {
        val result = ApiCaller.safeApiCall {
            newsApi.getTopHeadlines(category = category, page = page)
        }

        return when (result) {
            is NetworkResult.Success -> {
                val entities = result.data.articles.map { it.toEntity(category) }
                articleDao.insertArticles(entities)
                NetworkResult.Success(entities.size)
            }
            is NetworkResult.Failure -> result
        }
    }

    fun observeArticlesByCategory(category: String): Flow<List<Article>> {
        return articleDao.getArticlesByCategory(category).map { list ->
            list.map { it.toDomain() }
        }
    }

    fun observeBookmarkedArticles(): Flow<List<Article>> {
        return articleDao.getBookmarkedArticles().map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun toggleBookmark(url: String, bookmarked: Boolean) {
        articleDao.setBookmarked(url, bookmarked)
    }

    fun observeArticleByUrl(url: String): Flow<Article?> {
        return articleDao.observeArticleByUrl(url).map { it?.toDomain() }
    }

    // NewsRepository.kt — add this method
    fun searchArticles(query: String): Flow<List<Article>> {
        return articleDao.searchArticles(query).map { list ->
            list.map { it.toDomain() }
        }
    }
}