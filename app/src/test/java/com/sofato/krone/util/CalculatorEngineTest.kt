package com.sofato.krone.util

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Locale

class CalculatorEngineTest {

    private lateinit var engine: CalculatorEngine
    private lateinit var previousLocale: Locale

    @Before
    fun setup() {
        previousLocale = Locale.getDefault()
        Locale.setDefault(Locale.US)
        engine = CalculatorEngine()
    }

    @After
    fun tearDown() {
        Locale.setDefault(previousLocale)
    }

    // --- Digit input ---

    @Test
    fun `initial state shows zero`() {
        assertThat(engine.state.displayText).isEqualTo("0")
        assertThat(engine.state.expression).isEmpty()
    }

    @Test
    fun `entering digits updates expression and display`() {
        engine.onDigit('1')
        engine.onDigit('2')
        engine.onDigit('3')
        assertThat(engine.state.expression).isEqualTo("123")
        assertThat(engine.state.displayText).isEqualTo("123")
    }

    // --- Decimal input ---

    @Test
    fun `decimal after digits`() {
        engine.onDigit('5')
        engine.onDecimal()
        engine.onDigit('2')
        assertThat(engine.state.expression).isEqualTo("5.2")
    }

    @Test
    fun `decimal at start adds leading zero`() {
        engine.onDecimal()
        assertThat(engine.state.expression).isEqualTo("0.")
    }

    @Test
    fun `double decimal in same number segment is ignored`() {
        engine.onDigit('1')
        engine.onDecimal()
        engine.onDecimal()
        assertThat(engine.state.expression).isEqualTo("1.")
    }

    @Test
    fun `decimal allowed in new number segment after operator`() {
        engine.onDigit('1')
        engine.onOperator('+')
        engine.onDecimal()
        engine.onDigit('5')
        assertThat(engine.state.expression).isEqualTo("1+0.5")
    }

    // --- Operator input ---

    @Test
    fun `operator after digit`() {
        engine.onDigit('5')
        engine.onOperator('+')
        assertThat(engine.state.expression).isEqualTo("5+")
    }

    @Test
    fun `operator replaces previous operator`() {
        engine.onDigit('5')
        engine.onOperator('+')
        engine.onOperator('-')
        assertThat(engine.state.expression).isEqualTo("5-")
    }

    @Test
    fun `minus at start is allowed for negative number`() {
        engine.onOperator('-')
        assertThat(engine.state.expression).isEqualTo("-")
    }

    @Test
    fun `non-minus operator at start is ignored`() {
        engine.onOperator('+')
        assertThat(engine.state.expression).isEmpty()
    }

    @Test
    fun `operator after decimal is ignored`() {
        engine.onDigit('5')
        engine.onDecimal()
        engine.onOperator('+')
        assertThat(engine.state.expression).isEqualTo("5.")
    }

    // --- Backspace ---

    @Test
    fun `backspace removes last character`() {
        engine.onDigit('1')
        engine.onDigit('2')
        engine.onBackspace()
        assertThat(engine.state.expression).isEqualTo("1")
    }

    @Test
    fun `backspace on single character resets to initial state`() {
        engine.onDigit('1')
        engine.onBackspace()
        assertThat(engine.state.displayText).isEqualTo("0")
        assertThat(engine.state.expression).isEmpty()
    }

    @Test
    fun `backspace on empty expression does nothing`() {
        engine.onBackspace()
        assertThat(engine.state.displayText).isEqualTo("0")
    }

    // --- Clear ---

    @Test
    fun `clear resets to initial state`() {
        engine.onDigit('1')
        engine.onDigit('2')
        engine.onOperator('+')
        engine.onDigit('3')
        engine.onClear()
        assertThat(engine.state).isEqualTo(CalculatorState())
    }

    // --- Evaluate: basic arithmetic ---

    @Test
    fun `evaluate single number`() {
        engine.onDigit('4')
        engine.onDigit('2')
        val result = engine.evaluate()
        assertThat(result).isEqualTo(42.0)
    }

    @Test
    fun `evaluate addition`() {
        engine.onDigit('1')
        engine.onDigit('0')
        engine.onOperator('+')
        engine.onDigit('5')
        val result = engine.evaluate()
        assertThat(result).isEqualTo(15.0)
    }

    @Test
    fun `evaluate subtraction`() {
        engine.onDigit('2')
        engine.onDigit('0')
        engine.onOperator('-')
        engine.onDigit('8')
        val result = engine.evaluate()
        assertThat(result).isEqualTo(12.0)
    }

    @Test
    fun `evaluate multiplication`() {
        engine.onDigit('6')
        engine.onOperator('*')
        engine.onDigit('7')
        val result = engine.evaluate()
        assertThat(result).isEqualTo(42.0)
    }

    @Test
    fun `evaluate division`() {
        engine.onDigit('8')
        engine.onDigit('4')
        engine.onOperator('/')
        engine.onDigit('4')
        val result = engine.evaluate()
        assertThat(result).isEqualTo(21.0)
    }

    // --- Evaluate: operator precedence ---

    @Test
    fun `multiplication before addition`() {
        // 2 + 3 * 4 = 14
        engine.onDigit('2')
        engine.onOperator('+')
        engine.onDigit('3')
        engine.onOperator('*')
        engine.onDigit('4')
        val result = engine.evaluate()
        assertThat(result).isEqualTo(14.0)
    }

    @Test
    fun `division before subtraction`() {
        // 20 - 12 / 4 = 17
        engine.onDigit('2')
        engine.onDigit('0')
        engine.onOperator('-')
        engine.onDigit('1')
        engine.onDigit('2')
        engine.onOperator('/')
        engine.onDigit('4')
        val result = engine.evaluate()
        assertThat(result).isEqualTo(17.0)
    }

    // --- Evaluate: bill splitting use case ---

    @Test
    fun `bill split by 4`() {
        // 300 / 4 = 75
        engine.onDigit('3')
        engine.onDigit('0')
        engine.onDigit('0')
        engine.onOperator('/')
        engine.onDigit('4')
        val result = engine.evaluate()
        assertThat(result).isEqualTo(75.0)
    }

    @Test
    fun `bill split with addition`() {
        // 150 / 4 + 50 = 87.5
        engine.onDigit('1')
        engine.onDigit('5')
        engine.onDigit('0')
        engine.onOperator('/')
        engine.onDigit('4')
        engine.onOperator('+')
        engine.onDigit('5')
        engine.onDigit('0')
        val result = engine.evaluate()
        assertThat(result).isEqualTo(87.5)
    }

    // --- Evaluate: decimal amounts ---

    @Test
    fun `decimal amount evaluation`() {
        // 12.50 + 3.75 = 16.25
        engine.onDigit('1')
        engine.onDigit('2')
        engine.onDecimal()
        engine.onDigit('5')
        engine.onDigit('0')
        engine.onOperator('+')
        engine.onDigit('3')
        engine.onDecimal()
        engine.onDigit('7')
        engine.onDigit('5')
        val result = engine.evaluate()
        assertThat(result).isEqualTo(16.25)
    }

    // --- Evaluate: edge cases ---

    @Test
    fun `evaluate empty expression returns null`() {
        val result = engine.evaluate()
        assertThat(result).isNull()
    }

    @Test
    fun `trailing operator is stripped before eval`() {
        engine.onDigit('5')
        engine.onOperator('+')
        val result = engine.evaluate()
        assertThat(result).isEqualTo(5.0)
    }

    @Test
    fun `trailing decimal is stripped before eval`() {
        engine.onDigit('5')
        engine.onDecimal()
        val result = engine.evaluate()
        assertThat(result).isEqualTo(5.0)
    }

    @Test
    fun `division by zero returns null and sets error`() {
        engine.onDigit('5')
        engine.onOperator('/')
        engine.onDigit('0')
        val result = engine.evaluate()
        assertThat(result).isNull()
        assertThat(engine.state.hasError).isTrue()
    }

    @Test
    fun `negative result returns null and sets error`() {
        engine.onDigit('3')
        engine.onOperator('-')
        engine.onDigit('5')
        val result = engine.evaluate()
        assertThat(result).isNull()
        assertThat(engine.state.hasError).isTrue()
    }

    @Test
    fun `zero result returns null and sets error`() {
        engine.onDigit('5')
        engine.onOperator('-')
        engine.onDigit('5')
        val result = engine.evaluate()
        assertThat(result).isNull()
        assertThat(engine.state.hasError).isTrue()
    }

    // --- Display formatting ---

    @Test
    fun `display shows multiplication as times symbol`() {
        engine.onDigit('3')
        engine.onOperator('*')
        engine.onDigit('4')
        assertThat(engine.state.displayText).isEqualTo("3\u00D74")
    }

    @Test
    fun `display shows division as division symbol`() {
        engine.onDigit('8')
        engine.onOperator('/')
        engine.onDigit('2')
        assertThat(engine.state.displayText).isEqualTo("8\u00F72")
    }

    @Test
    fun `evaluate sets result in state`() {
        engine.onDigit('5')
        engine.evaluate()
        assertThat(engine.state.result).isEqualTo(5.0)
        assertThat(engine.state.displayText).isEqualTo("5")
    }

    @Test
    fun `integer result formats without decimals`() {
        engine.onDigit('6')
        engine.onOperator('*')
        engine.onDigit('7')
        engine.evaluate()
        assertThat(engine.state.displayText).isEqualTo("42")
    }

    @Test
    fun `fractional result formats with two decimals`() {
        engine.onDigit('1')
        engine.onDigit('0')
        engine.onOperator('/')
        engine.onDigit('3')
        engine.evaluate()
        assertThat(engine.state.displayText).isEqualTo("3.33")
    }

    // --- setState ---

    @Test
    fun `setState sets expression and display`() {
        engine.setState("125.50")
        assertThat(engine.state.expression).isEqualTo("125.50")
        assertThat(engine.state.displayText).isEqualTo("125.50")
    }

    @Test
    fun `setState with empty string shows zero`() {
        engine.setState("")
        assertThat(engine.state.displayText).isEqualTo("0")
    }

    // --- getDisplayAmount ---

    @Test
    fun `getDisplayAmount returns zero for empty`() {
        assertThat(engine.getDisplayAmount()).isEqualTo("0")
    }

    @Test
    fun `getDisplayAmount returns plain number without operators`() {
        engine.onDigit('4')
        engine.onDigit('2')
        assertThat(engine.getDisplayAmount()).isEqualTo("42")
    }

    @Test
    fun `getDisplayAmount evaluates partial expression`() {
        engine.onDigit('1')
        engine.onDigit('0')
        engine.onOperator('+')
        engine.onDigit('5')
        assertThat(engine.getDisplayAmount()).isEqualTo("15")
    }

    // --- Static evaluateExpression ---

    @Test
    fun `evaluateExpression with chained operations`() {
        // 10 + 5 * 2 - 3 = 17
        val result = CalculatorEngine.evaluateExpression("10+5*2-3")
        assertThat(result).isEqualTo(17.0)
    }

    @Test
    fun `evaluateExpression with negative start`() {
        val result = CalculatorEngine.evaluateExpression("-5+10")
        assertThat(result).isEqualTo(5.0)
    }

    @Test
    fun `evaluateExpression returns null for invalid input`() {
        val result = CalculatorEngine.evaluateExpression("abc")
        assertThat(result).isNull()
    }
}
