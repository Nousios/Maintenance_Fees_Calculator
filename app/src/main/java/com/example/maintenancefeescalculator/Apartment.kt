package com.example.maintenancefeescalculator

import android.os.Parcel
import android.os.Parcelable

data class Apartment(
    val id: Long,
    var name: String,
    var surname: String,
    var floor: String,
    var phone: String,
    var email: String,
    var sqMeters: String,
    var sharedFees: String,
    var elevatorFees: String?
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(surname)
        parcel.writeString(floor)
        parcel.writeString(phone)
        parcel.writeString(email)
        parcel.writeString(sqMeters)
        parcel.writeString(sharedFees)
        parcel.writeString(elevatorFees)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Apartment> {
        override fun createFromParcel(parcel: Parcel): Apartment {
            return Apartment(parcel)
        }

        override fun newArray(size: Int): Array<Apartment?> {
            return arrayOfNulls(size)
        }
    }
}
