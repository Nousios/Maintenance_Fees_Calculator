package com.example.maintenancefeescalculator

import android.os.Parcel
import android.os.Parcelable

data class Building(
    val buildingId: Long,
    val address: String,
    val addressNumber: String,
    val floors: String,
    val flats: String,
    val elevatorChecked: Boolean,
    val heatingChecked: Boolean,
    val m2calcChecked: Boolean
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(buildingId)
        parcel.writeString(address)
        parcel.writeString(addressNumber)
        parcel.writeString(floors)
        parcel.writeString(flats)
        parcel.writeByte(if (elevatorChecked) 1 else 0)
        parcel.writeByte(if (heatingChecked) 1 else 0)
        parcel.writeByte(if (m2calcChecked) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Building> {
        override fun createFromParcel(parcel: Parcel): Building {
            return Building(parcel)
        }

        override fun newArray(size: Int): Array<Building?> {
            return arrayOfNulls(size)
        }
    }
}