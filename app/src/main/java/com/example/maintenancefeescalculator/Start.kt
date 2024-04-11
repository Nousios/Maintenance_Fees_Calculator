package com.example.maintenancefeescalculator

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity


class Start : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        val start = findViewById<Button>(R.id.start)
        start.setOnClickListener {
            val Intent = Intent(this,Username::class.java)
            startActivity(Intent)
        }
    }
}