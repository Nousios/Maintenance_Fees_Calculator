package com.example.maintenancefeescalculator

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class Username : AppCompatActivity() {
    private lateinit var usernameEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPref = getPreferences(Context.MODE_PRIVATE)
        val usernameSet = sharedPref.getBoolean("USERNAME_SET", false)

        if (usernameSet) {

            startNextActivity()
        } else {

            setContentView(R.layout.activity_username)
            usernameEditText = findViewById(R.id.usernameText)
            val submitButton = findViewById<Button>(R.id.submitButton)
            submitButton.setOnClickListener {
                val username = usernameEditText.text.toString()

                if (username.isNotBlank()) {

                    saveUsernameToDatabase(username)

                    with(sharedPref.edit()) {
                        putBoolean("USERNAME_SET", true)
                        apply()
                    }
                    startNextActivity(username)
                }
            }
        }
    }

    private fun saveUsernameToDatabase(username: String) {
        val dbHelper = DatabaseHelper(this)
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(DatabaseHelper.UsernameEntry.COLUMN_NAME_USERNAME, username)
        }

        db.insert(DatabaseHelper.UsernameEntry.TABLE_NAME, null, values)
    }

    private fun startNextActivity(username: String? = null) {
        val intent = Intent(this, Welcome::class.java)
        if (username != null) {
            intent.putExtra("USERNAME", username)
        }
        startActivity(intent)
        finish()
    }
}