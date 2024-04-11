package com.example.maintenancefeescalculator

import android.content.Context
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ApartmentsFragment : Fragment() {

    private lateinit var dbHelper: DatabaseHelper

    private var elevatorChecked: Boolean = false
    private var heatingChecked: Boolean = false
    private var m2calcChecked: Boolean = false

    private var editedApartment: Apartment? = null

    interface OnButtonClickListener {
        fun onDoneButton(
            name: String,
            surname: String,
            floor: String,
            phone: String,
            email: String,
            sqMeters: String,
            sharedFees: String,
            elevator: String
        )
    }
    private var buttonClickListener: OnButtonClickListener? = null

    private lateinit var userName: EditText
    private lateinit var userSur: EditText
    private lateinit var userFloor: EditText
    private lateinit var userPhone: EditText
    private lateinit var userEmail: EditText
    private lateinit var userSqMeters: EditText
    private lateinit var userSharedFees: EditText
    private lateinit var userElevator: EditText


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_apartments, container, false)
        Log.d("TenantsFragment", "Fragment created. Visibility: ${isVisible}")

        elevatorChecked = arguments?.getBoolean("ELEVATOR_CHECKED") ?: false
        heatingChecked = arguments?.getBoolean("HEATING_CHECKED") ?: false
        m2calcChecked = arguments?.getBoolean("M2CALC_CHECKED") ?: false


        userName = view.findViewById(R.id.saveName)
        userSur = view.findViewById(R.id.saveSurname)
        userFloor = view.findViewById(R.id.saveFloor)
        userPhone = view.findViewById(R.id.savePhone)
        userEmail = view.findViewById(R.id.saveEmail)
        userSqMeters = view.findViewById(R.id.saveSqMeters)
        userSharedFees = view.findViewById(R.id.saveSharedFees)
        userElevator = view.findViewById(R.id.saveElevator)

        userName.inputType = InputType.TYPE_CLASS_TEXT
        userSur.inputType = InputType.TYPE_CLASS_TEXT

        val inputFilter = InputFilter { source, start, end, dest, dstart, dend ->
            for (i in start until end) {
                // Αν το γράμμα δεν είναι γράμμα, απορρίπτεται
                if (!Character.isLetter(source[i])) {
                    return@InputFilter ""
                }
            }
            null
        }

        // Εφαρμόστε τον InputFilter στα πεδία του ονόματος και του επωνύμου
        userName.filters = arrayOf(inputFilter)
        userSur.filters = arrayOf(inputFilter)

        val doneButton = view.findViewById<Button>(R.id.doneButton)
        doneButton.setOnClickListener {
            val name = userName.text.toString()
            val surname = userSur.text.toString()
            val floor = userFloor.text.toString()
            val phone = userPhone.text.toString()
            val email = userEmail.text.toString()
            val sqMeters = userSqMeters.text.toString()
            val sharedFees = userSharedFees.text.toString()
            val elevator = userElevator.text.toString()


            var isValid = true

            if (phone.length != 10) {
                userPhone.error = "Please add a correct phone number."
                isValid = false
            }


            if (!isValidEmail(email)) {
                userEmail.error = "Enter a valid email address or keep it blank if the tenant doesn't have one"
                isValid = false
            }

            if (isValid) {

                buttonClickListener?.onDoneButton(
                    name, surname, floor, phone, email, sqMeters, sharedFees, elevator)
                clearEditTextFields()
                val frameLayout = activity?.findViewById<FrameLayout>(R.id.fragment_container)
                frameLayout?.visibility = View.GONE
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (handleBackPressed()) {
                isEnabled = false // Disable the callback to prevent further handling
            } else {
                isEnabled = true // Continue with default back button behavior
            }
        }

        updateUIForElevator(elevatorChecked,m2calcChecked)
        updateUIForM2Calc(m2calcChecked)

        arguments?.getParcelable<Apartment>("EDIT_TENANT")?.let {
            fillTenantDetailsForEdit(it)
        }
    }

    fun handleBackPressed(): Boolean {
        // Close the fragment if it's visible
        if (isVisible && fragmentManager?.backStackEntryCount ?: 0 > 0) {
            parentFragmentManager.popBackStack()
            return true // Back button press handled
        }
        return false // Back button press not handled
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is OnButtonClickListener) {
            buttonClickListener = context
            dbHelper = DatabaseHelper(context)
        }
    }

    fun getEditedTenant(): Apartment? {
        return editedApartment
    }

    private fun clearEditTextFields() {
        userName.text.clear()
        userSur.text.clear()
        userFloor.text.clear()
        userPhone.text.clear()
        userEmail.text.clear()
        userSqMeters.text.clear()
        userSharedFees.text.clear()
        userElevator.text.clear()
    }

    private fun isValidEmail(email: String): Boolean {
        return email.isEmpty() || email.contains("@")
    }

    private fun fillTenantDetailsForEdit(apartment: Apartment) {
        editedApartment = apartment
        // Populate EditText fields with existing tenant details for editing
        userName.setText(editedApartment?.name ?: "")
        userSur.setText(editedApartment?.surname ?: "")
        userFloor.setText(editedApartment?.floor ?: "")
        userPhone.setText(editedApartment?.phone ?: "")
        userEmail.setText(editedApartment?.email ?: "")
        userSqMeters.setText(editedApartment?.sqMeters ?: "")
        userSharedFees.setText(editedApartment?.sharedFees ?: "")
        userElevator.setText(editedApartment?.elevatorFees ?: "")
    }

    private fun updateUIForElevator(elevatorChecked: Boolean,m2calcChecked: Boolean) {

        val elevatorPercent = view?.findViewById<TextView>(R.id.elevatorPercent)
        val saveElevator = view?.findViewById<EditText>(R.id.saveElevator)

        if (elevatorChecked && m2calcChecked ) {
            elevatorPercent?.visibility = View.VISIBLE
            saveElevator?.visibility = View.VISIBLE
            saveElevator?.isFocusable = false
            saveElevator?.isClickable = false
            saveElevator?.setBackgroundResource(R.drawable.grayed_rounded_text_background)
        }else if (elevatorChecked) {
            elevatorPercent?.visibility = View.VISIBLE
            saveElevator?.visibility = View.VISIBLE
            saveElevator?.isFocusable = true
            saveElevator?.isClickable = true
        } else {
            elevatorPercent?.visibility = View.VISIBLE
            saveElevator?.visibility = View.VISIBLE
            saveElevator?.isFocusable = false
            saveElevator?.isClickable = false
            saveElevator?.setBackgroundResource(R.drawable.grayed_rounded_text_background)
        }
    }


    private fun updateUIForM2Calc(m2calcChecked: Boolean) {
        val sharedFeesPercent = view?.findViewById<TextView>(R.id.sharedFeesPercent)
        val saveSharedFees = view?.findViewById<EditText>(R.id.saveSharedFees)

        if (m2calcChecked) {
            sharedFeesPercent?.visibility = View.VISIBLE
            saveSharedFees?.visibility = View.VISIBLE
            saveSharedFees?.isFocusable = false
            saveSharedFees?.isClickable = false
            saveSharedFees?.setBackgroundResource(R.drawable.grayed_rounded_text_background)
        } else {
            sharedFeesPercent?.visibility = View.VISIBLE
            saveSharedFees?.visibility = View.VISIBLE
            saveSharedFees?.isFocusable = true
            saveSharedFees?.isClickable = true
        }
    }
}