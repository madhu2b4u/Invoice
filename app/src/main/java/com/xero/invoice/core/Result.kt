package com.xero.invoice.core

sealed class Result<out T> {

    data class Success<out T>(val data: T) : Result<T>()

    data class Empty<out T>(val title: String, val message: String) : Result<T>()

    data class Error<out T>(val message: String, val data: T? = null) : Result<T>()

    data object Loading : Result<Nothing>()

    companion object {

        fun <T> success(data: T): Result<T> = Success(data)

        fun <T> empty(title: String, message: String): Result<T> = Empty(title, message)

        fun <T> error(msg: String, data: T? = null): Result<T> = Error(msg, data)

        fun <T> loading(): Result<T> = Loading
    }
}