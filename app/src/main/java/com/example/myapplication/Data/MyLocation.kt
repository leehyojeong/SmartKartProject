package com.example.myapplication.Data

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

data class MyLocation(var latitude:Double, var longitude:Double, var name:String):Serializable {

}