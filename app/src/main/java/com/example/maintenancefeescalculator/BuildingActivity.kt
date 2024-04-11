package com.example.maintenancefeescalculator

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.provider.BaseColumns
import android.text.InputFilter
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity



class BuildingActivity : AppCompatActivity(){

    private lateinit var checkBoxElevator: CheckBox
    private lateinit var checkBoxHeating: CheckBox
    private lateinit var checkBoxM2Calc: CheckBox

    companion object {
        const val EDIT_REQUEST_CODE = 1
        const val DELETE_REQUEST_CODE = 2
        const val EDIT_MODE_KEY = "EDIT_MODE"
    }

    private fun saveAddressToDatabase(
        address: String,
        addressNumber: String,
        floors: String,
        flats: String,
        elevatorChecked: Boolean,
        heatingChecked: Boolean,
        m2calcChecked: Boolean
    ): Long {
        val dbHelper = DatabaseHelper(this)
        dbHelper.writableDatabase.use { db ->
            val values = ContentValues().apply {
                put(DatabaseHelper.AddressEntry.COLUMN_NAME_ADDRESS, address)
                put(DatabaseHelper.AddressEntry.COLUMN_NAME_ADDRESS_NUMBER, addressNumber)
                put(DatabaseHelper.AddressEntry.COLUMN_NAME_FLOORS, floors)
                put(DatabaseHelper.AddressEntry.COLUMN_NAME_FLATS, flats)
                put(DatabaseHelper.AddressEntry.COLUMN_NAME_ELEVATOR_CHECKED, elevatorChecked)
                put(DatabaseHelper.AddressEntry.COLUMN_NAME_HEATING_CHECKED, heatingChecked)
                put(DatabaseHelper.AddressEntry.COLUMN_NAME_M2CALC_CHECKED, m2calcChecked)
            }

            return db.insert(DatabaseHelper.AddressEntry.TABLE_NAME, null, values)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_building)
        initAddressEditText()

        checkBoxElevator = findViewById(R.id.checkBoxElevator)
        checkBoxHeating = findViewById(R.id.checkBoxHeating)
        checkBoxM2Calc = findViewById(R.id.checkBoxM2Calc)

        val editMode = intent.getBooleanExtra("EDIT_MODE", false)

        if (editMode) {
            // Autofill fields for editing
            val address = intent.getStringExtra("ADDRESS")
            val addressNumber = intent.getStringExtra("ADDRESS_NUMBER")
            val floors = intent.getStringExtra("FLOORS")
            val flats = intent.getStringExtra("FLATS")
            val elevatorChecked = intent.getBooleanExtra("ELEVATOR_CHECKED", false)
            val heatingChecked = intent.getBooleanExtra("HEATING_CHECKED", false)
            val m2calcChecked = intent.getBooleanExtra("M2CALC_CHECKED", false)

            // Set values to your UI elements accordingly
            findViewById<EditText>(R.id.addressText).setText(address)
            findViewById<EditText>(R.id.addressNumText).setText(addressNumber)
            findViewById<EditText>(R.id.floorsNum).setText(floors)
            findViewById<EditText>(R.id.flatsNum).setText(flats)
            findViewById<CheckBox>(R.id.checkBoxElevator).isChecked = elevatorChecked
            findViewById<CheckBox>(R.id.checkBoxHeating).isChecked = heatingChecked
            findViewById<CheckBox>(R.id.checkBoxM2Calc).isChecked = m2calcChecked
        }

        val nextButton = findViewById<Button>(R.id.nextButton)
        nextButton.setOnClickListener {
            val dbHelper = DatabaseHelper(this)

            val address = findViewById<EditText>(R.id.addressText).text.toString()
            val addressNumber = findViewById<EditText>(R.id.addressNumText).text.toString()
            val floors = findViewById<EditText>(R.id.floorsNum).text.toString()
            val flats = findViewById<EditText>(R.id.flatsNum).text.toString()

            val elevatorChecked = checkBoxElevator.isChecked
            val heatingChecked = checkBoxHeating.isChecked
            val m2calcChecked = checkBoxM2Calc.isChecked

            if (editMode) {
                // If in edit mode, update the existing building
                val buildingId = intent.getLongExtra("BUILDING_ID", -1)
                Log.d("UpdateBuilding", "Building ID to update: $buildingId")

                val isUpdated = updateBuildingInDatabase(buildingId, address, addressNumber, floors, flats, elevatorChecked, heatingChecked, m2calcChecked)
                Log.d("SecondActivity", "Is Building Updated: $isUpdated")

                updateBuildingInDatabase(buildingId, address, addressNumber, floors, flats, elevatorChecked, heatingChecked, m2calcChecked)

                // Return to WelcomeActivity only if a building is updated
                val returnIntent = Intent()
                returnIntent.putExtra("IS_BUILDING_UPDATED", isUpdated)
                returnIntent.putExtra("BUILDING_ID", buildingId)
                setResult(RESULT_OK, returnIntent)
                finish()
                //updateBuildingListInWelcomeActivity()
            } else {
                // If not in edit mode, save a new building
                val buildingId = saveAddressToDatabase(address, addressNumber, floors, flats, elevatorChecked, heatingChecked, m2calcChecked)
                Log.d("InsertBuilding", "Inserted Building ID: $buildingId")

                // Go to ThirdActivity for a new building
                val intent = Intent(this, ApartmentsActivity::class.java)
                intent.putExtra("ELEVATOR_CHECKED", elevatorChecked)
                intent.putExtra("HEATING_CHECKED", heatingChecked)
                intent.putExtra("M2CALC_CHECKED", m2calcChecked)
                intent.putExtra("BUILDING_ID", buildingId)
                startActivity(intent)
            }
        }
    }

    private fun updateBuildingInDatabase(
        buildingId: Long,
        address: String,
        addressNumber: String,
        floors: String,
        flats: String,
        elevatorChecked: Boolean,
        heatingChecked: Boolean,
        m2calcChecked: Boolean
    ): Boolean {
        Log.d("UpdateBuilding", "Building ID: $buildingId")
        Log.d("UpdateBuilding", "Address: $address")
        val dbHelper = DatabaseHelper(this)
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(DatabaseHelper.AddressEntry.COLUMN_NAME_ADDRESS, address)
            put(DatabaseHelper.AddressEntry.COLUMN_NAME_ADDRESS_NUMBER, addressNumber)
            put(DatabaseHelper.AddressEntry.COLUMN_NAME_FLOORS, floors)
            put(DatabaseHelper.AddressEntry.COLUMN_NAME_FLATS, flats)
            put(DatabaseHelper.AddressEntry.COLUMN_NAME_ELEVATOR_CHECKED, elevatorChecked)
            put(DatabaseHelper.AddressEntry.COLUMN_NAME_HEATING_CHECKED, heatingChecked)
            put(DatabaseHelper.AddressEntry.COLUMN_NAME_M2CALC_CHECKED, m2calcChecked)
        }

        Log.d("UpdateBuilding", "Update Query: $values")
        // Update the selection to use the buildingId directly
        val selection = "${BaseColumns._ID} = ?"

        Log.d("UpdateBuilding", "Update Query: $values")
        Log.d("UpdateBuilding", "Selection: $selection")

        val updatedRows = db.update(DatabaseHelper.AddressEntry.TABLE_NAME, values, selection, arrayOf(buildingId.toString()))
        db.close()

        Log.d("UpdateBuilding", "Updated Rows: $updatedRows")

        return updatedRows > 0
    }

    private fun initAddressEditText() {
        val addressEditText = findViewById<EditText>(R.id.addressText)

        // Ορίστε τον τύπο εισαγωγής ως κείμενο
        addressEditText.inputType = InputType.TYPE_CLASS_TEXT

        // Ορίστε έναν InputFilter που επιτρέπει μόνο γράμματα
        val inputFilter = InputFilter { source, start, end, dest, dstart, dend ->
            for (i in start until end) {
                // Αν το γράμμα δεν είναι γράμμα, απορρίπτεται
                if (!Character.isLetter(source[i])) {
                    return@InputFilter ""
                }
            }
            null
        }

        // Εφαρμόστε τον InputFilter στο πεδίο της διεύθυνσης
        addressEditText.filters = arrayOf(inputFilter)
    }

    private fun updateBuildingListInWelcomeActivity() {
        // Fetch the updated building list from the database
        val dbHelper = DatabaseHelper(this)
        val buildingList = dbHelper.getBuildingList()

        // Update the buildingAdapter in WelcomeActivity with the new list
        val welcomeIntent = Intent(this, Welcome::class.java)
        welcomeIntent.putParcelableArrayListExtra("BUILDING_LIST", ArrayList(buildingList))
        setResult(RESULT_OK, welcomeIntent)
    }
}