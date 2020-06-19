package com.example.ddareungi.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.ddareungi.BasicApp
import com.example.ddareungi.DataRepository
import com.example.ddareungi.Event
import com.example.ddareungi.data.BikeStation
import com.example.ddareungi.data.BookmarkStation
import com.example.ddareungi.data.Result.Success
import kotlinx.coroutines.launch

class BikeStationViewModel(private val mRepository: DataRepository): ViewModel() {

    // 모든 따릉이 정류소 객체를 저장하는 리스트
    private val mBikeStations = MutableLiveData<List<BikeStation>>().apply { value = emptyList() }
    val bikeStations: LiveData<List<BikeStation>> = mBikeStations

    // 사용자가 북마크한 정류소 객체 리스트
    private val mBookmarkStations = MutableLiveData<List<BikeStation>>().apply { value = emptyList() }
    val bookmarkStations: LiveData<List<BikeStation>> = mBookmarkStations

    // 네트워크에서 데이터 가져오고 있는지 상태 정보
    private var mDataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = mDataLoading

    // 북마크한 정류소가 하나도 없는지에 대한 상태 정보
    val empty: LiveData<Boolean> = Transformations.map(mBookmarkStations) {
        it.isEmpty()
    }

    // 사용자가 북마크한 정류소 리스트를 클릭하여 [MapFragment]로 넘어가는 지에 대한 [Event]
    private val mNavigateToMapEvent = MutableLiveData<Event<String>>()
    val navigateToMapEvent: LiveData<Event<String>> = mNavigateToMapEvent

    // [SnackBar] 메세지를 포함한 [Event]
    private val mSnackbarText = MutableLiveData<Event<String>>()
    val snackbarText: LiveData<Event<String>> = mSnackbarText

    init {
        loadBikeStations()
    }

    /**
     * 모든 따릉이 정류소에 대한 실시간 정보 및 북마크한 정류소를 가져오기 위한 메소드
     */
    private fun loadBikeStations() {
        mDataLoading.value = true

        viewModelScope.launch {
            val bikeStationResult = mRepository.getBikeStations()
            val bookmarkStationResult = mRepository.getBookmarkStations()

            // 정류소 실시간 정보 및 북마크한 정류소 정보 요청 모두 성공한 경우
            if(bikeStationResult is Success && bookmarkStationResult is Success) {
                val bikeStations = bikeStationResult.data
                val bookmarkStationIds = bookmarkStationResult.data

                val bookmarkStations = arrayListOf<BikeStation>()

                for(station in bikeStations) {
                    if(bookmarkStationIds.contains(BookmarkStation(station.stationId, station.stationName))) {
                        bookmarkStations.add(station)
                    }
                }

                mBikeStations.value = bikeStations
                mBookmarkStations.value = bookmarkStations

            } else if(bikeStationResult is Success){    // 북마크한 정류소 정보 요청 실패한 경우
                mBikeStations.value = bikeStationResult.data
//                mBookmarkStations.value = emptyList()
                //show I/O error snack bar
                showSnackbarMessage("오류가 발생했습니다")

            } else if(bookmarkStationResult is Success) {   // 정류소 실시간 정보 요청 실패한 경우
                val bookmarkStationIds = bookmarkStationResult.data
                val bookmarkStations = arrayListOf<BikeStation>()

                for(id in bookmarkStationIds) {
                    bookmarkStations.add(BikeStation(id.stationId, id.stationName))
                }
                mBookmarkStations.value = bookmarkStations
                //show network error snack bar다
                showSnackbarMessage("네트워크 상태를 확인해주세요")

            } else {
//                mBikeStations.value = emptyList()
//                mBookmarkStations.value = emptyList()
            }

            // 데이터 로딩 종료 상태로 변경
            mDataLoading.value = false
        }
    }

    /**
     * 북마크한 정류소 정보를 업데이트하기 위한 메소드
     */
    private fun updateBookmarkStations() = viewModelScope.launch {
        val bookmarkStationResult = mRepository.getBookmarkStations()

        if(bookmarkStationResult is Success) {
            val bikeStations = mBikeStations.value!!
            val bookmarkStationIdAndNames = bookmarkStationResult.data
            val bookmarkStations = arrayListOf<BikeStation>()

            // 가지고 있는 정류소 정보가 없는 경우
            if(bikeStations.isEmpty()) {
                for(id in bookmarkStationIdAndNames) {
                    bookmarkStations.add(BikeStation(id.stationId, id.stationName))
                }
            } else {    // 가지고 있는 정류소 정보가 있는 경우
                for ((i, station) in bikeStations.withIndex()) {

                    // [bikeStations] 리스트에 북마크 상태를 업데이트
                    if (bookmarkStationIdAndNames.contains(
                            BookmarkStation(station.stationId, station.stationName)
                        )) {
                        bookmarkStations.add(station)
                        station.bookmarked = true
                    }
                    else {
                        station.bookmarked = false
                    }
                }
            }

            mBikeStations.value = bikeStations
            mBookmarkStations.value = bookmarkStations

        } else {
            mBookmarkStations.value = emptyList()

            //show I/O error snack bar
            showSnackbarMessage("오류가 발생했습니다")
        }
    }

    /**
     * 북마크한 정류소의 [stationId]와 [stationName] 정보를 데이터베이스에 저장
     */
    fun addBookmarkStation(stationId: String, stationName: String) = viewModelScope.launch {
        mRepository.addBookmarkStation(stationId, stationName)

        // viewModel에 업데이트된 북마크 정보 반영
        updateBookmarkStations()
    }

    /**
     * [stationId]를 id로 가지는 정류소를 데이터베이스에서 제거
     */
    fun deleteBookmarkStation(stationId: String) = viewModelScope.launch {
        mRepository.deleteBookmarkStation(stationId)

        // viewModel에 업데이트된 북마크 정보 반영
        updateBookmarkStations()
    }

    /**
     * [bookmarkStations]의 리스트의 [position] 인덱스 위치의 정류소를 데이터베이스에서 제거
     */
    fun deleteBookmarkStation(position: Int) = viewModelScope.launch {
        val stationId = bookmarkStations.value?.get(position)!!.stationId
        mRepository.deleteBookmarkStation(stationId)

        // viewModel에 업데이트된 북마크 정보 반영
        updateBookmarkStations()
    }


    fun refresh() {
        loadBikeStations()
    }

    /**
     * 사용자가 클릭한 정류소로 [MapFragment]로 전환하는 [Event]를 발생시키는 메소드
     * [Event]에 클릭한 정류소의 [stationId]를 같이 전달
     */
    fun showClickedStationInMap(stationId: String) {
        mNavigateToMapEvent.value = Event(stationId)
    }

    fun showSnackbarMessage(message: String) {
        mSnackbarText.value = Event(message)
    }

    companion object {
        class Factory(application: Application)
            : ViewModelProvider.NewInstanceFactory() {

            private val mRepository = (application as BasicApp).getDataRepository()

            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return BikeStationViewModel(mRepository) as T
            }
        }
    }
}