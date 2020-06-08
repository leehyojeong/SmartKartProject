package com.example.myapplication.Activity

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.location.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.AttributeSet
import android.util.Base64
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedList
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.example.myapplication.CodePage.CodeFragment
import com.example.myapplication.Data.*
import com.example.myapplication.MainDialog.EventDialog.EventDialog
import com.example.myapplication.MainDialog.SearchDialog.SearchDialog
import com.example.myapplication.R
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import kotlinx.android.synthetic.main.activity_main2.*
import kotlin.collections.ArrayList
import com.google.android.gms.location.LocationListener
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_code.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader


class MainActivity : AppCompatActivity(),OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,LocationListener{

    //DATA
    //for application
    var martDataLocation:ArrayList<MyLocation> = arrayListOf()
    var eventData:ArrayList<EventItem> = arrayListOf()
    var product:HashMap<String, Product> = hashMapOf()

    //for map
    val UPDATE_INTERVAL_MS:Long = 100
    val FASTEST_UPDATE_INTERVAL_MS:Long = 50
    lateinit var mGoogleApiClient:GoogleApiClient
    lateinit var mMap:GoogleMap
    var mRequestingLocationUpdates:Boolean = false
    var mRequestLocationUpdates = false
    var mMoveMapByAPI = true
    var mMoveMapByUser = true
    lateinit var mCurrentLocation:Location
    lateinit var currentPosition:LatLng
    var currentMarker:Marker ?= null
    var locationRequest = LocationRequest()
        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
    //for permission
    var permissionArray = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
    val PERMISSION_REQUEST_ACCESS_FINE_LOCATION= 2002
    val GPS_ENABLE_REQUEST_CODE = 2001

    //Fragment Variable
    lateinit var mainFrag: MainFragment
    lateinit var codeFrag:CodeFragment
    lateinit var fm:FragmentManager
    lateinit var  ft:FragmentTransaction

    //Dialog Variable
    var eventDialog:Dialog ?= null
    var searchDialog:Dialog ?= null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)



         getFileMart()
        getFileEvent()
        getFileProduct()


        locationRequest.interval = UPDATE_INTERVAL_MS
        locationRequest.fastestInterval = FASTEST_UPDATE_INTERVAL_MS

        mGoogleApiClient = GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build()

        checkPermissions()
        setNavigation()
    }

    override fun onDestroy() {
        super.onDestroy()

        eventDialog!!.dismiss()
        searchDialog!!.dismiss()
        finish()
    }


    fun getFileString(inputStream:InputStreamReader):String{
        var inputStreamReader = inputStream
        var bufferedReader = BufferedReader(inputStreamReader)
        var returnString:String ?= ""
        var stringBuilder = StringBuilder()

        while(returnString != null){
            returnString = bufferedReader.readLine()
            stringBuilder.append(returnString)
        }

        return stringBuilder.toString()
    }


    //get data from inFile
    fun getFileMart(){
        var inputStream = openFileInput("amazon_mart.json")

        if(inputStream != null){
            var inputStreamReader = InputStreamReader(inputStream)
            var ret = getFileString(inputStreamReader)
            inputStream.close()
            var jArray = JSONArray(ret)
            for(index in 0 until jArray.length()){
                var jObj = JSONObject(jArray[index].toString())
                var name = jObj.getString("name")
                var latitude = jObj.getString("latitude").toDouble()
                var longitude = jObj.getString("longitude").toDouble()
                martDataLocation.add(MyLocation(latitude,longitude,name))
            }
        }
    }


    fun getFileEvent(){
        var inputStream = openFileInput("amazon_event.json")
        if(inputStream != null){
            var inputStreamReader = InputStreamReader(inputStream)
            var ret = getFileString(inputStreamReader)
            inputStream.close()
            var jArray = JSONArray(ret)
            for(index in 0 until jArray.length()){
                var jObj = JSONObject(jArray[index].toString())
                var name = jObj.getString("name")
                var img = jObj.getString("img")
                Log.d("비트맵",img)
                var encodeByte = Base64.decode(img,Base64.DEFAULT)
                Log.d("비트맵",encodeByte.toString())
                var bitmap = BitmapFactory.decodeByteArray(encodeByte,0,encodeByte.size)

                eventData.add(EventItem(name,bitmap))
            }
        }
    }

    fun getFileProduct(){
        var inputStream = openFileInput("amazon_product.json")
        if(inputStream != null) {
            var inputStreamReader = InputStreamReader(inputStream)
            var ret = getFileString(inputStreamReader)
            inputStream.close()
            var jArray = JSONArray(ret)
            for(index in 0 until jArray.length()){
                var jObj = JSONObject(jArray[index].toString())
                Log.d("제이슨",jObj.toString())

                var name = jObj.getString("name")
                Log.d("제이슨 이름",name)

                var num = jObj.getString("num").toInt()
                var price = jObj.getString("price").toInt()

                var category_id = jObj.getString("category_id")
                var gprice = jObj.getString("gprice")

                var img = jObj.getString("img")

                var encodeByte = Base64.decode(img,Base64.NO_WRAP)
                var bitmap = BitmapFactory.decodeByteArray(encodeByte,0,encodeByte.size)

                product.put(name,Product(bitmap,name,num,price,category_id,gprice))
            }
        }
    }

    //MAP
    override fun onLocationChanged(p0: Location?) {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        Log.d("확인","onLocationChanged")
        currentPosition = LatLng(p0!!.latitude,p0!!.longitude)

        setCurrentLocation(p0!!)
        mCurrentLocation = p0!!
        Log.d("권한","OnLocationChanged")
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
        Log.d("권한","OnMapReady")
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

    fun startLocationUpdates(){
        Log.d("권한","startLocationUpdates")
        if(!checkLocationServiesStatus()){
            showDialogForLocationServicesSetting()
        }else{
            if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
                //퍼미션이 없음
                Log.d("퍼미션","없음")
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

    fun checkLocationServiesStatus():Boolean{
        Log.d("권한","checkLocationServiesStatus")
        var locationmanager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        return locationmanager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationmanager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    fun setCurrentLocation(location:Location){
        mMoveMapByUser = false

        if(currentMarker != null) currentMarker!!.remove()

        var currentLatLng = LatLng(location.latitude,location.longitude)

        //나의 좌표
        var myLocation = Location("나")
        myLocation.latitude = location.latitude
        myLocation.longitude = location.longitude

        Log.d("권한", "나의 현재 위치 $location")

        //마트 좌표 찍기
        for (i in 0..martDataLocation!!.size-1){

            Log.d("마트좌표",martDataLocation!![i].toString())
            if(martDataLocation!![i].latitude == 0.0){
                continue
            }
            var markerOptions = MarkerOptions()


            //마트 좌표
            var martLocation = Location(martDataLocation!![i].name)
            martLocation.latitude = martDataLocation!![i].latitude!!
            martLocation.longitude = martDataLocation!![i].longitude!!

            if(myLocation.distanceTo(martLocation) <= 300000000){
                var latlng = LatLng(martDataLocation!![i].latitude!!,martDataLocation!![i].longitude!!)
                markerOptions.position(latlng)
                markerOptions.title(martDataLocation!![i].name)
                currentMarker = mMap.addMarker(markerOptions)
            }
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
        Log.d("권한","SetDefaultLocation")
    }


    //Permission
    fun checkPermissions(){

        var fineLocationRationale = ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.ACCESS_FINE_LOCATION)
        var hasFineLocationPermission = ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)

        if(hasFineLocationPermission == PackageManager.PERMISSION_DENIED && fineLocationRationale){
            showDialogForPermission("앱을 사용하려면 GPS사용을 활성화해야 합니다")
            Log.d("퍼미션","앱을 사용하려면 GPS사용활성화")
        }else if(hasFineLocationPermission == PackageManager.PERMISSION_DENIED && !fineLocationRationale){
            showDialogForPermission("설정에서 GPS사용을 활성화해주세요")
            Log.d("퍼미션","설정에서 GPS활성화 해주세요")
        }else if(hasFineLocationPermission == PackageManager.PERMISSION_GRANTED){
            if(mGoogleApiClient.isConnected == false){
                mGoogleApiClient.connect()
                Log.d("퍼미션","권한 허용")
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

    //GPS setting
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

    //NavigationBar
    fun setNavigation(){
        mainFrag = MainFragment()
        codeFrag = CodeFragment()

        bottomNavigationView.menu.getItem(1).setChecked(true)

        setFragment(1)//초기에는 메인프래그먼트

        //네비게이션 바 이벤트 리스너 설정

        bottomNavigationView.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.event_menu->{
                    makeDialog(0)
                }
                R.id.cart_menu->{
                    setFragment(1)
                }
                R.id.search_menu->{
                    makeDialog(2)
                    var mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
                    mapFragment.getMapAsync(this)
                }
            }
            bottomNavigationView.menu.getItem(1).setChecked(true)//항상 검색이 선택되도록(화면꺼지고)
            true
        }
    }

    //DIalog
    fun makeDialog(n:Int){
        when(n){
            0->{
                //이벤트 다이얼로그
                Log.d("이벤트","이벤트 클릭")
                eventDialog = EventDialog(this,eventData!!)
                eventDialog!!.show()
                eventDialog!!.setOnCancelListener {

                }
                //크기 조절
                DialogSize(eventDialog!!)
            }
            2->{
                //검색 다이얼로그
                searchDialog = SearchDialog(this,product)
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

    //Fragment
    fun setFragment(n:Int) {
        //프래그먼트 페이지 설정
        fm = supportFragmentManager
        ft = fm.beginTransaction()

        when (n) {
            1 -> {
                //연동코드 입력 프래그먼트
                ft.replace(R.id.frameLayout, CodeFragment.newInstace(product))
                ft.commit()
            }
        }
    }
}
