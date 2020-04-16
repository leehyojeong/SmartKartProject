package com.example.myapplication.Data

import android.graphics.Bitmap
import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

data class EventItem(var name:String, var img:Bitmap) :Parcelable{
    override fun writeToParcel(p0: Parcel?, p1: Int) {
        p0!!.writeString(name)
        p0!!.writeParcelable(img, p1)
    }

    override fun describeContents(): Int {
       // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        return 0
    }

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readParcelable(Bitmap::class.java.classLoader)!!
    ) {

    }

    companion object CREATOR : Parcelable.Creator<EventItem> {
        override fun createFromParcel(parcel: Parcel): EventItem {
            return EventItem(parcel)
        }

        override fun newArray(size: Int): Array<EventItem?> {
            return arrayOfNulls(size)
        }
    }
}