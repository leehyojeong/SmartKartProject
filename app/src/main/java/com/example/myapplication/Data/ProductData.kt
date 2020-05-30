package com.example.myapplication.Data

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable
import java.io.Serializable

//get Data from DynamoDB product
@DynamoDBTable(tableName = "product")
class ProductData:Serializable {
    lateinit var img: String
    lateinit var name:String
    var price:Int = 0
    lateinit var category:String
    var number:Int = 0

    @DynamoDBHashKey(attributeName = "img")
    @DynamoDBAttribute(attributeName = "img")
    fun getProductImg():String{
        return img
    }

    fun setProductImg(img:String){
        this.img = img
    }

    @DynamoDBRangeKey(attributeName="name")
    @DynamoDBAttribute(attributeName = "name")
    fun getProductName():String{
        return name
    }

    fun setProductName(name:String){
        this.name = name
    }

    @DynamoDBRangeKey(attributeName="price")
    @DynamoDBAttribute(attributeName = "price")
    fun getProductPrice():Int{
        return price
    }

    fun setProductPrice(price:Int){
        this.price = price
    }

    @DynamoDBRangeKey(attributeName = "category")
    @DynamoDBAttribute(attributeName = "category")
    fun getProductCategory():String{
        return category
    }

    fun setProductCategory(category:String){
        this.category = category
    }

}