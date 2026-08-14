package com.twoonethree.saltdemo.viewmodel

import app.cash.turbine.test
import com.twoonethree.saltdemo.data.local.db.NewsCategory
import com.twoonethree.saltdemo.data.remote.network.NetworkResult
import com.twoonethree.saltdemo.data.repository.NewsRepository
import com.twoonethree.saltdemo.domain.model.Article
import com.twoonethree.saltdemo.util.MainDispatcherRule
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NewsListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val newsRepository: NewsRepository = mockk(relaxed = true)
    private lateinit var viewModel: NewsListViewModel

    private val mockArticle = Article(
        title = "Test Headline",
        description = "Test Description",
        content = "Content",
        category = "general",
        url = "https://example.com/test",
        urlToImage = null,
        publishedAt = "2026-08-14",
        sourceName = "Test Source",
        isBookmarked = false
    )

    @Before
    fun setUp() {
        every { newsRepository.observeArticlesByCategory(any()) } returns flowOf(listOf(mockArticle))
        coEvery { newsRepository.fetchTopHeadlines(any(), any()) } returns NetworkResult.Success(1)
    }

    @Test
    fun `initialization loads headlines and observes articles`() = runTest {
        viewModel = NewsListViewModel(newsRepository)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(1, state.articles.size)
            assertEquals("Test Headline", state.articles.first().title)
            assertFalse(state.isLoading)
        }

        coVerify(exactly = 1) { newsRepository.fetchTopHeadlines("general", 1) }
    }

    @Test
    fun `search input triggers debounced remote search`() = runTest {
        val searchResults = listOf(mockArticle.copy(title = "Search Hit"))
        coEvery { newsRepository.searchArticlesRemote("compose") } returns NetworkResult.Success(searchResults)

        viewModel = NewsListViewModel(newsRepository)
        viewModel.onSearchQueryChanged("compose")

        advanceTimeBy(600)

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.isSearchActive)
            assertEquals(1, state.searchResults.size)
            assertEquals("Search Hit", state.searchResults.first().title)
        }

        coVerify(exactly = 1) { newsRepository.searchArticlesRemote("compose") }
    }
}
