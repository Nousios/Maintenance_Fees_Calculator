package com.example.maintenancefeescalculator

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.provider.BaseColumns
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment


class HeatingUnitsFragment : Fragment() {

    private lateinit var dbHelper: DatabaseHelper
    private var okButtonClickListener: OnOkButtonClickListener? = null
    val apartmentList: MutableList<ApartmentWithHeatingUnits> = mutableListOf()
    private lateinit var onBackPressedListener: OnBackPressedListener


    data class ApartmentWithHeatingUnits(
        val apartmentName: String,
        val apartmentSurname: String,
        var heatingUnits: Float = 0.0f
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_heating_units, container, false)
        Log.d("HeatingUnitsFragment", "onCreateView called")
        val okButton: Button = rootView.findViewById(R.id.okButton)

        dbHelper = DatabaseHelper(requireContext())

        val apartmentListLayout: LinearLayout = rootView.findViewById(R.id.apartmentListLayout)

        okButton.setOnClickListener {
            val fieldsEmpty = areFieldsEmpty(apartmentListLayout)

            if (fieldsEmpty) {
                updateHeatingUnits()
                okButtonClickListener?.onOkButtonClick(dbHelper)
            } else {
                // Show error message if fields are empty
                Toast.makeText(requireContext(), "All fields must be filled", Toast.LENGTH_SHORT).show()
            }
        }

        // Fetch apartments from the database and display them
        fetchApartmentsAndDisplay(apartmentListLayout)

        return rootView
    }

    private fun areFieldsEmpty(layout: ViewGroup): Boolean {
        var allFieldsFilled = false

        Log.d("areFieldsEmpty", "Number of child views: ${layout.childCount}")

        // Iterate through each child view of the layout
        for (i in 0 until layout.childCount) {
            val childView = layout.getChildAt(i)

            // Log information about the child view
            Log.d("areFieldsEmpty", "Child view at index $i: ${childView.javaClass.simpleName}")

            // If the child view is a ViewGroup, recursively search its children
            if (childView is ViewGroup) {
                // Recursively check child ViewGroup
                allFieldsFilled = areFieldsEmpty(childView)
                if (!allFieldsFilled) {
                    // If any child ViewGroup contains empty fields, return false
                    Log.d("areFieldsEmpty", "Found empty fields in child view at index $i")
                    return false
                }
            }

            // Check if the child view is an EditText
            if (childView is EditText) {
                // Check if the EditText field is empty
                if (childView.text.isNullOrEmpty()) {
                    // If any EditText field is empty, return false
                    Log.d("areFieldsEmpty", "Found empty EditText at index $i")
                    return false
                }
            }
        }

        // If all EditText fields are non-empty, set allFieldsFilled to true
        allFieldsFilled = true
        Log.d("areFieldsEmpty", "All fields are filled")
        return allFieldsFilled
    }

    fun setOnOkButtonClickListener(listener: OnOkButtonClickListener) {
        okButtonClickListener = listener
    }

    private fun updateHeatingUnits() {
        // Open a writable database
        val db = dbHelper.writableDatabase

        // Iterate through the apartmentList and update heating units for each apartment
        for (apartment in apartmentList) {
            val apartmentId = getApartmentIdFromDatabase(apartment.apartmentName, apartment.apartmentSurname)
            if (apartmentId != null) {
                // Create a ContentValues object to store the values to be updated
                val values = ContentValues().apply {
                    put(DatabaseHelper.ApartmentsEntry.COLUMN_NAME_HEATING_UNITS, apartment.heatingUnits)
                }
                // Perform update operation
                val selection = "${BaseColumns._ID} = ?"
                val selectionArgs = arrayOf(apartmentId.toString())
                db.update(DatabaseHelper.ApartmentsEntry.TABLE_NAME, values, selection, selectionArgs)
            }
        }
    }

    private fun fetchApartmentsAndDisplay(layout: LinearLayout) {
        // Open a readable database
        val db = dbHelper.readableDatabase

        // Define the columns you want to fetch
        val projection = arrayOf(
            DatabaseHelper.ApartmentsEntry.COLUMN_NAME_NAME,
            DatabaseHelper.ApartmentsEntry.COLUMN_NAME_SURNAME
        )

        // Define the selection (WHERE clause)
        val selection = "${DatabaseHelper.ApartmentsEntry.COLUMN_NAME_BUILDING_ID} = ?"

        // Define the selection arguments
        val selectionArgs = arrayOf((requireActivity() as CalculateActivity).buildingId.toString())

        // Perform the query
        val cursor: Cursor? = db.query(
            DatabaseHelper.ApartmentsEntry.TABLE_NAME,  // The table to query
            projection,                  // The array of columns to return
            selection,                   // The columns for the WHERE clause
            selectionArgs,               // The values for the WHERE clause
            null,                        // don't group the rows
            null,                        // don't filter by row groups
            null                         // don't order the rows
        )

        cursor?.use { cursor ->
            // Iterate over the cursor to get apartment details
            while (cursor.moveToNext()) {
                val apartmentName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ApartmentsEntry.COLUMN_NAME_NAME))
                val apartmentSurname = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ApartmentsEntry.COLUMN_NAME_SURNAME))

                val apartment = ApartmentWithHeatingUnits(apartmentName, apartmentSurname)
                apartmentList.add(apartment)

                // Create a new LinearLayout for each apartment
                val apartmentLayout = LinearLayout(requireContext())
                apartmentLayout.orientation = LinearLayout.HORIZONTAL
                apartmentLayout.gravity = Gravity.CENTER

                // Create a TextView for displaying apartment name and surname
                val textView = TextView(requireContext())
                textView.text = "$apartmentName $apartmentSurname"
                textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.orange))
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18F)
                val textViewParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                textViewParams.setMargins(0, 0, 20, 0) // Add spacing on the right side
                textView.layoutParams = textViewParams

                // Create an EditText for users to type
                val editText = EditText(requireContext())
                editText.hint = "Heating Value"
                editText.setTextColor(ContextCompat.getColor(requireContext(), R.color.orange))
                editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18F)
                editText.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )

                editText.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                    override fun afterTextChanged(s: Editable?) {
                        apartment.heatingUnits = s.toString().toFloatOrNull() ?: 0.0f
                    }
                })

                // Add TextView and EditText to the LinearLayout
                apartmentLayout.addView(textView)
                apartmentLayout.addView(editText)

                // Add the LinearLayout for this apartment to the main layout
                layout.addView(apartmentLayout)
            }
        }

    }


    interface OnBackPressedListener {
        fun onBackPressed()
    }

    private fun getApartmentIdFromDatabase(name: String, surname: String): Long? {
        val db = dbHelper.readableDatabase
        val selection = "${DatabaseHelper.ApartmentsEntry.COLUMN_NAME_NAME} = ? AND ${DatabaseHelper.ApartmentsEntry.COLUMN_NAME_SURNAME} = ?"
        val selectionArgs = arrayOf(name, surname)
        val cursor = db.query(
            DatabaseHelper.ApartmentsEntry.TABLE_NAME,
            arrayOf(BaseColumns._ID),
            selection,
            selectionArgs,
            null,
            null,
            null
        )
        return cursor?.use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getLong(cursor.getColumnIndexOrThrow(BaseColumns._ID))
            } else {
                null
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Add callback for handling back button press
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            // Handle back button press here
            // For example, navigate back or perform any required action
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnBackPressedListener) {
            onBackPressedListener = context
        } else {
            throw RuntimeException("$context must implement OnBackPressedListener")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Close the database helper to prevent memory leaks
        dbHelper.close()
    }
}

interface OnOkButtonClickListener {
    fun onOkButtonClick(dbHelper: DatabaseHelper)
}