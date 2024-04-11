package com.example.maintenancefeescalculator

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class TenantAdapter(private val itemClickListener: OnItemClickListener) : ListAdapter<Apartment, TenantAdapter.ViewHolder>(TenantDiffCallback()) {

    companion object {
        private class TenantDiffCallback : DiffUtil.ItemCallback<Apartment>() {
            override fun areItemsTheSame(oldItem: Apartment, newItem: Apartment): Boolean {
                return oldItem.name == newItem.name
            }

            override fun areContentsTheSame(oldItem: Apartment, newItem: Apartment): Boolean {
                return oldItem == newItem
            }
        }
    }

    interface OnItemClickListener {
        fun onOptionsClick(apartment: Apartment, anchorView: View)
    }

     inner class ViewHolder(itemView: View, private val itemClickListener: OnItemClickListener) : RecyclerView.ViewHolder(itemView) {
         val textViewName: TextView = itemView.findViewById(R.id.textName)
         val textViewSurname: TextView = itemView.findViewById(R.id.textSurname)
         val textViewFloor: TextView = itemView.findViewById(R.id.textFloor)
         val textViewPhone: TextView = itemView.findViewById(R.id.textPhone)
         val textViewEmail: TextView = itemView.findViewById(R.id.textEmail)
         val textViewSqMeters: TextView = itemView.findViewById(R.id.textSqMeters)
         val textViewSharedFees: TextView = itemView.findViewById(R.id.textSharedFees)
         val textViewElevator: TextView = itemView.findViewById(R.id.textElevator)
         val optionsButton: Button = itemView.findViewById(R.id.optionsButton)

         init {
             // Set a click listener on the options button
             optionsButton.setOnClickListener {
                 val position = adapterPosition
                 if (position != RecyclerView.NO_POSITION) {
                     Log.d("ViewHolder", "Options button clicked at position $position")
                     itemClickListener.onOptionsClick(getItem(position), optionsButton)
                 }
             }
         }
     }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(

            R.layout.item_apartments,
            parent,
            false

        )
        return ViewHolder(view, itemClickListener)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val tenant = currentList[position]

        holder.textViewName.text = "Name: ${tenant.name}"
        holder.textViewSurname.text = "  ${tenant.surname}"
        holder.textViewFloor.text = "Floor: ${tenant.floor}"
        holder.textViewPhone.text = "Phone: ${tenant.phone}"
        holder.textViewEmail.text = "Email: ${tenant.email}"
        holder.textViewSqMeters.text = "Square Meters: ${tenant.sqMeters}"
        if (!tenant.sharedFees.isNullOrBlank()) {
            holder.textViewSharedFees.text = "Shared Fees: ${tenant.sharedFees}%"
        } else {
            holder.textViewSharedFees.visibility = View.GONE
        }
        if (!tenant.elevatorFees.isNullOrBlank()) {
            holder.textViewElevator.text = "Elevator Fees: ${tenant.elevatorFees}%"
        } else {
            holder.textViewElevator.visibility = View.GONE
        }
    }

}