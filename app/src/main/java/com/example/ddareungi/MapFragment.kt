package com.example.ddareungi


import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.a190306app.MyBike
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import kotlinx.android.synthetic.main.fragment_map.*

const val DEFAULT_ZOOM = 16f

class MapFragment : Fragment(), OnMapReadyCallback {

    lateinit var mMap: GoogleMap
    var mapView: MapView? = null    //GoogleMap을 보여주는 MapView
    var mLocationPermissionGranted = false  //GPS 권환 획득 유무를 확인하는 flag 값
    lateinit var fusedLocationClient: FusedLocationProviderClient   //휴대폰이 마지막으로 얻은 내 위치를 얻어오기 위한 객체
    val KONKUK_UNIV = LatLng(37.540, 127.07)
    lateinit var mBikeList: MutableList<MyBike>
    val visibleMarkers = mutableMapOf<String, Marker>()
    lateinit var markers: Markers
    lateinit var myLocation: Location
    var hasMyLocation = false


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
        markers = Markers(context!!, mMap)
        visibleMarkers.clear()

        mMap.setMinZoomPreference(14f)
        mMap.setOnCameraMoveListener {
            addMarker()
        }

        if (mLocationPermissionGranted) {
            if (hasMyLocation) {
                mMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(myLocation.latitude, myLocation.longitude),
                        DEFAULT_ZOOM
                    )
                )
            } else {
                fusedLocationClient.lastLocation.addOnSuccessListener {
                    myLocation = it
                    mMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(it.latitude, it.longitude),
                            DEFAULT_ZOOM
                        )
                    )
                }
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
        mMap.setOnCameraIdleListener {
            addMarker()
        }

        mMap.setOnMarkerClickListener {
            map_refresh_fab.hide()
            val clickedMarkerTag = it.tag as MyBike
            mMap.animateCamera(CameraUpdateFactory.newLatLng(it.position), 500, null)
            map_card_view.visibility = View.VISIBLE
            map_card_title_text.text = clickedMarkerTag.stationName
            map_card_regular_text.text = "${clickedMarkerTag.parkingBikeTotCnt}대 사용 가능"
            true
        }

        mMap.setOnMapClickListener {
            map_card_view.visibility = View.GONE
            map_refresh_fab.show()
        }

    }

    fun addMarker() {
        val bounds = mMap.projection.visibleRegion.latLngBounds

        for (bikeStop in mBikeList) {
            if (bounds.contains(LatLng(bikeStop.stationLatitude, bikeStop.stationLongitude))) {
                if (!visibleMarkers.containsKey(bikeStop.stationId)) {
                    if (mMap.cameraPosition.zoom >= 15f) {
                        visibleMarkers[bikeStop.stationId] = markers.setMarker(bikeStop)
                        visibleMarkers[bikeStop.stationId]!!.tag = bikeStop
                    } else {
                        if (bikeStop.parkingBikeTotCnt > 0) {
                            visibleMarkers[bikeStop.stationId] = markers.setMarker(bikeStop)
                            visibleMarkers[bikeStop.stationId]!!.tag = bikeStop
                        }
                    }
                } else {
                    if(mMap.cameraPosition.zoom < 15f && bikeStop.parkingBikeTotCnt == 0){
                        visibleMarkers[bikeStop.stationId]!!.remove()
                        visibleMarkers.remove(bikeStop.stationId)
                    }
                }
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (arguments != null) {
            mLocationPermissionGranted = arguments!!.getBoolean("mLocationPermissionGranted")
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context!!)
        val refreshFab = view!!.findViewById<FloatingActionButton>(R.id.map_refresh_fab)
        refreshFab.setOnClickListener {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


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
