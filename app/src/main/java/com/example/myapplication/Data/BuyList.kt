package com.example.myapplication.Data

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable
import java.io.Serializable

//get Data from DynamoDB User Buy List
@DynamoDBTable(tableName = "Item")
class BuyList:Serializable {
    lateinit var num:String//kart number
     var item:ArrayList<String> = arrayListOf()//item list

    @DynamoDBHashKey(attributeName = "code")
    @DynamoDBAttribute(attributeName = "code")
    fun getEventName():String{
        return num
    }

    fun setEventName(num:String){
        this.num = num
    }

    @DynamoDBAttribute(attributeName = "item")
    fun getEventImg():ArrayList<String>{
        return item
    }

    fun setEventImg(item:ArrayList<String>){
        this.item = item
    }
}