package com.example.test1

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.view.Display
import android.widget.Button
import android.widget.Toast
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import app.akexorcist.bluetotohspp.library.BluetoothSPP
import app.akexorcist.bluetotohspp.library.BluetoothState
import app.akexorcist.bluetotohspp.library.DeviceList
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.NaverMapOptions
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import org.jetbrains.anko.toast
import java.text.ChoiceFormat.nextDouble
import kotlin.concurrent.timer


// https://github.com/Bliends/BlueTooth/tree/master/app/src/main/java/com/bliends/bluetooth 블루투스 레퍼런스
class MainActivity : AppCompatActivity(), OnMapReadyCallback {

//    private val bt by lazy{ BluetoothSPP(this)}
    private var btService: BlutoothService? = null
    var bt : BluetoothSPP = BluetoothSPP(this)

    lateinit var mnaverMap : NaverMap
    private lateinit var locationSource: FusedLocationSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getPermission()
/*
        if (btService == null){
            btService = BlutoothService(this,handler)
        }
        */
        // Log 사용을 위한 임시방법


        Log.d("cmd", "Runned")
        // 네이버 맵 옵션 설정
        val options = NaverMapOptions() // 옵션을 가져오고
//            .camera(CameraPosition(LatLng(35.1798159, 129.0750222), 16.0)) // 초기위치를 선정한다.
            .mapType(NaverMap.MapType.Hybrid)   // 맵 종류는 하이브리드로 하고
            .enabledLayerGroups(NaverMap.LAYER_GROUP_MOUNTAIN)  // 산에 패스를 보여주는 레이어를 보여준다.

        // 프래그먼트로 네이버 맵 보여주기
        val fm = supportFragmentManager // 프래그먼트로 네이버 맵을 보여준다.
        // 맵 프래그먼트를 설정하고 가져온다.
        val mapFragment = fm.findFragmentById(R.id.map) as MapFragment?
            ?: MapFragment.newInstance(options).also {
                fm.beginTransaction().add(R.id.map, it).commit()
            }
        // 초기 실행시 OnMapReady가 수행되므로 그 부분에서 핸들을 가져온다.
        mapFragment.getMapAsync(this)
        locationSource =
            FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

        //BluetoothSPP 를 사용하고. 초반에 설정 해 준다.
        if(!bt.isBluetoothAvailable()) {
            toast("BT NOT READY")
//            Toast.makeText(this, "BT Ready", Toast.LENGTH_SHORT).show()
            finish()
        }
        // 버튼별 콜 사용방법

        var btnConn = findViewById(R.id.CONN) as Button
        var btnDisCon = findViewById(R.id.DISCON) as Button
        var btnSend = findViewById(R.id.SEND) as Button
        var btnShift = findViewById(R.id.SHIFT) as Button
        var cntTimer = 0

        val timer= timer(period = 1000) {

            cntTimer++
            runOnUiThread {
                btnShift.text = "$cntTimer 회 타이머 울림"


            }
        }

        btnConn.setOnClickListener{
            Toast.makeText(this, "Conn Pressed", Toast.LENGTH_SHORT).show()

                if (bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
                    bt.disconnect();
                } else {
                    val intent: Intent = Intent(this@MainActivity,DeviceList::class.java)
                    startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
                }


        }
        btnDisCon.setOnClickListener{
            Toast.makeText(this, "DisConn Pressed", Toast.LENGTH_SHORT).show()
        }
        btnSend.setOnClickListener{
            Toast.makeText(this, "Send Pressed", Toast.LENGTH_SHORT).show()
        }

        bt!!.setBluetoothConnectionListener(object : BluetoothSPP.BluetoothConnectionListener {
            override fun onDeviceDisconnected() {
                toast("기기와 연결이 끊어졌습니다.")
            }

            override fun onDeviceConnected(name: String?, address: String?) {
                toast("Connected to " + name + "\n" + address)
            }

            override fun onDeviceConnectionFailed() {
                toast("기기와 연걸이 실패하였습니다.")
            }
        })

        val marker = Marker()

        bt.setOnDataReceivedListener(object : BluetoothSPP.OnDataReceivedListener {
            override fun onDataReceived(data: ByteArray?, message: String) {
//                Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
//                toast(message!!)
                val startindex : Int? = message.indexOf("[",0,false)
                val endindex : Int? = message.indexOf("]",0,false)

                if((startindex != null) && (endindex != null) &&(startindex < endindex)){
                    var submsg:String = message.substring(startindex+1, endindex-1)
                    val GpsPos = submsg.split(",")
                    toast(GpsPos[0]+" + "+GpsPos[1])

                    marker.position = LatLng(GpsPos[0].toDouble()+nextDouble(0.000001), GpsPos[1].toDouble()+nextDouble(0.000001))


                    val disp : Display = getWindowManager().getDefaultDisplay()
                    val size : Point = Point()
                    disp.getSize(size)

                    marker.width = size.x/20
                    marker.height = size.y/25
                    marker.map = mnaverMap
                }

                // 디코딩 코드 추가
                // 디코딩을 쓰레드로 돌리기기

            }
        })


        bt.setAutoConnectionListener(object : BluetoothSPP.AutoConnectionListener {
            override fun onNewConnection(name: String, address: String) {
                //새로운 연결일때
                Log.e("new", "succes")
            }


            override fun onAutoConnectionStarted() {
                //자동 연결
                Log.e("auto", "succes")
                toast("모듈과 정상적으로 연결되었습니다.")
            }
        })


        btnShift.setOnClickListener{

        }


    }

    public override fun onStart() {
        super.onStart();
        if (!bt.isBluetoothEnabled()) { //
            val intent:Intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        } else {
            if (!bt.isServiceAvailable()) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER); //DEVICE_ANDROID는 안드로이드 기기 끼리
                //bt!!.autoConnect("KEPCORTK")
            }
        }
/*
        if (!bt.isBluetoothEnabled()) {
            bt.enable();
        } else {
            if (!bt.isServiceAvailable()) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER)
                bt!!.autoConnect("KEPCORTK")
            }
        }*/
    }

    override fun onDestroy() {
        super.onDestroy()
        bt.stopService()
    }



    //권한 설정
    fun getPermission() {
        ActivityCompat.requestPermissions(this@MainActivity,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION),
            0)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == 0) {
            if (grantResults[0] != 0) {
                Toast.makeText(this, "권한이 거절 되었습니다. 어플을 이용하려면 권한을 승낙하여야 합니다.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
        if (locationSource.onRequestPermissionsResult(requestCode, permissions,
                grantResults)) {
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    }

    @UiThread
    override fun onMapReady(naverMap: NaverMap) {
        naverMap.locationSource = locationSource

        Toast.makeText(this, "지도 준비 완료", Toast.LENGTH_SHORT).show()
//        naverMap.mapType = NaverMap.MapType.Hybrid
//        naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_MOUNTAIN, true)
//        naverMap.uiSettings.isZoomControlEnabled = false

        val southWest = LatLng(31.43, 122.37)
        val northEast = LatLng(44.35, 132.0)
        val bounds = LatLngBounds(southWest, northEast)

        /*
        val bounds = LatLngBounds.Builder()
            .include(LatLng(38.5640984, 126.9712268))
            .include(LatLng(37.5651279, 126.9767904))
            .include(LatLng(37.5625365, 126.9832241))
            .include(LatLng(37.5585305, 126.9809297))
            .include(LatLng(37.5590777, 126.974617))
            .build()
    */
/*        val marker = Marker()
        val locationOverlay=naverMap.locationOverlay
        marker.map = naverMap
*/
        //infoWindow.open(marker)

        naverMap.setOnMapClickListener { point, coord ->
            Toast.makeText(
                this, "경도 :   ${coord.latitude}\r\n위도 : ${coord.longitude}",

                Toast.LENGTH_SHORT
            ).show()

            val marker = Marker()
            marker.position = LatLng(coord.latitude, coord.longitude)

            val disp : Display = getWindowManager().getDefaultDisplay()
            val size : Point = Point()
            disp.getSize(size)

            marker.width = size.x/20
            marker.height = size.y/25
            marker.captionText = "경도 :   ${coord.latitude}\r\n위도 : ${coord.longitude}"
            marker.map = naverMap

            }
        mnaverMap = naverMap

    }
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK)
                bt.connect(data);
        } else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
                bt!!.autoConnect("KEPCORTK")
            } else {
                Toast.makeText(getApplicationContext()
                    , "Bluetooth was not enabled."
                    , Toast.LENGTH_SHORT).show();
                finish();
            }
        }


    }

}
