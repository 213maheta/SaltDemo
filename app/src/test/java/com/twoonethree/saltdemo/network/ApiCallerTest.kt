package com.twoonethree.saltdemo.network

import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ApiCallerTest {

    @Test
    fun `safeApiCall returns Success when call completes normally`() = runTest {
        val result = ApiCaller.safeApiCall { "Success Data" }

        assertTrue(result is NetworkResult.Success)
        assertEquals("Success Data", (result as NetworkResult.Success).data)
    }

    @Test
    fun `safeApiCall returns Unauthorized for HTTP 401`() = runTest {
        val errorResponse = Response.error<String>(
            401,
            "{\"message\":\"Invalid API Key\"}".toResponseBody(null)
        )
        val httpException = HttpException(errorResponse)

        val result = ApiCaller.safeApiCall<String> { throw httpException }

        assertTrue(result is NetworkResult.Failure)
        val failure = result as NetworkResult.Failure
        assertTrue(failure.error is NetworkError.Unauthorized)
    }

    @Test
    fun `safeApiCall returns RateLimited for HTTP 429`() = runTest {
        val errorResponse = Response.error<String>(
            429,
            "{\"message\":\"Too many requests\"}".toResponseBody(null)
        )
        val httpException = HttpException(errorResponse)

        val result = ApiCaller.safeApiCall<String> { throw httpException }

        assertTrue(result is NetworkResult.Failure)
        val failure = result as NetworkResult.Failure
        assertTrue(failure.error is NetworkError.RateLimited)
    }

    @Test
    fun `safeApiCall returns NetworkConnection for UnknownHostException or Timeout`() = runTest {
        val dnsResult = ApiCaller.safeApiCall<String> { throw UnknownHostException() }
        val timeoutResult = ApiCaller.safeApiCall<String> { throw SocketTimeoutException() }
        val ioResult = ApiCaller.safeApiCall<String> { throw IOException("Connection lost") }

        assertTrue(dnsResult is NetworkResult.Failure && dnsResult.error is NetworkError.NetworkConnection)
        assertTrue(timeoutResult is NetworkResult.Failure && timeoutResult.error is NetworkError.NetworkConnection)
        assertTrue(ioResult is NetworkResult.Failure && ioResult.error is NetworkError.NetworkConnection)
    }
}