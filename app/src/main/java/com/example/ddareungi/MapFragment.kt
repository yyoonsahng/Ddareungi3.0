package com.example.ddareungi


import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.support.v4.app.Fragment
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
import com.google.maps.android.clustering.ClusterManager

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
    lateinit var clusterManager: ClusterManager<MyBike>
    lateinit var myLocation: Location

//    companion object {
//        fun newMapFragment(locationPermissionGranted: Boolean, bikeList: MutableList<MyBike>): MapFragment {
//            val mapFragment = MapFragment()
//            mapFragment.mLocationPermissionGranted = locationPermissionGranted
//            mapFragment.mBikeList = bikeList
//            return mapFragment
//        }
//    }

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
//        clusterManager = ClusterManager(context!!, mMap)
//        clusterManager.renderer = CustomClusterRenderer(context, mMap, clusterManager)

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


        mMap.setOnCameraMoveListener(this)
        mMap.setOnMarkerClickListener {
            val clickedMarkerTag = it.tag as MyBike
            Toast.makeText(
                context!!,
                "${clickedMarkerTag.stationName} ${clickedMarkerTag.parkingBikeTotCnt}",
                Toast.LENGTH_SHORT
            ).show()
            false
        }
//        mMap.setOnCameraIdleListener(clusterManager)
//        clusterManager.setAnimation(false)
//
//        addItems(this.mBikeList)
//        clusterManager.cluster()

    }

//    fun addItems(bikeList: MutableList<MyBike>) {
//        for (bikeStop in bikeList) {
//            clusterManager.addItem(bikeStop)
//        }
//    }

    override fun onCameraMove() {
        val bounds = mMap.projection.visibleRegion.latLngBounds

        for (bikeStop in mBikeList) {
            if (bounds.contains(LatLng(bikeStop.stationLatitude, bikeStop.stationLongitude))) {
                if (!visibleMarkers.containsKey(bikeStop.stationId)) {
                    visibleMarkers[bikeStop.stationId] = markers.setMarker(bikeStop)
                    visibleMarkers[bikeStop.stationId]!!.tag = bikeStop
                }
            } else {
                if (visibleMarkers.containsKey(bikeStop.stationId)) {
                    visibleMarkers[bikeStop.stationId]!!.remove()
                    visibleMarkers.remove(bikeStop.stationId)
                }
            }
        }
    }

//    private class CustomClusterRenderer(context: Context?, map: GoogleMap?, clusterManager: ClusterManager<MyBike>?) :
//        DefaultClusterRenderer<MyBike>(context, map, clusterManager) {
//        val mContext = context
//        val mMap = map
//
//
//        override fun onBeforeClusterItemRendered(item: MyBike?, markerOptions: MarkerOptions?) {
//            //val markers = Markers(mContext!!, mMap!!, item!!)
//
//            if (isInBound(
//                    LatLng(item!!.stationLatitude, item.stationLongitude),
//                    mMap!!.projection.visibleRegion.latLngBounds
//                )
//            ) {
//                //marker의 위치 정보 설정
//                markerOptions!!.position(LatLng(item.stationLatitude, item.stationLongitude))
//                //marker에 해당하는 뷰 객체 얻어옴
//                val markerView = LayoutInflater.from(mContext).inflate(R.layout.bike_marker, null)
//                //markerView의 textView에 자전거 수 표시
//                markerView.bike_marker_num_textView.text = item.parkingBikeTotCnt.toString()
//
//                //자전거 수 별 다른 아이콘 이미지 설정, 자전거가 많을 수록 zIndex 값을 높게 설정해서 우선적으로 보이게 함
//                if (item.parkingBikeTotCnt == 0) {
//                    markerView.bike_marker_num_textView.setBackgroundResource(R.drawable.ic_marker_zero)
//                } else if (item.parkingBikeTotCnt < 10) {
//                    markerView.bike_marker_num_textView.setBackgroundResource(R.drawable.ic_marker_low)
//                    markerOptions.zIndex(1.0f)
//                } else {
//                    markerOptions.zIndex(2.0f)
//                }
//                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(mContext!!, markerView)))
//            }
//        }
//
//        fun isInBound(position: LatLng, bound: LatLngBounds): Boolean {
//            return bound.contains(position)
//        }
//
//
//        fun createDrawableFromView(context: Context, view: View): Bitmap {
//            val displayMetrics = DisplayMetrics()
//            (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
//            view.layoutParams =
//                ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
//            view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels)
//            view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels)
//            view.buildDrawingCache()
//            val bitmap = Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
//
//            val canvas = Canvas(bitmap)
//            view.draw(canvas)
//
//            return bitmap
//        }
//    }


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
