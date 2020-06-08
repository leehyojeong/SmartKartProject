package com.example.myapplication.BuyPage.OtherItemDialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.example.myapplication.BuyPage.ItemListAdapter
import com.example.myapplication.Data.BuyList
import com.example.myapplication.Data.Product
import com.example.myapplication.Data.ProductData
import com.example.myapplication.Data.RecommendData
import com.example.myapplication.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.other_item_dialog.*

class OtherItemDialog(context: Context,item: Product,product:HashMap<String,Product>) : Dialog(context) {

    //data
    lateinit var adapter:ItemListAdapter
    lateinit var list:ArrayList<Product>//추천상품들
    var product:HashMap<String,Product> = product
    var item = item

    //AWS
    var dynamoDBMapper: DynamoDBMapper?= null
    var ddb : AmazonDynamoDBClient?= null
    lateinit var credentials: CognitoCachingCredentialsProvider


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var v = R.layout.other_item_dialog
        setContentView(v)

        //다이얼로드 밖의 화면 흐리게
        var layoutParams = WindowManager.LayoutParams()
        layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
        layoutParams.dimAmount = 0.8f

        //크기 조절
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.gravity = Gravity.BOTTOM

        window!!.attributes = layoutParams

        list = arrayListOf()

        getAWS()
        makeList()
    }

    fun getAWS(){
        credentials = CognitoCachingCredentialsProvider(context,"ap-northeast-2:1140fa47-3059-4bdb-a382-25735d00f34d", Regions.AP_NORTHEAST_2)
        ddb = AmazonDynamoDBClient(credentials)
        ddb!!.setRegion((Region.getRegion(Regions.AP_NORTHEAST_2)))
        dynamoDBMapper = DynamoDBMapper.builder().dynamoDBClient(ddb).build()
    }

    fun loadData(item:Product){
        Thread(object:Runnable{
            override fun run() {
                list = arrayListOf()
                Log.d("코드 product",product.values.toString())
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                var item = dynamoDBMapper!!.load(RecommendData::class.java)

            }
        }).start()
    }

    fun initLayout(){
        Log.d("확인","initLayout")
        adapter = ItemListAdapter(list,context!!,false)//갯수 표시를 안해줌
        val layoutManager = LinearLayoutManager(context!!, LinearLayoutManager.VERTICAL,false)
        select_list.layoutManager = layoutManager
        select_list.adapter = adapter
    }

    fun makeList(){
        //유사 상품 리스트
        list = arrayListOf()
    }
}
