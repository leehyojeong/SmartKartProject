package com.example.myapplication.MainDialog.EventDialog

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedList
import com.example.myapplication.Data.EventData
import com.example.myapplication.Data.EventItem
import com.example.myapplication.R
import com.koushikdutta.ion.Ion
import kotlinx.android.synthetic.main.event_dialog.*

class EventDialog(context: Context, argeventList:ArrayList<EventItem>) : Dialog(context) {

    val images = arrayListOf(R.drawable.eventimage1, R.drawable.eventimage2, R.drawable.eventimage3)
    var eventList:ArrayList<EventItem> = argeventList

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var v = R.layout.event_dialog
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

        setInit()
    }

    fun setInit(){
        for(image in 0..3){
            changeImage(eventList[image].img,eventList[image].name)
        }
        var adapter = EventListAdapter(context,eventList)
        val layoutManager = LinearLayoutManager(context!!, LinearLayoutManager.VERTICAL,false)
        event_list_view.layoutManager = layoutManager
        event_list_view.adapter = adapter
    }

    fun changeImage(image:Bitmap,title:String){
        var context = context!!.applicationContext
        val eventLayout = LinearLayout(context)
        eventLayout.orientation = LinearLayout.VERTICAL


        val textView = TextView(context)
        val imageView = ImageView(context)
        Log.d("이벤트","http:"+image)

        imageView.setImageBitmap(image)

        textView.text = title


        eventLayout.addView(imageView)
        eventLayout.addView(textView)

        event_slide.addView(eventLayout)
        event_slide.flipInterval = 3000//1000에 1초
        event_slide.isAutoStart = true

        event_slide.setInAnimation(context, android.R.anim.slide_in_left)
        event_slide.setOutAnimation(context,android.R.anim.slide_out_right)
    }

}