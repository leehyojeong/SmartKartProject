package com.example.myapplication.CodePage


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.myapplication.BuyPage.BuyListFragment
import com.example.myapplication.Data.Product

import com.example.myapplication.R
import kotlinx.android.synthetic.main.fragment_code.*
import kotlinx.android.synthetic.main.fragment_code.view.*

/**
 * A simple [Fragment] subclass.
 */
class CodeFragment : Fragment() {

    lateinit var fm: FragmentManager
    lateinit var  ft: FragmentTransaction
    lateinit var buyFrag:BuyListFragment
    lateinit var codeBtn:Button
    lateinit var enterCode:EditText
    lateinit var product:HashMap<String,Product>

    companion object{
        fun newInstace(product:HashMap<String,Product>):Fragment{
            var codeFrag = CodeFragment()
            var args = Bundle()
            args.putSerializable("PRODUCT",product)
            codeFrag.arguments = args
            return codeFrag
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var v = inflater.inflate(R.layout.fragment_code, container, false)
        codeBtn = v.codeBtn
        enterCode = v.codeNum
        init()
        return v
    }

    fun init(){
        codeBtn.setOnClickListener {
            codebtnClick()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(arguments != null){
            this.product = arguments!!.getSerializable("PRODUCT") as HashMap<String,Product>
        }
    }

    fun codebtnClick(){
        //확인 버튼을 클릭했을 때
        var code = enterCode.text.toString()//입력한 코드 번호
        //각 지역의 매장마다의 코드 번호와 비교해야함
        var tmpCode = "123"//임의의 매장 카트 코드번호
        if(code == tmpCode){
            //코드 번호가 같은 것이 있는 경우 사용 가능
            buyFrag = BuyListFragment()
            setFragment()
        }
    }

    fun setFragment(){
        //프래그먼트 페이지 설정
        fm = activity!!.supportFragmentManager
        ft = fm.beginTransaction()

        ft.replace(R.id.frameLayout,BuyListFragment.newInstace(product))
        ft.commit()
    }

    override fun onDestroy() {
        super.onDestroy()

    }
}
