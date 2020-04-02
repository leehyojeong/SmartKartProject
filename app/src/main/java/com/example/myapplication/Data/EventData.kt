package com.example.myapplication.Data

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable

@DynamoDBTable(tableName = "event")
class EventData {
    lateinit var name:String
    lateinit var img:String

    @DynamoDBHashKey(attributeName = "name")
    @DynamoDBAttribute(attributeName = "name")
    fun getEventName():String{
        return name
    }

    fun setEventName(name:String){
        this.name = name
    }

    @DynamoDBRangeKey(attributeName="img")
    @DynamoDBAttribute(attributeName = "img")
    fun getEventImg():String{
        return img
    }

    fun setEventImg(img:String){
        this.img = img
    }
}