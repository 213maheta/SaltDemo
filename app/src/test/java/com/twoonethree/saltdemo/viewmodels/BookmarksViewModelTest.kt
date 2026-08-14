package com.twoonethree.saltdemo.viewmodels

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
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class BookmarksViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val newsRepository: NewsRepository = mockk(relaxed = true)
    private lateinit var viewModel: BookmarksViewModel

    private val mockArticle = Article(
        title = "Saved Kotlin Article",
        description = "Description",
        content = "Full content",
        category = "technology",
        url = "https://example.com/saved",
        urlToImage = null,
        publishedAt = "2026-08-14",
        sourceName = "Kotlin Blog",
        isBookmarked = true
    )

    @Test
    fun `bookmarkedArticles emits list from repository reactively`() = runTest {
        every { newsRepository.observeBookmarkedArticles() } returns flowOf(listOf(mockArticle))

        viewModel = BookmarksViewModel(newsRepository)

        viewModel.bookmarkedArticles.test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals("Saved Kotlin Article", list.first().title)
            assertTrue(list.first().isBookmarked)
        }
    }

    @Test
    fun `onBookmarkClick calls repository to unbookmark article`() = runTest {
        every { newsRepository.observeBookmarkedArticles() } returns flowOf(emptyList())

        viewModel = BookmarksViewModel(newsRepository)
        viewModel.onBookmarkClick(mockArticle)

        coVerify(exactly = 1) {
            newsRepository.toggleBookmark(mockArticle.url, false)
        }
    }
}