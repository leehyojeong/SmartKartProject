package com.example.myapplication.BuyPage


import android.os.*
import android.app.Dialog
import android.content.Intent
import android.graphics.Point
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
import com.example.myapplication.Activity.MainActivity
import com.example.myapplication.Activity.MainFragment
import com.example.myapplication.Activity.*
import com.example.myapplication.BuyPage.OtherItemDialog.OtherItemDialog
import com.example.myapplication.CodePage.CodeFragment
import com.example.myapplication.Data.*
import com.example.myapplication.MainDialog.EventDialog.EventDialog
import com.example.myapplication.MainDialog.SearchDialog.SearchDialog

import com.example.myapplication.R
import com.google.android.gms.maps.SupportMapFragment
import kotlinx.android.synthetic.main.activity_main2.*
import kotlinx.android.synthetic.main.fragment_buy_list.*
import kotlinx.android.synthetic.main.fragment_buy_list.view.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlinx.android.synthetic.main.search_dialog.*
import java.io.File

/**
 * A simple [Fragment] subclass.
 */
class BuyListFragment : Fragment() {

    lateinit var adapter: ItemListAdapter
    var list: ArrayList<Product> = arrayListOf()//카트에 담긴 아이템 리스트
    var product: HashMap<String, Product> = hashMapOf()//전체 아이템 리스트
    var product_key: ArrayList<String> = arrayListOf()
    var kartNum: String = "00000"
    lateinit var recycler: RecyclerView
    lateinit var total_price: TextView
    lateinit var seperateBox: CheckBox
    lateinit var end_buy: Button
    var checkedList: ArrayList<Product> = arrayListOf()


    //AWS
    var dynamoDBMapper: DynamoDBMapper? = null
    var ddb: AmazonDynamoDBClient? = null
    lateinit var credentials: CognitoCachingCredentialsProvider

    //handler
    lateinit var handler: Handler
    var READ_BUY_LIST = 7777

    //Dialog Variable
    var eventDialog: Dialog? = null
    var searchDialog: Dialog? = null
    //연동 코드
    lateinit var lambdacredentials: CognitoCachingCredentialsProvider
    lateinit var factory: LambdaInvokerFactory
    lateinit var dbInterface: DBInterface
    lateinit var codeBundle: Bundle
    var changeData: Boolean = false

    lateinit var handlerThread: HandlerThread
    lateinit var thread: Thread

    var isCheck = false

    companion object {
        fun newInstace(product: HashMap<String, Product>, kartNum: String): Fragment {
            var butFrag = BuyListFragment()
            var args = Bundle()
            args.putSerializable("PRODUCT", product)
            args.putString("KART", kartNum)
            butFrag.arguments = args
            return butFrag
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            this.product = arguments!!.getSerializable("PRODUCT") as HashMap<String, Product>
            this.kartNum = (arguments!!.getString("KART").toString()).replace("인증코드 : ", "")
            Log.d("물품", product.size.toString())
            Log.d("카트번호 번들", kartNum)
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
        end_buy = v.end_buy
        getAWS()//get AWS DynamoDB

        lambdacredentials = CognitoCachingCredentialsProvider(
            context, "ap-northeast-2:24399e8e-583b-4e21-8349-a4eca8fc8310",
            Regions.AP_NORTHEAST_2
        )
        factory = LambdaInvokerFactory(context, Regions.AP_NORTHEAST_2, lambdacredentials)
        dbInterface = factory.build(DBInterface::class.java)//잘 모르겠음

        initListLayout(false)
        makeAdapter()
        startTimer()

        init()
        checkSeperate()
        return v
    }

    fun startTimer() {
        thread = object : Thread() {
            override fun run() {
                super.run()
                var msg = handler.obtainMessage()
                if (msg.arg1 == READ_BUY_LIST) {
                    handler.sendMessage(msg)
                }
            }
        }
        thread.start()

        handler = Handler() {
            Log.d("핸들러", "READ LIST")
            makeList()
            initListLayout(isCheck)
            Thread.sleep(1000)
            init()
            return@Handler true
        }
    }


    fun getAWS() {
        credentials = CognitoCachingCredentialsProvider(
            context,
            "ap-northeast-2:1140fa47-3059-4bdb-a382-25735d00f34d",
            Regions.AP_NORTHEAST_2
        )
        ddb = AmazonDynamoDBClient(credentials)
        ddb!!.setRegion((Region.getRegion(Regions.AP_NORTHEAST_2)))
        dynamoDBMapper = DynamoDBMapper.builder().dynamoDBClient(ddb).build()
        Log.d("카트번호 AWS", dynamoDBMapper.toString())
    }

    fun loadData() {
        Thread(object : Runnable {
            override fun run() {
                product_key = arrayListOf()
                Log.d("코드 product", product.values.toString())
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                var scanItem = dynamoDBMapper!!.scan(BuyList::class.java, DynamoDBScanExpression())
                var item = dynamoDBMapper!!.load(BuyList::class.java, kartNum)
                if (item != null) {
                    Log.d("코드 product", item.toString())
                    for (i in 0 until item.item.size) {
                        Log.d("코드 물건 하나", item.item[i])
                        product_key.add(item.item[i])
                    }
                } else {
                    product_key = arrayListOf()
                }
                var message = handler.obtainMessage()
                message.arg1 = READ_BUY_LIST
                handler.sendMessage(message)
            }
        }).start()
    }

    fun init() {
        loadData()
        end_buy.setOnClickListener {
            var file = context!!.getFileStreamPath("KartCode.txt")
            file.delete()

            thread.interrupt()
            //end fragment
            var manager = activity!!.supportFragmentManager
            var ft = manager.beginTransaction()
            //manager.beginTransaction().remove(this).commit()
            //manager.popBackStack()

            ft.replace(R.id.frameLayout, CodeFragment.newInstace(product,kartNum))
            // ft.replace(R.id.frameLayout,codeFragment)
            ft.commit()
            (activity as MainActivity).callInit()
        }
    }

    fun makeList() {
        list = arrayListOf()
        var count = 0
        Log.d("핸들러 product", product.size.toString())
        for (i in product_key) {
            Log.d("핸들러 for문", i)
            Log.d("핸들러 hashmap", product[i].toString())
            if (product.containsKey(i)) {
                Log.d("핸들러 contains문", i)
                list.add(product.get(i)!!)
                list[count].num = 1
                count++
            }
        }
        Log.d("핸들러", list.size.toString())
    }

    fun initListLayout(isCheck: Boolean) {
        adapter = ItemListAdapter(list, context!!, true, isCheck, checkedList)//갯수 표시를 해줌
        val layoutManager = LinearLayoutManager(context!!, LinearLayoutManager.VERTICAL, false)
        recycler.layoutManager = layoutManager
        recycler.adapter = adapter
    }
        fun makeAdapter() {
            //아이템 클릭 했을 때 다이얼로그
            //비슷한 정보의 다른 아이템 보여줌
            adapter.itemClickListener = object : ItemListAdapter.OnItemClickListener {
                override fun OnItemClick(
                    holder: ItemListAdapter.ViewHolder,
                    view: View,
                    data: Product,
                    position: Int
                ) {
                    // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    //아이템 하나를 클릭했을 때 다이얼로그 보여줌
                    var otherDialog = OtherItemDialog(context!!, list[position], product)
                    otherDialog.show()
                }

            }

            adapter.itemCheckedListener = object : ItemListAdapter.OnItemCheckListener {
                override fun OnItemChecked(
                    holder: ItemListAdapter.ViewHolder,
                    view: View,
                    data: Product,
                    position: Int
                ) {
                    //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    if (holder.check.isChecked) {
                        checkedList.add(list[position])
                        Log.d("체크 fragment", data.toString())
                    } else {
                        checkedList.remove(list[position])
                        Log.d("체크 fragment", "Unchecked " + data.toString())
                    }
                }

            }

            //구매한 물건 총 금액
            var total = 0
            for (l in list) {
                total += (l.num * l.price)
            }
            total_price.text = total.toString()
        }

    fun checkSeperate() {
        seperateBox.setOnCheckedChangeListener { compoundButton, b ->
            if (seperateBox.isChecked) {
                //check
                isCheck = true
            } else {
                //uncheck
                isCheck = false
                if (checkedList.size > 0) {
                    //something in this list
                    //seperate product
                    for (seperate in checkedList) {
                        if (list.contains(seperate)) {
                            list.remove(seperate)
                        }
                    }
                    list = (list + checkedList) as ArrayList<Product>
                }
                initListLayout(false)
            }
            initListLayout(isCheck)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        thread.interrupt()
    }
}

