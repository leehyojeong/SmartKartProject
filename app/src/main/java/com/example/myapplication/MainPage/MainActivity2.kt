package com.example.myapplication.MainPage

import android.annotation.SuppressLint
import android.app.Application
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Point
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Display
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.myapplication.CodePage.CodeFragment
import com.example.myapplication.MainPage.EventDialog.EventDialog
import com.example.myapplication.MainPage.SearchDialog.SearchDialog
import com.example.myapplication.R
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_main2.*
import kotlinx.android.synthetic.main.search_dialog.*
import java.util.jar.Manifest

class MainActivity2 : AppCompatActivity(),OnMapReadyCallback{

    lateinit var mainFrag:MainFragment
    lateinit var codeFrag:CodeFragment
    lateinit var fm:FragmentManager
    lateinit var  ft:FragmentTransaction



    val PERMISSION_REQUEST_ACCESS_FINE_LOCATION= 5123
    var permissionArray = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)

    //나의 위치 관련된 변수
    lateinit var locationManager:LocationManager
    var latitude = 0.0 as Double//위도
    var longitude = 0.0 as Double//경도


    @SuppressLint("MissingPermission")
    override fun onMapReady(p0: GoogleMap?) {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        var locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        var locationProvider = LocationManager.GPS_PROVIDER
        var currentLocation = locationManager.getLastKnownLocation(locationProvider)
        if(currentLocation!=null){
            latitude = currentLocation.latitude
            longitude = currentLocation.longitude
        }

        Log.d("확인","ONMapReadey "+latitude.toString()+" "+longitude.toString())

        var position = LatLng(latitude,longitude)
        var markerOption = MarkerOptions()
        markerOption.position(position)
        markerOption.title("서울")
        markerOption.snippet("제발")

        p0!!.addMarker(markerOption)

        var center = CameraUpdateFactory.newLatLngZoom(position,17f) as CameraUpdate
        p0!!.moveCamera(center)
        p0!!.animateCamera(CameraUpdateFactory.zoomTo(17f))
    }

    //권한설정
    fun initPermission(){
        if(!checkPermission(permissionArray)){
            askPermission(permissionArray,PERMISSION_REQUEST_ACCESS_FINE_LOCATION)
        }
    }

    fun checkPermission(requestPermission:Array<String>):Boolean{
        val requestResult = BooleanArray(requestPermission.size)
        for(i in requestResult.indices){
            requestResult[i] = ContextCompat.checkSelfPermission(this,requestPermission[i])==PackageManager.PERMISSION_GRANTED
            if(!requestResult[i]){
                return false
            }
        }
        return true
    }

    fun askPermission(requestPermission:Array<String>,REQ_PERMISSION:Int){
        ActivityCompat.requestPermissions(this,requestPermission,REQ_PERMISSION)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            PERMISSION_REQUEST_ACCESS_FINE_LOCATION->{
                if(checkPermission(permissions)){
                    //권한승인됨
                }else{
                    finish()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    //여기까지 권한 받아오는 코드(위에)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.myapplication.R.layout.activity_main2)
        initPermission()
        setNavigation()

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager


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
                com.example.myapplication.R.id.event_menu->{
//                    setFragment(0)
                    makeDialog(0)
                }
                com.example.myapplication.R.id.cart_menu->{
                    setFragment(1)
                }
                com.example.myapplication.R.id.search_menu->{
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

                requestMyLocation()
                //구글지도
//                var fragmentManager = supportFragmentManager
//                var map = fragmentManager.findFragmentById(R.id.map) as SupportMapFragment
//
//                map!!.getMapAsync {
//                    Log.d("확인","구글지도")
//                    var SEOUL = LatLng(37.56, 126.97)
//
//                    var markerOption = MarkerOptions()
//                    markerOption.position(SEOUL)
//                    markerOption.title("서울")
//                    markerOption.snippet("시발")
//
//                    it.addMarker(markerOption)
//
//                    it.moveCamera(CameraUpdateFactory.newLatLng(SEOUL))
//                    it.animateCamera(CameraUpdateFactory.zoomTo(10f))
//                }
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
                ft.replace(com.example.myapplication.R.id.frameLayout,codeFrag)
                ft.commit()
            }
        }
    }

    //나의 위치 받아오는 함수

    fun setMyLocation(){
        var mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment!!.getMapAsync(this)
    }

    fun requestMyLocation(){
        Log.d("확인","requestMyLocation")

        //수동으로 위치 구하기
        setMyLocation()

        if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
            return
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,100,10f,object:LocationListener{
            override fun onLocationChanged(p0: Location?) {
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                Log.d("확인","onLocationChanged")
                locationManager.removeUpdates(this)//나의 위치를 한번만 가져옴

                latitude = p0!!.latitude
                longitude = p0!!.longitude
                Log.d("확인",latitude.toString())
                Log.d("확인",longitude.toString())

                var fragmentManager = supportFragmentManager
                var map = fragmentManager.findFragmentById(R.id.map) as SupportMapFragment

                map!!.getMapAsync {
                    var position = LatLng(latitude,longitude)
                    var markerOption = MarkerOptions()
                    markerOption.position(position)
                    markerOption.title("서울")
                    markerOption.snippet("시발")

                    it.addMarker(markerOption)

                    var center = CameraUpdateFactory.newLatLngZoom(position,17f) as CameraUpdate

                    it.moveCamera(center)
                    it.animateCamera(CameraUpdateFactory.zoomTo(17f))
                }
                locationManager.removeUpdates(this)
            }

            override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onProviderEnabled(p0: String?) {
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onProviderDisabled(p0: String?) {
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        })
    }


}
