package com.example.typetwest.model

import android.os.Parcel
import android.os.Parcelable

data class User(var id:String = "",
                var email:String = "",
                var image:String = "",
                var avgWpm:Int = 0,
                var accuracy:String = "0%",
):Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readString()!!
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(email)
        parcel.writeString(image)
        parcel.writeInt(avgWpm)
        parcel.writeString(accuracy)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }
}
