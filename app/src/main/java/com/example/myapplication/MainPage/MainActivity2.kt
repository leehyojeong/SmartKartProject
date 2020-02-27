package com.example.myapplication.MainPage

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Point
import android.location.*
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Display
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.myapplication.CodePage.CodeFragment
import com.example.myapplication.MainPage.EventDialog.EventDialog
import com.example.myapplication.MainPage.SearchDialog.SearchDialog
import com.example.myapplication.R
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApi
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import kotlinx.android.synthetic.main.activity_main2.*
import kotlinx.android.synthetic.main.search_dialog.*
import org.json.JSONObject
import java.util.*
import java.util.jar.Manifest
import kotlin.collections.ArrayList
import com.google.android.gms.location.LocationListener
import com.google.android.gms.maps.model.*

class MainActivity2 : AppCompatActivity(),OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,LocationListener{

    override fun onLocationChanged(p0: Location?) {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        Log.d("확인","onLocationChanged")
        currentPosition = LatLng(p0!!.latitude,p0!!.longitude)

        setCurrentLocation(p0!!)
        mCurrentLocation = p0!!
    }

    override fun onMapReady(p0: GoogleMap?) {
       // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        mMap = p0!!

        setDefaultLocation()

        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15f))
        mMap.setOnMapClickListener {
            mMoveMapByAPI = true
            true
        }
        mMap.setOnMapClickListener {

        }
        mMap.setOnCameraIdleListener {
            if(mMoveMapByUser && mRequestingLocationUpdates){
                mMoveMapByAPI = false
                //위치에 따른 카메라 이동 비활성화
            }
            mMoveMapByUser = true
        }
        mMap.setOnCameraMoveCanceledListener {

        }
        startLocationUpdates()
    }

    override fun onConnected(p0: Bundle?) {
      //  TODO("not implemented") //To change body of created functions use File | Settings | File Templates.

    }

    override fun onConnectionSuspended(p0: Int) {
     //   TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
      //  TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        setDefaultLocation()
    }

    override fun onStart() {
        if(mGoogleApiClient != null && mGoogleApiClient.isConnected == false){
            mGoogleApiClient.connect()
        }
        super.onStart()
    }

    override fun onStop() {
        if(mRequestingLocationUpdates){
            stopLocationUpdates()
        }
        if(mGoogleApiClient.isConnected){
            mGoogleApiClient.disconnect()
        }
        super.onStop()
    }

    lateinit var mainFrag:MainFragment
    lateinit var codeFrag:CodeFragment
    lateinit var fm:FragmentManager
    lateinit var  ft:FragmentTransaction



    //대형마트 정보
    var martArray:ArrayList<mart> = arrayListOf()


    //현재위치
    var permissionArray = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
    val PERMISSION_REQUEST_ACCESS_FINE_LOCATION= 2002
    val GPS_ENABLE_REQUEST_CODE = 2001
    val UPDATE_INTERVAL_MS:Long = 100
    val FASTEST_UPDATE_INTERVAL_MS:Long = 50
    lateinit var mGoogleApiClient:GoogleApiClient
     var mLocationPermissionGranted:Boolean = false
    lateinit var mMap:GoogleMap
    var mRequestingLocationUpdates:Boolean = false
    var mLastKnownLocation:Location ?= null
    var askPermissionOnceAgain = false
    var mRequestLocationUpdates = false
    var mMoveMapByAPI = true
    var mMoveMapByUser = true
    lateinit var mCurrentLocation:Location
    lateinit var currentPosition:LatLng
    var currentMarker:Marker ?= null

    var locationRequest = LocationRequest()
        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

    var eventDialog:Dialog ?= null
    var searchDialog:Dialog ?= null



    var martAddress = arrayListOf("서울특별시 송파구 충민로 10","서울특별시 노원구 마들로3길 15","서울 도봉구 노해로 65길 4")

    //지도
    fun startLocationUpdates(){
        Log.d("확인","startLocationUpdates")
        if(!checkLocationServiesStatus()){
            showDialogForLocationServicesSetting()
        }else{
            if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
                //퍼미션이 없음
                return
            }

            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,locationRequest,this)
            mRequestLocationUpdates = true
            mMap.isMyLocationEnabled = true
        }
    }

    fun stopLocationUpdates(){
        mRequestingLocationUpdates = false
    }

    fun getCurrentAddress(latlng:LatLng):String?{
        var geocoder = Geocoder(this,Locale.getDefault())

        var address = arrayListOf<Address>()
        address = geocoder.getFromLocation(latlng.latitude,latlng.longitude,1) as ArrayList<Address>

        if(address == null || address.size == 0){
            return null
        }else{
            var add = address.get(0)
            return add.getAddressLine(0).toString()
        }

    }

    fun checkLocationServiesStatus():Boolean{
        var locationmanager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        return locationmanager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationmanager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    fun setCurrentLocation(location:Location){
        mMoveMapByUser = false

        if(currentMarker != null) currentMarker!!.remove()

        var currentLatLng = LatLng(location.latitude,location.longitude)

//        var markerOptions = MarkerOptions()
//        markerOptions.position(currentLatLng)
//        markerOptions.title("나")
//        markerOptions.draggable(true)
//
//        currentMarker = mMap.addMarker(markerOptions)
        //반경
//        var circle = CircleOptions().center(currentLatLng)
//            .radius(3000.0)
//            .strokeWidth(0f)
//            .fillColor(Color.parseColor("#FFBB00ff"))
        //나의 좌표
        var myLocation = Location("나")
        myLocation.latitude = location.latitude
        myLocation.longitude = location.longitude

        //마트 좌표 찍기
        var geocoder = Geocoder(this,Locale.getDefault())
        for (i in martAddress){
            var markerOptions = MarkerOptions()

            var addresses = arrayListOf<Address>()
            addresses = geocoder.getFromLocationName(i,1) as ArrayList<Address>

            var latitude = addresses.get(0).latitude
            var longitude = addresses.get(0).longitude

            //마트 좌표
            var martLocation = Location("마트")
            martLocation.latitude = latitude
            martLocation.longitude = longitude

            if(myLocation.distanceTo(martLocation) <= 3000){
                var latlng = LatLng(latitude,longitude)
                markerOptions.position(latlng)
                markerOptions.title("마트")
                currentMarker = mMap.addMarker(markerOptions)
            }
//            mMap.addCircle(circle)
        }

        if(mMoveMapByAPI){
            var cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng)
            mMap.moveCamera(cameraUpdate)
        }
    }

    fun setDefaultLocation(){
        mMoveMapByUser = false

        var DEFAULT_LOCATION = LatLng(37.56,126.97)

        if(currentMarker != null) currentMarker!!.remove()

        var markerOptions = MarkerOptions()
        markerOptions.position(DEFAULT_LOCATION)
        markerOptions.title("초기값")
        markerOptions.draggable(true)
        currentMarker = mMap.addMarker(markerOptions)

        var cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION,15f)
        mMap.moveCamera(cameraUpdate)
    }


    fun checkPermissions(){
        var fineLocationRationale = ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.ACCESS_FINE_LOCATION)
        var hasFineLocationPermission = ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)

        if(hasFineLocationPermission == PackageManager.PERMISSION_DENIED && fineLocationRationale){
            showDialogForPermission("앱을 사용하려면 GPS사용을 활성화해야 합니다")
        }else if(hasFineLocationPermission == PackageManager.PERMISSION_DENIED && !fineLocationRationale){
            showDialogForPermission("설정에서 GPS사용을 활성화해주세요")
        }else if(hasFineLocationPermission == PackageManager.PERMISSION_GRANTED){
            if(mGoogleApiClient.isConnected == false){
                mGoogleApiClient.connect()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == PERMISSION_REQUEST_ACCESS_FINE_LOCATION && grantResults.size > 0){
            var permissionAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
            if(permissionAccepted){
                if(mGoogleApiClient.isConnected == false){
                    mGoogleApiClient.connect()
                }
            }else{
                checkPermissions()
            }
        }
    }

    fun showDialogForPermission(msg:String){
        var builder = AlertDialog.Builder(this)
        builder.setTitle("알림")
        builder.setMessage(msg)
        builder.setCancelable(false)
        builder.setPositiveButton("예",DialogInterface.OnClickListener { dialogInterface, i ->
            ActivityCompat.requestPermissions(this,permissionArray,PERMISSION_REQUEST_ACCESS_FINE_LOCATION)
        })
        builder.setNegativeButton("아니오",DialogInterface.OnClickListener { dialogInterface, i ->
            finish()
        })
        builder.create().show()
    }

    fun showDialogForLocationServicesSetting(){
        var builder = AlertDialog.Builder(this)
        builder.setTitle("위치 서비스 설정")
        builder.setMessage("앱을 사용하기 위해 위치 서비스가 필요합니다")
        builder.setCancelable(true)
        builder.setPositiveButton("설정",DialogInterface.OnClickListener { dialogInterface, i ->
            var callGPSSettingIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivityForResult(callGPSSettingIntent,GPS_ENABLE_REQUEST_CODE)
        })
        builder.setNegativeButton("취소",DialogInterface.OnClickListener { dialogInterface, i ->
            dialogInterface.cancel()
        })
        builder.create().show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            GPS_ENABLE_REQUEST_CODE->{
                if(checkLocationServiesStatus()){
                    if(mGoogleApiClient.isConnected == false){
                        mGoogleApiClient.connect()
                    }
                    return
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.myapplication.R.layout.activity_main2)
        setNavigation()

        val thread = JSONThread()
        thread.start()//json파일 읽기 스레드

        locationRequest.interval = UPDATE_INTERVAL_MS
        locationRequest.fastestInterval = FASTEST_UPDATE_INTERVAL_MS

        mGoogleApiClient = GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build()


    }



    inner class JSONThread:Thread(){
        override fun run() {
            super.run()
            readJSON()
        }
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
                    var mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
                    mapFragment.getMapAsync(this)
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

                eventDialog = EventDialog(this)
                eventDialog!!.show()

                eventDialog!!.setOnCancelListener {

                }

                //크기 조절
                DialogSize(eventDialog!!)
            }
            2->{
                //검색 다이얼로그

                searchDialog = SearchDialog(this)
                searchDialog!!.show()

                searchDialog!!.setOnCancelListener {
                    var f2 = supportFragmentManager.beginTransaction()
                    f2.remove(supportFragmentManager.findFragmentById(R.id.map)!!)
                    f2.commit()
                }
                //크기조절
                DialogSize(searchDialog!!)
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

    fun readJSON(){

        var geocoder = Geocoder(this)

        val assetManager = this.resources.assets
        val inputStream = assetManager.open("서울시.json")
        val jsonString = inputStream.bufferedReader().use { it.readText() }

        val jObject = JSONObject(jsonString)//jsonObject로 변환
        val jArray = jObject.getJSONArray("DATA")

        for (i in 0 until jArray.length()){
            val obj = jArray.getJSONObject(i)
//            Log.d("json",obj.toString())
            //분류가 대형마트인 것만 가져옴
            if(obj.getString("uptae_code").equals("대형마트") && obj.getString("mng_state_code").equals("영업중") && obj.getString("trnm_nm").contains("이마트")){
//                Log.d("대형마트",obj.getString("trnm_nm"))
                var geoAddress = geocoder.getFromLocationName(obj.getString("trnm_nm"),10)
                Log.d("대형마트",geoAddress.toString())
                if(geoAddress.size != 0){
                    //데이터가 조회되면
                    //정규식
                    var latitudeStr = geoAddress.get(0).toString().substringAfter("latitude=")
                    latitudeStr = latitudeStr.substringBefore(",")
                    var latitude = latitudeStr.toDouble()
                    var longitudeStr = geoAddress.get(0).toString().substringAfter("longitude=")
                    longitudeStr = longitudeStr.substringBefore(",")
                    var longitude = longitudeStr.toDouble()


                    Log.d("위치",latitude.toString()+ " "+longitude.toString())
                    martArray.add(mart(obj.getString("trnm_nm"),latitude,longitude))


                }
            }
        }
    }

}
