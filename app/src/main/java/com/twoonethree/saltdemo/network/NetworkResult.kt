package com.twoonethree.saltdemo.network

sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Failure(val error: NetworkError) : NetworkResult<Nothing>()
}

sealed class NetworkError(val message: String) {
    data class ApiError(val code: Int, val errorMessage: String) : NetworkError(errorMessage)
    data class NetworkError_(val errorMessage: String) : NetworkError(errorMessage)
    data class UnknownError(val errorMessage: String) : NetworkError(errorMessage)
}