@file:Suppress("UNCHECKED_CAST")

package de.dagadeta.schlauerbot.common

import de.dagadeta.schlauerbot.common.FailureType.Unspectacular
import de.dagadeta.schlauerbot.common.Result.Failure


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

    fun failureOrNull(): Pair<String, FailureType>? =
        when (value) {
            is Failure -> value.message to value.type
            else -> null
        }

    companion object {
        fun <T> success(value: T): Result<T> = Result(value)
        fun <T> failure(message: String, type: FailureType = Unspectacular): Result<T> =
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

internal fun createFailure(message: String, type: FailureType): Any = Failure(message, type)

inline fun <R, T : R> Result<T>.getOrElse(onFailure: (message: String, type: FailureType) -> R): R {
    return when (value) {
        is Failure -> onFailure(value.message, value.type)
        else -> value as T
    }
}

inline fun <T> Result<T>.onFailure(action: (message: String, type: FailureType) -> Unit): Result<T> {
    if (value is Failure) {
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
