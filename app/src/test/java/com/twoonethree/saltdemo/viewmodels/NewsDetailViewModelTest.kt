package com.twoonethree.saltdemo.viewmodels

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.twoonethree.saltdemo.model.Article
import com.twoonethree.saltdemo.repository.NewsRepository
import com.twoonethree.saltdemo.util.MainDispatcherRule
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

class NewsDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val newsRepository: NewsRepository = mockk(relaxed = true)

    private val testUrl = "https://example.com/article-detail"
    private val mockArticle = Article(
        title = "Detail Title",
        description = "Detail Description",
        content = "Detail content",
        category = "general",
        url = testUrl,
        urlToImage = null,
        publishedAt = "2026-08-14",
        sourceName = "BBC",
        isBookmarked = false
    )

    @Test
    fun `article state loads and observes article matching savedStateHandle url`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf("articleUrl" to testUrl))
        every { newsRepository.observeArticleByUrl(testUrl) } returns flowOf(mockArticle)

        val viewModel = NewsDetailViewModel(savedStateHandle, newsRepository)

        viewModel.article.test {
            val article = awaitItem()
            assertNotNull(article)
            assertEquals(testUrl, article?.url)
            assertEquals("Detail Title", article?.title)
        }
    }

    @Test
    fun `toggleBookmark inverts bookmark state for current article`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf("articleUrl" to testUrl))
        every { newsRepository.observeArticleByUrl(testUrl) } returns flowOf(mockArticle)

        val viewModel = NewsDetailViewModel(savedStateHandle, newsRepository)

        viewModel.article.test {
            awaitItem() // Wait for initial emission
            viewModel.toggleBookmark()
            coVerify(exactly = 1) { newsRepository.toggleBookmark(testUrl, true) }
        }
    }
}