package com.example.novacalc

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class BmiCalculatorActivity : AppCompatActivity() {

    private lateinit var etHeight: EditText
    private lateinit var etWeight: EditText
    private lateinit var tvBmiScore: TextView
    private lateinit var tvBmiCategory: TextView
    private lateinit var tvBmiDescription: TextView
    private lateinit var layoutResult: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bmi)

        etHeight = findViewById(R.id.etHeight)
        etWeight = findViewById(R.id.etWeight)
        tvBmiScore = findViewById(R.id.tvBmiScore)
        tvBmiCategory = findViewById(R.id.tvBmiCategory)
        tvBmiDescription = findViewById(R.id.tvBmiDescription)
        layoutResult = findViewById(R.id.layoutResult)

        findViewById<Button>(R.id.btnCalculateBmi).setOnClickListener {
            calculateBmi()
        }

        setupNavigation()
    }

    private fun calculateBmi() {
        val hStr = etHeight.text.toString()
        val wStr = etWeight.text.toString()

        if (hStr.isEmpty() || wStr.isEmpty()) {
            Toast.makeText(this, "Please enter height and weight", Toast.LENGTH_SHORT).show()
            return
        }

        val heightCm = hStr.toDoubleOrNull() ?: 0.0
        val weightKg = wStr.toDoubleOrNull() ?: 0.0

        if (heightCm > 0 && weightKg > 0) {
            val heightM = heightCm / 100.0
            val bmi = weightKg / (heightM * heightM)
            
            tvBmiScore.text = String.format("%.1f", bmi)
            
            val (category, description) = when {
                bmi < 18.5 -> "Underweight" to "You are underweight. Consider consulting a nutritionist."
                bmi < 25.0 -> "Normal Weight" to "You have a healthy body weight. Keep it up!"
                bmi < 30.0 -> "Overweight" to "You are overweight. Regular exercise and a balanced diet may help."
                else -> "Obese" to "You are in the obesity category. It's recommended to consult a doctor."
            }
            
            tvBmiCategory.text = category
            tvBmiDescription.text = description
            layoutResult.visibility = View.VISIBLE
        } else {
            Toast.makeText(this, "Please enter valid values", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupNavigation() {
        findViewById<ImageButton>(R.id.btnNavMenu).setOnClickListener { view ->
            val popup = PopupMenu(this, view)
            popup.menuInflater.inflate(R.menu.nav_menu, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.nav_scientific -> {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                        true
                    }
                    R.id.nav_age -> {
                        startActivity(Intent(this, AgeCalculatorActivity::class.java))
                        finish()
                        true
                    }
                    R.id.nav_settings -> {
                        startActivity(Intent(this, SettingsActivity::class.java))
                        true
                    }
                    R.id.nav_about -> {
                        startActivity(Intent(this, AboutActivity::class.java))
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }
}
