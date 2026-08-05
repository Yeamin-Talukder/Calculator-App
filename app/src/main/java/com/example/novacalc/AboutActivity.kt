package com.example.novacalc

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val btnBack = findViewById<ImageButton>(R.id.btnBackAbout)
        btnBack.setOnClickListener {
            finish()
        }

        val btnGithub = findViewById<Button>(R.id.btnGithub)
        btnGithub.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Yeamin-Talukder"))
            startActivity(intent)
        }
    }
}
