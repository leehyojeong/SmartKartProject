package com.example.myapplication.MainDialog.SearchDialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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
import kotlinx.android.synthetic.main.search_dialog.*

class SearchDialog(context: Context,product_map:HashMap<String,Product>) : Dialog(context){
    var latitude = 0.0
    var longitude = 0.0

    //data
    lateinit var adapter:ItemListAdapter
    var list:ArrayList<Product> = arrayListOf()//검색 list
    var product:HashMap<String,Product> = product_map//product list

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var v = R.layout.search_dialog

        getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        setContentView(v)


        //다이얼로드 밖의 화면 흐리게
        var layoutParams = WindowManager.LayoutParams()
        layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
        layoutParams.dimAmount = 0.8f

        //크기 조절
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.gravity = Gravity.BOTTOM

        window!!.attributes = layoutParams



        //검색버튼 눌렀을 때
        search_btn.setOnClickListener {
            //지도에도 마킹 해줘야댐
            var name = search_item.text.toString()//검색할 물품 이름
            //리스트를 만들어줌
            Log.d("검색",name)
            //이름이 포함되는 물품
            makeList(name)
//            initLayout()
        }

    }

    override fun cancel() {
        super.cancel()
        Log.d("종료","종료")
    }

    fun initLayout(){
        adapter = ItemListAdapter(list, context,false)
        val layoutManager = LinearLayoutManager(context!!, LinearLayoutManager.VERTICAL,false)
        search_list.layoutManager = layoutManager
        search_list.adapter = adapter
    }

    fun makeList(name:String){
        Log.d("검색 함수","클릭")
        //유사 상품 리스트
        list = arrayListOf()
        for(item in product.keys){
            Log.d("검색",item)
            if(item.contains(name)){
                list.add(Product(product[item]!!.img,product[item]!!.name,product[item]!!.num,product[item]!!.price,product[item]!!.category_id,product[item]!!.gprice))
            }
        }
        initLayout()
    }
}