package com.example.myapplication.Data

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable
import java.io.Serializable

//get Data from DynamoDB User Buy List
@DynamoDBTable(tableName = "Item")
class BuyList:Serializable {
    lateinit var code:String//kart number
     var item:ArrayList<String> = arrayListOf()//item list

    @DynamoDBHashKey(attributeName = "code")
    @DynamoDBAttribute(attributeName = "code")
    fun getCodeNum():String{
        return code
    }

    fun setCodeNum(num:String){
        this.code = num
    }

    @DynamoDBAttribute(attributeName = "item")
    fun getItemList():ArrayList<String>{
        return item
    }

    fun setItemList(item:ArrayList<String>){
        this.item = item
    }
}