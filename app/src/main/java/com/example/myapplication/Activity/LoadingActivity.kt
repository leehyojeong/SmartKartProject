package com.example.myapplication.Activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.location.Address
import android.location.Geocoder
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.util.Base64
import android.util.Log
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedList
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.example.myapplication.Data.*
import com.example.myapplication.R
import com.google.android.gms.common.util.JsonUtils
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.koushikdutta.ion.Ion
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.lang.StringBuilder
import java.net.Socket
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.FutureTask
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class LoadingActivity : AppCompatActivity() {

    //AWS
    var dynamoDBMapper: DynamoDBMapper?= null
    var ddb : AmazonDynamoDBClient?= null
    lateinit var credentials: CognitoCachingCredentialsProvider

    //Data
    var martDataArray:PaginatedList<MartData> ?= null
    var eventDataArray:PaginatedList<EventData> ?= null
    var itemDataArray:PaginatedList<ProductData> ?= null
    var martDataLocation:ArrayList<MyLocation> = arrayListOf()
    var eventData:ArrayList<EventItem> = arrayListOf()
    var product:ArrayList<Product> = arrayListOf()

    //Handler
    lateinit var handler:Handler

    //Handler Message
    var GET_DATA = 1111
    var DATA_FINISHED = 2222
    var SUCCESS_EVENT = 3333
    var SUCCESS_PRODUCT = 4444
    var SUCCESS_MART = 5555

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)
        //아마존 스레드
        if(checkDataExists()){
            Log.d("파일","액티비티 바꿈")
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }else{

        }
    }

    //check data exists in sharedPreferences
    fun checkDataExists():Boolean{
        var mart_file = File(this.filesDir,"amazon_mart.json")
        var event_file = File(this.filesDir,"amazon_event.json")
        var product_file = File(this.filesDir,"amazon_product.json")

        if(mart_file.exists() && event_file.exists() && product_file.exists()){
            return true
        }else{
            if(!mart_file.exists() && !event_file.exists() && !product_file.exists()){
                Log.d("파일","다 없음")
                init("ALL")
            }else{
               if(!mart_file.exists()){
                    //no mart json
                    Log.d("파일","마트 없음")
                    init("MART")
                }
                if(!event_file.exists()){
                    Log.d("파일","이벤트 없음")
                    init("EVENT")
                }
                if(!product_file.exists()){
                    Log.d("파일","상품 없음")
                    init("PRODUCT")
                }
            }

            return false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        finish()
    }

    fun init(message:String){
        //아마존에 연결
        credentials = CognitoCachingCredentialsProvider(this,"ap-northeast-2:1140fa47-3059-4bdb-a382-25735d00f34d", Regions.AP_NORTHEAST_2)
        ddb = AmazonDynamoDBClient(credentials)
        ddb!!.setRegion((Region.getRegion(Regions.AP_NORTHEAST_2)))
        Log.d("아마존",ddb.toString())
        dynamoDBMapper = DynamoDBMapper.builder().dynamoDBClient(ddb).build()
        //python 서버 연결
//        connectPython()

        //AsyncTask를 통해 데이터를 가져옴
        val AWSAsyncTask = AWSAsyncTask()
        AWSAsyncTask.execute()

        val getEventDataTask = getEventDataTask()
        val getMartDataTask = getMartDataTask()
        val getProductDataTask = getProductDataTask()

//        //핸들러 생성
        handler = Handler(Handler.Callback {
            //스레드 작업이 끝나면 할 것
            when(it.arg1){
                GET_DATA->{
                    //데이터를 다 불러옴
                    if(message.equals("ALL")){
                        getEventDataTask.execute()
                        getMartDataTask.execute()
                        getProductDataTask.execute()
                    }else if(message.equals("MART")){
                        getMartDataTask.execute()
                    }else if(message.equals("EVENT")){
                        getEventDataTask.execute()
                    }else if(message.equals("PRODUCT")){
                        getProductDataTask.execute()
                    }
                    Log.d("스레드","GET_DATA")
                }
                SUCCESS_PRODUCT->{
                    if(message.equals("PRODUCT")){
                        var message = handler.obtainMessage()
                        message.arg1 = DATA_FINISHED
                        handler.sendMessage(message)
                    }
                    if(getMartDataTask.status == AsyncTask.Status.FINISHED && getEventDataTask.status == AsyncTask.Status.FINISHED){
                        var message = handler.obtainMessage()
                        message.arg1 = DATA_FINISHED
                        handler.sendMessage(message)
                    }
                }
                SUCCESS_MART->{
                    if(message.equals("MART")){
                        var message = handler.obtainMessage()
                        message.arg1 = DATA_FINISHED
                        handler.sendMessage(message)
                    }
                    if(getProductDataTask.status == AsyncTask.Status.FINISHED && getEventDataTask.status == AsyncTask.Status.FINISHED){
                        var message = handler.obtainMessage()
                        message.arg1 = DATA_FINISHED
                        handler.sendMessage(message)
                    }
                }
                SUCCESS_EVENT->{
                    if(message.equals("EVENT")){
                        var message = handler.obtainMessage()
                        message.arg1 = DATA_FINISHED
                        handler.sendMessage(message)
                    }
                    if(getProductDataTask.status == AsyncTask.Status.FINISHED && getMartDataTask.status == AsyncTask.Status.FINISHED){
                        var message = handler.obtainMessage()
                        message.arg1 = DATA_FINISHED
                        handler.sendMessage(message)
                    }
                }
                DATA_FINISHED->{
                    //데이터를 경도 위도 좌표로 변환
                    Log.d("아이템",martDataLocation.size.toString()+" "+eventData.size.toString()+" "+product.size.toString())


                    getEventDataTask.cancel(true)
                    getMartDataTask.cancel(true)
                    getProductDataTask.cancel(true)
                    AWSAsyncTask.cancel(true)

                    Log.d("스레드","종료됨")
                    //모든 데이터를 받아왔으므로 액티비티 전환(intent)
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                }
            }
            return@Callback true
        })
    }

    fun toMartJson(value:ArrayList<MyLocation>):String{
        var jArray = JSONArray()
           for (item in value){
                var jObject = JSONObject()
               jObject.put("name",item.name)
               jObject.put("longitude",item.longitude)
               jObject.put("latitude",item.latitude)
               jArray.put(jObject)
           }
        return jArray.toString()
    }

    fun toEventJson(value:ArrayList<EventItem>):String{
        var jArray = JSONArray()
        for (item in value){
            var jObject = JSONObject()
            jObject.put("name",item.name)

            var byteArrayOutputStream = ByteArrayOutputStream()
            item.img.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream)
            var imageBytes = byteArrayOutputStream.toByteArray()
            var img = Base64.encodeToString(imageBytes,Base64.NO_WRAP)

            jObject.put("img",img)
            jArray.put(jObject)
        }
        return jArray.toString()
    }


    fun toProductJson(value:ArrayList<Product>):String{
        var jArray = JSONArray()
        for (item in value){
            var jObject = JSONObject()
            jObject.put("name",item.name)
            jObject.put("price",item.price.toString())
            jObject.put("num",item.num.toString())
            jObject.put("gprice",item.gprice)
            jObject.put("category_id",item.category_id)

            var byteArrayOutputStream = ByteArrayOutputStream()
            item.img.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream)
            var imageBytes = byteArrayOutputStream.toByteArray()
            var img = Base64.encodeToString(imageBytes,Base64.NO_WRAP)

            jObject.put("img",img)

            jArray.put(jObject)
        }
        return jArray.toString()
    }

    fun DataToProduct(start:Int, end:Int):ArrayList<Product>{
         var tempArray = arrayListOf<Product>()
        for(item in start until end){
            Log.d("아이템", itemDataArray!![item].name)
            var bit = Ion.with(this).load("http:"+itemDataArray!![item].getProductImg()).asBitmap().get()
            Log.d("아이템",bit.toString())
            tempArray.add( Product(bit,itemDataArray!![item].name,itemDataArray!![item].number,itemDataArray!![item].price.replace(",","").toInt(),itemDataArray!![item].category,itemDataArray!![item].gprice))
        }
        return tempArray
    }

    fun StringtoImage(){
        //이미지로 변환해주는 함수
        for(event in eventDataArray!!){
            var bit = Ion.with(this).load("http:"+event.getEventImg()).asBitmap().get()
            Log.d("아이템",event.getEventName())
            eventData.add(EventItem(event.getEventName(), bit))
        }
    }

    fun DataConvertInit(){
        //위도 경도로 변환하기 위한 함수
        var convert1 = DataConvert(martDataArray!!,0,martDataArray!!.size/2)
        var convert2 = DataConvert(martDataArray!!,martDataArray!!.size/2,martDataArray!!.size)

        var futureTask1 = FutureTask<ArrayList<MyLocation>>(convert1)
        var futureTask2 = FutureTask<ArrayList<MyLocation>>(convert2)

        var executor = Executors.newFixedThreadPool(2)

        //executorService에 보내서 실행한다
        executor.submit(futureTask1)
        executor.submit(futureTask2)

        martDataLocation.addAll(futureTask1.get())
        martDataLocation.addAll(futureTask2.get())
    }

    inner class DataImage:Callable<ArrayList<Product>>{
        var start:Int
        var end:Int

        constructor(start:Int,end:Int){
            this.start = start
            this.end = end
            Log.d("아이템",start.toString()+" "+end.toString())
        }

        override fun call(): ArrayList<Product> {
           // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            var returnData =   DataToProduct(start,end)
            return returnData
        }

    }

    inner class DataConvert:Callable<ArrayList<MyLocation>>{

        var data:PaginatedList<MartData>
        var start:Int
        var end:Int

        constructor(data:PaginatedList<MartData>,start:Int,end:Int){
            this.data = data
            this.start = start
            this.end = end
        }

        override fun call(): ArrayList<MyLocation> {
           // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.

            var arrayData:ArrayList<MyLocation> = arrayListOf()//반환할 값

            var geocoder = Geocoder(applicationContext,Locale.getDefault())
            for(index in start until end){
                Log.d("아이템",data[index].toString())
                var addresses = arrayListOf<Address>()
                addresses = geocoder.getFromLocationName(data[index].getMartRoad(),1) as ArrayList<Address>

                if(addresses.size == 0){
                    arrayData.add(MyLocation(0.0,0.0,data[index].getMartName()))
                }else{
                    var latitude = addresses.get(0).latitude
                    var longitude = addresses.get(0).longitude
                    arrayData.add(MyLocation(latitude,longitude,data[index].getMartName()))
                }
            }
            return arrayData
        }
    }

    inner class getProductDataTask:AsyncTask<Void,Void,Int>(){
        override fun doInBackground(vararg p0: Void?): Int {
            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            var convert1 = DataImage(0,250)
        var convert2 = DataImage(250,500)
//            var convert1 = DataImage(0,3)
//            var convert2 = DataImage(3,6)

        var futureTask1 = FutureTask<ArrayList<Product>>(convert1)
        var futureTask2 = FutureTask<ArrayList<Product>>(convert2)

        var executor = Executors.newFixedThreadPool(2)

        //executorService에 보내서 실행한다
        executor.submit(futureTask1)
        executor.submit(futureTask2)

        product.addAll(futureTask1.get())
        product.addAll(futureTask2.get())
            return SUCCESS_PRODUCT
        }

        override fun onPostExecute(result: Int?) {
            super.onPostExecute(result)

            var outputStream = openFileOutput("amazon_product.json",Context.MODE_PRIVATE)
            outputStream.write(toProductJson(product).toByteArray())
            outputStream.close()

            var message = handler.obtainMessage()
            message.arg1 = result!!
            handler.sendMessage(message)
        }
    }

    inner class getMartDataTask:AsyncTask<Void,Void,Int>(){
        override fun doInBackground(vararg p0: Void?): Int {
           // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            DataConvertInit()
            return SUCCESS_MART
        }

        override fun onPostExecute(result: Int?) {
            super.onPostExecute(result)

            //save File
            var outputStream = openFileOutput("amazon_mart.json",Context.MODE_PRIVATE)
            outputStream.write(toMartJson(martDataLocation).toByteArray())
            outputStream.close()

            var message = handler.obtainMessage()
            message.arg1 = result!!
            handler.sendMessage(message)
        }
    }

    inner class getEventDataTask:AsyncTask<Void,Void,Int>(){
        override fun doInBackground(vararg p0: Void?): Int {
           // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            StringtoImage()
            return SUCCESS_EVENT
        }

        override fun onPostExecute(result: Int?) {
            super.onPostExecute(result)

            //save data
            var outputStream = openFileOutput("amazon_event.json",Context.MODE_PRIVATE)
            outputStream.write(toEventJson(eventData).toByteArray())
            outputStream.close()

            var message = handler.obtainMessage()
            message.arg1 = result!!
            handler.sendMessage(message)
        }
    }

    inner class AWSAsyncTask: AsyncTask<Void,Void,Int>(){
        override fun doInBackground(vararg p0: Void?): Int {
           // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            martDataArray = dynamoDBMapper!!.scan(MartData::class.java, DynamoDBScanExpression())
            eventDataArray = dynamoDBMapper!!.scan(EventData::class.java,DynamoDBScanExpression())
            itemDataArray = dynamoDBMapper!!.scan(ProductData::class.java,DynamoDBScanExpression())
            Log.d("아마존 데이터",martDataArray!!.size.toString()+"   "+eventDataArray!!.size.toString()+"   "+itemDataArray!!.size.toString())
            return 0
        }

        override fun onPostExecute(result: Int?) {
            super.onPostExecute(result)
            Log.d("스레드","onPOstExecute")
            var message = handler.obtainMessage()
            message.arg1 = GET_DATA
            handler.sendMessage(message)
        }
    }
}
