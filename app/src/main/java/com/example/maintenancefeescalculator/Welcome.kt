package com.example.maintenancefeescalculator

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Welcome : AppCompatActivity() {

    private lateinit var buildingAdapter: BuildingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        val dbHelper = DatabaseHelper(this)
        val db = dbHelper.readableDatabase


        val usernameProjection = arrayOf(DatabaseHelper.UsernameEntry.COLUMN_NAME_USERNAME)
        val usernameCursor = db.query(
            DatabaseHelper.UsernameEntry.TABLE_NAME,
            usernameProjection,
            null,
            null,
            null,
            null,
            null
        )

        var username: String? = null

        with(usernameCursor) {
            while (moveToNext()) {
                username = getString(getColumnIndexOrThrow(DatabaseHelper.UsernameEntry.COLUMN_NAME_USERNAME))
            }
        }

        usernameCursor.close()

        buildingAdapter = BuildingAdapter()

        val recyclerView = findViewById<RecyclerView>(R.id.buildingRecyclerView)
        recyclerView.adapter = buildingAdapter

        val welcomeMessage = "Welcome, $username!"
        val welcomeTextView = findViewById<TextView>(R.id.welcomeTextView)
        welcomeTextView.text = welcomeMessage

        val buildingButton = findViewById<Button>(R.id.buildingButton)
        buildingButton.setOnClickListener {
            val intent = Intent(this, BuildingActivity::class.java)
            startActivity(intent)
            }

        val toTableButton = findViewById<Button>(R.id.toTableButton)
        toTableButton.setOnClickListener {
            val intent = Intent(this, DisplayActivity::class.java)
            intent.putExtra("FROM_WELCOME_ACTIVITY", true)
            startActivity(intent)
        }

        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            BuildingActivity.EDIT_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    // Check if building was updated
                    val isBuildingUpdated = data?.getBooleanExtra("IS_BUILDING_UPDATED", false) ?: false
                    if (isBuildingUpdated) {
                        val editedBuildingId = data?.getLongExtra("BUILDING_ID", -1) ?: -1
                        Log.d("ActivityResult", "Edited Building ID: $editedBuildingId")
                        updateBuildingList()
                    }
                }
            }
            BuildingActivity.DELETE_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    // Handle the result for delete
                    updateBuildingList()
                }
            }
        }
    }

    private fun updateBuildingList() {
        val dbHelper = DatabaseHelper(this)
        val buildingList = dbHelper.getBuildingList()

        Log.d("UpdateBuildingList", "Building List Size: ${buildingList.size}")

        // Update the buildingAdapter in WelcomeActivity with the new list
        buildingAdapter.submitList(buildingList)
        buildingAdapter.notifyDataSetChanged() // Notify the adapter that the data set has changed
    }

    override fun onResume() {
        super.onResume()

        // Refresh the building list every time the activity resumes
        updateBuildingList()
    }

    }