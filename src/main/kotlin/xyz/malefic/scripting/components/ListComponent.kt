package xyz.malefic.scripting.components

import xyz.malefic.scripting.KInquirer
import xyz.malefic.scripting.core.Choice
import xyz.malefic.scripting.core.Component
import xyz.malefic.scripting.core.KInquirerEvent
import xyz.malefic.scripting.core.KInquirerEvent.KeyPressDown
import xyz.malefic.scripting.core.KInquirerEvent.KeyPressEnter
import xyz.malefic.scripting.core.KInquirerEvent.KeyPressUp
import xyz.malefic.scripting.core.toAnsi
import xyz.malefic.scripting.core.toAnsiStr
import kotlin.collections.forEachIndexed
import kotlin.math.max
import kotlin.math.min

/**
 * ListComponent is a component for displaying a list of choices and handling user selection.
 *
 * @param T The type of the data associated with each choice.
 * @property message The message to display to the user.
 * @property hint A hint to display when the list is shown.
 * @property choices The list of choices to display.
 * @property pageSize The number of choices to display per page.
 * @property viewOptions The options for customizing the view.
 */
internal class ListComponent<T>(
    private val message: String,
    private val hint: String,
    private val choices: List<Choice<T>>,
    private val pageSize: Int = Int.MAX_VALUE,
    private val viewOptions: ListViewOptions = ListViewOptions(),
) : Component<T> {
    private var cursorIndex = 0
    private var interacting = true
    private var windowPageStartIndex = 0
    private var infoMessage = ""

    init {
        if (pageSize < choices.size) {
            infoMessage = "(move up and down to reveal more choices)"
        }
    }

    /**
     * Retrieves the value of the selected choice.
     *
     * @return The data associated with the selected choice.
     */
    override fun value(): T = choices[cursorIndex].data

    /**
     * Checks if the component is currently interacting.
     *
     * @return True if the component is interacting, false otherwise.
     */
    override fun isInteracting(): Boolean = interacting

    /**
     * Handles an event for the list component.
     *
     * @param event The event to be handled.
     */
    override fun onEvent(event: KInquirerEvent) {
        when (event) {
            KeyPressUp -> {
                cursorIndex = max(0, cursorIndex - 1)
                if (cursorIndex < windowPageStartIndex) {
                    windowPageStartIndex = max(0, windowPageStartIndex - 1)
                }
            }
            KeyPressDown -> {
                cursorIndex = min(choices.size - 1, cursorIndex + 1)
                if (cursorIndex > windowPageStartIndex + pageSize - 1) {
                    windowPageStartIndex = min(choices.size - 1, windowPageStartIndex + 1)
                }
            }
            KeyPressEnter -> {
                interacting = false
            }
            else -> {
                // No action needed for other events
            }
        }
    }

    /**
     * Renders the list component as a string.
     *
     * @return The rendered string representation of the list component.
     */
    override fun render(): String =
        buildString {
            // Question mark character
            append(viewOptions.questionMarkPrefix)
            append(" ")

            // Message
            append(message.toAnsi { bold() })
            append(" ")

            if (interacting) {
                // Hint
                if (hint.isNotBlank()) {
                    append(hint.toAnsi { fgBrightBlack() })
                }
                appendLine()
                // Choices
                choices.forEachIndexed { index, choice ->
                    appendListRow(index, choice)
                }
                // Info message
                if (infoMessage.isNotBlank()) {
                    appendLine(infoMessage.toAnsi { fgBrightBlack() })
                }
            } else {
                // Result
                appendLine(
                    choices[cursorIndex].displayName.toAnsi {
                        fgCyan()
                        bold()
                    },
                )
            }
        }

    /**
     * Appends a row for a choice to the string builder.
     *
     * @param currentIndex The index of the current choice.
     * @param choice The choice to append.
     */
    private fun StringBuilder.appendListRow(
        currentIndex: Int,
        choice: Choice<T>,
    ) {
        if (currentIndex in windowPageStartIndex until windowPageStartIndex + pageSize) {
            appendCursor(currentIndex)
            appendChoice(currentIndex, choice)
            appendLine()
        }
    }

    /**
     * Appends the cursor to the string builder.
     *
     * @param currentIndex The index of the current choice.
     */
    private fun StringBuilder.appendCursor(currentIndex: Int) {
        if (currentIndex == cursorIndex) {
            append(viewOptions.cursor)
        } else {
            append(viewOptions.nonCursor)
        }
    }

    /**
     * Appends a choice to the string builder.
     *
     * @param currentIndex The index of the current choice.
     * @param choice The choice to append.
     */
    private fun StringBuilder.appendChoice(
        currentIndex: Int,
        choice: Choice<T>,
    ) {
        if (currentIndex == cursorIndex) {
            append(
                choice.displayName.toAnsi {
                    fgCyan()
                    bold()
                },
            )
        } else {
            append(choice.displayName)
        }
    }
}

/**
 * Lazy property to check if the terminal is an old terminal.
 */
private val isOldTerminal: Boolean by lazy { System.getProperty("os.name").contains("win", ignoreCase = true) }

/**
 * ListViewOptions is a data class for customizing the view of the list component.
 *
 * @property questionMarkPrefix The prefix for the question mark.
 * @property cursor The string to display for the cursor.
 * @property nonCursor The string to display for non-cursor choices.
 */
data class ListViewOptions(
    val questionMarkPrefix: String =
        "?".toAnsiStr {
            bold()
            fgGreen()
        },
    val cursor: String = (if (isOldTerminal) " > " else " ❯ ").toAnsiStr { fgBrightCyan() },
    val nonCursor: String = "   ",
)

/**
 * Prompts the user to select a choice from a list.
 *
 * @param message The message to display to the user.
 * @param choices The list of choices to display.
 * @param hint A hint to display when the list is shown.
 * @param pageSize The number of choices to display per page.
 * @param viewOptions The options for customizing the view.
 * @return The selected choice as a string.
 */
fun KInquirer.promptList(
    message: String,
    choices: List<String> = emptyList(),
    hint: String = "",
    pageSize: Int = Int.MAX_VALUE,
    viewOptions: ListViewOptions = ListViewOptions(),
): String =
    promptListObject(
        message = message,
        hint = hint,
        choices = choices.map { choice -> Choice(choice, choice) },
        pageSize = pageSize,
        viewOptions = viewOptions,
    )

/**
 * Prompts the user to select a choice from a list.
 *
 * @param T The type of the data associated with each choice.
 * @param message The message to display to the user.
 * @param choices The list of choices to display.
 * @param hint A hint to display when the list is shown.
 * @param pageSize The number of choices to display per page.
 * @param viewOptions The options for customizing the view.
 * @return The selected choice as an object of type T.
 */
fun <T> KInquirer.promptListObject(
    message: String,
    choices: List<Choice<T>> = emptyList(),
    hint: String = "",
    pageSize: Int = Int.MAX_VALUE,
    viewOptions: ListViewOptions = ListViewOptions(),
): T =
    prompt(
        ListComponent(
            message = message,
            hint = hint,
            choices = choices,
            pageSize = pageSize,
            viewOptions = viewOptions,
        ),
    )
