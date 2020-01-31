package com.example.myapplication.MainPage

import android.app.Dialog
import android.graphics.Point
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Display
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.myapplication.CodePage.CodeFragment
import com.example.myapplication.MainPage.EventDialog.EventDialog
import com.example.myapplication.MainPage.SearchDialog.SearchDialog
import com.example.myapplication.R
import kotlinx.android.synthetic.main.activity_main2.*

class MainActivity2 : AppCompatActivity() {


    lateinit var mainFrag:MainFragment
    lateinit var codeFrag:CodeFragment
    lateinit var fm:FragmentManager
    lateinit var  ft:FragmentTransaction

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        setNavigation()
    }

    fun setNavigation(){
        mainFrag = MainFragment()
        codeFrag = CodeFragment()

//        bottomNavigationView.menu.getItem(0).setChecked(false)
        bottomNavigationView.menu.getItem(1).setChecked(true)
//        bottomNavigationView.menu.getItem(2).setChecked(false)

        setFragment(1)//초기에는 메인프래그먼트

        //네비게이션 바 이벤트 리스너 설정


        bottomNavigationView.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.event_menu->{
//                    setFragment(0)
                    makeDialog(0)
                }
                R.id.cart_menu->{
                    setFragment(1)
                }
                R.id.search_menu->{
//                    setFragment(2)
                    makeDialog(2)
                }
            }
            bottomNavigationView.menu.getItem(1).setChecked(true)//항상 검색이 선택되도록(화면꺼지고)
            true
        }
    }

    fun makeDialog(n:Int){
        when(n){
            0->{
                //이벤트 다이얼로그
                var eventDialog = EventDialog(this)
                eventDialog.show()

                //크기 조절
                DialogSize(eventDialog)
            }
            2->{
                //검색 다이얼로그
                var searchDialog = SearchDialog(this)
                searchDialog.show()

                //크기조절
                DialogSize(searchDialog)
            }
        }
    }

    fun DialogSize(dialog:Dialog){
        //크기를 조절하는 함수
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)

        var window = dialog.window

        var x = (size.x * 1f).toInt()
        var y = (size.y * 0.8f).toInt()

        window!!.setLayout(x,y)


    }

    fun setFragment(n:Int){
        //프래그먼트 페이지 설정
        fm = supportFragmentManager
        ft = fm.beginTransaction()

        when(n){
            1->{
                //연동코드 입력 프래그먼트
                ft.replace(R.id.frameLayout,codeFrag)
                ft.commit()
            }
        }
    }
}
