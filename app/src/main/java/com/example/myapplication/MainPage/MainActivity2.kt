package com.example.myapplication.MainPage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.myapplication.CodePage.CodeFragment
import com.example.myapplication.R
import kotlinx.android.synthetic.main.activity_main2.*

class MainActivity2 : AppCompatActivity() {

    lateinit var eventFrag:EventFragment
    lateinit var mainFrag:MainFragment
    lateinit var searchFrag:SearchFragment
    lateinit var codeFrag:CodeFragment
    lateinit var fm:FragmentManager
    lateinit var  ft:FragmentTransaction

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        setNavigation()
    }

    fun setNavigation(){
        eventFrag = EventFragment()
        mainFrag = MainFragment()
        searchFrag = SearchFragment()

        setFragment(1)//초기에는 메인프래그먼트

        //네비게이션 바 이벤트 리스너 설정
        bottomNavigationView.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.event_menu->{
                    setFragment(0)
                }
                R.id.cart_menu->{
                    setFragment(3)
                }
                R.id.search_menu->{
                    setFragment(2)
                }
            }
            true
        }
    }

    fun setFragment(n:Int){
        //프래그먼트 페이지 설정
        fm = supportFragmentManager
        ft = fm.beginTransaction()

        when(n){
            0->{
                //이벤트 프래그먼트일 때
                ft.replace(R.id.frameLayout,eventFrag)
                ft.commit()
            }
            1->{
                //메인 프래그먼트일 때
                ft.replace(R.id.frameLayout,mainFrag)
                mainFrag.setInit()
                ft.commit()
            }
            2->{
                //검색 프래그먼트일 때
                ft.replace(R.id.frameLayout,searchFrag)
                ft.commit()
            }
            3->{
                //연동코드 입력 프래그먼트
                ft.replace(R.id.frameLayout,codeFrag)
                ft.commit()
            }
        }
    }
}
