package com.example.myapplication.BuyPage


import android.os.Bundle
import android.os.Handler
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
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.example.myapplication.BuyPage.OtherItemDialog.OtherItemDialog
import com.example.myapplication.CodePage.CodeFragment
import com.example.myapplication.Data.BuyList
import com.example.myapplication.Data.Product
import com.example.myapplication.Data.ProductData

import com.example.myapplication.R
import kotlinx.android.synthetic.main.fragment_buy_list.*
import kotlinx.android.synthetic.main.fragment_buy_list.view.*

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
        init()
        checkSeperate()
        return v
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
               Log.d("코드 product",item.toString())
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
        loadData()
        handler = Handler(Handler.Callback {
            when(it.arg1){
                READ_BUY_LIST->{
                    makeList()
                    initListLayout(false)
                }
            }
            return@Callback true
        })
    }

    fun makeList(){
        list = arrayListOf()
        var count = 0
        for(i in product_key){
            list.add(product.get(i)!!)
            list[count].num = 1
            count++
        }
    }

    fun initListLayout(isCheck:Boolean){
        adapter = ItemListAdapter(list,context!!,true,isCheck)//갯수 표시를 해줌
        val layoutManager = LinearLayoutManager(context!!,LinearLayoutManager.VERTICAL,false)
        recycler.layoutManager = layoutManager
        recycler.adapter = adapter

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
                initListLayout(true)
            }else{
                //uncheck
                initListLayout(false)
            }
        }
    }
}
