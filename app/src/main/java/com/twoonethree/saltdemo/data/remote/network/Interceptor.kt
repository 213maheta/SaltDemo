package com.twoonethree.saltdemo.data.remote.network

import com.twoonethree.saltdemo.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException

object LoggingInterceptorProvider {
    fun create(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }
}

class ApiKeyInterceptor(private val apiKey: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val originalUrl = originalRequest.url

        val newUrl = originalUrl.newBuilder()
            .addQueryParameter("apiKey", apiKey)
            .build()

        val newRequest = originalRequest.newBuilder()
            .url(newUrl)
            .build()

        return chain.proceed(newRequest)
    }
}

class RetryInterceptor(
    private val maxRetries: Int = 2,
    private val initialDelayMs: Long = 1000L
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response: Response? = null
        var exception: IOException? = null
        var tryCount = 0

        while (tryCount <= maxRetries) {
            try {
                response?.close() // Close previous failed response
                response = chain.proceed(request)

                // If successful or client error (4xx), do not retry
                if (response.isSuccessful || (response.code in 400..499 && response.code != 408)) {
                    return response
                }
            } catch (e: IOException) {
                exception = e
            }

            tryCount++
            if (tryCount <= maxRetries) {
                try {
                    // Exponential backoff
                    Thread.sleep(initialDelayMs * (1L shl (tryCount - 1)))
                } catch (_: InterruptedException) {
                    Thread.currentThread().interrupt()
                    break
                }
            }
        }

        return response ?: throw (exception ?: IOException("Failed after $maxRetries retries"))
    }
}
