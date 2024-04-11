package com.example.maintenancefeescalculator

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.BaseColumns
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalculateActivity : AppCompatActivity(), OnOkButtonClickListener,
    HeatingUnitsFragment.OnBackPressedListener {

    private var elevatorChecked: Boolean = false
    private var heatingChecked: Boolean = false
    private var m2calcChecked: Boolean = false
    var buildingId: Long = -1
    private var apartmentId: Long = -1
    private var totalHeatingUnits: Float = 0.0f
    private var huButtonClicked: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculate)

        elevatorChecked = intent.getBooleanExtra("ELEVATOR_CHECKED", false)
        heatingChecked = intent.getBooleanExtra("HEATING_CHECKED", false)
        m2calcChecked = intent.getBooleanExtra("M2CALC_CHECKED",false)
        buildingId = intent.getLongExtra("BUILDING_ID", -1)
        apartmentId = intent.getLongExtra("APARTMENT_ID",-1)

        val currentMonthYear = getCurrentMonthYear()

        val infoTotalButton: Button = findViewById(R.id.infoTotal)
        infoTotalButton.setOnClickListener {
            showInfoMessage("Insert the total expenses of the building like electricity, cleaning, etc.")
        }

        val infoElevatorButton: Button = findViewById(R.id.infoElevator)
        infoElevatorButton.setOnClickListener {
            showInfoMessage("Insert the total expenses of the elevator maintenance, if any.")
        }

        val infoHeatingButton: Button = findViewById(R.id.infoHeating)
        infoHeatingButton.setOnClickListener {
            showInfoMessage("Insert the total heating expenses.")
        }

        val huButton: Button = findViewById(R.id.huButton)
        huButton.setOnClickListener {

            huButtonClicked = true

            val buttonCalculate: Button = findViewById(R.id.buttonCalculate)
            val infoTotal: TextView = findViewById(R.id.infoTotal)
            val infoElevator: TextView = findViewById(R.id.infoElevator)
            val infoHeating: TextView = findViewById(R.id.infoHeating)
            val huButton : Button = findViewById(R.id.huButton)

            buttonCalculate.visibility = View.GONE
            infoTotal.visibility = View.GONE
            infoElevator.visibility = View.GONE
            infoHeating.visibility = View.GONE
            huButton.visibility = View.GONE

            val newFragment = HeatingUnitsFragment().apply {
                arguments = Bundle().apply {
                    putLong("BUILDING_ID", buildingId)
                }
            }

            newFragment.setOnOkButtonClickListener(this)

            supportFragmentManager.beginTransaction().apply {
                replace(R.id.fragment_container, newFragment)
                addToBackStack(null)
                commit()
            }
        }

        val buttonCalculate: Button = findViewById(R.id.buttonCalculate)
        buttonCalculate.setOnClickListener {

            if (!huButtonClicked && heatingChecked ) {
                // Show a message or handle the case where huButton was not clicked and m2calc is not checked
                Snackbar.make(findViewById(R.id.calcActivity), "Please add the heating units before the calculation.", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val totalEditText = findViewById<EditText>(R.id.inputTotal)
            val elevatorEditText = findViewById<EditText>(R.id.inputElevator)
            val heatingEditText = findViewById<EditText>(R.id.inputHeating)
            val heatingUnitsButton = findViewById<Button>(R.id.huButton)

            val isElevatorFieldEmpty = if (elevatorEditText.isFocusable && elevatorEditText.isClickable) {
                val empty = elevatorEditText.text.isNullOrEmpty()
                Log.d("FieldStatus", "Elevator field empty: $empty")
                empty
            } else {
                Log.d("FieldStatus", "Elevator field not clickable or focusable")
                false
            }

            val isHeatingFieldEmpty = if (heatingEditText.isFocusable && heatingEditText.isClickable) {
                val empty = heatingEditText.text.isNullOrEmpty()
                Log.d("FieldStatus", "Heating field empty: $empty")
                empty
            } else {
                Log.d("FieldStatus", "Heating field not clickable or focusable")
                false
            }

            val isHuButtonClickable = if (heatingUnitsButton.isClickable) true else false

            if (totalEditText.text.isNullOrEmpty() || isElevatorFieldEmpty || isHeatingFieldEmpty ) {
                // Show a message to the user indicating that all fields must be filled
                Snackbar.make(findViewById(R.id.calcActivity), "All fields must be filled", Snackbar.LENGTH_SHORT).show()
            } else {
                val dbHelper = DatabaseHelper(this)
                val db = dbHelper.readableDatabase
                calculateTotalHeatingUnits()
                val projection = arrayOf(
                    "${DatabaseHelper.ApartmentsEntry.TABLE_NAME}.${DatabaseHelper.ApartmentsEntry.COLUMN_NAME_NAME}",
                    "${DatabaseHelper.ApartmentsEntry.TABLE_NAME}.${DatabaseHelper.ApartmentsEntry.COLUMN_NAME_SURNAME}",
                    "${DatabaseHelper.ApartmentsEntry.TABLE_NAME}.${DatabaseHelper.ApartmentsEntry.COLUMN_NAME_FLOOR}",
                    "${DatabaseHelper.ApartmentsEntry.TABLE_NAME}.${DatabaseHelper.ApartmentsEntry.COLUMN_NAME_PHONE}",
                    "${DatabaseHelper.ApartmentsEntry.TABLE_NAME}.${DatabaseHelper.ApartmentsEntry.COLUMN_NAME_EMAIL}",
                    "${DatabaseHelper.ApartmentsEntry.TABLE_NAME}.${DatabaseHelper.ApartmentsEntry.COLUMN_NAME_SQ_METERS}",
                    "${DatabaseHelper.ApartmentsEntry.TABLE_NAME}.${DatabaseHelper.ApartmentsEntry.COLUMN_NAME_SHARED_FEES}",
                    "${DatabaseHelper.ApartmentsEntry.TABLE_NAME}.${DatabaseHelper.ApartmentsEntry.COLUMN_NAME_ELEVATOR_FEES}",
                    "${DatabaseHelper.ApartmentsEntry.TABLE_NAME}.${DatabaseHelper.ApartmentsEntry.COLUMN_NAME_HEATING_UNITS}",
                    "${DatabaseHelper.AddressEntry.TABLE_NAME}.${DatabaseHelper.AddressEntry.COLUMN_NAME_ADDRESS}",
                    "${DatabaseHelper.AddressEntry.TABLE_NAME}.${DatabaseHelper.AddressEntry.COLUMN_NAME_ADDRESS_NUMBER}",
                    "${DatabaseHelper.AddressEntry.TABLE_NAME}.${DatabaseHelper.AddressEntry.COLUMN_NAME_FLOORS}",
                    "${DatabaseHelper.AddressEntry.TABLE_NAME}.${DatabaseHelper.AddressEntry.COLUMN_NAME_FLATS}",
                    "${DatabaseHelper.AddressEntry.TABLE_NAME}.${DatabaseHelper.AddressEntry.COLUMN_NAME_TOTAL_SQ_METERS}",
                    "${DatabaseHelper.AddressEntry.TABLE_NAME}.${DatabaseHelper.AddressEntry.COLUMN_NAME_ELEVATOR_CHECKED}",
                    "${DatabaseHelper.AddressEntry.TABLE_NAME}.${DatabaseHelper.AddressEntry.COLUMN_NAME_HEATING_CHECKED}",
                    "${DatabaseHelper.AddressEntry.TABLE_NAME}.${DatabaseHelper.AddressEntry.COLUMN_NAME_M2CALC_CHECKED}"
                )

                // Define the selection (WHERE clause)
                val selection =
                    "${DatabaseHelper.ApartmentsEntry.TABLE_NAME}.${DatabaseHelper.ApartmentsEntry.COLUMN_NAME_BUILDING_ID} = ?"

                // Define the selection arguments
                val selectionArgs = arrayOf(buildingId.toString())

                // Define the JOIN clause
                val join =
                    "${DatabaseHelper.ApartmentsEntry.TABLE_NAME} INNER JOIN ${DatabaseHelper.AddressEntry.TABLE_NAME} ON ${DatabaseHelper.ApartmentsEntry.TABLE_NAME}.${DatabaseHelper.ApartmentsEntry.COLUMN_NAME_BUILDING_ID} = ${DatabaseHelper.AddressEntry.TABLE_NAME}.${BaseColumns._ID}"

                // Perform the query
                val cursor = db.query(
                    join,                        // The table to query (joined tables)
                    projection,                  // The array of columns to return
                    selection,                   // The columns for the WHERE clause
                    selectionArgs,               // The values for the WHERE clause
                    null,                        // don't group the rows
                    null,                        // don't filter by row groups
                    null                         // don't order the rows
                )

                val generalExpenses =
                    findViewById<EditText>(R.id.inputTotal).text.toString().toDoubleOrNull() ?: 0.0
                val elevatorExpenses =
                    findViewById<EditText>(R.id.inputElevator).text.toString().toDoubleOrNull()
                        ?: 0.0
                val heatingExpenses =
                    findViewById<EditText>(R.id.inputHeating).text.toString().toDoubleOrNull()
                        ?: 0.0

                // Iterate over the cursor to get apartment details
                cursor?.use { cursor ->
                    while (cursor.moveToNext()) {
                        val apartmentName =
                            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ApartmentsEntry.COLUMN_NAME_NAME))
                        val apartmentSurname =
                            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ApartmentsEntry.COLUMN_NAME_SURNAME))
                        val apartmentFloor =
                            cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.ApartmentsEntry.COLUMN_NAME_FLOOR))
                        val apartmentPhone =
                            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ApartmentsEntry.COLUMN_NAME_PHONE))
                        val apartmentEmail =
                            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ApartmentsEntry.COLUMN_NAME_EMAIL))
                        val apartmentSqMeters =
                            cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.ApartmentsEntry.COLUMN_NAME_SQ_METERS))
                        val apartmentSharedFees =
                            cursor.getFloat(cursor.getColumnIndexOrThrow(DatabaseHelper.ApartmentsEntry.COLUMN_NAME_SHARED_FEES))
                        val apartmentElevatorFees =
                            cursor.getFloat(cursor.getColumnIndexOrThrow(DatabaseHelper.ApartmentsEntry.COLUMN_NAME_ELEVATOR_FEES))
                        val personalHeatingUnits =
                            cursor.getFloat(cursor.getColumnIndexOrThrow(DatabaseHelper.ApartmentsEntry.COLUMN_NAME_HEATING_UNITS))
                        val buildingAddress =
                            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.AddressEntry.COLUMN_NAME_ADDRESS))
                        val buildingNumber =
                            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.AddressEntry.COLUMN_NAME_ADDRESS_NUMBER))
                        val buildingFloors =
                            cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.AddressEntry.COLUMN_NAME_FLOORS))
                        val buildingFlats =
                            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.AddressEntry.COLUMN_NAME_FLATS))
                        val buildingTotalSqMeters =
                            cursor.getFloat(cursor.getColumnIndexOrThrow(DatabaseHelper.AddressEntry.COLUMN_NAME_TOTAL_SQ_METERS))
                        val buildingElevatorChecked =
                            cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.AddressEntry.COLUMN_NAME_ELEVATOR_CHECKED)) == 1
                        val buildingHeatingChecked =
                            cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.AddressEntry.COLUMN_NAME_HEATING_CHECKED)) == 1
                        val buildingTotalHeatingUnits = totalHeatingUnits
                        val buildingM2CalcChecked =
                            cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.AddressEntry.COLUMN_NAME_M2CALC_CHECKED)) == 1

                        val generalFeesSum = calculateGeneralFees(
                            generalExpenses,
                            buildingTotalSqMeters,
                            apartmentSqMeters,
                            buildingM2CalcChecked,
                            apartmentSharedFees
                        )

                        val elevatorFeesSum = calculateΕlevatorFees(
                            elevatorExpenses,
                            buildingTotalSqMeters,
                            apartmentSqMeters,
                            apartmentFloor,
                            buildingFloors,
                            apartmentElevatorFees,
                            buildingM2CalcChecked,
                            buildingElevatorChecked
                        )

                        Log.d("HeatingFees", "Heating Expenses: $heatingExpenses")
                        Log.d("HeatingFees", "Building Total Heating Units: $buildingTotalHeatingUnits")
                        Log.d("HeatingFees", "Personal Heating Units: $personalHeatingUnits")
                        Log.d("HeatingFees", "Building Heating Checked: $buildingHeatingChecked")

                        val heatingFeesSum = calculateHeatingFees(
                            heatingExpenses,
                            buildingTotalHeatingUnits,
                            personalHeatingUnits,
                            buildingHeatingChecked
                        )
                        val totalFees = DecimalFormat("#.##").format(
                            generalFeesSum.replace(",", ".").toFloat() +
                                    elevatorFeesSum.replace(",", ".").toFloat() +
                                    heatingFeesSum.replace(",", ".").toFloat()
                        )

                        savePaymentsToDatabase(
                            apartmentName,
                            apartmentSurname,
                            apartmentPhone,
                            totalFees.toFloat(),
                            generalFeesSum.toFloat(),
                            elevatorFeesSum.toFloat(),
                            heatingFeesSum.toFloat()
                        )

                    }
                }

                // Call the saveExpensesToDatabase function
                saveExpensesToDatabase(buildingId)

                // Call the copyAddress function
                dbHelper.copyAddress(buildingId)

                // Call the copyPayments function
                dbHelper.copyPayments(apartmentId)
                val numApartments = getNumApartmentsForBuilding(this, buildingId)
                val intent = Intent(this, DisplayActivity::class.java)
                intent.putExtra("NUM_APARTMENTS", numApartments)
                startActivity(intent)
            }
        }

        updateUIForElevator(elevatorChecked)
        updateUIForHeating(heatingChecked)
        updateUIForHeatingUnits(heatingChecked)
    }

    private fun getCurrentMonthYear(): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    private fun updateUIForElevator(elevatorChecked: Boolean) {
        val inputElevator = findViewById<TextView>(R.id.inputElevator)

        if (elevatorChecked) {
            inputElevator.visibility = View.VISIBLE
            inputElevator.isFocusable = true
            inputElevator.isClickable = true
        } else {
            inputElevator.visibility = View.VISIBLE
            inputElevator.isFocusable = false
            inputElevator.isClickable = false
            inputElevator.setBackgroundResource(R.drawable.grayed_rounded_text_background)
        }
    }

    private fun updateUIForHeating(heatingChecked: Boolean) {
        val inputHeating = findViewById<TextView>(R.id.inputHeating)

        if (heatingChecked) {
            inputHeating.visibility = View.VISIBLE
            inputHeating.isFocusable = true
            inputHeating.isClickable = true
        } else {
            inputHeating.visibility = View.VISIBLE
            inputHeating.isFocusable = false
            inputHeating.isClickable = false
            inputHeating.setBackgroundResource(R.drawable.grayed_rounded_text_background)
        }
    }

    private fun updateUIForHeatingUnits(heatingChecked: Boolean) {
        val heatingUnits = findViewById<Button>(R.id.huButton)

        if (heatingChecked) {
            heatingUnits.visibility = View.VISIBLE
            heatingUnits.isFocusable = true
            heatingUnits.isClickable = true
        } else {
            heatingUnits.visibility = View.VISIBLE
            heatingUnits.isFocusable = false
            heatingUnits.isClickable = false
            heatingUnits.setBackgroundResource(R.drawable.grayed_rounded_text_background)
        }
    }

    private fun showInfoMessage(message: String) {

        Snackbar.make(findViewById(R.id.calcActivity), message, Snackbar.LENGTH_SHORT).show()
    }

    private fun saveExpensesToDatabase(buildingId: Long) {
        val totalExpensesEditText = findViewById<EditText>(R.id.inputTotal)
        val elevatorExpensesEditText = findViewById<EditText>(R.id.inputElevator)
        val heatingExpensesEditText = findViewById<EditText>(R.id.inputHeating)

        val totalExpensesInput = totalExpensesEditText.text.toString().toFloatOrNull() ?: 0.0f
        val elevatorExpensesInput = elevatorExpensesEditText.text.toString().toFloatOrNull() ?: 0.0f
        val heatingExpensesInput = heatingExpensesEditText.text.toString().toFloatOrNull() ?: 0.0f
        val currentMonthYearInput = getCurrentMonthYear()

        val dbHelper = DatabaseHelper(this)
        val db = dbHelper.writableDatabase // your database reference

        val values = ContentValues().apply {
            put(DatabaseHelper.TotalExpensesEntry.COLUMN_NAME_GENERAL_EXPENSES, totalExpensesInput)
            put(DatabaseHelper.TotalExpensesEntry.COLUMN_NAME_ELEVATOR_EXPENSES, elevatorExpensesInput)
            put(DatabaseHelper.TotalExpensesEntry.COLUMN_NAME_HEATING_EXPENSES, heatingExpensesInput)
            put(DatabaseHelper.TotalExpensesEntry.COLUMN_NAME_MONTH_YEAR, currentMonthYearInput)
            put(DatabaseHelper.TotalExpensesEntry.COLUMN_NAME_BUILDING_ID, buildingId) // Add buildingId
            // Add other columns if needed
        }

        // Insert the values into the database
        db.insert(DatabaseHelper.TotalExpensesEntry.TABLE_NAME, null, values)

    }

    private fun savePaymentsToDatabase(apartmentName: String, apartmentSurname: String, apartmentPhone: String, totalFees: Float, generalFees: Float, elevatorFees: Float, heatingFees: Float) {
        val dbHelper = DatabaseHelper(this)
        val db = dbHelper.writableDatabase
        val currentMonthYear = getCurrentMonthYear()

        val values = ContentValues().apply {
            put(DatabaseHelper.TotalPaymentsEntry.COLUMN_NAME_NAME, apartmentName)
            put(DatabaseHelper.TotalPaymentsEntry.COLUMN_NAME_SURNAME, apartmentSurname)
            put(DatabaseHelper.TotalPaymentsEntry.COLUMN_NAME_PHONE, apartmentPhone)
            put(DatabaseHelper.TotalPaymentsEntry.COLUMN_NAME_MONTH_YEAR, currentMonthYear)
            put(DatabaseHelper.TotalPaymentsEntry.COLUMN_NAME_TOTAL_FEES, totalFees)
            put(DatabaseHelper.TotalPaymentsEntry.COLUMN_NAME_GENERAL_PAY_FEES, generalFees)
            put(DatabaseHelper.TotalPaymentsEntry.COLUMN_NAME_ELEVATOR_PAY_FEES, elevatorFees)
            put(DatabaseHelper.TotalPaymentsEntry.COLUMN_NAME_HEATING_PAY_FEES, heatingFees)
            put(DatabaseHelper.TotalPaymentsEntry.COLUMN_NAME_APARTMENTS_ID, apartmentId)
        }

        db.insert(DatabaseHelper.TotalPaymentsEntry.TABLE_NAME, null, values)
    }

    override fun onOkButtonClick(dbHelper: DatabaseHelper) {
        val db = dbHelper.writableDatabase


        // Make views visible again
        val buttonCalculate = findViewById<Button>(R.id.buttonCalculate)
        val infoTotal = findViewById<TextView>(R.id.infoTotal)
        val infoElevator = findViewById<TextView>(R.id.infoElevator)
        val infoHeating = findViewById<TextView>(R.id.infoHeating)
        val huButton = findViewById<Button>(R.id.huButton)

        buttonCalculate.visibility = View.VISIBLE
        infoTotal.visibility = View.VISIBLE
        infoElevator.visibility = View.VISIBLE
        infoHeating.visibility = View.VISIBLE
        huButton.visibility = View.VISIBLE

        // Retrieve the list of entered heating units values from the database
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as HeatingUnitsFragment?
        fragment?.let { heatingUnitsFragment ->
            heatingUnitsFragment.apartmentList.forEach { apartment ->
                // Update the database with the entered heating units value for each apartment
                val values = ContentValues().apply {
                    put(DatabaseHelper.ApartmentsEntry.COLUMN_NAME_HEATING_UNITS, apartment.heatingUnits)
                }
                val selection = "${DatabaseHelper.ApartmentsEntry.COLUMN_NAME_NAME} = ? AND ${DatabaseHelper.ApartmentsEntry.COLUMN_NAME_SURNAME} = ?"
                val selectionArgs = arrayOf(apartment.apartmentName, apartment.apartmentSurname)
                db.update(DatabaseHelper.ApartmentsEntry.TABLE_NAME, values, selection, selectionArgs)
            }
        }

        // Close the fragment
        supportFragmentManager.popBackStack()
    }

    fun getNumApartmentsForBuilding(context: Context, buildingId: Long): Int {
        val dbHelper = DatabaseHelper(context)
        val db = dbHelper.readableDatabase

        // Define the columns you want to fetch
        val projection = arrayOf(
            "COUNT(*)"
        )

        // Define the selection (WHERE clause)
        val selection = "${DatabaseHelper.ApartmentsEntry.COLUMN_NAME_BUILDING_ID} = ?"

        // Define the selection arguments
        val selectionArgs = arrayOf(buildingId.toString())

        // Perform the query
        val cursor = db.query(
            DatabaseHelper.ApartmentsEntry.TABLE_NAME,  // The table to query
            projection,                                  // The array of columns to return
            selection,                                   // The columns for the WHERE clause
            selectionArgs,                               // The values for the WHERE clause
            null,                                        // don't group the rows
            null,                                        // don't filter by row groups
            null                                         // don't order the rows
        )

        var numApartments = 0

        cursor?.use { cursor ->
            if (cursor.moveToFirst()) {
                numApartments = cursor.getInt(0) // Get the count from the first column (index 0)
            }
        }

        // Close the cursor and database
        cursor?.close()
        dbHelper.close()

        return numApartments
    }

    private fun calculateTotalHeatingUnits() {
        totalHeatingUnits = 0.0f // Reset the total
        val dbHelper = DatabaseHelper(this)
        val db = dbHelper.readableDatabase

        // Define the columns you want to fetch
        val projection = arrayOf(
            DatabaseHelper.ApartmentsEntry.COLUMN_NAME_HEATING_UNITS
        )

        // Perform the query
        val cursor: Cursor? = db.query(
            DatabaseHelper.ApartmentsEntry.TABLE_NAME,  // The table to query
            projection,                  // The array of columns to return
            null,                       // The columns for the WHERE clause
            null,                       // The values for the WHERE clause
            null,                       // don't group the rows
            null,                       // don't filter by row groups
            null                        // don't order the rows
        )

        cursor?.use { cursor ->
            // Iterate over the cursor to get heating units values
            while (cursor.moveToNext()) {
                val heatingUnits = cursor.getFloat(cursor.getColumnIndexOrThrow(DatabaseHelper.ApartmentsEntry.COLUMN_NAME_HEATING_UNITS))
                totalHeatingUnits += heatingUnits
            }
        }

        // Close the cursor and database
        cursor?.close()
        dbHelper.close()
    }

    override fun onBackPressed() {
        // Restore visibility of UI elements in activity when back button is pressed
        val buttonCalculate: Button = findViewById(R.id.buttonCalculate)
        val infoTotal: TextView = findViewById(R.id.infoTotal)
        val infoElevator: TextView = findViewById(R.id.infoElevator)
        val infoHeating: TextView = findViewById(R.id.infoHeating)
        val huButton : Button = findViewById(R.id.huButton)

        buttonCalculate.visibility = View.VISIBLE
        infoTotal.visibility = View.VISIBLE
        infoElevator.visibility = View.VISIBLE
        infoHeating.visibility = View.VISIBLE
        huButton.visibility = View.VISIBLE

        super.onBackPressed()
    }

}


private fun calculateGeneralFees(generalExpenses: Double, totalSqMeters: Float, sqMeters: Int, m2calcChecked: Boolean, sharedFees: Float): String {
    return if (m2calcChecked) {
        val result = (generalExpenses / totalSqMeters) * sqMeters
        DecimalFormat("#.##").format(result) // Format the result to two decimal places
    } else {
        val result = (generalExpenses * (sharedFees / 100))
        DecimalFormat("#.##").format(result) // Format the result to two decimal places
    }
}

private fun calculateΕlevatorFees(
    elevatorExpenses: Double,
    totalSqMeters: Float,
    sqMeters: Int,
    floor: Int,
    floors: Int,
    elevatorFees: Float,
    m2calcChecked: Boolean,
    elevatorChecked: Boolean
): String {

    return if (m2calcChecked && elevatorChecked) {
        val result =
            ((elevatorExpenses / (2 * totalSqMeters)) * sqMeters) + ((elevatorExpenses / (2 * floors)) * floor)
        Log.d("M2 & Elevator Checked", "elevatorExpenses: $elevatorExpenses, totalSqMeters: $totalSqMeters, sqMeters: $sqMeters, floor: $floor, floors: $floors")
        val formattedResult = DecimalFormat("#.##").format(result)
        Log.d("IntermediateResult", formattedResult)
        formattedResult // Format the result to two decimal places
    } else if (elevatorChecked) {
        val result = (elevatorExpenses * (elevatorFees / 100))
        Log.d("Elevator Checked", "elevatorExpenses: $elevatorExpenses, totalSqMeters: $totalSqMeters, sqMeters: $sqMeters, floor: $floor, floors: $floors")
        val formattedResult = DecimalFormat("#.##").format(result)
        Log.d("IntermediateResult", formattedResult)
        formattedResult // Format the result to two decimal places
    } else {
        Log.d("IntermediateResult", "0.0")
        "0.0" // Return a default value
    }
}

private fun calculateHeatingFees(heatingExpenses: Double, totalHeatingUnits: Float, personalHeatingUnits: Float, heatingChecked: Boolean): String {
    return if (heatingChecked) {
        val result = (heatingExpenses / totalHeatingUnits) * personalHeatingUnits
        DecimalFormat("#.##").format(result) // Format the result to two decimal places
    } else {
        "0.0" // Return a default value
    }
}





//ΜΕ ΒΑΣΗ ΟΡΟΦΟΥ ΚΑΙ ΤΜ

//(α) (έξοδα/τμΜΑΧ)τμΈνοικου=έξοδαΈνοικου

//(β) (έξοδα/2τμΜΑΧ)τμΈνοικου+(έξοδα/2όροφοςΜΑΧ)όροφοςΈνοικου=έξοδαΑνελκυστήρα

//(γ) (έξοδα/κατανάλωσηΜΑΧ)κατανάλωσηΈνοικου=έξοδαΘέρμανσης


//ΜΕ ΒΑΣΗ ΠΟΣΟΣΤΟΥ ποσοστό max=100

//(δ) έξοδα(ποσοστοΚοινοχρηστων/100)=έξοδαΈνοικου

//(ε) έξοδαΑνελκυστήρα(ποσοστόΑνελκυστήρα/100)=έξοδαΑνελκυστήρα

//(γ) (έξοδα/κατανάλωσηΜΑΧ)*κατανάλωσηΈνοικου=έξοδαΘέρμανσης

