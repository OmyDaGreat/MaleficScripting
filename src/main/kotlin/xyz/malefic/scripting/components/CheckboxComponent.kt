package xyz.malefic.scripting.components

import xyz.malefic.scripting.KInquirer
import xyz.malefic.scripting.core.Choice
import xyz.malefic.scripting.core.Component
import xyz.malefic.scripting.core.KInquirerEvent
import xyz.malefic.scripting.core.KInquirerEvent.KeyPressDown
import xyz.malefic.scripting.core.KInquirerEvent.KeyPressEnter
import xyz.malefic.scripting.core.KInquirerEvent.KeyPressSpace
import xyz.malefic.scripting.core.KInquirerEvent.KeyPressUp
import xyz.malefic.scripting.core.toAnsi
import xyz.malefic.scripting.core.toAnsiStr
import kotlin.collections.filterIndexed
import kotlin.collections.forEachIndexed
import kotlin.math.max
import kotlin.math.min

/**
 * CheckboxComponent is a component for displaying a list of choices and handling multiple selections.
 *
 * @param T The type of the data associated with each choice.
 * @property message The message to display to the user.
 * @property hint A hint to display when the list is shown.
 * @property choices The list of choices to display.
 * @property maxNumOfSelection The maximum number of selections allowed.
 * @property minNumOfSelection The minimum number of selections required.
 * @property pageSize The number of choices to display per page.
 * @property viewOptions The options for customizing the view.
 */
internal class CheckboxComponent<T>(
    val message: String,
    val hint: String,
    val choices: List<Choice<T>>,
    private val maxNumOfSelection: Int = Int.MAX_VALUE,
    private val minNumOfSelection: Int = 0,
    private val pageSize: Int = Int.MAX_VALUE,
    private val viewOptions: CheckboxViewOptions = CheckboxViewOptions(),
) : Component<List<T>> {
    private val selectedIndices = mutableSetOf<Int>()
    private var cursorIndex = 0
    private var interacting = true
    private var value: List<T> = emptyList()
    private var windowPageStartIndex = 0
    private var errorMessage = ""
    private var infoMessage = ""

    init {
        if (pageSize < choices.size) {
            infoMessage = "(move up and down to reveal more choices)"
        }
    }

    /**
     * Retrieves the value of the selected choices.
     *
     * @return The list of data associated with the selected choices.
     */
    override fun value(): List<T> = value

    /**
     * Checks if the component is currently interacting.
     *
     * @return True if the component is interacting, false otherwise.
     */
    override fun isInteracting(): Boolean = interacting

    /**
     * Handles an event for the checkbox component.
     *
     * @param event The event to be handled.
     */
    override fun onEvent(event: KInquirerEvent) {
        errorMessage = ""
        when (event) {
            is KeyPressUp -> {
                cursorIndex = max(0, cursorIndex - 1)
                if (cursorIndex < windowPageStartIndex) {
                    windowPageStartIndex = max(0, windowPageStartIndex - 1)
                }
            }
            is KeyPressDown -> {
                cursorIndex = min(choices.size - 1, cursorIndex + 1)
                if (cursorIndex > windowPageStartIndex + pageSize - 1) {
                    windowPageStartIndex = min(choices.size - 1, windowPageStartIndex + 1)
                }
            }
            is KeyPressSpace -> {
                when {
                    cursorIndex in selectedIndices -> {
                        selectedIndices.remove(cursorIndex)
                    }
                    selectedIndices.size + 1 <= maxNumOfSelection -> {
                        selectedIndices.add(cursorIndex)
                    }
                    selectedIndices.size + 1 > maxNumOfSelection -> {
                        errorMessage = "max selection: $maxNumOfSelection"
                    }
                }
            }
            is KeyPressEnter -> {
                if (selectedIndices.size < minNumOfSelection) {
                    errorMessage = "min selection: $minNumOfSelection"
                } else {
                    interacting = false
                    value =
                        choices
                            .filterIndexed { index, _ -> index in selectedIndices }
                            .map { choice -> choice.data }
                }
            }
            else -> {
                // No action needed for other events
            }
        }
    }

    /**
     * Renders the checkbox component as a string.
     *
     * @return The rendered string representation of the checkbox component.
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
                // Error message
                if (errorMessage.isNotBlank()) {
                    appendLine(
                        errorMessage.toAnsi {
                            bold()
                            fgRed()
                        },
                    )
                }
            } else {
                // Results
                appendLine(
                    choices
                        .filterIndexed { index, _ -> index in selectedIndices }
                        .joinToString(", ") { choice -> choice.displayName }
                        .toAnsi {
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
        if (currentIndex in selectedIndices) {
            append(viewOptions.checked)
        } else {
            append(viewOptions.unchecked)
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
        if (currentIndex in selectedIndices) {
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
 * CheckboxViewOptions is a data class for customizing the view of the checkbox component.
 *
 * @property questionMarkPrefix The prefix for the question mark.
 * @property cursor The string to display for the cursor.
 * @property nonCursor The string to display for non-cursor choices.
 * @property checked The string to display for checked choices.
 * @property unchecked The string to display for unchecked choices.
 */
data class CheckboxViewOptions(
    val questionMarkPrefix: String =
        "?".toAnsiStr {
            bold()
            fgGreen()
        },
    val cursor: String = (if (isOldTerminal) " > " else " ❯ ").toAnsiStr { fgBrightCyan() },
    val nonCursor: String = "   ",
    val checked: String = (if (isOldTerminal) "(*) " else "◉ ").toAnsiStr { fgGreen() },
    val unchecked: String = (if (isOldTerminal) "( ) " else "◯ "),
)

/**
 * Prompts the user to select multiple choices from a list.
 *
 * @param message The message to display to the user.
 * @param choices The list of choices to display.
 * @param hint A hint to display when the list is shown.
 * @param maxNumOfSelection The maximum number of selections allowed.
 * @param minNumOfSelection The minimum number of selections required.
 * @param pageSize The number of choices to display per page.
 * @param viewOptions The options for customizing the view.
 * @return The list of selected choices as strings.
 */
fun KInquirer.promptCheckbox(
    message: String,
    choices: List<String>,
    hint: String = "",
    maxNumOfSelection: Int = Int.MAX_VALUE,
    minNumOfSelection: Int = 0,
    pageSize: Int = 10,
    viewOptions: CheckboxViewOptions = CheckboxViewOptions(),
): List<String> =
    promptCheckboxObject(
        message = message,
        choices = choices.map { Choice(it, it) },
        hint = hint,
        maxNumOfSelection = maxNumOfSelection,
        minNumOfSelection = minNumOfSelection,
        pageSize = pageSize,
        viewOptions = viewOptions,
    )

/**
 * Prompts the user to select multiple choices from a list.
 *
 * @param T The type of the data associated with each choice.
 * @param message The message to display to the user.
 * @param choices The list of choices to display.
 * @param hint A hint to display when the list is shown.
 * @param maxNumOfSelection The maximum number of selections allowed.
 * @param minNumOfSelection The minimum number of selections required.
 * @param pageSize The number of choices to display per page.
 * @param viewOptions The options for customizing the view.
 * @return The list of selected choices as objects of type T.
 */
fun <T> KInquirer.promptCheckboxObject(
    message: String,
    choices: List<Choice<T>>,
    hint: String = "",
    maxNumOfSelection: Int = Int.MAX_VALUE,
    minNumOfSelection: Int = 0,
    pageSize: Int = 10,
    viewOptions: CheckboxViewOptions = CheckboxViewOptions(),
): List<T> =
    prompt(
        CheckboxComponent(
            message = message,
            hint = hint,
            maxNumOfSelection = maxNumOfSelection,
            minNumOfSelection = minNumOfSelection,
            choices = choices,
            pageSize = pageSize,
            viewOptions = viewOptions,
        ),
    )
