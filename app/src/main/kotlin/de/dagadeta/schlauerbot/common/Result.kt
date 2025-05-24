@file:Suppress("UNCHECKED_CAST")

package de.dagadeta.schlauerbot.common


@JvmInline
value class Result<out T> internal constructor(
    @PublishedApi
    internal val value: Any?
) {
    val isSuccess: Boolean get() = value !is Failure
    val isFailure: Boolean get() = value is Failure

    fun getOrNull(): T? =
        when {
            isFailure -> null
            else -> value as T
        }

    fun failureOrNull(): String? =
        when (value) {
            is Failure -> value.message
            else -> null
        }

    companion object {
        fun <T> success(value: T): Result<T> = Result(value)
        fun <T> failure(message: String): Result<T> =
            Result(createFailure(message))

    }

    internal class Failure(
        @JvmField
        val message: String
    )
}

internal fun createFailure(message: String): Any =
    Result.Failure(message)

inline fun <R, T : R> Result<T>.getOrElse(onFailure: (message: String) -> R): R {
    return when (val message = failureOrNull()) {
        null -> value as T
        else -> onFailure(message)
    }
}

inline fun <T> Result<T>.onFailure(action: (message: String) -> Unit): Result<T> {
    failureOrNull()?.let { action(it) }
    return this
}

inline fun <T> Result<T>.onSuccess(action: (value: T) -> Unit): Result<T> {
    if (isSuccess) action(value as T)
    return this
}
