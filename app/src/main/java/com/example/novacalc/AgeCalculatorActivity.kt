package com.example.novacalc

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class AgeCalculatorActivity : AppCompatActivity() {

    private lateinit var tvSelectedDate: TextView
    private lateinit var tvAgeMain: TextView
    private lateinit var tvAgeSub: TextView
    private lateinit var tvNextBirthday: TextView
    private lateinit var tvNextBirthdayDay: TextView
    private lateinit var layoutResult: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_age)

        tvSelectedDate = findViewById(R.id.tvSelectedDate)
        tvAgeMain = findViewById(R.id.tvAgeMain)
        tvAgeSub = findViewById(R.id.tvAgeSub)
        tvNextBirthday = findViewById(R.id.tvNextBirthday)
        tvNextBirthdayDay = findViewById(R.id.tvNextBirthdayDay)
        layoutResult = findViewById(R.id.layoutResult)

        findViewById<Button>(R.id.btnSelectDate).setOnClickListener {
            showDatePicker()
        }

        setupNavigation()
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(this, { _, y, m, d ->
            val birthDate = Calendar.getInstance()
            birthDate.set(y, m, d)
            
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            tvSelectedDate.text = sdf.format(birthDate.time)
            
            calculateAge(y, m, d)
            calculateNextBirthday(y, m, d)
            layoutResult.visibility = View.VISIBLE
        }, year, month, day)

        dpd.datePicker.maxDate = System.currentTimeMillis()
        dpd.show()
    }

    private fun calculateAge(year: Int, month: Int, day: Int) {
        val today = Calendar.getInstance()
        val dob = Calendar.getInstance()
        dob.set(year, month, day)

        var years = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
        var months = today.get(Calendar.MONTH) - dob.get(Calendar.MONTH)
        var days = today.get(Calendar.DAY_OF_MONTH) - dob.get(Calendar.DAY_OF_MONTH)

        if (days < 0) {
            months--
            val lastMonth = today.clone() as Calendar
            lastMonth.add(Calendar.MONTH, -1)
            days += lastMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
        }

        if (months < 0) {
            years--
            months += 12
        }

        tvAgeMain.text = "$years Years"
        tvAgeSub.text = "$months Months | $days Days"
    }

    private fun calculateNextBirthday(year: Int, month: Int, day: Int) {
        val today = Calendar.getInstance()
        val nextBDay = Calendar.getInstance()
        nextBDay.set(today.get(Calendar.YEAR), month, day)

        if (nextBDay.before(today) || nextBDay == today) {
            nextBDay.add(Calendar.YEAR, 1)
        }

        val diff = nextBDay.timeInMillis - today.timeInMillis
        val daysLeft = (diff / (1000 * 60 * 60 * 24)).toInt()
        
        val monthsUntil = daysLeft / 30
        val remainingDays = daysLeft % 30

        tvNextBirthday.text = "$monthsUntil Months | $remainingDays Days"
        
        val dayOfWeek = SimpleDateFormat("EEEE", Locale.getDefault()).format(nextBDay.time)
        tvNextBirthdayDay.text = "Next birthday will be on a $dayOfWeek"
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
                    R.id.nav_bmi -> {
                        startActivity(Intent(this, BmiCalculatorActivity::class.java))
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
