package com.example.myapplication.BuyPage


import android.app.Dialog
import android.graphics.Point
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.BuyPage.OtherItemDialog.OtherItemDialog

import com.example.myapplication.R
import kotlinx.android.synthetic.main.fragment_buy_list.view.*

/**
 * A simple [Fragment] subclass.
 */
class BuyListFragment : Fragment() {

    lateinit var adapter:ItemListAdapter
    lateinit var list:ArrayList<ListItem>//카트에 담긴 아이템 리스트
    lateinit var recycler:RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var v = inflater.inflate(R.layout.fragment_buy_list, container, false)
        recycler = v.total_list
        init()
        return v
    }

    fun init(){
        list = arrayListOf()
        list.add(ListItem("abc.jpg","새우깡",1,800))
        list.add(ListItem("abcd.jpg","새우깡2",2,900))

       initListLayout()
    }

    fun initListLayout(){
        adapter = ItemListAdapter(list,context!!,true)//갯수 표시를 해줌
        val layoutManager = LinearLayoutManager(context!!,LinearLayoutManager.VERTICAL,false)
        recycler.layoutManager = layoutManager
        recycler.adapter = adapter

        //아이템 클릭 했을 때 다이얼로그
        //비슷한 정보의 다른 아이템 보여줌
        adapter.itemClickListener = object:ItemListAdapter.OnItemClickListener{
            override fun OnItemClick(
                holder: ItemListAdapter.ViewHolder,
                view: View,
                data: ListItem,
                position: Int
            ) {
               // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                //아이템 하나를 클릭했을 때 다이얼로그 보여줌
                var otherDialog = OtherItemDialog(context!!)
                otherDialog.show()
            }

        }
    }



}
