package com.sofato.krone.util

data class CalculatorState(
    val expression: String = "",
    val displayText: String = "0",
    val result: Double? = null,
    val hasError: Boolean = false,
)

class CalculatorEngine {

    private var _state = CalculatorState()
    val state: CalculatorState get() = _state

    fun onDigit(digit: Char) {
        require(digit.isDigit())
        val newExpr = _state.expression + digit
        _state = _state.copy(
            expression = newExpr,
            displayText = formatExpression(newExpr),
            result = null,
        )
    }

    fun onDecimal() {
        val lastNumber = getLastNumberSegment(_state.expression)
        if (lastNumber.contains('.')) return
        val newExpr = if (_state.expression.isEmpty() || _state.expression.last().isOperator()) {
            _state.expression + "0."
        } else {
            _state.expression + "."
        }
        _state = _state.copy(
            expression = newExpr,
            displayText = formatExpression(newExpr),
            result = null,
        )
    }

    fun onOperator(op: Char) {
        require(op in OPERATORS)
        if (_state.expression.isEmpty()) {
            if (op == '-') {
                _state = _state.copy(
                    expression = "-",
                    displayText = "-",
                    result = null,
                )
            }
            return
        }
        val expr = _state.expression
        if (expr.last().isOperator()) {
            val newExpr = expr.dropLast(1) + op
            _state = _state.copy(
                expression = newExpr,
                displayText = formatExpression(newExpr),
            )
            return
        }
        if (expr.last() == '.') return
        val newExpr = expr + op
        _state = _state.copy(
            expression = newExpr,
            displayText = formatExpression(newExpr),
            result = null,
        )
    }

    fun onBackspace() {
        if (_state.expression.isEmpty()) return
        val newExpr = _state.expression.dropLast(1)
        _state = if (newExpr.isEmpty()) {
            CalculatorState()
        } else {
            _state.copy(
                expression = newExpr,
                displayText = formatExpression(newExpr),
                result = null,
            )
        }
    }

    fun onClear() {
        _state = CalculatorState()
    }

    fun evaluate(): Double? {
        val expr = _state.expression
        if (expr.isEmpty()) return null
        val cleanExpr = if (expr.last().isOperator() || expr.last() == '.') {
            expr.dropLast(1)
        } else {
            expr
        }
        if (cleanExpr.isEmpty()) return null

        val result = evaluateExpression(cleanExpr) ?: run {
            _state = _state.copy(hasError = true)
            return null
        }
        if (result <= 0) {
            _state = _state.copy(hasError = true)
            return null
        }
        _state = _state.copy(
            displayText = formatResult(result),
            result = result,
            hasError = false,
        )
        return result
    }

    fun getDisplayAmount(): String {
        if (_state.result != null) return formatResult(_state.result!!)
        val expr = _state.expression
        if (expr.isEmpty()) return "0"
        val cleanExpr = if (expr.last().isOperator() || expr.last() == '.') {
            expr.dropLast(1)
        } else {
            expr
        }
        if (cleanExpr.isEmpty()) return "0"
        val hasOperators = cleanExpr.drop(1).any { it.isOperator() }
        if (!hasOperators) return formatExpression(cleanExpr)
        val partialResult = evaluateExpression(cleanExpr)
        return if (partialResult != null) formatResult(partialResult) else "0"
    }

    fun setState(amount: String) {
        _state = CalculatorState(
            expression = amount,
            displayText = if (amount.isEmpty()) "0" else formatExpression(amount),
        )
    }

    private fun getLastNumberSegment(expr: String): String {
        val lastOpIndex = expr.indexOfLast { it.isOperator() }
        return if (lastOpIndex < 0) expr else expr.substring(lastOpIndex + 1)
    }

    private fun Char.isOperator(): Boolean = this in OPERATORS

    companion object {
        private val OPERATORS = setOf('+', '-', '*', '/')

        internal fun evaluateExpression(expr: String): Double? {
            val tokens = tokenize(expr) ?: return null
            if (tokens.isEmpty()) return null
            return evaluateTokens(tokens)
        }

        private fun tokenize(expr: String): List<Any>? {
            val tokens = mutableListOf<Any>()
            var i = 0
            while (i < expr.length) {
                val c = expr[i]
                if (c.isDigit() || c == '.') {
                    val start = i
                    while (i < expr.length && (expr[i].isDigit() || expr[i] == '.')) i++
                    val numStr = expr.substring(start, i)
                    val num = numStr.toDoubleOrNull() ?: return null
                    tokens.add(num)
                } else if (c in OPERATORS) {
                    if (c == '-' && (tokens.isEmpty() || tokens.last() is Char)) {
                        i++
                        val start = i
                        while (i < expr.length && (expr[i].isDigit() || expr[i] == '.')) i++
                        if (i == start) return null
                        val numStr = expr.substring(start, i)
                        val num = numStr.toDoubleOrNull() ?: return null
                        tokens.add(-num)
                    } else {
                        tokens.add(c)
                        i++
                    }
                } else {
                    return null
                }
            }
            return tokens
        }

        private fun evaluateTokens(tokens: List<Any>): Double? {
            val numbers = mutableListOf<Double>()
            val ops = mutableListOf<Char>()

            for (token in tokens) {
                when (token) {
                    is Double -> numbers.add(token)
                    is Char -> ops.add(token)
                }
            }
            if (numbers.isEmpty()) return null
            if (numbers.size != ops.size + 1) return null

            // First pass: * and /
            val reducedNumbers = mutableListOf(numbers[0])
            val reducedOps = mutableListOf<Char>()
            for (i in ops.indices) {
                val op = ops[i]
                val right = numbers[i + 1]
                if (op == '*' || op == '/') {
                    val left = reducedNumbers.removeLast()
                    if (op == '/' && right == 0.0) return null
                    reducedNumbers.add(if (op == '*') left * right else left / right)
                } else {
                    reducedOps.add(op)
                    reducedNumbers.add(right)
                }
            }

            // Second pass: + and -
            var result = reducedNumbers[0]
            for (i in reducedOps.indices) {
                val right = reducedNumbers[i + 1]
                result = if (reducedOps[i] == '+') result + right else result - right
            }
            return result
        }

        fun formatExpression(expr: String): String {
            return expr
                .replace('*', '\u00D7')
                .replace('/', '\u00F7')
        }

        fun formatResult(value: Double): String {
            return if (value == value.toLong().toDouble()) {
                value.toLong().toString()
            } else {
                String.format("%.2f", value)
            }
        }
    }
}
