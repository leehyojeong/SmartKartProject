package com.example.myapplication.BuyPage


import android.os.*
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunctionException
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaInvokerFactory
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.example.myapplication.Activity.*
import com.example.myapplication.BuyPage.OtherItemDialog.OtherItemDialog
import com.example.myapplication.CodePage.CodeFragment
import com.example.myapplication.Data.BuyList
import com.example.myapplication.Data.Product
import com.example.myapplication.Data.ProductData

import com.example.myapplication.R
import kotlinx.android.synthetic.main.fragment_buy_list.*
import kotlinx.android.synthetic.main.fragment_buy_list.view.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * A simple [Fragment] subclass.
 */
class BuyListFragment : Fragment() {

    lateinit var adapter:ItemListAdapter
    var list:ArrayList<Product> = arrayListOf()//카트에 담긴 아이템 리스트
    var product:HashMap<String,Product> = hashMapOf()//전체 아이템 리스트
    var product_key:ArrayList<String> = arrayListOf()
    lateinit var recycler:RecyclerView
    lateinit var total_price:TextView
    lateinit var seperateBox:CheckBox
    var checkedList:ArrayList<Product> = arrayListOf()


    //AWS
    var dynamoDBMapper: DynamoDBMapper?= null
    var ddb : AmazonDynamoDBClient?= null
    lateinit var credentials: CognitoCachingCredentialsProvider

    //handler
    lateinit var handler:Handler
    var READ_BUY_LIST = 7777

    //연동 코드
    lateinit var lambdacredentials:CognitoCachingCredentialsProvider
    lateinit var factory: LambdaInvokerFactory
    lateinit var dbInterface: DBInterface
    lateinit var codeBundle: Bundle
    var changeData:Boolean=false

    lateinit var handlerThread:HandlerThread
    lateinit var thread:Thread

    var isCheck = false

    companion object{
        fun newInstace(product:HashMap<String,Product>):Fragment{
            var butFrag = BuyListFragment()
            var args = Bundle()
            args.putSerializable("PRODUCT",product)
            butFrag.arguments = args
            return butFrag
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(arguments != null){
            this.product = arguments!!.getSerializable("PRODUCT") as HashMap<String,Product>
            Log.d("물품",product.size.toString())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var v = inflater.inflate(R.layout.fragment_buy_list, container, false)
        recycler = v.total_list
        total_price = v.total_price
        seperateBox = v.seperateBox
        getAWS()//get AWS DynamoDB

        lambdacredentials = CognitoCachingCredentialsProvider(context,"ap-northeast-2:24399e8e-583b-4e21-8349-a4eca8fc8310",
            Regions.AP_NORTHEAST_2)
        factory = LambdaInvokerFactory(context, Regions.AP_NORTHEAST_2,lambdacredentials)
        dbInterface = factory.build(DBInterface::class.java)//잘 모르겠음

       initListLayout(false)
        makeAdapter()
        startTimer()
     //   Log.d("aws_request",request.toString())

       // val AWSAsyncTask2 = AWSAsyncTask2()
        //AWSAsyncTask2.execute(request)
        init()
        checkSeperate()
        return v
    }

    fun startTimer() {
  //      timer = Timer()
   //     var TT = object:TimerTask(){
   //         override fun run() {
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//                val AWSAsyncTask2 = AWSAsyncTask2()
 //               var request = DBRequestClass(true)
 //               Log.d("타이머","타이머 실행")
 //               AWSAsyncTask2.execute(request)
//                Looper.prepare()
 //               handler = Handler()
  //              Looper.loop()
 //              init()
   //         }
   //     }
   //     timer.schedule(TT,0,1000)

     //   handlerThread = HandlerThread("AWS DATA")
     //   handlerThread.start()
     //   handler = Handler(handlerThread.looper){
     //       if(it.arg1 == READ_BUY_LIST){
       //         Log.d("핸들러 스레드","핸들러")
      //          //initListLayout(isCheck)
                //adapter.notifyDataSetChanged()
      //          init()
      //      }
     //       return@Handler true
     //   }
     //   return true

       thread = object:Thread(){
           override fun run() {
               super.run()
               var msg = handler.obtainMessage()
               if(msg.arg1 == READ_BUY_LIST){
                   handler.sendMessage(msg)
               }
           }
       }
        thread.start()

        handler = Handler(){
            Log.d("핸들러","READ LIST")
            makeList()
            initListLayout(isCheck)
            Thread.sleep(1000)
            init()
            return@Handler true
        }
    }


    fun getAWS(){
        credentials = CognitoCachingCredentialsProvider(context,"ap-northeast-2:1140fa47-3059-4bdb-a382-25735d00f34d", Regions.AP_NORTHEAST_2)
        ddb = AmazonDynamoDBClient(credentials)
        ddb!!.setRegion((Region.getRegion(Regions.AP_NORTHEAST_2)))
        dynamoDBMapper = DynamoDBMapper.builder().dynamoDBClient(ddb).build()
    }

    fun loadData(){
       Thread(object:Runnable{
           override fun run() {
               product_key = arrayListOf()
               Log.d("코드 product",product.values.toString())
               //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
               var scanItem = dynamoDBMapper!!.scan(BuyList::class.java, DynamoDBScanExpression())
               var item = dynamoDBMapper!!.load(BuyList::class.java,"2222")
               Log.d("코드 product",item.item.size.toString())
               for(i in 0 until item.item.size){
                   Log.d("코드 물건 하나",item.item[i])
                   product_key.add(item.item[i])
               }

               var message = handler.obtainMessage()
               message.arg1 = READ_BUY_LIST
               handler.sendMessage(message)
           }
       }).start()
    }

    fun init(){
        Log.d("핸들러","데이터불러옴")
        loadData()
        //handler = Handler(Handler.Callback {
        //    when(it.arg1){
        //        READ_BUY_LIST->{
        //            makeList()
         //           initListLayout(false)
         //       }
         //   }
         //   return@Callback true
       // })
    }

    fun makeList(){
        Log.d("핸들러","리스트만듦")
        list = arrayListOf()
        var count = 0
        Log.d("핸들러 product",product.size.toString())
            for(i in product_key){
                Log.d("핸들러 for문",i)
                Log.d("핸들러 hashmap",product[i].toString())
                if(product.containsKey(i)){
                    Log.d("핸들러 contains문",i)
                    list.add(product.get(i)!!)
                    list[count].num = 1
                    count++
                }
            }
        Log.d("핸들러",list.size.toString())
    }

    fun initListLayout(isCheck:Boolean){
        Log.d("핸들러","어댑터 붙임")
        adapter = ItemListAdapter(list,context!!,true,isCheck)//갯수 표시를 해줌
        val layoutManager = LinearLayoutManager(context!!,LinearLayoutManager.VERTICAL,false)
        recycler.layoutManager = layoutManager
        recycler.adapter = adapter
    }

    fun makeAdapter(){
        //아이템 클릭 했을 때 다이얼로그
        //비슷한 정보의 다른 아이템 보여줌
        adapter.itemClickListener = object:ItemListAdapter.OnItemClickListener{
            override fun OnItemClick(
                holder: ItemListAdapter.ViewHolder,
                view: View,
                data: Product,
                position: Int
            ) {
                // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                //아이템 하나를 클릭했을 때 다이얼로그 보여줌
                var otherDialog = OtherItemDialog(context!!,list[position],product)
                otherDialog.show()
            }

        }

        adapter.itemCheckedListener = object:ItemListAdapter.OnItemCheckListener{
            override fun OnItemChecked(
                holder: ItemListAdapter.ViewHolder,
                view: View,
                data: Product,
                position: Int
            ) {
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                if(holder.check.isChecked){
                    checkedList.add(list[position])
                    Log.d("체크",data.toString())
                }
            }

        }

        //구매한 물건 총 금액
        var total = 0
        for(l in list){
            total += (l.num*l.price)
        }
        total_price.text = total.toString()
    }

    fun checkSeperate(){
        seperateBox.setOnCheckedChangeListener { compoundButton, b ->
            if(seperateBox.isChecked){
                //check
                isCheck = true
            }else{
                //uncheck
                isCheck = false
            }
            initListLayout(isCheck)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        thread.interrupt()
    }

    //코드 랜덤 발생 람다 async 함수
    inner class AWSAsyncTask2 : AsyncTask<DBRequestClass, Void, DBResponseClass>() {
        override fun doInBackground(vararg params: DBRequestClass?): DBResponseClass? {
            Log.d("aws","DBdoInBackground")
            Log.d("파람",params.toString())
            try{
                return dbInterface.androidDBLambda(params[0])
            } catch(lfe: LambdaFunctionException){
                Log.e("Tag","Failed to invoke echo",lfe)
                return null
            }
        }

        override fun onPostExecute(result: DBResponseClass?) {
            if(result==null){
                return
            }
            Log.d("async",result.eventmake)
            if(result.eventmake.equals("DB 이벤트 발생")){
                Log.d("타이머","데이터셋 바뀜뀜")
               //timer.cancel()
            }
            // KartCode = result.authenticationCode.toString()
            changeData = result.eventmake!!.toBoolean()
            Log.d("데이터 바뀜",changeData.toString())
            // codeBundle
            //var message = handler.obtainMessage()
            //message.arg1 = GET_CODE
            //handler.sendMessage(message)

        }
    }
}
