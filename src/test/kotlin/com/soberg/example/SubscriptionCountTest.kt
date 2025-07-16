package com.soberg.example

import app.cash.turbine.test
import app.cash.turbine.turbineScope
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isZero
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class SubscriptionCountTest {

    @Test
    fun `example of subscriptionCount emissions`() = runTest {
        val sharedFlow = MutableSharedFlow<Unit>()
        sharedFlow.subscriptionCount.test {
            // Initially, we don't have any subscribers to sharedFlow - count will be 0.
            assertThat(awaitItem()).isZero()

            // Making a subscription to sharedFlow - count will not be 1.
            val firstSubscriber = sharedFlow.testIn(this)
            runCurrent()
            assertThat(awaitItem()).isEqualTo(1)

            // Making another subscription to sharedFlow - count will now be 2.
            val secondSubscriber = sharedFlow.testIn(this)
            runCurrent()
            assertThat(awaitItem()).isEqualTo(2)

            // Cancel one of the subscriptions - count will decrement, now 1.
            firstSubscriber.cancelAndIgnoreRemainingEvents()
            runCurrent()
            assertThat(awaitItem()).isEqualTo(1)

            // Cancel the last subscription - count will decrement and be 0 again.
            secondSubscriber.cancelAndIgnoreRemainingEvents()
            runCurrent()
            assertThat(awaitItem()).isZero()
        }
    }
}