package com.example.myapplication.MainDialog.EventDialog

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.myapplication.R

class CustomEventFlipItem: ConstraintLayout {
    lateinit var e_event_container:ConstraintLayout
    lateinit var e_image:ImageView
    lateinit var e_text:TextView

    constructor(context:Context):super(context){
        init(context)
    }

    constructor(context:Context, attrs:AttributeSet?):super(context,attrs){
        init(context)
    }

    constructor(context:Context, attrs:AttributeSet?, defStyleAttr:Int):super(context, attrs, defStyleAttr){
        init(context)
    }

    private fun init(context: Context){
        LayoutInflater.from(context).inflate(R.layout.event_flip_layout,this,true)
        e_event_container = findViewById(R.id.event_flip_container)
        e_image = findViewById(R.id.event_flip_image)
        e_text = findViewById(R.id.event_flip_text)
    }
}