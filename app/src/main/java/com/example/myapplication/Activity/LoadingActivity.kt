package com.example.myapplication.Activity

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.location.Address
import android.location.Geocoder
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobile.auth.userpools.CognitoUserPoolsSignInProvider
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedList
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunctionException
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaInvokerFactory
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.example.myapplication.Data.*
import com.example.myapplication.R
import com.google.gson.Gson
import com.koushikdutta.ion.Ion
import org.json.JSONArray
import org.json.JSONObject
import java.io.DataInputStream
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
    lateinit var lambdacredentials:CognitoCachingCredentialsProvider

    //Data
    var martDataArray:PaginatedList<MartData> ?= null
    var eventDataArray:PaginatedList<EventData> ?= null
    var itemDataArray:PaginatedList<ProductData> ?= null
    var martDataLocation:ArrayList<MyLocation> = arrayListOf()
    var eventData:ArrayList<EventItem> = arrayListOf()
    var product:ArrayList<Product> = arrayListOf()

    //연동 코드
    lateinit var factory:LambdaInvokerFactory
    lateinit var myInterface:MyInterface

    //Handler
    lateinit var handler:Handler

    //Handler Message
    var GET_DATA = 1111
    var DATA_TO_LOCATION = 2222

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)
        //아마존 스레드
        if(checkDataExists()){
            Log.d("내장디비","데이터 있음")
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
//            finish()
        }else{
            init()
//            finish()
        }
    }

    //check data exists in sharedPreferences
    fun checkDataExists():Boolean{
        var sh = getSharedPreferences("AMAZON_DATA", Activity.MODE_PRIVATE)
        if(sh.contains("AMAZON_MART")){
            Log.d("내장디비",sh.getString("AMAZON_MART",null))
            if(!sh.getString("AMAZON_MART",null)!!.isNotEmpty()){
                Log.d("내장디비","마트데이터가 없음")
                return false
            }
        }else{
            Log.d("내장디비","마트데이터 키 없음")
            return false
        }
        if(sh.contains("AMAZON_EVENT")){
            Log.d("내장디비",sh.getString("AMAZON_EVENT",null))
            if(!sh.getString("AMAZON_EVENT",null)!!.isNotEmpty()){
                Log.d("내장디비","이벤트데이터가 없음")
                return false
            }
        }else{
            Log.d("내장디비","이벤트데이터 키 없음")
            return false
        }
        if(sh.contains("AMAZON_PRODUCT")){
            Log.d("내장디비",sh.getString("AMAZON_PRODUCT",null))
            if(!sh.getString("AMAZON_PRODUCT",null)!!.isNotEmpty()){
                Log.d("내장디비","상품데이터가 없음")
                return false
            }
        }else{
            Log.d("내장디비","상품데이터 키 없음")
            return false
        }
        return true
    }

    fun init(){
        //아마존에 연결
        credentials = CognitoCachingCredentialsProvider(this,"ap-northeast-2:1140fa47-3059-4bdb-a382-25735d00f34d", Regions.AP_NORTHEAST_2)
        ddb = AmazonDynamoDBClient(credentials)
        ddb!!.setRegion((Region.getRegion(Regions.AP_NORTHEAST_2)))
        Log.d("아마존",ddb.toString())
        dynamoDBMapper = DynamoDBMapper.builder().dynamoDBClient(ddb).build()

        //AWS 람다 함수 연결
        lambdacredentials = CognitoCachingCredentialsProvider(this,"ap-northeast-2:78a1b59b-091f-4e12-ba88-047ad107cdf8",Regions.AP_NORTHEAST_2)
        factory = LambdaInvokerFactory(applicationContext,Regions.AP_NORTHEAST_2,lambdacredentials)
        myInterface = factory.build(MyInterface::class.java)//잘 모르겠음
        var request = RequestClass(true)

        //python 서버 연결
//        connectPython()

        //AsyncTask를 통해 데이터를 가져옴
        val AWSAsyncTask = AWSAsyncTask()
        AWSAsyncTask.execute()

        //AsyncTask를 이용한 카트 코드
        val AWSAsyncTask2 = AWSAsyncTask2()
        AWSAsyncTask2.execute(request)

//        //핸들러 생성
        handler = Handler(Handler.Callback {
            //스레드 작업이 끝나면 할 것
            when(it.arg1){
                GET_DATA->{
                    //데이터를 다 불러옴
                    Log.d("스레드","GET_DATA")
                    DataConvertInit()
                    DatatoProduct()
                    StringtoImage()
                }
                DATA_TO_LOCATION->{
                    //데이터를 경도 위도 좌표로 변환
                    Log.d("스레드",martDataLocation.size.toString())

                    //데이터를 내장 디비에 저장 (sharedpreference)
                    if(martDataLocation.size>0){
                        setDataArrayPref("AMAZON_MART",martDataLocation)
                    }
                    if(eventData.size>0){
                        setDataArrayPref("AMAZON_EVENT",eventData)
                    }
                    if(product.size>0)
                    {
                        setDataMapRef("AMAZON_PRODUCT",product)
                    }

                    //모든 데이터를 받아왔으므로 액티비티 전환(intent)
                    val intent = Intent(this, MainActivity::class.java)
//                    intent.putExtra("MART_DATA",martDataLocation)
//                    intent.putExtra("EVENT_DATA",eventData)
//                    intent.putExtra("PRODUCT_DATA",product)
//                    Log.d("아마존","인텐트 변경")
                    startActivity(intent)
                }
            }
            return@Callback true
        })
    }

    //save sharedPreference ArrayList
    fun <T>setDataArrayPref(key:String, values:ArrayList<T>){
        var sh = getSharedPreferences("AMAZON_DATA", Activity.MODE_PRIVATE)
        var editor = sh.edit()
        var gson = Gson()
        var json = gson.toJson(values)
        if(!values.isEmpty()){
            editor.remove(key)
            editor.putString(key,json)
        }else{
            editor.putString(key,null)
        }
        editor.commit()
    }

    //save sharedPreference HashMap
    fun setDataMapRef(key:String,values:ArrayList<Product>){
        var sh = getSharedPreferences("AMAZON_DATA", Activity.MODE_PRIVATE)
        var editor = sh.edit()
        var gson = Gson()
        var json = gson.toJson(values)
        if(!values.isEmpty()){
            editor.remove(key)
            editor.putString(key,json)
        }else{
            editor.putString(key,null)
        }
        editor.commit()
    }

    //python server
    fun connectPython(){
        Thread(object: Runnable {
            override fun run() {
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                Log.d("서버","연결중")
                var socket = Socket("192.168.0.11",35357)
                Log.d("서버",socket.toString())

                var dis = DataInputStream(socket.getInputStream())

//                while(true){
//                    var line = dis.read() as Int
//                    Log.d("서버","서버에서 받은 값 "+line.toString())
//
//                    if(line == 99){
//                        Log.d("서버","종료")
//                        socket.close()
//                        break
//                    }
//                }
            }

        }).start()
    }

    fun DatatoProduct(){
        for(item in itemDataArray!!){
            var bit = Ion.with(this).load("http:"+item.getProductImg()).asBitmap().get()
            product.add( Product(bit,item.name,item.number,item.price.replace(",","").toInt(),item.category,item.gprice))
        }
    }

    fun StringtoImage(){
        //이미지로 변환해주는 함수
        for(event in eventDataArray!!){
            var bit = Ion.with(this).load("http:"+event.getEventImg()).asBitmap().get()
            eventData.add(EventItem(event.getEventName(), bit))
        }

        //핸들러 메세지
        var message = handler.obtainMessage()
        message.arg1 = DATA_TO_LOCATION
        handler.sendMessage(message)
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


        while(true){
            if(futureTask1.isDone && futureTask2.isDone){
                //모두 종료된 경우
                martDataLocation.addAll(futureTask1.get())
                martDataLocation.addAll(futureTask2.get())
                break
            }
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
            Log.d("스레드","onPostExecute")
            var message = handler.obtainMessage()
            message.arg1 = GET_DATA
            handler.sendMessage(message)
        }
    }

    //코드 랜덤 발생 람다 async 함수
    inner class AWSAsyncTask2 : AsyncTask<RequestClass, Void, ResponseClass>() {
        override fun doInBackground(vararg params: RequestClass?): ResponseClass? {
            try{
                return myInterface.smartKartCode(params[0])
            } catch(lfe: LambdaFunctionException){
                Log.e("Tag","Failed to invoke echo",lfe)
                return null
            }
        }

        override fun onPostExecute(result: ResponseClass?) {
            if(result==null){
                return
            }
            Log.d("async",result.authenticationCode)
        }
    }
}
