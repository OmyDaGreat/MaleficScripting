package xyz.malefic.scripting

import xyz.malefic.scripting.core.Component
import xyz.malefic.scripting.core.KInquirerEvent

/**
 * Extension function for `Component` to handle a sequence of events.
 *
 * @param T The type of the value held by the component.
 * @param func A lambda function that adds events to a mutable list.
 */
internal fun <T> Component<T>.onEventSequence(func: MutableList<KInquirerEvent>.() -> Unit) {
    val events = mutableListOf<KInquirerEvent>()
    events.func()
    events.forEach { event -> onEvent(event) }
}
