package xyz.malefic.scripting.components

import xyz.malefic.scripting.KInquirer
import xyz.malefic.scripting.core.Component
import xyz.malefic.scripting.core.KInquirerEvent
import xyz.malefic.scripting.core.toAnsi

/**
 * ConfirmComponent is a component for handling user confirmation input in the console.
 *
 * @property message The message to display to the user.
 * @property default The default confirmation value.
 */
internal class ConfirmComponent(
    private val message: String,
    default: Boolean = false,
) : Component<Boolean> {
    private var confirmed = default
    private var interacting = true

    /**
     * Retrieves the current confirmation value.
     *
     * @return The current confirmation value.
     */
    override fun value(): Boolean = confirmed

    /**
     * Checks if the component is currently interacting.
     *
     * @return True if the component is interacting, false otherwise.
     */
    override fun isInteracting(): Boolean = interacting

    /**
     * Handles an event for the confirm component.
     *
     * @param event The event to be handled.
     */
    override fun onEvent(event: KInquirerEvent) {
        when (event) {
            is KInquirerEvent.KeyPressLeft -> confirmed = true
            is KInquirerEvent.KeyPressRight -> confirmed = false
            is KInquirerEvent.KeyPressEnter -> interacting = false
            is KInquirerEvent.CharInput -> {
                when (event.c) {
                    'y', 'Y' -> confirmed = true
                    'n', 'N' -> confirmed = false
                }
            }
            else -> confirmed = false
        }
    }

    /**
     * Renders the confirm component as a string.
     *
     * @return The rendered string representation of the confirm component.
     */
    override fun render(): String =
        buildString {
            append(
                "?".toAnsi {
                    fgGreen()
                    bold()
                },
            )
            append(" ")
            append(message.toAnsi { bold() })
            append(" ")
            when {
                interacting && confirmed -> append("[Yes] No ")
                interacting && !confirmed -> append(" Yes [No]")
                !interacting && confirmed ->
                    appendLine(
                        "Yes".toAnsi {
                            fgCyan()
                            bold()
                        },
                    )
                else ->
                    appendLine(
                        "No".toAnsi {
                            fgCyan()
                            bold()
                        },
                    )
            }
        }
}

/**
 * Prompts the user for a confirmation input.
 *
 * @param message The message to display to the user.
 * @param default The default confirmation value.
 * @return The user's confirmation input.
 */
fun KInquirer.promptConfirm(
    message: String,
    default: Boolean = false,
): Boolean = prompt(ConfirmComponent(message, default))
