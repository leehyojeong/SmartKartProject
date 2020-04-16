package com.example.myapplication.Data

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable
import java.io.Serializable

@DynamoDBTable(tableName = "mart")
class MartData:Serializable {

    lateinit var name:String
    lateinit var road:String

    @DynamoDBHashKey(attributeName = "name")
    @DynamoDBAttribute(attributeName = "name")
    fun getMartName():String{
        return name
    }

    fun setMartName(name:String){
        this.name = name
    }

    @DynamoDBRangeKey(attributeName="road")
    @DynamoDBAttribute(attributeName = "road")
    fun getMartRoad():String{
        return road
    }

    fun setMartRoad(road:String){
        this.road = road
    }

}