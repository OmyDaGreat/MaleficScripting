package xyz.malefic.scripting.core

/**
 * A generic interface representing a component in the scripting core.
 *
 * @param T The type of the value held by the component.
 */
interface Component<T> {
    /**
     * Retrieves the value held by the component.
     *
     * @return The value of type T.
     */
    fun value(): T

    /**
     * Checks if the component is currently interacting.
     *
     * @return True if the component is interacting, false otherwise.
     */
    fun isInteracting(): Boolean

    /**
     * Handles an event for the component.
     *
     * @param event The event to be handled.
     */
    fun onEvent(event: KInquirerEvent)

    /**
     * Renders the component as a string.
     *
     * @return The rendered string representation of the component.
     */
    fun render(): String
}
