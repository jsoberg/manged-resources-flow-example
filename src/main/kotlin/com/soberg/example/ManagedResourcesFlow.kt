package com.soberg.example

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart

/** @return A Flow that can be used to reliably manage the resources controlled by [onActive] and [onInactive].
 *  When subscribers are present, [onActive] will be called to initialize any managed resources.
 *  When subscribers are no longer present, [onInactive] will be called to cleanup any managed resources. */
class ManagedResourcesFlow(
    /** [CoroutineScope] that will be used to observe resource management changes. */
    scope: CoroutineScope,
    /** Callback to begin managing resources when subscribers are present. */
    private val onActive: suspend () -> Unit,
    /** Callback to cleanup managed resources when subscribers are no longer present. */
    private val onInactive: suspend () -> Unit,
) {
    private val internalMonitoringFlow = MutableSharedFlow<Unit>()

    init {
        internalMonitoringFlow.subscriptionCount
            .map { count -> count > 0 }
            .distinctUntilChanged()
            // Drop the first inactive emission - we can assume resources are inactive by default,
            // and therefore calling onInactive() is unnecessary.
            .drop(1)
            .onEach { isActive ->
                if (isActive) onActive() else onInactive()
            }
            .launchIn(scope)
    }

    /** @return A Flow that will control reliable management of resources defined in the constructor.
     * This Flow will **always** emit once, and only once. */
    operator fun invoke(): Flow<Unit> = internalMonitoringFlow
        // The output of this Flow is not important - we just need to make sure that it emits once
        // (and only once) so that downstream subscribers will be able to flatMap to what they need.
        .onStart { emit(Unit) }
        .distinctUntilChanged()
}