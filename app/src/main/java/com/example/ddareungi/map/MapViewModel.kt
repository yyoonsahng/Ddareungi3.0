package com.example.ddareungi.map

import android.app.Application
import android.location.Location
import android.util.Log
import androidx.lifecycle.*
import com.example.ddareungi.BasicApp
import com.example.ddareungi.DataRepository
import com.example.ddareungi.Event
import com.example.ddareungi.data.BikeStation
import com.example.ddareungi.data.Park
import com.example.ddareungi.data.Result
import com.example.ddareungi.viewmodel.BikeStationViewModel
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.model.Place
import kotlinx.coroutines.launch

class MapViewModel(private val mRepository: DataRepository) : ViewModel() {

    // 사용자가 선택한 대여소 객체
    private val mClickedStation = MutableLiveData<BikeStation>().apply { value = null }
    val clickedStation: LiveData<BikeStation> = mClickedStation

    // 지도에 추가되어 있는 마커에 해당하는 대여소 객체를 저장하고 있는 해쉬 맵
    private val addedStations = mutableMapOf<String, BikeStation>()

    private val mUpdateStations = MutableLiveData<Event<List<BikeStation>>>()
    val updateStations: LiveData<Event<List<BikeStation>>> = mUpdateStations

    // 사용자가 클릭한 대여소가 북마크 되어 있는지 여부를 확인하기 위한 변수
    val bookmarked = Transformations.map(clickedStation) {
        it?.bookmarked
    }

    // 전체 공원 데이터를 담고 있는 리스트
    private val mParkList = MutableLiveData<List<Park>>().apply { value = emptyList() }

    // 사용자가 마커를 클릭한 공원 객체
    private val mClickedPark = MutableLiveData<Park>().apply { value = null }
    val clickedPark: LiveData<Park> = mClickedPark

    // 지도에 추가되어 있는 마커에 해당하는 공원 객체를 저장하고 있는 해쉬 맵
    private val addedParks = mutableMapOf<Int, Park>()

    // 사용자가 지도를 움직일 때 새롭게 추가해야 할 공원 리스트를 업데이트하기 위한 이벤트
    private val mUpdateParks = MutableLiveData<Event<List<Park>>>()
    val updateParks: LiveData<Event<List<Park>>> = mUpdateParks

    private val mSearchedPlace = MutableLiveData<Marker>().apply { value = null }
    val searchedPlace: LiveData<Marker> = mSearchedPlace

    private val mDistToPlace = MutableLiveData<Float>().apply { value = 0f }
    val distToPlace: LiveData<Float> = mDistToPlace

    // 현재 사용자가 보고 있는 장소 정보(따릉이 대여소, 공원)
    private val mCurrentPlaceType = MutableLiveData<String>().apply { value = "Bike" }
    val currentPlaceType: LiveData<String> = mCurrentPlaceType

    // 사용자가 마커를 클릭한 상태인지를 저장하기 위한 변수
    private val mMarkerClicked = MutableLiveData<Boolean>().apply { value = false }
    val markerClicked: LiveData<Boolean> = mMarkerClicked

    // 사용자가 클릭한 마커가 없을 때
    private val mFocusOnMap = MutableLiveData<Boolean>().apply { value = true }
    val focusOnMap: LiveData<Boolean> = mFocusOnMap

    lateinit var bikeStations: List<BikeStation>

    fun start(bikeStations: List<BikeStation>) {
        this.bikeStations = bikeStations
        addedStations.clear()
        viewModelScope.launch {
            getParkList()
        }
    }

    /**
     * 사용자 지도의 projection [bound] 안에 새롭게 추가된 대여소를 탐색 및 업데이트
     */
    fun updateVisibleStations(bound: LatLngBounds) {
        val updateStationsArr = arrayListOf<BikeStation>()

        for (station in bikeStations) {
            val latLng = LatLng(station.stationLatitude, station.stationLongitude)
            if (bound.contains(latLng) && !addedStations.containsKey(station.stationId)) {
                updateStationsArr.add(station)
                addedStations[station.stationId] = station
            }
        }
        mUpdateStations.value = Event(updateStationsArr)
    }

    /**
     * 사용자 지도의 projection [bound] 안에 새롭게 추가된 공원을 탐색 및 업데이트
     */
    fun updateVisibleParks(bound: LatLngBounds) {
        val updatedParksArr = arrayListOf<Park>()

        for (park in mParkList.value!!.iterator()) {
            val latLng = LatLng(park.latitude, park.longitude)
            if (bound.contains(latLng) && !addedParks.containsKey(park.id)) {
                updatedParksArr.add(park)
                addedParks[park.id] = park
            }
        }
        mUpdateParks.value = Event(updatedParksArr)
    }

    /**
     * 지도 상에 추가되어 있는 대여소 리스트 초기화
     */
    fun clearAddedStations() {
        addedStations.clear()
    }

    /**
     * 지도 상에 추가되어 있는 공원 리스트 초기화
     */
    fun clearAddedParks() {
        addedParks.clear()
    }

    /**
     * 사용자가 클릭한 마커에 해당하는 [station]을 set
     */
    fun setClickedStation(station: BikeStation?) {
        mClickedStation.value = station
        mFocusOnMap.value = false
        mMarkerClicked.value = true
    }

    /**
     * [BookmarkFragment]에서 정류소 리스트를 클릭하여 [MapFramgent]로 전환되는 경우,
     * 넘겨 받은 [stationId]로 해당하는 정류소 객체를 탐색
     */
    fun setClickedStationWithId(stationId: String) {
        var station: BikeStation? = null

        for(s in bikeStations) {
            if(s.stationId == stationId) {
                station = s
                break
            }
        }
        setClickedStation(station)
    }

    /**
     * 선택한 정류소의 viewModel인 [bsViewModel]에 대해서 북마크를 추가, 삭제
     */
    fun changeBookmarkState(bsViewModel: BikeStationViewModel) {
        val station = clickedStation.value

        if(station != null) {
            if(station.bookmarked) {
                bsViewModel.deleteBookmarkStation(station.stationId)
            } else {
                bsViewModel.addBookmarkStation(station.stationId, station.stationName)
            }
            station.bookmarked = !station.bookmarked

            mClickedStation.value = station
        }
    }

    /**
     * 공원 정보를 받아오는 메소드
     */
    private suspend fun getParkList() {

        viewModelScope.launch {
            val parkListResult = mRepository.getParks()

            if(parkListResult is Result.Success) {
                mParkList.value = parkListResult.data
            }
            else if(parkListResult is Result.Error) {
                Log.i("MapViewModel", "Error while getting park list")
            }
        }
    }

    /**
     * 검색 결과를 [mSearchedPlace]에 저장, 포커스를 마커로 지정
     */
    fun setSearchedPlace(marker: Marker, userLocTask: Task<Location>?) {
        mFocusOnMap.value = false

        if(userLocTask == null) {
            mDistToPlace.value = -1.0f
            mSearchedPlace.value = marker
            mMarkerClicked.value = true
        }

        userLocTask?.addOnSuccessListener {
            if(it != null) {
                val place = marker.tag as Place
                val dest = Location("dest")
                dest.latitude = place.latLng!!.latitude
                dest.longitude = place.latLng!!.longitude

                val dist = it.distanceTo(dest) / 1000
                mDistToPlace.value = dist
            }
            else {
                mDistToPlace.value = -1.0f
            }
            mSearchedPlace.value = marker
            mMarkerClicked.value = true
        }
    }

    /**
     * [mSearchedPlace]을 null로 초기화, 검색 결과에 대한 마커 제거
     */
    fun setSearchedPlaceAsNull() {
        mSearchedPlace.value?.remove()
        mSearchedPlace.value = null
    }

    /**
     * 사용자가 선택한 마커 종류 지정
     */
    fun setPlaceType(type: String) {
        mCurrentPlaceType.value = type
    }

    /**
     * 사용자가 선택한 마커에 해당하는 공원 데이터 저장 및 사용자 위치 이용해 거리 계산
     */
    fun setClickedPark(park: Park, userLocTask: Task<Location>?) {
        mFocusOnMap.value = false

        if(userLocTask == null) {
            park.dist = -1.0f
            mClickedPark.value = park
            mMarkerClicked.value = true
        }

        userLocTask?.addOnSuccessListener {
            if(it != null) {
                val dest = Location("dest")
                dest.latitude = park.latitude
                dest.longitude = park.longitude

                val dist = it.distanceTo(dest) / 1000
                park.dist = dist
            }
            else {
                park.dist = -1.0f
            }
            mClickedPark.value = park
            mMarkerClicked.value = true
        }
    }

    /**
     * 현재 마커 종류에 따라 클릭되어 있던 마커에 해당하는 데이터 null로 초기화, 포커스를 지도로 변경
     */
    fun setClickedAsNull() {
        if (mCurrentPlaceType.value == "Bike") {
            mClickedStation.value = null
        }
        else if(mCurrentPlaceType.value == "Park") {
            mClickedPark.value = null
        }

        mMarkerClicked.value = false
        mFocusOnMap.value = true
    }

    /**
     * [lat], [lng]으로부터 가장 가까운 대여소 탐색
     */
    fun findClosetBikeStation(lat: Double, lng: Double): BikeStation? {
        val dest = Location("dest")
        dest.latitude = lat
        dest.longitude = lng

        var closestStation: BikeStation? = null
        var dist = Float.MAX_VALUE

        for (station in bikeStations) {
            val stationLoc = Location("bike")
            stationLoc.latitude = station.stationLatitude
            stationLoc.longitude = station.stationLongitude

            var tempDist: Float = dest.distanceTo(stationLoc)
            if(dist > tempDist) {
                dist = tempDist
                closestStation = station
            }
        }
        return closestStation
    }

    companion object {
        class Factory(application: Application)
            : ViewModelProvider.NewInstanceFactory() {

            private val mRepository = (application as BasicApp).getDataRepository()

            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return MapViewModel(mRepository) as T
            }
        }
    }
}