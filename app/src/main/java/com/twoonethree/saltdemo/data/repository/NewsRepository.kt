package com.twoonethree.saltdemo.data.repository

import com.twoonethree.saltdemo.data.local.db.ArticleDao
import com.twoonethree.saltdemo.data.mapper.toDomain
import com.twoonethree.saltdemo.data.mapper.toEntity
import com.twoonethree.saltdemo.data.remote.api.NewsApi
import com.twoonethree.saltdemo.data.remote.network.ApiCaller
import com.twoonethree.saltdemo.data.remote.network.NetworkResult
import com.twoonethree.saltdemo.domain.model.Article
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class NewsRepository(
    private val newsApi: NewsApi,
    private val articleDao: ArticleDao
) {

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

    suspend fun searchArticlesRemote(query: String, page: Int = 1): NetworkResult<List<Article>> {
        val result = ApiCaller.safeApiCall {
            newsApi.searchArticles(query = query, page = page)
        }
        return when (result) {
            is NetworkResult.Success -> {
                val entities = result.data.articles.map { it.toEntity("search") }

                // Insert search results into Room so observeArticleByUrl finds them!
                articleDao.insertArticles(entities)

                val bookmarkedUrls = articleDao.getBookmarkedArticles().first().map { it.url }.toSet()
                val domainArticles = entities.map { entity ->
                    entity.toDomain().copy(
                        isBookmarked = bookmarkedUrls.contains(entity.url)
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

    fun searchArticlesLocal(query: String): Flow<List<Article>> {
        return articleDao.searchArticles(query).map { list ->
            list.map { it.toDomain() }
        }
    }
}
