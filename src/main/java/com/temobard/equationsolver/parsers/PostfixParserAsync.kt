package com.temobard.equationsolver.parsers

import com.temobard.equationsolver.solvers.PostfixSolver
import com.temobard.equationsolver.tokens.Delimiter
import com.temobard.equationsolver.tokens.Operator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/**
 * The asynchronous postfix (Reverse Polish Notation) parser.
 * Uses the Shunting-yard algorithm to parse a mathematical expression
 * NOTE: this class uses an experimental Kotlin coroutine-based solution.
 * @param eqString String containing the expression
 * @see PostfixParser
 */
@OptIn
class PostfixParserAsync(
    eqString: String,
    variableSymbol: String = DEFAULT_VARIABLE_SYMBOL
) : PostfixBaseParser(eqString, variableSymbol) {

    /**
     * Thread-blocking parser method.
     * Use this method only from within a regular thread.
     * @return parsed equation in Reversed Polish Notation format
     */
    override fun parse(): PostfixSolver = runBlocking {
        parseSuspend()
    }

    /**
     * Coroutine scope-friendly parser method.
     * Call this method from a coroutine scope.
     * @return parsed equation in Reversed Polish Notation format
     */
    suspend fun parseSuspend(): PostfixSolver {
        val splits = splitAll(eqString)
        return PostfixSolver(infixToPostfix(splits))
    }

    /**
     * Splits the expression string into tokens
     */
    private suspend fun splitAll(eqString: String): ArrayList<String> = withContext(Dispatchers.Default) {
        val eq = eqString.replace(" ", "").toLowerCase()

        val splitters = ArrayList<String>().apply { addAll(Delimiter.types) }
        Operator.values().forEach { splitters.add(it.value) }

        val breaks = ArrayList<String>().apply { add(eq) }
        for (splitter in splitters) {
            val a = ArrayList<String>()
            for (ind in 0 until breaks.size) {
                breaks[ind].let {
                    if (it.length > 1) {
                        val job = async { splitString(it, splitter) }
                        a.addAll(job.await())
                    } else a.add(it)
                }
            }
            breaks.clear()
            breaks.addAll(a)
        }
        breaks
    }

    /**
     * Splits string into a series of sub-strings
     */
    private fun splitString(input: String, delimeter: String): ArrayList<String> {
        val tokens = input.split(delimeter)
        val array = ArrayList<String>()
        for (t in tokens) {
            array.add(t)
            array.add(delimeter)
        }
        array.removeAt(array.size - 1)
        return array
    }
}
