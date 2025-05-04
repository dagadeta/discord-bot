package de.dagadeta.schlauerbot.result

import de.dagadeta.schlauerbot.Result.Companion.failure
import de.dagadeta.schlauerbot.Result.Companion.success
import de.dagadeta.schlauerbot.getOrElse
import de.dagadeta.schlauerbot.onFailure
import de.dagadeta.schlauerbot.onSuccess
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ResultTest {

    private val success = success("Yeah!")
    private val failure = failure<Unit>("Oh no!")

    @Test
    fun `on success isSuccess is true` () {
        assertThat(success.isSuccess).isTrue
        assertThat(success.isFailure).isFalse
    }

    @Test
    fun `on failure isSuccess is true` () {
        assertThat(failure.isSuccess).isFalse
        assertThat(failure.isFailure).isTrue
    }

    @Test
    fun `getOrNull returns the success value, otherwise null`() {
        assertThat(success.getOrNull()).isEqualTo("Yeah!")
        assertThat(failure.getOrNull()).isNull()
    }

    @Test
    fun `failureOrNull returns the failure message, otherwise null`() {
        assertThat(success.failureOrNull()).isNull()
        assertThat(failure.failureOrNull()).isEqualTo("Oh no!")
    }

    @Test
    fun `getOrElse, on success returns the value and doesn't run the provided function`() {
        var wasCalled = false
        val value = success.getOrElse {
            wasCalled = true
        }

        assertThat(wasCalled).isFalse
        assertThat(value).isEqualTo("Yeah!")
    }

    @Test
    fun `getOrElse, on failure runs the provided function`() {
        var wasCalled = false
        val value = failure.getOrElse {
            wasCalled = true
            "this shall be returned"
        }

        assertThat(wasCalled).isTrue
        assertThat(value).isEqualTo("this shall be returned")
    }

    @Test
    fun `onFailure, on success returns the itself and doesn't run the provided function`() {
        var wasCalled = false
        val value = success.onFailure {
            wasCalled = true
        }

        assertThat(wasCalled).isFalse
        assertThat(value).isEqualTo(success)
    }

    @Test
    fun `onFailure, on failure runs the provided function`() {
        var wasCalled = false
        val value = failure.onFailure {
            wasCalled = true
            "this shall be returned"
        }

        assertThat(wasCalled).isTrue
        assertThat(value).isEqualTo(failure)
    }

    @Test
    fun `onSuccess, on success runs the provided function`() {
        var wasCalled = false
        val value = success.onSuccess {
            wasCalled = true
        }

        assertThat(wasCalled).isTrue
        assertThat(value).isEqualTo(success)
    }

    @Test
    fun `onSuccess, on failure returns the itself and doesn't run the provided function`() {
        var wasCalled = false
        val value = failure.onSuccess {
            wasCalled = true
            "this shall be returned"
        }

        assertThat(wasCalled).isFalse
        assertThat(value).isEqualTo(failure)
    }
}