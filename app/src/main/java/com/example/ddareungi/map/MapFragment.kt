package com.example.ddareungi.map


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.ddareungi.R
import com.example.ddareungi.data.BikeStation
import com.example.ddareungi.data.Park
import com.example.ddareungi.databinding.MapFragBinding
import com.example.ddareungi.utils.GpsUtils
import com.example.ddareungi.utils.setupSnackBar
import com.example.ddareungi.utils.NaverMapUtils
import com.example.ddareungi.utils.NetworkUtils
import com.example.ddareungi.viewmodel.BikeStationViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.google.android.gms.common.api.Status
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.internal.NavigationMenu
import io.github.yavski.fabspeeddial.FabSpeedDial
import kotlinx.android.synthetic.main.activity_main.*


class MapFragment : Fragment(), OnMapReadyCallback, FabSpeedDial.MenuListener {

    // MapFragment binding 객체
    private lateinit var binding: MapFragBinding

    // GoogleMap 객체
    private lateinit var googleMap: GoogleMap

    // 따릉이 정류소 ViewModel 객체
    private lateinit var bsViewModel: BikeStationViewModel

    // 지도 ViewModel 객체
    private lateinit var mapViewModel: MapViewModel

    // 정류소 마커 관련 기능 Controller
    private lateinit var markerController: MarkerController

    // 목적지 검색 fragment 객체
    private var autoCompleteFragment: AutocompleteSupportFragment? = null

    // 사용자 GPS 권한 획득 여부
    private var locationPermissionGranted = false



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        bsViewModel = activity?.run {
            ViewModelProviders.of(this)[BikeStationViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        mapViewModel = activity?.run {
            ViewModelProviders.of(this)[MapViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        binding = MapFragBinding.inflate(inflater, container, false)
            .apply {
                bsviewmodel = bsViewModel
                mapviewmodel = mapViewModel
                stationCardView.viewModel = mapViewModel
                parkCardView.setMapVM(mapViewModel)
                searchCardView.setMapVM(mapViewModel)
            }

        initAutocompleteFragment()

        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)

        binding.lifecycleOwner = this.viewLifecycleOwner

        mapViewModel.start(bsViewModel.bikeStations.value!!)

        setClickedStation()

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setUpBookmarkButton()

        setUpRefreshFab()

        setCardViewVisibility()

        setupSnackbar()

        setUpPathButtonInNaverMap()

        setUpRentButton()

        binding.mapPlaceFab.setMenuListener(this)

        Log.i("MapFragment", "visibility in onActivityCreated: ${binding.mapPlaceFab.visibility}")
    }

    override fun onMapReady(map: GoogleMap?) {
        googleMap = map!!

        markerController = MarkerController(requireContext(), mapViewModel)

        initCameraPosition()

        setUpMyLocationButton()

        with(googleMap) {

            // 최소 카메라 줌 제한
            setMinZoomPreference(MIN_CAMERA_ZOOM)

            // 카메라가 멈췄을 때 projection bound 안의 마커 업데이트
            setOnCameraIdleListener {
                val bound = projection.visibleRegion.latLngBounds

                if(mapViewModel.currentPlaceType.value == "Bike")
                    markerController.updateBikeStationMarkers(viewLifecycleOwner, googleMap, bound)
                else if(mapViewModel.currentPlaceType.value == "Park")
                    markerController.updateParkMarkers(viewLifecycleOwner, googleMap, bound)
            }

            // 카메라가 움직일 때 projection bound 안의 마커 업데이트
            setOnCameraMoveListener {
                val bound = projection.visibleRegion.latLngBounds

                if(mapViewModel.currentPlaceType.value == "Bike")
                    markerController.updateBikeStationMarkers(viewLifecycleOwner, googleMap, bound)
                else if(mapViewModel.currentPlaceType.value == "Park")
                    markerController.updateParkMarkers(viewLifecycleOwner, googleMap, bound)
            }

            // 마커 클릭 리스너 추가
            setOnMarkerClickListener {

                // 클릭한 마커를 중심으로 카메라 이동
                animateCamera(CameraUpdateFactory.newLatLng(it.position))

                // 검색 결과 마커가 없는 경우
                if(mapViewModel.searchedPlace.value == null) {

                    // 클릭한 정류소 정보를 viewModel에 전달
                    if (mapViewModel.currentPlaceType.value == "Bike") {
                        val station = it.tag as BikeStation
                        mapViewModel.setClickedStation(station)
                    }
                    else if (mapViewModel.currentPlaceType.value == "Park") {
                        val park = it.tag as Park
                        mapViewModel.setClickedPark(park, getUserLocationTask())
                    }
                }
                else {
                    // 검색 결과 마커가 있는 경우
                    if(it.tag is Place) {
                        val place = it.tag as Place
                        mapViewModel.setSearchedPlace(it, getUserLocationTask())
                        Log.i("MapFragment", "place : ${place.name}")
                    }
                    // 검색 결과 마커가 있고 다른 마커를 클릭한 경우
                    else {
                        if (mapViewModel.currentPlaceType.value == "Bike") {
                            val station = it.tag as BikeStation
                            mapViewModel.setClickedStation(station)
                        }
                        else if (mapViewModel.currentPlaceType.value == "Park") {
                            val park = it.tag as Park
                            mapViewModel.setClickedPark(park, getUserLocationTask())
                        }
                    }
                }
                    true
            }

            // 지도 클릭 리스너 추가
            setOnMapClickListener {
                if(mapViewModel.markerClicked.value!!) {
                    mapViewModel.setClickedAsNull()
                }
            }
        }

        // 대여소 실시간 정보를 새로 받아왔을 때 마커에 표시되는 정보 업데이트
        bsViewModel.bikeStations.observe(viewLifecycleOwner, Observer {
            if(it.isNotEmpty() && mapViewModel.currentPlaceType.value == "Bike") {
                mapViewModel.bikeStations = it
                val bound = googleMap.projection.visibleRegion.latLngBounds
                markerController.updateBikeStationMarkers(viewLifecycleOwner, googleMap, bound)
            }
        })

        // 마커 종류를 변경했을 때
        mapViewModel.currentPlaceType.observe(viewLifecycleOwner, Observer {
            googleMap.clear()

            if(it == "Bike") {
                mapViewModel.clearAddedStations()
                val bound = googleMap.projection.visibleRegion.latLngBounds
                markerController.updateBikeStationMarkers(viewLifecycleOwner, googleMap, bound)
            }
            else if(it == "Park") {
                mapViewModel.clearAddedParks()
                val bound = googleMap.projection.visibleRegion.latLngBounds
                markerController.updateParkMarkers(viewLifecycleOwner, googleMap, bound)
            }
        })

        if(!mapViewModel.focusOnMap.value!!) {
            binding.mapPlaceFab.hide()
        }
    }

    private fun setCardViewVisibility() {
        mapViewModel.markerClicked.observe(this, Observer {

            // 목적지 검색 결과가 없는 경우
            if(it && mapViewModel.searchedPlace.value == null) {
                val placeType = mapViewModel.currentPlaceType.value
                if(placeType == "Bike") {
                    binding.stationCardView.root.visibility = View.VISIBLE
                }
                else if(placeType == "Park") {
                    binding.parkCardView.root.visibility = View.VISIBLE
                }
            }
            else if(it && mapViewModel.searchedPlace.value != null) {
                // 목적지 검색 결과가 있고 해당 마커를 클릭한 경우
                if(mapViewModel.clickedStation.value == null && mapViewModel.clickedPark.value == null) {
                    binding.searchCardView.root.visibility = View.VISIBLE
                }
                else if(mapViewModel.clickedStation.value != null) {
                    binding.stationCardView.root.visibility = View.VISIBLE
                    binding.searchCardView.root.visibility = View.GONE
                }
                else if(mapViewModel.clickedPark.value != null) {
                    binding.parkCardView.root.visibility = View.VISIBLE
                    binding.searchCardView.root.visibility = View.GONE
                }
            }
            else if(!it){
                binding.stationCardView.root.visibility = View.GONE
                binding.parkCardView.root.visibility = View.GONE
                binding.searchCardView.root.visibility = View.GONE
            }
        })
    }

    private fun setUpRefreshFab() {

        binding.setFabClickListener {
            // 네트워크 연결 있을 때만 정류소 실시간 정보 요청
            if(NetworkUtils.isNetworkAvailable(requireContext())) {
                mapViewModel.clearAddedStations()
                bsViewModel.refresh()
            } else {
                //show no network snack bar
                bsViewModel.showSnackbarMessage("현재 네트워크 연결이 없습니다.")
            }
        }
    }

    private fun setUpMyLocationButton() {

        // GoogleMap의 기본 내 위치 버튼 비활성화
        googleMap.uiSettings.isMyLocationButtonEnabled = false

        if(locationPermissionGranted) {
            // my-location layer 활성화 (지도 상에서 내 위치에 파란 점으로 보여주는 layer)
            googleMap.isMyLocationEnabled = true
        }

        binding.myLocationButton.setOnClickListener {
            // GPS 권한 없으면 다시 요청
            if(!locationPermissionGranted) {
                requestLocationPermission()
            }
            else {
                if(!GpsUtils.isDeviceGpsTurnOn(requireContext())) {

                } else {
                    val fusedLocationClient
                            = LocationServices.getFusedLocationProviderClient(requireActivity())

                    // 사용자 위치에 대한 캐쉬가 있으면 사용자 위치로 이동
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener {
                            if (it != null) {
                                googleMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(it.latitude, it.longitude)))
                            }
                        }
                }
            }
        }
    }

    private fun getUserLocationTask(): Task<Location>? {
        if(locationPermissionGranted) {
            if(GpsUtils.isDeviceGpsTurnOn(requireContext())) {
                val fusedLocationClient
                        = LocationServices.getFusedLocationProviderClient(requireActivity())

                return fusedLocationClient.lastLocation
            }
        }
        return null
    }

    /**
     * 카메라 위치 초기화
     */
    private fun initCameraPosition() {
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // 기본 위치는 시청으로 설정
        var posLatLng = DEFAULT_POS

        // [BookmarkFragment]에서 클릭해서 넘어온 경우 해당 정류소 위치로 카메라를 이동
        if(mapViewModel.markerClicked.value!! && mapViewModel.currentPlaceType.value == "Bike") {
            val station = mapViewModel.clickedStation.value!!
            posLatLng = LatLng(station.stationLatitude, station.stationLongitude)
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(posLatLng))
        }
        // 사용자 위치 정보가 있으면 사용자 위치로, 없으면 기본 위치로 카메라 이동
        else {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                if (it != null) {
                    posLatLng = LatLng(it.latitude, it.longitude)
                }
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(posLatLng))
            }
        }
    }

    private fun initAutocompleteFragment() {
        val context = context ?: return

        Places.initialize(context, resources.getString(R.string.google_maps_key))
        val placesClient = Places.createClient(context)
        val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)

        // 목적지 검색 결과를 서울로 한정
        val bounds = LatLngBounds(LatLng(37.413294, 126.734086), LatLng(37.715133, 127.269311))
        val rectBounds = RectangularBounds.newInstance(bounds)

        autoCompleteFragment =
            childFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment?
        autoCompleteFragment!!.setHint("목적지 검색")
        autoCompleteFragment!!.setPlaceFields(placeFields)
        autoCompleteFragment!!.setLocationRestriction(rectBounds)

        autoCompleteFragment!!.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(p0: Place) {
                val request = FetchPlaceRequest.builder(p0.id.toString(), placeFields).build()
                placesClient.fetchPlace(request).addOnSuccessListener {

                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(it.place.latLng))

                    val marker = markerController.addSearchedMarker(it.place, googleMap)
                    mapViewModel.setSearchedPlace(marker, getUserLocationTask())
                }
            }

            override fun onError(p0: Status) {
                Log.i("place search", "An error occurred: $p0")

            }

        })
    }

    private fun showPathInNaverMap(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addCategory(Intent.CATEGORY_BROWSABLE)

        val list: MutableList<ResolveInfo> =
            activity!!.packageManager.queryIntentActivities(
                intent,
                PackageManager.MATCH_DEFAULT_ONLY
            )
        if (list.isEmpty()) {
            //네이버 지도 앱이 깔려있지 않은 경우에 플레이 스토어로 연결
            context!!.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.nhn.android.nmap"))
            )
        } else {
            context!!.startActivity(intent)
        }
    }

    private fun setUpPathButtonInNaverMap() {

        binding.stationCardView.setPathClickListener {
            val currStation = mapViewModel.clickedStation.value!!

            showPathInNaverMap(NaverMapUtils.getUrlForBikeStation(currStation))
        }

        binding.parkCardView.setPathClickListener {
            val currPark = mapViewModel.clickedPark.value!!
            val closetBikeStation = mapViewModel.findClosetBikeStation(currPark.latitude, currPark.longitude)

            showPathInNaverMap(NaverMapUtils.getUrlForPark(currPark, closetBikeStation!!))
        }

        binding.searchCardView.setPathClickListener {
            val searchedPlace = mapViewModel.searchedPlace.value!!.tag as Place
            val closestBikeStation
                    = mapViewModel.findClosetBikeStation(searchedPlace.latLng!!.latitude, searchedPlace.latLng!!.longitude)

            showPathInNaverMap(NaverMapUtils.getUrlForPlace(searchedPlace, closestBikeStation!!))
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem?): Boolean {

        when (menuItem!!.itemId) {
            R.id.bike_station_fab -> {
                mapViewModel.setPlaceType("Bike")
            }
            R.id.park_fab -> {
                mapViewModel.setPlaceType("Park")
            }
        }

        return false
    }

    private fun setUpRentButton() {
        binding.stationCardView.setRentClickListener {
            val ddareungiHome =
                Uri.parse("https://www.bikeseoul.com/app/station/moveStationRealtimeStatus.do")
            val webIntent = Intent(Intent.ACTION_VIEW, ddareungiHome)
            startActivity(webIntent)
        }
    }

    override fun onPrepareMenu(p0: NavigationMenu?): Boolean { return true }

    override fun onMenuClosed() { }

    private fun setUpBookmarkButton() {
        binding.stationCardView.setBookmarkClickListener {
            mapViewModel.changeBookmarkState(bsViewModel)
        }
    }

    private fun setClickedStation() {
        val stationId = arguments?.getString(CLICKED_IN_BOOKMARK_FRAG_TAG)
        if(stationId != null) {
            mapViewModel.setClickedStationWithId(stationId)
        }
    }



    private fun setupSnackbar() {
        view?.setupSnackBar(this, bsViewModel.snackbarText, Snackbar.LENGTH_LONG)
    }

    fun onBackPressed(): Boolean {
        // 클릭한 마커가 있어 보여지고 있는 CardView가 있는 경우, 지도를 클릭한 경우와 같은 효과
        if(mapViewModel.markerClicked.value!!) {
            mapViewModel.setClickedAsNull()

            return true
        }
        else {
            // 검색 결과 마커를 지도에서 제거
            if (mapViewModel.searchedPlace.value != null) {
                mapViewModel.setSearchedPlaceAsNull()

                return true
            }
            return false
        }

    }

    private fun checkLocationPermission(): Boolean {
        return (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestLocationPermission() {
        val requestPermission = (arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION))
        val requestCode = MY_LOCATION_REQUEST_CODE

        ActivityCompat.requestPermissions(requireActivity(), requestPermission, requestCode)
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()

        // [MapFragment]에서는 상단의 액션바를 숨김
        (activity as AppCompatActivity).supportActionBar!!.hide()

        (activity as AppCompatActivity).bottom_nav_view.apply {
            menu.findItem(R.id.map).isChecked = true
        }

        locationPermissionGranted = checkLocationPermission()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()

        // [MapFragment] 외에는 상단 액션바를 다시 보여줌
        (activity as AppCompatActivity).supportActionBar!!.show()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    companion object {
        const val MY_LOCATION_REQUEST_CODE = 1
        const val CLICKED_IN_BOOKMARK_FRAG_TAG = "clicked station in bookmark frag"
        const val MIN_CAMERA_ZOOM = 14f
        val DEFAULT_POS = LatLng(37.566414, 126.977912)

        fun newInstance(stationId: String?): MapFragment {
            return if(stationId == null) {
                MapFragment()
            } else {
                val fragment = MapFragment()
                val args = Bundle()
                args.putString(CLICKED_IN_BOOKMARK_FRAG_TAG, stationId)
                fragment.arguments = args

                fragment
            }
        }

    }

}
