package com.twoonethree.saltdemo.network

sealed interface NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>
    data class Failure(val error: NetworkError) : NetworkResult<Nothing> {
        val message: String get() = error.userMessage
    }
}

sealed class NetworkError(val userMessage: String) {
    data class Unauthorized(val msg: String = "Invalid API Key or unauthorized request.") : NetworkError(msg)
    data class RateLimited(val msg: String = "API rate limit exceeded. Please try again later.") : NetworkError(msg)
    data class ServerError(val code: Int, val msg: String = "Server error ($code). Please try again later.") : NetworkError(msg)
    data class NetworkConnection(val msg: String = "No internet connection. Please check your network.") : NetworkError(msg)
    data class Unknown(val msg: String = "An unexpected error occurred.") : NetworkError(msg)
}