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
    }
}
