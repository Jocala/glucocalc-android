package com.jocala.glucocalc

import android.os.Bundle
import android.text.util.Linkify
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.jocala.glucocalc.databinding.ActivityMainBinding
import java.util.Locale

enum class CalcMode { EAG, A1C }

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var mode = CalcMode.A1C
    private var mmol = false
    private var input = ""
    private var result1 = ""
    private var result2 = ""
    // True after "=" was pressed; the next digit starts a fresh entry.
    private var didCompute = false

    private val prompt: String
        get() = when (mode) {
            CalcMode.EAG -> if (mmol) "Enter HbA1c below (mmol/mol)" else "Enter HbA1c below (%)"
            CalcMode.A1C -> if (mmol) "Enter eAG below (mmol/L)" else "Enter eAG below (mg/dl)"
        }

    private val resultLabel1: String
        get() = when (mode) {
            CalcMode.EAG -> "Calculated eAG (mg/dl)"
            CalcMode.A1C -> "NGSP HbA1c (%)"
        }

    private val resultLabel2: String
        get() = when (mode) {
            CalcMode.EAG -> "Calculated eAG (mmol/L)"
            CalcMode.A1C -> "IFCC HbA1c (mmol/mol)"
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.app_name)

        wireKeypad()

        binding.modeGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                mode = if (checkedId == binding.modeEag.id) CalcMode.EAG else CalcMode.A1C
                clearResults()
                updateDisplay()
            }
        }

        binding.ukSwitch.setOnCheckedChangeListener { _, checked ->
            toggleUnits(to = checked)
        }

        updateDisplay()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_help -> {
                showHelp()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun wireKeypad() {
        binding.key0.setOnClickListener { pressDigit("0") }
        binding.key1.setOnClickListener { pressDigit("1") }
        binding.key2.setOnClickListener { pressDigit("2") }
        binding.key3.setOnClickListener { pressDigit("3") }
        binding.key4.setOnClickListener { pressDigit("4") }
        binding.key5.setOnClickListener { pressDigit("5") }
        binding.key6.setOnClickListener { pressDigit("6") }
        binding.key7.setOnClickListener { pressDigit("7") }
        binding.key8.setOnClickListener { pressDigit("8") }
        binding.key9.setOnClickListener { pressDigit("9") }
        binding.keyDot.setOnClickListener { pressDot() }
        binding.keyClear.setOnClickListener { clear() }
        binding.keyBack.setOnClickListener { backspace() }
        binding.keyEquals.setOnClickListener { calculate() }
    }

    private fun pressDigit(d: String) {
        if (didCompute) {
            clear()
            didCompute = false
        }
        if (input == "0") {
            input = d
            updateDisplay()
            return
        }
        if (input.contains(".")) {
            // max 2 decimal places
            val fraction = input.substringAfter('.', "").length
            if (fraction >= 2) return
        } else {
            // max 999.99
            if (input.length >= 3) return
        }
        input += d
        updateDisplay()
    }

    private fun pressDot() {
        if (didCompute) {
            clear()
            didCompute = false
        }
        if (input.isEmpty()) {
            input = "0."
            updateDisplay()
            return
        }
        if (input.contains(".")) return
        input += "."
        updateDisplay()
    }

    private fun backspace() {
        if (input.isNotEmpty()) input = input.dropLast(1)
        updateDisplay()
    }

    private fun clear() {
        input = ""
        clearResults()
        updateDisplay()
    }

    private fun clearResults() {
        result1 = ""
        result2 = ""
        didCompute = false
    }

    private fun calculate() {
        val value = input.toDoubleOrNull()
        if (value == null || value <= 0) {
            showInvalidInput()
            return
        }
        didCompute = true
        computeResults(value)
        updateDisplay()
    }

    // US <-> UK toggle: convert the current input to the other unit system and
    // recompute the results so input and output stay consistent. Results always
    // show both units, so only the input value needs converting.
    private fun toggleUnits(to: Boolean) {
        val old = mmol
        mmol = to
        if (old == to) {
            updateDisplay()
            return
        }
        val value = input.toDoubleOrNull()
        if (value == null || value <= 0) {
            clearResults()
            updateDisplay()
            return
        }
        val converted: Double = when (mode) {
            CalcMode.A1C -> if (to) value / 18 else value * 18
            CalcMode.EAG -> if (to) (value * 10.93) - 23.5 else (value * 0.09148) + 2.152
        }
        input = String.format(Locale.US, "%.2f", converted)
        if (didCompute) computeResults(converted)
        updateDisplay()
    }

    private fun computeResults(value: Double) {
        when (mode) {
            CalcMode.EAG -> {
                val r = GlucocalcMath.eAG(fromA1c = value, mmol = mmol)
                result1 = String.format(Locale.US, "%.2f", r.mgdl)
                result2 = String.format(Locale.US, "%.2f", r.mmolL)
            }
            CalcMode.A1C -> {
                val r = GlucocalcMath.a1c(fromEAG = value, mmol = mmol)
                result1 = String.format(Locale.US, "%.2f", r.ngsp)
                result2 = String.format(Locale.US, "%.2f", r.ifcc)
            }
        }
    }

    private fun updateDisplay() {
        binding.promptText.text = prompt
        binding.inputValue.text = if (input.isEmpty()) "0" else input
        binding.resultLabel1.text = resultLabel1
        binding.resultValue1.text = if (result1.isEmpty()) "0" else result1
        binding.resultLabel2.text = resultLabel2
        binding.resultValue2.text = if (result2.isEmpty()) "0" else result2
    }

    private fun showInvalidInput() {
        AlertDialog.Builder(this)
            .setTitle(R.string.invalid_input)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun showHelp() {
        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 8, 24, 8)
        }

        fun text(string: String, sizeSp: Float, bold: Boolean = false, spacingTop: Int = 0): TextView =
            TextView(this).apply {
                text = string
                textSize = sizeSp
                setTextColor(getColor(android.R.color.black))
                if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
                if (spacingTop > 0) {
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply { topMargin = spacingTop }
                }
            }

        fun section(title: String, vararg lines: String) {
            column.addView(text("$title\n", 16f, bold = true, spacingTop = 16))
            for (line in lines) column.addView(text(line, 14f))
        }

        column.addView(text("Glucocalc — Glucose/HbA1c Calculator", 18f, bold = true))
        column.addView(text("Version ${BuildConfig.VERSION_NAME}", 14f))
        column.addView(text("(c) 2018-2026 jocala", 14f))

        column.addView(TextView(this).apply {
            text = "jocala@jocala.com\nhttps://www.jocala.com"
            textSize = 14f
            autoLinkMask = Linkify.EMAIL_ADDRESSES or Linkify.WEB_URLS
            movementMethod = android.text.method.LinkMovementMethod.getInstance()
            setTextColor(getColor(android.R.color.holo_blue_dark))
            setPadding(0, 8, 0, 0)
        })

        section(
            "Usage",
            "Glucocalc calculates estimated HbA1c and estimated average blood glucose. It accepts numeric entries via the keypad.",
            "UK (IFCC) switch: input/output values in mmol/L and mmol/mol.",
            "Calculate eAG: calculate eAG from input HbA1c value.",
            "Calculate HbA1c: calculate HbA1c from input eAG value."
        )

        section(
            "Terminology",
            "NGSP: National Glycohemoglobin Standardization Program",
            "IFCC: The International Federation of Clinical Chemistry and Laboratory Medicine",
            "mg/dl: milligrams per deciliter (weight)",
            "mmol/L: millimoles per litre (volume)",
            "eAG: estimated average glucose",
            "ADAG: A1c-derived average glucose",
            "HbA1c: % Glycated hemoglobin (NGSP)",
            "HbA1c: mmol/mol Glycated hemoglobin (IFCC)"
        )

        section(
            "Formulas",
            "This software uses the 2008 ADAG Study Group formulas",
            "Compute eAG: (28.7 × A1c) – 46.7",
            "Compute A1c: (eAG + 46.7) / 28.7",
            "mg/dl to mmol/L: mg/dl / 18",
            "mmol/L to mg/dl: mmol/L × 18",
            "NGSP = (0.09148 × IFCC) + 2.152",
            "IFCC = (10.93 × NGSP) - 23.50 (mmol/mol)"
        )

        section(
            "About HbA1c",
            "Glycated hemoglobin (hemoglobin A1c) is a form of hemoglobin that is measured primarily to identify the average plasma glucose concentration over prolonged periods of time.",
            "A high A1c represents poor glucose control.",
            "However, a good HbA1c still hides a history of recent hypoglycemia, or even spikes of hyperglycemia.",
            "Regular blood glucose monitoring is still the best method for the analysis of overall vascular health with respect to blood sugar control."
        )

        val scroll = ScrollView(this)
        scroll.addView(column)

        AlertDialog.Builder(this)
            .setTitle(R.string.help_label)
            .setView(scroll)
            .setPositiveButton("Done", null)
            .show()
    }
}
