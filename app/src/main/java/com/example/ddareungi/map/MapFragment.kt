package com.example.ddareungi.map

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.support.design.internal.NavigationMenu
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.example.ddareungi.R
import com.example.ddareungi.bookmark.PlaceType
import com.example.ddareungi.data.Bike
import com.example.ddareungi.data.Park
import com.example.ddareungi.data.Toilet
import com.example.ddareungi.util.checkLocationPermission
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import io.github.yavski.fabspeeddial.FabSpeedDial
import java.util.*

class MapFragment() : Fragment(), MapContract.View, OnMapReadyCallback, FabSpeedDial.MenuListener {

    override lateinit var presenter: MapContract.Presenter

    lateinit var mapView: MapView
    lateinit var googleMap: GoogleMap
    var autoCompleteFragment: AutocompleteSupportFragment? = null

    lateinit var myLocationBtn: FloatingActionButton
    lateinit var refreshBtn: FloatingActionButton
    lateinit var placeTypeBtn: FabSpeedDial
    lateinit var bookmarkBtn: ImageButton
    lateinit var rentBtn: Button
    lateinit var bikePathBtn: Button
    lateinit var parkPathBtn: ImageButton
    lateinit var searchPathButton: ImageButton

    lateinit var bikeCardView: CardView
    lateinit var parkCardView: CardView
    lateinit var destCardView: CardView

    lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var markerController: MarkerController
    var searchMarker: Marker? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.map_frag, container, false)
        with(root)
        {
            mapView = findViewById(R.id.mapView)
            bikeCardView = findViewById(R.id.bike_card_view)
            parkCardView = findViewById(R.id.park_card_view)
            destCardView = findViewById(R.id.dest_card_view)

            myLocationBtn = (findViewById<FloatingActionButton>(R.id.my_location_button)).also {
                it.setOnClickListener { moveCameraToUser() }
            }
            refreshBtn = (findViewById<FloatingActionButton>(R.id.map_refresh_fab)).also {
                it.setOnClickListener { presenter.requestBikeDataUpdate() }
            }
            bookmarkBtn = (findViewById<ImageButton>(R.id.bookmark_button)).also {
                it.setOnClickListener { presenter.updateBookmarkState() }
            }
            bikePathBtn = (findViewById<Button>(R.id.path_button)).also {
                it.setOnClickListener { presenter.getUrlForNaverMap(R.id.path_button) }
            }
            rentBtn = (findViewById<Button>(R.id.rent_button)).also {
                it.setOnClickListener {showDdareungiWebPage() }
            }
            parkPathBtn = (findViewById<ImageButton>(R.id.park_card_path_button)).also {
                it.setOnClickListener { presenter.getUrlForNaverMap(R.id.park_card_path_button) }
            }
            searchPathButton = (findViewById<ImageButton>(R.id.dest_card_path_button)).also {
                it.setOnClickListener { presenter.getUrlForNaverMap(R.id.dest_card_path_button) }
            }
            placeTypeBtn = (findViewById<FabSpeedDial>(R.id.map_place_fab)).also { it.setMenuListener(this@MapFragment) }
        }

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        initAutocompleteFragment()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        return root
    }

    override fun onMapReady(map: GoogleMap?) {
        googleMap = map!!
        markerController = MarkerController(context!!, googleMap)

        with(googleMap) {

            googleMap.setMinZoomPreference(MIN_CAMERA_ZOOM)

            presenter.initCameraPosition()

            setOnCameraIdleListener {
                val bounds = projection.visibleRegion.latLngBounds
                val zoomLevel = cameraPosition.zoom
                presenter.updateMarkers(bounds, zoomLevel, false) }

            setOnCameraMoveListener {
                val bounds = projection.visibleRegion.latLngBounds
                val zoomLevel = cameraPosition.zoom
                presenter.updateMarkers(bounds, zoomLevel, false) }

            setOnMarkerClickListener {
                animateCamera(CameraUpdateFactory.newLatLng(it.position))
                showClickedMarkerCardView(it)
                true
            }

            setOnMapClickListener {
                presenter.currentClickedMarker = null
                hideMarkerCardView()
            }
        }
    }

    private fun initAutocompleteFragment() {
        val context = context ?: return
        Places.initialize(context, resources.getString(R.string.google_maps_key))
        val placesClient = Places.createClient(context)
        val placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
        val bounds = LatLngBounds(LatLng(37.413294, 126.734086), LatLng(37.715133, 127.269311))
        val rectBounds = RectangularBounds.newInstance(bounds)

        //Places.initialize(context, resources.getString(R.string.google_maps_key))
        autoCompleteFragment = childFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment?
        autoCompleteFragment!!.setHint("목적지 검색")
        autoCompleteFragment!!.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME))
        autoCompleteFragment!!.setLocationRestriction(rectBounds)

        autoCompleteFragment!!.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(p0: Place) {
                val request = FetchPlaceRequest.builder(p0.id.toString(), placeFields).build()
                placesClient.fetchPlace(request).addOnSuccessListener {
                    showSearchMarker(it.place)
                }
            }

            override fun onError(p0: Status) {
                Log.i("place search", "An error occurred: $p0")

            }

        })

    }

    override fun showMarkerOnCurrentMap(markersToShow: MutableMap<String, Any>, showKeyList: MutableList<String>, removeKeyList: MutableList<String>
    ) {
        when(presenter.currentPlaceType) {
            PlaceType.BIKE -> markerController.addBikeMarker(markersToShow as MutableMap<String, Bike>, showKeyList, removeKeyList)
            PlaceType.TOILET -> markerController.addToiletMarker(markersToShow as MutableMap<String, Toilet>, showKeyList)
            PlaceType.PARK -> markerController.addParkMarker(markersToShow as MutableMap<String, Park>, showKeyList)
            else -> {}
        }
    }

    private fun showClickedMarkerCardView(marker: Marker) {
        presenter.currentClickedMarker = marker
        destCardView.visibility = View.GONE

        if(marker.tag is Place)
            presenter.currentClickedMarkerType = PlaceType.SEARCH
        else
            presenter.currentClickedMarkerType = presenter.currentPlaceType


        if(presenter.currentClickedMarkerType == PlaceType.TOILET)
                marker.showInfoWindow()

        presenter.updateClickedMarkerCardView(marker)
    }

    override fun showBikeCardView(stationName: String, leftBikeNum: String, bookmarked: Boolean) {
        refreshBtn.hide()
        placeTypeBtn.hide()
        bikeCardView.visibility = View.VISIBLE

        with(requireActivity()) {
            findViewById<TextView>(R.id.station_name_text).text = stationName
            findViewById<TextView>(R.id.left_bike_num_text).text = leftBikeNum
        }
        if(bookmarked) {
            bookmarkBtn.setImageDrawable(requireContext().getDrawable(R.drawable.ic_star_black_24dp))
        } else {
            bookmarkBtn.setImageDrawable(requireContext().getDrawable(R.drawable.ic_star_border_black_24dp))
        }
    }

    override fun showParkCardView(name: String, dist: String) {
        refreshBtn.hide()
        placeTypeBtn.hide()
        parkCardView.visibility = View.VISIBLE

        with(requireActivity()) {
            findViewById<TextView>(R.id.park_name_text).text = name
            findViewById<TextView>(R.id.park_dist_text).text = dist
        }
    }

    override fun showDestCardView(name: String?, dist: String) {
        refreshBtn.hide()
        placeTypeBtn.hide()
        destCardView.visibility = View.VISIBLE

        with(requireActivity()) {
            findViewById<TextView>(R.id.dest_name_text).text = name
            findViewById<TextView>(R.id.dest_dist_text).text = dist
        }
    }

    override fun showUpdatedBikeMarker() {
        val bounds = googleMap.projection.visibleRegion.latLngBounds
        val zoomLevel = googleMap.cameraPosition.zoom
        presenter.updateMarkers(bounds, zoomLevel, true)
    }

    private fun showSearchMarker(place: Place) {
        if(searchMarker != null) {
            markerController.removeMarker(searchMarker!!.title)
        }
        searchMarker = markerController.addSearchMarker(place)
        presenter.currentClickedMarker = searchMarker
        presenter.currentClickedMarkerType = PlaceType.SEARCH
        moveCamera(searchMarker!!.position, DEFAUT_ZOOM)

        showClickedMarkerCardView(searchMarker!!)
    }

    private fun hideMarkerCardView() {
        bikeCardView.visibility = View.GONE
        parkCardView.visibility = View.GONE
        destCardView.visibility = View.GONE

        with(presenter) {
            if(currentClickedMarker != null)
                currentClickedMarker!!.hideInfoWindow()
            if(currentClickedMarkerType != PlaceType.SEARCH)
                currentClickedMarker = null
        }

        refreshBtn.show()
        placeTypeBtn.show()
    }

    override fun changeBookmarkState(bookmarked: Int) {
        if(bookmarked == 0) {
            bookmarkBtn.setImageDrawable(requireContext().getDrawable(R.drawable.ic_star_border_black_24dp))
        } else if(bookmarked == 1) {
            bookmarkBtn.setImageDrawable(requireContext().getDrawable(R.drawable.ic_star_black_24dp))
        }
    }

    override fun getUserLocation(callback: MapPresenter.GetUserLocationCallback) {
        var location: Location? = null

        if(checkLocationPermission()) {
            fusedLocationClient.lastLocation.addOnSuccessListener {
                if(it != null) {
                    location = it
                }
                else {
                    location = null
                }
                callback.onSuccess(location)
            }
        }
    }

    override fun moveCameraToUser() {
        if(checkLocationPermission()) {
            fusedLocationClient.lastLocation.addOnSuccessListener {
                if(it != null) {
                    moveCamera(LatLng(it.latitude, it.longitude), DEFAUT_ZOOM)
                } else {
                    moveCamera(DEFAULT_POS, -1f)
                }
            }
        }
    }

    override fun moveCameraByPos(lat: Double, lng: Double) {
        val pos = LatLng(lat, lng)
        val zoomLevel = DEFAUT_ZOOM
        moveCamera(pos, zoomLevel)
    }

    private fun moveCamera(pos: LatLng, zoomLevel: Float) {
        if(zoomLevel == -1f) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(pos))
        }
        else {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, zoomLevel))
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem?): Boolean {

        val bounds = googleMap.projection.visibleRegion.latLngBounds
        val zoomLevel = googleMap.cameraPosition.zoom

        when(menuItem!!.itemId) {
            R.id.bike_stop_fab -> presenter.currentPlaceType = PlaceType.BIKE
            R.id.toilet_fab -> presenter.currentPlaceType = PlaceType.TOILET
            R.id.park_fab -> presenter.currentPlaceType = PlaceType.PARK
        }
        googleMap.clear()
        markerController.visibleMarkers.clear()
        presenter.updateMarkers(bounds, zoomLevel, true)

        return false
    }

    override fun showPathInNaverMap(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addCategory(Intent.CATEGORY_BROWSABLE)
        val list: MutableList<ResolveInfo> =
            activity!!.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        if (list.isEmpty()) {
            //네이버 지도 앱이 깔려있지 않은 경우에 플레이 스토어로 연결
            context!!.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.nhn.android.nmap"))
            )
        } else {
            context!!.startActivity(intent)
        }
    }

    override fun showLoadingDataFailedDialog() {
        Toast.makeText(requireContext(), "데이터를 불러오는데 실패하였습니다", Toast.LENGTH_SHORT).show()
    }

    override fun showNoGpsDialog() {

    }

    fun onBackButtonPressed(): Boolean {
        if(bikeCardView.visibility == View.VISIBLE || destCardView.visibility == View.VISIBLE || parkCardView.visibility == View.VISIBLE) {
            bikeCardView.visibility = View.GONE
            parkCardView.visibility = View.GONE
            destCardView.visibility = View.GONE

            refreshBtn.show()
            placeTypeBtn.show()

            return true
        } else if(searchMarker != null) {
            markerController.removeMarker(searchMarker!!.title)
            searchMarker = null
            return true
        } else
            return false
    }

    private fun showDdareungiWebPage() {
        val ddareungiHome = Uri.parse("https://www.bikeseoul.com")
        val webIntent = Intent(Intent.ACTION_VIEW, ddareungiHome)
        startActivity(webIntent)
    }

    override fun onMenuClosed() {}

    override fun onPrepareMenu(navigationMenu: NavigationMenu?): Boolean { return true }


    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        (activity as AppCompatActivity).supportActionBar!!.hide()
    }


    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
        (activity as AppCompatActivity).supportActionBar!!.show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }


    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    companion object {
        const val DEFAUT_ZOOM = 15f
        val DEFAULT_POS = LatLng(37.540, 127.07)
        val MIN_CAMERA_ZOOM = 14f
    }

}

