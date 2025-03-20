package xyz.malefic.scripting.core

/**
 * Represents a choice with a display name and associated data.
 *
 * @param T The type of the data associated with the choice.
 * @property displayName The name to be displayed for the choice.
 * @property data The data associated with the choice.
 */
data class Choice<T>(
    val displayName: String,
    val data: T,
)

/**
 * Sealed class representing various events in KInquirer.
 */
sealed class KInquirerEvent {
    /**
     * Event representing the "up" key press.
     * MacOS = 27,91,65 | Windows = 27,79,65
     */
    object KeyPressUp : KInquirerEvent()

    /**
     * Event representing the "down" key press.
     * MacOS = 27,91,66 | Windows = 27,79,66
     */
    object KeyPressDown : KInquirerEvent()

    /**
     * Event representing the "right" key press.
     * MacOS = 27,91,67 | Windows = 27,79,67
     */
    object KeyPressRight : KInquirerEvent()

    /**
     * Event representing the "left" key press.
     * MacOS = 27,91,68 | Windows = 27,79,68
     */
    object KeyPressLeft : KInquirerEvent()

    /**
     * Event representing the "enter" key press.
     * MacOS = 13 | Windows = 13
     */
    object KeyPressEnter : KInquirerEvent()

    /**
     * Event representing the "space" key press.
     * MacOS = 32 | Windows = 32
     */
    object KeyPressSpace : KInquirerEvent()

    /**
     * Event representing the "clear screen" action.
     * MacOS = 12 | Windows = ?
     */
    object ClearScreen : KInquirerEvent()

    /**
     * Event representing the "backspace" key press.
     * MacOS = 127 | Windows = 8
     */
    object KeyPressBackspace : KInquirerEvent()

    /**
     * Event representing a not supported character.
     */
    object NotSupportedChar : KInquirerEvent()

    /**
     * Event representing a character input.
     *
     * @property c The character that was input.
     */
    data class CharInput(
        val c: Char,
    ) : KInquirerEvent()
}
