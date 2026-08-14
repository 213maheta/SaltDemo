package com.twoonethree.saltdemo.network

import retrofit2.HttpException
import java.io.IOException

object ApiCaller {

    suspend fun <T> safeApiCall(apiCall: suspend () -> T): NetworkResult<T> {
        return try {
            val response = apiCall()
            NetworkResult.Success(response)
        } catch (e: HttpException) {
            // Server responded, but with an error status code (4xx/5xx)
            val errorBody = e.response()?.errorBody()?.string()
            NetworkResult.Failure(
                NetworkError.ApiError(
                    code = e.code(),
                    errorMessage = errorBody ?: e.message()
                )
            )
        } catch (e: IOException) {
            // No internet, timeout, DNS failure, etc.
            NetworkResult.Failure(
                NetworkError.NetworkError_(
                    errorMessage = "Network error. Please check your connection."
                )
            )
        } catch (e: Exception) {
            // Anything unexpected — including serialization/parsing failures
            NetworkResult.Failure(
                NetworkError.UnknownError(
                    errorMessage = e.localizedMessage ?: "Something went wrong."
                )
            )
        }
    }
}