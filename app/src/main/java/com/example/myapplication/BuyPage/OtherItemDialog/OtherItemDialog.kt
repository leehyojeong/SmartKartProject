package com.example.myapplication.BuyPage.OtherItemDialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.BuyPage.ItemListAdapter
import com.example.myapplication.Data.Product
import com.example.myapplication.Data.ProductData
import com.example.myapplication.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.other_item_dialog.*

class OtherItemDialog(context: Context,item: Product) : Dialog(context) {

    lateinit var adapter:ItemListAdapter
    lateinit var list:ArrayList<Product>//추천상품들
    var item = item as ProductData

    var database = FirebaseDatabase.getInstance() as FirebaseDatabase
    var myRef = database.getReference(item.category)

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
        makeList()
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
    myRef.addValueEventListener(object:ValueEventListener{
        override fun onCancelled(p0: DatabaseError) {
           // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onDataChange(p0: DataSnapshot) {
           // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            var value = p0.value as HashMap<String,HashMap<String,*>>

            //문자열이 포함되는지
            for (key in value.keys){
                if(key.contains(item.name)){
                    //문자열이 포함되면
                    Log.d("데이터",value[key]!!["gprice"].toString())
                    var price = value[key]!!["price"].toString().replace(",","")
                    list.add(
                        Product(
                            value[key]!!["img"].toString(),
                            key,
                            0,
                            price.toInt(),
                            item.category
                        )
                    )
                }
            }
            Log.d("확인","for끝")
            initLayout()
        }

    })
    }
}
