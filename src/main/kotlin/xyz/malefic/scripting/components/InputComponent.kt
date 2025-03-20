package xyz.malefic.scripting.components

import org.fusesource.jansi.Ansi.ansi
import xyz.malefic.scripting.KInquirer
import xyz.malefic.scripting.core.Component
import xyz.malefic.scripting.core.KInquirerEvent
import xyz.malefic.scripting.core.KInquirerEvent.CharInput
import xyz.malefic.scripting.core.KInquirerEvent.KeyPressBackspace
import xyz.malefic.scripting.core.KInquirerEvent.KeyPressEnter
import xyz.malefic.scripting.core.KInquirerEvent.KeyPressSpace
import xyz.malefic.scripting.core.toAnsi
import java.math.BigDecimal

/**
 * InputComponent is a component for handling user input in the console.
 *
 * @property message The message to display to the user.
 * @property default The default value if no input is provided.
 * @property hint A hint to display when the input is empty.
 * @property validation A function to validate the input.
 * @property filter A function to filter the input.
 * @property transform A function to transform the input before displaying.
 */
internal class InputComponent(
    val message: String,
    val default: String = "",
    val hint: String = "",
    val validation: (s: String) -> Boolean = { true },
    val filter: (s: String) -> Boolean = { true },
    val transform: (s: String) -> String = { s -> s },
) : Component<String> {
    private var value: String? = null
    private var interacting = true
    private var errorMessage = ""

    /**
     * Retrieves the current value of the input.
     *
     * @return The current value or the default value if none is provided.
     */
    override fun value(): String = value ?: default

    /**
     * Checks if the component is currently interacting.
     *
     * @return True if the component is interacting, false otherwise.
     */
    override fun isInteracting(): Boolean = interacting

    /**
     * Handles an event for the input component.
     *
     * @param event The event to be handled.
     */
    override fun onEvent(event: KInquirerEvent) {
        errorMessage = ""
        when (event) {
            is KeyPressEnter -> {
                if (validation(value())) {
                    interacting = false
                } else {
                    errorMessage = "invalid input"
                }
            }
            is KeyPressBackspace -> {
                value = value?.dropLast(1)
            }
            is KeyPressSpace -> {
                if (filter(" ")) {
                    value = value?.plus(" ") ?: " "
                }
            }
            is CharInput -> {
                val tempVal = value.orEmpty() + event.c
                if (filter(tempVal)) {
                    value = tempVal
                }
            }
            else -> {
                // No action needed for other events
            }
        }
    }

    /**
     * Renders the input component as a string.
     *
     * @return The rendered string representation of the input component.
     */
    override fun render(): String =
        buildString {
            // Question mark character
            append(
                "?".toAnsi {
                    fgGreen()
                    bold()
                },
            )
            append(" ")

            // Message
            append(message.toAnsi { bold() })
            append(" ")

            when {
                interacting && value.isNullOrEmpty() && hint.isNotBlank() -> {
                    // Hint
                    append("  ")
                    append(hint.toAnsi { fgBrightBlack() })
                    append(ansi().cursorLeft(hint.length + 2))
                }
                interacting -> {
                    // User Input
                    append(transform(value()))
                    // Error message
                    if (errorMessage.isNotBlank()) {
                        append("  ")
                        append(
                            errorMessage.toAnsi {
                                bold()
                                fgRed()
                            },
                        )
                        append(ansi().cursorLeft(errorMessage.length + 2))
                    }
                }
                else -> {
                    // User Input with new line
                    appendLine(
                        transform(value()).toAnsi {
                            fgCyan()
                            bold()
                        },
                    )
                }
            }
        }
}

/**
 * Prompts the user for input.
 *
 * @param message The message to display to the user.
 * @param default The default value if no input is provided.
 * @param hint A hint to display when the input is empty.
 * @param validation A function to validate the input.
 * @param filter A function to filter the input.
 * @param transform A function to transform the input before displaying.
 * @return The user's input.
 */
fun KInquirer.promptInput(
    message: String,
    default: String = "",
    hint: String = "",
    validation: (s: String) -> Boolean = { true },
    filter: (s: String) -> Boolean = { true },
    transform: (s: String) -> String = { it },
): String = prompt(InputComponent(message, default, hint, validation, filter, transform))

/**
 * Prompts the user for a password input.
 *
 * @param message The message to display to the user.
 * @param default The default value if no input is provided.
 * @param hint A hint to display when the input is empty.
 * @param mask The character to mask the input.
 * @return The user's input.
 */
fun KInquirer.promptInputPassword(
    message: String,
    default: String = "",
    hint: String = "",
    mask: String = "*",
): String {
    val validation: (s: String) -> Boolean = { true }
    val filter: (s: String) -> Boolean = { true }
    val transform: (s: String) -> String = { it.map { mask }.joinToString("") }

    return prompt(InputComponent(message, default, hint, validation, filter, transform))
}

/**
 * Prompts the user for a numeric input.
 *
 * @param message The message to display to the user.
 * @param default The default value if no input is provided.
 * @param hint A hint to display when the input is empty.
 * @param transform A function to transform the input before displaying.
 * @return The user's input as a BigDecimal.
 */
fun KInquirer.promptInputNumber(
    message: String,
    default: String = "",
    hint: String = "",
    transform: (s: String) -> String = { it },
): BigDecimal {
    val validation: (s: String) -> Boolean = { it.matches("\\d+.?\\d*".toRegex()) }
    val filter: (s: String) -> Boolean = { it.matches("\\d*\\.?\\d*".toRegex()) }

    return BigDecimal(prompt(InputComponent(message, default, hint, validation, filter, transform)))
}
