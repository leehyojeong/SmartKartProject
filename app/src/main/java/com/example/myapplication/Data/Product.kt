package com.example.myapplication.Data

import android.graphics.Bitmap
import android.os.Parcel
import android.os.Parcelable

data class Product(var img:Bitmap,var name:String, var num:Int=0, var price:Int, var category_id:String, var gprice:String):Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(Bitmap::class.java.classLoader)!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(img, flags)
        parcel.writeString(name)
        parcel.writeInt(num)
        parcel.writeInt(price)
        parcel.writeString(category_id)
        parcel.writeString(gprice)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Product> {
        override fun createFromParcel(parcel: Parcel): Product {
            return Product(parcel)
        }

        override fun newArray(size: Int): Array<Product?> {
            return arrayOfNulls(size)
        }
    }


}