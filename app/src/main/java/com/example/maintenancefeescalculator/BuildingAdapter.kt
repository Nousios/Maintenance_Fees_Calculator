package com.example.maintenancefeescalculator

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class BuildingAdapter : ListAdapter<Building, BuildingAdapter.ViewHolder>(BuildingDiffCallback()) {

    companion object {
        private class BuildingDiffCallback : DiffUtil.ItemCallback<Building>() {
            override fun areItemsTheSame(oldItem: Building, newItem: Building): Boolean {
                return oldItem.buildingId == newItem.buildingId
            }

            override fun areContentsTheSame(oldItem: Building, newItem: Building): Boolean {
                return oldItem == newItem
            }
        }
    }


    inner class ViewHolder(itemView: View, private val context: Context) : RecyclerView.ViewHolder(itemView) {
        val textViewAddress: TextView = itemView.findViewById(R.id.textAddress)
        val textViewFloors: TextView = itemView.findViewById(R.id.textFloors)
        val textViewFlats: TextView = itemView.findViewById(R.id.textFlats)
        val textViewElevator: TextView = itemView.findViewById(R.id.textElevator)
        val textViewHeating: TextView = itemView.findViewById(R.id.textHeating)
        val textViewM2Calc: TextView = itemView.findViewById(R.id.textM2Calc)
        val optionsButton: Button = itemView.findViewById(R.id.optionsButton)

        var building: Building? = null

        init {
            optionsButton.setOnClickListener {
                building?.let { nonNullBuilding ->
                    showPopupMenu(nonNullBuilding.buildingId)
                }
            }
        }

        fun bind(building: Building) {
            this.building = building
            Log.d("BuildingAdapter", "Building ID in bind: ${building.buildingId}")
        }

        private fun showPopupMenu(buildingId: Long) {
            val popupMenu = PopupMenu(itemView.context, optionsButton)
            popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_edit -> {
                        // Handle Edit option
                        handleEditOption(buildingId)
                        true
                    }
                    R.id.menu_delete -> {
                        // Handle Delete option
                        handleDeleteOption(buildingId)
                        true
                    }
                    else -> false
                }
            }

            popupMenu.show()
        }

        private fun handleEditOption(buildingId: Long) {
            val building = getItem(position)

            Log.d("EditOption", "Position: $position, Building ID before Intent: ${building.buildingId}")

            val intent = Intent(context, BuildingActivity::class.java).apply {
                putExtra("EDIT_MODE", true)
                putExtra("ADDRESS", building.address)
                putExtra("ADDRESS_NUMBER", building.addressNumber)
                putExtra("FLOORS", building.floors)
                putExtra("FLATS", building.flats)
                putExtra("ELEVATOR_CHECKED", building.elevatorChecked)
                putExtra("HEATING_CHECKED", building.heatingChecked)
                putExtra("M2CALC_CHECKED", building.m2calcChecked)
                putExtra("BUILDING_ID", buildingId)
            }
            (context as AppCompatActivity).startActivityForResult(intent, BuildingActivity.EDIT_REQUEST_CODE)
        }

        private fun handleDeleteOption(buildingId: Long) {
            val context = itemView.context
            val buildingId = getItem(position).buildingId
            val dbHelper = DatabaseHelper(context)

            // Delete building entry from the AddressEntry table
            dbHelper.deleteBuilding(buildingId)

            // Optionally, delete related entries from the TenantsEntry table
            dbHelper.deleteApartmentByBuildingId(buildingId)

            // Set the result to indicate that the delete operation was successful
            (context as AppCompatActivity).setResult(Activity.RESULT_OK)

            // Finish the activity to trigger onActivityResult in the WelcomeActivity
            context.finish()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_building,
            parent,
            false
        )
        return ViewHolder(view, parent.context)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val building = getItem(position)
        holder.bind(building)

        Log.d("BuildingAdapter", "Building ID in onBindViewHolder: ${building.buildingId}")

        holder.textViewAddress.text = "Address: ${building.address} ${building.addressNumber}"
        holder.textViewFloors.text = "Floors: ${building.floors}"
        holder.textViewFlats.text = "Flats: ${building.flats}"
        holder.textViewElevator.text = "Elevator: ${if (building.elevatorChecked) "Yes" else "No"}"
        holder.textViewHeating.text = "Heating: ${if (building.heatingChecked) "Yes" else "No"}"
        holder.textViewM2Calc.text = "Calculator with m² and floor: ${if (building.m2calcChecked) "Yes" else "No"}"

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            Log.d("ItemClick", "Building ID: ${building.buildingId}")
            val intent = Intent(context, ApartmentsActivity::class.java).apply {
                putExtra("ADDRESS", building.address)
                putExtra("FLOORS", building.floors)
                putExtra("FLATS", building.flats)
                putExtra("ELEVATOR_CHECKED", building.elevatorChecked)
                putExtra("HEATING_CHECKED", building.heatingChecked)
                putExtra("M2CALC_CHECKED", building.m2calcChecked)
                putExtra("BUILDING_ID", building.buildingId)
            }
            context.startActivity(intent)
        }
    }

}