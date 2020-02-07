package com.example.myapplication.MainPage.EventDialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.ViewFlipper
import com.example.myapplication.R
import kotlinx.android.synthetic.main.event_dialog.*
import kotlin.coroutines.coroutineContext

class EventDialog(context: Context) : Dialog(context) {

    val images = arrayListOf(R.drawable.eventimage1, R.drawable.eventimage2, R.drawable.eventimage3)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var v = R.layout.event_dialog
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
//        Log.d("확인","SetInit")
        for(image in images){
            changeImage(image)
        }
    }

    fun changeImage(image:Int){
        var context = context!!.applicationContext
        val imageView = ImageView(context)
        imageView.setBackgroundResource(image)

        event_slide.addView(imageView)
        event_slide.flipInterval = 3000//1000에 1초
        event_slide.isAutoStart = true

        event_slide.setInAnimation(context, android.R.anim.slide_in_left)
        event_slide.setOutAnimation(context,android.R.anim.slide_out_right)
    }

}