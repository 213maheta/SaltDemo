package com.twoonethree.saltdemo.repository

import com.twoonethree.saltdemo.api.NewsApi
import com.twoonethree.saltdemo.model.ArticleDto
import com.twoonethree.saltdemo.model.NewsResponseDto
import com.twoonethree.saltdemo.model.SourceDto
import com.twoonethree.saltdemo.network.NetworkResult
import com.twoonethree.saltdemo.room.ArticleDao
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.IOException

class NewsRepositoryTest {

    private val newsApi: NewsApi = mockk()
    private val articleDao: ArticleDao = mockk(relaxed = true)
    private lateinit var repository: NewsRepository

    private val mockArticleDto = ArticleDto(
        source = SourceDto(id = "1", name = "Source"),
        author = "Author",
        title = "Title",
        description = "Description",
        url = "https://example.com",
        urlToImage = null,
        publishedAt = "2026-08-14T00:00:00Z",
        content = "Content"
    )

    private val mockResponse = NewsResponseDto(
        status = "ok",
        totalResults = 1,
        articles = listOf(mockArticleDto)
    )

    @Before
    fun setUp() {
        repository = NewsRepository(newsApi, articleDao)
    }

    @Test
    fun `fetchTopHeadlines saves to Room on success and returns count`() = runTest {
        coEvery { newsApi.getTopHeadlines(any(), any(), any(), any()) } returns mockResponse

        val result = repository.fetchTopHeadlines("general", 1)

        assertTrue(result is NetworkResult.Success)
        assertEquals(1, (result as NetworkResult.Success).data)
        coVerify(exactly = 1) { articleDao.insertArticles(any()) }
    }

    @Test
    fun `fetchTopHeadlines returns Failure on network exception`() = runTest {
        coEvery { newsApi.getTopHeadlines(any(), any(), any(), any()) } throws IOException("Timeout")

        val result = repository.fetchTopHeadlines("general", 1)

        assertTrue(result is NetworkResult.Failure)
    }
}