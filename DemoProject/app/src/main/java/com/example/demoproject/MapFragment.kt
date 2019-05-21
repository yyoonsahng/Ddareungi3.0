package com.example.demoproject


import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.fragment_map.*

const val DEFAULT_ZOOM = 16f

class MapFragment : Fragment(), OnMapReadyCallback {
    lateinit var mMap: GoogleMap    //GoogleMap 객체
    var mapView: MapView? = null    //GoogleMap을 보여주는 MapView
    var mLocationPermissionGranted = false  //GPS 권환 획득 유무를 확인하는 flag 값
    lateinit var fusedLocationClient: FusedLocationProviderClient   //휴대폰이 마지막으로 얻은 내 위치를 얻어오기 위한 객체
    //const val DEFAULT_ZOOM = 16f
    val KONKUK_UNIV = LatLng(37.540, 127.07)

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap!!
        //val behavior = BottomSheetBehavior.from(bottom_sheet)
        //behavior.isHideable = true

        if (mLocationPermissionGranted) {
            //앱을 실행시켰을 때 내 위치로 지도를 이동시킴
            fusedLocationClient.lastLocation.addOnSuccessListener {
                mMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(it.latitude, it.longitude)))
            }

            //내 위치로 지도 이동(GPS) 버튼 클릭 리스너
            mMap.isMyLocationEnabled = true
            mMap.setOnMyLocationButtonClickListener {
                //Toast.makeText(context, "My Location button clicked", Toast.LENGTH_SHORT).show()
                fusedLocationClient.lastLocation.addOnSuccessListener {
                    mMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(it.latitude, it.longitude),
                            DEFAULT_ZOOM
                        ), 500, null
                    )
                }
                /*true 값 반환 : 디폴트로 설정되어 있는 GPS 버튼에 대한 행동을 실행하지 않음
                  false 값 반환 : 디폴트로 설정되어 있는 행동 실행(camera를 내 위치로 이동)*/
                true
            }
        } else {
            //GPS 권한이 없는 경우 지도 초기 값을 건국대학교 위치로 설정
            mMap.isMyLocationEnabled = false
            mMap.moveCamera(CameraUpdateFactory.newLatLng(KONKUK_UNIV))
        }
        //지도에 marker 생성
        val mMapController = MapController(context!!, mMap)
        mMapController.setMarker()

        //marker의 클릭 리스너, 따릉이 정류소 선택 했을 때 bottom sheet 보이게
        mMap.setOnMarkerClickListener {
            mMap.animateCamera(CameraUpdateFactory.newLatLng(it.position), 500, null)
//            behavior.state = BottomSheetBehavior.STATE_HIDDEN
            val stopMarkerItem: StopMarkerItem = it.tag as StopMarkerItem
            bottom_sheet.visibility = View.VISIBLE
            bottom_sheet_text_view.text = "${stopMarkerItem.id}. ${stopMarkerItem.name}"
//            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            true
        }

        //지도를 클릭 했을 때 bottom sheet 다시 안 보이게
        mMap.setOnMapClickListener {
//            behavior.peekHeight = 0
//            behavior.state = BottomSheetBehavior.STATE_COLLAPSED
            //behavior.state = BottomSheetBehavior.STATE_HIDDEN
            bottom_sheet.visibility = View.GONE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val layout = inflater.inflate(R.layout.fragment_map, container, false)
        mapView = layout.findViewById(R.id.mapView)
        mapView!!.onCreate(savedInstanceState)
        mapView!!.getMapAsync(this)
        return layout
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mLocationPermissionGranted = arguments!!.getBoolean("mLocationPermissionGranted")
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context!!)
    }

    override fun onStart() {
        super.onStart()
        mapView!!.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView!!.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView!!.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView!!.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView!!.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView!!.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView!!.onLowMemory()
    }
}
