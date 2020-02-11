package com.example.myapplication.BuyPage.OtherItemDialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.BuyPage.ItemListAdapter
import com.example.myapplication.BuyPage.ListItem
import com.example.myapplication.R
import kotlinx.android.synthetic.main.other_item_dialog.*

class OtherItemDialog(context: Context) : Dialog(context) {

    lateinit var adapter:ItemListAdapter
    lateinit var list:ArrayList<ListItem>//추천상품들

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
        initLayout()
    }

    fun initLayout(){
        adapter = ItemListAdapter(list,context!!,false)//갯수 표시를 해줌
        val layoutManager = LinearLayoutManager(context!!, LinearLayoutManager.VERTICAL,false)
        select_list.layoutManager = layoutManager
        select_list.adapter = adapter
    }
}
