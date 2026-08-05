package com.example.novacalc

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import android.content.Intent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.*

class MainActivity : AppCompatActivity() {

    private lateinit var tvExpression: EditText
    private lateinit var tvResult: TextView

    private var currentInput = "0"
    private var expression = ""
    private var isEvaluated = false
    private var isDegreeMode = true
    private var lastAns = 0.0

    // Math Mode State
    private var isMathMode = false
    private var mathType = ""
    private var activeBox = 1

    private lateinit var historyManager: HistoryManager
    private lateinit var layoutNormalDisplay: View
    private lateinit var layoutMathDisplay: View
    private lateinit var tvMathPrefix: TextView
    private lateinit var tvMathBox1: TextView
    private lateinit var tvMathBox2: TextView

    private lateinit var layoutHistoryTape: android.widget.LinearLayout
    private lateinit var svHistoryTape: android.widget.ScrollView
    private lateinit var gridScientific: android.widget.GridLayout
    private lateinit var gestureDetector: android.view.GestureDetector
    private var isScientificVisible = true
    private var isSwipeInControls = false

    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPref = getSharedPreferences("AppPrefs", android.content.Context.MODE_PRIVATE)
        val isDark = sharedPref.getBoolean("pref_dark_mode", true)
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
            if (isDark) androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES else androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
        )
        super.onCreate(savedInstanceState)
        // Edge-to-edge support
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
        val windowInsetsController = androidx.core.view.WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = !isDark
        windowInsetsController.isAppearanceLightNavigationBars = !isDark
        
        setContentView(R.layout.activity_main)

        // Apply window insets to avoid overlapping with system bars
        val rootLayout = findViewById<android.view.View>(android.R.id.content)
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { view, insets ->
            val systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tvExpression = findViewById(R.id.tvExpression)
        tvResult = findViewById(R.id.tvResult)

        tvExpression.showSoftInputOnFocus = false
        historyManager = HistoryManager(this)

        setupSpinner()
        setupListeners()
        showTutorialIfNeeded()
    }

    private fun setupSpinner() {
        val btnNav = findViewById<ImageButton>(R.id.btnNavMenu)
        btnNav.setOnClickListener { view ->
            val popup = PopupMenu(this, view)
            popup.menuInflater.inflate(R.menu.nav_menu, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.nav_age -> {
                        val intent = Intent(this@MainActivity, AgeCalculatorActivity::class.java)
                        startActivity(intent)
                        true
                    }
                    R.id.nav_bmi -> {
                        val intent = Intent(this@MainActivity, BmiCalculatorActivity::class.java)
                        startActivity(intent)
                        true
                    }
                    R.id.nav_settings -> {
                        val intent = Intent(this@MainActivity, SettingsActivity::class.java)
                        startActivity(intent)
                        true
                    }
                    R.id.nav_about -> {
                        val intent = Intent(this@MainActivity, AboutActivity::class.java)
                        startActivity(intent)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    private fun setupListeners() {
        layoutNormalDisplay = findViewById(R.id.layoutNormalDisplay)
        layoutMathDisplay = findViewById(R.id.layoutMathDisplay)
        tvMathPrefix = findViewById(R.id.tvMathPrefix)
        tvMathBox1 = findViewById(R.id.tvMathBox1)
        tvMathBox2 = findViewById(R.id.tvMathBox2)

        layoutHistoryTape = findViewById(R.id.layoutHistoryTape)
        svHistoryTape = findViewById(R.id.svHistoryTape)
        gridScientific = findViewById(R.id.gridScientific)

        gestureDetector = android.view.GestureDetector(this, object : android.view.GestureDetector.SimpleOnGestureListener() {
            override fun onFling(e1: android.view.MotionEvent?, e2: android.view.MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                if (e1 == null) return false
                val deltaY = e2.y - e1.y
                if (Math.abs(deltaY) > 50 && Math.abs(velocityY) > 100) {
                    if (deltaY < 0) {
                        // Swipe UP -> Hide upper part
                        if (isScientificVisible) toggleScientificPanel()
                    } else {
                        // Swipe DOWN -> Show upper part
                        if (!isScientificVisible) toggleScientificPanel()
                    }
                    return true
                }
                return false
            }
        })

        populateHistoryTape()

        tvMathBox1.setOnClickListener {
            playHapticFeedback()
            activeBox = 1
            tvMathBox1.setBackgroundColor(getThemeColor(R.color.btn_op_bg))
            tvMathBox2.setBackgroundColor(getThemeColor(R.color.btn_num_bg))
        }

        tvMathBox2.setOnClickListener {
            playHapticFeedback()
            activeBox = 2
            tvMathBox1.setBackgroundColor(getThemeColor(R.color.btn_num_bg))
            tvMathBox2.setBackgroundColor(getThemeColor(R.color.btn_op_bg))
        }

        tvExpression.setOnClickListener {
            if (isEvaluated) {
                isEvaluated = false
            }
        }

        tvExpression.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && isEvaluated) {
                isEvaluated = false
            }
        }

        tvExpression.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val newExpr = s?.toString() ?: ""
                if (expression != newExpr) {
                    expression = newExpr
                    if (isEvaluated) {
                        isEvaluated = false
                    }
                }
            }
        })

        val numberButtons = mapOf(
            R.id.btn0 to "0", R.id.btn1 to "1", R.id.btn2 to "2", R.id.btn3 to "3",
            R.id.btn4 to "4", R.id.btn5 to "5", R.id.btn6 to "6", R.id.btn7 to "7",
            R.id.btn8 to "8", R.id.btn9 to "9"
        )

        for ((id, digit) in numberButtons) {
            findViewById<Button>(id).setOnClickListener { appendToken(digit) }
        }

        // Functions & Constants
        findViewById<Button>(R.id.btnSin).setOnClickListener { appendFunction("sin(") }
        findViewById<Button>(R.id.btnCos).setOnClickListener { appendFunction("cos(") }
        findViewById<Button>(R.id.btnTan).setOnClickListener { appendFunction("tan(") }
        findViewById<Button>(R.id.btnLn).setOnClickListener { appendFunction("ln(") }
        findViewById<Button>(R.id.btnLog).setOnClickListener { appendFunction("log(") }
        findViewById<Button>(R.id.btnSqrt).setOnClickListener { appendFunction("√(") }
        findViewById<Button>(R.id.btnPower).setOnClickListener { appendToken("^") }
        findViewById<Button>(R.id.btnSquare).setOnClickListener { appendToken("^2") }
        findViewById<Button>(R.id.btnFact).setOnClickListener { appendToken("!") }

        findViewById<Button>(R.id.btnPi).setOnClickListener { appendToken("π") }
        findViewById<Button>(R.id.btnE).setOnClickListener { appendToken("e") }
        findViewById<Button>(R.id.btnOpenP).setOnClickListener { appendToken("(") }
        findViewById<Button>(R.id.btnCloseP).setOnClickListener { appendToken(")") }

        // Operators
        findViewById<Button>(R.id.btnPlus).setOnClickListener { appendToken("+") }
        findViewById<Button>(R.id.btnMinus).setOnClickListener { appendToken("−") }
        findViewById<Button>(R.id.btnMultiply).setOnClickListener { appendToken("×") }
        findViewById<Button>(R.id.btnDivide).setOnClickListener { appendToken("÷") }
        findViewById<Button>(R.id.btnMod).setOnClickListener { appendToken(" mod ") }
        findViewById<Button>(R.id.btnDot).setOnClickListener { appendToken(".") }

        // Controls
        findViewById<Button>(R.id.btnClear).setOnClickListener { clearAll() }
        findViewById<Button>(R.id.btnBackspace).setOnClickListener { backspace() }
        findViewById<Button>(R.id.btnEquals).setOnClickListener { calculateResult() }

        // Mode Toggle
        findViewById<Button>(R.id.btnDeg)?.setOnClickListener {
            playHapticFeedback()
            isDegreeMode = !isDegreeMode
            (it as Button).text = if (isDegreeMode) "DEG" else "RAD"
        }

        // New Functions
        findViewById<Button>(R.id.btnBin).setOnClickListener { convertToBinary() }
        findViewById<Button>(R.id.btnPrime).setOnClickListener { primeFactorize() }
        findViewById<Button>(R.id.btnGcd).setOnClickListener { activateMathMode("GCD") }
        findViewById<Button>(R.id.btnLcm).setOnClickListener { activateMathMode("LCM") }
        findViewById<ImageButton>(R.id.btnHistory).setOnClickListener { showHistoryDialog() }
    }

    private fun insertAtCursor(textToInsert: String) {
        val start = maxOf(tvExpression.selectionStart, 0)
        val end = maxOf(tvExpression.selectionEnd, 0)
        val currentText = tvExpression.text.toString()

        val minPos = minOf(start, end)
        val maxPos = maxOf(start, end)

        val newText = currentText.replaceRange(minPos, maxPos, textToInsert)
        expression = newText
        tvExpression.setText(expression)
        tvExpression.setSelection(minPos + textToInsert.length)
    }

    private fun appendToken(token: String) {
        playHapticFeedback()
        if (isMathMode) {
            if (token in listOf("0","1","2","3","4","5","6","7","8","9","-")) {
                val tv = if (activeBox == 1) tvMathBox1 else tvMathBox2
                tv.text = tv.text.toString() + token
            }
            return
        }

        if (isEvaluated) {
            expression = if ("+-×÷^%! GCD  LCM  mod ".contains(token)) currentInput + token else token
            currentInput = ""
            isEvaluated = false
            tvExpression.setText(expression)
            tvExpression.setSelection(expression.length)
        } else {
            insertAtCursor(token)
        }
        updateDisplay()
    }

    private fun appendFunction(func: String) {
        playHapticFeedback()
        if (isEvaluated) {
            expression = func
            currentInput = ""
            isEvaluated = false
            tvExpression.setText(expression)
            tvExpression.setSelection(expression.length)
        } else {
            insertAtCursor(func)
        }
        updateDisplay()
    }

    private fun backspace() {
        playHapticFeedback()
        if (isMathMode) {
            val tv = if (activeBox == 1) tvMathBox1 else tvMathBox2
            val t = tv.text.toString()
            if (t.isNotEmpty()) tv.text = t.dropLast(1)
            return
        }

        if (isEvaluated) {
            isEvaluated = false
        }

        val start = tvExpression.selectionStart
        val end = tvExpression.selectionEnd
        val currentText = tvExpression.text.toString()

        if (start != end) {
            val minPos = minOf(start, end)
            val maxPos = maxOf(start, end)
            expression = currentText.removeRange(minPos, maxPos)
            tvExpression.setText(expression)
            tvExpression.setSelection(minPos)
        } else if (start > 0) {
            val toRemove = if (currentText.substring(0, start).endsWith(" mod ")) 5 else 1
            expression = currentText.removeRange(start - toRemove, start)
            tvExpression.setText(expression)
            tvExpression.setSelection(start - toRemove)
        }
        updateDisplay()
    }

    private fun clearAll() {
        playHapticFeedback()
        if (isMathMode) {
            isMathMode = false
            layoutMathDisplay.visibility = View.GONE
            layoutNormalDisplay.visibility = View.VISIBLE
        }
        expression = ""
        currentInput = "0"
        isEvaluated = false
        updateDisplay()
    }

    private fun calculateResult() {
        playHapticFeedback()
        if (isMathMode) {
            val v1 = tvMathBox1.text.toString().toLongOrNull() ?: 0L
            val v2 = tvMathBox2.text.toString().toLongOrNull() ?: 0L
            val res = if (mathType == "GCD") MathUtils.gcd(v1, v2) else MathUtils.lcm(v1, v2)
            
            isMathMode = false
            layoutMathDisplay.visibility = View.GONE
            layoutNormalDisplay.visibility = View.VISIBLE
            
            currentInput = res.toString()
            expression = "$mathType($v1, $v2)"
            isEvaluated = true
            historyManager.addHistory(expression, currentInput)
            addToHistoryTape(expression, currentInput)
            updateDisplay()
            return
        }

        if (expression.isBlank()) return
        try {
            val res = ExpressionEvaluator(expression, isDegreeMode, lastAns).evaluate()
            lastAns = res
            currentInput = formatResult(res)
            isEvaluated = true
            historyManager.addHistory(expression, currentInput)
            addToHistoryTape(expression, currentInput)
        } catch (e: Exception) {
            currentInput = "Error"
            isEvaluated = true
        }
        updateDisplay()
    }

    private fun formatResult(valRes: Double): String {
        if (valRes.isNaN() || valRes.isInfinite()) return "Error"
        return if (valRes % 1.0 == 0.0 && abs(valRes) < 1e12) {
            valRes.toLong().toString()
        } else {
            val s = String.format("%.8f", valRes).trimEnd('0').trimEnd('.')
            if (s.isBlank()) "0" else s
        }
    }

    private fun updateDisplay() {
        if (tvExpression.text.toString() != expression) {
            val sel = tvExpression.selectionStart
            tvExpression.setText(expression)
            if (sel in 0..expression.length) {
                tvExpression.setSelection(sel)
            }
        }
        tvResult.text = currentInput
    }

    private fun populateHistoryTape() {
        val historyList = historyManager.getHistory()
        for (item in historyList) {
            addToHistoryTape(item.expression, item.result, scroll = false)
        }
        svHistoryTape.post {
            svHistoryTape.fullScroll(View.FOCUS_DOWN)
        }
    }

    private fun addToHistoryTape(expr: String, res: String, scroll: Boolean = true) {
        val itemLayout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(0, 24, 0, 24)
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
            }
        }
        
        val tvExpr = TextView(this).apply {
            text = expr
            setTextColor(getThemeColor(R.color.text_secondary))
            textSize = 18f
            gravity = android.view.Gravity.START
        }
        
        val tvRes = TextView(this).apply {
            text = "= $res"
            setTextColor(getThemeColor(R.color.text_primary))
            textSize = 22f
            gravity = android.view.Gravity.START
        }
        
        itemLayout.addView(tvExpr)
        itemLayout.addView(tvRes)
        
        itemLayout.setOnClickListener {
            expression = expr
            currentInput = res
            isEvaluated = true
            updateDisplay()
        }

        layoutHistoryTape.addView(itemLayout)
        if (scroll) {
            svHistoryTape.postDelayed({
                svHistoryTape.smoothScrollTo(0, layoutHistoryTape.bottom)
            }, 150)
        }
    }

    private fun toggleScientificPanel() {
        isScientificVisible = !isScientificVisible
        if (isScientificVisible) {
            gridScientific.visibility = View.VISIBLE
            svHistoryTape.visibility = View.GONE
        } else {
            gridScientific.visibility = View.GONE
            svHistoryTape.visibility = View.VISIBLE
        }
    }

    override fun dispatchTouchEvent(ev: android.view.MotionEvent?): Boolean {
        if (ev != null && ::gestureDetector.isInitialized) {
            if (ev.actionMasked == android.view.MotionEvent.ACTION_DOWN) {
                val gridScientific = findViewById<View>(R.id.gridScientific)
                val lowerControls = findViewById<View>(R.id.layoutLowerControls)
                
                var topBoundary = Float.MAX_VALUE
                
                if (gridScientific != null && gridScientific.visibility == View.VISIBLE) {
                    val location = IntArray(2)
                    gridScientific.getLocationOnScreen(location)
                    topBoundary = location[1].toFloat()
                } else if (lowerControls != null) {
                    val location = IntArray(2)
                    lowerControls.getLocationOnScreen(location)
                    topBoundary = location[1].toFloat()
                }
                
                isSwipeInControls = ev.rawY >= topBoundary
            }
            if (isSwipeInControls) {
                gestureDetector.onTouchEvent(ev)
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun getAsLong(): Long? {
        if (!isEvaluated && expression.isNotBlank()) {
            calculateResult()
        }
        if (currentInput == "Error") return null
        return currentInput.toDoubleOrNull()?.toLong()
    }

    private fun convertToBinary() {
        playHapticFeedback()
        val num = getAsLong() ?: return
        currentInput = java.lang.Long.toBinaryString(num) + "₂"
        expression = "BIN($num)"
        isEvaluated = true
        updateDisplay()
    }

    private fun primeFactorize() {
        playHapticFeedback()
        val num = getAsLong() ?: return
        if (num <= 1) {
            currentInput = num.toString()
        } else {
            var n = num
            val factors = mutableMapOf<Long, Int>()
            var i = 2L
            while (i * i <= n) {
                while (n % i == 0L) {
                    factors[i] = factors.getOrDefault(i, 0) + 1
                    n /= i
                }
                i++
            }
            if (n > 1) {
                factors[n] = factors.getOrDefault(n, 0) + 1
            }
            
            val resultStr = factors.entries.joinToString(" × ") { entry ->
                val factor = entry.key
                val count = entry.value
                if (count > 1) {
                    val superscripts = count.toString().map { c ->
                        when (c) {
                            '0' -> '⁰'; '1' -> '¹'; '2' -> '²'; '3' -> '³'
                            '4' -> '⁴'; '5' -> '⁵'; '6' -> '⁶'; '7' -> '⁷'
                            '8' -> '⁸'; '9' -> '⁹'; else -> c
                        }
                    }.joinToString("")
                    "$factor$superscripts"
                } else {
                    factor.toString()
                }
            }
            currentInput = resultStr
        }
        expression = "PRIME($num)"
        isEvaluated = true
        updateDisplay()
    }

    private fun activateMathMode(type: String) {
        playHapticFeedback()
        isMathMode = true
        mathType = type
        activeBox = 1
        tvMathPrefix.text = "$type("
        tvMathBox1.text = ""
        tvMathBox2.text = ""
        tvMathBox1.setBackgroundColor(getThemeColor(R.color.btn_op_bg))
        tvMathBox2.setBackgroundColor(getThemeColor(R.color.btn_num_bg))
        
        layoutNormalDisplay.visibility = View.GONE
        layoutMathDisplay.visibility = View.VISIBLE
    }

    private fun showHistoryDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_history, null)
        val dialog = android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val listContainer = dialogView.findViewById<android.widget.LinearLayout>(R.id.dialogHistoryList)
        val btnClear = dialogView.findViewById<Button>(R.id.btnClearHistory)
        val btnClose = dialogView.findViewById<Button>(R.id.btnCloseHistory)

        val historyList = historyManager.getHistory()

        if (historyList.isEmpty()) {
            val emptyTv = TextView(this).apply {
                text = "No history available"
                setTextColor(getThemeColor(R.color.text_secondary))
                textSize = 16f
                gravity = android.view.Gravity.CENTER
                setPadding(0, 40, 0, 40)
            }
            listContainer.addView(emptyTv)
            btnClear.visibility = View.GONE
        } else {
            for (item in historyList) {
                val itemLayout = layoutInflater.inflate(R.layout.item_history, listContainer, false)
                val tvExpr = itemLayout.findViewById<TextView>(R.id.tvHistoryExpression)
                val tvRes = itemLayout.findViewById<TextView>(R.id.tvHistoryResult)

                tvExpr.text = item.expression
                tvRes.text = "= ${item.result}"

                itemLayout.setOnClickListener {
                    expression = item.expression
                    currentInput = item.result
                    isEvaluated = true
                    updateDisplay()
                    dialog.dismiss()
                }
                listContainer.addView(itemLayout)
            }
        }

        btnClear.setOnClickListener {
            android.app.AlertDialog.Builder(this)
                .setTitle("Clear History")
                .setMessage("Are you sure you want to clear all calculation history?")
                .setPositiveButton("Yes") { _, _ ->
                    historyManager.clearHistory()
                    layoutHistoryTape.removeAllViews()
                    dialog.dismiss()
                }
                .setNegativeButton("No", null)
                .show()
        }

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
    
    private fun getThemeColor(resId: Int): Int {
        return androidx.core.content.ContextCompat.getColor(this, resId)
    }

    private fun showTutorialIfNeeded() {
        val sharedPref = getSharedPreferences("AppPrefs", android.content.Context.MODE_PRIVATE)
        val hasSeenTutorial = sharedPref.getBoolean("pref_has_seen_tutorial", false)
        if (!hasSeenTutorial) {
            val root = findViewById<android.view.ViewGroup>(android.R.id.content)
            val tutorialView = layoutInflater.inflate(R.layout.layout_tutorial, root, false)
            root.addView(tutorialView)

            val ivHand = tutorialView.findViewById<android.widget.ImageView>(R.id.ivTutorialHand)
            val btnGotIt = tutorialView.findViewById<android.widget.Button>(R.id.btnTutorialGotIt)

            val animator = android.animation.ObjectAnimator.ofFloat(ivHand, "translationY", 0f, -100f, 0f)
            animator.duration = 1500
            animator.repeatCount = android.animation.ValueAnimator.INFINITE
            animator.start()

            btnGotIt.setOnClickListener {
                animator.cancel()
                root.removeView(tutorialView)
                sharedPref.edit().putBoolean("pref_has_seen_tutorial", true).apply()
            }
        }
    }

    private fun playHapticFeedback() {
        val sharedPref = getSharedPreferences("AppPrefs", android.content.Context.MODE_PRIVATE)
        val hapticEnabled = sharedPref.getBoolean("pref_haptic", true)
        if (hapticEnabled) {
            window.decorView.rootView.performHapticFeedback(
                android.view.HapticFeedbackConstants.KEYBOARD_TAP,
                android.view.HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
            )
        }
    }
}

class ExpressionEvaluator(private val expr: String, private val isDegree: Boolean, private val ans: Double) {
    fun evaluate(): Double {
        val sanitized = expr
            .replace(" mod ", "m")
            .replace(" GCD ", "g")
            .replace(" LCM ", "l")
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("π", Math.PI.toString())
            .replace("e", Math.E.toString())
            .replace("Ans", ans.toString())
            .replace("√", "sqrt")

        return parseExpression(sanitized)
    }

    private fun parseExpression(str: String): Double {
        var localPos = -1
        var localCh = 0

        fun advance() {
            localCh = if (++localPos < str.length) str[localPos].code else -1
        }

        fun match(toMatch: Int): Boolean {
            while (localCh == ' '.code) advance()
            if (localCh == toMatch) {
                advance()
                return true
            }
            return false
        }

        fun parseFactor(): Double {
            if (match('+'.code)) return parseFactor()
            if (match('-'.code)) return -parseFactor()

            var x: Double
            val startPos = localPos
            if (match('('.code)) {
                x = parseExpression(str.substring(localPos))
                // Advance past expression matching
                var level = 1
                while (level > 0 && localPos < str.length) {
                    if (str[localPos] == '(') level++
                    else if (str[localPos] == ')') level--
                    advance()
                }
            } else if ((localCh in '0'.code..'9'.code) || localCh == '.'.code) {
                while ((localCh in '0'.code..'9'.code) || localCh == '.'.code || localCh == 'E'.code || localCh == 'e'.code) advance()
                x = str.substring(startPos, localPos).toDouble()
            } else if (localCh in 'a'.code..'z'.code) {
                while (localCh in 'a'.code..'z'.code) advance()
                val func = str.substring(startPos, localPos)
                x = parseFactor()
                x = when (func) {
                    "sqrt" -> sqrt(x)
                    "sin" -> sin(if (isDegree) Math.toRadians(x) else x)
                    "cos" -> cos(if (isDegree) Math.toRadians(x) else x)
                    "tan" -> tan(if (isDegree) Math.toRadians(x) else x)
                    "log" -> log10(x)
                    "ln" -> ln(x)
                    else -> throw RuntimeException("Unknown function: $func")
                }
            } else {
                throw RuntimeException("Unexpected character: " + localCh.toChar())
            }

            if (match('^'.code)) x = x.pow(parseFactor())
            if (match('!'.code)) {
                var fact = 1.0
                val n = x.toInt()
                for (i in 1..n) fact *= i.toDouble()
                x = fact
            }
            if (match('%'.code)) x /= 100.0

            return x
        }

        fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                if (match('*'.code)) x *= parseFactor()
                else if (match('/'.code)) x /= parseFactor()
                else if (match('m'.code)) x %= parseFactor()
                else if (match('g'.code)) x = MathUtils.gcd(x.toLong(), parseFactor().toLong()).toDouble()
                else if (match('l'.code)) x = MathUtils.lcm(x.toLong(), parseFactor().toLong()).toDouble()
                else return x
            }
        }

        fun parseExpr(): Double {
            var x = parseTerm()
            while (true) {
                if (match('+'.code)) x += parseTerm()
                else if (match('-'.code)) x -= parseTerm()
                else return x
            }
        }

        advance()
        return parseExpr()
    }
}

object MathUtils {
    fun gcd(a: Long, b: Long): Long {
        var num1 = a
        var num2 = b
        while (num2 != 0L) {
            val temp = num2
            num2 = num1 % num2
            num1 = temp
        }
        return abs(num1)
    }

    fun lcm(a: Long, b: Long): Long {
        if (a == 0L || b == 0L) return 0L
        return abs(a * b) / gcd(a, b)
    }
}
