package com.example.myapplication.MainPage.SearchDialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import com.example.myapplication.R

class SearchDialog(context: Context) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var v = R.layout.search_dialog
        setContentView(v)

        //다이얼로드 밖의 화면 흐리게
        var layoutParams = WindowManager.LayoutParams()
        layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
        layoutParams.dimAmount = 0.8f

        //크기 조절
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.gravity = Gravity.BOTTOM

        window!!.attributes = layoutParams

    }
}