package com.soberg.example

import app.cash.turbine.turbineScope
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isZero
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ManagedResourcesFlowTest {

    @Test
    fun `do not call onInactive for first subscriber`() = runTest {
        var onInactiveCalls = 0
        val managedResourcesFlow = ManagedResourcesFlow(
            scope = TestScope(testScheduler),
            onActive = { },
            onInactive = { onInactiveCalls++ },
        )
        runCurrent()
        val flow = managedResourcesFlow().flatMapLatest {
            MutableSharedFlow<Int>()
        }

        turbineScope {
            assertThat(onInactiveCalls).isZero()

            val job = flow.testIn(this)
            runCurrent()
            assertThat(onInactiveCalls).isZero()
            job.cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `call onActive once for first subscriber`() = runTest {
        var activeCount = 0
        val managedResourcesFlow = ManagedResourcesFlow(
            scope = TestScope(testScheduler),
            onActive = { activeCount++ },
            onInactive = { activeCount-- },
        )
        runCurrent()
        val flow = managedResourcesFlow().flatMapLatest {
            MutableSharedFlow<Int>()
        }

        turbineScope {
            assertThat(activeCount).isZero()

            val job = flow.testIn(this)
            runCurrent()
            assertThat(activeCount).isEqualTo(1)
            job.cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `do not call onActive again for second subscriber`() = runTest {
        var activeCount = 0
        val managedResourcesFlow = ManagedResourcesFlow(
            scope = TestScope(testScheduler),
            onActive = { activeCount++ },
            onInactive = { activeCount-- },
        )
        runCurrent()
        val flow = managedResourcesFlow().flatMapLatest {
            MutableSharedFlow<Int>()
        }

        turbineScope {
            assertThat(activeCount).isZero()

            val first = flow.testIn(this)
            runCurrent()
            assertThat(activeCount).isEqualTo(1)

            val second = flow.testIn(this)
            runCurrent()
            assertThat(activeCount).isEqualTo(1)

            first.cancelAndIgnoreRemainingEvents()
            second.cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `call onInactive when all subscribers leave`() = runTest {
        var activeCount = 0
        val managedResourcesFlow = ManagedResourcesFlow(
            scope = TestScope(testScheduler),
            onActive = { activeCount++ },
            onInactive = { activeCount-- },
        )
        runCurrent()
        val flow = managedResourcesFlow().flatMapLatest {
            MutableSharedFlow<Int>()
        }

        turbineScope {
            assertThat(activeCount).isZero()

            val first = flow.testIn(this)
            val second = flow.testIn(this)
            runCurrent()
            assertThat(activeCount).isEqualTo(1)

            second.cancelAndIgnoreRemainingEvents()
            runCurrent()
            assertThat(activeCount).isEqualTo(1)

            first.cancelAndIgnoreRemainingEvents()
            runCurrent()
            assertThat(activeCount).isZero()
        }
    }
}