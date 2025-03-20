package xyz.malefic.scripting

import org.jline.terminal.Terminal
import org.jline.terminal.TerminalBuilder
import xyz.malefic.scripting.core.AnsiOutput
import xyz.malefic.scripting.core.Component
import xyz.malefic.scripting.core.KInquirerReaderHandler
import java.io.Reader

/**
 * Object for handling user prompts in the terminal.
 */
object KInquirer {
    /**
     * Prompts the user with the given component and returns the result.
     *
     * @param T The type of the value held by the component.
     * @param component The component to prompt the user with.
     * @return The value of the component after user interaction.
     */
    fun <T> prompt(component: Component<T>): T {
        runTerminal { reader ->
            val readerHandler = KInquirerReaderHandler.getInstance()
            AnsiOutput.display(component.render())
            while (component.isInteracting()) {
                val event = readerHandler.handleInteraction(reader)
                component.onEvent(event)
                AnsiOutput.display(component.render())
            }
        }
        return component.value()
    }

    /**
     * Runs the terminal in raw mode and executes the given function with a reader.
     *
     * @param func The function to execute with the terminal reader.
     */
    private fun runTerminal(func: (reader: Reader) -> Unit) {
        val terminal: Terminal =
            TerminalBuilder
                .builder()
                .jna(true)
                .system(true)
                .build()
        terminal.enterRawMode()
        val reader = terminal.reader()

        func(reader)

        reader.close()
        terminal.close()
    }
}
