package com.example.ddareungi


import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.a190306app.MyBike
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker


const val DEFAULT_ZOOM = 16f

class MapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnCameraMoveListener {

    lateinit var mMap: GoogleMap
    var mapView: MapView? = null    //GoogleMap을 보여주는 MapView
    var mLocationPermissionGranted = false  //GPS 권환 획득 유무를 확인하는 flag 값
    lateinit var fusedLocationClient: FusedLocationProviderClient   //휴대폰이 마지막으로 얻은 내 위치를 얻어오기 위한 객체
    val KONKUK_UNIV = LatLng(37.540, 127.07)
    lateinit var mBikeList: MutableList<MyBike>
    val visibleMarkers = mutableMapOf<String, Marker>()
    lateinit var markers: Markers
    lateinit var myLocation: Location


    fun setData(locationPermissionGranted: Boolean, bikeList: MutableList<MyBike>) {
        mLocationPermissionGranted = locationPermissionGranted
        mBikeList = bikeList
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_map, container, false)
        mapView = view.findViewById(R.id.mapView)
        mapView!!.onCreate(savedInstanceState)
        mapView!!.getMapAsync(this)

        return view
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap!!
        markers = Markers(context!!, mMap, mBikeList[0])

        mMap.setOnCameraMoveListener(this)

        mMap.moveCamera(CameraUpdateFactory.newLatLng(KONKUK_UNIV))
        if (mLocationPermissionGranted) {
            fusedLocationClient.lastLocation.addOnSuccessListener {
                myLocation = it
                mMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(it.latitude, it.longitude),
                        DEFAULT_ZOOM
                    ), 500, null
                )
            }
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
        val bounds = mMap.projection.visibleRegion.latLngBounds
        for (bikeStop in mBikeList) {
            if (bounds.contains(LatLng(bikeStop.stationLatitude, bikeStop.stationLongitude))) {
                if (!visibleMarkers.containsKey(bikeStop.stationId)) {
                    visibleMarkers[bikeStop.stationId] = markers.setMarker(bikeStop)
                    visibleMarkers[bikeStop.stationId]!!.tag = bikeStop
                }
            }
        }

        mMap.setOnMarkerClickListener {
            val clickedMarkerTag = it.tag as MyBike
            Toast.makeText(
                context!!,
                "${clickedMarkerTag.stationName} ${clickedMarkerTag.parkingBikeTotCnt}",
                Toast.LENGTH_SHORT
            ).show()
            false
        }

    }

    override fun onCameraMove() {
        val bounds = mMap.projection.visibleRegion.latLngBounds
        Log.v("Camera Zoom", mMap.cameraPosition.zoom.toString())
        for (bikeStop in mBikeList) {
            if (bounds.contains(LatLng(bikeStop.stationLatitude, bikeStop.stationLongitude))) {
                if (!visibleMarkers.containsKey(bikeStop.stationId)) {
                    visibleMarkers[bikeStop.stationId] = markers.setMarker(bikeStop)
                    visibleMarkers[bikeStop.stationId]!!.tag = bikeStop
                }
            }
//            else {
//                if (visibleMarkers.containsKey(bikeStop.stationId)) {
//                    visibleMarkers[bikeStop.stationId]!!.remove()
//                    visibleMarkers.remove(bikeStop.stationId)
//                }
//            }
        }
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
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
