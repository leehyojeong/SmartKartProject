package com.example.myapplication.MainPage.SearchDialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.BuyPage.ItemListAdapter
import com.example.myapplication.BuyPage.ListItem
import com.example.myapplication.MainPage.MainFragment
import com.example.myapplication.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.search_dialog.*

class SearchDialog(context: Context) : Dialog(context){

    lateinit var adapter:ItemListAdapter
    lateinit var list:ArrayList<ListItem>//검색

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
            //이름이 포함되는 물품
            initLayout()
        }

    }


    fun initLayout(){
        adapter = ItemListAdapter(list, context,false)
        val layoutManager = LinearLayoutManager(context!!, LinearLayoutManager.VERTICAL,false)
        search_list.layoutManager = layoutManager
        search_list.adapter = adapter
    }
}