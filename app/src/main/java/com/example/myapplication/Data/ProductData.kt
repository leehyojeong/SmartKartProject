package com.example.myapplication.Data

import android.util.Log
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
    lateinit var gprice:String
    lateinit var category:String
    var number:Int = 0
    lateinit var price:String

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

    @DynamoDBAttribute(attributeName = "gprice")
    fun getProductgPrice():String{
        return gprice
    }

    fun setProductgPrice(price:String){
        if(price.equals("null")){
            this.gprice = "0"
        }else{
            this.gprice = price
        }
    }

    @DynamoDBAttribute(attributeName = "price")
    fun getProductPrice():String{
        return price
    }

    fun setProductPrice(price:String){
        this.price = price
    }

    @DynamoDBRangeKey(attributeName="category_id")
    @DynamoDBAttribute(attributeName = "category_id")
    fun getProductCategory():String{
        return category
    }

    fun setProductCategory(category:String){
        this.category = category
    }

}