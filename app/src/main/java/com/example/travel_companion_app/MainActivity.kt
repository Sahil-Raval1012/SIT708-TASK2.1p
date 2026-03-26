package com.example.travel_companion_app
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
class MainActivity : AppCompatActivity() {
    private lateinit var spinnerCategory: Spinner
    private lateinit var spinnerFrom: Spinner
    private lateinit var spinnerTo: Spinner
    private lateinit var editTextInput: EditText
    private lateinit var buttonConvert: Button
    private lateinit var textViewResult: TextView
    private val categoryUnits = mapOf(
        "Currency"        to listOf("USD", "AUD", "EUR", "JPY", "GBP"),
        "Fuel / Distance" to listOf("mpg", "km/L", "Gallon (US)", "Litre", "Nautical Mile", "Kilometre"),
        "Temperature"     to listOf("Celsius", "Fahrenheit", "Kelvin")
    )
    private val usdRates = mapOf(
        "USD" to 1.0,
        "AUD" to 1.55,
        "EUR" to 0.92,
        "JPY" to 148.50,
        "GBP" to 0.78
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        spinnerCategory  = findViewById(R.id.spinnerCategory)
        spinnerFrom      = findViewById(R.id.spinnerFrom)
        spinnerTo        = findViewById(R.id.spinnerTo)
        editTextInput    = findViewById(R.id.editTextInput)
        buttonConvert    = findViewById(R.id.buttonConvert)
        textViewResult   = findViewById(R.id.textViewResult)
        setupCategorySpinner()
        buttonConvert.setOnClickListener {
            performConversion()
        }
    }
    private fun setupCategorySpinner() {
        val categories = categoryUnits.keys.toList()
        val adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter

        spinnerCategory.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>, view: View?, pos: Int, id: Long
                ) {
                    val chosen = categories[pos]
                    updateUnitSpinners(chosen)
                    textViewResult.text = "Enter a value"
                }
                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
    }
    private fun updateUnitSpinners(category: String) {
        val units = categoryUnits[category] ?: return

        val fromAdapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_item, units)
        fromAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item)
        spinnerFrom.adapter = fromAdapter

        val toAdapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_item, units)
        toAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item)
        spinnerTo.adapter = toAdapter
    }
    private fun performConversion() {
        val inputStr = editTextInput.text.toString().trim()

        if (inputStr.isEmpty()) {
            Toast.makeText(this,
                "Please enter a number.", Toast.LENGTH_SHORT).show()
            return
        }
        val value = inputStr.toDoubleOrNull()
        if (value == null) {
            Toast.makeText(this,
                "Invalid input", Toast.LENGTH_SHORT).show()
            return
        }
        val category = spinnerCategory.selectedItem.toString()
        val fromUnit = spinnerFrom.selectedItem.toString()
        val toUnit   = spinnerTo.selectedItem.toString()
        if (fromUnit == toUnit) {
            textViewResult.text = "$value $fromUnit (Identity Conversions)"
            Toast.makeText(this,
                "Same unit selected.", Toast.LENGTH_SHORT).show()
            return
        }
        if (category != "Temperature" && value < 0) {
            Toast.makeText(this,
                "Negative values is not valid for fuel and currency $category.",
                Toast.LENGTH_SHORT).show()
            return
        }

        val result = convert(category, fromUnit, toUnit, value)
        val formatted = "%.4f".format(result)
        textViewResult.text = "$value $fromUnit  =  $formatted $toUnit"
    }

    private fun convert(
        category: String, from: String, to: String, value: Double
    ): Double {
        return when (category) {
            "Currency"        -> convertCurrency(from, to, value)
            "Fuel / Distance" -> convertFuel(from, to, value)
            "Temperature"     -> convertTemperature(from, to, value)
            else -> throw IllegalArgumentException("Unknown category")
        }
    }
    private fun convertCurrency(from: String, to: String, value: Double): Double {
        val fromRate = usdRates[from]!!
        val toRate   = usdRates[to]!!
        val inUsd    = value / fromRate
        return inUsd * toRate
    }
    private fun convertFuel(from: String, to: String, value: Double): Double {
        val base = toFuelBase(from, value)
        return fromFuelBase(to, base)
    }
    private fun toFuelBase(unit: String, value: Double): Double = when (unit) {
        "mpg"          -> value * 0.425
        "km/L"         -> value
        "Gallon (US)"  -> value * 3.785
        "Litre"        -> value
        "Nautical Mile"-> value * 1.852
        "Kilometre"    -> value
        else -> throw IllegalArgumentException("Unknown unit: $unit")
    }
    private fun fromFuelBase(unit: String, base: Double): Double = when (unit) {
        "mpg"          -> base / 0.425
        "km/L"         -> base
        "Gallon (US)"  -> base / 3.785
        "Litre"        -> base
        "Nautical Mile"-> base / 1.852
        "Kilometre"    -> base
        else -> throw IllegalArgumentException("Unknown unit: $unit")
    }
    private fun convertTemperature(from: String, to: String, value: Double): Double {
        val celsius = toCelsius(from, value)
        return fromCelsius(to, celsius)
    }
    private fun toCelsius(unit: String, value: Double): Double = when (unit) {
        "Celsius"    -> value
        "Fahrenheit" -> (value - 32) / 1.8
        "Kelvin"     -> value - 273.15
        else -> throw IllegalArgumentException("Unknown unit: $unit")
    }
    private fun fromCelsius(unit: String, celsius: Double): Double = when (unit) {
        "Celsius"    -> celsius
        "Fahrenheit" -> (celsius * 1.8) + 32
        "Kelvin"     -> celsius + 273.15
        else -> throw IllegalArgumentException("Unknown unit: $unit")
    }
}