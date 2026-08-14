package com.twoonethree.saltdemo.data.remote.network

import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object ApiCaller {

    suspend fun <T> safeApiCall(apiCall: suspend () -> T): NetworkResult<T> {
        return try {
            val response = apiCall()
            NetworkResult.Success(response)
        } catch (e: HttpException) {
            val code = e.code()
            val parsedMessage = extractErrorMessage(e)

            val error = when (code) {
                401 -> NetworkError.Unauthorized(parsedMessage ?: "Unauthorized access. Check API key.")
                429 -> NetworkError.RateLimited(parsedMessage ?: "Rate limit reached. Try again later.")
                in 500..599 -> NetworkError.ServerError(code, "Server is currently unavailable.")
                else -> NetworkError.Unknown(parsedMessage ?: "Error $code: ${e.message()}")
            }
            NetworkResult.Failure(error)
        } catch (e: UnknownHostException) {
            NetworkResult.Failure(NetworkError.NetworkConnection("Unable to connect to server. Check your connection."))
        } catch (e: SocketTimeoutException) {
            NetworkResult.Failure(NetworkError.NetworkConnection("Connection timed out. Please try again."))
        } catch (e: IOException) {
            NetworkResult.Failure(NetworkError.NetworkConnection("Network error occurred."))
        } catch (e: Exception) {
            NetworkResult.Failure(NetworkError.Unknown(e.localizedMessage ?: "An unexpected error occurred."))
        }
    }

    private fun extractErrorMessage(e: HttpException): String? {
        return try {
            val errorJson = e.response()?.errorBody()?.string() ?: return null
            val jsonObject = JSONObject(errorJson)
            jsonObject.optString("message").takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        }
    }
}
