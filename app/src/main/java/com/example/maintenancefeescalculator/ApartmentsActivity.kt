package com.example.maintenancefeescalculator

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.provider.BaseColumns
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ApartmentsActivity : AppCompatActivity(), ApartmentsFragment.OnButtonClickListener, TenantAdapter.OnItemClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TenantAdapter
    private var buildingId: Long = -1
    private lateinit var dbHelper: DatabaseHelper

    private var fragmentVisible = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apartments)

        buildingId = intent.getLongExtra("BUILDING_ID", buildingId)
        dbHelper = DatabaseHelper(this)

        val frameLayout = findViewById<FrameLayout>(R.id.fragment_container)


        // Set FAB click listener
        val tenantsFab = findViewById<FloatingActionButton>(R.id.tenantsFab)
        val calcButton = findViewById<Button>(R.id.calcButton)

        findViewById<FrameLayout>(R.id.fragment_container).visibility = View.GONE

        tenantsFab.setOnClickListener {
            Log.d("FabClick", "FAB button clicked")

            val fragmentContainer = findViewById<FrameLayout>(R.id.fragment_container)
            val isFragmentVisible = supportFragmentManager.findFragmentById(R.id.fragment_container) != null

            val fragmentTransaction = supportFragmentManager.beginTransaction()

            if (!isFragmentVisible) {
                // Fragment is not added, add it
                val newFragment = ApartmentsFragment()
                fragmentTransaction.replace(R.id.fragment_container, newFragment)
                    .addToBackStack(null)
                    .commit()

                // Show fragment container
                fragmentContainer.visibility = View.VISIBLE

                // Hide calcButton and tenantsFab when opening the fragment
                setVisibilityOfButtons(View.GONE)
            } else {
                // Fragment is already added, toggle its visibility
                if (fragmentContainer.visibility == View.VISIBLE) {
                    // Fragment is visible, hide it
                    fragmentContainer.visibility = View.GONE

                    // Show calcButton and tenantsFab when closing the fragment
                    setVisibilityOfButtons(View.VISIBLE)

                    // Pop back stack to remove the fragment
                    supportFragmentManager.popBackStack()
                } else {
                    // Fragment is not visible, show it
                    fragmentContainer.visibility = View.VISIBLE

                    // Hide calcButton and tenantsFab when opening the fragment
                    setVisibilityOfButtons(View.GONE)
                }
            }
        }

        supportFragmentManager.addOnBackStackChangedListener {
            fragmentVisible = supportFragmentManager.backStackEntryCount > 0
        }

        // Create fragment and set arguments
        val fragment = ApartmentsFragment().apply {
            arguments = Bundle().apply {
                putBoolean("ELEVATOR_CHECKED", intent.getBooleanExtra("ELEVATOR_CHECKED", false))
                putBoolean("HEATING_CHECKED", intent.getBooleanExtra("HEATING_CHECKED", false))
                putBoolean("M2CALC_CHECKED", intent.getBooleanExtra("M2CALC_CHECKED", false))
                putLong("BUILDING_ID", buildingId)
            }
        }

        val elevatorChecked = intent.getBooleanExtra("ELEVATOR_CHECKED", false)
        val heatingChecked = intent.getBooleanExtra("HEATING_CHECKED", false)
        val m2calcChecked = intent.getBooleanExtra("M2CALC_CHECKED", false)

        calcButton.setOnClickListener {

            val apartments = getAllApartmentsForBuilding(buildingId)

            // Step 2: Calculate the sum of the sq_meters field
            val totalSqMeters = calculateTotalSqMeters(apartments)

            // Step 3: Save the result to the COLUMN_NAME_TOTAL_SQ_METERS in the database
            saveTotalSqMetersToDatabase(totalSqMeters)

            val intent = Intent(this, CalculateActivity::class.java)
            intent.putExtra("ELEVATOR_CHECKED", elevatorChecked)
            intent.putExtra("HEATING_CHECKED", heatingChecked)
            intent.putExtra("M2CALC_CHECKED", m2calcChecked)
            intent.putExtra("BUILDING_ID", buildingId)
            startActivity(intent)
        }

        val fragmentManager: FragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()

        recyclerView = findViewById(R.id.tenantRecyclerView)
        adapter = TenantAdapter(this)
        recyclerView.adapter = adapter


        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        updateTenantList()
    }

    override fun onOptionsClick(apartment: Apartment, anchorView: View) {
        val popupMenu = PopupMenu(this, anchorView)

        // Programmatically add the "Edit" option
        val editMenuItem = popupMenu.menu.add(Menu.NONE, Menu.NONE, 0, "Edit")

        // Set an OnMenuItemClickListener for the added option
        editMenuItem.setOnMenuItemClickListener {
            openEditTenantScreen(apartment)
            true
        }

        popupMenu.show()
    }

    private fun setVisibilityOfButtons(visibility: Int) {
        val calcButton = findViewById<Button>(R.id.calcButton)
        val tenantsFab = findViewById<FloatingActionButton>(R.id.tenantsFab)

        calcButton.visibility = visibility
        tenantsFab.visibility = visibility
    }


    private fun openEditTenantScreen(apartment: Apartment) {
        // Log a message to check if the function is called
        Log.d("ThirdActivity", "Open edit screen for tenant: ${apartment.name}")

        // Open TenantsFragment in edit mode
        val fragment = ApartmentsFragment().apply {
            arguments = Bundle().apply {
                putBoolean("ELEVATOR_CHECKED", intent.getBooleanExtra("ELEVATOR_CHECKED", false))
                putBoolean("HEATING_CHECKED", intent.getBooleanExtra("HEATING_CHECKED", false))
                putBoolean("M2CALC_CHECKED", intent.getBooleanExtra("M2CALC_CHECKED", false))
                putLong("BUILDING_ID", buildingId)
                putParcelable("EDIT_TENANT", apartment)
            }
        }

        // Fragment transaction
        val fragmentManager: FragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()

        val frameLayout = findViewById<FrameLayout>(R.id.fragment_container)
        frameLayout.visibility = View.VISIBLE
        setVisibilityOfButtons(View.GONE)
    }

    override fun onDoneButton(
        name: String,
        surname: String,
        floor: String,
        phone: String,
        email: String,
        sqMeters: String,
        sharedFees: String,
        elevator: String
    ) {
        val frameLayout = findViewById<FrameLayout>(R.id.fragment_container)
        frameLayout.visibility = View.GONE

        setVisibilityOfButtons(View.VISIBLE)

        val editedTenant = (supportFragmentManager.findFragmentById(R.id.fragment_container) as? ApartmentsFragment)?.getEditedTenant()

        editedTenant?.let {
            // Update existing tenant
            it.name = name
            it.surname = surname
            it.floor = floor
            it.phone = phone
            it.email = email
            it.sqMeters = sqMeters
            it.sharedFees = sharedFees
            it.elevatorFees = elevator

            dbHelper.updateApartment(it)

            updateTenantList() // Update RecyclerView after editing
        } ?: run {
            // Add new tenant
            val dbHelper = DatabaseHelper(this)
            dbHelper.insertApartment(
                name, surname, floor, phone, email, sqMeters, sharedFees, elevator, buildingId)
            updateTenantList()
        }
    }

    private fun calculateTotalSqMeters(apartments: List<Apartment>): Float {
        var totalSqMeters = 0.0f
        for (apartment in apartments) {
            val sqMetersStr = apartment.sqMeters
            if (sqMetersStr.isNotEmpty()) {
                totalSqMeters += sqMetersStr.toFloat()
            }
        }
        return totalSqMeters
    }

    private fun getAllApartmentsForBuilding(buildingId: Long): List<Apartment> {
        val dbHelper = DatabaseHelper(this)
        return dbHelper.getApartmentList(buildingId)
    }

    private fun saveTotalSqMetersToDatabase(totalSqMeters: Float) {
        val dbHelper = DatabaseHelper(this)
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(DatabaseHelper.AddressEntry.COLUMN_NAME_TOTAL_SQ_METERS, totalSqMeters)
        }

        val selection = "${BaseColumns._ID} = ?"
        val selectionArgs = arrayOf(buildingId.toString())

        db.update(
            DatabaseHelper.AddressEntry.TABLE_NAME,
            values,
            selection,
            selectionArgs
        )

        db.close()
    }

    private fun updateTenantList() {
        val apartmentList = getAllApartmentsForBuilding(buildingId)
        adapter.submitList(apartmentList)
        adapter.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()

        // Refresh the building list every time the activity resumes
        updateTenantList()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
            val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
            if (fragment is ApartmentsFragment && fragment.isVisible) {
                // Handle back button press when ApartmentsFragment is visible
                fragment.handleBackPressed()
                setVisibilityOfButtons(View.VISIBLE)
            } else {
                // Start Welcome activity if ApartmentsFragment is not visible
                val intent = Intent(this, Welcome::class.java)
                startActivity(intent)
            }
        }
}