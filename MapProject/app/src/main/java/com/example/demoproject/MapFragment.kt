package com.example.demoproject


import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
    lateinit var mMap: GoogleMap
    var mapView: MapView? = null
    var mLocationPermissionGranted = false
    lateinit var fusedLocationClient: FusedLocationProviderClient
    //const val DEFAULT_ZOOM = 16f
    val KONKUK_UNIV = LatLng(37.540, 127.07)

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap!!
        //val behavior = BottomSheetBehavior.from(bottom_sheet)
        //behavior.isHideable = true

        if (mLocationPermissionGranted) {
            fusedLocationClient.lastLocation.addOnSuccessListener {
                mMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(it.latitude, it.longitude)))
            }

            mMap.isMyLocationEnabled = true
            mMap.setOnMyLocationButtonClickListener {
                Toast.makeText(context, "My Location button clicked", Toast.LENGTH_SHORT).show()
                fusedLocationClient.lastLocation.addOnSuccessListener {
                    mMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(it.latitude, it.longitude),
                            DEFAULT_ZOOM
                        ), 500, null
                    )
                }
                true
            }
        } else {
            mMap.isMyLocationEnabled = false
            mMap.moveCamera(CameraUpdateFactory.newLatLng(KONKUK_UNIV))
        }
        val mMapController = MapController(context!!, mMap)
        mMapController.setMarker()

        mMap.setOnMarkerClickListener {
            mMap.animateCamera(CameraUpdateFactory.newLatLng(it.position), 500, null)
//            behavior.state = BottomSheetBehavior.STATE_HIDDEN
            val stopMarkerItem: StopMarkerItem = it.tag as StopMarkerItem
            bottom_sheet.visibility = View.VISIBLE
            bottom_sheet_text_view.text = "${stopMarkerItem.id}. ${stopMarkerItem.name}"
//            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            true
        }

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
