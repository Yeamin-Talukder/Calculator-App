package com.example.novacalc

import android.content.Context
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        val switchHaptic = findViewById<Switch>(R.id.switchHaptic)
        val switchDarkMode = findViewById<Switch>(R.id.switchDarkMode)
        val sharedPref = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        
        switchHaptic.isChecked = sharedPref.getBoolean("pref_haptic", true)
        // Default to dark mode if not set, depending on system default. Let's assume true for this app context.
        switchDarkMode.isChecked = sharedPref.getBoolean("pref_dark_mode", true)

        switchHaptic.setOnCheckedChangeListener { _, isChecked ->
            sharedPref.edit().putBoolean("pref_haptic", isChecked).apply()
        }
        
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            sharedPref.edit().putBoolean("pref_dark_mode", isChecked).apply()
            val mode = if (isChecked) {
                androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
            } else {
                androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
            }
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(mode)
        }
    }
}
