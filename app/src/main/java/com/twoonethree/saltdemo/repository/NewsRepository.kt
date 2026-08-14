package com.twoonethree.saltdemo.repository

import com.twoonethree.saltdemo.api.NewsApi
import com.twoonethree.saltdemo.model.Article
import com.twoonethree.saltdemo.model.toDomain
import com.twoonethree.saltdemo.model.toEntity
import com.twoonethree.saltdemo.network.ApiCaller
import com.twoonethree.saltdemo.network.NetworkResult
import com.twoonethree.saltdemo.room.ArticleDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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

    // Remote Search: Fetches from API & checks against local DB to maintain bookmark flags
    // Remote Search in NewsRepository.kt
    suspend fun searchArticlesRemote(query: String, page: Int = 1): NetworkResult<List<Article>> {
        val result = ApiCaller.safeApiCall {
            newsApi.searchArticles(query = query, page = page)
        }

        return when (result) {
            is NetworkResult.Success -> {
                val bookmarkedUrls = articleDao.getBookmarkedArticles().first().map { it.url }.toSet()

                // Convert DTO -> Entity -> Domain
                val domainArticles = result.data.articles.mapNotNull { dto ->
                    dto.toEntity("search").toDomain()?.copy(
                        isBookmarked = bookmarkedUrls.contains(dto.url)
                    )
                }
                NetworkResult.Success(domainArticles)
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

    // Local offline search
    fun searchArticlesLocal(query: String): Flow<List<Article>> {
        return articleDao.searchArticles(query).map { list ->
            list.map { it.toDomain() }
        }
    }
}