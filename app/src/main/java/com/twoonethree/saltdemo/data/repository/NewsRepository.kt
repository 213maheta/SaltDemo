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

    suspend fun fetchTopHeadlines(category: String, page: Int): NetworkResult<Int> {
        val result = ApiCaller.safeApiCall {
            newsApi.getTopHeadlines(category = category, page = page)
        }

        return when (result) {
            is NetworkResult.Success -> {
                // 1. Fetch currently bookmarked article URLs from Room
                val bookmarkedUrls = articleDao.getBookmarkedArticles()
                    .first()
                    .map { it.url }
                    .toSet()

                // 2. Map DTOs to Entities, preserving bookmark state
                val entities = result.data.articles.map { dto ->
                    dto.toEntity(category).copy(
                        isBookmarked = bookmarkedUrls.contains(dto.url)
                    )
                }

                // 3. If refreshing page 1, clear unbookmarked cached articles first
                if (page == 1) {
                    articleDao.clearCategoryCache(category)
                }

                // 4. Save fresh batch into Room
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
                val bookmarkedUrls = articleDao.getBookmarkedArticles()
                    .first()
                    .map { it.url }
                    .toSet()

                val entities = result.data.articles.map { dto ->
                    dto.toEntity("search").copy(
                        isBookmarked = bookmarkedUrls.contains(dto.url)
                    )
                }

                // Persist search articles so detail screen can load them immediately
                articleDao.insertArticles(entities)

                val domainArticles = entities.map { it.toDomain() }
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

    suspend fun toggleBookmark(url: String, isBookmarked: Boolean) {
        articleDao.setBookmarked(url, isBookmarked)
    }

    fun observeArticleByUrl(url: String): Flow<Article?> {
        return articleDao.observeArticleByUrl(url).map { it?.toDomain() }
    }
}