package com.example.myapplication.MainPage


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

import com.example.myapplication.R
import kotlinx.android.synthetic.main.fragment_main.*

/**
 * A simple [Fragment] subclass.
 */
class MainFragment : Fragment() {

    val images = arrayListOf(R.drawable.eventimage1, R.drawable.eventimage2, R.drawable.eventimage3)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    fun setInit(){
        for(image in images){
            changeImage(image)
        }
    }

    fun changeImage(image:Int){
        val imageView = ImageView(context)
        imageView.setBackgroundResource(image)

        event_slide.addView(imageView)
        event_slide.flipInterval = 3000//1000에 1초
        event_slide.isAutoStart = true

        event_slide.setInAnimation(context, android.R.anim.slide_in_left)
        event_slide.setOutAnimation(context,android.R.anim.slide_out_right)
    }

}
