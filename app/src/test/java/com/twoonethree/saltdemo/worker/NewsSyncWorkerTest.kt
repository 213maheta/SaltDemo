package com.twoonethree.saltdemo.worker

import android.content.Context
import androidx.work.ListenableWorker.Result
import androidx.work.WorkerParameters
import com.twoonethree.saltdemo.data.remote.network.NetworkError
import com.twoonethree.saltdemo.data.remote.network.NetworkResult
import com.twoonethree.saltdemo.data.repository.NewsRepository
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NewsSyncWorkerTest {

    private val context: Context = mockk(relaxed = true)
    private val workerParams: WorkerParameters = mockk(relaxed = true)
    private val newsRepository: NewsRepository = mockk()

    private lateinit var worker: NewsSyncWorker

    @Before
    fun setUp() {
        worker = spyk(
            NewsSyncWorker(
                context = context,
                workerParams = workerParams,
                newsRepository = newsRepository
            )
        )
        every { worker.showBreakingNewsNotification() } just Runs
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `doWork on repository success with new articles triggers notification and returns Result success`() = runTest {
        coEvery { newsRepository.fetchTopHeadlines(category = "general", page = 1) } returns NetworkResult.Success(5)

        val result = worker.doWork()

        assertTrue(result is Result.Success)
        assertEquals(Result.success(), result)
        coVerify(exactly = 1) { newsRepository.fetchTopHeadlines("general", 1) }
        verify(exactly = 1) { worker.showBreakingNewsNotification() }
    }

    @Test
    fun `doWork on repository success with zero articles returns Result success without notification`() = runTest {
        coEvery { newsRepository.fetchTopHeadlines(category = "general", page = 1) } returns NetworkResult.Success(0)

        val result = worker.doWork()

        assertTrue(result is Result.Success)
        assertEquals(Result.success(), result)
        coVerify(exactly = 1) { newsRepository.fetchTopHeadlines("general", 1) }
        verify(exactly = 0) { worker.showBreakingNewsNotification() }
    }

    @Test
    fun `doWork on network connection failure returns Result retry`() = runTest {
        coEvery { newsRepository.fetchTopHeadlines(category = "general", page = 1) } returns NetworkResult.Failure(
            NetworkError.NetworkConnection("No internet connection")
        )

        val result = worker.doWork()

        assertTrue(result is Result.Retry)
        assertEquals(Result.retry(), result)
        coVerify(exactly = 1) { newsRepository.fetchTopHeadlines("general", 1) }
        verify(exactly = 0) { worker.showBreakingNewsNotification() }
    }

    @Test
    fun `doWork on server error returns Result retry`() = runTest {
        coEvery { newsRepository.fetchTopHeadlines(category = "general", page = 1) } returns NetworkResult.Failure(
            NetworkError.ServerError(500, "Internal Server Error")
        )

        val result = worker.doWork()

        assertTrue(result is Result.Retry)
        assertEquals(Result.retry(), result)
        coVerify(exactly = 1) { newsRepository.fetchTopHeadlines("general", 1) }
        verify(exactly = 0) { worker.showBreakingNewsNotification() }
    }
}
