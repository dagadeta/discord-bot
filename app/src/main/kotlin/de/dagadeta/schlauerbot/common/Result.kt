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

    fun failureTypeOrNull(): FailureType? =
        when (value) {
            is Failure -> value.type
            else -> null
        }

    companion object {
        fun <T> success(value: T): Result<T> = Result(value)
        fun <T> failure(message: String, type: FailureType = FailureType.Unspectacular): Result<T> =
            Result(createFailure(message, type))

    }

    @PublishedApi
    internal class Failure(
        @PublishedApi
        @JvmField
        internal val message: String,
        @PublishedApi
        @JvmField
        internal val type: FailureType
    )
}

internal fun createFailure(message: String, type: FailureType): Any =
    Result.Failure(message, type)

inline fun <R, T : R> Result<T>.getOrElse(onFailure: (message: String, type: FailureType) -> R): R {
    return when (value) {
        is Result.Failure -> onFailure(value.message, value.type)
        else -> value as T
    }
}

inline fun <T> Result<T>.onFailure(action: (message: String, type: FailureType) -> Unit): Result<T> {
    if (value is Result.Failure) {
        action(value.message, value.type)
    }
    return this
}

inline fun <T> Result<T>.onSuccess(action: (value: T) -> Unit): Result<T> {
    if (isSuccess) action(value as T)
    return this
}

enum class FailureType {
    Unspectacular, Critical
}
