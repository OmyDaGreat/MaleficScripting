package xyz.malefic.scripting.core

import xyz.malefic.scripting.core.KInquirerEvent.CharInput
import xyz.malefic.scripting.core.KInquirerEvent.ClearScreen
import xyz.malefic.scripting.core.KInquirerEvent.KeyPressBackspace
import xyz.malefic.scripting.core.KInquirerEvent.KeyPressDown
import xyz.malefic.scripting.core.KInquirerEvent.KeyPressEnter
import xyz.malefic.scripting.core.KInquirerEvent.KeyPressLeft
import xyz.malefic.scripting.core.KInquirerEvent.KeyPressRight
import xyz.malefic.scripting.core.KInquirerEvent.KeyPressSpace
import xyz.malefic.scripting.core.KInquirerEvent.KeyPressUp
import xyz.malefic.scripting.core.KInquirerEvent.NotSupportedChar
import java.io.Reader

private val isOldTerminal = System.getProperty("os.name").contains("win", ignoreCase = true)

/**
 * Functional interface for handling reader interactions in KInquirer.
 */
internal fun interface KInquirerReaderHandler {
    companion object {
        /**
         * Returns an instance of KInquirerReaderHandler based on the terminal type.
         *
         * @return An instance of KInquirerReaderHandler.
         */
        fun getInstance() =
            if (isOldTerminal) {
                KInquirerReaderHandlerOldTerminal
            } else {
                KInquirerReaderHandlerNewTerminal
            }
    }

    /**
     * Handles the interaction from the given reader and returns the corresponding event.
     *
     * @param reader The reader to read input from.
     * @return The corresponding KInquirerEvent.
     */
    fun handleInteraction(reader: Reader): KInquirerEvent
}

/**
 * Object for handling reader interactions in new terminals.
 */
internal object KInquirerReaderHandlerNewTerminal : KInquirerReaderHandler {
    /**
     * Handles the interaction from the given reader and returns the corresponding event.
     *
     * @param reader The reader to read input from.
     * @return The corresponding KInquirerEvent.
     */
    override fun handleInteraction(reader: Reader): KInquirerEvent =
        when (val c = reader.read()) {
            127 -> KeyPressBackspace
            13 -> KeyPressEnter
            32 -> KeyPressSpace
            12 -> ClearScreen
            27 -> readEscValues(reader)
            else -> CharInput(Char(c))
        }

    /**
     * Reads escape values from the reader and returns the corresponding event.
     *
     * @param reader The reader to read input from.
     * @return The corresponding KInquirerEvent.
     */
    private fun readEscValues(reader: Reader): KInquirerEvent {
        if (reader.read() == 91) {
            return when (reader.read()) {
                65 -> KeyPressUp // "↑"
                66 -> KeyPressDown // "↓"
                67 -> KeyPressRight // "→"
                68 -> KeyPressLeft // "←"
                else -> NotSupportedChar
            }
        }
        return NotSupportedChar
    }
}

/**
 * Object for handling reader interactions in old terminals.
 */
internal object KInquirerReaderHandlerOldTerminal : KInquirerReaderHandler {
    /**
     * Handles the interaction from the given reader and returns the corresponding event.
     *
     * @param reader The reader to read input from.
     * @return The corresponding KInquirerEvent.
     */
    override fun handleInteraction(reader: Reader): KInquirerEvent =
        when (val c = reader.read()) {
            8 -> KeyPressBackspace
            13 -> KeyPressEnter
            32 -> KeyPressSpace
            12 -> ClearScreen // (TODO)
            27 -> readEscValues(reader)
            else -> CharInput(Char(c))
        }

    /**
     * Reads escape values from the reader and returns the corresponding event.
     *
     * @param reader The reader to read input from.
     * @return The corresponding KInquirerEvent.
     */
    private fun readEscValues(reader: Reader): KInquirerEvent {
        if (reader.read() == 79) {
            return when (reader.read()) {
                65 -> KeyPressUp // "↑"
                66 -> KeyPressDown // "↓"
                67 -> KeyPressRight // "→"
                68 -> KeyPressLeft // "←"
                else -> NotSupportedChar
            }
        }
        return NotSupportedChar
    }
}
