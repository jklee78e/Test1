package com.example.test1

import android.app.Activity
import android.content.Intent
import android.graphics.Point
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Display
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.annotation.UiThread
import androidx.fragment.app.FragmentActivity
import app.akexorcist.bluetotohspp.library.BluetoothSPP
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.*
import com.naver.maps.map.NaverMap.LAYER_GROUP_MOUNTAIN
import com.naver.maps.map.overlay.Marker



class MainActivity : AppCompatActivity(), OnMapReadyCallback {

//    private val bt by lazy{ BluetoothSPP(this)}
    var bt : BluetoothSPP = BluetoothSPP(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Log 사용을 위한 임시방법
        Log.d("cmd", "Runned")
        // 네이버 맵 옵션 설정
        val options = NaverMapOptions() // 옵션을 가져오고
            .camera(CameraPosition(LatLng(35.1798159, 129.0750222), 16.0)) // 초기위치를 선정한다.
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

        //BluetoothSPP 를 사용하고. 초반에 설정 해 준다.
        if(!bt.isBluetoothAvailable()) {
            Toast.makeText(this, "BT Ready", Toast.LENGTH_SHORT).show()
        }
        // 버튼별 콜 사용방법

        val btnConn = findViewById(R.id.CONN) as Button
        val btnDisCon = findViewById(R.id.DISCON) as Button
        val btnSend = findViewById(R.id.SEND) as Button
        val btnShift = findViewById(R.id.SHIFT) as Button

        btnConn.setOnClickListener{
            Toast.makeText(this, "Conn Pressed", Toast.LENGTH_SHORT).show()
        }
        btnDisCon.setOnClickListener{
            Toast.makeText(this, "DisConn Pressed", Toast.LENGTH_SHORT).show()
        }
        btnSend.setOnClickListener{
            Toast.makeText(this, "Send Pressed", Toast.LENGTH_SHORT).show()
        }
        btnShift.setOnClickListener{
            val intent : Intent = Intent(this, SubActivity::class.java)
            startActivity(intent)

        }


    }


    @UiThread
    override fun onMapReady(naverMap: NaverMap) {
        Toast.makeText(this, "지도 준비 완료", Toast.LENGTH_SHORT).show()
//        naverMap.mapType = NaverMap.MapType.Hybrid
//        naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_MOUNTAIN, true)
        naverMap.uiSettings.isZoomControlEnabled = false

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
    }

}
