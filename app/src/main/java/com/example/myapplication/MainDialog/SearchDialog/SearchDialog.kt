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
import com.example.myapplication.BuyPage.ListItem
import com.example.myapplication.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.search_dialog.*

class SearchDialog(context: Context) : Dialog(context){
    var latitude = 0.0
    var longitude = 0.0



    lateinit var adapter:ItemListAdapter
    lateinit var list:ArrayList<ListItem>//검색

    var database = FirebaseDatabase.getInstance() as FirebaseDatabase
    var myRef = database.reference


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
        //유사 상품 리스트
        list = arrayListOf()
        Log.d("검색",myRef.toString())
        myRef.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot) {
                // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                var value = p0.value as HashMap<String,HashMap<String,HashMap<String,*>>>
                Log.d("검색",value.keys.toString())
                //문자열이 포함되는지
                for (category in value.keys){
                    for(key in value[category]!!.keys){
                        if(key.contains(name)){
                            //문자열이 포함되면
                            var price = value[category]!![key]!!["price"].toString().replace(",","")
                            list.add(ListItem(value[category]!![key]!!["img"].toString(),key,0,price.toInt(),""))
                        }
                    }
                }
                Log.d("검색","for끝")
                initLayout()
            }

        })
    }
}