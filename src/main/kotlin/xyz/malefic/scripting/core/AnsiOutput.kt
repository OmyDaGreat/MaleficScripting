package xyz.malefic.scripting.core

import org.fusesource.jansi.Ansi
import org.fusesource.jansi.Ansi.ansi
import org.fusesource.jansi.AnsiConsole
import kotlin.text.Charsets.UTF_8

/**
 * AnsiOutput is a utility object for displaying ANSI formatted text in the console.
 */
internal object AnsiOutput {
    private var prevViewHeight = 0

    /**
     * Displays the given view string in the console with ANSI formatting.
     *
     * @param view The string to be displayed.
     */
    fun display(view: String) {
        val rendered =
            buildString {
                appendAnsi {
                    cursorToColumn(0)
                    eraseLine(Ansi.Erase.ALL)
                    if (prevViewHeight > 2) {
                        repeat(prevViewHeight - 1) {
                            eraseLine(Ansi.Erase.ALL)
                            cursorUpLine()
                        }
                        eraseLine(Ansi.Erase.ALL)
                    }
                }
                append(view)

                prevViewHeight = view.lines().size
            }

        with(AnsiConsole.out()) {
            write(rendered.toByteArray(UTF_8))
            flush()
        }
    }

    /**
     * Appends ANSI formatted text to the StringBuilder.
     *
     * @param func A lambda function to apply ANSI formatting.
     */
    private fun StringBuilder.appendAnsi(func: Ansi.() -> Unit = {}) {
        val ansi = ansi()
        func(ansi)
        append(ansi)
    }
}

/**
 * Converts the string to an ANSI formatted Ansi object.
 *
 * @param func A lambda function to apply ANSI formatting.
 * @return The ANSI formatted Ansi object.
 */
internal fun String.toAnsi(func: Ansi.() -> Unit = {}): Ansi {
    val ansi = ansi()
    func(ansi)
    return ansi.a(this).reset()
}

/**
 * Converts the string to an ANSI formatted string.
 *
 * @param func A lambda function to apply ANSI formatting.
 * @return The ANSI formatted string.
 */
internal fun String.toAnsiStr(func: Ansi.() -> Unit = {}): String = toAnsi(func).toString()
