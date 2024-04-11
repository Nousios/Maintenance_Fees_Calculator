package com.example.maintenancefeescalculator

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PaymentAdapter(private val paymentsData: List<DisplayActivity.Payment>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val HEADER_VIEW_TYPE = 0
    private val ITEM_VIEW_TYPE = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == HEADER_VIEW_TYPE) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_payment_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_payment, parent, false)
            PaymentViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == ITEM_VIEW_TYPE) {
            val payment = paymentsData[position - 1] // Subtract 1 because of the header
            (holder as PaymentViewHolder).bind(payment)
        } else {
            // Handle the header view here if needed
        }
    }

    override fun getItemCount(): Int {
        // Add 1 to account for the header row
        return paymentsData.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) HEADER_VIEW_TYPE else ITEM_VIEW_TYPE
    }

    inner class PaymentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        private val surnameTextView: TextView = itemView.findViewById(R.id.surnameTextView)
        private val phoneTextView: TextView = itemView.findViewById(R.id.phoneTextView)
        private val monthYearTextView: TextView = itemView.findViewById(R.id.monthYearTextView)
        private val totalFeesTextView: TextView = itemView.findViewById(R.id.totalFeesTextView)
        private val generalFeesTextView: TextView = itemView.findViewById(R.id.generalFeesTextView)
        private val elevatorFeesTextView: TextView = itemView.findViewById(R.id.elevatorFeesTextView)
        private val heatingFeesTextView: TextView = itemView.findViewById(R.id.heatingFeesTextView)

        fun bind(payment: DisplayActivity.Payment) {
            nameTextView.text = payment.name
            surnameTextView.text = payment.surname
            phoneTextView.text = payment.phone
            monthYearTextView.text = payment.monthYear
            totalFeesTextView.text = payment.totalFees.toString()
            generalFeesTextView.text = payment.generalPayFees.toString()
            elevatorFeesTextView.text = payment.elevatorPayFees.toString()
            heatingFeesTextView.text = payment.heatingPayFees.toString()
        }
    }

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val headerNameTextView: TextView = itemView.findViewById(R.id.headerNameTextView)
        private val headerSurnameTextView: TextView = itemView.findViewById(R.id.headerSurnameTextView)
        private val headerPhoneTextView: TextView = itemView.findViewById(R.id.headerPhoneTextView)
        private val headerMonthYearTextView: TextView = itemView.findViewById(R.id.headerMonthYearTextView)
        private val headerTotalFeesTextView: TextView = itemView.findViewById(R.id.headerTotalFeesTextView)
        private val headerGeneralFeesTextView: TextView = itemView.findViewById(R.id.headerGeneralFeesTextView)
        private val headerElevatorFeesTextView: TextView = itemView.findViewById(R.id.headerElevatorFeesTextView)
        private val headerHeatingFeesTextView: TextView = itemView.findViewById(R.id.headerHeatingFeesTextView)

        fun bind() {
            headerNameTextView.text = "Name"
            headerSurnameTextView.text = "Surname"
            headerPhoneTextView.text = "Phone"
            headerMonthYearTextView.text = "Month/Year"
            headerTotalFeesTextView.text = "Total Fees"
            headerGeneralFeesTextView.text = "General Fees"
            headerElevatorFeesTextView.text = "Elevator Fees"
            headerHeatingFeesTextView.text = "Heating Fees"
        }
    }
}