package com.example.fingerprint

import android.os.Parcel
import android.os.Parcelable


// Parcelable wrapper class for an ArrayList<Float>
data class FloatArrayListParcelable(val floatList: List<Float>) : Parcelable {
    constructor(parcel: Parcel) : this(parcel.createFloatArray()?.toCollection(ArrayList()) ?: arrayListOf())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeFloatArray(floatList.toFloatArray())
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<FloatArrayListParcelable> {
        override fun createFromParcel(parcel: Parcel): FloatArrayListParcelable {
            return FloatArrayListParcelable(parcel)
        }

        override fun newArray(size: Int): Array<FloatArrayListParcelable?> {
            return arrayOfNulls(size)
        }
    }
}
