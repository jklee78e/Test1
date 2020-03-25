package com.example.test1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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

    private val bt by lazy{ BluetoothSPP(this)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d("cmd", "Runned")

        val options = NaverMapOptions()
            .camera(CameraPosition(LatLng(35.1798159, 129.0750222), 16.0))
            .mapType(NaverMap.MapType.Hybrid)
            .enabledLayerGroups(NaverMap.LAYER_GROUP_MOUNTAIN)

        val fm = supportFragmentManager

        val mapFragment = fm.findFragmentById(R.id.map) as MapFragment?
            ?: MapFragment.newInstance(options).also {
                fm.beginTransaction().add(R.id.map, it).commit()
            }
        mapFragment.getMapAsync(this)

        if(!bt.isBluetoothAvailable()) {
            Toast.makeText(this, "BT Ready", Toast.LENGTH_SHORT).show()
            finish()
        }
//https://blog.codejun.space/13

 /*       bt.setOnDataReceivedListener(BluetoothSPP.OnDataReceivedListener(){
            public void onDataReceived(var data : Byte[]; var message : String){
                Toast.makeText(this,message, toast.LENGTH_SHORT).show()
            }
        })*/
}

    @UiThread
    override fun onMapReady(naverMap: NaverMap) {
        Toast.makeText(this, "지도 준비 완료", Toast.LENGTH_SHORT).show()
//        naverMap.mapType = NaverMap.MapType.Hybrid
//        naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_MOUNTAIN, true)

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
                this, "${coord.latitude}, ${coord.longitude}",
                Toast.LENGTH_SHORT
            ).show()
            val marker = Marker()
            marker.position = LatLng(coord.latitude, coord.longitude)
            marker.map = naverMap

        }
    }

}
