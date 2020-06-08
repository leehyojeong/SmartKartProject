package com.example.myapplication.Data

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable

@DynamoDBTable(tableName = "recommend")
class RecommendData {
    lateinit var name:String
    var recommends:ArrayList<String> = arrayListOf()

    @DynamoDBHashKey(attributeName = "name")
    @DynamoDBAttribute(attributeName = "name")
    fun getRecommendName():String{
        return name
    }

    fun setRecommendName(name:String){
        this.name = name
    }

    @DynamoDBAttribute(attributeName = "recommend")
    fun getRecommendList():ArrayList<String>{
        return recommends
    }

    fun setRecommendList(item:ArrayList<String>){
        this.recommends = item
    }
}